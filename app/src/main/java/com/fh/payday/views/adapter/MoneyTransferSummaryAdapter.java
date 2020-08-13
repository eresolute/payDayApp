package com.fh.payday.views.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.datasource.models.moneytransfer.ui.TransferSummary;

import java.util.List;

public class MoneyTransferSummaryAdapter extends RecyclerView.Adapter<MoneyTransferSummaryAdapter.ViewHolder> {
    private List<TransferSummary> transferSummaryList;
    private int rows;

    public MoneyTransferSummaryAdapter(List<TransferSummary> transferSummaryList, int rows) {
        this.transferSummaryList = transferSummaryList;
        this.rows = rows;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.money_transfer_summary_item, parent, false);
        return new ViewHolder(view, parent.getHeight(), rows);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindTo(transferSummaryList.get(position));
    }

    @Override
    public int getItemCount() {
        return transferSummaryList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvItem, tvDescription;

        public ViewHolder(@NonNull View itemView, int height, int rows) {
            super(itemView);
            tvItem = itemView.findViewById(R.id.tv_item);
            tvDescription = itemView.findViewById(R.id.tv_description);
            this.itemView.setMinimumHeight(height/rows);
        }

        void bindTo(TransferSummary transferSummary) {

            tvItem.setText(transferSummary.getItem());
            tvDescription.setText(transferSummary.getDescription());
        }
    }

}
