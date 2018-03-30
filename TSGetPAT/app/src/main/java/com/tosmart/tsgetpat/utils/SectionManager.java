package com.tosmart.tsgetpat.utils;

import android.util.Log;


import com.tosmart.tsgetpat.beans.Packet;
import com.tosmart.tsgetpat.beans.Section;

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
    private static final int PACKET_LENGTH_188 = 188;
    private static final int PACKET_LENGTH_204 = 204;

    private int mVersionNumber = -1;
    private byte[][] mList;
    private int[] mCursor;
    private int[] mNextContinuityCounter;


    private List<Section> mSectionList = new ArrayList<>();


    public SectionManager() {
        super();
    }

    public int matchSection(Packet packet, int inputTableId) {
        Log.d(TAG, " ---------------------------------------------- ");
        Log.d(TAG, " -- matchSection()");
        byte[] packetBuff = packet.getPacket();
        int packetLength = packetBuff.length;

        int syncByte = packet.getSyncByte();
        int transportErrorIndicator = packet.getTransportErrorIndicator();
        int payloadUnitStartIndicator = packet.getPayloadUnitStartIndicator();
        int pid = packet.getPid();
        int continuityCounter = packet.getContinuityCounter();
        Log.d(TAG, "syncByte : 0x" + toHexString(syncByte));
        Log.d(TAG, "transportErrorIndicator : 0x" + toHexString(transportErrorIndicator));
        Log.d(TAG, "payloadUnitStartIndicator : 0x" + toHexString(payloadUnitStartIndicator));
        Log.d(TAG, "pid : 0x" + toHexString(pid));
        Log.d(TAG, "continuityCounter : 0x" + toHexString(continuityCounter));

        // 判断传输错误
        if (transportErrorIndicator == 0x1) {
            Log.e(TAG, "transport_error_indicator : " + toHexString(transportErrorIndicator));
            return -1;
        }


        // 判断 packet 的类型
        if (payloadUnitStartIndicator == 0x1) {

            // 解表头的数据
            int tableId = packetBuff[SECTION_BEGIN_POSITION_1] & 0xFF;
            int sectionLength = (((packetBuff[SECTION_BEGIN_POSITION_1 + 1] & 0xF) << 8) |
                    (packetBuff[SECTION_BEGIN_POSITION_1 + 2] & 0xFF)) & 0xFFF;
            int versionNumber = (packetBuff[SECTION_BEGIN_POSITION_1 + 5] >> 1) & 0x1F;
            int sectionNumber = packetBuff[SECTION_BEGIN_POSITION_1 + 6] & 0xFF;
            int lastSectionNumber = packetBuff[SECTION_BEGIN_POSITION_1 + 7] & 0xFF;

            // 判断 tableId
            if (tableId != inputTableId) {
                Log.e(TAG, "tableId : 0x" + toHexString(tableId));
                return -1;
            }
            Log.d(TAG, "  ");
            Log.d(TAG, "tableId : 0x" + toHexString(tableId));
            Log.d(TAG, "sectionLength : 0x" + toHexString(sectionLength) + "(" + sectionLength + ")");
            Log.d(TAG, "versionNumber : 0x" + toHexString(versionNumber));
            Log.d(TAG, "sectionNumber : 0x" + toHexString(sectionNumber));
            Log.d(TAG, "lastSectionNumber : 0x" + toHexString(lastSectionNumber));

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
                Log.d(TAG, "  ");
                Log.d(TAG, "new versionNumber : 0x" + toHexString(versionNumber));
                // 初始化数据：mVersionNumber, mList, mCursor, mNextContinuityCounter
                initData(versionNumber, lastSectionNumber);

            } else {
                // 版本更新
                if (mVersionNumber != versionNumber) {
                    Log.d(TAG, "  ");
                    Log.d(TAG, "new versionNumber : 0x" + toHexString(versionNumber));
                    initData(versionNumber, lastSectionNumber);
                }
            }

            // 判断 sectionNumber 是否已经在 mList
            // 未记录：新建
            // 已记录：结束（忽略）
            int num = mCursor[sectionNumber];
            if (num == 0) {
                Log.d(TAG, "  ");
                Log.d(TAG, "new sectionNumber : 0x" + toHexString(sectionNumber));
                mList[sectionNumber] = new byte[sectionSize];

                Log.d(TAG, "mList[" + sectionNumber + "].length : " + mList[sectionNumber].length);
            } else {
                Log.e(TAG, "old sectionNumber : 0x" + toHexString(sectionNumber));
                return -1;
            }

            // 下面将进行记录当前 sectionNumber 操作

            // 判断 sectionLength
            int theMaxEffectiveLength = packetLength - PACKET_HEADER_LENGTH - SKIP_ONE;
            if (packetLength == PACKET_LENGTH_204) {
                theMaxEffectiveLength  = packetLength - PACKET_HEADER_LENGTH - SKIP_ONE - 16;
            }
            if (sectionSize <= theMaxEffectiveLength) {
                for (int i = 0; i < sectionSize; i++) {
                    mList[sectionNumber][i] = packetBuff[SECTION_BEGIN_POSITION_1 + i];
                    mCursor[sectionNumber]++;
                }
                // -2 表示当前 sectionNumber 的数据已写完
                mNextContinuityCounter[sectionNumber] = -2;

                // 把所有完整的 section 都加进 mSectionList
                Section section = new Section(mList[sectionNumber]);
                mSectionList.add(section);
                if (mSectionList.size() == section.getLastSectionNumber() + 1) {
                    return 1;
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
            }
            Log.d(TAG, "mCursor[" + sectionNumber + "] : " + mCursor[sectionNumber] + "(" + sectionSize + ")");
            Log.d(TAG, "mNextContinuityCounter[" + sectionNumber + "] : " + toHexString(mNextContinuityCounter[sectionNumber]));

        } else {

            if (mVersionNumber == -1) {
                Log.e(TAG, "no versionNumber");
                return -1;
            }

            // 寻找需要拼接的 sectionNumber
            int unFinishSectionNumber = -1;
            for (int i = 0; i < mNextContinuityCounter.length; i++) {
                if (mNextContinuityCounter[i] == continuityCounter) {
                    unFinishSectionNumber = i;
                }
            }

            if (unFinishSectionNumber == -1) {
                Log.e(TAG, "no unFinishSectionNumber");
                return -1;
            }

            int sectionSize = mList[unFinishSectionNumber].length;
            int theMaxEffectiveLength = packetLength - PACKET_HEADER_LENGTH;
            if (packetLength == PACKET_LENGTH_204) {
                theMaxEffectiveLength  = packetLength - PACKET_HEADER_LENGTH - 16;
            }
            int surplusValue = sectionSize - mCursor[unFinishSectionNumber];
            if (surplusValue <= theMaxEffectiveLength) {
                for (int i = 0; i < surplusValue; i++) {
                    int cursor = mCursor[unFinishSectionNumber];
                    mList[unFinishSectionNumber][cursor] = packetBuff[SECTION_BEGIN_POSITION_2 + i];
                    mCursor[unFinishSectionNumber]++;
                }
                mNextContinuityCounter[unFinishSectionNumber] = -2;

                // 把所有完整的 section 都加进 mSectionList
                Section section = new Section(mList[unFinishSectionNumber]);
                mSectionList.add(section);
                if (mSectionList.size() == section.getLastSectionNumber() + 1) {
                    return 1;
                }

            } else {
                for (int i = 0; i < theMaxEffectiveLength; i++) {
                    int cursor = mCursor[unFinishSectionNumber];
                    mList[unFinishSectionNumber][cursor] = packetBuff[SECTION_BEGIN_POSITION_2 + i];
                    mCursor[unFinishSectionNumber]++;
                }
                // 记录下一个 packet 的 ContinuityCounter
                if (continuityCounter == 15) {
                    continuityCounter = -1;
                }
                mNextContinuityCounter[unFinishSectionNumber] = continuityCounter + 1;
            }
            Log.d(TAG, "mCursor[" + unFinishSectionNumber + "] : " + mCursor[unFinishSectionNumber] + "(" + sectionSize + ")");
            Log.d(TAG, "mNextContinuityCounter[" + unFinishSectionNumber + "] : " + toHexString(mNextContinuityCounter[unFinishSectionNumber]));

        }

        return 0;
    }

    /**
     * 初始化解析 section 所需的参数
     */
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

        mSectionList.clear();
    }

    public List<Section> getSectionList() {
        if (mSectionList.size() == 0) {
            Log.e(TAG, "no match section ");
            return null;
        }
        return mSectionList;
    }

    public void print() {
        if (mList == null) {
            Log.e(TAG, "no match section ");
            return;
        }
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
                if (j % 20 == 19) {
                    sb.append("\n");
                }
            }
            Log.d(TAG, sb.toString());
        }

    }


}
