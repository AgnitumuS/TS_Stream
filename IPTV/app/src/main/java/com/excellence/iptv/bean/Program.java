package com.excellence.iptv.bean;

/**
 * Program
 *
 * @author ggz
 * @date 2018/4/4
 */

public class Program {

    private int programNumber;
    private int programMapPid;
    private String programName;

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
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public int getProgramMapPid() {
        return programMapPid;
    }

    public void setProgramMapPid(int programMapPid) {
        this.programMapPid = programMapPid;
    }
}
