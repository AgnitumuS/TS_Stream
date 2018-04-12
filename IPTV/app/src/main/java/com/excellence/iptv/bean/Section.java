package com.excellence.iptv.bean;

/**
 * Section
 *
 * @author ggz
 * @date 2018/3/22
 */

public class Section {

    private int tableId;
    private int sectionLength = 0;
    private int transportStreamIdOrServiceId;
    private int versionNumber;
    private int sectionNumber;
    private int lastSectionNumber;


    private int sectionCursor = 0;
    private int nextContinuityCounter = 0;
    private byte[] sectionData;


    /**
     * 构造函数
     */
    public Section(int tableId, int sectionLength, int transportStreamIdOrServiceId,
                   int versionNumber, int sectionNumber, int lastSectionNumber) {
        super();
        this.tableId = tableId;
        this.sectionLength = sectionLength;
        this.transportStreamIdOrServiceId = transportStreamIdOrServiceId;
        this.versionNumber = versionNumber;
        this.sectionNumber = sectionNumber;
        this.lastSectionNumber = lastSectionNumber;

        sectionData = new byte[sectionLength + 3];
    }


    public Section(byte[] sectionBuff) {
        super();

        int tableId = sectionBuff[0] & 0xFF;
        int sectionLength = (((sectionBuff[1] & 0xF) << 8) | (sectionBuff[2] & 0xFF)) & 0xFFF;
        int transportStreamIdOrServiceId = (((sectionBuff[3] & 0xFF) << 8) |
                (sectionBuff[4] & 0xFF)) & 0xFFFF;
        int versionNumber = (sectionBuff[5] >> 1) & 0x1F;
        int sectionNumber = sectionBuff[6] & 0xFF;
        int lastSectionNumber = sectionBuff[7] & 0xFF;

        this.tableId = tableId;
        this.sectionLength = sectionLength;
        this.transportStreamIdOrServiceId = transportStreamIdOrServiceId;
        this.versionNumber = versionNumber;
        this.sectionNumber = sectionNumber;
        this.lastSectionNumber = lastSectionNumber;
        this.sectionData = sectionBuff;
    }


    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public int getSectionLength() {
        return sectionLength;
    }

    public void setSectionLength(int sectionLength) {
        this.sectionLength = sectionLength;
    }

    public int getTransportStreamIdOrServiceId() {
        return transportStreamIdOrServiceId;
    }

    public void setTransportStreamIdOrServiceId(int transportStreamIdOrServiceId) {
        this.transportStreamIdOrServiceId = transportStreamIdOrServiceId;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }


    public int getSectionNumber() {
        return sectionNumber;
    }

    public void setSectionNumber(int sectionNumber) {
        this.sectionNumber = sectionNumber;
    }

    public int getLastSectionNumber() {
        return lastSectionNumber;
    }

    public void setLastSectionNumber(int lastSectionNumber) {
        this.lastSectionNumber = lastSectionNumber;
    }


    /**
     * sectionData 操作
     */

    public int getSectionCursor() {
        return sectionCursor;
    }

    public void setSectionCursor(int sectionCursor) {
        this.sectionCursor = sectionCursor;
    }

    public int getNextContinuityCounter() {
        return nextContinuityCounter;
    }

    public void setNextContinuityCounter(int nextContinuityCounter) {
        this.nextContinuityCounter = nextContinuityCounter;
    }

    public byte[] getSectionData() {
        return sectionData;
    }

    public void setSectionData(byte[] sectionData) {
        this.sectionData = sectionData;
    }

}
