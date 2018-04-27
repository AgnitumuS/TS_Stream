package com.excellence.iptv.util;

import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.excellence.iptv.bean.Packet;
import com.excellence.iptv.bean.Program;
import com.excellence.iptv.bean.Section;
import com.excellence.iptv.bean.tables.Eit;
import com.excellence.iptv.bean.tables.Pat;
import com.excellence.iptv.bean.tables.Pmt;
import com.excellence.iptv.bean.tables.Sdt;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * PacketManager
 *
 * @author ggz
 * @date 2018/3/22
 */

public class PacketManager {
    private static final String TAG = "PacketManager";
    private static final int TEN_TIMES = 10;
    private static final int PACKET_HEADER_SYNC_BYTE = 0x47;
    private static final int PACKET_LENGTH_188 = 188;
    private static final int PACKET_LENGTH_204 = 204;
    private static final int PAT_PID = 0x0000;
    private static final int SDT_PID = 0x0011;
    private static final int EIT_PID = 0x0012;
    private static final int PMT_TABLE_ID = 0x02;
    private static final String OUTPUT_FILE_PATH =
            Environment.getExternalStorageDirectory().getPath() + "/ts_history/resultFile";


    private boolean mInterrupt = false;

    private String mInputFilePath = null;
    private String mOutputFilePath = OUTPUT_FILE_PATH;

    private int mPacketLength = -1;
    private int mPacketStartPosition = -1;

    private Pat mPat = null;
    private Sdt mSdt = null;
    private Eit mEit = null;
    private Pmt mPmt = null;

    private List<Pmt> mPmtList = new ArrayList<>();

    private List<Program> mProgramList = null;


    public PacketManager() {
        super();
    }


