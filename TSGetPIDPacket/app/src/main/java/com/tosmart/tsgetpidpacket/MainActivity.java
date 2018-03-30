package com.tosmart.tsgetpidpacket;

import android.Manifest;
import android.graphics.Color;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.pickerview.OptionsPickerView;
import com.google.gson.Gson;
import com.tosmart.tsgetpidpacket.beans.Picker;
import com.tosmart.tsgetpidpacket.threads.GetPacketLengthThread;
import com.tosmart.tsgetpidpacket.threads.GetPidPacketThread;
import com.tosmart.tsgetpidpacket.utils.GetJsonDataUtil;
import com.tosmart.tsgetpidpacket.utils.PacketManager;

import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * MainActivity
 *
 * @author ggz
 * @date 2018/3/24
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private static final int WRITE_EXTERNAL_PERMISSION = 1;
    public static final int REFRESH_UI_PACKET_LENGTH = 0;
    public static final int REFRESH_UI_PACKET_NUMBER = 1;
    public static final String PACKET_LENGTH_KEY = "packetLen";
    public static final String PACKET_START_POSITION_KEY = "packetStartPosition";
    public static final String PACKET_NUMBER_KEY = "packetNum";
    public static final int HEX = 16;
    public static final int PICKERVIEW_SUB_TEXT_SIZE = 18;
    public static final int PICKERVIEW_CONTENT_TEXT_SIZE = 20;


    private String mTSFilePath;
    private List<String> mFileList = new ArrayList<>();

    private PacketManager mPacketManager;
    private TextView mFileNameTv;
    private TextView mPacketLengthTv;
    private TextView mPacketNumTv;
    private TextView mPidTv;
    private TextView mTableIdTv;
    private PopupWindow mPopupWindow;

    private ArrayList<Picker> mOptions1Items = new ArrayList<>();
    private ArrayList<ArrayList<String>> mOptions2Items = new ArrayList<>();
    private String mPidStr;
    private String mTableIdStr;
    private int mPidInt = -1;
    private int mTableIdInt = -1;
    private String mInputFilePath;


    Handler myUIHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            int packetLen;
            int packetStartPosition;
            int packetNum;
            String strResult;
            switch (msg.what) {
                case REFRESH_UI_PACKET_LENGTH:
                    packetLen = data.getInt(PACKET_LENGTH_KEY);
                    packetStartPosition = data.getInt(PACKET_START_POSITION_KEY);
                    strResult = getResources().getString(R.string.main_tv_packet_length_result);
                    strResult = String.format(strResult, packetLen, packetStartPosition);
                    mPacketLengthTv.setText(strResult);
                    break;

                case REFRESH_UI_PACKET_NUMBER:
                    packetNum = data.getInt(PACKET_NUMBER_KEY);
                    strResult = getResources().getString(R.string.main_tv_packet_number_result);
                    strResult = String.format(strResult, packetNum);
                    mPacketNumTv.setText(strResult);
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

        initData();
    }

    private void initView() {
        mFileNameTv = findViewById(R.id.tv_file_name);
        mPacketLengthTv = findViewById(R.id.tv_packet_length);
        mPacketNumTv = findViewById(R.id.tv_packet_number);
        mPidTv = findViewById(R.id.tv_pid);
        mTableIdTv = findViewById(R.id.tv_table_id);

        TextView showPopupWindowTv = findViewById(R.id.tv_show_popupwindow);
        showPopupWindowTv.setOnClickListener(this);

        LinearLayout openPickerViewLL = findViewById(R.id.ll_open_picker);
        openPickerViewLL.setOnClickListener(this);

        Button parseSectionBtn = findViewById(R.id.btn_parse_section);
        parseSectionBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_show_popupwindow:
                if (requestPermission()) {
                    showPopupWindow();
                }
                break;

            case R.id.ll_open_picker:
                showPickerView();
                break;

            case R.id.btn_parse_section:
                if (mInputFilePath != null && mPidInt != -1 && mTableIdInt != -1) {
                    // 解 Section
                    mPacketManager = new PacketManager(
                            mInputFilePath,
                            mPidInt,
                            mTableIdInt);
                    // 非必须：文件输出路径
                    mPacketManager.setOutputFilePath(mTSFilePath + "resultFile" + (mFileList.size() - 2));
                    Log.d(TAG, " ---- 开启线程");
                    GetPidPacketThread getPidPacketThread = new GetPidPacketThread(
                            mPacketManager,
                            myUIHandler);
                    getPidPacketThread.start();
                } else {
                    showPickerView();
                }
                break;

            default:
                break;
        }
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

        initPickerViewData();
    }

    /**
     * 显示显示并初始化 PopupWindow
     */
    private void showPopupWindow() {
        //  获取屏幕的宽高像素
        Display display = this.getWindow().getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        View view = LayoutInflater.from(this).inflate(R.layout.main_popupwindow, null);

        mPopupWindow = new PopupWindow(view, screenWidth, screenHeight / 3 * 2, true);
        mPopupWindow.setContentView(view);

        initListView(view);

        // 外部可点击，即点击 PopupWindow 以外的区域，PopupWindow 消失
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopupWindow.setOutsideTouchable(true);

        // 设置启动关闭动画
        mPopupWindow.setAnimationStyle(R.style.PopupWindowAnim);

        // 将 PopupWindow 的实例放在一个父容器中，并定位
        View locationView = LayoutInflater.from(this).inflate(R.layout.main_activity, null);
        mPopupWindow.showAtLocation(locationView, Gravity.BOTTOM, 0, 0);

        ImageView closePopupWindowIv = view.findViewById(R.id.iv_close_popupwindow);
        closePopupWindowIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
            }
        });
    }

    /**
     * 初始化 PopupWindow 里面的 listView 数据
     */
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

                mInputFilePath = mTSFilePath + mFileList.get(position);

                // 解包长
                mPacketManager = new PacketManager(mInputFilePath);
                Log.d(TAG, " ---- 开启线程");
                GetPacketLengthThread getPacketLengthThread = new GetPacketLengthThread(
                        mPacketManager,
                        myUIHandler);
                getPacketLengthThread.start();

                mPopupWindow.dismiss();
            }
        });
    }

    /**
     * 初始化 PickerView 的数据
     */
    private void initPickerViewData() {
        //获取 assets 目录下的 json 文件数据
        String jsonData = new GetJsonDataUtil().getJson(this, "picker.json");

        ArrayList<Picker> pidList = new ArrayList<>();
        try {
            JSONArray data = new JSONArray(jsonData);
            Gson gson = new Gson();
            for (int i = 0; i < data.length(); i++) {
                Picker entity = gson.fromJson(data.optJSONObject(i).toString(), Picker.class);
                pidList.add(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mOptions1Items = pidList;

        for (int i = 0; i < pidList.size(); i++) {
            ArrayList<String> tableIdList = new ArrayList<>();

            for (int j = 0; j < pidList.get(i).getTableId().size(); j++) {
                String tableId = pidList.get(i).getTableId().get(j);
                tableIdList.add(tableId);
            }

            mOptions2Items.add(tableIdList);
        }

    }


    /**
     * 初始化并显示 PickerView
     */
    private void showPickerView() {

        OptionsPickerView pvOptions = new OptionsPickerView.Builder(this, new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                mPidStr = mOptions1Items.get(options1).getPickerViewText();
                mTableIdStr = mOptions2Items.get(options1).get(options2);

                mPidTv.setText(mPidStr);
                mTableIdTv.setText(mTableIdStr);

                // 16进制 String 转 int
                String str = mPidStr.split("x")[1];
                Log.d(TAG, "str : " + str);
                mPidInt = Integer.parseInt(str, HEX);
                str = mTableIdStr.split("x")[1];
                Log.d(TAG, "str : " + str);
                mTableIdInt = Integer.parseInt(str, HEX);
            }
        })
                .setTitleText(getResources().getString(R.string.main_pickerview_title))
                .setDividerColor(Color.GRAY)
                .setTextColorCenter(Color.BLACK)
                .setSubCalSize(PICKERVIEW_SUB_TEXT_SIZE)
                .setContentTextSize(PICKERVIEW_CONTENT_TEXT_SIZE)
                .setSelectOptions(0, 0)
                .build();

        pvOptions.setPicker(mOptions1Items, mOptions2Items);
        pvOptions.show();
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