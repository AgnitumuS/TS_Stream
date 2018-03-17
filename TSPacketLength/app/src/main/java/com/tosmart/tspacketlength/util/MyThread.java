package com.tosmart.tspacketlength.util;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.tosmart.tspacketlength.MainActivity;

/**
 * MyThread
 *
 * @author ggz
 * @date 2018/3/15
 */

public class MyThread extends Thread {
    private static final String TAG = "MyThread";
    public static final String PACKET_LENGTH_KEY = "packetLen";
    public static final String PACKET_START_POSITION_KEY = "packetStartPosition";
    public static final String PACKET_NUMBER_KEY = "packetNum";

    private PacketManager mPacketManager;

    private String mFilePath;

    private int mInputPID;

    private Handler mHandler;


    public MyThread(String filePath, int inputPID, Handler handler) {
        super();
        this.mFilePath = filePath;
        this.mInputPID = inputPID;
        this.mHandler = handler;
    }


    @Override
    public void run() {
        super.run();

        // Packet 包的管理
        mPacketManager = new PacketManager();

        // Packet 包的开始位置
        int packetStartPosition = 0;
        // Packet 包长
        int packetLen;

        // 获取包的 长度 和 开始位置
        packetLen = mPacketManager.getPacketLength(mFilePath);
        if (packetLen != -1) {
            Log.d(TAG, "succeed to get Packet Length : "
                    + packetLen);
            packetStartPosition = mPacketManager.getPacketStartPosition();
            Log.d(TAG, "succeed to get Packet Start Position : "
                    + packetStartPosition);
        } else {
            Log.e(TAG, "failed to get Packet Length");
        }

        // 更新 UI
        Message msg = Message.obtain();
        msg.what = MainActivity.REFRESH_UI_PACKET_LENGTH;
        Bundle data = new Bundle();
        data.putInt(PACKET_LENGTH_KEY, packetLen);
        data.putInt(PACKET_START_POSITION_KEY, packetStartPosition);
        msg.setData(data);
        mHandler.sendMessage(msg);

        // 获取指定 PID 的包
        int packetNum = mPacketManager.getSpecifiedPacket(
                mInputPID,
                mFilePath,
                "/sdcard/ts/PID.txt");
        Log.d(TAG, "succeed to get all Specified Packet : " + packetNum);


        // 更新 UI
        msg = Message.obtain();
        msg.what = MainActivity.REFRESH_UI_PACKET_NUM;
        data.putInt(PACKET_NUMBER_KEY, packetNum);
        msg.setData(data);
        mHandler.sendMessage(msg);
    }

}
