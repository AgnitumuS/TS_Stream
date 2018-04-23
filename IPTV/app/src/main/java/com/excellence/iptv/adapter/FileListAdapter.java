package com.excellence.iptv.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.excellence.iptv.R;
import com.excellence.iptv.view.RobotoMediumTextView;

import java.util.List;


/**
 * FileListAdapter
 *
 * @author ggz
 * @date 2018/4/2
 */

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.MyViewHolder> {

    private Context mContext;
    private List<String> mList;

    private OnItemClickListener mOnItemClickListener;

    static class MyViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        RobotoMediumTextView fileNameTv;

        public MyViewHolder(View v) {
            super(v);
            itemView = v;
            fileNameTv = v.findViewById(R.id.tv_file_name);
        }
    }

    public FileListAdapter(Context context, List<String> list) {
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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.select_file_item, parent, false);
        final MyViewHolder holder = new MyViewHolder(view);

        // itemView 的点击事件
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
        String strResult = mContext.getResources().getString(R.string.select_file_item_tv_file_name);
        strResult = String.format(strResult, mList.get(position));
        holder.fileNameTv.setText(strResult);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    public interface OnItemClickListener {
        /**
         * item click
         *
         * @param view     item view
         * @param position item position
         */
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }
}
