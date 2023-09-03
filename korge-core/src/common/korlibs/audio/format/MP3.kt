@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package korlibs.audio.format

import korlibs.audio.sound.AudioSamples
import korlibs.audio.sound.AudioSamplesDeque
import korlibs.audio.sound.AudioStream
import korlibs.datastructure.ByteArrayDeque
import korlibs.datastructure.DoubleArrayList
import korlibs.datastructure.binarySearch
import korlibs.encoding.*
import korlibs.ffi.WASMLib
import korlibs.time.TimeSpan
import korlibs.time.measureTimeWithResult
import korlibs.time.microseconds
import korlibs.time.seconds
import korlibs.io.compression.deflate.ZLib
import korlibs.io.compression.uncompress
import korlibs.io.lang.*
import korlibs.io.stream.*
import korlibs.math.*
import korlibs.memory.*

object MP3 : AudioFormat("mp3") {
	override suspend fun tryReadInfo(data: AsyncStream, props: AudioDecodingProps): Info? {
        try {
            val header = data.readBytesExact(4)
            val h0 = header.toUByteArray()[0].toInt()
            val h1 = header.toUByteArray()[1].toInt()
            val h2 = header.toUByteArray()[2].toInt()

            val isId3 = header.readStringz(0, 3) == "ID3"
            val isSync = (h0 == 0xFF) &&
                (((h1 and 0xF0) == 0xF0) || ((h1 and 0xFE) == 0xE2)) &&
                (h1.extract2(1) != 0) &&
                (h2.extract4(4) != 15) &&
                (h2.extract2(2) != 3)

            if (!isId3 && !isSync) return null

            val parser = Parser(data, data.getLength())
            val (duration, decodingTime) = measureTimeWithResult {
                when (props.exactTimings) {
                    null -> parser.getDurationExact() // Try to guess what's better based on VBR?
                    true -> parser.getDurationExact()
                    else -> parser.getDurationEstimate()
                }
            }
            return Info(duration, parser.info?.channelMode?.channels ?: 2, decodingTime)
        } catch (e: Throwable) {
            //e.printStackTrace()
            return null
        }
    }

    class SeekingTable(
        val microseconds: DoubleArrayList,
        val filePositions: DoubleArrayList,
        val rate: Int = 44100
    ) {
        val lengthTime: TimeSpan get() = microseconds[microseconds.size - 1].microseconds
        val lengthSamples: Long get() = (lengthTime.seconds * rate).toLong()

        fun locate(time: TimeSpan): Long {
            val searchMicro = time.microseconds
            val result = microseconds.binarySearch(searchMicro)
            return filePositions[result.nearIndex].toLong()
        }

        fun locateSample(sample: Long): Long {
            return locate((sample.toDouble() / rate).seconds)
        }
    }

	class Parser(val data: AsyncStream, val dataLength: Long) {
		var info: Mp3Info? = null

		//Read first mp3 frame only...  bind for CBR constant bit rate MP3s
		suspend fun getDurationEstimate() = _getDuration(use_cbr_estimate = true)

		suspend fun getDurationExact() = _getDuration(use_cbr_estimate = false)

        suspend fun getSeekingTable(rate: Int = 44100): SeekingTable {
            val times = DoubleArrayList()
            val filePositions = DoubleArrayList()
            _getDuration(use_cbr_estimate = false, emit = { filePos, totalMicro, info ->
                times.add(totalMicro)
                filePositions.add(filePos.toDouble())
            })
            return SeekingTable(times, filePositions, rate)
        }

