package com.excellence.iptv.util;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.excellence.iptv.SelectFileActivity;
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

import static java.lang.Integer.toHexString;

/**
 * PacketManager
 *
 * @author ggz
 * @date 2018/3/22
 */

public class PacketManager {
    private static final String TAG = "PacketManager";
    private static final int CYCLE_TEN_TIMES = 10;
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

    private int mInputPID = -1;
    private int mInputTableID = -1;
    private int mPacketNum = -1;

    private List<SectionManager> mSectionManagerList = new ArrayList<>();

    private Pat mPat = null;
    private Sdt mSdt = null;
    private Eit mEit = null;
    private Pmt mPmt = null;

    private List<Pmt> mPmtList = new ArrayList<>();

    private List<Program> mProgramList = null;


    /**
     * 构造函数
     */
    public PacketManager() {
        super();
    }

    public PacketManager(String inputFilePath) {
        super();
        this.mInputFilePath = inputFilePath;
    }

    public PacketManager(String inputFilePath, int inputPID, int inputTableId) {
        super();
        this.mInputFilePath = inputFilePath;
        this.mInputPID = inputPID;
        this.mInputTableID = inputTableId;

    }


    /**
     * 匹配 Packet Length 和 Packet Start Position
     */
    public int matchPacketLength(String inputFilePath) {
        Log.d(TAG, " -- matchPacketLength()");

        mInputFilePath = inputFilePath;

        mPacketLength = -1;
        mPacketStartPosition = -1;

        try {
            File file = new File(mInputFilePath);
            FileInputStream fis = new FileInputStream(file);

            int tmp;
            while ((tmp = fis.read()) != -1) {
                // 记录包的开始位置
                mPacketStartPosition++;
                Log.d(TAG, "current position : " + mPacketStartPosition +
                        "   0x" + toHexString(tmp));

                // matchSection to 0x47
                if (tmp == PACKET_HEADER_SYNC_BYTE) {
                    Log.d(TAG, "matchSection to 0x" + toHexString(PACKET_HEADER_SYNC_BYTE));

                    /*
                    循环 10 次跳 188
                    匹配 isFinish == true，结束
                    否则 isFinish == false，检测 204
                    */
                    boolean isFinish = true;
                    for (int i = 0; i < CYCLE_TEN_TIMES; i++) {
                        // seek 188 byteArray
                        long lg = fis.skip(PACKET_LENGTH_188 - 1);
                        if (lg != PACKET_LENGTH_188 - 1) {
                            // 如果长度不够，返回失败结果
                            Log.e(TAG, "failed to skip " + (PACKET_LENGTH_188 - 1) + "byteArray");
                            return -1;
                        }
                        tmp = fis.read();
                        Log.d(TAG, "skip " +
                                PACKET_LENGTH_188 + " * " + (i + 1) +
                                " byteArray :  0x" + toHexString(tmp));

                        // determine the length of the packet is not the 188
                        if (tmp != PACKET_HEADER_SYNC_BYTE) {
                            // seek back
                            lg = fis.skip((-1) * PACKET_LENGTH_188 * (i + 1));
                            if (lg != (-1) * PACKET_LENGTH_188 * (i + 1)) {
                                Log.e(TAG, "failed to skip " +
                                        "(-1) * " + PACKET_LENGTH_188 + " * " + (i + 1) +
                                        " byteArray");
                                return -1;
                            }

                            isFinish = false;
                            Log.d(TAG, "skip " +
                                    "(-1) * " + PACKET_LENGTH_188 + " * " + (i + 1) +
                                    " byteArray");
                            // 跳出 10 次检测
                            break;
                        }

                    }
                    if (isFinish) {
                        Log.d(TAG, "isFinish -- mPacketLength = " + PACKET_LENGTH_188);
                        mPacketLength = PACKET_LENGTH_188;
                        // 跳出 while ，结束检测
                        break;
                    }

                    /*
                    循环 10 次跳 204
                    匹配 isFinish == true，结束
                    否则 isFinish == false，继续检测下一位
                    */
                    isFinish = true;
                    for (int i = 0; i < CYCLE_TEN_TIMES; i++) {
                        // seek 204 byteArray
                        long lg = fis.skip(PACKET_LENGTH_204 - 1);
                        if (lg != PACKET_LENGTH_204 - 1) {
                            // 如果长度不够，返回失败结果
                            Log.e(TAG, "failed to skip " + (PACKET_LENGTH_204 - 1) + "byteArray");
                            return -1;
                        }
                        tmp = fis.read();
                        Log.d(TAG, "skip " +
                                PACKET_LENGTH_204 + " * " + (i + 1) +
                                " byteArray :  0x" + toHexString(tmp));

                        // determine the length of the packet is not the 204
                        if (tmp != PACKET_HEADER_SYNC_BYTE) {
                            // seek back
                            lg = fis.skip((-1) * PACKET_LENGTH_204 * (i + 1));
                            if (lg != (-1) * PACKET_LENGTH_204 * (i + 1)) {
                                Log.e(TAG, "failed to skip " +
                                        "(-1) * " + PACKET_LENGTH_204 + " * " + (i + 1) +
                                        " byteArray");
                                return -1;
                            }

                            isFinish = false;
                            Log.d(TAG, "skip " +
                                    "(-1) * " + PACKET_LENGTH_204 + " * " + (i + 1) +
                                    " byteArray");
                            // 跳出 10 次检测
                            break;
                        }
                    }
                    if (isFinish) {
                        Log.d(TAG, "isFinish -- mPacketLength = " + PACKET_LENGTH_204);
                        mPacketLength = PACKET_LENGTH_204;
                        // 跳出 while ，结束检测
                        break;
                    }
                }

            }

            fis.close();

        } catch (IOException e) {
            Log.e(TAG, "IOException : 打开文件失败");
            e.printStackTrace();
        }

        return mPacketLength;
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
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(mInputFilePath));
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
            Log.e(TAG, "IOException : 打开文件失败");
            e.printStackTrace();
        }

        return 0;
    }


    public int matchSection(String inputFilePath, int[][] searchArray) {
        Log.d(TAG, " -- matchSection()");

        mInputFilePath = inputFilePath;

        // 准备 SectionManager
        SparseIntArray sparseIntArray = new SparseIntArray();
        mSectionManagerList.clear();
        for (int i = 0; i < searchArray.length; i++) {
            SectionManager sectionManager = new SectionManager();
            mSectionManagerList.add(sectionManager);
            sparseIntArray.put(searchArray[i][0], i);
        }


        // 如果 PacketLength PacketStartPosition 的值为异常，重新 matchPacketLength()
        if (mPacketLength == -1 || mPacketStartPosition == -1) {
            matchPacketLength(mInputFilePath);
        }

        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(mInputFilePath));
