package io.github.iur.arm.modbus

import org.junit.Assert.assertEquals
import org.junit.Test

class Int32WordOrderTest {
    @Test
    fun highWordFirst_splitsPositiveInt32() {
        assertEquals(
            0x1234 to 0x5678,
            0x12345678.toModbusWords(Int32WordOrder.HIGH_WORD_FIRST),
        )
    }

    @Test
    fun lowWordFirst_reversesRegisterOrder() {
        assertEquals(
            0x5678 to 0x1234,
            0x12345678.toModbusWords(Int32WordOrder.LOW_WORD_FIRST),
        )
    }

    @Test
    fun negativeInt32_preservesTwosComplementBits() {
        assertEquals(
            0xFFFF to 0xFFFE,
            (-2).toModbusWords(Int32WordOrder.HIGH_WORD_FIRST),
        )
    }
}
