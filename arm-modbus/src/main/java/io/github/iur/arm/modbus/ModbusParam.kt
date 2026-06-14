package io.github.iur.arm.modbus

import com.serotonin.modbus4j.ModbusMaster

/**
 * modbus 初始化参数
 */
interface ModbusParam {
    /**
     * 超时（毫秒）
     */
    var timeout: Int

    /**
     * 重试次数
     */
    var retries: Int

    /**
     * 创建 ModbusMaster
     */
    fun createModbusMaster(): ModbusMaster

    companion object {
        /**
         * 默认超时（毫秒）
         */
        const val DEFAULT_TIMEOUT: Int = 500

        /**
         * 默认重试次数
         */
        const val DEFAULT_RETRIES: Int = 2
    }
}