		//Read entire file, frame by frame... ie: Variable Bit Rate (VBR)
		private suspend fun _getDuration(use_cbr_estimate: Boolean, emit: ((filePosition: Long, totalMicroseconds: Double, info: Mp3Info) -> Unit)? = null): TimeSpan {
			data.position = 0
			val fd = data.duplicate()
            val len = fd.getLength()

			var durationMicroseconds = 0.0
			val offset = this.skipID3v2Tag(fd.readStream(100))
            var pos = offset

			var info: Mp3Info? = null

            var nframes = 0
            val block2 = UByteArrayInt(ByteArray(10))

            val fdbase = fd.base
            val fdsync = fdbase.toSyncOrNull()

            var nreads = 0
            var nskips = 0
            var nasync = 0

            //println("fdbase: $fdbase")

            try {
                while (pos < len) {
                    val block2Size = when {
                        fdsync != null -> fdsync.read(pos, block2.bytes, 0, 10)
                        else -> {
                            nasync++
                            fd.position = pos
                            fd.readBytesUpTo(block2.bytes, 0, 10)
                        }
                    }
                    nreads++
                    if (block2Size < 10) break
                    pos += block2Size

                    when {
                        block2[0] == 0xFF && ((block2[1] and 0xe0) != 0) -> {
                            val framePos = fd.position
                            info = parseFrameHeader(block2)
                            emit?.invoke(framePos, durationMicroseconds, info)
                            nframes++
                            //println("FRAME: $nframes")
                            this.info = info
                            if (info.frameSize == 0) return durationMicroseconds.microseconds
                            pos += info.frameSize - 10
                            durationMicroseconds += (info.samples * 1_000_000L) / info.samplingRate
                        }
                        block2.bytes.openSync().readString(3) == "TAG" -> {
                            pos += 128 - 10 //skip over id3v1 tag size
                        }
                        else -> {
                            pos -= 9
                            nskips++
                        }
                    }

                    if ((info != null) && use_cbr_estimate) {
                        return estimateDuration(info.bitrate, info.channelMode.channels, offset.toInt()).microseconds
                    }
                }
            } finally {
                //println("MP3.Parser._getDuration: nreads=$nreads, nskips=$nskips, nasync=$nasync")
                //printStackTrace()
            }
			return durationMicroseconds.microseconds
		}

		private suspend fun estimateDuration(bitrate: Int, channels: Int, offset: Int): Long {
			val kbps = (bitrate * 1_000) / 8
			val dataSize = dataLength - offset
			return dataSize * (2 / channels) * 1_000_000L / kbps
		}

		private suspend fun skipID3v2Tag(block: AsyncStream): Long {
			val b = block.duplicate()

			if (b.readString(3, Charsets.LATIN1) == "ID3") {
                val bb = b.readBytesExact(7)
				val id3v2_major_version = bb.readU8(0)
				val id3v2_minor_version = bb.readU8(1)
				val id3v2_flags = bb.readU8(2)
				val z0 = bb.readU8(3)
				val z1 = bb.readU8(4)
				val z2 = bb.readU8(5)
				val z3 = bb.readU8(6)

                val flag_unsynchronisation = id3v2_flags.extract(7)
                val flag_extended_header = id3v2_flags.extract(6)
                val flag_experimental_ind = id3v2_flags.extract(5)
                val flag_footer_present = id3v2_flags.extract(4)

				if (((z0 and 0x80) == 0) && ((z1 and 0x80) == 0) && ((z2 and 0x80) == 0) && ((z3 and 0x80) == 0)) {
					val header_size = 10
					val tag_size =
						((z0 and 0x7f) * 2097152) + ((z1 and 0x7f) * 16384) + ((z2 and 0x7f) * 128) + (z3 and 0x7f)
					val footer_size = if (flag_footer_present) 10 else 0
					return (header_size + tag_size + footer_size).toLong()//bytes to skip
				}
			}
			return 0L
		}

