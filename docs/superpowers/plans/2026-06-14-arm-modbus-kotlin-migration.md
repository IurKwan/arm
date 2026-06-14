# arm-modbus Kotlin 化实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 `arm-modbus` 模块从 Java 全量迁移到 Kotlin，使用协程 + `Result<T>` 替代 `AsyncTask` + 回调。

**Architecture:** 9 个 Java 文件逐个改写为 Kotlin。`ModbusWorker` 接口的 `init/read/write` 方法全部 `suspend` 化，内部通过 `kotlinx.coroutines.sync.Mutex` 序列化请求并 `withContext(Dispatchers.IO)` 调度。错误以 `Result<T>` 返回，删除 `ModbusCallback` 接口。

**Tech Stack:** Kotlin 2.3.0, kotlinx-coroutines 1.10.2, modbus4j 3.1.1-alpha1, android-serialport 2.1.3, AGP 9.0.0, Gradle 9.4.0, Java target 11

**Spec:** [`docs/superpowers/specs/2026-06-14-arm-modbus-kotlin-migration-design.md`](../specs/2026-06-14-arm-modbus-kotlin-migration-design.md)

---

## 任务概览

| Task | 内容 | 文件 |
|------|------|------|
| 1 | 添加 kotlin-android 插件别名到版本目录 | `gradle/libs.versions.toml` |
| 2 | 重写 build.gradle.kts，加入 kotlin 插件 + coroutines 依赖 | `arm-modbus/build.gradle.kts` |
| 3 | 验证空 Kotlin 源集编译通过（基线检查） | (build) |
| 4 | 迁移 `ModbusRespException` 到 Kotlin | `ModbusRespException.kt` |
| 5 | 迁移 `ModbusParam` 接口到 Kotlin（简化泛型） | `ModbusParam.kt` |
| 6 | 迁移 `AndroidSerialPortWrapper` 到 Kotlin | `AndroidSerialPortWrapper.kt` |
| 7 | 迁移 `param/SerialParam` 到 Kotlin | `param/SerialParam.kt` |
| 8 | 迁移 `param/TcpParam` 到 Kotlin | `param/TcpParam.kt` |
| 9 | 迁移 `param/UdpParam` 到 Kotlin | `param/UdpParam.kt` |
| 10 | 创建新的 `ModbusWorker` 接口（协程化） | `ModbusWorker.kt` |
| 11 | 创建 `DefaultModbusWorker` 实现 | `DefaultModbusWorker.kt` |
| 12 | 删除 `ModbusCallback.java` 与所有 Java 源 | (deletion) |
| 13 | 全模块编译 + 测试通过 | (build) |

---

## Task 1: 添加 kotlin-android 插件别名

**Files:**
- Modify: `gradle/libs.versions.toml:64-68` (`[plugins]` 区段)

- [ ] **Step 1: 编辑版本目录**

在 `[plugins]` 区段添加 `kotlin-android` 别名（其他模块如 `arm-keyboard` 实际是 Java + Kotlin 混合，但插件应用方式仅在新模块需要时加载）。

打开 `gradle/libs.versions.toml`，在 `[plugins]` 区段（当前 64-68 行）末尾添加一行：

```toml
[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
android-library = { id = "com.android.library", version.ref = "agp" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
```

- [ ] **Step 2: 验证 Gradle 配置可解析**

Run: `./gradlew :arm-modbus:help -q`
Expected: 配置成功，无报错（此时 arm-modbus 还没用到该插件，只验证目录解析）。

- [ ] **Step 3: Commit**

```bash
git add gradle/libs.versions.toml
git commit -m "chore: add kotlin-android plugin alias to version catalog"
```

---

## Task 2: 重写 arm-modbus build.gradle.kts

**Files:**
- Modify: `arm-modbus/build.gradle.kts`

- [ ] **Step 1: 重写 build.gradle.kts**

完全替换 `arm-modbus/build.gradle.kts` 内容为：

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    `maven-publish`
}

android {
    namespace = "io.github.iur.arm.modbus"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    // kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    // 串口
    implementation("com.licheedev:android-serialport:2.1.3")
    // modbus4j
    implementation("com.github.licheedev:modbus4j:3.1.1-alpha1")
}


afterEvaluate {
    tasks.register<Jar>("androidSourcesJar") {
        archiveClassifier.set("sources")

        // Android 已统一 Java/Kotlin 源集，这里包含 Java + Kotlin 文件夹
        from(android.sourceSets["main"].java.srcDirs)

        // 额外包含 manifest 与 res（可选）
        from("src/main/aidl")
        from("src/main/manifest")
    }

    artifacts {
        add("archives", tasks.named("androidSourcesJar"))
    }
}

