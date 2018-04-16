package com.excellence.iptv.util;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;

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

                // matchData to 0x47
                if (tmp == PACKET_HEADER_SYNC_BYTE) {
                    Log.d(TAG, "matchData to 0x" + toHexString(PACKET_HEADER_SYNC_BYTE));

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
     * 单次匹配指定 PID 和 table_id
     */
    public int matchPidTableId(String inputFilePath, int inputPid, int inputTableId) {
        Log.d(TAG, " -- matchPidTableId()");
        mInputFilePath = inputFilePath;
        mInputPID = inputPid;
        mInputTableID = inputTableId;
        Log.d(TAG, "inputFilePath : " + mInputFilePath);
        Log.d(TAG, "outputFilePath : " + mOutputFilePath);

        mSectionManagerList.clear();
        SectionManager sectionManager = new SectionManager();
        mSectionManagerList.add(sectionManager);


        // 如果 PacketLength PacketStartPosition 的值为异常，重新 matchPacketLength()
        if (mPacketLength == -1 || mPacketStartPosition == -1) {
            matchPacketLength(mInputFilePath);
        }

        try {
            FileInputStream fis = new FileInputStream(mInputFilePath);
            FileOutputStream fos = new FileOutputStream(mOutputFilePath);

            // 跳到包的开始位置
            long lg = fis.skip(mPacketStartPosition);
            if (lg != mPacketStartPosition) {
                Log.e(TAG, "failed to skip " + mPacketStartPosition + "byteArray");
                return -1;
            }

            mPacketNum = 0;
            int err;
            do {
                // 中断查找
                if (mInterrupt) {
                    mInterrupt = false;
                    Log.e(TAG, "matchPidTableId() Interrupt !!!");
                    break;
                }

                byte[] buff = new byte[mPacketLength];
                err = fis.read(buff);
                if (err == mPacketLength) {
                    if (buff[0] == PACKET_HEADER_SYNC_BYTE) {
                        // 构建 packet 对象
                        Packet packet = new Packet(buff);

                        // 匹配 pid
                        if (packet.getPid() == mInputPID) {
                            mPacketNum++;

                            // 匹配 section
                            int result = mSectionManagerList.get(0).matchSection(packet, mInputTableID);

                            // 写出文件
                            fos.write(buff);
                        }
                    }
                }
            } while (err != -1);

            fos.close();
            fis.close();
        } catch (IOException e) {
            Log.e(TAG, "IOException : 打开文件失败");
            e.printStackTrace();
        }

        // 打印 section 结果
        mSectionManagerList.get(0).print();

        // 解表
        parseToTable(mSectionManagerList.get(0).getSectionList(), mInputPID);

        Log.d(TAG, " ---------------------------------------------- ");
        Log.d(TAG, "the number of the Packet's PID = 0x" + toHexString(mInputPID)
                + " is " + mPacketNum);
        Log.d(TAG, "success to write file to " + mOutputFilePath);

        return mPacketNum;
    }


    public int matchData(String inputFilePath, int[][] searchArray) {
        Log.d(TAG, " -- matchData()");

        mInputFilePath = inputFilePath;

        // 准备 SectionManager
        mSectionManagerList.clear();
        for (int i = 0; i < searchArray.length; i++) {
            SectionManager sectionManager = new SectionManager();
            mSectionManagerList.add(sectionManager);
        }

        // 如果 PacketLength PacketStartPosition 的值为异常，重新 matchPacketLength()
        if (mPacketLength == -1 || mPacketStartPosition == -1) {
            matchPacketLength(mInputFilePath);
        }

        try {
            BufferedInputStream fis = new BufferedInputStream(new FileInputStream(mInputFilePath));
//            FileInputStream fis = new FileInputStream(mInputFilePath);

            // 跳到包的开始位置
            long lg = fis.skip(mPacketStartPosition);
            if (lg != mPacketStartPosition) {
                Log.e(TAG, "failed to skip " + mPacketStartPosition + "byteArray");
                return -1;
            }

            int err;
            do {
                // 中断查找
                if (mInterrupt) {
                    mInterrupt = false;
                    Log.e(TAG, "matchData() mInterrupt !!!");
                    return -1;
                }

                byte[] buff = new byte[mPacketLength];
                err = fis.read(buff);
                if (err == mPacketLength) {
                    if (buff[0] == PACKET_HEADER_SYNC_BYTE) {
                        // 构建 packet 对象
                        Packet packet = new Packet(buff);

                        // 匹配 pid
                        for (int i = 0; i < searchArray.length; i++) {
                            if (packet.getPid() == searchArray[i][0]) {
                                // 匹配 section
                                SectionManager sectionManager = mSectionManagerList.get(i);
                                sectionManager.matchSection(packet, searchArray[i][1]);

                                break;
                            }
                        }
                    }
                }


            } while (err != -1);

            fis.close();

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
        Log.d(TAG, " -- parseToTable()");
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

