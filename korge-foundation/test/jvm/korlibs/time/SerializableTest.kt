package korlibs.time

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlin.test.Test
import kotlin.test.assertTrue

class SerializableTest {
    fun <T> serializeDeserializeObject(instance: T): Any? {
        val bao = ByteArrayOutputStream().also { ObjectOutputStream(it).writeObject(instance) }.toByteArray()
        return ObjectInputStream(ByteArrayInputStream(bao)).readObject()
    }

    @Test
    fun test() {
        val time = 1_000_000L
        assertTrue { serializeDeserializeObject(DateTimeTz.fromUnixLocal(time)) is DateTimeTz }
    }
}
