package com.excellence.iptv.util;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.excellence.iptv.SelectFileActivity;
import com.excellence.iptv.bean.Packet;
import com.excellence.iptv.bean.Program;
import com.excellence.iptv.bean.Section;
import com.excellence.iptv.bean.tables.Pat;
import com.excellence.iptv.bean.tables.Pmt;
import com.excellence.iptv.bean.tables.Sdt;

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
    private static final int PMT_TABLE_ID = 0x02;
    private static final String OUTPUT_FILE_PATH =
            Environment.getExternalStorageDirectory().getPath() + "/ts_history/resultFile";


    private boolean isOver = false;

    private String mInputFilePath = null;
    private String mOutputFilePath = OUTPUT_FILE_PATH;
    private Handler mHandler;

    private int mPacketLength = -1;
    private int mPacketStartPosition = -1;

    private int mInputPID = -1;
    private int mInputTableID = -1;
    private int mPacketNum = -1;

    private List<SectionManager> mSectionManagerList = new ArrayList<>();

    private Pat mPat = null;
    private Sdt mSdt = null;
    private Pmt mPmt = null;

    private List<Pmt> mPmtList = new ArrayList<>();

    private List<Program> mProgramList = null;


    /**
     * 构造函数
     */
    public PacketManager(Handler handler) {
        super();
        this.mHandler = handler;
    }

    public PacketManager(String inputFilePath) {
        super();
        this.mInputFilePath = inputFilePath;
    }

    public PacketManager(String inputFilePath, int inputPID, int inputTableId, Handler handler) {
        super();
        this.mInputFilePath = inputFilePath;
        this.mInputPID = inputPID;
        this.mInputTableID = inputTableId;
        this.mHandler = handler;
    }


    /**
     * 匹配 Packet Length 和 Packet Start Position
     */
    public int matchPacketLength(String inputFilePath) {
        Log.d(TAG, " -- matchPacketLength()");
        long startTime = System.currentTimeMillis();

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

                // matchArray to 0x47
                if (tmp == PACKET_HEADER_SYNC_BYTE) {
                    Log.d(TAG, "matchArray to 0x" + toHexString(PACKET_HEADER_SYNC_BYTE));

                    /*
                    循环 10 次跳 188
                    匹配 isFinish == true，结束
                    否则 isFinish == false，检测 204
                    */
                    boolean isFinish = true;
                    for (int i = 0; i < CYCLE_TEN_TIMES; i++) {
                        // seek 188 bytes
                        long lg = fis.skip(PACKET_LENGTH_188 - 1);
                        if (lg != PACKET_LENGTH_188 - 1) {
                            // 如果长度不够，返回失败结果
                            Log.e(TAG, "failed to skip " + (PACKET_LENGTH_188 - 1) + "bytes");
                            return -1;
                        }
                        tmp = fis.read();
                        Log.d(TAG, "skip " +
                                PACKET_LENGTH_188 + " * " + (i + 1) +
                                " bytes :  0x" + toHexString(tmp));

                        // determine the length of the packet is not the 188
                        if (tmp != PACKET_HEADER_SYNC_BYTE) {
                            // seek back
                            lg = fis.skip((-1) * PACKET_LENGTH_188 * (i + 1));
                            if (lg != (-1) * PACKET_LENGTH_188 * (i + 1)) {
                                Log.e(TAG, "failed to skip " +
                                        "(-1) * " + PACKET_LENGTH_188 + " * " + (i + 1) +
                                        " bytes");
                                return -1;
                            }

                            isFinish = false;
                            Log.d(TAG, "skip " +
                                    "(-1) * " + PACKET_LENGTH_188 + " * " + (i + 1) +
                                    " bytes");
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
                        // seek 204 bytes
                        long lg = fis.skip(PACKET_LENGTH_204 - 1);
                        if (lg != PACKET_LENGTH_204 - 1) {
                            // 如果长度不够，返回失败结果
                            Log.e(TAG, "failed to skip " + (PACKET_LENGTH_204 - 1) + "bytes");
                            return -1;
                        }
                        tmp = fis.read();
                        Log.d(TAG, "skip " +
                                PACKET_LENGTH_204 + " * " + (i + 1) +
                                " bytes :  0x" + toHexString(tmp));

                        // determine the length of the packet is not the 204
                        if (tmp != PACKET_HEADER_SYNC_BYTE) {
                            // seek back
                            lg = fis.skip((-1) * PACKET_LENGTH_204 * (i + 1));
                            if (lg != (-1) * PACKET_LENGTH_204 * (i + 1)) {
                                Log.e(TAG, "failed to skip " +
                                        "(-1) * " + PACKET_LENGTH_204 + " * " + (i + 1) +
                                        " bytes");
                                return -1;
                            }

                            isFinish = false;
                            Log.d(TAG, "skip " +
                                    "(-1) * " + PACKET_LENGTH_204 + " * " + (i + 1) +
                                    " bytes");
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

        long endTime = System.currentTimeMillis();
        Log.d(TAG, "当前方法耗时： " + (endTime - startTime) + " ms");
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


        // 获取 PacketLength 和 PacketStartPosition
        matchPacketLength(mInputFilePath);

        try {
            FileInputStream fis = new FileInputStream(mInputFilePath);
            FileOutputStream fos = new FileOutputStream(mOutputFilePath);

            // 跳到包的开始位置
            long lg = fis.skip(mPacketStartPosition);
            if (lg != mPacketStartPosition) {
                Log.e(TAG, "failed to skip " + mPacketStartPosition + "bytes");
                return -1;
            }

            mPacketNum = 0;
            int err;
            do {
                // 结束查找
                if (isOver) {
                    isOver = false;
                    Log.e(TAG, "isOver !!!");
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


    public int matchArray(String inputFilePath, int[][] searchArray) {
        Log.d(TAG, " -- matchArray()");
        mInputFilePath = inputFilePath;
        Log.d(TAG, "inputFilePath : " + mInputFilePath);

        // 根据数组大小来 new SectionManager
        mSectionManagerList.clear();
        for (int i = 0; i < searchArray.length; i++) {
            SectionManager sectionManager = new SectionManager();
            mSectionManagerList.add(sectionManager);
        }

        // 获取 PacketLength 和 PacketStartPosition
        matchPacketLength(mInputFilePath);

        try {
            FileInputStream fis = new FileInputStream(mInputFilePath);

            // 跳到包的开始位置
            long lg = fis.skip(mPacketStartPosition);
            if (lg != mPacketStartPosition) {
                Log.e(TAG, "failed to skip " + mPacketStartPosition + "bytes");
                return -1;
            }

            int err;
            do {
                // 结束查找
                if (isOver) {
                    isOver = false;
                    Log.e(TAG, "isOver !!!");
                    return -2;
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
                                // 找第一版
                                if (!sectionManager.getIsFinishOne()) {
                                    sectionManager.matchSection(packet, searchArray[i][1]);
                                }
                                sectionManager.matchSection(packet, searchArray[i][1]);
                                break;
                            }
                        }
                    }
                }

                // 全找第一版
                boolean isFinish = true;
                for (SectionManager sectionManager : mSectionManagerList) {
                    if (!sectionManager.getIsFinishOne()) {
                        isFinish = false;
                    }
                }
                if (isFinish) {
                    break;
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
                mHandler.sendEmptyMessage(SelectFileActivity.GET_PAT);
                break;

            case SDT_PID:
                SdtManager sdtManager = new SdtManager();
                mSdt = sdtManager.makeSDT(list);
                mHandler.sendEmptyMessage(SelectFileActivity.GET_SDT);
                break;

            default:
                if (list.get(0).getTableId() == PMT_TABLE_ID) {
                    PmtManager pmtManager = new PmtManager();
                    mPmt = pmtManager.makePMT(list);
                    mPmtList.add(mPmt);
                    if (mPmtList.size() == mPat.getPatProgramList().size()) {
                        mHandler.sendEmptyMessage(SelectFileActivity.GET_ALL_PMT);
                    }
                }
                break;
        }
    }

    /**
     * 合成节目列表
     */
    public void parseToProgramList() {
        if (mPat != null && mSdt != null) {
            ProgramManager programManager = new ProgramManager();
            mProgramList = programManager.makeProgramList(mPat, mSdt);
            mHandler.sendEmptyMessage(SelectFileActivity.GET_PROGRAM_LIST);
        }
    }


    public void setOver(boolean over) {
        isOver = over;
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

    public int getPacketStartPosition() {
        return mPacketStartPosition;
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

    public void setHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    public Handler getHandler() {
        return mHandler;
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

