package com.excellence.iptv;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;

import com.rengwuxian.materialedittext.MaterialEditText;

/**
 * SearchActivity
 *
 * @author ggz
 * @date 2018/4/3
 */

public class SearchActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "SearchActivity";

    private MaterialEditText searchEt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);
        initView();
    }

    private void initView() {
        searchEt = findViewById(R.id.et_search);

        ImageView searchIv = findViewById(R.id.iv_search);
        searchIv.setOnClickListener(this);

        Button cancelBtn = findViewById(R.id.btn_cancel);
        cancelBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:
                hideKeyboard(v);
                searchEt.setText("");
                break;

            case R.id.iv_search:
                hideKeyboard(v);
                search();
                break;

            default:
                break;
        }
    }

    private void search() {


    }


    private void hideKeyboard(View v) {
        // 隐藏键盘
        InputMethodManager imm = (InputMethodManager)
                getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
