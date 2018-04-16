package com.excellence.iptv.bean.tables;

import java.io.Serializable;

/**
 * EitEvent
 *
 * @author ggz
 * @date 2018/4/12
 */

public class EitEvent implements Serializable {

    private int serviceId;

    /**
     * event_id : 16 bit
     */
    private int eventId;

    /**
     * start_time : 40 bit
     * start_time_mjd : 16
     * start_time_bcd : 24
     */
    private int startTimeDataMjd;
    private byte[] startTimeBcd;

    /**
     * duration : 24 bit
     */
    private byte[] duration;

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

    public EitEvent(int serviceId, int eventId, int startTimeDataMjd, byte[] startTimeBcd, byte[] duration,
                    int runningStatus, int freeCaMode, int descriptorsLoopLength,
                    int eventNameLength, String eventName) {
        super();
        this.serviceId = serviceId;
        this.eventId = eventId;
        this.startTimeDataMjd = startTimeDataMjd;
        this.startTimeBcd = startTimeBcd;
        this.duration = duration;
        this.runningStatus = runningStatus;
        this.freeCaMode = freeCaMode;
        this.descriptorsLoopLength = descriptorsLoopLength;
        this.eventNameLength = eventNameLength;
        this.eventName = eventName;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public int getStartTimeDataMjd() {
        return startTimeDataMjd;
    }

    public void setStartTimeDataMjd(int startTimeDataMjd) {
        this.startTimeDataMjd = startTimeDataMjd;
    }

    public byte[] getStartTimeBcd() {
        return startTimeBcd;
    }

    public void setStartTimeBcd(byte[] startTimeBcd) {
        this.startTimeBcd = startTimeBcd;
    }

    public byte[] getDurationBcd() {
        return duration;
    }

    public void setDuration(byte[] duration) {
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
