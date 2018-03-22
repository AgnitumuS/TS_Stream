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
 * @date 2018/3/22
 */

public class SectionManager {
    private static final String TAG = "SectionManager";
    private static final int PACKET_HEADER_LENGTH = 4;
    private static final int SKIP_ONE = 1;
    private static final int THE_FIRST_THREE = 3;

    private boolean isStartToMatchOneSection = false;
    private int mContinuityCounter = -1;
    private int mNeedPacketNum = 1;

    private Section mSection = new Section();
    private List<Section> mSectionList = new ArrayList<>();


    public SectionManager() {
        super();
    }

    public void matchSection(Packet packet, int inputTableId) {
        byte[] buff = packet.getPacket();
        int packetLength = buff.length;

        int transportErrorIndicator = packet.getTransportErrorIndicator();
        int payloadUnitStartIndicator = packet.getPayloadUnitStartIndicator();
        int continuityCounter = packet.getContinuityCounter();
        Log.d(TAG, " ---------------------------------------------- ");
        Log.d(TAG, "transportErrorIndicator : " + toHexString(transportErrorIndicator));
        Log.d(TAG, "payloadUnitStartIndicator : " + toHexString(payloadUnitStartIndicator));
        Log.d(TAG, "continuityCounter : " + toHexString(continuityCounter));

        // 判断传输错误
        if (transportErrorIndicator == 0x1) {
            Log.e(TAG, "transport_error_indicator : " + toHexString(transportErrorIndicator));
            return;
        }

        int skipToSectionBegin = 3;
        if (payloadUnitStartIndicator == 0x1) {
            isStartToMatchOneSection = true;
            // 包的累加计算
            mContinuityCounter = continuityCounter;

            skipToSectionBegin += 1;

            int tableId = buff[skipToSectionBegin + 1] & 0xFF;
            int sectionLength = ((buff[skipToSectionBegin + 2] & 0xF) << 8 | buff[skipToSectionBegin + 3]) & 0xFFF;
            int versionNumber = (buff[skipToSectionBegin + 6] >> 1) & 0x1F;
            int sectionNumber = buff[skipToSectionBegin + 7] & 0xFF;
            int lastSectionNumber = buff[skipToSectionBegin + 8] & 0xFF;
            Log.d(TAG, "  ");
            Log.d(TAG, "tableId : " + toHexString(tableId));
            Log.d(TAG, "sectionLength : " + toHexString(sectionLength));
            Log.d(TAG, "versionNumber : " + toHexString(versionNumber));
            Log.d(TAG, "sectionNumber : " + toHexString(sectionNumber));
            Log.d(TAG, "lastSectionNumber : " + toHexString(lastSectionNumber));

            if (tableId != inputTableId) {
                Log.e(TAG, "tableId : " + toHexString(tableId));
                return;
            }

            mSection = new Section(tableId, sectionLength, versionNumber, sectionNumber, lastSectionNumber);

            // tableId : 8
            // section_syntax_indicator : 1
            // zero : 1
            // reserved_1 : 2
            // sectionLength : 12
            // -- total : 24 (3 byte)
            int sectionSize = sectionLength + 3;
            Log.d(TAG, "sectionSize : " + sectionSize);

            byte[] sectionData = new byte[sectionSize];
            int sectionCursor = mSection.getSectionCursor();

            // 计算 section length 是否挎包,跨多少
            mNeedPacketNum = 1;
            int theMaxEffectiveData = packetLength - PACKET_HEADER_LENGTH - SKIP_ONE - THE_FIRST_THREE;
            int dValue = sectionLength - theMaxEffectiveData;
            if (dValue <= 0) {
                Log.d(TAG, " ------------ 需要包数 : " + mNeedPacketNum);
                for (int i = 0; i < sectionSize; i++) {
                    sectionCursor++;
                    sectionData[i] = buff[skipToSectionBegin + 1 + i];
                }
            } else {
                mNeedPacketNum += dValue / (packetLength - PACKET_HEADER_LENGTH) + 1;
                Log.d(TAG, " ------------ 需要包数 : " + mNeedPacketNum);
                for (int i = 0; i < theMaxEffectiveData; i++) {
                    sectionCursor++;
                    sectionData[i] = buff[skipToSectionBegin + 1 + i];
                }
            }
            mNeedPacketNum--;
            Log.d(TAG, " ------------ 还需包数 : " + mNeedPacketNum);

            // 保存 Section
            mSection.setSectionData(sectionData);
            // 记录 Section 写到的位置
            mSection.setSectionCursor(sectionCursor);

        } else {
            int sectionCursor = mSection.getSectionCursor();
            if (sectionCursor == 0) {
                Log.e(TAG, "还未有表头信息");
                return;
            } else {
                // 继续补充 section
                byte[] unFinishSectionData = mSection.getSectionData();
                int times = 0;

                int theMaxEffectiveData = packetLength - PACKET_HEADER_LENGTH;
                int surplusValue = mSection.getSectionLength() - (sectionCursor - THE_FIRST_THREE);
                if (surplusValue <= theMaxEffectiveData) {
                    for (int i = 0; i < surplusValue; i++) {
                        times++;
                        unFinishSectionData[sectionCursor + i] = buff[PACKET_HEADER_LENGTH + i];
                    }
                } else {
                    for (int i = 0; i < theMaxEffectiveData; i++) {
                        times++;
                        unFinishSectionData[sectionCursor + i] = buff[skipToSectionBegin + 1 + i];
                    }
                }
                mNeedPacketNum--;
                Log.d(TAG, " ------------ 还需包数 : " + mNeedPacketNum);

                mSection.setSectionData(unFinishSectionData);
                mSection.setSectionCursor(sectionCursor + times);
            }
        }

        if (mNeedPacketNum == 0) {
            updateSectionList();
        }
    }

