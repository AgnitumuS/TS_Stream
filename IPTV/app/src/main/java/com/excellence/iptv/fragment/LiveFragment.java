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
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.excellence.iptv.FavoriteActivity;
import com.excellence.iptv.MainActivity;
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
    public static final String KEY_SEARCH_LIST = "searchList";

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
//        if (list != null) {
//            mProgramList.clear();
//            for (Program program : list) {
//                mProgramList.add(program);
//            }
//        }

        initView(mView);

        initRecyclerView(mView);

        return mView;
    }

    private void initView(View v) {
        LinearLayout searchLl = v.findViewById(R.id.ll_search);
        searchLl.setOnClickListener(this);

        ImageView favouriteIv = v.findViewById(R.id.iv_favorite);
        favouriteIv.setOnClickListener(this);
    }

    private void initRecyclerView(View v) {
        RecyclerView recyclerView = v.findViewById(R.id.rv_program_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mMainActivity);
        recyclerView.setLayoutManager(layoutManager);
        mProgramListAdapter = new ProgramListAdapter(mMainActivity, mProgramList);
        recyclerView.setAdapter(mProgramListAdapter);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_search:
                mMainActivity.showContent(MainActivity.SEARCH_FRAGMENT);
                break;

            case R.id.iv_favorite:
                Intent intent2 = new Intent(mMainActivity, FavoriteActivity.class);
                startActivity(intent2);
                break;

            default:
                break;
        }
    }
}
