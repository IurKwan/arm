package io.github.iur.arm.modbus

import com.serotonin.modbus4j.ModbusMaster
import com.serotonin.modbus4j.msg.ReadCoilsResponse
import com.serotonin.modbus4j.msg.ReadDiscreteInputsResponse
import com.serotonin.modbus4j.msg.ReadHoldingRegistersResponse
import com.serotonin.modbus4j.msg.ReadInputRegistersResponse
import com.serotonin.modbus4j.msg.WriteCoilResponse
import com.serotonin.modbus4j.msg.WriteCoilsResponse
import com.serotonin.modbus4j.msg.WriteRegisterResponse
import com.serotonin.modbus4j.msg.WriteRegistersResponse

/**
 * Modbus 工作接口，协程化版本。
 *
 * 所有 IO 操作均为 suspend 函数，可能失败的操作返回 [Result]。
 * 失败的 [Throwable] 类型可能是：
 * - [com.serotonin.modbus4j.exception.ModbusInitException] 未初始化或初始化失败
 * - [com.serotonin.modbus4j.exception.ModbusTransportException] 传输失败
 * - [ModbusRespException] 响应包含 modbus 异常码
 * - [IllegalStateException] worker 已 release
 */
interface ModbusWorker {

    /**
     * 是否已经打开 Modbus
     */
    val isOpened: Boolean

    /**
     * 当前 ModbusMaster（未初始化时为 null）
     */
    val modbusMaster: ModbusMaster?

    /**
     * 两次发送命令之间必须等待的时间（毫秒）。0 表示无节流。
     */
    var sendIntervalTime: Long

    /**
     * 初始化 Modbus
     */
    suspend fun init(param: ModbusParam): Result<ModbusMaster>

    /**
     * 关掉 ModbusMaster（关闭后仍可再次 [init]）
     */
    suspend fun close()

    /**
     * 释放整个 Worker。release 后无法再次 [init]。
     */
    suspend fun release()

    /**
     * 01 (0x01) 读线圈
     */
    suspend fun readCoil(slaveId: Int, start: Int, len: Int): Result<ReadCoilsResponse>

    /**
     * 02 (0x02) 读离散量输入
     */
    suspend fun readDiscreteInput(
        slaveId: Int,
        start: Int,
        len: Int,
    ): Result<ReadDiscreteInputsResponse>

    /**
     * 03 (0x03) 读保持寄存器
     */
    suspend fun readHoldingRegisters(
        slaveId: Int,
        start: Int,
        len: Int,
    ): Result<ReadHoldingRegistersResponse>

    /**
     * 04 (0x04) 读输入寄存器
     */
    suspend fun readInputRegisters(
        slaveId: Int,
        start: Int,
        len: Int,
    ): Result<ReadInputRegistersResponse>

    /**
     * 05 (0x05) 写单个线圈
     */
    suspend fun writeCoil(
        slaveId: Int,
        offset: Int,
        value: Boolean,
    ): Result<WriteCoilResponse>

    /**
     * 06 (0x06) 写单个寄存器
     */
    suspend fun writeSingleRegister(
        slaveId: Int,
        offset: Int,
        value: Int,
    ): Result<WriteRegisterResponse>

    /**
     * 15 (0x0F) 写多个线圈
     */
    suspend fun writeCoils(
        slaveId: Int,
        start: Int,
        values: BooleanArray,
    ): Result<WriteCoilsResponse>

    /**
     * 16 (0x10) 写多个寄存器
     */
    suspend fun writeRegisters(
        slaveId: Int,
        start: Int,
        values: ShortArray,
    ): Result<WriteRegistersResponse>

    /**
     * 16 (0x10) 写多个寄存器，但只写 1 个
     */
    suspend fun writeRegistersButOne(
        slaveId: Int,
        start: Int,
        value: Int,
    ): Result<WriteRegistersResponse>
}
