package io.github.iur.arm.modbus.param;

import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.ip.IpParameters;
import com.serotonin.modbus4j.ip.udp.UdpMaster;

import io.github.iur.arm.modbus.ModbusParam;

/**
 * UDP参数
 */
public class UdpParam implements ModbusParam<UdpParam> {

    private final IpParameters mParameters;
    /**
     * 超时
     */
    private int timeout = 500;
    /**
     * 重试
     */
    private int retries = 2;
    /**
     * 是否验证响应中的从站ID
     */
    private boolean validateResponse;

    private UdpParam(String host, int port) {
        mParameters = new IpParameters();
        mParameters.setHost(host);
        mParameters.setPort(port);
    }

    public static UdpParam create(String host, int port) {
        return new UdpParam(host, port);
    }

    public String getHost() {
        return mParameters.getHost();
    }

    public UdpParam setHost(String host) {
        mParameters.setHost(host);
        return this;
    }

    public int getPort() {
        return mParameters.getPort();
    }

    public UdpParam setPort(int port) {
        mParameters.setPort(port);
        return this;
    }

    public boolean isEncapsulated() {
        return mParameters.isEncapsulated();
    }

    public UdpParam setEncapsulated(boolean encapsulated) {
        mParameters.setEncapsulated(encapsulated);
        return this;
    }

    public int getTimeout() {
        return timeout;
    }

    public UdpParam setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public int getRetries() {
        return retries;
    }

    public UdpParam setRetries(int retries) {
        this.retries = retries;
        return this;
    }

    public boolean isValidateResponse() {
        return validateResponse;
    }

    public UdpParam setValidateResponse(boolean validateResponse) {
        this.validateResponse = validateResponse;
        return this;
    }

    @Override
    public ModbusMaster createModbusMaster() {
        UdpMaster master = new UdpMaster(mParameters, isValidateResponse());
        master.setRetries(getRetries());
        master.setTimeout(getTimeout());
        return master;
    }
}
