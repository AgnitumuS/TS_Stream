package com.excellence.iptv.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.excellence.iptv.MainActivity;
import com.excellence.iptv.PlayerActivity;
import com.excellence.iptv.R;
import com.excellence.iptv.adapter.ProgramListAdapter;
import com.excellence.iptv.bean.Program;

import java.util.ArrayList;
import java.util.List;

/**
 * LiveFragment
 *
 * @author ggz
 * @date 2018/4/3
 */

public class LiveFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "LiveFragment";
    public static final String KEY_TS = "ts";
    public static final String KEY_PROGRAM_NUM = "programNum";

    private View mView;
    private MainActivity mMainActivity;

    private List<Program> mProgramList = new ArrayList<>();
    private ProgramListAdapter mProgramListAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.live_fragment, container, false);
        mMainActivity = (MainActivity) getActivity();

        // 从 Activity 获取节目列表数据
        mProgramList = mMainActivity.getProgramList();

        initView(mView);

        initRecyclerView(mView);

        return mView;
    }

    private void initView(View v) {
        LinearLayout searchLl = v.findViewById(R.id.ll_search);
        searchLl.setOnClickListener(this);

        ImageView favouriteIv = v.findViewById(R.id.iv_favorite);
        favouriteIv.setOnClickListener(this);
        AnimationSet animationSet = (AnimationSet) AnimationUtils
                .loadAnimation(mMainActivity, R.anim.view_scale_in);
        favouriteIv.startAnimation(animationSet);
    }

    private void initRecyclerView(View v) {
        RecyclerView recyclerView = v.findViewById(R.id.rv_program_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mMainActivity);
        recyclerView.setLayoutManager(layoutManager);
        mProgramListAdapter = new ProgramListAdapter(mMainActivity, mProgramList);
        recyclerView.setAdapter(mProgramListAdapter);

        AnimationSet animationSet = (AnimationSet) AnimationUtils
                .loadAnimation(mMainActivity, R.anim.view_translate_in);
        recyclerView.startAnimation(animationSet);

        mProgramListAdapter.setOnItemClickListener(new ProgramListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                int programNum = mProgramList.get(position).getProgramNumber();

                Intent intent = new Intent(mMainActivity, PlayerActivity.class);
                intent.putExtra(KEY_TS, mMainActivity.getTs());
                intent.putExtra(KEY_PROGRAM_NUM, programNum);
                mMainActivity.startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_search:
                mMainActivity.showContent(MainActivity.SEARCH_FRAGMENT);
                break;

            case R.id.iv_favorite:
                mMainActivity.showContent(MainActivity.FAVORITE_FRAGMENT);
                break;

            default:
                break;
        }
    }
}
