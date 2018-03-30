package com.tosmart.tsgetpat.beans.tables;

/**
 * PmtStream
 *
 * @author ggz
 * @date 2018/3/29
 */

public class PmtStream {

    /**
     * stream_type : 8 bit
     */
    private int streamType;

    /**
     * elementary_pid : 13 bit
     */
    private int elementaryPid;

    /**
     * es_info_length : 12 bit
     */
    private int esInfoLength;


    public PmtStream(int streamType, int elementaryPid, int esInfoLength) {
        super();
        this.streamType = streamType;
        this.elementaryPid = elementaryPid;
        this.esInfoLength = esInfoLength;
    }

    public int getStreamType() {
        return streamType;
    }

    public void setStreamType(int streamType) {
        this.streamType = streamType;
    }

    public int getElementaryPid() {
        return elementaryPid;
    }

    public void setElementaryPid(int elementaryPid) {
        this.elementaryPid = elementaryPid;
    }

    public int getEsInfoLength() {
        return esInfoLength;
    }

    public void setEsInfoLength(int esInfoLength) {
        this.esInfoLength = esInfoLength;
    }
}
