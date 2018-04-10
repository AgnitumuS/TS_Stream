package com.excellence.iptv.bean;

import java.io.Serializable;

/**
 * Program
 *
 * @author ggz
 * @date 2018/4/4
 */

public class Program implements Serializable {

    private int programNumber;
    private int programMapPid;
    private String programName;

    private boolean isFavorite = false;

    public Program(int programNumber, int programMapPid, String programName) {
        super();
        this.programNumber = programNumber;
        this.programMapPid = programMapPid;
        this.programName = programName;
    }

    public int getProgramNumber() {
        return programNumber;
    }

    public void setProgramNumber(int programNumber) {
        this.programNumber = programNumber;
    }

    public String getProgramName() {
        return this.programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public int getProgramMapPid() {
        return this.programMapPid;
    }

    public void setProgramMapPid(int programMapPid) {
        this.programMapPid = programMapPid;
    }

    public boolean getIsFavorite() {
        return this.isFavorite;
    }

    public void setIsFavorite(boolean favorite) {
        this.isFavorite = favorite;
    }
}
