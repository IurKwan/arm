package io.github.iur.arm.modbus.param

import com.serotonin.modbus4j.ModbusMaster
import com.serotonin.modbus4j.ip.IpParameters
import io.github.iur.arm.modbus.AppUdpMaster
import io.github.iur.arm.modbus.ModbusParam

/**
 * UDP 参数
 */
class UdpParam private constructor(
    host: String,
    port: Int,
) : ModbusParam {
    private val ipParameters: IpParameters =
        IpParameters().apply {
            this.host = host
            this.port = port
        }

    override var timeout: Int = ModbusParam.DEFAULT_TIMEOUT
    override var retries: Int = ModbusParam.DEFAULT_RETRIES

    /**
     * 是否验证响应中的从站 ID
     */
    var validateResponse: Boolean = false

    var host: String
        get() = ipParameters.host
        set(value) {
            ipParameters.host = value
        }

    var port: Int
        get() = ipParameters.port
        set(value) {
            ipParameters.port = value
        }

    var encapsulated: Boolean
        get() = ipParameters.isEncapsulated
        set(value) {
            ipParameters.setEncapsulated(value)
        }

    override fun createModbusMaster(): ModbusMaster {
        val master = AppUdpMaster(ipParameters, validateResponse, port)
        master.retries = retries
        master.timeout = timeout
        return master
    }

    companion object {
        @JvmStatic
        fun create(
            host: String,
            port: Int,
        ): UdpParam = UdpParam(host, port)
    }
}
