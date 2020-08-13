package com.fh.payday.views.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.datasource.models.shared.ListModel;

import java.util.List;

public class LoanDetailAdapter extends RecyclerView.Adapter<LoanDetailAdapter.ViewHolder> {

    List<ListModel> loanDetails;

    public LoanDetailAdapter(List<ListModel> loanDetails) {
        this.loanDetails = loanDetails;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loan_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
        holder.tvLoanTitle.setText(loanDetails.get(i).getKey());
        holder.tvLoanDetails.setText(loanDetails.get(i).getValue());
    }

    @Override
    public int getItemCount() {
        return loanDetails.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLoanTitle, tvLoanDetails;
         public ViewHolder(@NonNull View itemView) {
             super(itemView);
             tvLoanTitle = itemView.findViewById(R.id.tv_loan_title);
             tvLoanDetails = itemView.findViewById(R.id.tv_loan_details);
         }
     }


}
