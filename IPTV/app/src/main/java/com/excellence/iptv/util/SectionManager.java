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
    private static final boolean IS_LOG = false;

    private List<Section> mSectionList = new ArrayList<>();

    private boolean isFinishOne = false;


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
        int transportErrorIndicator = packet.getTransportErrorIndicator();
        int payloadUnitStartIndicator = packet.getPayloadUnitStartIndicator();
        int continuityCounter = packet.getContinuityCounter();

        // 判断传输错误
        if (transportErrorIndicator == 0x1) {
            if (IS_LOG) {
                Log.e(TAG, "Error transport_error_indicator : 0x" + toHexString(transportErrorIndicator));
            }
            return -1;
        }

        // 判断 payloadUnitStartIndicator
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
                if (IS_LOG) {
                    Log.e(TAG, "Error not the match tableId : 0x" + toHexString(tableId));
                }
                return -1;
            }


            if (mSectionList.size() != 0) {
                List<Section> needToRefreshList = new ArrayList<>();
                // 遍历 mSectionList
                for (Section section : mSectionList) {
                    // 判断 transportStreamIdOrServiceId  versionNumber
                    if (section.getTransportStreamIdOrServiceId() == transportStreamIdOrServiceId) {
                        // 版本号不同，版本更新
                        if (section.getVersionNumber() != versionNumber) {
                            needToRefreshList.add(section);
                        } else {
                            // 版本号相同，判断 sectionNum 是否存在
                            if (section.getSectionNumber() == sectionNumber) {
                                if (IS_LOG) {
                                    Log.e(TAG, "Error old sectionNumber : 0x" + toHexString(sectionNumber));
                                }
                                return -1;
                            }
                        }
                    }
                }
                // 清除旧版本
                mSectionList.removeAll(needToRefreshList);
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
            Section section = new Section(tableId, sectionLength, transportStreamIdOrServiceId,
                    versionNumber, sectionNumber, lastSectionNumber);
            section.setSectionCursor(sectionCursor);
            section.setNextContinuityCounter(nextContinuityCounter);
            section.setSectionData(sectionData);
            mSectionList.add(section);


        } else {
            // payloadUnitStartIndicator == 0

            if (mSectionList.size() == 0) {
                if (IS_LOG) {
                    Log.e(TAG, "Error mSectionList.size() == 0 ");
                }
                return -1;
            }

            Section unFinishSection = null;
            // 遍历 mSectionList，寻找需要拼接的 sectionNumber
            for (Section section : mSectionList) {
                if (section.getNextContinuityCounter() == continuityCounter
                        && section.isUnFinish()) {
                    unFinishSection = section;
                    break;
                }
            }

            if (unFinishSection == null) {
                if (IS_LOG) {
                    Log.e(TAG, "Error no match unFinish Section");
                }
                return -1;
            }

            // 初始化参数，拼接 section
            int sectionSize = unFinishSection.getSectionLength() + 3;
            byte[] sectionData = unFinishSection.getSectionData();
            int sectionCursor = unFinishSection.getSectionCursor();
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
            unFinishSection.setSectionCursor(sectionCursor);
            unFinishSection.setNextContinuityCounter(nextContinuityCounter);
            unFinishSection.setSectionData(sectionData);
        }

        return 0;
    }


    public List<Section> getSectionList() {

        if (mSectionList.size() == 0) {
            Log.e(TAG, "Error mSectionList.size() == 0");
            return null;
        }

        // 清除未完成的 Section
        List<Section> unFinishSectionList = new ArrayList<>();
        for (Section section : mSectionList) {
            if (section.isUnFinish()) {
                unFinishSectionList.add(section);
            }
        }
        mSectionList.removeAll(unFinishSectionList);

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


    public boolean getIsFinishOne() {
        return isFinishOne;
    }

}
