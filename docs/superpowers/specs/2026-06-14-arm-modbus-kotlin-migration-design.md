# arm-modbus Kotlin 化设计

**日期**: 2026-06-14
**状态**: 已批准，待实施

## 1. 背景

`arm-modbus` 是 ARM 项目中新增的模块（git status 显示为新增），当前以 Java 实现，包含 9 个 `.java` 文件，约 1000 行代码。模块封装 modbus4j 库，提供 Modbus RTU/TCP/UDP 通信能力。

代码存在以下问题：

- 使用已在 API 30 弃用的 `AsyncTask` 处理异步回调
- 使用 `ExecutorService` + `Future` + `SystemClock.sleep` 阻塞调用线程
- 使用 Java callback 接口（`onSuccess`/`onFailure`/`onFinally`）传递结果
- Java fluent setter API 风格与项目其他 Kotlin 模块（`arm-mvi`、`arm-keyboard`）不一致
- `ModbusRespException` 是 Java checked exception，Kotlin 调用方需要冗余 try-catch

## 2. 目标

- 将整个 `arm-modbus` 模块改为 Kotlin 实现
- 用协程 + `Result<T>` 替代 `AsyncTask` + 回调
- 删除 `ModbusCallback` 接口
- 异步 API 全面 `suspend` 化
- 不引入新的运行时依赖（已有 modbus4j、android-serialport），**新增** `kotlinx-coroutines` 依赖
- 保持发布坐标 `io.github.iur:arm-modbus:1.0.0` 不变（模块尚未发布）
- 保留命名空间 `io.github.iur.arm.modbus`
- 提供工厂函数 `create(...)` 兼容原有创建方式

## 3. 模块构建配置

### 3.1 版本目录

`gradle/libs.versions.toml` 的 `[plugins]` 区段新增：

```toml
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
```

### 3.2 模块 build.gradle.kts

参考 `arm-keyboard/build.gradle.kts` 的结构。在 `arm-modbus/build.gradle.kts` 中：

- 在 `plugins {}` 中加入 `alias(libs.plugins.kotlin.android)`
- `dependencies {}` 中加入：
  ```kotlin
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
  ```
- 其他 Android 配置保持不变

## 4. 文件迁移

```
src/main/java/io/github/iur/arm/modbus/        →  src/main/kotlin/io/github/iur/arm/modbus/
├── IModbusWorker.java          →  ModbusWorker.kt           (interface)
├── ModbusWorker.java           →  DefaultModbusWorker.kt    (class)
├── ModbusParam.java            →  ModbusParam.kt            (interface)
├── ModbusCallback.java         →  [删除]
├── ModbusRespException.java    →  ModbusRespException.kt
├── AndroidSerialPortWrapper.java → AndroidSerialPortWrapper.kt
└── param/
    ├── SerialParam.java        →  SerialParam.kt
    ├── TcpParam.java           →  TcpParam.kt
    └── UdpParam.java           →  UdpParam.kt
```

迁移完成后删除 `src/main/java/io/github/iur/arm/modbus/` 目录下所有 Java 文件。

测试源集（`src/test/java`、`src/androidTest/java`）的 Kotlin 文件保持原位（已是 Kotlin），**不**改动。

## 5. API 设计

### 5.1 ModbusWorker（接口）

