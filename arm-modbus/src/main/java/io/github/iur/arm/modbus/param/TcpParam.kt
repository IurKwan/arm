package io.github.iur.arm.modbus.param

import com.serotonin.modbus4j.ModbusFactory
import com.serotonin.modbus4j.ModbusMaster
import com.serotonin.modbus4j.ip.IpParameters
import io.github.iur.arm.modbus.ModbusParam

/**
 * TCP 参数
 */
class TcpParam private constructor(
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
    var keepAlive: Boolean = false

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
        val factory = ModbusFactory()
        return factory.createTcpMaster(ipParameters, keepAlive).apply {
            setRetries(this@TcpParam.retries)
            setTimeout(this@TcpParam.timeout)
        }
    }

    companion object {
        @JvmStatic
        fun create(
            host: String,
            port: Int,
        ): TcpParam = TcpParam(host, port)
    }
}
