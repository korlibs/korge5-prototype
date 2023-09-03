package korlibs.memory

@Deprecated("", ReplaceWith("korlibs.platform.Arch"))
typealias Arch = korlibs.platform.Arch
@Deprecated("", ReplaceWith("korlibs.platform.BuildVariant"))
typealias BuildVariant = korlibs.platform.BuildVariant
@Deprecated("", ReplaceWith("korlibs.platform.Endian"))
typealias Endian = korlibs.platform.Endian
@Deprecated("", ReplaceWith("korlibs.platform.Os"))
typealias Os = korlibs.platform.Os
@Deprecated("", ReplaceWith("korlibs.platform.Platform"))
typealias Platform = korlibs.platform.Platform
@Deprecated("", ReplaceWith("korlibs.platform.Runtime"))
typealias Runtime = korlibs.platform.Runtime

@Deprecated("", ReplaceWith("korlibs.platform.checkIsJsBrowser()"))
fun checkIsJsBrowser() {
    korlibs.platform.checkIsJsBrowser()
}