android {
    publishing {
        singleVariant("release") {
//            withSourcesJar()
//            withJavadocJar()
        }
    }
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "io.github.iur"
            artifactId = "arm-modbus"
            version = "1.0.0"

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set("ARM Modbus")
                description.set("")
                url.set("https://github.com/your-repo/arm")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("developer")
                        name.set("Iur")
                        email.set("guanzhirui@outlook.com")
                    }
                }
            }
        }
    }

    repositories {
        maven {
            name = "aliyun"
            url =
                uri("https://packages.aliyun.com/62e88d2c1a358b4399afaf04/maven/2260669-release-lzjiju")
            credentials {
                username = "REDACTED_ALIYUN_USERNAME"
                password = "REDACTED_ALIYUN_PASSWORD"
            }
        }
    }
}
```

变更点：
- `plugins {}` 块新增 `alias(libs.plugins.kotlin.android)`
- `dependencies {}` 块新增两行 kotlinx-coroutines 依赖
- 其他保持不变

- [ ] **Step 2: 验证配置成功**

Run: `./gradlew :arm-modbus:tasks -q`
Expected: 列出 tasks，无报错。

- [ ] **Step 3: Commit**

```bash
git add arm-modbus/build.gradle.kts
git commit -m "build(arm-modbus): apply kotlin-android plugin and coroutines deps"
```

---

## Task 3: 基线编译验证

**Files:** 无修改

- [ ] **Step 1: 编译现有 Java 代码确认基线绿色**

Run: `./gradlew :arm-modbus:compileReleaseJavaWithJavac`
Expected: BUILD SUCCESSFUL（迁移前 Java 代码完整，应当编译通过）

如果失败，先修复 Java 编译，否则后续 Kotlin 迁移过程中无法判断回归来源。

- [ ] **Step 2: 创建 Kotlin 源集目录**

Run:
```bash
mkdir -p /Users/guanzhirui/rui-projects/arm/arm-modbus/src/main/kotlin/io/github/iur/arm/modbus/param
```

- [ ] **Step 3: 验证空 Kotlin 源集与 Java 共存可编译**

Run: `./gradlew :arm-modbus:assembleRelease`
Expected: BUILD SUCCESSFUL

---

## Task 4: 迁移 ModbusRespException

**Files:**
- Create: `arm-modbus/src/main/kotlin/io/github/iur/arm/modbus/ModbusRespException.kt`
- Delete: `arm-modbus/src/main/java/io/github/iur/arm/modbus/ModbusRespException.java`

- [ ] **Step 1: 创建 Kotlin 文件**

写入 `arm-modbus/src/main/kotlin/io/github/iur/arm/modbus/ModbusRespException.kt`:

```kotlin
package io.github.iur.arm.modbus

import com.serotonin.modbus4j.msg.ModbusResponse

/**
 * Modbus 响应异常
 */
