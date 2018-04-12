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
                mEit = new Eit(sectionData);
            } else {
                int sectionNumber = sectionData[6] & 0xFF;
                mEit.setSectionNumber(sectionNumber);
            }
            Log.d(TAG, " ---------------------------------------------- ");
            Log.d(TAG, " -- makeEit()");
            Log.d(TAG, "tableId : 0x" + toHexString(mEit.getTableId()));
            Log.d(TAG, "sectionSyntaxIndicator : 0x" + toHexString(mEit.getSectionSyntaxIndicator()));
            Log.d(TAG, "sectionLength : 0x" + toHexString(mEit.getSectionLength()));
            Log.d(TAG, "serviceId : 0x" + toHexString(mEit.getServiceId()));
            Log.d(TAG, "versionNumber : 0x" + toHexString(mEit.getVersionNumber()));
            Log.d(TAG, "currentNextIndicator : 0x" + toHexString(mEit.getCurrentNextIndicator()));
            Log.d(TAG, "sectionNumber : 0x" + toHexString(mEit.getSectionNumber()));
            Log.d(TAG, "lastSectionNumber : 0x" + toHexString(mEit.getLastSectionNumber()));
            Log.d(TAG, "transportStreamId : 0x" + toHexString(mEit.getTransportStreamId()));
            Log.d(TAG, "originalNetworkId : 0x" + toHexString(mEit.getOriginalNetworkId()));
            Log.d(TAG, "segmentLastSectionNumber : 0x" + toHexString(mEit.getSegmentLastSectionNumber()));
            Log.d(TAG, "lastTableId : 0x" + toHexString(mEit.getLastTableId()));

            int sectionSize = sectionData.length;
            int theEffectiveLength = sectionSize - 14 - 4;
            for (int j = 0; j < theEffectiveLength; ) {
                Log.d(TAG, " -- ");
                int eventId = (((sectionData[14 + j] & 0xFF) << 8) | (sectionData[15 + j] & 0xFF)) & 0xFFFF;
                int startTimeMjd = (((sectionData[16 + j] & 0xFF) << 8) | (sectionData[17 + j] & 0xFF)) & 0xFFFF;
                int startTimeBcd = (((sectionData[18 + j] & 0xFF) << 16)
                        | ((sectionData[19 + j] & 0xFF) << 8)
                        | (sectionData[20 + j] & 0xFF)) & 0xFFFFFF;
                int duration = (((sectionData[21 + j] & 0xFF) << 16)
                        | ((sectionData[22 + j] & 0xFF) << 8)
                        | (sectionData[23 + j] & 0xFF)) & 0xFFFFFF;
                int runningStatus = (sectionData[24 + j] >> 5) & 0x7;
                int freeCaMode = (sectionData[24 + j] >> 4) & 0x1;
                int descriptorsLoopLength = (((sectionData[24 + j] & 0xF) << 8) | (sectionData[25 + j] & 0xFF)) & 0xFFF;

                int descriptor_length = sectionData[26 + j] & 0xFF;
                int eventNameLength = sectionData[31 + j] & 0xFF;
                byte[] strBytes = new byte[eventNameLength];
                for (int n = 0; n < eventNameLength; n++) {
                    strBytes[n] = sectionData[32 + j + n];
                }
                String eventName = new String(strBytes);


                Log.d(TAG, "eventId : 0x" + toHexString(eventId));
                Log.d(TAG, "startTimeMjd : 0x" + toHexString(startTimeMjd));
                Log.d(TAG, "startTimeBcd : 0x" + toHexString(startTimeBcd));
                Log.d(TAG, "duration : 0x" + toHexString(duration));
                Log.d(TAG, "runningStatus : 0x" + toHexString(runningStatus));
                Log.d(TAG, "freeCaMode : 0x" + toHexString(freeCaMode));
                Log.d(TAG, "descriptorsLoopLength : 0x" + toHexString(descriptorsLoopLength));
                Log.d(TAG, "descriptor_length : " + descriptor_length);
                Log.d(TAG, "eventNameLength : " + eventNameLength);
                Log.d(TAG, "eventName : " + eventName);

                EitEvent eitEvent = new EitEvent(eventId, startTimeMjd, startTimeBcd, duration,
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
