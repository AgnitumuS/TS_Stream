package com.excellence.iptv.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.excellence.iptv.R;
import com.excellence.iptv.bean.Program;
import com.excellence.iptv.view.RobotoMediumTextView;
import com.excellence.iptv.view.RobotoRegularTextView;

import java.util.List;

/**
 * ProgramListAdapter
 *
 * @author ggz
 * @date 2018/4/4
 */

public class ProgramListAdapter extends RecyclerView.Adapter<ProgramListAdapter.MyViewHolder> {
    private static final String TAG = "ProgramListAdapter";
    private static final String NULL_STRING = "null";

    private Context mContext;
    private List<Program> mList;

    private OnItemClickListener mOnItemClickListener;

    static class MyViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        RobotoMediumTextView programNumTv;
        RobotoMediumTextView programNameTv;
        RobotoRegularTextView programEitInfoTv;
        RelativeLayout addFavRl;
        ImageView addFavIv;

        public MyViewHolder(View v) {
            super(v);
            itemView = v;
            programNumTv = v.findViewById(R.id.tv_program_num);
            programNameTv = v.findViewById(R.id.tv_program_name);
            programEitInfoTv = v.findViewById(R.id.tv_program_eit_info);
            addFavRl = v.findViewById(R.id.rl_add_fav);
            addFavIv = v.findViewById(R.id.iv_add_fav);
        }
    }

    public ProgramListAdapter(Context context, List<Program> list) {
        super();
        this.mContext = context;
        this.mList = list;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.live_item, parent, false);
        final MyViewHolder holder = new MyViewHolder(view);

        // 添加收藏
        holder.addFavRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if (holder.addFavIv.isSelected()) {
                    mList.get(position).setIsFavorite(false);
                    holder.addFavIv.setSelected(false);
                } else {
                    mList.get(position).setIsFavorite(true);
                    holder.addFavIv.setSelected(true);
                }
            }
        });

        // item 点击事件
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    int position = holder.getAdapterPosition();
                    mOnItemClickListener.onItemClick(v, position);
                }
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // 显示 programNum
        String strResult = mContext.getResources().getString(R.string.live_item_tv_program_num_result);
        strResult = String.format(strResult, mList.get(position).getProgramNumber());
        holder.programNumTv.setText(strResult);

        // 显示 programName
        strResult = mContext.getResources().getString(R.string.live_item_tv_program_name_result);
        strResult = String.format(strResult, mList.get(position).getProgramName());
        holder.programNameTv.setText(strResult);

        // 显示 startTime endTime eventName
        String startTime = mList.get(position).getStartTime();
        if (!startTime.equals(NULL_STRING)) {
            startTime = startTime.substring(0, 5);
            String endTime = mList.get(position).getEndTime();
            endTime = endTime.substring(0, 5);
            String eventName = mList.get(position).getEventName();
            strResult = mContext.getResources().getString(R.string.live_item_tv_eit_info_result);
            strResult = String.format(strResult, startTime, endTime, eventName);
            holder.programEitInfoTv.setText(strResult);
        } else {
            strResult = mContext.getResources().getString(R.string.live_item_tv_eit_info);
            holder.programEitInfoTv.setText(strResult);
        }

        // 更新 Favorite 图标
        boolean isFavorite = mList.get(position).getIsFavorite();
        if (isFavorite) {
            holder.addFavIv.setSelected(true);
        } else {
            holder.addFavIv.setSelected(false);
        }

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    public interface OnItemClickListener {
        /**
         * onItemClick
         *
         * @param view     itemView
         * @param position item position
         */
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }
}
