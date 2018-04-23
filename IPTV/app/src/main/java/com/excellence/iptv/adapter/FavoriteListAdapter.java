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
import com.excellence.iptv.view.FavImageView;

import java.util.List;
import java.util.Random;

/**
 * FavoriteListAdapter
 *
 * @author ggz
 * @date 2018/4/4
 */

public class FavoriteListAdapter extends RecyclerView.Adapter<FavoriteListAdapter.MyViewHolder> {
    private static final String TAG = "FavoriteListAdapter";

    private Context mContext;
    private List<Program> mList;

    private boolean isEditMode = false;

    private OnItemClickListener mOnItemClickListener;

    static class MyViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        FavImageView programPicIv;
        TextView programInfoTv;
        ImageView deleteIconIv;

        public MyViewHolder(View v) {
            super(v);
            itemView = v;
            programPicIv = v.findViewById(R.id.iv_favorite_item_program_pic);
            programInfoTv = v.findViewById(R.id.tv_favorite_item_program_info);
            deleteIconIv = v.findViewById(R.id.iv_favorite_item_delete_icon);
        }
    }

    public FavoriteListAdapter(Context context, List<Program> list) {
        super();
        this.mContext = context;
        this.mList = list;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.favorite_item, parent, false);
        final MyViewHolder holder = new MyViewHolder(view);

        holder.deleteIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                mList.get(position).setIsFavorite(false);
                mList.remove(position);
                notifyDataSetChanged();
                if (mList.size() == 0) {
                    editMode(false);
                }
            }
        });


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null && !isEditMode) {
                    int position = holder.getAdapterPosition();
                    mOnItemClickListener.onItemClick(v, position);
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!isEditMode) {
                    editMode(true);
                }
                return true;
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        // 显示节目图片
        int imageNum = mList.get(position).getProgramNumber() % 9;
        holder.programPicIv.setImageResource(R.drawable.bg01 + imageNum);

        // 显示节目信息
        String strResult = mContext.getResources().getString(R.string.favorite_item_tv_program_info_result);
        strResult = String.format(strResult,
                mList.get(position).getProgramNumber(), mList.get(position).getProgramName());
        holder.programInfoTv.setText(strResult);

        // 是否为编辑模式状态
        if (isEditMode) {
            holder.programPicIv.setEditMode(true);
            holder.deleteIconIv.setVisibility(View.VISIBLE);
        } else {
            holder.programPicIv.setEditMode(false);
            holder.deleteIconIv.setVisibility(View.INVISIBLE);
        }
    }

    public void editMode(boolean isRight) {
        isEditMode = isRight;
        notifyDataSetChanged();
    }

    public boolean getIsEditMode() {
        return isEditMode;
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
