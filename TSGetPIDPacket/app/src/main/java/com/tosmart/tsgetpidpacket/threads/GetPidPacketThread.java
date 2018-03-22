package com.tosmart.tsgetpidpacket.threads;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.tosmart.tsgetpidpacket.MainActivity;
import com.tosmart.tsgetpidpacket.utils.PacketManager;

import static com.tosmart.tsgetpidpacket.MainActivity.PACKET_LENGTH_KEY;
import static com.tosmart.tsgetpidpacket.MainActivity.PACKET_NUMBER_KEY;
import static com.tosmart.tsgetpidpacket.MainActivity.PACKET_START_POSITION_KEY;


/**
 * GetPidPacketThread
 *
 * @author ggz
 * @date 2018/3/20
 */

public class GetPidPacketThread extends Thread {
    private static final String TAG = "GetPidPacketThread";

    private PacketManager mPacketManager;
    private Handler mHandler;


    public GetPidPacketThread(PacketManager packetManager, Handler handler) {
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
            // 如果没有，重新获取
            packetLen = mPacketManager.matchPacketLength(mPacketManager.getInputFilePath());
            packetStartPosition = mPacketManager.getPacketStartPosition();

            sendPacketLengthMessage(packetLen, packetStartPosition);
        } else {
            packetStartPosition = mPacketManager.getPacketStartPosition();

            sendPacketLengthMessage(packetLen, packetStartPosition);
        }


        // 获取指定 PID 的包
        int packetNum = mPacketManager.matchPidPacket(
                mPacketManager.getInputFilePath(),
                mPacketManager.getInputPID(),
                mPacketManager.getInputTableID());
        if (packetNum != -1) {
            Log.d(TAG, "succeed to get all Specified Packet : " + packetNum);

            sendPacketNumMessage(packetNum);
        } else {
            Log.e(TAG, "failed to get Specified Packet !!!");
        }
    }

    private void sendPacketLengthMessage(int packetLen, int packetStartPosition) {
        Message msg = Message.obtain();
        msg.what = MainActivity.REFRESH_UI_PACKET_LENGTH;
        Bundle data = new Bundle();
        data.putInt(PACKET_LENGTH_KEY, packetLen);
        data.putInt(PACKET_START_POSITION_KEY, packetStartPosition);
        msg.setData(data);
        mHandler.sendMessage(msg);
    }


    private void sendPacketNumMessage(int packetNum) {
        Message msg = Message.obtain();
        msg.what = MainActivity.REFRESH_UI_PACKET_NUMBER;
        Bundle data = new Bundle();
        data.putInt(PACKET_NUMBER_KEY, packetNum);
        msg.setData(data);
        mHandler.sendMessage(msg);
    }
}
