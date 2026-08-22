package com.serotonin.modbus4j.msg;

import com.serotonin.modbus4j.base.ModbusUtils;
import com.serotonin.modbus4j.code.FunctionCode;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.sero.util.queue.ByteQueue;

/**
 * Response for the MCU-specific function 06 extension whose register value is a big-endian Int32.
 */
public class WriteInt32Response extends ModbusResponse {
    private int writeOffset;
    private int writeValue;

    public WriteInt32Response(int slaveId) throws ModbusTransportException {
        super(slaveId);
    }

    public WriteInt32Response(int slaveId, int writeOffset, int writeValue) throws ModbusTransportException {
        super(slaveId);
        this.writeOffset = writeOffset;
        this.writeValue = writeValue;
    }

    @Override
    public byte getFunctionCode() {
        return FunctionCode.WRITE_REGISTER;
    }

    @Override
    protected void writeResponse(ByteQueue queue) {
        ModbusUtils.pushShort(queue, writeOffset);
        queue.push((byte) (writeValue >> 24));
        queue.push((byte) (writeValue >> 16));
        queue.push((byte) (writeValue >> 8));
        queue.push((byte) writeValue);
    }

    @Override
    protected void readResponse(ByteQueue queue) {
        writeOffset = ModbusUtils.popUnsignedShort(queue);
        writeValue = (ModbusUtils.popUnsignedByte(queue) << 24)
                | (ModbusUtils.popUnsignedByte(queue) << 16)
                | (ModbusUtils.popUnsignedByte(queue) << 8)
                | ModbusUtils.popUnsignedByte(queue);
    }

    public int getWriteOffset() {
        return writeOffset;
    }

    public int getWriteValue() {
        return writeValue;
    }
}
