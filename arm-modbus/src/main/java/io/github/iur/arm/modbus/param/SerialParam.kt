package io.github.iur.arm.modbus.param

import com.serotonin.modbus4j.ModbusFactory
import com.serotonin.modbus4j.ModbusMaster
import io.github.iur.arm.modbus.AndroidSerialPortWrapper
import io.github.iur.arm.modbus.ModbusParam

/**
 * 串口参数
 */
class SerialParam private constructor(
    /**
     * 串口设备
     */
    var serialDevice: String,
    /**
     * 串口波特率
     */
    var baudRate: Int,
) : ModbusParam {
    override var timeout: Int = ModbusParam.DEFAULT_TIMEOUT
    override var retries: Int = ModbusParam.DEFAULT_RETRIES

    /**
     * 数据位，默认 8，可选 5~8
     */
    var dataBits: Int = 8

    /**
     * 校验位，0 无校验(默认)，1 奇校验，2 偶校验
     */
    var parity: Int = 0

    /**
     * 停止位，1 或 2
     */
    var stopBits: Int = 1

    override fun createModbusMaster(): ModbusMaster {
        val factory = ModbusFactory()
        val wrapper = AndroidSerialPortWrapper(serialDevice, baudRate, dataBits, parity, stopBits)
        return factory.createRtuMaster(wrapper).apply {
            setRetries(this@SerialParam.retries)
            setTimeout(this@SerialParam.timeout)
        }
    }

    companion object {
        @JvmStatic
        fun create(
            serialDevice: String,
            baudRate: Int,
        ): SerialParam = SerialParam(serialDevice, baudRate)
    }
}