class ModbusRespException(response: ModbusResponse) :
    Exception("${response.exceptionMessage} ,code=${response.exceptionCode}") {
    val exceptionCode: Byte = response.exceptionCode
}
```

- [ ] **Step 2: 删除 Java 版本**

Run: `rm /Users/guanzhirui/rui-projects/arm/arm-modbus/src/main/java/io/github/iur/arm/modbus/ModbusRespException.java`

- [ ] **Step 3: 编译验证**

Run: `./gradlew :arm-modbus:compileReleaseKotlin :arm-modbus:compileReleaseJavaWithJavac`
Expected: BUILD SUCCESSFUL（其他 Java 文件如 `ModbusWorker.java` 仍引用 `ModbusRespException`，因 Kotlin 类与 Java 类同包同名，引用应保持有效）

- [ ] **Step 4: Commit**

```bash
git add arm-modbus/src/main/kotlin/io/github/iur/arm/modbus/ModbusRespException.kt
git rm arm-modbus/src/main/java/io/github/iur/arm/modbus/ModbusRespException.java
git commit -m "refactor(arm-modbus): migrate ModbusRespException to Kotlin"
```

---

## Task 5: 迁移 ModbusParam 接口

**Files:**
- Create: `arm-modbus/src/main/kotlin/io/github/iur/arm/modbus/ModbusParam.kt`
- Delete: `arm-modbus/src/main/java/io/github/iur/arm/modbus/ModbusParam.java`

注意：Java 版本的 `ModbusParam<T extends ModbusParam>` 递归泛型移除。`SerialParam`/`TcpParam`/`UdpParam` 在后续 Task 7-9 会一起改为新的非泛型形态，故 **此 Task 期间这三个 Java 类会编译失败**。Task 5/6/7/8/9 必须连续完成才能再次 build。

- [ ] **Step 1: 创建 Kotlin 文件**

写入 `arm-modbus/src/main/kotlin/io/github/iur/arm/modbus/ModbusParam.kt`:

```kotlin
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
```

- [ ] **Step 2: 删除 Java 版本**

Run: `rm /Users/guanzhirui/rui-projects/arm/arm-modbus/src/main/java/io/github/iur/arm/modbus/ModbusParam.java`

- [ ] **Step 3: 跳过编译验证**

不在此处编译——`SerialParam.java` 等仍用旧接口签名，会失败。继续 Task 6-9 后再编译。

- [ ] **Step 4: Commit**

```bash
git add arm-modbus/src/main/kotlin/io/github/iur/arm/modbus/ModbusParam.kt
git rm arm-modbus/src/main/java/io/github/iur/arm/modbus/ModbusParam.java
git commit -m "refactor(arm-modbus): migrate ModbusParam interface to Kotlin"
```

---

## Task 6: 迁移 AndroidSerialPortWrapper

**Files:**
- Create: `arm-modbus/src/main/kotlin/io/github/iur/arm/modbus/AndroidSerialPortWrapper.kt`
- Delete: `arm-modbus/src/main/java/io/github/iur/arm/modbus/AndroidSerialPortWrapper.java`

- [ ] **Step 1: 创建 Kotlin 文件**

写入 `arm-modbus/src/main/kotlin/io/github/iur/arm/modbus/AndroidSerialPortWrapper.kt`:

```kotlin
package io.github.iur.arm.modbus

import android.serialport.SerialPort
import com.serotonin.modbus4j.serial.SerialPortWrapper
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * modbus 的 Android 串口实现
 */
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

- [ ] **Step 2: 删除 Java 版本**

Run: `rm /Users/guanzhirui/rui-projects/arm/arm-modbus/src/main/java/io/github/iur/arm/modbus/AndroidSerialPortWrapper.java`

- [ ] **Step 3: 跳过编译（继续到 Task 7-9 后再统一编译）**

- [ ] **Step 4: Commit**

```bash
git add arm-modbus/src/main/kotlin/io/github/iur/arm/modbus/AndroidSerialPortWrapper.kt
git rm arm-modbus/src/main/java/io/github/iur/arm/modbus/AndroidSerialPortWrapper.java
git commit -m "refactor(arm-modbus): migrate AndroidSerialPortWrapper to Kotlin"
```

---

## Task 7: 迁移 SerialParam

**Files:**
- Create: `arm-modbus/src/main/kotlin/io/github/iur/arm/modbus/param/SerialParam.kt`
- Delete: `arm-modbus/src/main/java/io/github/iur/arm/modbus/param/SerialParam.java`

- [ ] **Step 1: 创建 Kotlin 文件**

写入 `arm-modbus/src/main/kotlin/io/github/iur/arm/modbus/param/SerialParam.kt`:

```kotlin
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
        fun create(serialDevice: String, baudRate: Int): SerialParam =
            SerialParam(serialDevice, baudRate)
    }
}
```

- [ ] **Step 2: 删除 Java 版本**

Run: `rm /Users/guanzhirui/rui-projects/arm/arm-modbus/src/main/java/io/github/iur/arm/modbus/param/SerialParam.java`

- [ ] **Step 3: 跳过编译**

- [ ] **Step 4: Commit**

```bash
git add arm-modbus/src/main/kotlin/io/github/iur/arm/modbus/param/SerialParam.kt
git rm arm-modbus/src/main/java/io/github/iur/arm/modbus/param/SerialParam.java
git commit -m "refactor(arm-modbus): migrate SerialParam to Kotlin"
```

---

## Task 8: 迁移 TcpParam

**Files:**
- Create: `arm-modbus/src/main/kotlin/io/github/iur/arm/modbus/param/TcpParam.kt`
- Delete: `arm-modbus/src/main/java/io/github/iur/arm/modbus/param/TcpParam.java`

- [ ] **Step 1: 创建 Kotlin 文件**

写入 `arm-modbus/src/main/kotlin/io/github/iur/arm/modbus/param/TcpParam.kt`:

