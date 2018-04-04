package com.excellence.iptv;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.excellence.iptv.bean.Program;
import com.excellence.iptv.fragment.AboutFragment;
import com.excellence.iptv.fragment.LiveFragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

/**
 * MainActivity
 *
 * @author ggz
 * @date 2018/4/2
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private static final int LIVE_FRAGMENT = 0;
    private static final int ABOUT_FRAGMENT = 1;

    private List<Program> mProgramList;

    private ImageView mNavLiveIv, mNavAboutIv;
    private TextView mNavLiveTv, mNavAboutTv;

    int mFragmentPos;
    private FragmentManager mFragmentManager;
    private Fragment mLiveFragment, mAboutFragment;
    private String[] tags = new String[]{"LiveFragment", "AboutFragment"};


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_acitvity);

        // 获取节目列表数据
        String data = getIntent().getStringExtra(SelectFileActivity.KEY_DATA);
        String nullStr = "null";
        if (!nullStr.equals(data)) {
            mProgramList = new Gson().fromJson(data,new TypeToken<List<Program>>(){}.getType());
        }


        initView();


        // 默认
        mNavLiveIv.setSelected(true);
        mNavLiveTv.setSelected(true);
        mFragmentPos = LIVE_FRAGMENT;
        showContent(LIVE_FRAGMENT);
    }

    private void initView() {
        LinearLayout navLiveLl = findViewById(R.id.ll_live);
        LinearLayout navAboutLl = findViewById(R.id.ll_about);

        mNavLiveIv = findViewById(R.id.iv_live);
        mNavAboutIv = findViewById(R.id.iv_about);

        mNavLiveTv = findViewById(R.id.tv_live);
        mNavAboutTv = findViewById(R.id.tv_about);

        navLiveLl.setOnClickListener(this);
        navAboutLl.setOnClickListener(this);
    }

    private void resetSelected() {
        mNavLiveIv.setSelected(false);
        mNavAboutIv.setSelected(false);

        mNavLiveTv.setSelected(false);
        mNavAboutTv.setSelected(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_live:
                resetSelected();
                mNavLiveIv.setSelected(true);
                mNavLiveTv.setSelected(true);
                showContent(LIVE_FRAGMENT);
                break;

            case R.id.ll_about:
                resetSelected();
                mNavAboutIv.setSelected(true);
                mNavAboutTv.setSelected(true);
                showContent(ABOUT_FRAGMENT);
                break;

            default:
                break;
        }
    }

    private void showContent(int to) {
        if (mFragmentManager == null) {
            mFragmentManager = getSupportFragmentManager();
        }
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        hideFragment(fragmentTransaction);
        showFragment(fragmentTransaction, to);
        fragmentTransaction.commit();
    }

    private void hideFragment(FragmentTransaction ft) {
        if (mLiveFragment != null) {
            ft.hide(mLiveFragment);
        }
        if (mAboutFragment != null) {
            ft.hide(mAboutFragment);
        }
    }

    private void showFragment(FragmentTransaction ft, int to) {
        switch (to) {
            case LIVE_FRAGMENT:
                mFragmentPos = LIVE_FRAGMENT;
                if (mLiveFragment == null) {
                    mLiveFragment = new LiveFragment();
                    ft.add(R.id.fl_content, mLiveFragment, tags[to]);
                } else {
                    ft.show(mLiveFragment);
                }
                break;

            case ABOUT_FRAGMENT:
                mFragmentPos = ABOUT_FRAGMENT;
                if (mAboutFragment == null) {
                    mAboutFragment = new AboutFragment();
                    ft.add(R.id.fl_content, mAboutFragment, tags[to]);
                } else {
                    ft.show(mAboutFragment);
                }
                break;

            default:
                break;
        }
    }


    public List<Program> getProgramList() {
        return mProgramList;
    }
}
