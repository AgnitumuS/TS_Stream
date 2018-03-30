package com.tosmart.tsgetpmt.utils;

import android.util.Log;

import com.tosmart.tsgetpmt.beans.Section;
import com.tosmart.tsgetpmt.beans.tables.Pat;
import com.tosmart.tsgetpmt.beans.tables.PatProgram;

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
                mPat = new Pat(sectionData);
            } else {
                int sectionNumber = sectionData[6] & 0xFF;
                mPat.setSectionNumber(sectionNumber);
            }
            Log.d(TAG, " ---------------------------------------------- ");
            Log.d(TAG, " -- makePAT()");
            Log.d(TAG, "tableId : 0x" + toHexString(mPat.getTableId()));
            Log.d(TAG, "sectionSyntaxIndicator : 0x" + toHexString(mPat.getSectionSyntaxIndicator()));
            Log.d(TAG, "sectionLength : 0x" + toHexString(mPat.getSectionLength()));
            Log.d(TAG, "transportStreamId : 0x" + toHexString(mPat.getTransportStreamId()));
            Log.d(TAG, "versionNumber : 0x" + toHexString(mPat.getVersionNumber()));
            Log.d(TAG, "currentNextIndicator : 0x" + toHexString(mPat.getCurrentNextIndicator()));
            Log.d(TAG, "sectionNumber : 0x" + toHexString(mPat.getSectionNumber()));
            Log.d(TAG, "lastSectionNumber : 0x" + toHexString(mPat.getLastSectionNumber()));

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
                Log.d(TAG, " -- ");
                int programNumber = (((sectionData[8 + j] & 0xFF) << 8) | (sectionData[9 + j] & 0xFF)) & 0xFFFF;
                Log.d(TAG, "programNumber : 0x" + toHexString(programNumber));

                if (programNumber == 0x00) {
                    int networkPid = (((sectionData[10 + j] & 0x1F) << 8) | (sectionData[11 + j] & 0xFF)) & 0x1FFF;
                    Log.d(TAG, "networkPid : 0x" + toHexString(networkPid));

                    mPat.setNetworkPid(networkPid);

                } else {
                    int programMapPid = (((sectionData[10 + j] & 0x1F) << 8) | (sectionData[11 + j] & 0xFF)) & 0x1FFF;
                    Log.d(TAG, "programMapPid : 0x" + toHexString(programMapPid));

                    PatProgram patProgram = new PatProgram(programNumber, programMapPid);
                    mPatProgramList.add(patProgram);
                }
            }

//            long crc32 = ((sectionData[sectionSize - 4] & 0xFF << 24) |
//                    (sectionData[sectionSize - 3] & 0xFF << 16) |
//                    (sectionData[sectionSize - 2] & 0xFF << 8) |
//                    (sectionData[sectionSize - 1] & 0xFF)) & 0xFFFFFFFF;
        }

        mPat.setPatProgramList(mPatProgramList);

        return mPat;
    }
}
