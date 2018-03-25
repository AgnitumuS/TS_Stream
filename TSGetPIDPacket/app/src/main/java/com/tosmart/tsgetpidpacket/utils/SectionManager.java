package com.tosmart.tsgetpidpacket.utils;

import android.util.Log;


import com.tosmart.tsgetpidpacket.beans.Packet;
import com.tosmart.tsgetpidpacket.beans.Section;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.toHexString;

/**
 * SectionManager
 *
 * @author ggz
 * @date 2018/3/24
 */

public class SectionManager {
    private static final String TAG = "SectionManager";
    private static final int PACKET_HEADER_LENGTH = 4;
    private static final int SKIP_ONE = 1;
    private static final int SECTION_BEGIN_POSITION_1 = 5;
    private static final int SECTION_BEGIN_POSITION_2 = 4;


    private int mVersionNumber = -1;
    private byte[][] mList;
    private int[] mCursor;
    private int[] mNextContinuityCounter;


    private List<Section> mSectionList = new ArrayList<>();


    public SectionManager() {
        super();
    }

    public void matchSection(Packet packet, int inputTableId) {
        byte[] packetBuff = packet.getPacket();
        int packetLength = packetBuff.length;

        int syncByte = packet.getSyncByte();
        int transportErrorIndicator = packet.getTransportErrorIndicator();
        int payloadUnitStartIndicator = packet.getPayloadUnitStartIndicator();
        int pid = packet.getPid();
        int continuityCounter = packet.getContinuityCounter();
        Log.d(TAG, " ---------------------------------------------- ");
        Log.d(TAG, "syncByte : " + toHexString(syncByte));
        Log.d(TAG, "transportErrorIndicator : " + toHexString(transportErrorIndicator));
        Log.d(TAG, "payloadUnitStartIndicator : " + toHexString(payloadUnitStartIndicator));
        Log.d(TAG, "pid : " + toHexString(pid));
        Log.d(TAG, "continuityCounter : " + toHexString(continuityCounter));

        // 判断传输错误
        if (transportErrorIndicator == 0x1) {
            Log.e(TAG, "transport_error_indicator : " + toHexString(transportErrorIndicator));
            return;
        }


        // 判断 packet 的类型
        if (payloadUnitStartIndicator == 0x1) {

            // 解表头的数据
            int tableId = packetBuff[SECTION_BEGIN_POSITION_1] & 0xFF;
            int sectionLength = ( ((packetBuff[SECTION_BEGIN_POSITION_1 + 1] & 0xF) << 8) | (packetBuff[SECTION_BEGIN_POSITION_1 + 2] & 0xFF) ) & 0xFFF;
            int versionNumber = (packetBuff[SECTION_BEGIN_POSITION_1 + 5] >> 1) & 0x1F;
            int sectionNumber = packetBuff[SECTION_BEGIN_POSITION_1 + 6] & 0xFF;
            int lastSectionNumber = packetBuff[SECTION_BEGIN_POSITION_1 + 7] & 0xFF;

            // 判断 tableId
            if (tableId != inputTableId) {
                Log.e(TAG, "tableId : " + toHexString(tableId));
                return;
            }
            Log.d(TAG, "  ");
            Log.d(TAG, "tableId : " + toHexString(tableId));
            Log.d(TAG, "sectionLength : " + toHexString(sectionLength));
            Log.d(TAG, "versionNumber : " + toHexString(versionNumber));
            Log.d(TAG, "sectionNumber : " + toHexString(sectionNumber));
            Log.d(TAG, "lastSectionNumber : " + toHexString(lastSectionNumber));

            // tableId : 8
            // section_syntax_indicator : 1
            // zero : 1
            // reserved_1 : 2
            // sectionLength : 12
            // -- total : 24 (3 byte)
            int sectionSize = sectionLength + 3;
            Log.d(TAG, "sectionSize : " + sectionSize);

            // 判断 versionNumber
            if (mVersionNumber == -1) {
                Log.d(TAG, " ---- new versionNumber : " + toHexString(versionNumber));
                // 初始化数据：mVersionNumber, mList, mCursor, mNextContinuityCounter
                initData(versionNumber, lastSectionNumber);

            } else {
                // 版本更新
                if (mVersionNumber != versionNumber) {
                    Log.d(TAG, " ---- new versionNumber : " + toHexString(versionNumber));
                    initData(versionNumber, lastSectionNumber);
                }
            }

            // 判断 sectionNumber 是否已经在 mList
            // 未记录：新建
            // 已记录：结束（忽略）
            int num = mCursor[sectionNumber];
            if (num == 0) {
                Log.d(TAG, " ---- new sectionNumber : " + toHexString(sectionNumber));
                mList[sectionNumber] = new byte[sectionSize];

                Log.d(TAG, "mList[" + sectionNumber + "].length : " + mList[sectionNumber].length);
            } else {
                Log.e(TAG, " ---- old sectionNumber : " + toHexString(sectionNumber));
                return;
            }

            // 下面将进行记录当前 sectionNumber 操作

            // 判断 sectionLength
            int theMaxEffectiveLength = packetLength - PACKET_HEADER_LENGTH - SKIP_ONE;
            if (sectionSize <= theMaxEffectiveLength) {
                for (int i = 0; i < sectionSize; i++) {
                    mList[sectionNumber][i] = packetBuff[SECTION_BEGIN_POSITION_1 + i];
                    mCursor[sectionNumber]++;
                }
            } else {
                // 挎包
                for (int i = 0; i < theMaxEffectiveLength; i++) {
                    mList[sectionNumber][i] = packetBuff[SECTION_BEGIN_POSITION_1 + i];
                    mCursor[sectionNumber]++;
                }
                // 记录下一个 packet 的 ContinuityCounter
                if (continuityCounter == 15) {
                    continuityCounter = -1;
                }
                mNextContinuityCounter[sectionNumber] = continuityCounter + 1;
                Log.d(TAG, " ---- mNextContinuityCounter[" + sectionNumber + "] : " + mNextContinuityCounter[sectionNumber]);
            }
            Log.d(TAG, " ---- mCursor[" + sectionNumber + "] : " + mCursor[sectionNumber] + "(" + sectionSize + ")");


        } else {

            if (mVersionNumber == -1) {
                Log.e(TAG, " ---- no versionNumber");
                return;
            }

            // 寻找需要拼接的 sectionNumber
            int unFinishSectionNumber = -1;
            for (int i = 0; i < mNextContinuityCounter.length; i++) {
                if (mNextContinuityCounter[i] == continuityCounter) {
                    unFinishSectionNumber = i;
                }
            }

            if (unFinishSectionNumber == -1) {
                Log.e(TAG, " ----  no unFinishSectionNumber");
                return;
            }

            int sectionSize = mList[unFinishSectionNumber].length;
            int theMaxEffectiveLength = packetLength - PACKET_HEADER_LENGTH;
            int surplusValue = sectionSize - mCursor[unFinishSectionNumber];
            if (surplusValue <= theMaxEffectiveLength) {
                int cursor = 0;
                for (int i = 0; i < surplusValue; i++) {
                    cursor = mCursor[unFinishSectionNumber];
                    mList[unFinishSectionNumber][cursor] = packetBuff[SECTION_BEGIN_POSITION_2 + i];
                    mCursor[unFinishSectionNumber]++;
                }
                mNextContinuityCounter[unFinishSectionNumber] = -1;
            } else {
                int cursor = 0;
                for (int i = 0; i < theMaxEffectiveLength; i++) {
                    cursor = mCursor[unFinishSectionNumber];
                    mList[unFinishSectionNumber][cursor] = packetBuff[SECTION_BEGIN_POSITION_2 + i];
                    mCursor[unFinishSectionNumber]++;
                }
                // 记录下一个 packet 的 ContinuityCounter
                if (continuityCounter == 15) {
                    continuityCounter = -1;
                }
                mNextContinuityCounter[unFinishSectionNumber] = continuityCounter + 1;
                Log.d(TAG, " ---- mNextContinuityCounter[" + unFinishSectionNumber + "] : " + mNextContinuityCounter[unFinishSectionNumber]);
            }
            Log.d(TAG, " ---- mCursor[" + unFinishSectionNumber + "] : " + mCursor[unFinishSectionNumber] + "(" + sectionSize + ")");


        }
    }

    // 初始化数据
    private void initData(int versionNumber, int lastSectionNumber) {
        int size = lastSectionNumber + 1;

        mVersionNumber = versionNumber;
        mList = new byte[size][];
        mCursor = new int[size];
        mNextContinuityCounter = new int[size];
        for (int i = 0; i < size; i++) {
            mCursor[i] = 0;
            mNextContinuityCounter[i] = -1;
        }
    }


    public void print() {
        Log.d(TAG, " --------------------------------------------------------------------------- ");
        Log.d(TAG, " Section List : " + mList.length);
        for (int i = 0; i < mList.length; i++) {
            Log.d(TAG, " --------------------------------------------------------------------------- ");
            Log.d(TAG, " Section : " + i);
            Log.d(TAG, " Section Size : " + mCursor[i]);
            byte[] tmp = mList[i];
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < tmp.length; j++) {
                sb.append(" " + toHexString(tmp[j] & 0xFF));
                if (j % 15 == 14) {
                    sb.append("\n");
                }
            }
            Log.d(TAG, sb.toString());
        }

    }


}
