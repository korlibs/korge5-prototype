package korlibs.time

import kotlin.test.Test
import kotlin.test.assertEquals

class SerializableDateTimeTest {
	@Test
	fun testSerializableInstances() {
		@Suppress("USELESS_IS_CHECK")
		assertEquals(true, DateTime.nowLocal() is DateTimeTz)
	}
}