		companion object {
            suspend operator fun invoke(data: AsyncStream) = Parser(data, data.getLength())

			enum class ChannelMode(val id: Int, val channels: Int) {
				STEREO(0b00, 2),
				JOINT_STEREO(0b01, 1),
				DUAL_CHANNEL(0b10, 2),
				SINGLE_CHANNEL(0b11, 1);

				companion object {
					val BY_ID = values().map { it.id to it }.toMap()
				}
			}

			val versions = arrayOf("2.5", "x", "2", "1")
			val layers = intArrayOf(-1, 3, 2, 1)

			val bitrates: Map<Int, IntArray> = mapOf(
                getBitrateKey(1, 1) to intArrayOf(0, 32, 64, 96, 128, 160, 192, 224, 256, 288, 320, 352, 384, 416, 448),
                getBitrateKey(1, 2) to intArrayOf(0, 32, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320, 384),
                getBitrateKey(1, 3) to intArrayOf(0, 32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320),
                getBitrateKey(2, 1) to intArrayOf(0, 32, 48, 56, 64, 80, 96, 112, 128, 144, 160, 176, 192, 224, 256),
                getBitrateKey(2, 2) to intArrayOf(0, 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160),
                getBitrateKey(2, 3) to intArrayOf(0, 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160)
			)

            fun getBitrateKey(version: Int, layer: Int): Int {
                return version * 10 + layer
            }

			val sampleRates = mapOf(
				"1" to intArrayOf(44100, 48000, 32000),
				"2" to intArrayOf(22050, 24000, 16000),
				"2.5" to intArrayOf(11025, 12000, 8000)
			)

			val samples = mapOf(
				1 to mapOf(1 to 384, 2 to 1152, 3 to 1152), // MPEGv1,     Layers 1,2,3
				2 to mapOf(1 to 384, 2 to 1152, 3 to 576)   // MPEGv2/2.5, Layers 1,2,3
			)

			data class Mp3Info(
				val version: String,
				val layer: Int,
				val bitrate: Int,
				val samplingRate: Int,
				val channelMode: ChannelMode,
				val frameSize: Int,
				val samples: Int
			)

			fun parseFrameHeader(f4: UByteArrayInt): Mp3Info {
				val b0 = f4[0]
				val b1 = f4[1]
				val b2 = f4[2]
				val b3 = f4[3]
				if (b0 != 0xFF) invalidOp

				val version = versions[b1.extract(3, 2)]
				val simple_version = if (version == "2.5") 2 else version.toInt()

				val layer = layers[b1.extract(1, 2)]

				val protection_bit = b1.extract(0, 1)
				val bitrate_key = getBitrateKey(simple_version, layer)
				val bitrate_idx = b2.extract(4, 4)

				val bitrate = bitrates[bitrate_key]?.getOrNull(bitrate_idx) ?: 0
				val sample_rate = sampleRates[version]?.getOrNull(b2.extract(2, 2)) ?: 0
				val padding_bit = b2.extract(1, 1)
				val private_bit = b2.extract(0, 1)
				val channelMode = ChannelMode.BY_ID[b3.extract(6, 2)]!!
				val mode_extension_bits = b3.extract(4, 2)
				val copyright_bit = b3.extract(3, 1)
				val original_bit = b3.extract(2, 1)
				val emphasis = b3.extract(0, 2)

				return Mp3Info(
					version = version,
					layer = layer,
					bitrate = bitrate,
					samplingRate = sample_rate,
					channelMode = channelMode,
					frameSize = this.framesize(layer, bitrate, sample_rate, padding_bit),
					samples = samples[simple_version]?.get(layer) ?: 0
				)
			}

			private fun framesize(layer: Int, bitrate: Int, sample_rate: Int, padding_bit: Int): Int {
                if (sample_rate == 0) error("division by 0")
				return if (layer == 1) {
					((12 * bitrate * 1000 / sample_rate) + padding_bit) * 4
				} else {
					//layer 2, 3
					((144 * bitrate * 1000) / sample_rate) + padding_bit
				}
			}
		}
	}

	override suspend fun decodeStreamInternal(data: AsyncStream, props: AudioDecodingProps): AudioStream? {
		return createDecoderStream(data, props, null)
	}

