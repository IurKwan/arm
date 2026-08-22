package io.github.iur.arm.modbus

import com.serotonin.modbus4j.exception.ModbusTransportException

internal object McuInt32FrameCodec {
    private const val FUNCTION_WRITE_REGISTER = 0x06
    private const val EXCEPTION_FLAG = 0x80
    private const val REQUEST_SIZE = 10
    private const val EXCEPTION_SIZE = 5

    @JvmStatic
    fun encode(slaveId: Int, offset: Int, value: Int): ByteArray {
        require(slaveId in 0..0xFF) { "Invalid slave id: $slaveId" }
        require(offset in 0..0xFFFF) { "Invalid offset: $offset" }

        val frame =
            byteArrayOf(
                slaveId.toByte(),
                FUNCTION_WRITE_REGISTER.toByte(),
                (offset ushr 8).toByte(),
                offset.toByte(),
                (value ushr 24).toByte(),
                (value ushr 16).toByte(),
                (value ushr 8).toByte(),
                value.toByte(),
                0,
                0,
            )
        val crc = crc16(frame, REQUEST_SIZE - 2)
        frame[REQUEST_SIZE - 2] = crc.toByte()
        frame[REQUEST_SIZE - 1] = (crc ushr 8).toByte()
        return frame
    }

    @JvmStatic
    @Throws(ModbusTransportException::class, ModbusRespException::class)
    fun decodeEcho(response: ByteArray, expected: ByteArray): WriteInt32Response {
        validateCrc(response)
        val function = response[1].toInt() and 0xFF
        if (function == (FUNCTION_WRITE_REGISTER or EXCEPTION_FLAG)) {
            if (response.size != EXCEPTION_SIZE) {
                throw ModbusTransportException("Invalid exception response length: ${response.size}")
            }
            throw ModbusRespException(response[2])
        }
        if (!response.contentEquals(expected)) {
            throw ModbusTransportException("Int32 function 06 response does not echo request")
        }

        return WriteInt32Response(
            slaveId = response[0].toInt() and 0xFF,
            offset = ((response[2].toInt() and 0xFF) shl 8) or (response[3].toInt() and 0xFF),
            value =
                ((response[4].toInt() and 0xFF) shl 24) or
                    ((response[5].toInt() and 0xFF) shl 16) or
                    ((response[6].toInt() and 0xFF) shl 8) or
                    (response[7].toInt() and 0xFF),
        )
    }

    private fun validateCrc(frame: ByteArray) {
        if (frame.size < EXCEPTION_SIZE) {
            throw ModbusTransportException("Invalid Int32 function 06 response length: ${frame.size}")
        }
        val expectedCrc = crc16(frame, frame.size - 2)
        val actualCrc =
            (frame[frame.size - 2].toInt() and 0xFF) or
                ((frame[frame.size - 1].toInt() and 0xFF) shl 8)
        if (actualCrc != expectedCrc) {
            throw ModbusTransportException("CRC mismatch: given=$actualCrc, calc=$expectedCrc")
        }
    }

    private fun crc16(bytes: ByteArray, length: Int): Int {
        var crc = 0xFFFF
        repeat(length) { index ->
            crc = crc xor (bytes[index].toInt() and 0xFF)
            repeat(8) {
                crc = if ((crc and 1) != 0) (crc ushr 1) xor 0xA001 else crc ushr 1
            }
        }
        return crc
    }
}
