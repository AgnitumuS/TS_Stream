package com.excellence.iptv.bean.tables;

import java.io.Serializable;

/**
 * EitEvent
 *
 * @author ggz
 * @date 2018/4/12
 */

public class EitEvent implements Serializable {

    /**
     * event_id : 16 bit
     */
    private int eventId;

    /**
     * start_time : 40 bit
     * start_time_mjd : 16
     * start_time_bcd : 24
     */
    private int startTimeMjd;
    private int startTimeBcd;

    /**
     * duration : 24 bit
     */
    private int duration;

    /**
     * running_status : 3 bit
     */
    private int runningStatus;

    /**
     * free_CA_mode : 1 bit
     */
    private int freeCaMode;

    /**
     * descriptors_loop_length : 12 bit
     */
    private int descriptorsLoopLength;


    /**
     * event_name_length : 8 bit (1 byte)
     */
    private int eventNameLength;

    /**
     * event_name : ?
     */
    private String eventName;

    public EitEvent(int eventId, int startTimeMjd, int startTimeBcd, int duration,
                    int runningStatus, int freeCaMode, int descriptorsLoopLength,
                    int eventNameLength, String eventName) {
        super();
        this.eventId = eventId;
        this.startTimeMjd = startTimeMjd;
        this.startTimeBcd = startTimeBcd;
        this.duration = duration;
        this.runningStatus = runningStatus;
        this.freeCaMode = freeCaMode;
        this.descriptorsLoopLength = descriptorsLoopLength;
        this.eventNameLength = eventNameLength;
        this.eventName = eventName;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public int getStartTimeMjd() {
        return startTimeMjd;
    }

    public void setStartTimeMjd(int startTimeMjd) {
        this.startTimeMjd = startTimeMjd;
    }

    public int getStartTimeBcd() {
        return startTimeBcd;
    }

    public void setStartTimeBcd(int startTimeBcd) {
        this.startTimeBcd = startTimeBcd;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getRunningStatus() {
        return runningStatus;
    }

    public void setRunningStatus(int runningStatus) {
        this.runningStatus = runningStatus;
    }

    public int getFreeCaMode() {
        return freeCaMode;
    }

    public void setFreeCaMode(int freeCaMode) {
        this.freeCaMode = freeCaMode;
    }

    public int getDescriptorsLoopLength() {
        return descriptorsLoopLength;
    }

    public void setDescriptorsLoopLength(int descriptorsLoopLength) {
        this.descriptorsLoopLength = descriptorsLoopLength;
    }

    public int getEventNameLength() {
        return eventNameLength;
    }

    public void setEventNameLength(int eventNameLength) {
        this.eventNameLength = eventNameLength;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
}
