package com.fh.payday.views2.transactionhistory;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.datasource.models.transactionhistory.LoanTransaction;
import com.fh.payday.utilities.DateTime;
import com.fh.payday.utilities.NumberFormatterKt;

import java.util.List;

public class LoanTransactionAdapter extends RecyclerView.Adapter<LoanTransactionAdapter.ViewHolder> {

    private List<LoanTransaction> list;
    private TransactionHistoryDetailFragment.OnLoanTransactionClickListener listener;

     LoanTransactionAdapter(List<LoanTransaction> list, TransactionHistoryDetailFragment.OnLoanTransactionClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.transaction_history_detail_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
        if (!list.get(i).getTransactionAmount().equals(""))
        holder.tvAmount.setText(String.format(holder.context.getString(R.string.amount_in_aed), NumberFormatterKt.getDecimalValue(list.get(i).getTransactionAmount())));
        holder.tvDate.setText(DateTime.Companion.parse(list.get(i).getTransactionDate(),
                "yyyy-MM-dd",
                "dd/MM/yyyy"));
        holder.tvType.setText(list.get(i).getTransactionDescription());

        if (list.get(i).getTransactionType().equals("DR")) {
            holder.textView.setText("-");
            holder.textView.setTextColor(ContextCompat.getColor(holder.context,R.color.debit_color));
        } else if (list.get(i).getTransactionType().equals("CR")) {
            holder.textView.setText("+");
            holder.textView.setTextColor(ContextCompat.getColor(holder.context,R.color.credit_color));
        }
//        holder.imageView.setVisibility(View.GONE);
        holder.layout.setOnClickListener(v -> listener.onTransactionClick(list.get(i)));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvDate, tvAmount;
        TextView textView;
        ConstraintLayout layout;
        Context context;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            context =  itemView.getContext();
            tvType = itemView.findViewById(R.id.tv_transaction_type);
            tvDate = itemView.findViewById(R.id.tv_transaction_date);
            tvAmount = itemView.findViewById(R.id.tv_transaction_amount);
            layout = itemView.findViewById(R.id.root_view);
            textView = itemView.findViewById(R.id.iv_arrow);
        }
    }
}
