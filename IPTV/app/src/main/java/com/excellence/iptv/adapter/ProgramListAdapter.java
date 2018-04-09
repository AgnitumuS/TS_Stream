package com.excellence.iptv.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.excellence.iptv.R;
import com.excellence.iptv.bean.Program;

import java.util.List;

/**
 * ProgramListAdapter
 *
 * @author ggz
 * @date 2018/4/4
 */

public class ProgramListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "ProgramListAdapter";

    private Context mContext;
    private List<Program> mList;

    private OnItemClickListener mOnItemClickListener;

    private class MyViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        TextView programNumTv;
        TextView programNameTv;
        TextView programEitTimeTv;
        TextView programEitNameTv;
        ImageView addFavIv;

        public MyViewHolder(View v) {
            super(v);
            itemView = v;
            programNumTv = v.findViewById(R.id.tv_program_num);
            programNameTv = v.findViewById(R.id.tv_program_name);
            programEitTimeTv = v.findViewById(R.id.tv_program_eit_time);
            programEitNameTv = v.findViewById(R.id.tv_program_eit_name);
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
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.live_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final MyViewHolder myViewHolder = (MyViewHolder) holder;
        String strResult = mContext.getResources().getString(R.string.live_item_tv_program_num_result);
        strResult = String.format(strResult, mList.get(position).getProgramNumber());
        myViewHolder.programNumTv.setText(strResult);

        strResult = mContext.getResources().getString(R.string.live_item_tv_program_name_result);
        strResult = String.format(strResult, mList.get(position).getProgramName());
        myViewHolder.programNameTv.setText(strResult);

        boolean isFavorite = mList.get(position).getIsFavorite();
        if (isFavorite) {
            myViewHolder.addFavIv.setSelected(true);
        } else {
            myViewHolder.addFavIv.setSelected(false);
        }
        myViewHolder.addFavIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myViewHolder.addFavIv.isSelected()) {
                    mList.get(position).setIsFavorite(false);
                    myViewHolder.addFavIv.setSelected(false);
                } else {
                    mList.get(position).setIsFavorite(true);
                    myViewHolder.addFavIv.setSelected(true);
                }

            }
        });

        if (mOnItemClickListener != null) {
            myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = myViewHolder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(myViewHolder.itemView, pos);

                }
            });
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
