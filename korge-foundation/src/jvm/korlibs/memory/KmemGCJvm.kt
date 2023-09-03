package korlibs.memory

actual fun gc(full: Boolean): Unit {
    System.gc()
}
