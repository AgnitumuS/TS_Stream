package com.excellence.iptv.bean;

import com.excellence.iptv.bean.tables.Pat;
import com.excellence.iptv.bean.tables.Pmt;
import com.excellence.iptv.bean.tables.Sdt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Ts
 *
 * @author ggz
 * @date 2018/4/4
 */

public class Ts implements Serializable {

    private String filePath = null;
    private Pat pat = null;
    private Sdt sdt = null;
    private List<Pmt> pmtList = new ArrayList<>();
    private List<Program> programList = new ArrayList<>();

    public Ts() {
        super();
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Pat getPat() {
        return pat;
    }

    public void setPat(Pat pat) {
        this.pat = pat;
    }

    public Sdt getSdt() {
        return sdt;
    }

    public void setSdt(Sdt sdt) {
        this.sdt = sdt;
    }

    public List<Pmt> getPmtList() {
        return pmtList;
    }

    public void setPmtList(List<Pmt> pmtList) {
        this.pmtList = pmtList;
    }

    public List<Program> getProgramList() {
        return programList;
    }

    public void setProgramList(List<Program> programList) {
        this.programList = programList;
    }
}
