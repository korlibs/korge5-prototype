package korlibs.audio.sound

import doIOTest
import korlibs.audio.format.AudioDecodingProps
import korlibs.audio.format.AudioFormats
import korlibs.audio.format.MP3
import korlibs.crypto.sha1
import korlibs.io.async.suspendTest
import korlibs.io.file.std.resourcesVfs
import korlibs.logger.*
import korlibs.math.*
import korlibs.time.measureTimeWithResult
import kotlin.test.Test
import kotlin.test.assertEquals

class SoftMp3DecoderTest {
    val formats = AudioFormats(MP3)
    val logger = Logger("SoftMp3DecoderTest")

    @Test
    fun testMiniMp31() = suspendTest({ doIOTest }) {
        //resourcesVfs["mp31.mp3"].readAudioData(FastMP3Decoder).toSound()
        assertEquals(
            "1,44100,22050,c82a407c8353c9d47c6f499a5195f85809bbbf8a",
            resourcesVfs["mp31.mp3"].readAudioData(MP3).toFingerprintString()
        )
    }

    @Test fun mp3_1() = suspendTest({ doIOTest }) {
        assertEquals(
            "1,44100,28800,ee797bf9ec5a2b5ed0e3064cc5d091157921be6f",
            resourcesVfs["circle_ok.mp3"].readAudioData(formats).toFingerprintString()
        )
    }
    @Test fun mp3_2() = suspendTest({ doIOTest }) {
        assertEquals(
            "1,44100,16128,e4848a4bd5b3117665dcafc14109fdc677c9ee2f",
            resourcesVfs["line_missed.mp3"].readAudioData(formats).toFingerprintString()
        )
    }
    @Test fun mp3_3() = suspendTest({ doIOTest }) {
        assertEquals(
            "1,44100,14976,f38dc856841ba47afe815d6a64654f29b63b822e",
            resourcesVfs["line_ok.mp3"].readAudioData(formats).toFingerprintString(),
        )
    }
    @Test fun monkeyDramaMiniMp3() = suspendTest({ doIOTest }) {
        val (mp3Bytes, readTime) = measureTimeWithResult { resourcesVfs["monkey_drama.mp3"].readBytes() }
        logger.debug { "Read in $readTime" }
        val (decode, decodeTime) = measureTimeWithResult { formats.decode(mp3Bytes, AudioDecodingProps(maxSamples = 569088)) }
        logger.debug { "Decoded in $decodeTime" }
        val (fingerprint, fingerprintTime) = measureTimeWithResult { decode?.toFingerprintString() }
        logger.debug { "Fingerprint in in $fingerprintTime" }
        assertEquals(
            "2,44100,569088,f43f395b2029b060f9f6ef06a1a96b2e1e6f3860",
            fingerprint,
        )
    }

    //@Ignore
    //@Test
    //fun monkeyDramaMiniMp3() = suspendTest({ doIOTest }) {
    //    resourcesVfs["monkey_drama.mp3"].readAudioData(JavaMp3AudioFormat, AudioDecodingProps(maxSamples = 569088)).toFingerprintString()
    //}

    @Test fun snowland() = suspendTest({ doIOTest }) {
        assertEquals(
            "2,48000,565920,36945a5c28a37e4f860b951fe397f03ba1bd187d",
            resourcesVfs["Snowland.mp3"].readAudioData(formats).toFingerprintString(),
        )
    }

    fun AudioData.toFingerprintString(): String {
        val sdata = samplesInterleaved.data
        val data = ByteArray(sdata.size) { sdata[it].toInt().divRound(256 * 8).toByte() }
        return "$channels,$rate,$totalSamples,${data.sha1().hex}"
    }

}