    /**
     * 匹配 Packet Length 和 Packet Start Position
     */
    public int matchPacketLength(String inputFilePath) {
        Log.d(TAG, " ---------------------------------------------- ");
        Log.d(TAG, " -- matchPacketLength()");
        long startTime = System.currentTimeMillis();

        mInputFilePath = inputFilePath;
        mPacketStartPosition = -1;
        mPacketLength = -1;

        try {
            File file = new File(mInputFilePath);
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
//            FileInputStream bis = new FileInputStream(file);

            boolean isFinish = false;
            byte[] buff = new byte[204 * 11];
            int counter = 0;
            SparseArray<int[]> sparseArray188 = new SparseArray<>();
            SparseArray<int[]> sparseArray204 = new SparseArray<>();

            while (!isFinish) {
                int err = bis.read(buff);
                if (err == -1) {
                    break;
                }
                int length = buff.length;
                for (int i = 0; i < length; i++) {
                    // 当找到 0x47 ,根据相对位置,比对间隔(currentValue)，
                    // 如果相邻，累加；否则清空。
                    if (buff[i] == PACKET_HEADER_SYNC_BYTE) {
                        int result;
                        // 判断 188
                        result = packetLengthMethod(counter, PACKET_LENGTH_188, sparseArray188);
                        if (result == -1) {
                            isFinish = true;
                            break;
                        }
                        // 判断 204
                        result = packetLengthMethod(counter, PACKET_LENGTH_204, sparseArray204);
                        if (result == -1) {
                            isFinish = true;
                            break;
                        }
                    }

                    counter++;
                }
            }

            bis.close();

            Log.d(TAG, "read size : " + counter + " byte");
        } catch (IOException e) {
            Log.e(TAG, "Error IOException");
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        Log.e(TAG, " time : " + (endTime - startTime) + " ms");
        return mPacketLength;
    }

    private int packetLengthMethod(int counter, int matchPacketLen, SparseArray<int[]> sparseArray) {
        // 间隔值
        int currentValue = counter / matchPacketLen;
        // 相对位置
        int relativePosition = counter % matchPacketLen;
        // 查寻相对位置的的记录
        int[] data = sparseArray.get(relativePosition, null);
        if (data != null) {
            int packetStartPosition = data[0];
            int value = data[1];
            int accumulator = data[2];
            // 累加数 10
            if (accumulator == TEN_TIMES) {
                mPacketStartPosition = packetStartPosition;
                mPacketLength = matchPacketLen;
                Log.d(TAG, "PacketStartPosition : " + packetStartPosition);
                Log.d(TAG, "PacketLen : " + matchPacketLen);
                Log.d(TAG, "accumulator : " + accumulator);
                return -1;
            }
            // 判断间隔是否相邻，是进行累加；不是，清空数据
            if (currentValue - value == 1) {
                data[1] = currentValue;
                data[2] += 1;
                sparseArray.put(relativePosition, data);
            } else {
                data[0] = counter;
                data[1] = currentValue;
                data[2] = 1;
                sparseArray.put(relativePosition, data);
            }
        } else {
            // PacketStartPosition 、间隔值、累加数
            data = new int[3];
            data[0] = counter;
            data[1] = currentValue;
            data[2] = 1;
            sparseArray.put(relativePosition, data);
        }

        return 0;
    }


    /**
     * 匹配指定 PID
     * 找到音视频文件
     */
    public int matchPid(String inputFilePath, int[] inputPid, String[] outputFilePath) {
        Log.d(TAG, " -- matchPid()");
        mInputFilePath = inputFilePath;

        // 如果 PacketLength PacketStartPosition 的值为异常，重新 matchPacketLength()
        if (mPacketLength == -1 || mPacketStartPosition == -1) {
            matchPacketLength(mInputFilePath);
        }

        try {
            File file = new File(mInputFilePath);
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
//            FileInputStream fis = new FileInputStream(mInputFilePath);

            SparseIntArray sparseIntArray = new SparseIntArray();
            List<FileOutputStream> fileOutputStreamList = new ArrayList<>();
            for (int i = 0; i < inputPid.length; i++) {
                FileOutputStream fos = new FileOutputStream(outputFilePath[i]);
                fileOutputStreamList.add(fos);
                sparseIntArray.put(inputPid[i], i);
            }

            // 跳到包的开始位置
            long lg = bis.skip(mPacketStartPosition);
            if (lg != mPacketStartPosition) {
                Log.e(TAG, "failed to skip " + mPacketStartPosition + "byteArray");
                return -1;
            }

            int err;
            do {
                // 中断查找
                if (mInterrupt) {
                    mInterrupt = false;
                    Log.e(TAG, "matchPidTableId() Interrupt !!!");
                    break;
                }

                byte[] buff = new byte[mPacketLength];
                err = bis.read(buff);
                if (err == mPacketLength) {
                    if (buff[0] == PACKET_HEADER_SYNC_BYTE) {
                        // 构建 packet 对象
                        Packet packet = new Packet(buff);

                        // 匹配 pid
                        int position = sparseIntArray.get(packet.getPid(), -1);
                        if (position != -1) {
                            // 写出文件
                            fileOutputStreamList.get(position).write(buff);
                        }

                    }
                }
            } while (err != -1);

            // 关闭文件
            for (FileOutputStream fos : fileOutputStreamList) {
                fos.close();
            }
            bis.close();

        } catch (IOException e) {
            Log.e(TAG, "Error IOException");
            e.printStackTrace();
        }

        return 0;
    }


    /**
     * 匹配 Section
     * 同时找出多个表的 Section
     */
    public int matchSection(String inputFilePath, int[][] searchArray) {
        Log.d(TAG, " ---------------------------------------------- ");
        Log.d(TAG, " -- matchSection()");
        long startTime = System.currentTimeMillis();

        mInputFilePath = inputFilePath;

        SparseIntArray sparseIntArray = new SparseIntArray();
        List<SectionManager> sectionManagerList = new ArrayList<>();

        for (int i = 0; i < searchArray.length; i++) {
            SectionManager sectionManager = new SectionManager();
            sectionManagerList.add(sectionManager);
            sparseIntArray.put(searchArray[i][0], i);
        }


        // 如果 PacketLength PacketStartPosition 的值为异常，重新 matchPacketLength()
        if (mPacketLength == -1 || mPacketStartPosition == -1) {
            matchPacketLength(mInputFilePath);
        }

        try {
            File file = new File(mInputFilePath);
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
//            FileInputStream bis = new FileInputStream(file);

            // 跳到包的开始位置
            long lg = bis.skip(mPacketStartPosition);
            if (lg != mPacketStartPosition) {
                Log.e(TAG, "failed to skip " + mPacketStartPosition + "byteArray");
                return -1;
            }

            int err;
            byte[] buff = new byte[mPacketLength * 50];
            byte[] onePacket = new byte[mPacketLength];
            while ((err = bis.read(buff)) != -1) {
                for (int i = 0; i < err / mPacketLength; i++) {
                    // 中断标志
                    if (mInterrupt) {
                        mInterrupt = false;
                        Log.e(TAG, "matchSection() mInterrupt !!!");
                        return -1;
                    }

                    // 字符串截取
                    System.arraycopy(buff, mPacketLength * i, onePacket, 0, mPacketLength);

                    if (onePacket[0] == PACKET_HEADER_SYNC_BYTE) {
                        // 构建 packet 对象
                        Packet packet = new Packet(onePacket);
                        // 判断传输错误
                        if (packet.getTransportErrorIndicator() == 1) {
                            Log.e(TAG, "Error transport_error_indicator : 1");
                            continue;
                        }

                        int position = sparseIntArray.get(packet.getPid(), -1);
                        if (position != -1) {
                            SectionManager sectionManager = sectionManagerList.get(position);
                            sectionManager.matchSection(packet, searchArray[position][1]);
                        }
                    }

                }
            }

            bis.close();

        } catch (IOException e) {
            Log.e(TAG, "Error IOException");
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        Log.e(TAG, "match Section time : " + (endTime - startTime) + " ms");


        // 解表
        for (int i = 0; i < searchArray.length; i++) {
            parseTable(sectionManagerList.get(i).getSectionList(), searchArray[i][0]);
        }
        long endTime2 = System.currentTimeMillis();
        Log.e(TAG, "parse Table time : " + (endTime2 - endTime) + " ms");

        return 0;
    }


    /**
     * 解表
     * 1）合成 PAT 表
     * 2）合成 SDT 表
     * 3）合成 PMT 表
     */
    private void parseTable(List<Section> list, int pid) {
        if (list == null) {
            Log.e(TAG, "Section List == null !!!");
            return;
        }
        switch (pid) {
            case PAT_PID:
                PatManager patManager = new PatManager();
                mPat = patManager.makePAT(list);
                break;

            case SDT_PID:
                SdtManager sdtManager = new SdtManager();
                mSdt = sdtManager.makeSDT(list);
                break;

            case EIT_PID:
                EitManager eitManager = new EitManager();
                mEit = eitManager.makeEit(list);
                break;

            default:
                if (list.get(0).getTableId() == PMT_TABLE_ID) {
                    PmtManager pmtManager = new PmtManager();
                    mPmt = pmtManager.makePMT(list);
                    mPmtList.add(mPmt);
                }
                break;
        }
    }

    /**
     * 合成节目列表：ProgramList
     */
    public int parseProgramList() {
        if (mPat == null || mSdt == null || mEit == null) {
            return -1;
        }
        ProgramManager programManager = new ProgramManager();
        mProgramList = programManager.makeProgramList(mPat, mSdt, mEit);
        return 0;
    }


    public void setInterrupt(boolean mInterrupt) {
        this.mInterrupt = mInterrupt;
    }

    public String getInputFilePath() {
        return mInputFilePath;
    }

    public void setInputFilePath(String mInputFilePath) {
        this.mInputFilePath = mInputFilePath;
    }

    public void setOutputFilePath(String mOutputFilePath) {
        this.mOutputFilePath = mOutputFilePath;
    }

    public int getPacketLength() {
        return mPacketLength;
    }

    public void setPacketLength(int mPacketLength) {
        this.mPacketLength = mPacketLength;
    }

    public int getPacketStartPosition() {
        return mPacketStartPosition;
    }

    public void setPacketStartPosition(int mPacketStartPosition) {
        this.mPacketStartPosition = mPacketStartPosition;
    }

    public Pat getPat() {
        return mPat;
    }

    public void setPat(Pat mPat) {
        this.mPat = mPat;
    }

    public Sdt getSdt() {
        return mSdt;
    }

    public void setSdt(Sdt mSdt) {
        this.mSdt = mSdt;
    }

    public Eit getEit() {
        return mEit;
    }

    public void setEit(Eit mEit) {
        this.mEit = mEit;
    }

    public List<Pmt> getPmtList() {
        return mPmtList;
    }

    public List<Program> getProgramList() {
        return mProgramList;
    }
}