```kotlin
package io.github.iur.arm.modbus

import com.serotonin.modbus4j.ModbusMaster
import com.serotonin.modbus4j.msg.*

interface ModbusWorker {
    val isOpened: Boolean
    val modbusMaster: ModbusMaster?
    var sendIntervalTime: Long

    suspend fun init(param: ModbusParam): Result<ModbusMaster>
    suspend fun close()
    suspend fun release()

    suspend fun readCoil(slaveId: Int, start: Int, len: Int): Result<ReadCoilsResponse>
    suspend fun readDiscreteInput(slaveId: Int, start: Int, len: Int): Result<ReadDiscreteInputsResponse>
    suspend fun readHoldingRegisters(slaveId: Int, start: Int, len: Int): Result<ReadHoldingRegistersResponse>
    suspend fun readInputRegisters(slaveId: Int, start: Int, len: Int): Result<ReadInputRegistersResponse>

    suspend fun writeCoil(slaveId: Int, offset: Int, value: Boolean): Result<WriteCoilResponse>
    suspend fun writeSingleRegister(slaveId: Int, offset: Int, value: Int): Result<WriteRegisterResponse>
    suspend fun writeCoils(slaveId: Int, start: Int, values: BooleanArray): Result<WriteCoilsResponse>
    suspend fun writeRegisters(slaveId: Int, start: Int, values: ShortArray): Result<WriteRegistersResponse>
    suspend fun writeRegistersButOne(slaveId: Int, start: Int, value: Int): Result<WriteRegistersResponse>
}
```

错误处理：所有可能失败的方法返回 `Result<T>`，失败时 `Throwable` 可能为：

- `ModbusInitException` — 未初始化或初始化失败
- `ModbusTransportException` — 传输失败
- `ModbusRespException` — 响应包含错误（modbus 异常码）
- `IllegalStateException` — 已 release

### 5.2 DefaultModbusWorker（实现）

关键点：

- 内部用 `kotlinx.coroutines.sync.Mutex` 序列化所有 modbus 请求（替代 `newSingleThreadExecutor`）
- 实际 IO 工作通过 `withContext(Dispatchers.IO)` 调度
- `sendIntervalTime` 节流通过 `delay()` 实现，不再用 `SystemClock.sleep`
- 所有 modbus 操作走统一的私有 helper：

```kotlin
class DefaultModbusWorker : ModbusWorker {

    private val mutex = Mutex()
    private var master: ModbusMaster? = null
    private var sendTime: Long = 0L
    @Volatile private var released: Boolean = false

    override var sendIntervalTime: Long = 0L
        set(value) {
            require(value >= 0) { "Send interval time should not be negative, but now ms=$value" }
            field = value
        }

    override val isOpened: Boolean get() = master != null
    override val modbusMaster: ModbusMaster? get() = master

    override suspend fun init(param: ModbusParam): Result<ModbusMaster> = execute {
        sendTime = 0L
        master?.runCatching { destroy() }
        master = null

        val newMaster = param.createModbusMaster()
            ?: throw ModbusInitException("Invalid ModbusParam!")
        try {
            newMaster.init()
        } catch (e: ModbusInitException) {
            runCatching { newMaster.destroy() }
            throw e
        }
        master = newMaster
        newMaster
    }

    override suspend fun close() = mutex.withLock {
        master?.runCatching { destroy() }
        master = null
    }

    override suspend fun release() = mutex.withLock {
        master?.runCatching { destroy() }
        master = null
        released = true
    }

    override suspend fun readCoil(slaveId: Int, start: Int, len: Int) = execute {
        val req = ReadCoilsRequest(slaveId, start, len)
        sendChecked<ReadCoilsResponse>(req)
    }

    // ... 其余 modbus 操作类似

    private suspend fun <T> execute(block: () -> T): Result<T> = mutex.withLock {
        if (released) {
            return@withLock Result.failure(IllegalStateException("ModbusWorker has been released"))
        }
        applySendInterval()
        val result = withContext(Dispatchers.IO) {
            runCatching { block() }
        }
        if (result.isSuccess) {
            sendTime = SystemClock.uptimeMillis()
        }
        result
    }

    private suspend fun applySendInterval() {
        if (sendIntervalTime > 0 && sendTime > 0) {
            val offset = sendIntervalTime - (SystemClock.uptimeMillis() - sendTime)
            if (offset > 0) delay(offset)
        }
    }

    private inline fun <reified R : ModbusResponse> sendChecked(request: ModbusRequest): R {
        val m = master ?: throw ModbusInitException(NO_INIT_MESSAGE)
        val response = m.send(request) as R
        if (response.isException) throw ModbusRespException(response)
        return response
    }

    companion object {
        private const val NO_INIT_MESSAGE = "ModbusMaster hasn't been inited!"
    }
}
```

