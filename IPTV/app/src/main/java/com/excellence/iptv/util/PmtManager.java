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
                mPmt = new Pmt(sectionData);
            } else {
                int sectionNumber = sectionData[6] & 0xFF;
                mPmt.setSectionNumber(sectionNumber);
            }
            Log.d(TAG, " ---------------------------------------------- ");
            Log.d(TAG, " -- makePMT()");
            Log.d(TAG, "tableId : 0x" + toHexString(mPmt.getTableId()));
            Log.d(TAG, "sectionSyntaxIndicator : 0x" + toHexString(mPmt.getSectionSyntaxIndicator()));
            Log.d(TAG, "sectionLength : 0x" + toHexString(mPmt.getSectionLength()));
            Log.d(TAG, "programNumber : 0x" + toHexString(mPmt.getProgramNumber()));
            Log.d(TAG, "versionNumber : 0x" + toHexString(mPmt.getVersionNumber()));
            Log.d(TAG, "currentNextIndicator : 0x" + toHexString(mPmt.getCurrentNextIndicator()));
            Log.d(TAG, "sectionNumber : 0x" + toHexString(mPmt.getSectionNumber()));
            Log.d(TAG, "lastSectionNumber : 0x" + toHexString(mPmt.getLastSectionNumber()));
            Log.d(TAG, "pcrPid : 0x" + toHexString(mPmt.getPcrPid()));
            Log.d(TAG, "programInfoLength : 0x" + toHexString(mPmt.getProgramInfoLength()));

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
            int theEffectiveLength = sectionSize - (16 + mPmt.getProgramInfoLength());
            int pos = 12 + mPmt.getProgramInfoLength();
            for (int n = 0; n < theEffectiveLength; ) {
                Log.d(TAG, " -- ");
                int streamType = sectionData[pos + n] & 0xFF;
                Log.d(TAG, "streamType : 0x" + toHexString(streamType));
                int elementaryPid = (((sectionData[pos + 1 + n] & 0x1F) << 8) |
                        (sectionData[pos + 2 + n] & 0xFF)) & 0x1FFF;
                Log.d(TAG, "elementaryPid : 0x" + toHexString(elementaryPid));
                int esInfoLength = (((sectionData[pos + 3 + n] & 0xF) << 8) |
                        (sectionData[pos + 4 + n] & 0xFF)) & 0xFFF;
                Log.d(TAG, "esInfoLength : 0x" + toHexString(esInfoLength));

                PmtStream pmtStream = new PmtStream(streamType, elementaryPid, esInfoLength);
                mPmtStreamList.add(pmtStream);

                n += (5 + esInfoLength);
            }
        }

        mPmt.setPmtStreamList(mPmtStreamList);

        return mPmt;
    }
}
