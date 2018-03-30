package com.tosmart.tsgetpat.beans;

/**
 * Section
 *
 * @author ggz
 * @date 2018/3/22
 */

public class Section {

    /**
     * tableId 8 bit
     */
    private int tableId = -1;

    /**
     * sectionLength 12 bit
     */
    private int sectionLength = 0;

    /**
     * versionNumber 5 bit
     */
    private int versionNumber;

    /**
     * sectionNumber 8 bit
     */
    private int sectionNumber;

    /**
     * lastSectionNumber 8 bit
     */
    private int lastSectionNumber;

    /**
     * sectionData 操作
     */
    private byte[] sectionData;

    /**
     * 构造函数
     */
    public Section() {
        super();
    }

    public Section(byte[] sectionBuff) {
        super();

        int tableId = sectionBuff[0] & 0xFF;
        int sectionLength = (((sectionBuff[1] & 0xF) << 8) | (sectionBuff[2] & 0xFF)) & 0xFFF;
        int versionNumber = (sectionBuff[5] >> 1) & 0x1F;
        int sectionNumber = sectionBuff[6] & 0xFF;
        int lastSectionNumber = sectionBuff[7] & 0xFF;

        this.tableId = tableId;
        this.sectionLength = sectionLength;
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
    public byte[] getSectionData() {
        return sectionData;
    }

    public void setSectionData(byte[] sectionData) {
        this.sectionData = sectionData;
    }

}
