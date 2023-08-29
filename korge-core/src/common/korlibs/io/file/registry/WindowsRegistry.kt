package korlibs.io.file.registry

import korlibs.ffi.FFILib
import korlibs.memory.Platform

typealias HKEY = Int

object WindowsRegistry {
    val isSupported: Boolean get() = Platform.isWindows

    private fun parsePathEx(path: String): Pair<HKEY, String>? =
        parsePath(path)?.let { Pair(it.first, it.second) }

    private fun parsePathWithValueEx(path: String): Triple<HKEY, String, String>? =
        parsePathWithValue(path)?.let { Triple(it.first, it.second, it.third) }

    fun listSubKeys(path: String): List<String> {
        val (root, keyPath) = parsePathEx(path) ?: return KEY_MAP.keys.toList()

        val phkKey = IntArray(0)
        val samDesiredExtra = 0

        checkError(Advapi32.RegOpenKeyExA(
            root, keyPath, 0,
            KEY_READ or samDesiredExtra, phkKey
        ))

        //return try {
        //    Advapi32Util.registryGetKeys(phkKey[0]).toList()
        //} finally {
        //    rc = Advapi32.INSTANCE.RegCloseKey(phkKey.value)
        //    if (rc != W32Errors.ERROR_SUCCESS) {
        //        throw Win32Exception(rc)
        //    }
        //}
        TODO()
    }

    fun listValues(path: String): Map<String, Any?> {
        val (root, keyPath) = parsePathEx(path) ?: return emptyMap()
        //return Advapi32Util.registryGetValues(root, keyPath)
        TODO()
    }

    fun getValue(path: String): Any? {
        val (root, keyPathPath, valueName) = parsePathWithValueEx(path) ?: return null
        //Advapi32Util.registryValueExists()
        //return Advapi32Util.registryGetValue(root, keyPathPath, valueName).also { result ->
        //    //println("root=$root, keyPathPath=$keyPathPath, valueName=$valueName, result=$result")
        //}
        TODO()
    }

    fun setValue(path: String, value: Any?) {
        val (root, keyName, valueName) = parsePathWithValueEx(path) ?: return
        //when (value) {
        //    null -> {
        //        // @TODO: This should set to none
        //        Advapi32Util.registrySetStringValue(root, keyName, valueName, null)
        //    }
        //    is String -> Advapi32Util.registrySetStringValue(root, keyName, valueName, value)
        //    is ByteArray -> Advapi32Util.registrySetBinaryValue(root, keyName, valueName, value)
        //    is Int -> Advapi32Util.registrySetIntValue(root, keyName, valueName, value)
        //    is Long -> Advapi32Util.registrySetLongValue(root, keyName, valueName, value)
        //    is List<*> -> Advapi32Util.registrySetStringArray(root, keyName, valueName, value.map { it?.toString() }.toTypedArray())
        //}
        TODO()
    }

    fun deleteValue(path: String) {
        val (root, keyName, valueName) = parsePathWithValueEx(path) ?: return
        checkError(Advapi32.RegDeleteValueA(root, keyName))
    }

    fun deleteKey(path: String) {
        val (root, keyName) = parsePathEx(path) ?: return
        checkError(Advapi32.RegDeleteKeyA(root, keyName))
    }

    fun createKey(path: String): Boolean {
        val (root, keyName) = parsePathEx(path) ?: return false
        //return Advapi32Util.registryCreateKey(root, keyName)

        val phkRes = IntArray(1)
        val disp = IntArray(1)
        checkError(Advapi32.RegCreateKeyExA(root, keyName, 0, null, 0, 131097, null, phkRes, disp))
        Advapi32.RegCloseKey(phkRes[0])
        return disp[0] == 1
    }

    fun hasKey(path: String): Boolean {
        val (root, keyName) = parsePathEx(path) ?: return false
        val phkKey = IntArray(1)
        return (Advapi32.RegOpenKeyExA(root, keyName, 0, 131097 or 0, phkKey) == 0).also {
            if (it) Advapi32.RegCloseKey(phkKey[0])
        }
    }

