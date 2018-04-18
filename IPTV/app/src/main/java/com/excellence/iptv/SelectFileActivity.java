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
import android.util.Log;
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
import com.excellence.iptv.thread.TraverseFileThread;
import com.excellence.iptv.thread.TsThread;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

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

    public static final int GET_FILE = 0;
    public static final int GET_LENGTH_AND_START = 1;
    public static final int GET_PAT_SDT_EIT = 2;
    public static final int GET_PROGRAM_LIST = 3;
    public static final int GET_ALL_PMT = 4;
    public static final String KEY_TS_DATA = "TsData";

    private MyHandler mHandler;

    private SmartRefreshLayout mRefreshLayout;
    private FileListAdapter mFileListAdapter;
    private PopupWindow mPopupWindow;
    private TextView mLoadingTv;

    private List<String> mFileNameList = new ArrayList<>();
    private List<String> mFilePathList = new ArrayList<>();

    private String mInputFilePath;
    private Ts mTs;
    private List<Ts> mTsList = new ArrayList<>();

    private TraverseFileThread mTraverseFileThread = null;
    private TsThread mTsThread = null;

    private long mExitTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_file_activity);

        mHandler = new MyHandler(this);

        // 判断 Android 版本是否大于 23 （Android 6.0）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Request Read And Write Permission
            requestPermission();
        }

        // 显示文件列表
        initRecyclerView();

        initSmartRefreshLayout();

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

                // 根据文件路径判断是否点击已经解过的文件
                mInputFilePath = mFilePathList.get(position);
                boolean isNewTs = true;
                if (mTsList.size() != 0) {
                    for (Ts ts : mTsList) {
                        if (ts.getFilePath().equals(mInputFilePath)) {
                            mTs = ts;
                            isNewTs = false;
                            break;
                        }
                    }
                }
                if (isNewTs) {
                    mTs = new Ts();
                    mTsList.add(mTs);
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
        if (mFileNameList.size() == 0) {
            mRefreshLayout.autoRefresh();
        }

        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                if (requestPermission()) {
                    // 显示等待框
                    showPopupWindow();
                    // 开启线程,遍历文件
                    traverseFile();
                }

                refreshlayout.finishRefresh();
            }
        });
    }

    /**
     * 遍历 TS 文件夹
     */
    private void traverseFile() {
        // 开启线程
        mTraverseFileThread = new TraverseFileThread(TS_FOLDER_PATH, mHandler, mFilePathList, mFileNameList);
        mTraverseFileThread.start();
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

        mLoadingTv = view.findViewById(R.id.tv_loading);

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
                if (mTsThread != null) {
                    mTsThread.setOver();
                }
                if (mTraverseFileThread != null) {
                    mTraverseFileThread.setOver();
                }
            }
        });
    }


    /**
     * Handler
     */
    private static class MyHandler extends Handler {
        WeakReference<SelectFileActivity> mWeakReference;

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
                    case GET_FILE:
                        selectFileActivity.mPopupWindow.dismiss();
                        selectFileActivity.mFileListAdapter.notifyDataSetChanged();
                        break;
                    case GET_LENGTH_AND_START:
                        String s1 = selectFileActivity
                                .getResources().getString(R.string.select_file_popup_tv_content);
                        s1 = String.format(s1, "Get Length StartPosition");
                        selectFileActivity.mLoadingTv.setText(s1);
                        break;
                    case GET_PAT_SDT_EIT:
                        String s2 = selectFileActivity
                                .getResources().getString(R.string.select_file_popup_tv_content);
                        s2 = String.format(s2, "Get PAT SDT EIT");
                        selectFileActivity.mLoadingTv.setText(s2);
                        break;
                    case GET_PROGRAM_LIST:
                        String s3 = selectFileActivity
                                .getResources().getString(R.string.select_file_popup_tv_content);
                        s3 = String.format(s3, "Get ProgramList");
                        Toast.makeText(selectFileActivity, s3, Toast.LENGTH_SHORT).show();
                        break;
                    case GET_ALL_PMT:
                        String s4 = selectFileActivity
                                .getResources().getString(R.string.select_file_popup_tv_content);
                        s4 = String.format(s4, "Get PMT");
                        Toast.makeText(selectFileActivity, s4, Toast.LENGTH_SHORT).show();

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