//            FileInputStream fis = new FileInputStream(mInputFilePath);

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
                    Log.e(TAG, "matchSection() mInterrupt !!!");
                    return -1;
                }

                byte[] buff = new byte[mPacketLength];
                err = bis.read(buff);
                if (err == mPacketLength) {
                    if (buff[0] == PACKET_HEADER_SYNC_BYTE) {
                        // 构建 packet 对象
                        Packet packet = new Packet(buff);

                        int position = sparseIntArray.get(packet.getPid(), -1);
                        if (position != -1) {
                            SectionManager sectionManager = mSectionManagerList.get(position);
                            sectionManager.matchSection(packet, searchArray[position][1]);
                        }
                    }
                }


            } while (err != -1);

            bis.close();

        } catch (IOException e) {
            Log.e(TAG, "IOException : 打开文件失败");
            e.printStackTrace();
        }

        // 解表
        for (int i = 0; i < searchArray.length; i++) {
            parseToTable(mSectionManagerList.get(i).getSectionList(), searchArray[i][0]);
        }

        return 0;
    }


    /**
     * 解表
     * 1）合成 PAT 表
     * 2）合成 SDT 表
     * 3）合成 PMT 表
     */
    private void parseToTable(List<Section> list, int pid) {
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
    public int parseToProgramList() {
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

    public int getInputPID() {
        return mInputPID;
    }

    public void setInputPID(int mInputPID) {
        this.mInputPID = mInputPID;
    }

    public int getInputTableID() {
        return mInputTableID;
    }

    public void setInputTableID(int mInputTableID) {
        this.mInputTableID = mInputTableID;
    }

    public void setOutputFilePath(String mOutputFilePath) {
        this.mOutputFilePath = mOutputFilePath;
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

    public Pmt getPmt() {
        return mPmt;
    }

    public void setPmt(Pmt mPmt) {
        this.mPmt = mPmt;
    }

    public List<Pmt> getPmtList() {
        return mPmtList;
    }

    public List<Program> getProgramList() {
        return mProgramList;
    }
}

