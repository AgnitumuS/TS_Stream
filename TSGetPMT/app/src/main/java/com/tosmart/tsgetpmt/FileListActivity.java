package com.tosmart.tsgetpmt;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * FileListActivity
 *
 * @author ggz
 * @date 2018/3/27
 */

public class FileListActivity extends AppCompatActivity {
    private static final String TAG = "FileListActivity";
    private static final int WRITE_EXTERNAL_PERMISSION = 1;
    public static final String KEY_FOLDER_PATH = "FolderPath";
    public static final String KEY_FILE_NAME = "FileName";

    private SmartRefreshLayout mRefreshLayout;
    private ArrayAdapter mAdapter;

    private String mTSFolderPath;
    private List<String> mFileList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_list_activity);

        // 初始化 UI 控件
        initView();

        // 判断 Android 版本是否大于 23 （Android 6.0）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Request Read And Write Permission
            requestPermission();
        }

        initData();

        initListView();
    }

    private void initView() {
        mRefreshLayout = findViewById(R.id.refresh_layout_refresh_file_list);
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                if (requestPermission()) {
                    initData();
                    mAdapter.notifyDataSetChanged();
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

    private void initData() {
        mTSFolderPath = Environment.getExternalStorageDirectory().getPath() + "/ts/";
        File file = new File(mTSFolderPath);
        // 获取 ts 文件夹里面的文件列表
        String[] fileList = file.list();
        if (fileList != null) {
            mFileList.clear();
            for (String str : fileList) {
                mFileList.add(str);
            }
        }
    }

    /**
     * 初始化 listView
     */
    private void initListView() {
        ListView listView = findViewById(R.id.lv_file_list);

        mAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, mFileList);
        listView.setAdapter(mAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                Intent intent = new Intent(FileListActivity.this, ProgramListActivity.class);
                intent.putExtra(KEY_FOLDER_PATH, mTSFolderPath);
                intent.putExtra(KEY_FILE_NAME, mFileList.get(position));
                startActivity(intent);
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