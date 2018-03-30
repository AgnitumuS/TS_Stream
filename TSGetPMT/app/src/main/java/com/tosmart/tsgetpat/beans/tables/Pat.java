package com.tosmart.tsgetpat.beans.tables;

import java.util.ArrayList;
import java.util.List;

/**
 * Pat
 *
 * @author ggz
 * @date 2018/3/27
 */

public class Pat {

    /**
     * tableId : 8 bit
     */
    private int tableId = 0x00;

    /**
     * section_syntax_indicator : 1 bit
     */
    private int sectionSyntaxIndicator = 0x1;

    /**
     * sectionLength : 12 bit
     */
    private int sectionLength = 0;

    /**
     * transport_stream_id : 16 bit
     */
    private int transportStreamId;

    /**
     * version_number : 5 bit
     */
    private int versionNumber;

    /**
     * current_next_indicator : 1 bit
     * 发送的PAT是当前有效还是下一个PAT有效
     */
    private int currentNextIndicator;

    /**
     * section_number : 8 bit
     */
    private int sectionNumber;

    /**
     * last_section_number 8 bit
     */
    private int lastSectionNumber;



    private List<PatProgram> patProgramList = new ArrayList<>();



    /**
     * network_PID : 13 bit
     * 网络信息表（NIT）的PID,节目号为0时对应的PID为network_PID
     */
    private int networkPid;

    /**
     * CRC_32 : 32 bit
     */
    private int crc32;


    public Pat(byte[] sectionData) {
        super();
        int tableId = sectionData[0] & 0xFF;
        int sectionSyntaxIndicator = (sectionData[1] >> 7) & 0x1;
        int sectionLength = (((sectionData[1] & 0xF) << 8) | (sectionData[2] & 0xFF)) & 0xFFF;
        int transportStreamId = ((sectionData[3] & 0xFF) | (sectionData[4] & 0xFF)) & 0xFFFF;
        int versionNumber = (sectionData[5] >> 1) & 0x1F;
        int currentNextIndicator = sectionData[5] & 0x1;
        int sectionNumber = sectionData[6] & 0xFF;
        int lastSectionNumber = sectionData[7] & 0xFF;

        this.tableId = tableId;
        this.sectionSyntaxIndicator = sectionSyntaxIndicator;
        this.sectionLength = sectionLength;
        this.transportStreamId = transportStreamId;
        this.versionNumber = versionNumber;
        this.currentNextIndicator = currentNextIndicator;
        this.sectionNumber = sectionNumber;
        this.lastSectionNumber = lastSectionNumber;
    }

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public int getSectionSyntaxIndicator() {
        return sectionSyntaxIndicator;
    }

    public void setSectionSyntaxIndicator(int sectionSyntaxIndicator) {
        this.sectionSyntaxIndicator = sectionSyntaxIndicator;
    }

    public int getSectionLength() {
        return sectionLength;
    }

    public void setSectionLength(int sectionLength) {
        this.sectionLength = sectionLength;
    }

    public int getTransportStreamId() {
        return transportStreamId;
    }

    public void setTransportStreamId(int transportStreamId) {
        this.transportStreamId = transportStreamId;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public int getCurrentNextIndicator() {
        return currentNextIndicator;
    }

    public void setCurrentNextIndicator(int currentNextIndicator) {
        this.currentNextIndicator = currentNextIndicator;
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

    public List<PatProgram> getPatProgramList() {
        return patProgramList;
    }

    public void setPatProgramList(List<PatProgram> patProgramList) {
        this.patProgramList = patProgramList;
    }

    public int getNetworkPid() {
        return networkPid;
    }

    public void setNetworkPid(int networkPid) {
        this.networkPid = networkPid;
    }

    public int getCrc32() {
        return crc32;
    }

    public void setCrc32(int crc32) {
        this.crc32 = crc32;
    }
}
