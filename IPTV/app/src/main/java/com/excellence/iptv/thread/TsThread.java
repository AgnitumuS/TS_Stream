package com.excellence.iptv.thread;

import android.os.Environment;

import com.excellence.iptv.util.PacketManager;

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

    private String mInputFilePath;
    private int[][] mSearchArray;
    private PacketManager mPacketManager;

    public TsThread(String inputFilePath, int[][] searchArray, PacketManager packetManager) {
        super();
        this.mInputFilePath = inputFilePath;
        this.mSearchArray = searchArray;
        this.mPacketManager = packetManager;
    }

    @Override
    public void run() {
        super.run();

        mPacketManager.matchArray(mInputFilePath, mSearchArray);
    }

    /**
     * 中断查找
     */
    public void over() {
        mPacketManager.setOver(true);
    }
}
