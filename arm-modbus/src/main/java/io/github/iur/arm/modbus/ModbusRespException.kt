package io.github.iur.arm.modbus

import com.serotonin.modbus4j.msg.ModbusResponse

/**
 * Modbus 响应异常
 */
class ModbusRespException(
    response: ModbusResponse,
) : Exception("${response.exceptionMessage} ,code=${response.exceptionCode}") {
    val exceptionCode: Byte = response.exceptionCode
}
