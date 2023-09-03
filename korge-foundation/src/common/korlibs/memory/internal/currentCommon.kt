package korlibs.memory.internal

import korlibs.platform.Arch
import korlibs.platform.BuildVariant
import korlibs.platform.Os
import korlibs.platform.Runtime

internal expect val currentOs: Os
internal expect val currentRuntime: Runtime
internal expect val currentArch: Arch
internal expect val currentIsDebug: Boolean
internal expect val currentIsLittleEndian: Boolean
internal expect val currentRawPlatformName: String
internal expect val currentRawOsName: String
val currentBuildVariant: BuildVariant get() = if (currentIsDebug) BuildVariant.DEBUG else BuildVariant.RELEASE
internal expect val multithreadedSharedHeap: Boolean
