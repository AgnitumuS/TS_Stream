package com.excellence.iptv.bean.tables;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Sdt
 *
 * @author ggz
 * @date 2018/3/31
 */

public class Sdt implements Serializable {

    /**
     * tableId : 8 bit
     */
    private int tableId = 0x42;

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
     */
    private int currentNextIndicator;

    /**
     * section_number : 8 bit
     */
    private int sectionNumber;

    /**
     * last_section_number : 8 bit
     */
    private int lastSectionNumber;

    /**
     * original_network_id : 16 bit
     */
    private int originalNetworkId;




    List<SdtService> sdtServiceList = new ArrayList<>();



    /**
     * CRC_32 : 32 bit
     */
    private int crc32;


    public Sdt(byte[] sectionData) {
        super();
        int tableId = sectionData[0] & 0xFF;
        int sectionSyntaxIndicator = (sectionData[1] >> 7) & 0x1;
        int sectionLength = (((sectionData[1] & 0xF) << 8) | (sectionData[2] & 0xFF)) & 0xFFF;
        int transportStreamId = (((sectionData[3] & 0xFF) << 8) | (sectionData[4] & 0xFF)) & 0xFFFF;
        int versionNumber = (sectionData[5] >> 1) & 0x1F;
        int currentNextIndicator = sectionData[5] & 0x1;
        int sectionNumber = sectionData[6] & 0xFF;
        int lastSectionNumber = sectionData[7] & 0xFF;
        int originalNetworkId = (((sectionData[8] & 0xFF) << 8) | (sectionData[9] & 0xFF)) & 0xFFFF;

        this.tableId = tableId;
        this.sectionSyntaxIndicator = sectionSyntaxIndicator;
        this.sectionLength = sectionLength;
        this.transportStreamId = transportStreamId;
        this.versionNumber = versionNumber;
        this.currentNextIndicator = currentNextIndicator;
        this.sectionNumber = sectionNumber;
        this.lastSectionNumber = lastSectionNumber;
        this.originalNetworkId = originalNetworkId;
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

    public int getOriginalNetworkId() {
        return originalNetworkId;
    }

    public void setOriginalNetworkId(int originalNetworkId) {
        this.originalNetworkId = originalNetworkId;
    }

    public List<SdtService> getSdtServiceList() {
        return sdtServiceList;
    }

    public void setSdtServiceList(List<SdtService> sdtServiceList) {
        this.sdtServiceList = sdtServiceList;
    }
}
