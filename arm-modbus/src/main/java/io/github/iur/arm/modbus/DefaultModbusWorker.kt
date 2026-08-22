package io.github.iur.arm.modbus

import android.os.SystemClock
import android.util.Log
import com.serotonin.modbus4j.ModbusMaster
import com.serotonin.modbus4j.exception.ModbusInitException
import com.serotonin.modbus4j.msg.ModbusRequest
import com.serotonin.modbus4j.msg.ModbusResponse
import com.serotonin.modbus4j.msg.ReadCoilsRequest
import com.serotonin.modbus4j.msg.ReadCoilsResponse
import com.serotonin.modbus4j.msg.ReadDiscreteInputsRequest
import com.serotonin.modbus4j.msg.ReadDiscreteInputsResponse
import com.serotonin.modbus4j.msg.ReadHoldingRegistersRequest
import com.serotonin.modbus4j.msg.ReadHoldingRegistersResponse
import com.serotonin.modbus4j.msg.ReadInputRegistersRequest
import com.serotonin.modbus4j.msg.ReadInputRegistersResponse
import com.serotonin.modbus4j.msg.WriteCoilRequest
import com.serotonin.modbus4j.msg.WriteCoilResponse
import com.serotonin.modbus4j.msg.WriteCoilsRequest
import com.serotonin.modbus4j.msg.WriteCoilsResponse
import com.serotonin.modbus4j.msg.WriteInt32Response
import com.serotonin.modbus4j.msg.WriteRegisterRequest
import com.serotonin.modbus4j.msg.WriteRegisterResponse
import com.serotonin.modbus4j.msg.WriteRegistersRequest
import com.serotonin.modbus4j.msg.WriteRegistersResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * [ModbusWorker] 默认实现。
 *
 * 内部用 [Mutex] 序列化所有 Modbus 请求，IO 工作 dispatch 到 [Dispatchers.IO]。
 */
open class DefaultModbusWorker : ModbusWorker {
    private val mutex = Mutex()

    @Volatile
    private var master: ModbusMaster? = null

    @Volatile
    private var released: Boolean = false

    private var sendTime: Long = 0L

    override var sendIntervalTime: Long = 0L
        set(value) {
            require(value >= 0) {
                "Send interval time should not be negative, but now ms=$value"
            }
            field = value
        }

    override val isOpened: Boolean
        get() = master != null

    override val modbusMaster: ModbusMaster?
        get() = master

    override suspend fun init(param: ModbusParam): Result<ModbusMaster> =
        mutex.withLock {
            if (released) {
                return@withLock Result.failure(IllegalStateException(RELEASED_MESSAGE))
            }
            withContext(Dispatchers.IO) {
                runCatchingCancellable {
                    sendTime = 0L
                    master?.runCatching { destroy() }
                    master = null

                    val newMaster = param.createModbusMaster()
                    try {
                        newMaster.init()
                    } catch (e: ModbusInitException) {
                        Log.w(TAG, "ModbusMaster init failed", e)
                        runCatching { newMaster.destroy() }
                        throw e
                    }
                    master = newMaster
                    newMaster
                }
            }
        }

    override suspend fun close() {
        mutex.withLock {
            master?.runCatching { destroy() }
            master = null
        }
    }

    override suspend fun release() {
        mutex.withLock {
            master?.runCatching { destroy() }
            master = null
            released = true
        }
    }

    override suspend fun readCoil(
        slaveId: Int,
        start: Int,
        len: Int,
    ): Result<ReadCoilsResponse> =
        execute {
            send(ReadCoilsRequest(slaveId, start, len))
        }

    override suspend fun readDiscreteInput(
        slaveId: Int,
        start: Int,
        len: Int,
    ): Result<ReadDiscreteInputsResponse> =
        execute {
            send(ReadDiscreteInputsRequest(slaveId, start, len))
        }

    override suspend fun readHoldingRegisters(
        slaveId: Int,
        start: Int,
        len: Int,
    ): Result<ReadHoldingRegistersResponse> =
        execute {
            send(ReadHoldingRegistersRequest(slaveId, start, len))
        }

    override suspend fun readInputRegisters(
        slaveId: Int,
        start: Int,
        len: Int,
    ): Result<ReadInputRegistersResponse> =
        execute {
            send(ReadInputRegistersRequest(slaveId, start, len))
        }

    override suspend fun writeCoil(
        slaveId: Int,
        offset: Int,
        value: Boolean,
    ): Result<WriteCoilResponse> =
        execute {
            send(WriteCoilRequest(slaveId, offset, value))
        }

    override suspend fun writeSingleRegister(
        slaveId: Int,
        offset: Int,
        value: Int,
    ): Result<WriteRegisterResponse> =
        execute {
            send(WriteRegisterRequest(slaveId, offset, value))
        }

    override suspend fun writeInt32(
        slaveId: Int,
        offset: Int,
        value: Int,
    ): Result<WriteInt32Response> =
        execute {
            val udpMaster = master as? AppUdpMaster
                ?: throw UnsupportedOperationException("Custom Int32 function 06 requires AppUdpMaster")
            udpMaster.writeInt32(slaveId, offset, value)
        }

    override suspend fun writeCoils(
        slaveId: Int,
        start: Int,
        values: BooleanArray,
    ): Result<WriteCoilsResponse> =
        execute {
            send(WriteCoilsRequest(slaveId, start, values))
        }

    override suspend fun writeRegisters(
        slaveId: Int,
        start: Int,
        values: ShortArray,
    ): Result<WriteRegistersResponse> =
        execute {
            send(WriteRegistersRequest(slaveId, start, values))
        }

    override suspend fun writeRegistersButOne(
        slaveId: Int,
        start: Int,
        value: Int,
    ): Result<WriteRegistersResponse> = writeRegisters(slaveId, start, shortArrayOf(value.toShort()))

    private suspend fun <T> execute(block: () -> T): Result<T> =
        mutex.withLock {
            if (released) {
                return@withLock Result.failure(IllegalStateException(RELEASED_MESSAGE))
            }
            applySendInterval()
            val result =
                withContext(Dispatchers.IO) {
                    runCatchingCancellable { block() }
                }
            if (result.isSuccess) {
                sendTime = SystemClock.uptimeMillis()
            }
            result
        }

    private suspend fun applySendInterval() {
        val interval = sendIntervalTime
        if (interval > 0 && sendTime > 0) {
            val elapsed = SystemClock.uptimeMillis() - sendTime
            val remaining = interval - elapsed
            if (remaining > 0) {
                delay(remaining)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : ModbusResponse> send(request: ModbusRequest): T {
        val current = master ?: throw ModbusInitException(NO_INIT_MESSAGE)
        val response = current.send(request) as T
        if (response.isException) {
            throw ModbusRespException(response)
        }
        return response
    }

    /**
     * Like [runCatching] but rethrows [CancellationException] so coroutine cancellation
     * propagates instead of being silently captured into a [Result.failure].
     */
    private inline fun <T> runCatchingCancellable(block: () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (c: CancellationException) {
            throw c
        } catch (t: Throwable) {
            Result.failure(t)
        }

    companion object {
        private const val TAG = "ModbusWorker"
        private const val NO_INIT_MESSAGE = "ModbusMaster hasn't been inited!"
        private const val RELEASED_MESSAGE = "ModbusWorker has been released"
    }
}
