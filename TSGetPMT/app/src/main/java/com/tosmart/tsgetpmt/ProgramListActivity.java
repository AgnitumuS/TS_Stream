package com.tosmart.tsgetpmt;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.tosmart.tsgetpmt.adapter.ProgramListAdapter;
import com.tosmart.tsgetpmt.beans.tables.Pat;
import com.tosmart.tsgetpmt.beans.tables.PatProgram;
import com.tosmart.tsgetpmt.beans.tables.Pmt;
import com.tosmart.tsgetpmt.threads.GetPidPacketThread;
import com.tosmart.tsgetpmt.utils.PacketManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.toHexString;

/**
 * ProgramListActivity
 *
 * @author ggz
 * @date 2018/3/27
 */

public class ProgramListActivity extends AppCompatActivity {
    private static final String TAG = "ProgramListActivity";
    private static final int PAT_PID = 0x0000;
    private static final int PAT_TABLE_ID = 0x00;
    private static final int PMT_TABLE_ID = 0x02;
    public static final int REFRESH_UI_PROGRAM_LIST_PAT = 0;
    public static final int REFRESH_UI_PROGRAM_LIST_PMT = 1;
    private static final String HISTORY_FOLDER_PATH = Environment.getExternalStorageDirectory()
            .getPath() + "/ts_history/";

    private SmartRefreshLayout mRefreshLayout;

    private String mTSFolderPath;
    private String mFileName;
    private String mFilePath;

    private PacketManager mPacketManager;
    private List<PatProgram> mPatProgramList = new ArrayList<>();
    private ProgramListAdapter mAdapter;

    private GetPidPacketThread mGetPatThread;
    private GetPidPacketThread mGetPmtThread;

    private TextView mPmtPidTv;
    private TextView mPmtResultTv;


    Handler mUIHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case REFRESH_UI_PROGRAM_LIST_PAT:
                    mRefreshLayout.autoRefresh();
                    break;

                case REFRESH_UI_PROGRAM_LIST_PMT:
                    Pmt pmt = mPacketManager.getPmt();
                    if (pmt != null) {
                        if (mPmtResultTv != null) {
                            mPmtResultTv.setText(pmt.print());
                        }
                    }
                    break;

                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.program_list_activity);

        // 获取输入文件的路径和名字
        mTSFolderPath = getIntent().getStringExtra(FileListActivity.KEY_FOLDER_PATH);
        mFileName = getIntent().getStringExtra(FileListActivity.KEY_FILE_NAME);
        mFilePath = mTSFolderPath + mFileName;

        initView();

        initData();
    }

    private void initView() {
        ImageView backIv = findViewById(R.id.iv_back);
        backIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        RecyclerView programListRv = findViewById(R.id.rv_program_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        programListRv.setLayoutManager(layoutManager);
        mAdapter = new ProgramListAdapter(this, mPatProgramList);
        programListRv.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new ProgramListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String pidStr = "0x" + toHexString(mPatProgramList.get(position).getProgramMapPid());
                Toast.makeText(ProgramListActivity.this, pidStr, Toast.LENGTH_SHORT).show();

                /*
                 * 重新设置
                 * matchPidPacket(String inputFilePath, int inputPID, int inputTableID)
                 * 的参数,寻找 PMT
                 * */
                if (mFilePath != null) {
                    String inputFilePath = mFilePath;

                    boolean isFindHistoryFile = false;
                    String historyFilePath = findHistoryFile(mFileName + "_pmt_" + pidStr);
                    if (historyFilePath != null) {
                        isFindHistoryFile = true;
                        inputFilePath = historyFilePath;
                    }

                    mPacketManager = new PacketManager(
                            inputFilePath,
                            mPatProgramList.get(position).getProgramMapPid(),
                            PMT_TABLE_ID,
                            mUIHandler);
                    // 可选
                    if (!isFindHistoryFile) {
                        mPacketManager.setOutputFilePath(HISTORY_FOLDER_PATH + mFileName + "_pmt_" + pidStr);
                    }
                    mGetPmtThread = new GetPidPacketThread(
                            mPacketManager,
                            mUIHandler);
                    mGetPmtThread.start();

                    showPopupWindow();
                    String str = getResources().getString(R.string.program_list_popup_widow_tv_pmt_id_result);
                    str = String.format(str, pidStr);
                    if (mPmtPidTv != null) {
                        mPmtPidTv.setText(str);
                    }
                }
            }
        });


        mRefreshLayout = findViewById(R.id.refresh_layout_refresh_program_list);
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                Pat pat = mPacketManager.getPat();
                if (pat != null) {
                    mPatProgramList.clear();
                    List<PatProgram> patProgramList = pat.getPatProgramList();
                    for (PatProgram patProgram : patProgramList) {
                        mPatProgramList.add(patProgram);
                    }
                    mAdapter.notifyDataSetChanged();
                }
                mRefreshLayout.finishRefresh();
            }
        });

        mRefreshLayout.autoRefresh();
    }

    private void initData() {
        if (mFilePath != null) {
            String inputFilePath = mFilePath;

            boolean isFindHistoryFile = false;
            String historyFilePath = findHistoryFile(mFileName + "_pat");
            if (historyFilePath != null) {
                isFindHistoryFile = true;
                inputFilePath = historyFilePath;
            }

            // 解 PAT
            mPacketManager = new PacketManager(
                    inputFilePath,
                    PAT_PID,
                    PAT_TABLE_ID,
                    mUIHandler);
            // 非必须：文件输出路径
            if (!isFindHistoryFile) {
                mPacketManager.setOutputFilePath(HISTORY_FOLDER_PATH + mFileName + "_pat");
            }

            Log.d(TAG, " ---- 开启线程");
            mGetPatThread = new GetPidPacketThread(
                    mPacketManager,
                    mUIHandler);
            mGetPatThread.start();
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

    /**
     * 显示 PMT 解析结果
     */
    private void showPopupWindow() {
        //  获取屏幕的宽高像素
        Display display = this.getWindow().getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        View view = LayoutInflater.from(this).inflate(R.layout.program_list_popup_window_pmt, null);

        PopupWindow popupWindow = new PopupWindow(view,
                screenWidth / 5 * 4, screenHeight / 5 * 3, true);
        popupWindow.setContentView(view);

        mPmtPidTv = view.findViewById(R.id.tv_pmt_id);
        mPmtResultTv = view.findViewById(R.id.tv_pmt_result);

        // 外部可点击，即点击 PopupWindow 以外的区域，PopupWindow 消失
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);

        // 将 PopupWindow 的实例放在一个父容器中，并定位
        View locationView = LayoutInflater.from(this).inflate(R.layout.program_list_activity, null);
        popupWindow.showAtLocation(locationView, Gravity.CENTER, 0, 0);

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mPmtResultTv.setText(getResources().getString(R.string.program_list_popup_window_tv_pmt_wait));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGetPatThread.over();

    }
}
