package com.excellence.iptv.bean.tables;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.toHexString;

/**
 * Pmt
 *
 * @author ggz
 * @date 2018/3/29
 */

public class Pmt implements Serializable {

    private int tableId = 0x02;

    private int programNumber;

    List<PmtStream> pmtStreamList = new ArrayList<>();


    public Pmt() {
        super();
    }

    public String print() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("programNumber : " + programNumber + "\n");
        for (PmtStream pmtStream : pmtStreamList) {
            stringBuilder.append(" -- \n" +
                    "streamType : 0x" + toHexString(pmtStream.getStreamType()) + "\n" +
                    "elementaryPid : 0x" + toHexString(pmtStream.getElementaryPid()) + "\n" +
                    "esInfoLength : 0x" + toHexString(pmtStream.getEsInfoLength()) + "\n");
        }

        return stringBuilder.toString();
    }


    public int getTableId() {
        return tableId;
    }

    public int getProgramNumber() {
        return programNumber;
    }

    public void setProgramNumber(int programNumber) {
        this.programNumber = programNumber;
    }

    public List<PmtStream> getPmtStreamList() {
        return pmtStreamList;
    }

    public void setPmtStreamList(List<PmtStream> pmtStreamList) {
        this.pmtStreamList = pmtStreamList;
    }

}