    private void updateSectionList() {
        // 判断是否为一条完整的 section
        if (mSection.getSectionCursor() == mSection.getSectionLength() + 3) {
            // 分配新内存，防止引用相同内存地址
            Section section = new Section(
                    mSection.getTableId(),
                    mSection.getSectionLength(),
                    mSection.getVersionNumber(),
                    mSection.getSectionNumber(),
                    mSection.getLastSectionNumber(),
                    mSection.getSectionData(),
                    mSection.getSectionCursor());

            // 如果 mSectionList 为空，则先加一条
            if (mSectionList.size() == 0) {
                mSectionList.add(section);
            } else {
                Section compareSection = mSectionList.get(mSectionList.size() - 1);
                // 判断 VersionNumber
                if (section.getVersionNumber() == compareSection.getVersionNumber()) {
                    if (section.getSectionNumber() != compareSection.getSectionNumber()) {
                        mSectionList.add(section);
                    }
                } else {
                    mSectionList.clear();
                    mSectionList.add(section);
                }
            }

        }
    }

    public List<Section> getSectionList() {
        Section section = mSectionList.get(mSectionList.size() - 1);
        byte[] tmp = section.getSectionData();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tmp.length; i++) {
            sb.append(" " + toHexString(tmp[i] & 0xFF));
        }
        Log.d(TAG, " ---------------------------------------------- ");
        Log.d(TAG, "Section List Size : " + mSectionList.size());
        Log.d(TAG, "table_id : " + toHexString(section.getTableId()));
        Log.d(TAG, "section_length : " + toHexString(section.getSectionLength()));
        Log.d(TAG, "version_number : " + toHexString(section.getVersionNumber()));
        Log.d(TAG, "section_number : " + toHexString(section.getSectionNumber()));
        Log.d(TAG, "last_section_number : " + toHexString(section.getLastSectionNumber()));
        Log.d(TAG, sb.toString());

        return mSectionList;
    }

}
