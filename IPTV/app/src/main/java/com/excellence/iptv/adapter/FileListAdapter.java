package com.excellence.iptv.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.excellence.iptv.R;

import java.util.List;


/**
 * FileListAdapter
 *
 * @author ggz
 * @date 2018/4/2
 */

public class FileListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<String> mList;
    private Typeface mTypeface;

    private OnItemClickListener mOnItemClickListener;

    class MyViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        TextView fileNameTv;

        public MyViewHolder(View v) {
            super(v);
            itemView = v;
            fileNameTv = v.findViewById(R.id.tv_file_name);
            fileNameTv.setTypeface(mTypeface);
        }
    }

    public FileListAdapter(Context context, List<String> list, Typeface typeface) {
        super();
        this.mContext = context;
        this.mList = list;
        this.mTypeface = typeface;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.select_file_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final MyViewHolder myViewHolder = (MyViewHolder) holder;

        String strResult = mContext.getResources().getString(R.string.select_file_item_tv_file_name);
        strResult = String.format(strResult, mList.get(position));
        myViewHolder.fileNameTv.setText(strResult);

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
