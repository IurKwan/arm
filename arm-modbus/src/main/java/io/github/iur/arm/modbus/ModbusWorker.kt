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
 * 32 位整数在两个连续 16 位寄存器中的字序。
 */
enum class Int32WordOrder {
    HIGH_WORD_FIRST,
    LOW_WORD_FIRST,
}

internal fun Int.toModbusWords(wordOrder: Int32WordOrder): Pair<Int, Int> {
    val highWord = this ushr Short.SIZE_BITS
    val lowWord = this and 0xFFFF
    return when (wordOrder) {
        Int32WordOrder.HIGH_WORD_FIRST -> highWord to lowWord
        Int32WordOrder.LOW_WORD_FIRST -> lowWord to highWord
    }
}

/**
 * 使用两次功能码 06 写入 32 位整数后的响应。
 */
data class WriteInt32Response(
    val firstRegister: WriteRegisterResponse,
    val secondRegister: WriteRegisterResponse,
)

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
    suspend fun readCoil(
        slaveId: Int,
        start: Int,
        len: Int,
    ): Result<ReadCoilsResponse>

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
     * 使用两次 06 (0x06) 将一个有符号 32 位整数写入两个连续保持寄存器。
     *
     * 两次请求在 Worker 内连续执行，不会与同一 Worker 的其他请求交错，但 Modbus
     * 协议不保证两个独立请求具备原子性。如果第二次写入失败，第一次写入不会回滚。
     */
    suspend fun writeInt32(
        slaveId: Int,
        start: Int,
        value: Int,
        wordOrder: Int32WordOrder = Int32WordOrder.HIGH_WORD_FIRST,
    ): Result<WriteInt32Response>

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
