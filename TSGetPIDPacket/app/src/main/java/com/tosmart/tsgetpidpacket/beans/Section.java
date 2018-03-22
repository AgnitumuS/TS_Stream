package com.tosmart.tsgetpidpacket.beans;

/**
 * MainActivity
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
    private int sectionCursor = 0;

    /**
     * 构造函数
     */
    public Section() {
        super();
    }

    public Section(int tableId, int sectionLength, int versionNumber,
                   int sectionNumber, int lastSectionNumber) {
        super();
        this.tableId = tableId;
        this.sectionLength = sectionLength;
        this.versionNumber = versionNumber;
        this.sectionNumber = sectionNumber;
        this.lastSectionNumber = lastSectionNumber;
    }

    public Section(int tableId, int sectionLength, int versionNumber,
                   int sectionNumber, int lastSectionNumber,
                   byte[] sectionData, int sectionCursor) {
        super();
        this.tableId = tableId;
        this.sectionLength = sectionLength;
        this.versionNumber = versionNumber;
        this.sectionNumber = sectionNumber;
        this.lastSectionNumber = lastSectionNumber;
        this.sectionData = sectionData;
        this.sectionCursor = sectionCursor;
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

    public int getSectionCursor() {
        return sectionCursor;
    }

    public void setSectionCursor(int sectionCursor) {
        this.sectionCursor = sectionCursor;
    }

}
