package com.excellence.iptv.util;


import android.util.Log;

import com.excellence.iptv.bean.Section;
import com.excellence.iptv.bean.tables.Sdt;
import com.excellence.iptv.bean.tables.SdtService;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.toHexString;

/**
 * SdtManager
 *
 * @author ggz
 * @date 2018/3/31
 */


public class SdtManager {
    private static final String TAG = "SdtManager";
    private static final boolean IS_LOG = false;

    private Sdt mSdt = null;
    private List<SdtService> mSdtServiceList = new ArrayList<>();

    public SdtManager() {
        super();
    }

    public Sdt makeSDT(List<Section> sectionList) {

        for (int i = 0; i < sectionList.size(); i++) {

            Section section = sectionList.get(i);
            byte[] sectionData = section.getSectionData();

            if (mSdt == null) {
                mSdt = new Sdt();
            }

            if (IS_LOG) {
                int tableId = sectionData[0] & 0xFF;
                int sectionSyntaxIndicator = (sectionData[1] >> 7) & 0x1;
                int sectionLength = (((sectionData[1] & 0xF) << 8) | (sectionData[2] & 0xFF)) & 0xFFF;
                int transportStreamId = (((sectionData[3] & 0xFF) << 8) | (sectionData[4] & 0xFF)) & 0xFFFF;
                int versionNumber = (sectionData[5] >> 1) & 0x1F;
                int currentNextIndicator = sectionData[5] & 0x1;
                int sectionNumber = sectionData[6] & 0xFF;
                int lastSectionNumber = sectionData[7] & 0xFF;
                int originalNetworkId = (((sectionData[8] & 0xFF) << 8) | (sectionData[9] & 0xFF)) & 0xFFFF;

                Log.d(TAG, " ---------------------------------------------- ");
                Log.d(TAG, " -- makeSDT()");
                Log.d(TAG, "tableId : 0x" + toHexString(tableId));
                Log.d(TAG, "sectionSyntaxIndicator : 0x" + toHexString(sectionSyntaxIndicator));
                Log.d(TAG, "sectionLength : 0x" + toHexString(sectionLength));
                Log.d(TAG, "transportStreamId : 0x" + toHexString(transportStreamId));
                Log.d(TAG, "versionNumber : 0x" + toHexString(versionNumber));
                Log.d(TAG, "currentNextIndicator : 0x" + toHexString(currentNextIndicator));
                Log.d(TAG, "sectionNumber : 0x" + toHexString(sectionNumber));
                Log.d(TAG, "lastSectionNumber : 0x" + toHexString(lastSectionNumber));
                Log.d(TAG, "originalNetworkId : 0x" + toHexString(originalNetworkId));
            }


            /*
            * to reserved_future_use : 11 byte
            * CRC_32 : 4 byte
            *
            * need to delete = 15
            *
            * service_id : 16 bit
            * reserved_future_use : 6 bit
            * eit_schedule_flag : 1 bit
            * eit_present_following_flag : 1 bit
            * running_status : 3 bit
            * free_CA_mode : 1 bit
            * descriptors_loop_length : 12 bit
            *
            * to descriptors_loop_length : 5 byte
            * Service = 5 + ?
            *
            * -> descriptors_loop_length
            *   tag : 1 byte
            *   len : 1 byte
            *   data -> len
            *       service_type : 1 byte
            *       service_provider_name_length : 1 byte
            *       service_provider_name -> name_length
            *       service_name_length : 1 byte
            *       service_name -> name_length
            * */
            int sectionSize = sectionData.length;
            int theEffectiveLength = sectionSize - 15;
            for (int j = 0; j < theEffectiveLength; ) {
                int serviceId = (((sectionData[11 + j] & 0xFF) << 8) | (sectionData[12 + j] & 0xFF)) & 0xFFFF;
                int eitScheduleFlag = (sectionData[13 + j] >> 1) & 0x1;
                int eitPresentFollowingFlag = sectionData[13 + j] & 0x1;
                int runningStatus = (sectionData[14 + j] >> 5) & 0x7;
                int freeCaMode = (sectionData[14 + j] >> 4) & 0x1;
                int descriptorsLoopLength = (((sectionData[14 + j] & 0xF) << 8) | (sectionData[15 + j] & 0xFF)) & 0xFFF;

                int descriptor_length = sectionData[17 + j] & 0xFF;
                int serviceType = sectionData[18 + j] & 0xFF;
                int serviceProviderNameLength = sectionData[19 + j] & 0xFF;
                byte[] strBytes = new byte[serviceProviderNameLength];
                for (int n = 0; n < serviceProviderNameLength; n++) {
                    strBytes[n] = sectionData[20 + j + n];
                }
                String serviceProviderName = new String(strBytes);
                int serviceNameLength = sectionData[20 + serviceProviderNameLength + j] & 0xFF;
                strBytes = new byte[serviceNameLength];
                for (int n = 0; n < serviceNameLength; n++) {
                    strBytes[n] = sectionData[21 + serviceProviderNameLength + j + n];
                }
                String serviceName = new String(strBytes);

                if (IS_LOG) {
                    Log.d(TAG, " -- ");
                    Log.d(TAG, "serviceId : 0x" + toHexString(serviceId));
                    Log.d(TAG, "eitScheduleFlag : 0x" + toHexString(eitScheduleFlag));
                    Log.d(TAG, "eitPresentFollowingFlag : 0x" + toHexString(eitPresentFollowingFlag));
                    Log.d(TAG, "runningStatus : 0x" + toHexString(runningStatus));
                    Log.d(TAG, "freeCaMode : 0x" + toHexString(freeCaMode));
                    Log.d(TAG, "descriptorsLoopLength : 0x" + toHexString(descriptorsLoopLength));
                    Log.d(TAG, "descriptor_length : " + descriptor_length);
                    Log.d(TAG, "serviceType : 0x" + toHexString(serviceType));
                    Log.d(TAG, "serviceProviderNameLength : " + serviceProviderNameLength);
                    Log.d(TAG, "serviceProviderName : " + serviceProviderName);
                    Log.d(TAG, "serviceNameLength : " + serviceNameLength);
                    Log.d(TAG, "serviceName : " + serviceName);
                }

                SdtService sdtService = new SdtService(
                        serviceId, eitScheduleFlag, eitPresentFollowingFlag,
                        runningStatus, freeCaMode, descriptorsLoopLength,
                        serviceType, serviceProviderNameLength, serviceProviderName,
                        serviceNameLength, serviceName);
                mSdtServiceList.add(sdtService);

                j += (5 + descriptorsLoopLength);
            }
        }

        mSdt.setSdtServiceList(mSdtServiceList);

        return mSdt;
    }
}
