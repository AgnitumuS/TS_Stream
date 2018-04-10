package com.excellence.iptv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.excellence.iptv.bean.Program;
import com.excellence.iptv.bean.Ts;
import com.excellence.iptv.broadcast.MyActoin;
import com.excellence.iptv.fragment.AboutFragment;
import com.excellence.iptv.fragment.FavoriteFragment;
import com.excellence.iptv.fragment.LiveFragment;
import com.excellence.iptv.fragment.SearchFragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;
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
    public static final int SEARCH_FRAGMENT = 2;
    public static final int FAVORITE_FRAGMENT = 3;

    private Ts mTs;

    private List<Program> mProgramList;

    private LinearLayout mNavigationLl;
    private ImageView mNavLiveIv, mNavAboutIv;
    private TextView mNavLiveTv, mNavAboutTv;

    int mFragmentPos;
    private FragmentManager mFragmentManager;
    private Fragment mLiveFragment, mAboutFragment, mSearchFragment, mFavoriteFragment;
    private String[] tags = new String[]{"LiveFragment", "AboutFragment", "SearchFragment", "FavoriteFragment"};

    private MyLocalReceiver mLocalReceiver;
    private LocalBroadcastManager mLocalBroadcastManager;
    private IntentFilter mIntentFilter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_acitvity);

        // 获取 TS 对象
        mTs = (Ts) getIntent().getSerializableExtra(SelectFileActivity.KEY_TS_DATA);
        mProgramList = mTs.getProgramList();

        // 比对历史的节目列表
        initProgramList(mTs);

        // 本地广播
        initLocalBroadcast();

        // 初始化底部导航栏
        initNavView();

        // 默认显示的 Fragment
        mFragmentPos = LIVE_FRAGMENT;
        resetSelected(mFragmentPos);
        showContent(LIVE_FRAGMENT);
    }

    private void initProgramList(Ts ts) {
        // 从 SP 中获取喜爱列表
        SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
        String fileName = new File(ts.getFilePath()).getName();
        String key = fileName;
        String json = sp.getString(key, null);
        if (json != null) {
            List<Program> favoriteList = new Gson().fromJson(json, new TypeToken<List<Program>>() {
            }.getType());

            for (Program program : favoriteList) {
                for (int i = 0; i < mProgramList.size(); i++) {
                    if (mProgramList.get(i).getProgramNumber() == program.getProgramNumber()) {
                        mProgramList.get(i).setIsFavorite(true);
                        break;
                    }
                }
            }
        }

    }

    private void initLocalBroadcast() {
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(MyActoin.LIVE_FRAGMENT_BACK_LOCAL_ACTION);
        mIntentFilter.addAction(MyActoin.ABOUT_FRAGMENT_BACK_LOCAL_ACTION);
        mIntentFilter.addAction(MyActoin.SEARCH_FRAGMENT_BACK_LOCAL_ACTION);
        mIntentFilter.addAction(MyActoin.FAVORITE_FRAGMENT_BACK_LOCAL_ACTION);
        mLocalReceiver = new MyLocalReceiver();
        mLocalBroadcastManager.registerReceiver(mLocalReceiver, mIntentFilter);
    }

    private void initNavView() {
        mNavigationLl = findViewById(R.id.ll_nav);

        LinearLayout navLiveLl = findViewById(R.id.ll_live);
        LinearLayout navAboutLl = findViewById(R.id.ll_about);

        mNavLiveIv = findViewById(R.id.iv_live);
        mNavAboutIv = findViewById(R.id.iv_about);

        mNavLiveTv = findViewById(R.id.tv_live);
        mNavAboutTv = findViewById(R.id.tv_about);

        navLiveLl.setOnClickListener(this);
        navAboutLl.setOnClickListener(this);
    }

    private void resetSelected(int position) {
        mNavLiveIv.setSelected(false);
        mNavAboutIv.setSelected(false);
        mNavLiveTv.setSelected(false);
        mNavAboutTv.setSelected(false);
        switch (position) {
            case LIVE_FRAGMENT:
                mNavLiveIv.setSelected(true);
                mNavLiveTv.setSelected(true);
                break;
            case ABOUT_FRAGMENT:
                mNavAboutIv.setSelected(true);
                mNavAboutTv.setSelected(true);
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_live:
                resetSelected(LIVE_FRAGMENT);
                showContent(LIVE_FRAGMENT);
                break;

            case R.id.ll_about:
                resetSelected(ABOUT_FRAGMENT);
                showContent(ABOUT_FRAGMENT);
                break;

            default:
                break;
        }
    }

    public void showContent(int to) {
        if (mFragmentManager == null) {
            mFragmentManager = getSupportFragmentManager();
        }
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fl_content, getFragments(to), tags[to]);
        fragmentTransaction.commit();
    }

    private Fragment getFragments(int to) {
        Fragment fragment;
        switch (to) {
            case LIVE_FRAGMENT:
                mFragmentPos = LIVE_FRAGMENT;
                mLiveFragment = new LiveFragment();
                fragment = mLiveFragment;
                break;

            case ABOUT_FRAGMENT:
                mFragmentPos = ABOUT_FRAGMENT;
                mAboutFragment = new AboutFragment();
                fragment = mAboutFragment;
                break;

            case SEARCH_FRAGMENT:
                mFragmentPos = SEARCH_FRAGMENT;
                mSearchFragment = new SearchFragment();
                fragment = mSearchFragment;
                mNavigationLl.setVisibility(View.GONE);
                break;

            case FAVORITE_FRAGMENT:
                mFragmentPos = FAVORITE_FRAGMENT;
                mFavoriteFragment = new FavoriteFragment();
                fragment = mFavoriteFragment;
                mNavigationLl.setVisibility(View.GONE);
                break;

            default:
                fragment = new Fragment();
                break;
        }
        return fragment;
    }


    public List<Program> getProgramList() {
        return mProgramList;
    }


    private class MyLocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MyActoin.LIVE_FRAGMENT_BACK_LOCAL_ACTION)) {
            }
            if (action.equals(MyActoin.ABOUT_FRAGMENT_BACK_LOCAL_ACTION)) {
            }
            if (action.equals(MyActoin.SEARCH_FRAGMENT_BACK_LOCAL_ACTION)) {
                showContent(LIVE_FRAGMENT);
                mNavigationLl.setVisibility(View.VISIBLE);
            }
            if (action.equals(MyActoin.FAVORITE_FRAGMENT_BACK_LOCAL_ACTION)) {
                showContent(LIVE_FRAGMENT);
                mNavigationLl.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            // 如果在 SEARCH_FRAGMENT ，按返回键则返回 LIVE_FRAGMENT
            switch (mFragmentPos) {
                case LIVE_FRAGMENT:
                    finish();
                    break;
                case ABOUT_FRAGMENT:
                    finish();
                    break;
                case SEARCH_FRAGMENT:
                    Intent intent2 = new Intent(MyActoin.SEND_SEARCH_FRAGMENT_LOCAL_ACTION);
                    mLocalBroadcastManager.sendBroadcast(intent2);
                    break;
                case FAVORITE_FRAGMENT:
                    Intent intent3 = new Intent(MyActoin.SEND_FAVORITE_FRAGMENT_LOCAL_ACTION);
                    mLocalBroadcastManager.sendBroadcast(intent3);
                    break;

                default:
                    break;
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocalBroadcastManager.unregisterReceiver(mLocalReceiver);

        // 将收藏列表存入 SharedPreferences
        List<Program> favoriteList = new ArrayList<>();
        for (Program program : mProgramList) {
            if (program.getIsFavorite()) {
                favoriteList.add(program);
            }
        }
        String json = new Gson().toJson(favoriteList);
        String fileName = new File(mTs.getFilePath()).getName();
        String key = fileName;
        SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
        editor.putString(key, json);
        editor.apply();
    }
}
