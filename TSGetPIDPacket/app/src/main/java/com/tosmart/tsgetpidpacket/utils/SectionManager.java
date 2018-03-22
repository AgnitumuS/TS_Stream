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

    private Section mSection;

    private List<Section> mSectionList = new ArrayList<>();


    public SectionManager() {
        super();
    }

    public void matchSection(Packet packet, int inputTableId) {
        byte[] buff = packet.getPacket();

        // 判断 Section Data 的开始标志
        int skipToSectionBegin = 3;
        if (packet.getPayloadUnitStartIndicator() == 0x1) {
            // 除去一个字节
            skipToSectionBegin += 1;

            int tableId = buff[skipToSectionBegin + 1] & 0xFF;
            int sectionLength = ((buff[skipToSectionBegin + 2] & 0xF) << 8 | buff[skipToSectionBegin + 3]) & 0xFFF;
            int versionNumber = (buff[skipToSectionBegin + 6] >> 1) & 0x1F;
            int sectionNumber = buff[skipToSectionBegin + 7] & 0xFF;
            int lastSectionNumber = buff[skipToSectionBegin + 8] & 0xFF;

            if (tableId != inputTableId) {
                return;
            }

            mSection = new Section(
                    tableId,
                    sectionLength,
                    versionNumber,
                    sectionNumber,
                    lastSectionNumber);

            // tableId : 8
            // section_syntax_indicator : 1
            // zero : 1
            // reserved_1 : 2
            // sectionLength : 12
            // -- total : 24 (3 byte)
            byte[] sectionData = new byte[sectionLength + 3];
            // 已读取的 section 长度
            int sectionCursor = mSection.getSectionCursor();

            // 如果 sectionLength <= Packet Data 的最大负荷，循环 sectionLength
            if (sectionLength <= buff.length - 8) {
                for (int i = 0; i < sectionLength + 3; i++) {
                    sectionCursor++;
                    sectionData[i] = buff[skipToSectionBegin + 1 + i];
                }
            } else {
                // 如果 sectionLength > Packet Data 的最大负荷，循环 Packet Length - 8
                for (int i = 0; i < buff.length - 8; i++) {
                    sectionCursor++;
                    sectionData[i] = buff[skipToSectionBegin + 1 + i];
                }
            }
            // 保存 Section
            mSection.setSectionData(sectionData);
            // 记录 Section 写到的位置
            mSection.setSectionCursor(sectionCursor);

        } else {
            // 判断是否存已经存有 section ，
            // 没有就什么都不干，有继续写 unFinishSection
            int sectionCursor = mSection.getSectionCursor();
            if (sectionCursor != 0) {
                // 继续补充 section
                byte[] unFinishSectionData = mSection.getSectionData();
                int times = 0;
                for (int i = 0; i < mSection.getSectionLength() - (sectionCursor - 3); i++) {
                    times++;
                    unFinishSectionData[sectionCursor + i] = buff[skipToSectionBegin + 1 + i];
                }
                mSection.setSectionData(unFinishSectionData);
                mSection.setSectionCursor(sectionCursor + times);
            }
        }

        updateSectionList();
    }

    private void updateSectionList() {
        // 判断是否为一条完整的 section
        if (mSection.getSectionCursor() == mSection.getSectionLength() + 3) {
            Log.d(TAG, "get a complete one section");
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
