package io.github.iur.arm.modbus

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class McuInt32FrameCodecTest {
    @Test
    fun encode_matchesConfirmedMcuRequest() {
        assertArrayEquals(
            hex("5A 06 00 67 00 00 00 01 57 40"),
            McuInt32FrameCodec.encode(slaveId = 0x5A, offset = 0x0067, value = 1),
        )
    }

    @Test
    fun decodeEcho_returnsWrittenValue() {
        val frame = hex("5A 06 00 67 00 00 00 01 57 40")

        assertEquals(
            WriteInt32Response(slaveId = 0x5A, offset = 0x0067, value = 1),
            McuInt32FrameCodec.decodeEcho(frame, frame),
        )
    }

    private fun hex(value: String): ByteArray =
        value.split(' ').map { it.toInt(16).toByte() }.toByteArray()
}
