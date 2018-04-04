package com.excellence.iptv;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.excellence.iptv.adapter.ProgramListAdapter;
import com.excellence.iptv.bean.Program;

import java.util.ArrayList;
import java.util.List;

/**
 * FavoriteActivity
 *
 * @author ggz
 * @date 2018/4/4
 */

public class FavoriteActivity extends AppCompatActivity {

    private List<Program> mFavoriteList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorite_activity);

        initRecyclerView();
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.rv_favorite_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        ProgramListAdapter programListAdapter = new ProgramListAdapter(this, mFavoriteList);
        recyclerView.setAdapter(programListAdapter);
    }
}
