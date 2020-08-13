package com.fh.payday.views.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.datasource.models.transactionhistory.Transactions;
import com.fh.payday.utilities.DateTime;

import java.util.List;

/**
 * PayDayFH
 * Created by EResolute on 10/22/2018.
 */
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private final List<Transactions> transactions;
    private int transactionType;

    public TransactionAdapter(@NonNull List<Transactions> transactions, int transactionType) {
        this.transactions = transactions;
        this.transactionType = transactionType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
       /* TransactionItemBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.transaction_item,
                parent,
                false
        );*/

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_item,
                parent,
                false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindTo(transactions.get(position));
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        /*private TransactionItemBinding binding;*/
        TextView tvTitle;
        TextView tvDate;
        TextView tvAmount;
        TextView tvCurrency;

        ViewHolder(@NonNull/* TransactionItemBinding binding*/ View view) {
            /*super(binding.getRoot());
            this.binding = binding;*/
            super(view);
            tvTitle = view.findViewById(R.id.tv_title);
            tvDate = view.findViewById(R.id.tv_date);
            tvAmount = view.findViewById(R.id.tv_amount);
            tvCurrency = view.findViewById(R.id.tv_currency);
        }

        void bindTo(Transactions transaction) {
            if (transaction == null) return;

            if (transactionType == 1) {
//                if (transaction.getTransactionType().equalsIgnoreCase("C"))
//                    tvTitle.setText(tvTitle.getContext().getString(R.string.credit));
//                else
//                    tvTitle.setText(tvTitle.getContext().getString(R.string.debit));

                tvTitle.setText(transaction.getTransactionDescription());
                tvDate.setText(DateTime.Companion.parse(transaction.getTransactionDateTime(),"yyyy-MM-dd'T'HH:mm:ss.SSSZ", "dd MMM yyyy hh:mm a"));

            } else {
                tvTitle.setText(DateTime.Companion.parse(transaction.getTransactionDateTime(), "yyyy-MM-dd", "dd MMMM"));
                tvDate.setText(DateTime.Companion.parse(transaction.getTransactionDateTime(), "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "dd/MM/yyyy hh:mm a"));
            }

            tvAmount.setText(transaction.getTransactionAmount());

            //TODO: CREATE A GENERAL METHOD FOR CURRENCY ABBRV.
            if (Integer.parseInt(transaction.getTransactionCurrency()) == 784)
                tvCurrency.setText(tvCurrency.getContext().getString(R.string.aed));
        }
    }
}
