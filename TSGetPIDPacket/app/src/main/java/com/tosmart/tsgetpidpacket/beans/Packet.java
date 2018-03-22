package com.tosmart.tsgetpidpacket.beans;

/**
 * MainActivity
 *
 * @author ggz
 * @date 2018/3/22
 */

public class Packet {

    /**
     * sync_byte : 8 bit
     * 同步字节
     */
    private int syncByte;

    /**
     * transport_error_indicator : 1 bit
     * 错误指示信息（1：该包至少有 1bits 传输错误）
     */
    private int transportErrorIndicator;

    /**
     * payload_unit_start_indicator : 1 bit
     * 负载单元开始标志（packet不满188字节时需填充）
     */
    private int payloadUnitStartIndicator;

    /**
     * transport_priority : 1 bit
     * 传输优先级标志（1：优先级高）
     */
    private int transportPriority;

    /**
     * PID : 13 bit
     * 传输优先级标志（1：优先级高）
     */
    private int pid;

    /**
     * transport_scrambling_control : 2 bit
     * 加密标志（00：未加密；其他表示已加密）
     */
    private int transportScramblingControl;

    /**
     * adaptation_field_control : 2 bit
     * 附加区域控制
     */
    private int adaptationFieldControl;

    /**
     * continuity_counter : 4 bit
     * 包递增计数器
     */
    private int continuityCounter;


    /**
     * 包操作
     */
    private byte[] packet;


    /**
     * 构造函数
     */
    public Packet(byte[] buff) {
        super();
        this.syncByte = buff[0] & 0xFF;
        this.transportErrorIndicator = (buff[1] >> 7) & 0x1;
        this.payloadUnitStartIndicator = (buff[1] >> 6) & 0x1;
        this.transportPriority = (buff[1] >> 5) & 0x1;
        this.pid = ((buff[1] & 0x1F) << 8 | buff[2]) & 0x1FFF;
        this.transportScramblingControl = (buff[3] >> 6) & 0x3;
        this.adaptationFieldControl = (buff[3] >> 4) & 0x3;
        this.continuityCounter = buff[3] & 0xF;

        this.packet = buff;
    }


    public int getSyncByte() {
        return syncByte;
    }

    public void setSyncByte(int syncByte) {
        this.syncByte = syncByte;
    }

    public int getTransportErrorIndicator() {
        return transportErrorIndicator;
    }

    public void setTransportErrorIndicator(int transportErrorIndicator) {
        this.transportErrorIndicator = transportErrorIndicator;
    }

    public int getPayloadUnitStartIndicator() {
        return payloadUnitStartIndicator;
    }

    public void setPayloadUnitStartIndicator(int payloadUnitStartIndicator) {
        this.payloadUnitStartIndicator = payloadUnitStartIndicator;
    }

    public int getTransportPriority() {
        return transportPriority;
    }

    public void setTransportPriority(int transportPriority) {
        this.transportPriority = transportPriority;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getTransportScramblingControl() {
        return transportScramblingControl;
    }

    public void setTransportScramblingControl(int transportScramblingControl) {
        this.transportScramblingControl = transportScramblingControl;
    }

    public int getAdaptationFieldControl() {
        return adaptationFieldControl;
    }

    public void setAdaptationFieldControl(int adaptationFieldControl) {
        this.adaptationFieldControl = adaptationFieldControl;
    }

    public int getContinuityCounter() {
        return continuityCounter;
    }

    public void setContinuityCounter(int continuityCounter) {
        this.continuityCounter = continuityCounter;
    }

    public byte[] getPacket() {
        return packet;
    }

    public void setPacket(byte[] packet) {
        this.packet = packet;
    }
}
