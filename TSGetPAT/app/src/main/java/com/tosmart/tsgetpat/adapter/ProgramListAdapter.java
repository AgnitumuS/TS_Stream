package com.tosmart.tsgetpat.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tosmart.tsgetpat.R;
import com.tosmart.tsgetpat.beans.tables.PatProgram;

import java.util.List;

import static java.lang.Integer.toHexString;

/**
 * ProgramListAdapter
 *
 * @author ggz
 * @date 2018/3/27
 */

public class ProgramListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<PatProgram> mList;

    class MyViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        TextView programNumTv;
        TextView programMapPidTv;

        public MyViewHolder(View v) {
            super(v);
            itemView = v;
            programNumTv = v.findViewById(R.id.tv_program_number);
            programMapPidTv = v.findViewById(R.id.tv_program_map_pid);
        }
    }

    public ProgramListAdapter(Context context, List<PatProgram> list) {
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
                .inflate(R.layout.program_list_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MyViewHolder myViewHolder = (MyViewHolder) holder;
        String str = "0x" + toHexString(mList.get(position).getProgramNumber());
        String strResult = mContext.getResources().getString(R.string.program_list_item_tv_program_number_result);
        str = String.format(strResult, str);
        myViewHolder.programNumTv.setText(str);

        str = "0x" + toHexString(mList.get(position).getProgramMapPid());
        strResult = mContext.getResources().getString(R.string.program_list_item_tv_program_map_pid_result);
        str = String.format(strResult, str);
        myViewHolder.programMapPidTv.setText(str);

        myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "prepare to parse PMT info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
