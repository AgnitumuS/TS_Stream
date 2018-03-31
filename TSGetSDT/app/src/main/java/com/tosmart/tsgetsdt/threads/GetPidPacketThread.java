package com.tosmart.tsgetsdt.threads;

import android.os.Handler;
import android.util.Log;

import com.tosmart.tsgetsdt.utils.PacketManager;


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

        int packetNum = mPacketManager.matchPidPacket(
                mPacketManager.getInputFilePath(),
                mPacketManager.getInputPID(),
                mPacketManager.getInputTableID());
        if (packetNum != -1) {
            Log.d(TAG, "succeed to get all Specified Packet : " + packetNum);
        } else {
            Log.e(TAG, "failed to get Specified Packet !!!");
        }
    }

    public void over() {
        mPacketManager.setOver(true);
        Log.e(TAG, "GetPidPacketThread is Over !!!");
    }
}