```kotlin
package io.github.iur.arm.modbus.param

import com.serotonin.modbus4j.ModbusFactory
import com.serotonin.modbus4j.ModbusMaster
import com.serotonin.modbus4j.ip.IpParameters
import io.github.iur.arm.modbus.ModbusParam

/**
 * TCP 参数
 */
class TcpParam private constructor(host: String, port: Int) : ModbusParam {

    private val ipParameters: IpParameters = IpParameters().apply {
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
        fun create(host: String, port: Int): TcpParam = TcpParam(host, port)
    }
}
```

- [ ] **Step 2: 删除 Java 版本**

Run: `rm /Users/guanzhirui/rui-projects/arm/arm-modbus/src/main/java/io/github/iur/arm/modbus/param/TcpParam.java`

- [ ] **Step 3: 跳过编译**

- [ ] **Step 4: Commit**

```bash
git add arm-modbus/src/main/kotlin/io/github/iur/arm/modbus/param/TcpParam.kt
git rm arm-modbus/src/main/java/io/github/iur/arm/modbus/param/TcpParam.java
git commit -m "refactor(arm-modbus): migrate TcpParam to Kotlin"
```

---

## Task 9: 迁移 UdpParam

**Files:**
- Create: `arm-modbus/src/main/kotlin/io/github/iur/arm/modbus/param/UdpParam.kt`
- Delete: `arm-modbus/src/main/java/io/github/iur/arm/modbus/param/UdpParam.java`

- [ ] **Step 1: 创建 Kotlin 文件**

写入 `arm-modbus/src/main/kotlin/io/github/iur/arm/modbus/param/UdpParam.kt`:

```kotlin
package io.github.iur.arm.modbus.param

import com.serotonin.modbus4j.ModbusMaster
import com.serotonin.modbus4j.ip.IpParameters
import com.serotonin.modbus4j.ip.udp.UdpMaster
import io.github.iur.arm.modbus.ModbusParam

/**
 * UDP 参数
 */
class UdpParam private constructor(host: String, port: Int) : ModbusParam {

    private val ipParameters: IpParameters = IpParameters().apply {
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
        set(value) { ipParameters.host = value }

    var port: Int
        get() = ipParameters.port
        set(value) { ipParameters.port = value }

    var encapsulated: Boolean
        get() = ipParameters.isEncapsulated
        set(value) { ipParameters.setEncapsulated(value) }

    override fun createModbusMaster(): ModbusMaster {
        val master = UdpMaster(ipParameters, validateResponse)
        master.setRetries(retries)
        master.setTimeout(timeout)
        return master
    }

    companion object {
        @JvmStatic
        fun create(host: String, port: Int): UdpParam = UdpParam(host, port)
    }
}
```

- [ ] **Step 2: 删除 Java 版本**

Run: `rm /Users/guanzhirui/rui-projects/arm/arm-modbus/src/main/java/io/github/iur/arm/modbus/param/UdpParam.java`

- [ ] **Step 3: 跳过编译（仍有 ModbusWorker.java 等待迁移，引用旧接口）**

- [ ] **Step 4: Commit**

```bash
git add arm-modbus/src/main/kotlin/io/github/iur/arm/modbus/param/UdpParam.kt
git rm arm-modbus/src/main/java/io/github/iur/arm/modbus/param/UdpParam.java
git commit -m "refactor(arm-modbus): migrate UdpParam to Kotlin"
```

---

## Task 10: 创建新的 ModbusWorker 接口

**Files:**
- Create: `arm-modbus/src/main/kotlin/io/github/iur/arm/modbus/ModbusWorker.kt`
- Delete: `arm-modbus/src/main/java/io/github/iur/arm/modbus/IModbusWorker.java`

替换原 `IModbusWorker` 接口为新的协程化 `ModbusWorker`。

- [ ] **Step 1: 创建 Kotlin 文件**

写入 `arm-modbus/src/main/kotlin/io/github/iur/arm/modbus/ModbusWorker.kt`:

```kotlin
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
```

- [ ] **Step 2: 删除原 IModbusWorker.java**

Run: `rm /Users/guanzhirui/rui-projects/arm/arm-modbus/src/main/java/io/github/iur/arm/modbus/IModbusWorker.java`

- [ ] **Step 3: 跳过编译**

`ModbusWorker.java` 仍存在并实现 `IModbusWorker`，删除其将在 Task 11 / Task 12 完成。

- [ ] **Step 4: Commit**

