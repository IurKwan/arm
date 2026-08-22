package io.github.iur.arm.modbus

import com.serotonin.modbus4j.msg.ModbusResponse
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
        val response: ModbusResponse = McuInt32FrameCodec.decodeEcho(frame, frame)

        assertEquals(0x5A, response.slaveId)
        assertEquals(0x06, response.functionCode.toInt())
        val int32Response = response as com.serotonin.modbus4j.msg.WriteInt32Response
        assertEquals(0x0067, int32Response.writeOffset)
        assertEquals(1, int32Response.writeValue)
    }

    private fun hex(value: String): ByteArray =
        value.split(' ').map { it.toInt(16).toByte() }.toByteArray()
}