注意：原 `doSync` 内部存在 `(getSendIntervalTime() - SystemClock.uptimeMillis() - mSendTime)` 的间隔计算公式 bug（实际应是 `interval - (now - sendTime)`），上面 `applySendInterval()` 顺便修正。

### 5.3 ModbusParam（接口）

简化掉原递归泛型 `<T extends ModbusParam>`，改用 `var` 属性：

```kotlin
package io.github.iur.arm.modbus

import com.serotonin.modbus4j.ModbusMaster

interface ModbusParam {
    var timeout: Int
    var retries: Int
    fun createModbusMaster(): ModbusMaster

    companion object {
        const val DEFAULT_TIMEOUT = 500
        const val DEFAULT_RETRIES = 2
    }
}
```

调用方使用 `apply {}` 配置：

```kotlin
val param = SerialParam.create("/dev/ttyS0", 9600).apply {
    timeout = 1000
    retries = 3
}
```

### 5.4 param 子包

**SerialParam.kt**

```kotlin
class SerialParam private constructor(
    var serialDevice: String,
    var baudRate: Int,
) : ModbusParam {
    override var timeout: Int = ModbusParam.DEFAULT_TIMEOUT
    override var retries: Int = ModbusParam.DEFAULT_RETRIES
    var dataBits: Int = 8
    var parity: Int = 0
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
        fun create(serialDevice: String, baudRate: Int) = SerialParam(serialDevice, baudRate)
    }
}
```

**TcpParam.kt**

```kotlin
class TcpParam private constructor(host: String, port: Int) : ModbusParam {
    private val ipParameters = IpParameters().apply {
        this.host = host
        this.port = port
    }

    override var timeout: Int = ModbusParam.DEFAULT_TIMEOUT
    override var retries: Int = ModbusParam.DEFAULT_RETRIES
    var keepAlive: Boolean = false

    var host: String
        get() = ipParameters.host
        set(value) { ipParameters.host = value }

    var port: Int
        get() = ipParameters.port
        set(value) { ipParameters.port = value }

    var encapsulated: Boolean
        get() = ipParameters.isEncapsulated
        set(value) { ipParameters.setEncapsulated(value) }

    override fun createModbusMaster(): ModbusMaster {
        val factory = ModbusFactory()
        return factory.createTcpMaster(ipParameters, keepAlive).apply {
            setRetries(this@TcpParam.retries)
            setTimeout(this@TcpParam.timeout)
        }
    }

    companion object {
        @JvmStatic
        fun create(host: String, port: Int) = TcpParam(host, port)
    }
}
```

**UdpParam.kt** — 与 TcpParam 同构，使用 `UdpMaster`，新增 `validateResponse: Boolean` 属性。

### 5.5 ModbusRespException

保留为 `Exception` 子类（仍是 `Throwable`，可放进 `Result.failure`）：

```kotlin
package io.github.iur.arm.modbus

import com.serotonin.modbus4j.msg.ModbusResponse

class ModbusRespException(response: ModbusResponse) :
    Exception("${response.exceptionMessage} ,code=${response.exceptionCode}") {
    val exceptionCode: Byte = response.exceptionCode
}
```

### 5.6 AndroidSerialPortWrapper

实现 modbus4j 的 Java 接口 `SerialPortWrapper`，转换为 Kotlin 类，行为不变：