```bash
git add arm-modbus/src/main/kotlin/io/github/iur/arm/modbus/ModbusWorker.kt
git rm arm-modbus/src/main/java/io/github/iur/arm/modbus/IModbusWorker.java
git commit -m "refactor(arm-modbus): replace IModbusWorker with coroutine-based ModbusWorker"
```

---

## Task 11: 创建 DefaultModbusWorker 实现

**Files:**
- Create: `arm-modbus/src/main/kotlin/io/github/iur/arm/modbus/DefaultModbusWorker.kt`

- [ ] **Step 1: 创建 Kotlin 文件**

写入 `arm-modbus/src/main/kotlin/io/github/iur/arm/modbus/DefaultModbusWorker.kt`:

```kotlin
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
import com.serotonin.modbus4j.msg.WriteRegisterRequest
import com.serotonin.modbus4j.msg.WriteRegisterResponse
import com.serotonin.modbus4j.msg.WriteRegistersRequest
import com.serotonin.modbus4j.msg.WriteRegistersResponse
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
class DefaultModbusWorker : ModbusWorker {

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

    override suspend fun init(param: ModbusParam): Result<ModbusMaster> = mutex.withLock {
        if (released) {
            return@withLock Result.failure(IllegalStateException(RELEASED_MESSAGE))
        }
        withContext(Dispatchers.IO) {
            runCatching {
                sendTime = 0L
                master?.runCatching { destroy() }
                master = null

                val newMaster = param.createModbusMaster()
                    ?: throw ModbusInitException("Invalid ModbusParam!")
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
    ): Result<ReadCoilsResponse> = execute {
        send(ReadCoilsRequest(slaveId, start, len))
    }

    override suspend fun readDiscreteInput(
        slaveId: Int,
        start: Int,
        len: Int,
    ): Result<ReadDiscreteInputsResponse> = execute {
        send(ReadDiscreteInputsRequest(slaveId, start, len))
    }

    override suspend fun readHoldingRegisters(
        slaveId: Int,
        start: Int,
        len: Int,
    ): Result<ReadHoldingRegistersResponse> = execute {
        send(ReadHoldingRegistersRequest(slaveId, start, len))
    }

    override suspend fun readInputRegisters(
        slaveId: Int,
        start: Int,
        len: Int,
    ): Result<ReadInputRegistersResponse> = execute {
        send(ReadInputRegistersRequest(slaveId, start, len))
    }

    override suspend fun writeCoil(
        slaveId: Int,
        offset: Int,
        value: Boolean,
    ): Result<WriteCoilResponse> = execute {
        send(WriteCoilRequest(slaveId, offset, value))
    }

    override suspend fun writeSingleRegister(
        slaveId: Int,
        offset: Int,
        value: Int,
    ): Result<WriteRegisterResponse> = execute {
        send(WriteRegisterRequest(slaveId, offset, value))
    }

    override suspend fun writeCoils(
        slaveId: Int,
        start: Int,
        values: BooleanArray,
    ): Result<WriteCoilsResponse> = execute {
        send(WriteCoilsRequest(slaveId, start, values))
    }

    override suspend fun writeRegisters(
        slaveId: Int,
        start: Int,
        values: ShortArray,
    ): Result<WriteRegistersResponse> = execute {
        send(WriteRegistersRequest(slaveId, start, values))
    }

    override suspend fun writeRegistersButOne(
        slaveId: Int,
        start: Int,
        value: Int,
    ): Result<WriteRegistersResponse> = writeRegisters(slaveId, start, shortArrayOf(value.toShort()))

    private suspend fun <T> execute(block: () -> T): Result<T> = mutex.withLock {
        if (released) {
            return@withLock Result.failure(IllegalStateException(RELEASED_MESSAGE))
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

    companion object {
        private const val TAG = "ModbusWorker"
        private const val NO_INIT_MESSAGE = "ModbusMaster hasn't been inited!"
        private const val RELEASED_MESSAGE = "ModbusWorker has been released"
    }
}
```

- [ ] **Step 2: 跳过编译（旧 ModbusWorker.java 仍存在，会同名冲突——下一步删除）**

- [ ] **Step 3: Commit**

```bash
git add arm-modbus/src/main/kotlin/io/github/iur/arm/modbus/DefaultModbusWorker.kt
git commit -m "feat(arm-modbus): add DefaultModbusWorker coroutine-based implementation"
```

---

## Task 12: 删除剩余 Java 源

