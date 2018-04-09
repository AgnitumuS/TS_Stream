package com.excellence.iptv.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.excellence.iptv.R;

import java.util.List;

/**
 * TagAdapter
 *
 * @author ggz
 * @date 2018/4/4
 */

public class TagAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "TagAdapter";

    private Context mContext;
    private List<String> mList;

    private OnItemClickListener mOnItemClickListener;

    private class MyViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        TextView tagName;
        ImageView deleteIcon;

        public MyViewHolder(View v) {
            super(v);
            itemView = v;
            tagName = v.findViewById(R.id.tv_search_item_name);
//            deleteIcon = v.findViewById();
        }
    }

    public TagAdapter(Context context, List<String> list) {
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
                .inflate(R.layout.search_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final MyViewHolder myViewHolder = (MyViewHolder) holder;
        String strResult = mContext.getResources().getString(R.string.search_item_tv_tag_name_result);
        strResult = String.format(strResult, mList.get(position));
        myViewHolder.tagName.setText(strResult);
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
