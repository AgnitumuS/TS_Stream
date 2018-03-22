package com.tosmart.tsgetpidpacket;

import android.Manifest;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tosmart.tsgetpidpacket.threads.GetPidPacketThread;
import com.tosmart.tsgetpidpacket.utils.PacketManager;

import java.io.File;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * MainActivity
 *
 * @author ggz
 * @date 2018/3/19
 */

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int WRITE_EXTERNAL_PERMISSION = 1;
    public static final int REFRESH_UI_PACKET_LENGTH = 0;
    public static final int REFRESH_UI_PACKET_NUMBER = 1;
    public static final String PACKET_LENGTH_KEY = "packetLen";
    public static final String PACKET_START_POSITION_KEY = "packetStartPosition";
    public static final String PACKET_NUMBER_KEY = "packetNum";


    private String mTSFilePath;
    private String[] mFileList;

    private PacketManager mPacketManager;
    private ListView mListView;
    private ArrayAdapter<String> mArrayAdapter;
    private TextView mPacketLengthTv;
    private TextView mPacketNumTv;
    private Button mRequestPermissionBtn;

    Handler myUIHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            int packetLen;
            int packetStartPosition;
            int packetNum;
            switch (msg.what) {
                case REFRESH_UI_PACKET_LENGTH:
                    packetLen = data.getInt(PACKET_LENGTH_KEY);
                    packetStartPosition = data.getInt(PACKET_START_POSITION_KEY);
                    mPacketLengthTv.setText("PacketLength : " + packetLen +
                            " , PacketStartPosition : " + packetStartPosition);
                    break;

                case REFRESH_UI_PACKET_NUMBER:
                    packetNum = data.getInt(PACKET_NUMBER_KEY);
                    mPacketNumTv.setText("PacketNum : " + packetNum);

                    initData();
                    initListView();

                    break;
                default:
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // 初始化 UI 控件
        initView();

        // 判断 Android 版本是否大于 23 （Android 6.0）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Request Read And Write Permission
            requestPermission();
        }

        // 初始化控件内容
        initData();

        // 初始化 ListView
        initListView();
    }

    private void initView() {
        mPacketLengthTv = findViewById(R.id.tv_packet_length);
        mPacketNumTv = findViewById(R.id.tv_packet_number);

        mRequestPermissionBtn = findViewById(R.id.btn_request_permission);
        mRequestPermissionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermission();
            }
        });
    }

    private void initData() {
        mTSFilePath = Environment.getExternalStorageDirectory().getPath() + "/ts/";
        File file = new File(mTSFilePath);
        // 获取 ts 文件夹里面的文件列表
        mFileList = file.list();
    }

    private void initListView() {
        mListView = findViewById(R.id.lv_file_list);

        if (mFileList != null) {
            mArrayAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_list_item_1, mFileList);
            mListView.setAdapter(mArrayAdapter);
        }

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                mPacketLengthTv.setText(R.string.main_tv_packet_length);
                mPacketNumTv.setText(R.string.main_tv_packet_number);

                /*
                 * Demo
                 * 每种线程都有对应的构造方法来传参数
                 * 详情请看 PacketManager 的构造方法
                 * */
                mPacketManager = new PacketManager(
                        mTSFilePath + mFileList[position],
                        0x0000,
                        0x00);
                // 非必须：文件输出路径
                mPacketManager.setOutputFilePath(mTSFilePath + "resultFile" + (mFileList.length - 2));
                Log.d(TAG, " ---- 开启线程");
                GetPidPacketThread getPidPacketThread = new GetPidPacketThread(
                        mPacketManager,
                        myUIHandler);
                getPidPacketThread.start();


            }
        });
    }


    /**
     * 请求系统读写权限
     */
    private void requestPermission() {
        int checkReadPermission = ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        //  是否已经授予权限
        if (checkReadPermission != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_PERMISSION);
        }
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
                    mRequestPermissionBtn.setVisibility(View.GONE);
                } else {
                    Toast.makeText(this, "WRITE_EXTERNAL_STORAGE DENY", Toast.LENGTH_SHORT).show();
                    mRequestPermissionBtn.setVisibility(View.VISIBLE);
                }

                initData();
                initListView();
                break;

            default:
                break;
        }
    }

}