	private suspend fun createDecoderStream(data: AsyncStream, props: AudioDecodingProps, table: SeekingTable? = null): AudioStream {
		val dataStartPosition = data.position
		val decoder = createMp3Decoder()
		decoder.info.reset()
		val mp3SeekingTable: SeekingTable? = when (props.exactTimings) {
			true -> table ?: (if (data.hasLength()) Parser(data, data.getLength()).getSeekingTable(44100) else null)
			else -> null
		}

		suspend fun readMoreSamples(): Boolean {
			//println("currentThreadId=$currentThreadId, currentThreadName=$currentThreadName")
			while (true) {
				if (decoder.info.compressedData.availableRead < decoder.info.tempBuffer.size && data.hasAvailable()) {
					decoder.info.compressedData.write(data.readBytesUpTo(0x1000))
				}
				val result = decoder.info.step(decoder)
				if (decoder.info.hz == 0 || decoder.info.nchannels == 0) continue
				return result
			}
		}

		readMoreSamples()

		//println("Minimp3AudioFormat: decoder.hz=${decoder.hz}, decoder.nchannels=${decoder.nchannels}")

		return object : AudioStream(decoder.info.hz, decoder.info.nchannels) {
			override var finished: Boolean = false

			override var totalLengthInSamples: Long? = decoder.info.totalSamples.toLong().takeIf { it != 0L }
				?: mp3SeekingTable?.lengthSamples

			var _currentPositionInSamples: Long = 0L

			override var currentPositionInSamples: Long
				get() = _currentPositionInSamples
				set(value) {
					finished = false
					if (mp3SeekingTable != null) {
						data.position = mp3SeekingTable.locateSample(value)
						_currentPositionInSamples = value
					} else {
						// @TODO: We should try to estimate by using decoder.bitrate_kbps

						data.position = 0L
						_currentPositionInSamples = 0L
					}
					decoder.info.reset()
				}

			override suspend fun read(out: AudioSamples, offset: Int, length: Int): Int {
				var noMoreSamples = false
				while (decoder.info.pcmDeque!!.availableRead < length) {
					if (!readMoreSamples()) {
						//println("no more samples!")
						noMoreSamples = true
						break
					}
				}
				//println("audioSamplesDeque!!.availableRead=${audioSamplesDeque!!.availableRead}")
				return if (noMoreSamples && decoder.info.pcmDeque!!.availableRead == 0) {
					finished = true
					0
				} else {
					decoder.info.pcmDeque!!.read(out, offset, length)
				}.also {
					_currentPositionInSamples += it
					//println(" -> $it")
					//println("MP3.read: offset=$offset, length=$length, noMoreSamples=$noMoreSamples, finished=$finished")
				}
			}

			override fun close() {
				decoder.close()
			}

			override suspend fun clone(): AudioStream = createDecoderStream(data.duplicate().also { it.position = dataStartPosition }, props, table)
		}
	}

	private fun createMp3Decoder() = Minimp3Decoder()

	internal class Minimp3Decoder : BaseMp3Decoder {
		val MINIMP3_MAX_SAMPLES_PER_FRAME = (1152*2)
		val MP3_DEC_SIZE = 6668
		private var mp3dec: ByteArray = ByteArray(MP3_DEC_SIZE)
		private var mp3decFrameInfo: IntArray = IntArray(6)
		override val info: BaseMp3DecoderInfo = BaseMp3DecoderInfo()

