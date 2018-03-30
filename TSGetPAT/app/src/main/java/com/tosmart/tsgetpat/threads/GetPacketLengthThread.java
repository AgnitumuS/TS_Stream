package com.tosmart.tsgetpat.threads;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.tosmart.tsgetpat.FileListActivity;
import com.tosmart.tsgetpat.utils.PacketManager;


/**
 * GetPidPacketThread
 *
 * @author ggz
 * @date 2018/3/20
 */

public class GetPacketLengthThread extends Thread {
    private static final String TAG = "GetPacketLengthThread";

    private PacketManager mPacketManager;
    private Handler mHandler;


    public GetPacketLengthThread(PacketManager packetManager, Handler handler) {
        super();
        this.mPacketManager = packetManager;
        this.mHandler = handler;
    }


    @Override
    public void run() {
        super.run();

        int packetLen = -1;
        int packetStartPosition = -1;

        // 获取包的 长度 和 开始位置
        packetLen = mPacketManager.getPacketLength();
        if (packetLen == -1) {
            packetLen = mPacketManager.matchPacketLength(mPacketManager.getInputFilePath());
            packetStartPosition = mPacketManager.getPacketStartPosition();

//            sendMessage(packetLen, packetStartPosition);
        } else {
            packetStartPosition = mPacketManager.getPacketStartPosition();

//            sendMessage(packetLen, packetStartPosition);
        }
    }

//    private void sendMessage(int packetLen, int packetStartPosition) {
//        Log.d(TAG, "get Packet Length : " + packetLen);
//        Log.d(TAG, "get Packet Start Position : " + packetStartPosition);
//        // 更新 UI
//        Message msg = Message.obtain();
//        msg.what = FileListActivity.REFRESH_UI_PACKET_LENGTH;
//        Bundle data = new Bundle();
//        data.putInt(PACKET_LENGTH_KEY, packetLen);
//        data.putInt(PACKET_START_POSITION_KEY, packetStartPosition);
//        msg.setData(data);
//        mHandler.sendMessage(msg);
//    }
}
