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
    private String startTime;
    private String duration;
    private String endTime;
    private String eventName;

    private boolean isFavorite = false;

    public Program(int programNumber, int programMapPid, String programName,
                   String startTime, String duration, String endTime, String eventName) {
        super();
        this.programNumber = programNumber;
        this.programMapPid = programMapPid;
        this.programName = programName;
        this.startTime = startTime;
        this.duration = duration;
        this.endTime = endTime;
        this.eventName = eventName;
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

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public boolean getIsFavorite() {
        return this.isFavorite;
    }

    public void setIsFavorite(boolean favorite) {
        this.isFavorite = favorite;
    }
}