    private fun checkError(value: Int) {
        if (value != 0) error("Error '$value'")
    }

    private fun parsePath(path: String): Pair<Int, String>? {
        val rpath = normalizePath(path)
        if (rpath.isEmpty()) return null
        val rootKeyStr = rpath.substringBefore('\\')
        val keyPath = rpath.substringAfter('\\', "")
        val rootKey = KEY_MAP[rootKeyStr.uppercase()] ?: error("Invalid rootKey '${rootKeyStr}', it should start with HKEY_ and be known")
        return rootKey to keyPath
    }

    private fun parsePathWithValue(path: String): Triple<Int, String, String>? {
        val (root, keyPath) = parsePath(path) ?: return null
        val keyPathPath = keyPath.substringBeforeLast('\\')
        val valueName = keyPath.substringAfterLast('\\', "")
        return Triple(root, keyPathPath, valueName)
    }

    private fun normalizePath(path: String) = path.trim('/').replace('/', '\\')

    fun hasValue(path: String): Boolean = getValue(path) != null
    fun listValueKeys(path: String): List<String> = listValues(path).keys.toList()

    private const val KEY_READ = 131097
    private const val ERROR_SUCCESS = 0

    private const val HKEY_CLASSES_ROOT = (0x80000000L).toInt()
    private const val HKEY_CURRENT_USER = (0x80000001L).toInt()
    private const val HKEY_LOCAL_MACHINE = (0x80000002L).toInt()
    private const val HKEY_USERS = (0x80000003L).toInt()
    private const val HKEY_PERFORMANCE_DATA= (0x80000004L).toInt()
    private const val HKEY_PERFORMANCE_TEXT= (0x80000050L).toInt()
    private const val HKEY_PERFORMANCE_NLSTEXT = (0x80000060L).toInt()
    private const val HKEY_CURRENT_CONFIG  = (0x80000005L).toInt()
    private const val HKEY_DYN_DATA = (0x80000006L).toInt()
    private const val HKEY_CURRENT_USER_LOCAL_SETTINGS = (0x80000007L).toInt()
    val KEY_MAP: Map<String, Int> = mapOf<String, Int>(
        "HKEY_CLASSES_ROOT" to HKEY_CLASSES_ROOT,
        "HKEY_CURRENT_USER" to HKEY_CURRENT_USER,
        "HKEY_LOCAL_MACHINE" to HKEY_LOCAL_MACHINE,
        "HKEY_USERS" to HKEY_USERS,
        "HKEY_CURRENT_CONFIG" to HKEY_CURRENT_CONFIG,
        "HKEY_PERFORMANCE_DATA" to HKEY_PERFORMANCE_DATA,
        "HKEY_PERFORMANCE_TEXT" to HKEY_PERFORMANCE_TEXT,
        "HKEY_PERFORMANCE_NLSTEXT" to HKEY_PERFORMANCE_NLSTEXT,
        "HKEY_DYN_DATA" to HKEY_DYN_DATA,
        "HKEY_CURRENT_USER_LOCAL_SETTINGS" to HKEY_CURRENT_USER_LOCAL_SETTINGS,
    )

    object Advapi32 : FFILib("Advapi32") {
        val RegOpenKeyExA: (hKey: HKEY, subKey: String, ulOptions: Int, samDesired: Int, phkResult: IntArray?) -> Int by func()
        val RegDeleteValueA: (hKey: HKEY, valueName: String) -> Int by func()
        val RegDeleteKeyA: (hKey: HKEY, subKey: String) -> Int by func()
        val RegCreateKeyExA: (hKey: HKEY, subKey: String, reserved: Int, lpClass: String?, options: Int, samDesired: Int, securityAttributes: IntArray?, phkResult: IntArray?, ldwDisposition: IntArray) -> Int by func()
        val RegCloseKey: (hKey: HKEY) -> Int by func()
    }
}
