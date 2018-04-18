package com.excellence.iptv.thread;

import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.util.List;

import static com.excellence.iptv.SelectFileActivity.GET_FILE;

/**
 * TsThread
 *
 * @author ggz
 * @date 2018/4/10
 */

public class TraverseFileThread extends Thread {

    private String mInputFilePath;
    private Handler mHandler;
    private List<String> mFilePathList;
    private List<String> mFileNameList;

    private boolean isOver = false;

    public TraverseFileThread(String inputFilePath, Handler handler,
                              List<String> filePathList, List<String> fileNameList) {
        super();
        this.mInputFilePath = inputFilePath;
        this.mHandler = handler;
        this.mFilePathList = filePathList;
        this.mFileNameList = fileNameList;
    }

    @Override
    public void run() {
        super.run();

        mFilePathList.clear();
        mFileNameList.clear();
        traverseTsFile(mInputFilePath);

        mHandler.sendEmptyMessage(GET_FILE);

    }

    private void traverseTsFile(String path) {
        if (isOver) {
            return;
        }

        File dir = new File(path);
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                // 递归
                traverseTsFile(files[i].getAbsolutePath());
            } else {
                String filePath = files[i].getAbsolutePath();
                String fileName = files[i].getName();
                mFilePathList.add(filePath);
                mFileNameList.add(fileName);
                //判断后缀
//                int j = fileName.lastIndexOf(".");
//                String suffix = fileName.substring(j + 1);
//                if (suffix.equalsIgnoreCase("ts")) {
//                    mFileNameList.add(fileName);
//                    mFilePathList.add(files[i].getAbsolutePath());
//                }
            }
        }
    }

    public void setOver() {
        isOver = true;
    }
}
