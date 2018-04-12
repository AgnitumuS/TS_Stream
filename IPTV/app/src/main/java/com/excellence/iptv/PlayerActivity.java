package com.excellence.iptv;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.excellence.iptv.bean.Program;
import com.excellence.iptv.bean.Ts;
import com.excellence.iptv.bean.tables.Pmt;
import com.excellence.iptv.fragment.LiveFragment;
import com.excellence.iptv.view.RobotoRegularTextView;

import java.lang.ref.WeakReference;
import java.util.List;

import static java.lang.Integer.toHexString;

/**
 * PlayerActivity
 *
 * @author ggz
 * @date 2018/4/2
 */

public class PlayerActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "PlayerActivity";
    private static final int MESSAGE_SWITCH_BAR = 0;

    private Ts mTs;
    private int mProgramNum;
    private List<Program> mProgramList;
    private List<Pmt> mPmtList;
    private Program mProgram;
    private Pmt mPmt;

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

        // 获取传进来的数据
        Intent intent = getIntent();
        mTs = (Ts) intent.getSerializableExtra(LiveFragment.KEY_TS);
        mProgramNum = intent.getIntExtra(LiveFragment.KEY_PROGRAM_NUM, -1);
        mProgramList = mTs.getProgramList();
        mPmtList = mTs.getPmtList();
        // 匹配节目
        for (int i = 0; i < mProgramList.size(); i++) {
            if (mProgramList.get(i).getProgramNumber() == mProgramNum) {
                mProgram = mProgramList.get(i);
                break;
            }
        }
        // 匹配节目的 PMT 表
        for (int i = 0; i < mPmtList.size(); i++) {
            if (mPmtList.get(i).getProgramNumber() == mProgramNum) {
                mPmt = mPmtList.get(i);
                break;
            }
        }


        initView();
    }

    private void initView() {
        mTitleBarLl = findViewById(R.id.ll_title_bar);
        ImageView backIv = findViewById(R.id.iv_player_back);
        backIv.setOnClickListener(this);
        RobotoRegularTextView titleTv = findViewById(R.id.tv_player_title);
        String str = getResources().getString(R.string.player_tv_title_bar_result);
        str = String.format(str, mProgram.getProgramNumber(), mProgram.getProgramName());
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
                showPopupWindow();
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


    private void showPopupWindow() {
        //  获取屏幕的宽高像素
        Display display = this.getWindow().getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        View view = LayoutInflater.from(this).inflate(R.layout.player_popup_window_pmt, null);

        PopupWindow popupWindow = new PopupWindow(view,
                screenWidth / 5 * 4, screenHeight / 5 * 3, true);
        popupWindow.setContentView(view);

        TextView pmtIdTv = view.findViewById(R.id.tv_pmt_id);
        TextView pmtResultTv = view.findViewById(R.id.tv_pmt_result);
        String str = getResources().getString(R.string.player_popup_widow_tv_pmt_id_result);
        str = String.format(str, toHexString(mProgram.getProgramMapPid()));
        pmtIdTv.setText(str);
        pmtResultTv.setText(mPmt.print());

        // 外部可点击，即点击 PopupWindow 以外的区域，PopupWindow 消失
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);

        // 将 PopupWindow 的实例放在一个父容器中，并定位
        View locationView = LayoutInflater.from(this).inflate(R.layout.player_activity, null);
        popupWindow.showAtLocation(locationView, Gravity.CENTER, 0, 0);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
