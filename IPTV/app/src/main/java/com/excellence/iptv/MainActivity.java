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
import android.util.Log;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.excellence.iptv.bean.Program;
import com.excellence.iptv.bean.Ts;
import com.excellence.iptv.broadcast.MyActoin;
import com.excellence.iptv.fragment.AboutFragment;
import com.excellence.iptv.fragment.FavoriteFragment;
import com.excellence.iptv.fragment.LiveFragment;
import com.excellence.iptv.fragment.SearchFragment;
import com.excellence.iptv.view.RobotoRegularTextView;
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
    public static final int LIVE_FRAGMENT = 0;
    public static final int ABOUT_FRAGMENT = 1;
    public static final int SEARCH_FRAGMENT = 2;
    public static final int FAVORITE_FRAGMENT = 3;

    private Ts mTs;

    private List<Program> mProgramList;

    private LinearLayout mNavigationLl;
    private ImageView mNavLiveIv, mNavAboutIv;
    private RobotoRegularTextView mNavLiveTv, mNavAboutTv;

    int mFragmentPos;
    private FragmentManager mFragmentManager;
    private String[] tags = new String[]{"LiveFragment", "AboutFragment", "SearchFragment", "FavoriteFragment"};

    private MyLocalReceiver mLocalReceiver;
    private LocalBroadcastManager mLocalBroadcastManager;

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
        String key = new File(ts.getFilePath()).getName();
        String json = sp.getString(key, null);
        if (json != null) {
            SparseIntArray sparseIntArray = new SparseIntArray();
            for (int i = 0; i < mProgramList.size(); i++) {
                sparseIntArray.put(mProgramList.get(i).getProgramNumber(), i);
            }

            List<Program> list = new Gson().fromJson(json, new TypeToken<List<Program>>() {
            }.getType());
            for (Program program : list) {
                int position = sparseIntArray.get(program.getProgramNumber(), -1);
                if (position != -1) {
                    mProgramList.get(position).setIsFavorite(true);
                }
            }

        }

    }

    private void initLocalBroadcast() {
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyActoin.LIVE_FRAGMENT_BACK_LOCAL_ACTION);
        intentFilter.addAction(MyActoin.ABOUT_FRAGMENT_BACK_LOCAL_ACTION);
        intentFilter.addAction(MyActoin.SEARCH_FRAGMENT_BACK_LOCAL_ACTION);
        intentFilter.addAction(MyActoin.FAVORITE_FRAGMENT_BACK_LOCAL_ACTION);
        mLocalReceiver = new MyLocalReceiver();
        mLocalBroadcastManager.registerReceiver(mLocalReceiver, intentFilter);
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
        setAnimations(fragmentTransaction, to);
        fragmentTransaction.replace(R.id.fl_content, getFragments(to), tags[to]);
        fragmentTransaction.commit();
    }

    /**
     * 动画切换
     */
    private void setAnimations(FragmentTransaction fragmentTransaction, int to) {
        if (mFragmentPos == LIVE_FRAGMENT && to == ABOUT_FRAGMENT) {
            fragmentTransaction.setCustomAnimations(R.anim.fragment_right_in,
                    R.anim.fragment_left_out);
        }
        if (mFragmentPos == ABOUT_FRAGMENT && to == LIVE_FRAGMENT) {
            fragmentTransaction.setCustomAnimations(R.anim.fragment_left_in,
                    R.anim.fragment_right_out);
        }
        if (mFragmentPos == LIVE_FRAGMENT && to == FAVORITE_FRAGMENT) {
            fragmentTransaction.setCustomAnimations(R.anim.fragment_right_in,
                    R.anim.fragment_alpha_out);
        }
    }

    private Fragment getFragments(int to) {
        Fragment fragment;
        switch (to) {
            case LIVE_FRAGMENT:
                mFragmentPos = LIVE_FRAGMENT;
                fragment = new LiveFragment();
                break;

            case ABOUT_FRAGMENT:
                mFragmentPos = ABOUT_FRAGMENT;
                fragment = new AboutFragment();
                break;

            case SEARCH_FRAGMENT:
                mFragmentPos = SEARCH_FRAGMENT;
                fragment = new SearchFragment();
                mNavigationLl.setVisibility(View.GONE);
                break;

            case FAVORITE_FRAGMENT:
                mFragmentPos = FAVORITE_FRAGMENT;
                fragment = new FavoriteFragment();
                mNavigationLl.setVisibility(View.GONE);
                break;

            default:
                fragment = new Fragment();
                break;
        }
        return fragment;
    }

    private class MyLocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(MyActoin.LIVE_FRAGMENT_BACK_LOCAL_ACTION)) {
                    Log.d(TAG, "LIVE_FRAGMENT_BACK_LOCAL_ACTION");
                }
                if (action.equals(MyActoin.ABOUT_FRAGMENT_BACK_LOCAL_ACTION)) {
                    Log.d(TAG, "ABOUT_FRAGMENT_BACK_LOCAL_ACTION");
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
    }

    public Ts getTs() {
        return mTs;
    }

    public List<Program> getProgramList() {
        return mProgramList;
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
        List<Program> list = new ArrayList<>();
        for (Program program : mProgramList) {
            if (program.getIsFavorite()) {
                list.add(program);
            }
        }
        String json = new Gson().toJson(list);
        String key = new File(mTs.getFilePath()).getName();
        SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
        editor.putString(key, json);
        editor.apply();
    }

}