```kotlin
class AndroidSerialPortWrapper(
    private val device: String,
    private val baudRate: Int,
    private val dataBits: Int,
    private val parity: Int,
    private val stopBits: Int,
) : SerialPortWrapper {

    private var inputStream: BufferedInputStream? = null
    private var outputStream: BufferedOutputStream? = null
    private var serialPort: SerialPort? = null

    override fun open() {
        val port = SerialPort.newBuilder(device, baudRate)
            .parity(parity)
            .dataBits(dataBits)
            .stopBits(stopBits)
            .build()
        serialPort = port
        inputStream = BufferedInputStream(port.inputStream)
        outputStream = BufferedOutputStream(port.outputStream)
    }

    override fun close() {
        inputStream?.runCatching { close() }
        outputStream?.runCatching { close() }
        serialPort?.runCatching { close() }
        inputStream = null
        outputStream = null
        serialPort = null
    }

    override fun getInputStream(): InputStream? = inputStream
    override fun getOutputStream(): OutputStream? = outputStream
    override fun getBaudRate(): Int = baudRate
    override fun getDataBits(): Int = dataBits
    override fun getStopBits(): Int = stopBits
    override fun getParity(): Int = parity
}
```

## 6. 数据流

```
caller (coroutine)
  → worker.readCoil(slaveId, start, len)        [suspend]
    → mutex.withLock {                          [序列化所有请求]
      → withContext(Dispatchers.IO) {           [迁移到 IO 线程]
        → runCatching {
          → applySendInterval()                 [delay() 节流]
          → master.send(req) as ReadCoilsResponse
          → if (response.isException) throw ModbusRespException(response)
          → response
        }
      }
    }
  ← Result<ReadCoilsResponse>
```

调用方典型用法：

```kotlin
viewModelScope.launch {
    val worker = DefaultModbusWorker()
    val param = TcpParam.create("192.168.1.100", 502).apply {
        timeout = 1000
        retries = 3
    }
    worker.init(param)
        .onSuccess { /* ... */ }
        .onFailure { e -> log("init failed", e) }

    worker.readHoldingRegisters(1, 0, 10)
        .onSuccess { resp -> /* use resp.shortData */ }
        .onFailure { e -> log("read failed", e) }
}
```

## 7. 测试

模块当前测试是脚手架级别的 `ExampleUnitTest.kt` / `ExampleInstrumentedTest.kt`，本次不新增功能测试（迁移目标是行为等价，且 modbus 通信测试需要真实硬件）。如果后续要补，建议：

- ViewModel 层用 fake `ModbusWorker` 实现进行测试
- 集成测试需要真实串口或 modbus simulator

## 8. 风险与注意事项

1. **二进制兼容性**: 模块未发布，无下游使用方，可随意 break API
2. **AsyncTask 移除**: 不再依赖 Android UI 线程回调，调用方必须在协程作用域内调用
3. **modbus4j 仍是 Java 库**: 不做替换，仅 wrapper 层 Kotlin 化
4. **`SystemClock` 依赖**: 保留 Android `SystemClock.uptimeMillis()` 用于节流计算（与原行为一致）
5. **泛型简化**: `ModbusParam<T extends ModbusParam>` 改为 `ModbusParam`，破坏不存在（无现有调用方）
6. **节流公式 bug 修正**: 原 Java 实现 `getSendIntervalTime() - SystemClock.uptimeMillis() - mSendTime` 实际计算结果总是负数（除非 `sendIntervalTime` 大于绝对时间戳），等同于禁用节流。Kotlin 实现修正为 `interval - (now - sendTime)`

## 9. 实施清单

1. 更新 `gradle/libs.versions.toml` 加入 `kotlin-android` 插件别名
2. 重写 `arm-modbus/build.gradle.kts`：加入 kotlin-android 插件 + coroutines 依赖
3. 创建 `src/main/kotlin/` 目录
4. 按 §4 文件映射逐个迁移：
   - `ModbusRespException.kt`
   - `ModbusParam.kt`
   - `AndroidSerialPortWrapper.kt`
   - `param/SerialParam.kt`、`param/TcpParam.kt`、`param/UdpParam.kt`
   - `ModbusWorker.kt`（接口）
   - `DefaultModbusWorker.kt`
5. 删除 `src/main/java/io/github/iur/arm/modbus/` 下全部 `.java` 文件（含 `ModbusCallback.java`）
6. 运行 `./gradlew :arm-modbus:assembleRelease` 验证编译通过
7. 运行 `./gradlew :arm-modbus:test` 验证现有测试通过
