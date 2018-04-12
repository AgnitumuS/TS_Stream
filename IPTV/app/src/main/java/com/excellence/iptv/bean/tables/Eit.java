package com.excellence.iptv.bean.tables;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Eit
 *
 * @author ggz
 * @date 2018/4/12
 */

public class Eit implements Serializable {

    /**
     * tableId : 8 bit
     */
    private int tableId = 0x4e;

    /**
     * section_syntax_indicator : 1 bit
     */
    private int sectionSyntaxIndicator = 0x1;

    /**
     * sectionLength : 12 bit
     */
    private int sectionLength = 0;

    /**
     * service_id : 16 bit
     */
    private int serviceId;

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
     * transport_stream_id : 16 bit
     */
    private int transportStreamId;

    /**
     * original_network_id : 16 bit
     */
    private int originalNetworkId;

    /**
     * segment_last_section_number : 8
     */
    private int segmentLastSectionNumber;

    /**
     * last_table_id : 8
     */
    private int lastTableId;


    private List<EitEvent> eitEventList = new ArrayList<>();


    public Eit(byte[] sectionData) {
        super();
        int tableId = sectionData[0] & 0xFF;
        int sectionSyntaxIndicator = (sectionData[1] >> 7) & 0x1;
        int sectionLength = (((sectionData[1] & 0xF) << 8) | (sectionData[2] & 0xFF)) & 0xFFF;
        int serviceId = (((sectionData[3] & 0xFF) << 8) | (sectionData[4] & 0xFF)) & 0xFFFF;
        int versionNumber = (sectionData[5] >> 1) & 0x1F;
        int currentNextIndicator = sectionData[5] & 0x1;
        int sectionNumber = sectionData[6] & 0xFF;
        int lastSectionNumber = sectionData[7] & 0xFF;
        int transportStreamId = (((sectionData[8] & 0xFF) << 8) | (sectionData[9] & 0xFF)) & 0xFFFF;
        int originalNetworkId = (((sectionData[10] & 0xFF) << 8) | (sectionData[11] & 0xFF)) & 0xFFFF;
        int segmentLastSectionNumber = sectionData[12] & 0xFF;
        int lastTableId = sectionData[13] & 0xFF;

        this.tableId = tableId;
        this.sectionSyntaxIndicator = sectionSyntaxIndicator;
        this.sectionLength = sectionLength;
        this.serviceId = serviceId;
        this.versionNumber = versionNumber;
        this.currentNextIndicator = currentNextIndicator;
        this.sectionNumber = sectionNumber;
        this.lastSectionNumber = lastSectionNumber;
        this.transportStreamId = transportStreamId;
        this.originalNetworkId = originalNetworkId;
        this.segmentLastSectionNumber = segmentLastSectionNumber;
        this.lastTableId = lastTableId;
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

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
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

    public int getTransportStreamId() {
        return transportStreamId;
    }

    public void setTransportStreamId(int transportStreamId) {
        this.transportStreamId = transportStreamId;
    }

    public int getOriginalNetworkId() {
        return originalNetworkId;
    }

    public void setOriginalNetworkId(int originalNetworkId) {
        this.originalNetworkId = originalNetworkId;
    }

    public int getSegmentLastSectionNumber() {
        return segmentLastSectionNumber;
    }

    public void setSegmentLastSectionNumber(int segmentLastSectionNumber) {
        this.segmentLastSectionNumber = segmentLastSectionNumber;
    }

    public int getLastTableId() {
        return lastTableId;
    }

    public void setLastTableId(int lastTableId) {
        this.lastTableId = lastTableId;
    }

    public List<EitEvent> getEitEventList() {
        return eitEventList;
    }

    public void setEitEventList(List<EitEvent> eitEventList) {
        this.eitEventList = eitEventList;
    }
}
