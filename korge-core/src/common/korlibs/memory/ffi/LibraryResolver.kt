package korlibs.memory.ffi

import korlibs.audio.error.seekingNotSupported
import korlibs.io.file.sync.SyncIO
import korlibs.io.file.sync.SyncIOAPI
import korlibs.io.file.sync.file
import korlibs.io.file.sync.platformSyncIO
import korlibs.memory.Platform

open class LibraryResolver(val fs: SyncIO, val platform: Platform) {
    val ldLibraries by lazy { LDLibraries(fs) }

    @SyncIOAPI
    companion object : LibraryResolver(platformSyncIO, Platform)

    fun resolve(vararg names: String): String? = names.firstNotNullOfOrNull { resolve(it) }

    fun resolve(name: String): String? {
        // @TODO: Search in LDLibraries, search frameworks, append .dll, prepend lib, etc.
        if (name.endsWith(".dylib") || name.endsWith(".so") || name.endsWith(".dll")) {
            return name
        }

        return when {
            platform.isMac -> {
                val bases = listOf("/Library/Frameworks", "/System/Library/Frameworks")
                bases.firstNotNullOfOrNull {
                    val base = "$it/$name.framework"
                    if (fs.file(base).isDirectory) {
                        "$base/$name"
                    } else {
                        null
                    }
                }
            }

            platform.isWindows -> {
                listOf("$name.dll", name)
                    .map { fs.file(it) }
                    .filter { it.exists() }
                    .firstNotNullOfOrNull { it.name }
            }

            else -> {
                val exact = ldLibraries.ldFolders
                    .firstNotNullOfOrNull { folder ->
                        val tries = listOf("lib$name.so", name)
                        tries.firstNotNullOfOrNull { folder[it].takeIf { it.exists() } }
                    }?.fullPath

                exact ?: ldLibraries.ldFolders
                    .firstNotNullOfOrNull { folder ->
                        println(folder.list())
                        folder.list().firstOrNull {
                            it.name.startsWith("lib$name") || it.name.startsWith(name)
                        }
                    }?.fullPath
            }
        }
    }
}