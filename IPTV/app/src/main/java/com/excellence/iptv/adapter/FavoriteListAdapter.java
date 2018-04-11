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
 * FavoriteListAdapter
 *
 * @author ggz
 * @date 2018/4/4
 */

public class FavoriteListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "FavoriteListAdapter";

    private Context mContext;
    private List<Program> mList;

    private boolean isEditMode = false;

    private OnItemClickListener mOnItemClickListener;

    private class MyViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        ImageView programPicIv;
        TextView programInfoTv;
        ImageView deleteIconIv;
        ImageView editModeIv;

        public MyViewHolder(View v) {
            super(v);
            itemView = v;
            programPicIv = v.findViewById(R.id.iv_favorite_item_program_pic);
            programInfoTv = v.findViewById(R.id.tv_favorite_item_program_info);
            deleteIconIv = v.findViewById(R.id.iv_favorite_item_delete_icon);
            editModeIv = v.findViewById(R.id.iv_favorite_item_edit_mode);
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
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.favorite_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final MyViewHolder myViewHolder = (MyViewHolder) holder;

        myViewHolder.programPicIv.setImageResource(selectImage(position));

        String strResult = mContext.getResources().getString(R.string.favorite_item_tv_program_info_result);
        strResult = String.format(strResult,
                mList.get(position).getProgramNumber(), mList.get(position).getProgramName());
        myViewHolder.programInfoTv.setText(strResult);

        // 编辑模式图标
        if (isEditMode) {
            myViewHolder.deleteIconIv.setVisibility(View.VISIBLE);
            myViewHolder.editModeIv.setVisibility(View.VISIBLE);
        } else {
            myViewHolder.deleteIconIv.setVisibility(View.INVISIBLE);
            myViewHolder.editModeIv.setVisibility(View.INVISIBLE);
        }
        myViewHolder.deleteIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mList.get(position).setIsFavorite(false);
                mList.remove(position);
                notifyDataSetChanged();
                if (mList.size() == 0) {
                    editMode(false);
                }
            }
        });

        myViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                editMode(true);
                return true;
            }
        });
    }

    public void editMode(boolean isRight) {
        isEditMode = isRight;
        notifyDataSetChanged();
    }

    public boolean getIsEditMode() {
        return isEditMode;
    }

    private int selectImage(int position){
        int imageId = R.drawable.bg01;
        switch (position % 9){
            case 0:
                imageId = R.drawable.bg01;
                break;
            case 1:
                imageId = R.drawable.bg02;
                break;
            case 2:
                imageId = R.drawable.bg03;
                break;
            case 3:
                imageId = R.drawable.bg04;
                break;
            case 4:
                imageId = R.drawable.bg05;
                break;
            case 5:
                imageId = R.drawable.bg06;
                break;
            case 6:
                imageId = R.drawable.bg07;
                break;
            case 7:
                imageId = R.drawable.bg08;
                break;
            case 8:
                imageId = R.drawable.bg09;
                break;
            default:
                break;
        }
        return imageId;
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
