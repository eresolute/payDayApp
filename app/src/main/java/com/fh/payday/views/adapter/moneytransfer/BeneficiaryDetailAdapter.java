package com.fh.payday.views.adapter.moneytransfer;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.utilities.TextViewUtilsKt;

import java.util.List;
import java.util.Map;

public class BeneficiaryDetailAdapter extends RecyclerView.Adapter<BeneficiaryDetailAdapter.ViewHolder> {

    List<Map.Entry<String, String>> list;

    public BeneficiaryDetailAdapter(List<Map.Entry<String, String>> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_beneficiary_detail, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
        holder.tvValue.setText(list.get(i).getValue());
        holder.tvKey.setText(list.get(i).getKey());

        if (list.get(i).getKey().equals("Mobile Number")) {
            String mobileNo = list.get(i).getValue();
            holder.tvValue.setText(String.format(holder.itemView.getContext().getString(R.string.plus_971_sign),
                    mobileNo.startsWith("0") ? mobileNo.substring(1) : mobileNo));
            //TextViewUtilsKt.addHypen(holder.tvValue, list.get(i).getValue());
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvKey, tvValue;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvKey = itemView.findViewById(R.id.tv_key);
            tvValue = itemView.findViewById(R.id.tv_value);
        }
    }
}
