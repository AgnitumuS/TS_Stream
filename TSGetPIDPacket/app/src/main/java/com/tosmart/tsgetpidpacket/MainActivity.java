package com.tosmart.tsgetpidpacket;

import android.Manifest;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.tosmart.tsgetpidpacket.threads.GetPidPacketThread;
import com.tosmart.tsgetpidpacket.utils.PacketManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
    private List<String> mFileList = new ArrayList<>();

    private PacketManager mPacketManager;
    private TextView mFileNameTv;
    private TextView mPacketLengthTv;
    private TextView mPacketNumTv;

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
                    break;

                default:
                    break;
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
    }

    private void initView() {
        mFileNameTv = findViewById(R.id.tv_file_name);
        mPacketLengthTv = findViewById(R.id.tv_packet_length);
        mPacketNumTv = findViewById(R.id.tv_packet_number);

        TextView showPopupWindowTv = findViewById(R.id.tv_show_popupwindow);
        showPopupWindowTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (requestPermission()) {
                    showPopupWindow();
                }
            }
        });
    }

    private void initData() {
        mTSFilePath = Environment.getExternalStorageDirectory().getPath() + "/ts/";
        File file = new File(mTSFilePath);
        // 获取 ts 文件夹里面的文件列表
        String[] fileList = file.list();
        if (fileList != null) {
            mFileList.clear();
            for (String str : fileList) {
                mFileList.add(str);
            }
        }
    }

    private void showPopupWindow() {
        //  获取屏幕的宽高像素
        Display display = this.getWindow().getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        View view = LayoutInflater.from(this).inflate(R.layout.main_popupwindow, null);

        final PopupWindow popupWindow = new PopupWindow(view, screenWidth, screenHeight / 3 * 2, true);
        popupWindow.setContentView(view);

        initListView(view);

        // 外部可点击，即点击 PopupWindow 以外的区域，PopupWindow 消失
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);

        // 设置启动关闭动画
        popupWindow.setAnimationStyle(R.style.PopupWindowAnim);

        // 将 PopupWindow 的实例放在一个父容器中，并定位
        View locationView = LayoutInflater.from(this).inflate(R.layout.main_activity, null);
        popupWindow.showAtLocation(locationView, Gravity.BOTTOM, 0, 0);

        ImageView closePopupWindowIv = view.findViewById(R.id.iv_close_popupwindow);
        closePopupWindowIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    private void initListView(View v) {
        ListView listView = v.findViewById(R.id.lv_file_list);

        ArrayAdapter adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, mFileList);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                mFileNameTv.setText(mFileList.get(position));
                mPacketLengthTv.setText(R.string.main_tv_packet_length);
                mPacketNumTv.setText(R.string.main_tv_packet_number);

                /*
                 * Demo
                 * 每种线程都有对应的构造方法来传参数
                 * 详情请看 PacketManager 的构造方法
                 * */
                mPacketManager = new PacketManager(
                        mTSFilePath + mFileList.get(position),
                        0x0012,
                        0x4e);
                // 非必须：文件输出路径
                mPacketManager.setOutputFilePath(mTSFilePath + "resultFile" + (mFileList.size() - 2));
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

                    initData();

                } else {
                    Toast.makeText(this, "WRITE_EXTERNAL_STORAGE DENY", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }
    }

}