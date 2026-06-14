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
        val port =
            SerialPort
                .newBuilder(device, baudRate)
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