		override fun decodeFrame(availablePeek: Int): ShortArray? = MiniMp3Wasm.stackKeep {
			val mp3dec = MiniMp3Wasm.stackAllocAndWrite(this.mp3dec)
			val mp3decFrameInfo = MiniMp3Wasm.stackAllocAndWrite(this.mp3decFrameInfo)
			val mp3Data = MiniMp3Wasm.stackAllocAndWrite(info.tempBuffer)
			val pcmData = MiniMp3Wasm.stackAlloc(MINIMP3_MAX_SAMPLES_PER_FRAME * 2 * 2 * Short.SIZE_BYTES)

			val samples = MiniMp3Wasm.mp3dec_decode_frame(
				mp3dec,
				mp3Data,
				availablePeek,
				pcmData,
				mp3decFrameInfo
			)
			this.mp3dec = MiniMp3Wasm.readBytes(mp3dec, MP3_DEC_SIZE)
			this.mp3decFrameInfo = MiniMp3Wasm.readInts(mp3decFrameInfo, 6)

			//  int frame_bytes, frame_offset, channels, hz, layer, bitrate_kbps;
			info.frame_bytes = this.mp3decFrameInfo[0]
			val frame_offset = this.mp3decFrameInfo[1]
			info.nchannels = this.mp3decFrameInfo[2]
			info.hz = this.mp3decFrameInfo[3]
			val layer = this.mp3decFrameInfo[4]
			info.bitrate_kbps = this.mp3decFrameInfo[5]
			info.samples = samples
			//println("samples=$samples, info=$info, ${this.mp3decFrameInfo.toList()}")

			if (info.nchannels == 0 || samples <= 0) {
				//println(" ---> null , info.nchannels=${info.nchannels}, samples=$samples")
				return null
			}

			return MiniMp3Wasm.readShorts(pcmData, samples * info.nchannels).also {
				//println(" ---> ${it.size}")
				//println(it.toList())
			}
		}

		override fun close() {
		}
	}

	class BaseMp3DecoderInfo {
		val tempBuffer = ByteArray(1152 * 2 * 2)
		val compressedData = ByteArrayDeque()
		var pcmDeque: AudioSamplesDeque? = null
		var hz = 0
		var bitrate_kbps = 0
		var nchannels = 0
		var samples: Int = 0
		var frame_bytes: Int = 0
		var skipRemaining: Int = 0
		var samplesAvailable: Int = 0
		var totalSamples: Int = 0
		var samplesRead: Int = 0
		var xingFrames: Int = 0
		var xingDelay: Int = 0
		var xingPadding: Int = 0

		fun reset() {
			pcmDeque?.clear()
			compressedData.clear()
			skipRemaining = 0
			samplesAvailable = -1
			samplesRead = 0
		}

