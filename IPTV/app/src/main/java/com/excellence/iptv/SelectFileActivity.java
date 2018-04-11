package com.excellence.iptv;

import android.Manifest;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.excellence.iptv.adapter.FileListAdapter;
import com.excellence.iptv.bean.Ts;
import com.excellence.iptv.thread.TsThread;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * SelectFileActivity
 *
 * @author ggz
 * @date 2018/4/2
 */

public class SelectFileActivity extends AppCompatActivity {
    private static final String TAG = "SelectFileActivity";
    private static final int WRITE_EXTERNAL_PERMISSION = 1;
    private static final String TS_FOLDER_PATH =
            Environment.getExternalStorageDirectory().getPath() + "/ts/";

    public static final int GET_PAT = 0;
    public static final int GET_SDT = 1;
    public static final int GET_PROGRAM_LIST = 2;
    public static final int GET_ALL_PMT = 3;
    public static final String KEY_TS_DATA = "TsData";

    private SmartRefreshLayout mRefreshLayout;
    private FileListAdapter mFileListAdapter;
    private PopupWindow mPopupWindow;

    private List<String> mFileNameList = new ArrayList<>();
    private List<String> mFilePathList = new ArrayList<>();

    private String mInputFilePath;
    private MyHandler mHandler = new MyHandler(this);
    private Ts mTs = new Ts();
    private TsThread mTsThread;

    private long mExitTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_file_activity);

        // 判断 Android 版本是否大于 23 （Android 6.0）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Request Read And Write Permission
            requestPermission();
        }

        // 遍历 ts 文件夹
        mFileNameList.clear();
        mFilePathList.clear();
        traverseTsFile(TS_FOLDER_PATH);

        // 显示文件列表
        initRecyclerView();
        initSmartRefreshLayout();
    }

    /**
     * 遍历 TS 文件夹
     */
    private void traverseTsFile(String path) {
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
                String fileName = files[i].getName();
                mFileNameList.add(fileName);
                mFilePathList.add(files[i].getAbsolutePath());
//                //判断后缀
//                int j = fileName.lastIndexOf(".");
//                String suffix = fileName.substring(j + 1);
//                if (suffix.equalsIgnoreCase("ts")) {
//                    mFileNameList.add(fileName);
//                    mFilePathList.add(files[i].getAbsolutePath());
//                }
            }
        }
    }

    /**
     * 初始化文件列表
     */
    private void initRecyclerView() {
        RecyclerView fileListRv = findViewById(R.id.recycler_view_file_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        fileListRv.setLayoutManager(layoutManager);
        mFileListAdapter = new FileListAdapter(this, mFileNameList);
        fileListRv.setAdapter(mFileListAdapter);

        // OnItemClick
        mFileListAdapter.setOnItemClickListener(new FileListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // 显示 loading 提示框
                showPopupWindow();

                // 判断文件路径是否为同一个
                mInputFilePath = mFilePathList.get(position);
                if (!mTs.getFilePath().equals(mInputFilePath)) {
                    mTs = new Ts();
                    mTs.setFilePath(mInputFilePath);
                }

                // 开启线程
                mTsThread = new TsThread(mInputFilePath, mHandler, mTs);
                mTsThread.start();
            }
        });
    }


    /**
     * 下拉刷新文件列表控件
     */
    private void initSmartRefreshLayout() {
        mRefreshLayout = findViewById(R.id.refresh_layout_refresh_file_list);
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                if (requestPermission()) {
                    mFileNameList.clear();
                    mFilePathList.clear();
                    traverseTsFile(TS_FOLDER_PATH);
                    mFileListAdapter.notifyDataSetChanged();
                    mRefreshLayout.finishRefresh(true);
                }

                // 刷新超时 2 秒
                refreshlayout.getLayout().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshLayout.finishRefresh(false);
                    }
                }, 2000);
            }
        });
    }

    /**
     * loading 提示框
     */
    private void showPopupWindow() {
        //  获取屏幕的宽高像素
        Display display = getWindow().getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        View view = LayoutInflater.from(this).inflate(R.layout.select_file_popup_window, null);

        mPopupWindow = new PopupWindow(view,
                560, 353, true);
        mPopupWindow.setContentView(view);

        // 播放读取中的旋转动画
        ImageView loadingAnimIv = view.findViewById(R.id.iv_loading_animation);
        Animation rotate = AnimationUtils.loadAnimation(
                this, R.anim.select_file_popup_window_loading_rotate);
        LinearInterpolator lin = new LinearInterpolator();
        rotate.setInterpolator(lin);
        loadingAnimIv.setAnimation(rotate);
        loadingAnimIv.startAnimation(rotate);

        TextView loadingTv = view.findViewById(R.id.tv_loading);

        // 外部可点击，即点击 PopupWindow 以外的区域，PopupWindow 消失
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopupWindow.setOutsideTouchable(true);

        // 将 PopupWindow 的实例放在一个父容器中，并定位
        View locationView = LayoutInflater.from(this).inflate(R.layout.select_file_activity, null);
        mPopupWindow.showAtLocation(locationView, Gravity.CENTER, 0, 0);

        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                // 中断线程
                mTsThread.setOver();
            }
        });
    }


    /**
     * Handler
     */
    private static class MyHandler extends Handler {
        WeakReference<SelectFileActivity> mWeakReference;
        boolean isGetPAT = false;
        boolean isGetSDT = false;
        boolean isGetProgramList = false;
        boolean isGetPmtList = false;

        public MyHandler(SelectFileActivity activity) {
            super();
            mWeakReference = new WeakReference<SelectFileActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            SelectFileActivity selectFileActivity = mWeakReference.get();

            if (selectFileActivity != null) {
                switch (msg.what) {
                    case GET_PAT:
                        isGetPAT = true;
                        break;
                    case GET_SDT:
                        isGetSDT = true;
                        break;
                    case GET_PROGRAM_LIST:
                        isGetProgramList = true;
                        break;
                    case GET_ALL_PMT:
                        // 关闭等待框
                        selectFileActivity.mPopupWindow.dismiss();
                        // 进入 MainActivity
                        Intent intent = new Intent(selectFileActivity, MainActivity.class);
                        intent.putExtra(KEY_TS_DATA, selectFileActivity.mTs);
                        selectFileActivity.startActivity(intent);
                        break;

                    default:
                        break;
                }
            }
        }
    }


    /**
     * 请求系统读写权限
     */
    private boolean requestPermission() {
        int checkPermission = ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        //  是否已经授予权限
        if (checkPermission != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_PERMISSION);
            return false;
        }
        return true;
    }

    /**
     * 注册权限申请回调
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case WRITE_EXTERNAL_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                    Toast.makeText(this, "WRITE_EXTERNAL_STORAGE ALLOW", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "WRITE_EXTERNAL_STORAGE DENY", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 双击返回键退出应用
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                // 销毁 ActivityCollector.activityList 的 Activity
//                ActivityCollector.finishAll();
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
