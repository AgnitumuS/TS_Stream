package com.excellence.iptv.thread;

import android.os.Environment;
import android.util.Log;

import com.excellence.iptv.bean.tables.Pat;
import com.excellence.iptv.bean.tables.Sdt;
import com.excellence.iptv.util.PacketManager;

import java.io.File;


/**
 * GetPatSdtThread
 *
 * @author ggz
 * @date 2018/3/20
 */

public class GetPatSdtThread extends Thread {
    private static final String TAG = "GetPatSdtThread";
    private static final String HISTORY_FOLDER_PATH =
            Environment.getExternalStorageDirectory().getPath() + "/ts_history/";
    private static final int PAT_PID = 0x0000;
    private static final int SDT_PID = 0x0011;

    private String mInputFilePath;
    private int mInputPID;
    private int mInputTableId;
    private PacketManager mPacketManager;
    private PacketManager mRunningManager;


    public GetPatSdtThread(String inputFilePath, int inputPID, int inputTableId,
                           PacketManager packetManager) {
        super();
        this.mInputFilePath = inputFilePath;
        this.mInputPID = inputPID;
        this.mInputTableId = inputTableId;
        this.mPacketManager = packetManager;
    }

    @Override
    public void run() {
        super.run();

        // 寻找历史文件
        File file = new File(mInputFilePath);
        String fileName = file.getName();

        boolean isFindHistoryFile = false;
        String historyFilePath = null;
        if (mInputPID == PAT_PID) {
            historyFilePath = findHistoryFile(fileName + "_pat");
        }
        if (mInputPID == SDT_PID) {
            historyFilePath = findHistoryFile(fileName + "_sdt");
        }
        if (historyFilePath != null) {
            isFindHistoryFile = true;
            mInputFilePath = historyFilePath;
        }

        // The Running Packet Manager
        mRunningManager = new PacketManager(mPacketManager.getHandler());


        // 设置历史文件存放位置（非必须）
        if (!isFindHistoryFile) {
            if (mInputPID == PAT_PID) {
                mRunningManager.setOutputFilePath(HISTORY_FOLDER_PATH + fileName + "_pat");
            }
            if (mInputPID == SDT_PID) {
                mRunningManager.setOutputFilePath(HISTORY_FOLDER_PATH + fileName + "_sdt");
            }
        }

        /*
        * 开始匹配数据
        * */
        int packetNum = mRunningManager.matchPidPacket(
                mInputFilePath,
                mInputPID,
                mInputTableId);
        if (packetNum != -1) {
            // 将结果传进 mPacketManager
            if (mInputPID == PAT_PID) {
                Pat pat = mRunningManager.getPat();
                if (pat != null) {
                    mPacketManager.setPat(pat);
                }
            }
            if (mInputPID == SDT_PID) {
                Sdt sdt = mRunningManager.getSdt();
                if (sdt != null) {
                    mPacketManager.setSdt(sdt);
                }
            }

        } else {
            Log.e(TAG, "Error !!!");
        }
    }


    private String findHistoryFile(String searchName) {
        String path;
        File file = new File(HISTORY_FOLDER_PATH);
        if (!file.exists()) {
            file.mkdir();
        }
        String[] fileList = file.list();
        if (fileList != null) {
            for (String str : fileList) {
                if (str.equals(searchName)) {
                    path = HISTORY_FOLDER_PATH + searchName;
                    return path;
                }
            }
        }
        return null;
    }

    public void over() {
        mRunningManager.setOver(true);
    }
}
