package com.fh.payday.views2.transactionhistory;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.datasource.models.TransactionHistoryDetail;

import java.util.List;

public class TransactionHistoryDetailAdapter extends RecyclerView.Adapter<TransactionHistoryDetailAdapter.ViewHolder> {

    private List<TransactionHistoryDetail> items;

    public TransactionHistoryDetailAdapter(List<TransactionHistoryDetail> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.transaction_history_detail_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TransactionHistoryDetail item = items.get(position);
        holder.bindTo(item);
    }

    @Override
    public int getItemCount() {
        if (items == null) {
            return 0;
        }
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvType, tvDate, tvAmount;
        public ViewHolder(View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tv_transaction_type);
            tvDate = itemView.findViewById(R.id.tv_transaction_date);
            tvAmount = itemView.findViewById(R.id.tv_transaction_amount);
        }

        public void bindTo(TransactionHistoryDetail item) {
            //code ..
            tvType.setText(item.getType());
            tvDate.setText(item.getDate());
            tvAmount.setText(item.getAmount());
        }
    }
}