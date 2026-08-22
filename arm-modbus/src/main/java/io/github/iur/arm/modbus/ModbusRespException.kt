package io.github.iur.arm.modbus

import com.serotonin.modbus4j.msg.ModbusResponse

/**
 * Modbus 响应异常
 */
class ModbusRespException(
    val exceptionCode: Byte,
) : Exception("${exceptionMessage(exceptionCode)} ,code=$exceptionCode") {
    constructor(response: ModbusResponse) : this(response.exceptionCode)

    companion object {
        private fun exceptionMessage(code: Byte): String =
            when (code.toInt()) {
                1 -> "Illegal function"
                2 -> "Illegal data address"
                3 -> "Illegal data value"
                4 -> "Slave device failure"
                5 -> "Acknowledge"
                6 -> "Slave device busy"
                8 -> "Memory parity error"
                10 -> "Gateway path unavailable"
                11 -> "Gateway target device failed to respond"
                else -> "Unknown exception code: $code"
            }
    }
}
