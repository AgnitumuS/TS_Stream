package com.excellence.iptv.util;


import android.util.Log;

import com.excellence.iptv.bean.Section;
import com.excellence.iptv.bean.tables.Eit;
import com.excellence.iptv.bean.tables.EitEvent;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.toHexString;

/**
 * EitManager
 *
 * @author ggz
 * @date 2018/3/31
 */


public class EitManager {
    private static final String TAG = "EitManager";
    private static final boolean IS_LOG = false;

    private Eit mEit = null;
    private List<EitEvent> mEitEventList = new ArrayList<>();

    public EitManager() {
        super();
    }

    public Eit makeEit(List<Section> sectionList) {

        for (int i = 0; i < sectionList.size(); i++) {

            Section section = sectionList.get(i);
            byte[] sectionData = section.getSectionData();

            if (mEit == null) {
                mEit = new Eit();
            }

            if (IS_LOG) {
                int tableId = sectionData[0] & 0xFF;
                int sectionSyntaxIndicator = (sectionData[1] >> 7) & 0x1;
                int sectionLength = (((sectionData[1] & 0xF) << 8) | (sectionData[2] & 0xFF)) & 0xFFF;
                int serviceId = (((sectionData[3] & 0xFF) << 8) | (sectionData[4] & 0xFF)) & 0xFFFF;
                int versionNumber = (sectionData[5] >> 1) & 0x1F;
                int currentNextIndicator = sectionData[5] & 0x1;
                int sectionNumber = sectionData[6] & 0xFF;
                int lastSectionNumber = sectionData[7] & 0xFF;
                int transportStreamId = (((sectionData[8] & 0xFF) << 8) | (sectionData[9] & 0xFF)) & 0xFFFF;
                int originalNetworkId = (((sectionData[10] & 0xFF) << 8) | (sectionData[11] & 0xFF)) & 0xFFFF;
                int segmentLastSectionNumber = sectionData[12] & 0xFF;
                int lastTableId = sectionData[13] & 0xFF;

                Log.d(TAG, " ---------------------------------------------- ");
                Log.d(TAG, " -- makeEit()");
                Log.d(TAG, "tableId : 0x" + toHexString(tableId));
                Log.d(TAG, "sectionSyntaxIndicator : 0x" + toHexString(sectionSyntaxIndicator));
                Log.d(TAG, "sectionLength : 0x" + toHexString(sectionLength));
                Log.d(TAG, "serviceId : 0x" + toHexString(serviceId));
                Log.d(TAG, "versionNumber : 0x" + toHexString(versionNumber));
                Log.d(TAG, "currentNextIndicator : 0x" + toHexString(currentNextIndicator));
                Log.d(TAG, "sectionNumber : 0x" + toHexString(sectionNumber));
                Log.d(TAG, "lastSectionNumber : 0x" + toHexString(lastSectionNumber));
                Log.d(TAG, "transportStreamId : 0x" + toHexString(transportStreamId));
                Log.d(TAG, "originalNetworkId : 0x" + toHexString(originalNetworkId));
                Log.d(TAG, "segmentLastSectionNumber : 0x" + toHexString(segmentLastSectionNumber));
                Log.d(TAG, "lastTableId : 0x" + toHexString(lastTableId));
            }

            int sectionSize = sectionData.length;
            int theEffectiveLength = sectionSize - 14 - 4;
            for (int j = 0; j < theEffectiveLength; ) {
                int serviceId = (((sectionData[3] & 0xFF) << 8) | (sectionData[4] & 0xFF)) & 0xFFFF;
                int eventId = (((sectionData[14 + j] & 0xFF) << 8) | (sectionData[15 + j] & 0xFF)) & 0xFFFF;
                int startTimeMjd = (((sectionData[16 + j] & 0xFF) << 8) | (sectionData[17 + j] & 0xFF)) & 0xFFFF;
                byte[] startTimeBcd = new byte[3];
                startTimeBcd[0] = sectionData[18 + j];
                startTimeBcd[1] = sectionData[19 + j];
                startTimeBcd[2] = sectionData[20 + j];
                byte[] duration = new byte[3];
                duration[0] = sectionData[21 + j];
                duration[1] = sectionData[22 + j];
                duration[2] = sectionData[23 + j];
                int runningStatus = (sectionData[24 + j] >> 5) & 0x7;
                int freeCaMode = (sectionData[24 + j] >> 4) & 0x1;
                int descriptorsLoopLength = (((sectionData[24 + j] & 0xF) << 8) | (sectionData[25 + j] & 0xFF)) & 0xFFF;

                int descriptor_length = sectionData[26 + j] & 0xFF;
                int eventNameLength = sectionData[31 + j] & 0xFF;
                byte[] strBytes = new byte[eventNameLength];
                for (int n = 0; n < eventNameLength; n++) {
                    strBytes[n] = sectionData[32 + j + n];
                }
                String eventName = new StringEncodingUtil(strBytes).makeString();

                if (IS_LOG) {
                    Log.d(TAG, " -- ");
                    Log.d(TAG, "serviceId : 0x" + toHexString(serviceId));
                    Log.d(TAG, "eventId : 0x" + toHexString(eventId));
                    Log.d(TAG, "startTimeMjd : 0x" + toHexString(startTimeMjd));
                    Log.d(TAG, "runningStatus : 0x" + toHexString(runningStatus));
                    Log.d(TAG, "freeCaMode : 0x" + toHexString(freeCaMode));
                    Log.d(TAG, "descriptorsLoopLength : 0x" + toHexString(descriptorsLoopLength));
                    Log.d(TAG, "descriptor_length : " + descriptor_length);
                    Log.d(TAG, "eventNameLength : " + eventNameLength);
                    Log.d(TAG, "eventName : " + eventName);
                }


                EitEvent eitEvent = new EitEvent(serviceId, eventId, startTimeMjd, startTimeBcd, duration,
                        runningStatus, freeCaMode, descriptorsLoopLength,
                        eventNameLength, eventName);

                mEitEventList.add(eitEvent);

                j += (12 + descriptorsLoopLength);
            }
        }

        mEit.setEitEventList(mEitEventList);

        return mEit;
    }
}
