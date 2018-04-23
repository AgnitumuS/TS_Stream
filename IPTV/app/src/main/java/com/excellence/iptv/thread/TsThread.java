package com.excellence.iptv.thread;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.excellence.iptv.bean.Ts;
import com.excellence.iptv.bean.tables.Eit;
import com.excellence.iptv.bean.tables.Pat;
import com.excellence.iptv.bean.tables.PatProgram;
import com.excellence.iptv.bean.tables.Pmt;
import com.excellence.iptv.bean.tables.Sdt;
import com.excellence.iptv.util.PacketManager;

import java.util.List;

import static com.excellence.iptv.SelectFileActivity.GET_ALL_PMT;
import static com.excellence.iptv.SelectFileActivity.GET_LENGTH_AND_START;
import static com.excellence.iptv.SelectFileActivity.GET_PAT_SDT_EIT;
import static com.excellence.iptv.SelectFileActivity.GET_PROGRAM_LIST;
import static com.excellence.iptv.SelectFileActivity.TS_THREAD_IS_OVER;

/**
 * TsThread
 *
 * @author ggz
 * @date 2018/4/10
 */

public class TsThread extends Thread {
    private static final String TAG = "TsThread";
    private static final String HISTORY_FOLDER_PATH =
            Environment.getExternalStorageDirectory().getPath() + "/ts_history/";
    private static final int PAT_PID = 0x0000;
    private static final int PAT_TABLE_ID = 0x00;
    private static final int SDT_PID = 0x0011;
    private static final int SDT_TABLE_ID = 0x42;
    private static final int EIT_PID = 0x0012;
    private static final int EIT_TABLE_ID = 0x4e;
    private static final int PMT_TABLE_ID = 0x02;

    private String mInputFilePath;
    private Handler mHandler;
    private Ts mTs;

    private PacketManager mPacketManager;

    public TsThread(String inputFilePath, Handler handler, Ts ts) {
        super();
        this.mInputFilePath = inputFilePath;
        this.mHandler = handler;
        this.mTs = ts;
        this.mTs.setFilePath(inputFilePath);
    }

    @Override
    public void run() {
        super.run();
        //记录开始时间
        long startTime = System.currentTimeMillis();

        mPacketManager = new PacketManager();
        // 重置已获取的数据
        int packetLength = mTs.getPacketLength();
        int packetStartPosition = mTs.getPacketStartPosition();
        if (packetLength != -1) {
            mPacketManager.setPacketLength(packetLength);
            mPacketManager.setPacketStartPosition(packetStartPosition);
        }
        Pat pat = mTs.getPat();
        if (pat != null) {
            mPacketManager.setPat(pat);
        }
        Sdt sdt = mTs.getSdt();
        if (sdt != null) {
            mPacketManager.setSdt(sdt);
        }
        Eit eit = mTs.getEit();
        if (eit != null) {
            mPacketManager.setEit(eit);
        }
        List<Pmt> pmtList = mTs.getPmtList();
        if (pmtList.size() != 0) {
            // 已获取了全部数据
            mHandler.sendEmptyMessage(GET_ALL_PMT);
            return;
        }

        // 解 PacketLength 、 PacketStartPosition
        if (mPacketManager.getPacketLength() == -1 || mPacketManager.getPacketStartPosition() == -1) {
            int err = mPacketManager.matchPacketLength(mInputFilePath);
            if (err == -1) {
                Log.e(TAG, "Failed to get PacketLength and PacketStartPosition");
                mHandler.sendEmptyMessage(TS_THREAD_IS_OVER);
                return;
            } else {
                // 保存 PacketLength 、 PacketStartPosition
                mTs.setPacketLength(mPacketManager.getPacketLength());
                mTs.setPacketStartPosition(mPacketManager.getPacketStartPosition());
                mHandler.sendEmptyMessage(GET_LENGTH_AND_START);
            }
        }
        // 解 PAT、SDT、EIT
        if (mPacketManager.getPat() == null || mPacketManager.getSdt() == null
                || mPacketManager.getEit() == null) {
            int[][] searchArray = new int[3][2];
            searchArray[0][0] = PAT_PID;
            searchArray[0][1] = PAT_TABLE_ID;
            searchArray[1][0] = SDT_PID;
            searchArray[1][1] = SDT_TABLE_ID;
            searchArray[2][0] = EIT_PID;
            searchArray[2][1] = EIT_TABLE_ID;
            // 开始匹配
            int err = mPacketManager.matchSection(mInputFilePath, searchArray);
            if (err == -1) {
                Log.e(TAG, "Failed to get PAT SDT EIT");
                mHandler.sendEmptyMessage(TS_THREAD_IS_OVER);
                return;
            } else {
                // 保存 PAT、SDT、EIT
                mTs.setPat(mPacketManager.getPat());
                mTs.setSdt(mPacketManager.getSdt());
                mTs.setEit(mPacketManager.getEit());
                mHandler.sendEmptyMessage(GET_PAT_SDT_EIT);

                // 合成为节目列表 ProgramList
                int error = mPacketManager.parseProgramList();
                if (error == -1) {
                    Log.e(TAG, "Failed to get ProgramList");
                    mHandler.sendEmptyMessage(TS_THREAD_IS_OVER);
                    return;
                } else {
                    // 保存 ProgramList
                    mTs.setProgramList(mPacketManager.getProgramList());
                    mHandler.sendEmptyMessage(GET_PROGRAM_LIST);
                }
            }
        }
        // 解 all PMT
        if (mPacketManager.getPmtList().size() == 0) {
            List<PatProgram> patProgramList = mPacketManager.getPat().getPatProgramList();
            int[][] searchArray = new int[patProgramList.size()][2];
            for (int i = 0; i < patProgramList.size(); i++) {
                searchArray[i][0] = patProgramList.get(i).getProgramMapPid();
                searchArray[i][1] = PMT_TABLE_ID;
            }
            int err = mPacketManager.matchSection(mInputFilePath, searchArray);
            if (err == -1) {
                Log.e(TAG, "Failed to get all PMT");
                mHandler.sendEmptyMessage(TS_THREAD_IS_OVER);
                return;
            } else {
                // 保存 PMT List
                mTs.setPmtList(mPacketManager.getPmtList());
                mHandler.sendEmptyMessage(GET_ALL_PMT);
            }
        }

        //记录结束时间
        long endTime = System.currentTimeMillis();
        Log.e(TAG, " time : " + (endTime - startTime) + " ms");
        mHandler.sendEmptyMessage(TS_THREAD_IS_OVER);
    }


    /**
     * 中断查找
     */
    public void setOver() {
        mPacketManager.setInterrupt(true);
    }
}
