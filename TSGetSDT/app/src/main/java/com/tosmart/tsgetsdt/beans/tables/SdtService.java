package com.tosmart.tsgetsdt.beans.tables;

/**
 * Sdt
 *
 * @author ggz
 * @date 2018/3/31
 */

public class SdtService {

    /**
     * service_id : 16 bit
     */
    private int serviceId;

    /**
     * eit_schedule_flag : 1 bit
     */
    private int eitScheduleFlag;

    /**
     * eit_present_following_flag : 1 bit
     */
    private int eitPresentFollowingFlag;

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


    // descriptor()


    /**
     * service_type : 8 bit (1byte)
     */
    private int serviceType;

    /**
     * service_provider_name_length : 8 bit (1 byte)
     */
    private int serviceProviderNameLength;

    /**
     * service_provider_name : ?
     */
    private String serviceProviderName;

    /**
     * service_name_length : 8 bit (1 byte)
     */
    private int serviceNameLength;

    /**
     * service_name : ?
     */
    private String serviceName;


}
