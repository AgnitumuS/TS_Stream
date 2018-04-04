package com.tosmart.tsgetsdt.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tosmart.tsgetsdt.R;
import com.tosmart.tsgetsdt.beans.tables.SdtService;

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
    private List<SdtService> mList;

    class MyViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        TextView serviceIdTv;
        TextView serviceNameTv;

        public MyViewHolder(View v) {
            super(v);
            itemView = v;
            serviceIdTv = v.findViewById(R.id.tv_service_id);
            serviceNameTv = v.findViewById(R.id.tv_service_name);
        }
    }

    public ProgramListAdapter(Context context, List<SdtService> list) {
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
        String str = "0x" + toHexString(mList.get(position).getServiceId());
        String strResult = mContext.getResources().getString(R.string.program_list_item_tv_service_id_result);
        str = String.format(strResult, str);
        myViewHolder.serviceIdTv.setText(str);

        str = mList.get(position).getServiceName();
        strResult = mContext.getResources().getString(R.string.program_list_item_tv_service_name_result);
        str = String.format(strResult, str);
        myViewHolder.serviceNameTv.setText(str);

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
