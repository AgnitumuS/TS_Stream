package com.excellence.iptv.util;

import android.util.Log;
import android.util.SparseArray;

import com.excellence.iptv.bean.Packet;
import com.excellence.iptv.bean.Section;

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
    private static final int PACKET_LENGTH_204 = 204;
    private static final boolean IS_LOG = false;

    private Section mSection;
    private SparseArray<List<Section>> mSparseArray = new SparseArray<>();

    private List<Section> mSectionList = new ArrayList<>();


    public SectionManager() {
        super();
    }

    public int matchSection(Packet packet, int inputTableId) {
        if (IS_LOG) {
            Log.d(TAG, " --------------------------------------- ");
            Log.d(TAG, " -- matchSection()");
        }

        // 获取包数据
        byte[] packetBuff = packet.getPacket();
        int packetLength = packetBuff.length;

        int payloadUnitStartIndicator = packet.getPayloadUnitStartIndicator();
        int continuityCounter = packet.getContinuityCounter();
        if (IS_LOG) {
            Log.d(TAG, "payloadUnitStartIndicator : " + payloadUnitStartIndicator);
            Log.d(TAG, "continuityCounter : " + continuityCounter);
        }

        // 判断 payloadUnitStartIndicator
        if (payloadUnitStartIndicator == 1) {

            // 解表头的数据
            int tableId = packetBuff[SECTION_BEGIN_POSITION_1] & 0xFF;
            int sectionLength = (((packetBuff[SECTION_BEGIN_POSITION_1 + 1] & 0xF) << 8) |
                    (packetBuff[SECTION_BEGIN_POSITION_1 + 2] & 0xFF)) & 0xFFF;
            int transportStreamIdOrServiceId = (((packetBuff[SECTION_BEGIN_POSITION_1 + 3] & 0xFF) << 8) |
                    (packetBuff[SECTION_BEGIN_POSITION_1 + 4] & 0xFF)) & 0xFFFF;
            int versionNumber = (packetBuff[SECTION_BEGIN_POSITION_1 + 5] >> 1) & 0x1F;
            int sectionNumber = packetBuff[SECTION_BEGIN_POSITION_1 + 6] & 0xFF;
            int lastSectionNumber = packetBuff[SECTION_BEGIN_POSITION_1 + 7] & 0xFF;

            // 判断 tableId
            if (tableId != inputTableId) {
                if (IS_LOG) {
                    Log.e(TAG, "Error not the match tableId : 0x" + toHexString(tableId));
                }
                return -1;
            }
            if (IS_LOG) {
                Log.d(TAG, " -- ");
                Log.d(TAG, "tableId : 0x" + toHexString(tableId));
                Log.d(TAG, "sectionLength : " + sectionLength);
                Log.d(TAG, "transportStreamIdOrServiceId : 0x" + toHexString(transportStreamIdOrServiceId));
                Log.d(TAG, "versionNumber : 0x" + toHexString(versionNumber));
                Log.d(TAG, "sectionNumber : 0x" + toHexString(sectionNumber));
                Log.d(TAG, "lastSectionNumber : 0x" + toHexString(lastSectionNumber));
            }


            List<Section> sectionList = mSparseArray.get(transportStreamIdOrServiceId,
                    null);
            if (sectionList != null) {
                // 判断 VersionNumber
                if (sectionList.get(0).getVersionNumber() != versionNumber) {
                    // 版本号不同，版本更新
                    sectionList.clear();
                    if (IS_LOG) {
                        Log.e(TAG, "New VersionNumber : 0x" + toHexString(versionNumber));
                    }
                } else {
                    // 版本号相同，判断 SectionNumber
                    for (Section section : sectionList) {
                        if (section.getSectionNumber() == sectionNumber) {
                            if (IS_LOG) {
                                Log.e(TAG, "Error old sectionNumber : 0x" + toHexString(sectionNumber));
                            }
                            return -1;
                        }
                    }
                }
            }

            // 初始化参数，添加新 section
            int sectionSize = sectionLength + 3;
            byte[] sectionData = new byte[sectionSize];
            int sectionCursor = 0;
            int nextContinuityCounter;

            // 判断当前包所含的 section 数据长度
            int theMaxEffectiveLength = packetLength - PACKET_HEADER_LENGTH - SKIP_ONE;
            if (packetLength == PACKET_LENGTH_204) {
                theMaxEffectiveLength -= 16;
            }
            // 判断 sectionLength
            if (sectionSize <= theMaxEffectiveLength) {
                // 数据在单个包内
                for (int i = 0; i < sectionSize; i++) {
                    sectionData[i] = packetBuff[SECTION_BEGIN_POSITION_1 + i];
                    sectionCursor++;
                }
                nextContinuityCounter = -1;

            } else {
                // 挎包
                for (int i = 0; i < theMaxEffectiveLength; i++) {
                    sectionData[i] = packetBuff[SECTION_BEGIN_POSITION_1 + i];
                    sectionCursor++;
                }
                // 记录 nextContinuityCounter
                nextContinuityCounter = continuityCounter + 1;
                if (nextContinuityCounter > 15) {
                    nextContinuityCounter = 0;
                }
            }
            // 构建 Section 对象
            mSection = new Section(tableId, sectionLength, transportStreamIdOrServiceId,
                    versionNumber, sectionNumber, lastSectionNumber);
            mSection.setSectionCursor(sectionCursor);
            mSection.setNextContinuityCounter(nextContinuityCounter);
            mSection.setSectionData(sectionData);

        } else {
            // payloadUnitStartIndicator == 0

            if (mSection == null || !mSection.isUnFinish()) {
                if (IS_LOG) {
                    Log.e(TAG, "Error no match unFinish Section");
                }
                return -1;
            }

            if (mSection.getNextContinuityCounter() != continuityCounter) {
                if (IS_LOG) {
                    Log.e(TAG, "Error continuityCounter is not right");
                }
                return -1;
            }

            // 初始化参数，拼接 section
            int sectionSize = mSection.getSectionLength() + 3;
            byte[] sectionData = mSection.getSectionData();
            int sectionCursor = mSection.getSectionCursor();
            int nextContinuityCounter;

            int theMaxEffectiveLength = packetLength - PACKET_HEADER_LENGTH;
            if (packetLength == PACKET_LENGTH_204) {
                theMaxEffectiveLength -= 16;
            }
            int surplusValue = sectionSize - sectionCursor;
            if (surplusValue <= theMaxEffectiveLength) {
                // 数据在单个包内
                for (int i = 0; i < surplusValue; i++) {
                    sectionData[sectionCursor] = packetBuff[SECTION_BEGIN_POSITION_2 + i];
                    sectionCursor++;
                }
                nextContinuityCounter = -1;

            } else {
                // 挎包
                for (int i = 0; i < theMaxEffectiveLength; i++) {
                    sectionData[sectionCursor] = packetBuff[SECTION_BEGIN_POSITION_2 + i];
                    sectionCursor++;
                }
                // 记录 nextContinuityCounter
                nextContinuityCounter = continuityCounter + 1;
                if (nextContinuityCounter > 15) {
                    nextContinuityCounter = 0;
                }
            }
            // 更新 mSection 值
            mSection.setSectionCursor(sectionCursor);
            mSection.setNextContinuityCounter(nextContinuityCounter);
            mSection.setSectionData(sectionData);
        }

        if (IS_LOG) {
            Log.d(TAG, "  ");
            Log.d(TAG, "sectionCursor : " + mSection.getSectionCursor());
            Log.d(TAG, "nextContinuityCounter : " + mSection.getNextContinuityCounter());
        }

        if (!mSection.isUnFinish()) {
            List<Section> sectionList = mSparseArray.get(mSection.getTransportStreamIdOrServiceId(),
                    null);
            if (sectionList != null) {
                sectionList.add(mSection);
            } else {
                sectionList = new ArrayList<>();
                sectionList.add(mSection);
                mSparseArray.put(mSection.getTransportStreamIdOrServiceId(), sectionList);
            }
        }

        return 0;
    }


    public List<Section> getSectionList() {

        mSectionList.clear();
        for (int i = 0; i < mSparseArray.size(); i++) {
            int key = mSparseArray.keyAt(i);
            List<Section> list = mSparseArray.get(key);
            if (list.size() == list.get(0).getLastSectionNumber() + 1) {
                mSectionList.addAll(list);
            }
        }

        return mSectionList;
    }

    public void print() {

        mSectionList.clear();
        for (int i = 0; i < mSparseArray.size(); i++) {
            int key = mSparseArray.keyAt(i);
            List<Section> list = mSparseArray.get(key);
            if (list.size() == list.get(0).getLastSectionNumber() + 1) {
                mSectionList.addAll(list);
            }
        }

        Log.d(TAG, " --------------------------------------------------------------------------- ");
        Log.d(TAG, " Section List : " + mSectionList.size());
        for (int i = 0; i < mSectionList.size(); i++) {
            Section section = mSectionList.get(i);
            Log.d(TAG, " --------------------------------------------------------------------------- ");
            Log.d(TAG, " Section : " + i);
            Log.d(TAG, " Section Size : " + section.getSectionLength() + 3);
            byte[] tmp = section.getSectionData();
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