		fun step(decoder: BaseMp3Decoder): Boolean {
			val availablePeek = compressedData.peek(tempBuffer, 0, tempBuffer.size)
			val xingIndex = tempBuffer.indexOf(XingTag).takeIf { it >= 0 } ?: tempBuffer.size
			val infoIndex = tempBuffer.indexOf(InfoTag).takeIf { it >= 0 } ?: tempBuffer.size

			if (xingIndex < tempBuffer.size || infoIndex < tempBuffer.size) {
				try {
					val index = kotlin.math.min(xingIndex, infoIndex)
					val data = FastByteArrayInputStream(tempBuffer, index)
					data.skip(7)
					if (data.available >= 1) {
						val flags = data.readU8()
						//println("xing=$xingIndex, infoIndex=$infoIndex, index=$index, flags=$flags")
						val FRAMES_FLAG = 1
						val BYTES_FLAG = 2
						val TOC_FLAG = 4
						val VBR_SCALE_FLAG = 8
						if (flags.hasFlags(FRAMES_FLAG) && data.available >= 4) {
							xingFrames = data.readS32BE()
							if (flags.hasFlags(BYTES_FLAG)) data.skip(4)
							if (flags.hasFlags(TOC_FLAG)) data.skip(100)
							if (flags.hasFlags(VBR_SCALE_FLAG)) data.skip(4)
							if (data.available >= 1) {
								val info = data.readU8()
								if (info != 0) {
									data.skip(20)
									if (data.available >= 3) {
										val t0 = data.readU8()
										val t1 = data.readU8()
										val t2 = data.readU8()
										xingDelay = ((t0 shl 4) or (t1 ushr 4)) + (528 + 1)
										xingPadding = (((t1 and 0xF) shl 8) or (t2)) - (528 + 1)
									}
									//println("frames=$frames, flags=$flags, delay=$delay, padding=$padding, $t0,$t1,$t2")
								}
							}
						}
					}
				} catch (e: Throwable) {
					e.printStackTrace()
				}
			}
			val buf = decoder.decodeFrame(availablePeek)

			if (nchannels != 0 && (xingFrames != 0 || xingDelay != 0 || xingPadding != 0)) {
				val rpadding = xingPadding * nchannels
				val to_skip = xingDelay * nchannels
				var detected_samples = samples * xingFrames * nchannels
				if (detected_samples >= to_skip) detected_samples -= to_skip
				if (rpadding in 1..detected_samples) detected_samples -= rpadding
				skipRemaining = to_skip + (samples * nchannels)
				samplesAvailable = detected_samples
				//println("nchannels=$nchannels")
				totalSamples = detected_samples / nchannels
				//println("frames=$frames, delay=$delay, padding=$padding :: rpadding=$rpadding, to_skip=$to_skip, detected_samples=$detected_samples")

				xingFrames = 0
				xingDelay = 0
				xingPadding = 0
			}

			//println("availablePeek=$availablePeek, frame_bytes=$frame_bytes, samples=$samples, nchannels=$nchannels, hz=$hz, bitrate_kbps=")

			if (nchannels != 0 && pcmDeque == null) {
				pcmDeque = AudioSamplesDeque(nchannels)
			}

			if (samples > 0) {
				var offset = 0
				var toRead = samples * nchannels

				if (skipRemaining > 0) {
					val skipNow = kotlin.math.min(skipRemaining, toRead)
					offset += skipNow
					toRead -= skipNow
					skipRemaining -= skipNow
				}
				if (samplesAvailable >= 0) {
					toRead = kotlin.math.min(toRead, samplesAvailable)
					samplesAvailable -= toRead
				}

				//println("writeInterleaved. offset=$offset, toRead=$toRead")
				pcmDeque!!.writeInterleaved(buf!!, offset, toRead)
			}

			//println("mp3decFrameInfo: samples=$samples, channels=${struct.channels}, frame_bytes=${struct.frame_bytes}, frame_offset=${struct.frame_offset}, bitrate_kbps=${struct.bitrate_kbps}, hz=${struct.hz}, layer=${struct.layer}")

			compressedData.skip(frame_bytes)

			if (frame_bytes == 0 && samples == 0) {
				return false
			}

			if (pcmDeque != null && pcmDeque!!.availableRead > 0) {
				return true
			}

			return true
		}
	}

	interface BaseMp3Decoder : Closeable {
		val info: BaseMp3DecoderInfo
		fun decodeFrame(availablePeek: Int): ShortArray?
	}

	private val XingTag = byteArrayOf('X'.code.toByte(), 'i'.code.toByte(), 'n'.code.toByte(), 'g'.code.toByte())
	private val InfoTag = byteArrayOf('I'.code.toByte(), 'n'.code.toByte(), 'f'.code.toByte(), 'o'.code.toByte())
}

// emcc -Oz MiniMp3Program.c -s STANDALONE_WASM --no-entry -s WASM -o MiniMp3Program.wasm
// ~/go/bin/zlib < MiniMp3Program.wasm > MiniMp3Program.wasm.zlib
// base64 -i MiniMp3Program.wasm.zlib
private object MiniMp3Wasm : WASMLib(MINIMP3_WASM_BYTES) {
	val mp3dec_init: (ptr: Int) -> Unit by func()
	val mp3dec_decode_frame: (dec: Int, mp3: Int, mp3_bytes: Int, pcm: Int, info: Int) -> Int by func()
}

private val MINIMP3_WASM_BYTES = "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"
	.fromBase64()
	.uncompress(ZLib)
