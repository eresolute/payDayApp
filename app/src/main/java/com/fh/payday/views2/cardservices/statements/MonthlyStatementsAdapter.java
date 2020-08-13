package com.fh.payday.views2.cardservices.statements;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.datasource.models.MonthlyStatement;
import com.fh.payday.utilities.DateTime;
import com.fh.payday.utilities.NumberFormatterKt;

import java.util.List;

public class MonthlyStatementsAdapter extends RecyclerView.Adapter<MonthlyStatementsAdapter.ViewHolder> {

    private final List<MonthlyStatement> statements;

    MonthlyStatementsAdapter(@NonNull List<MonthlyStatement> statements) {
        this.statements = statements;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.monthly_statements_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindTo(statements.get(position));
    }

    @Override
    public int getItemCount() {
        return statements.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tvMonth;
        private TextView tvDebit;
        private TextView tvCredit;

        ViewHolder(@NonNull View view) {
            super(view);

            tvMonth = view.findViewById(R.id.tv_month);
            tvDebit = view.findViewById(R.id.tv_debit);
            tvCredit = view.findViewById(R.id.tv_credit);
        }

        public void bindTo(MonthlyStatement monthlyStatement) {
            tvMonth.setText(DateTime.Companion.getMonth(monthlyStatement.getStatementDate()));
            Context context = itemView.getContext();
            tvDebit.setText(getColoredSpan(context.getString(R.string.dr),
                     NumberFormatterKt.getDecimalValue(monthlyStatement.getTotalDebit()),
                    ContextCompat.getColor(context, R.color.grey_400)));
            tvCredit.setText(getColoredSpan(context.getString(R.string.cr),
                    NumberFormatterKt.getDecimalValue(monthlyStatement.getTotalCredit()),
                    ContextCompat.getColor(context, R.color.grey_400)));
        }

        private SpannableString getColoredSpan(String title, String amount, int color) {
            final String finalText = title + "  " + amount;

            SpannableString coloredSpan = new SpannableString(finalText);
            coloredSpan.setSpan(new ForegroundColorSpan(color), 0, title.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

            return coloredSpan;
        }

    }
}
