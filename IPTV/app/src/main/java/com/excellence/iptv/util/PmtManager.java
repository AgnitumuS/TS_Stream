package com.excellence.iptv.util;

import android.util.Log;

import com.excellence.iptv.bean.Section;
import com.excellence.iptv.bean.tables.Pmt;
import com.excellence.iptv.bean.tables.PmtStream;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.toHexString;

/**
 * PmtManager
 *
 * @author ggz
 * @date 2018/3/29
 */

public class PmtManager {
    private static final String TAG = "PmtManager";
    private static final boolean IS_LOG = false;

    private Pmt mPmt = null;
    private List<PmtStream> mPmtStreamList = new ArrayList<>();

    public PmtManager() {
        super();
    }

    public Pmt makePMT(List<Section> sectionList) {

        for (int i = 0; i < sectionList.size(); i++) {

            Section section = sectionList.get(i);
            byte[] sectionData = section.getSectionData();

            if (mPmt == null) {
                mPmt = new Pmt();
                int programNumber = (((sectionData[3] & 0xFF) << 8) | (sectionData[4] & 0xFF)) & 0xFFFF;
                mPmt.setProgramNumber(programNumber);
            }

            if (IS_LOG) {
                int tableId = sectionData[0] & 0xFF;
                int sectionSyntaxIndicator = (sectionData[1] >> 7) & 0x1;
                int sectionLength = (((sectionData[1] & 0xF) << 8) | (sectionData[2] & 0xFF)) & 0xFFF;
                int programNumber = (((sectionData[3] & 0xFF) << 8) | (sectionData[4] & 0xFF)) & 0xFFFF;
                int versionNumber = (sectionData[5] >> 1) & 0x1F;
                int currentNextIndicator = sectionData[5] & 0x1;
                int sectionNumber = sectionData[6] & 0xFF;
                int lastSectionNumber = sectionData[7] & 0xFF;
                int pcrPid = (((sectionData[8] & 0x1F) << 8) | (sectionData[9] & 0xFF)) & 0x1FFF;
                int programInfoLength = (((sectionData[10] & 0xF) << 8) | (sectionData[11] & 0xFF)) & 0xFFF;

                Log.d(TAG, " ---------------------------------------------- ");
                Log.d(TAG, " -- makePMT()");
                Log.d(TAG, "tableId : 0x" + toHexString(tableId));
                Log.d(TAG, "sectionSyntaxIndicator : 0x" + toHexString(sectionSyntaxIndicator));
                Log.d(TAG, "sectionLength : 0x" + toHexString(sectionLength));
                Log.d(TAG, "programNumber : 0x" + toHexString(programNumber));
                Log.d(TAG, "versionNumber : 0x" + toHexString(versionNumber));
                Log.d(TAG, "currentNextIndicator : 0x" + toHexString(currentNextIndicator));
                Log.d(TAG, "sectionNumber : 0x" + toHexString(sectionNumber));
                Log.d(TAG, "lastSectionNumber : 0x" + toHexString(lastSectionNumber));
                Log.d(TAG, "pcrPid : 0x" + toHexString(pcrPid));
                Log.d(TAG, "programInfoLength : 0x" + toHexString(programInfoLength));
            }


            /*
            * to programInfoLength : 12 byte
            * programInfoLength : 不定
            * CRC_32 : 4 byte
            * total = 16 + ？
            *
            * esInfoLength : 不定
            * Stream : 5 + ？
            * */
            int sectionSize = sectionData.length;
            int programInfoLength = (((sectionData[10] & 0xF) << 8) | (sectionData[11] & 0xFF)) & 0xFFF;
            int theEffectiveLength = sectionSize - (16 + programInfoLength);
            int pos = 12 + programInfoLength;
            for (int n = 0; n < theEffectiveLength; ) {
                int streamType = sectionData[pos + n] & 0xFF;
                int elementaryPid = (((sectionData[pos + 1 + n] & 0x1F) << 8) |
                        (sectionData[pos + 2 + n] & 0xFF)) & 0x1FFF;
                int esInfoLength = (((sectionData[pos + 3 + n] & 0xF) << 8) |
                        (sectionData[pos + 4 + n] & 0xFF)) & 0xFFF;
                if (IS_LOG) {
                    Log.d(TAG, " -- ");
                    Log.d(TAG, "streamType : 0x" + toHexString(streamType));
                    Log.d(TAG, "elementaryPid : 0x" + toHexString(elementaryPid));
                    Log.d(TAG, "esInfoLength : 0x" + toHexString(esInfoLength));
                }

                PmtStream pmtStream = new PmtStream(streamType, elementaryPid, esInfoLength);
                mPmtStreamList.add(pmtStream);

                n += (5 + esInfoLength);
            }
        }

        mPmt.setPmtStreamList(mPmtStreamList);

        return mPmt;
    }
}
