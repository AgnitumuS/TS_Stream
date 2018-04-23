package com.excellence.iptv.util;

import android.util.Log;

import com.excellence.iptv.bean.Section;
import com.excellence.iptv.bean.tables.Pat;
import com.excellence.iptv.bean.tables.PatProgram;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.toHexString;

/**
 * PatManager
 *
 * @author ggz
 * @date 2018/3/27
 */

public class PatManager {
    private static final String TAG = "PatManager";
    private static final boolean IS_LOG = false;

    private Pat mPat = null;
    private List<PatProgram> mPatProgramList = new ArrayList<>();

    public PatManager() {
        super();
    }

    public Pat makePAT(List<Section> sectionList) {

        for (int i = 0; i < sectionList.size(); i++) {

            Section section = sectionList.get(i);
            byte[] sectionData = section.getSectionData();

            if (mPat == null) {
                mPat = new Pat();
            }

            if (IS_LOG) {
                int tableId = sectionData[0] & 0xFF;
                int sectionSyntaxIndicator = (sectionData[1] >> 7) & 0x1;
                int sectionLength = (((sectionData[1] & 0xF) << 8) | (sectionData[2] & 0xFF)) & 0xFFF;
                int transportStreamId = ((sectionData[3] & 0xFF) | (sectionData[4] & 0xFF)) & 0xFFFF;
                int versionNumber = (sectionData[5] >> 1) & 0x1F;
                int currentNextIndicator = sectionData[5] & 0x1;
                int sectionNumber = sectionData[6] & 0xFF;
                int lastSectionNumber = sectionData[7] & 0xFF;

                Log.d(TAG, " ---------------------------------------------- ");
                Log.d(TAG, " -- makePAT()");
                Log.d(TAG, "tableId : 0x" + toHexString(tableId));
                Log.d(TAG, "sectionSyntaxIndicator : 0x" + toHexString(sectionSyntaxIndicator));
                Log.d(TAG, "sectionLength : 0x" + toHexString(sectionLength));
                Log.d(TAG, "transportStreamId : 0x" + toHexString(transportStreamId));
                Log.d(TAG, "versionNumber : 0x" + toHexString(versionNumber));
                Log.d(TAG, "currentNextIndicator : 0x" + toHexString(currentNextIndicator));
                Log.d(TAG, "sectionNumber : 0x" + toHexString(sectionNumber));
                Log.d(TAG, "lastSectionNumber : 0x" + toHexString(lastSectionNumber));
            }

            /*
            * to lastSectionNumber : 8 byte
            * CRC_32 : 4 byte
            * total = 12 byte
            *
            * Program : 4 byte
            * */
            int sectionSize = sectionData.length;
            int theEffectiveLength = sectionSize - 12;
            for (int j = 0; j < theEffectiveLength; j += 4) {
                int programNumber = (((sectionData[8 + j] & 0xFF) << 8) | (sectionData[9 + j] & 0xFF)) & 0xFFFF;
                if (IS_LOG) {
                    Log.d(TAG, " -- ");
                    Log.d(TAG, "programNumber : 0x" + toHexString(programNumber));
                }

                if (programNumber == 0x00) {
                    int networkPid = (((sectionData[10 + j] & 0x1F) << 8) | (sectionData[11 + j] & 0xFF)) & 0x1FFF;
                    if (IS_LOG) {
                        Log.d(TAG, "networkPid : 0x" + toHexString(networkPid));
                    }

                    mPat.setNetworkPid(networkPid);

                } else {
                    int programMapPid = (((sectionData[10 + j] & 0x1F) << 8) | (sectionData[11 + j] & 0xFF)) & 0x1FFF;
                    if (IS_LOG) {
                        Log.d(TAG, "programMapPid : 0x" + toHexString(programMapPid));
                    }

                    PatProgram patProgram = new PatProgram(programNumber, programMapPid);
                    mPatProgramList.add(patProgram);
                }
            }

        }

        mPat.setPatProgramList(mPatProgramList);

        return mPat;
    }
}
