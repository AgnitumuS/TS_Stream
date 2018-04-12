package com.excellence.iptv.util;

import android.util.Log;

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


    private Section mSection;
    private List<Section> mSectionList = new ArrayList<>();

    private boolean isFinishOne = false;


    public SectionManager() {
        super();
    }

    public int matchSection(Packet packet, int inputTableId) {
        Log.d(TAG, " --------------------------------------- ");
        Log.d(TAG, " -- matchSection()");

        byte[] packetBuff = packet.getPacket();
        int packetLength = packetBuff.length;

        int transportErrorIndicator = packet.getTransportErrorIndicator();
        int payloadUnitStartIndicator = packet.getPayloadUnitStartIndicator();
        int continuityCounter = packet.getContinuityCounter();

        // 判断传输错误
        if (transportErrorIndicator == 0x1) {
            Log.e(TAG, "Error transport_error_indicator : " + toHexString(transportErrorIndicator));
            return -1;
        }

        // 判断 packet 的类型
        if (payloadUnitStartIndicator == 0x1) {
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
                Log.e(TAG, "Error tableId : 0x" + toHexString(tableId));
                return -1;
            }


            // 构建 Section 对象
            mSection = new Section(tableId, sectionLength, transportStreamIdOrServiceId,
                    versionNumber, sectionNumber, lastSectionNumber);

            // 匹配 TransportStreamIdOrServiceId ，寻找同组的 section
            if (mSectionList.size() != 0) {
                List<Section> needToRefreshList = new ArrayList<>();
                for (Section section : mSectionList) {
                    // 同组 section
                    if (section.getTransportStreamIdOrServiceId() == mSection.getTransportStreamIdOrServiceId()) {
                        // 版本号不同
                        if (section.getVersionNumber() != mSection.getVersionNumber()) {
                            // 版本更新
                            needToRefreshList.add(section);
                        } else {
                            // 版本号相同，判断 sectionNum 是否存在
                            if (section.getSectionNumber() == mSection.getSectionNumber()){
                                Log.e(TAG, "Error old sectionNumber : 0x" + toHexString(sectionNumber));
                                return -1;
                            }
                        }
                    }
                }
                mSectionList.removeAll(needToRefreshList);
            }

            // 取出待操作数据
            int sectionSize = mSection.getSectionLength() + 3;
            byte[] sectionData = mSection.getSectionData();
            int sectionCursor = mSection.getSectionCursor();
            int nextContinuityCounter = mSection.getNextContinuityCounter();

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
            // 更新 mSection 值
            mSection.setSectionCursor(sectionCursor);
            mSection.setNextContinuityCounter(nextContinuityCounter);
            mSection.setSectionData(sectionData);
            mSectionList.add(mSection);

        } else {

            if (mSectionList.size() == 0) {
                Log.e(TAG, "Error mSectionList.size() == 0 ");
                return -1;
            }

            // 匹配需要拼接的 sectionNumber
            int unFinishSectionNumber = -1;
            for (Section section : mSectionList) {
                if (section.getNextContinuityCounter() == continuityCounter) {
                    mSection = section;
                    break;
                }
            }

            if (mSection == null) {
                Log.e(TAG, "Error no match unFinish Section");
                return -1;
            }

            int sectionSize = mSection.getSectionLength() + 3;
            byte[] sectionData = mSection.getSectionData();
            int sectionCursor = mSection.getSectionCursor();
            int nextContinuityCounter = mSection.getNextContinuityCounter();

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

        return 0;
    }


    public List<Section> getSectionList() {
        for (Section section : mSectionList) {
            if (section.getSectionCursor() < section.getSectionLength() + 3) {
                mSectionList.remove(section);
            }
        }
        if (mSectionList.size() == 0) {
            Log.e(TAG, "Error mSectionList.size() == 0");
            return null;
        }
        return mSectionList;
    }

    public void print() {
        if (mSectionList.size() == 0) {
            Log.e(TAG, "Error mSectionList.size() == 0");
            return;
        }
        Log.d(TAG, " --------------------------------------------------------------------------- ");
        Log.d(TAG, " Section List : " + mSectionList.size());
        for (int i = 0; i < mSectionList.size(); i++) {
            mSection = mSectionList.get(i);
            Log.d(TAG, " --------------------------------------------------------------------------- ");
            Log.d(TAG, " Section : " + i);
            Log.d(TAG, " Section Size : " + mSection.getSectionLength() + 3);
            byte[] tmp = mSection.getSectionData();
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


    public boolean getIsFinishOne() {
        return isFinishOne;
    }

}
