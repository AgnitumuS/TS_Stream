package com.excellence.iptv.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.excellence.iptv.MainActivity;
import com.excellence.iptv.PlayerActivity;
import com.excellence.iptv.R;
import com.excellence.iptv.adapter.FavoriteListAdapter;
import com.excellence.iptv.bean.Program;
import com.excellence.iptv.broadcast.MyActoin;

import java.util.ArrayList;
import java.util.List;

import static com.excellence.iptv.fragment.LiveFragment.KEY_PROGRAM_NUM;
import static com.excellence.iptv.fragment.LiveFragment.KEY_TS;

/**
 * FavoriteFragment
 *
 * @author ggz
 * @date 2018/4/3
 */

public class FavoriteFragment extends Fragment {
    private static final String TAG = "FavoriteFragment";

    private View mView;
    private MainActivity mMainActivity;

    private List<Program> mProgramList;
    private List<Program> mFavoriteList = new ArrayList<>();

    private LocalBroadcastManager mLocalBroadcastManager;
    private MyLocalReceiver mLocalReceiver;
    private IntentFilter mIntentFilter;

    private FavoriteListAdapter mFavoriteListAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.favorite_fragment, container, false);
        mMainActivity = (MainActivity) getActivity();

        // 从 Activity 获取节目列表数据
        mProgramList = mMainActivity.getProgramList();
        initData();

        // 注册本地广播监听器
        initLocalBroadcast();

        initView(mView);

        initFavoriteList(mView);

        return mView;
    }

    private void initData() {
        for (int i = 0; i < mProgramList.size(); i++) {
            if (mProgramList.get(i).getIsFavorite()) {
                mFavoriteList.add(mProgramList.get(i));
            }
        }
    }

    private void initLocalBroadcast() {
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(mMainActivity);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(MyActoin.SEND_FAVORITE_FRAGMENT_LOCAL_ACTION);
        mLocalReceiver = new MyLocalReceiver();
        mLocalBroadcastManager.registerReceiver(mLocalReceiver, mIntentFilter);
    }

    private void initView(View v) {
        // back
        ImageView iv = v.findViewById(R.id.iv_favorite_back);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFavoriteListAdapter.editMode(false);
                Intent intent = new Intent(MyActoin.FAVORITE_FRAGMENT_BACK_LOCAL_ACTION);
                mLocalBroadcastManager.sendBroadcast(intent);
            }
        });
    }

    private void initFavoriteList(View v) {
        Display display = mMainActivity.getWindow().getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        int spanCount = screenWidth / 360;

        RecyclerView recyclerView = v.findViewById(R.id.rv_favorite_list);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(
                spanCount, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        mFavoriteListAdapter = new FavoriteListAdapter(mMainActivity, mFavoriteList);
        recyclerView.setAdapter(mFavoriteListAdapter);

        mFavoriteListAdapter.setOnItemClickListener(new FavoriteListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                int programNum = mFavoriteList.get(position).getProgramNumber();

                Intent intent = new Intent(mMainActivity, PlayerActivity.class);
                intent.putExtra(KEY_TS, mMainActivity.getTs());
                intent.putExtra(KEY_PROGRAM_NUM, programNum);
                mMainActivity.startActivity(intent);
            }
        });
    }


    /**
     * 监听本地广播
     */
    private class MyLocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MyActoin.SEND_FAVORITE_FRAGMENT_LOCAL_ACTION)) {
                if (mFavoriteListAdapter.getIsEditMode()) {
                    mFavoriteListAdapter.editMode(false);
                } else {
                    Intent intent0 = new Intent(MyActoin.FAVORITE_FRAGMENT_BACK_LOCAL_ACTION);
                    mLocalBroadcastManager.sendBroadcast(intent0);
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mLocalBroadcastManager.unregisterReceiver(mLocalReceiver);
    }
}
