package com.excellence.iptv;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.excellence.iptv.fragment.LiveFragment;
import com.excellence.iptv.view.RobotoRegularTextView;

import java.lang.ref.WeakReference;

/**
 * PlayerActivity
 *
 * @author ggz
 * @date 2018/4/2
 */

public class PlayerActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "PlayerActivity";
    private static final int MESSAGE_SWITCH_BAR = 0;

    private int mProgramNum;
    private String mProgramName;

    private LinearLayout mTitleBarLl;
    private LinearLayout mInfoBarLl;

    private ImageView mPlayerStatusIv;

    private boolean mBarIsShow = true;
    private boolean mIsPlaying = true;

    private MyHandler mHandler = new MyHandler(this);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.player_activity);

        Intent intent = getIntent();
        mProgramNum = intent.getIntExtra(LiveFragment.KEY_PROGRAM_NUM, -1);
        mProgramName = intent.getStringExtra(LiveFragment.KEY_PROGRAM_NAME);

        initView();

        if (mBarIsShow) {
            mHandler.sendEmptyMessageDelayed(MESSAGE_SWITCH_BAR, 5000);
        }
    }

    private void initView() {
        mTitleBarLl = findViewById(R.id.ll_title_bar);
        ImageView backIv = findViewById(R.id.iv_player_back);
        backIv.setOnClickListener(this);
        RobotoRegularTextView titleTv = findViewById(R.id.tv_player_title);
        String str = getResources().getString(R.string.player_tv_title_bar_result);
        str = String.format(str, mProgramNum, mProgramName);
        titleTv.setText(str);

        mInfoBarLl = findViewById(R.id.ll_info_bar);
        mPlayerStatusIv = findViewById(R.id.iv_player_status);
        mPlayerStatusIv.setOnClickListener(this);
        RobotoRegularTextView eitTimeTv = findViewById(R.id.tv_player_eit_time);
        RobotoRegularTextView eitNameTv = findViewById(R.id.tv_program_eit_name);


        ImageView playerIv = findViewById(R.id.iv_player);
        playerIv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_player:
                switchBar();
                mHandler.removeMessages(MESSAGE_SWITCH_BAR);
                if (mBarIsShow) {
                    mHandler.sendEmptyMessageDelayed(MESSAGE_SWITCH_BAR, 5000);
                }
                break;
            case R.id.iv_player_status:
                mPlayerStatusIv.setSelected(mIsPlaying);
                mIsPlaying = !mIsPlaying;
                break;
            case R.id.iv_player_back:
                finish();
                break;
            default:
                break;
        }
    }

    private void switchBar() {
        mBarIsShow = !mBarIsShow;
        if (mBarIsShow) {
            AnimationSet animationSet1 = (AnimationSet) AnimationUtils
                    .loadAnimation(this, R.anim.player_title_bar_show);
            mTitleBarLl.startAnimation(animationSet1);
            mTitleBarLl.setVisibility(View.VISIBLE);

            AnimationSet animationSet2 = (AnimationSet) AnimationUtils
                    .loadAnimation(this, R.anim.player_info_bar_show);
            mInfoBarLl.startAnimation(animationSet2);
            mInfoBarLl.setVisibility(View.VISIBLE);
        } else {
            AnimationSet animationSet1 = (AnimationSet) AnimationUtils
                    .loadAnimation(this, R.anim.player_title_bar_hide);
            mTitleBarLl.startAnimation(animationSet1);
            mTitleBarLl.setVisibility(View.GONE);

            AnimationSet animationSet2 = (AnimationSet) AnimationUtils
                    .loadAnimation(this, R.anim.player_info_bar_hide);
            mInfoBarLl.startAnimation(animationSet2);
            mInfoBarLl.setVisibility(View.GONE);
        }
    }


    private static class MyHandler extends Handler {
        WeakReference<PlayerActivity> mWeakReference;

        public MyHandler(PlayerActivity activity) {
            super();
            mWeakReference = new WeakReference<PlayerActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            PlayerActivity playerActivity = mWeakReference.get();
            if (playerActivity != null) {
                switch (msg.what) {
                    case MESSAGE_SWITCH_BAR:
                        playerActivity.switchBar();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
