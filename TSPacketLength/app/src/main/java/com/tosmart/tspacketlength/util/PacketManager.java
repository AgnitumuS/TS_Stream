package com.tosmart.tspacketlength.util;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import static java.lang.Integer.toHexString;

/**
 * MainActivity
 *
 * @author ggz
 * @date 2018/3/17
 */

public class PacketManager {
    private static final String TAG = "PacketManager";
    private static final int CYCLE_TEN_TIMES = 10;
    private static final int PACKET_HEADER_SYNC_BYTE = 0x47;
    private static final int PACKET_LENGTH_188 = 188;
    private static final int PACKET_LENGTH_204 = 204;


    /**
     * 包头的开始位置
     */
    private int mPacketStartPosition = -1;
    /**
     * 包的长度
     */
    private int mPacketLength = -1;


    /**
     * 构造函数
     */
    public PacketManager() {
        super();
    }

    /**
     * 获取包的 长度 和 开始位置
     */
    public int getPacketLength(String filePath) {
        long startTime = System.currentTimeMillis();

        Log.d(TAG, filePath);
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);

            int tmp;
            while ((tmp = fis.read()) != -1) {
                // 记录包的开始位置
                mPacketStartPosition++;
                Log.d(TAG, "current position : " + mPacketStartPosition +
                        "   0x" + toHexString(tmp));

                // match to 0x47
                if (tmp == PACKET_HEADER_SYNC_BYTE) {
                    Log.d(TAG, "match to 0x" + toHexString(PACKET_HEADER_SYNC_BYTE));

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



    public int getPacketStartPosition() {
        return mPacketStartPosition;
    }


    /**
     * 获取指定 PID 的包
     */
    public int getSpecifiedPacket(int inputPID, String inputFilePath, String outputFilePath) {
        int targetPacketNum = 0;
        try {

            FileInputStream fis = new FileInputStream(inputFilePath);
            FileOutputStream fos = new FileOutputStream(outputFilePath);

            // 跳到包的开始位置
            long lg = fis.skip(mPacketStartPosition);
            if (lg != mPacketStartPosition) {
                Log.e(TAG, "failed to skip " + mPacketStartPosition + "bytes");
                return -1;
            }

            // 写入文件的内容
            StringBuilder sb = new StringBuilder();

            byte[] bytesArray = new byte[mPacketLength];

            int err;
            do {
                // 截取一个包
                err = fis.read(bytesArray);
                if (err == mPacketLength) {
                    // 解出 pid
                    int pid = ((bytesArray[1] & 0x1F) << 8 | bytesArray[2]) & 0x1FFF;

                    // 匹配 pid
                    if (pid == inputPID) {
                        Log.d(TAG, "match to " + inputPID);
                        targetPacketNum++;

                        for (int i = 0; i < bytesArray.length; i++) {
                            String byteToString = toHexString(bytesArray[i] & 0xFF);
                            sb.append(" ");
                            sb.append(byteToString);
                        }
                        sb.append("\n");
                    }
                }
            } while (err != -1);


            // 写出文件
            OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8");
            writer.append(sb.toString());
            writer.close();

            fos.close();
            fis.close();
        } catch (IOException e) {
            Log.e(TAG, "IOException : 打开文件失败");
            e.printStackTrace();
        }

        Log.d(TAG, "the number of the Packet's PID = 0x" + inputPID
                + " is " + targetPacketNum);
        return targetPacketNum;
    }

}