**Files:**
- Delete: `arm-modbus/src/main/java/io/github/iur/arm/modbus/ModbusWorker.java`
- Delete: `arm-modbus/src/main/java/io/github/iur/arm/modbus/ModbusCallback.java`
- Delete: `arm-modbus/src/main/java/io/github/iur/arm/modbus/` (空目录)
- Delete: `arm-modbus/src/main/java/io/github/iur/arm/modbus/param/` (空目录)

注意：旧 `ModbusWorker.java` 与新 Kotlin 接口 `ModbusWorker.kt` 同包同名（不同后缀但 JVM class 同名 `ModbusWorker`），必须删除。

- [ ] **Step 1: 删除 Java 源文件**

Run:
```bash
rm /Users/guanzhirui/rui-projects/arm/arm-modbus/src/main/java/io/github/iur/arm/modbus/ModbusWorker.java
rm /Users/guanzhirui/rui-projects/arm/arm-modbus/src/main/java/io/github/iur/arm/modbus/ModbusCallback.java
```

- [ ] **Step 2: 清理空目录**

Run:
```bash
rmdir /Users/guanzhirui/rui-projects/arm/arm-modbus/src/main/java/io/github/iur/arm/modbus/param 2>/dev/null
rmdir /Users/guanzhirui/rui-projects/arm/arm-modbus/src/main/java/io/github/iur/arm/modbus 2>/dev/null
rmdir /Users/guanzhirui/rui-projects/arm/arm-modbus/src/main/java/io/github/iur/arm 2>/dev/null
rmdir /Users/guanzhirui/rui-projects/arm/arm-modbus/src/main/java/io/github/iur 2>/dev/null
rmdir /Users/guanzhirui/rui-projects/arm/arm-modbus/src/main/java/io 2>/dev/null
rmdir /Users/guanzhirui/rui-projects/arm/arm-modbus/src/main/java 2>/dev/null
```

如果目录非空 `rmdir` 会失败但不阻断流程，那是良性结果（保留剩余文件）。

- [ ] **Step 3: 验证 git 状态**

Run: `git status arm-modbus/src/main/java`
Expected: 显示 ModbusWorker.java、ModbusCallback.java 被删除（其他文件已在前置 task 删除）。

- [ ] **Step 4: Commit**

```bash
git add -A arm-modbus/src/main/java
git commit -m "refactor(arm-modbus): remove Java sources after Kotlin migration"
```

---

## Task 13: 全模块编译 + 测试

**Files:** 无修改

- [ ] **Step 1: 清理构建缓存**

Run: `./gradlew :arm-modbus:clean`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: 编译 release**

Run: `./gradlew :arm-modbus:assembleRelease`
Expected: BUILD SUCCESSFUL，无 Kotlin 编译错误。

- [ ] **Step 3: 运行单元测试**

Run: `./gradlew :arm-modbus:testReleaseUnitTest`
Expected: BUILD SUCCESSFUL（仅 `ExampleUnitTest` 占位测试，应通过）

- [ ] **Step 4: 整库 assemble 检查无下游引用 break**

Run: `./gradlew assemble`
Expected: BUILD SUCCESSFUL（其他模块如 app 不依赖 arm-modbus，不应受影响；如有失败需检查）

- [ ] **Step 5: 验证 Kotlin 源文件清单**

Run:
```bash
find /Users/guanzhirui/rui-projects/arm/arm-modbus/src/main -name "*.kt" -o -name "*.java" | sort
```

Expected: 仅显示 8 个 `.kt` 文件（`ModbusRespException.kt`、`ModbusParam.kt`、`AndroidSerialPortWrapper.kt`、`ModbusWorker.kt`、`DefaultModbusWorker.kt`、`param/SerialParam.kt`、`param/TcpParam.kt`、`param/UdpParam.kt`），无 `.java` 文件。

- [ ] **Step 6: 最终 Commit (no-op 验证)**

如果前面所有步骤都已正确 commit，此步无需新 commit。运行：

```bash
git log --oneline arm-modbus/ | head -15
```

Expected: 显示前述 task 的 commits（chore/build/refactor/feat 各类型）。

---

## 总结

完成后，`arm-modbus` 模块应当：

1. 全部源代码使用 Kotlin（8 个 `.kt` 文件）
2. `ModbusCallback` 接口被删除
3. 异步 API 全面 `suspend` 化，错误以 `Result<T>` 返回
4. 不再依赖 `AsyncTask`
5. 通过 `Mutex` + `Dispatchers.IO` 实现请求序列化
6. 节流计算公式修正
7. 发布坐标保持 `io.github.iur:arm-modbus:1.0.0`
