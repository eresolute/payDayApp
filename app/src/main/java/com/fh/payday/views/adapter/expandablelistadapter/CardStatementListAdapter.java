package com.fh.payday.views.adapter.expandablelistadapter;

import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.datasource.models.StatementHistory;
import com.fh.payday.utilities.DateTime;
import com.fh.payday.utilities.NumberFormatterKt;

import java.util.List;

public class CardStatementListAdapter extends BaseExpandableListAdapter {

    private List<StatementHistory> statements;

    public CardStatementListAdapter(List<StatementHistory> statements) {
        this.statements = statements;
    }

    @Override
    public int getGroupCount() {
        return statements.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return 1;
    }

    @Override
    public Object getGroup(int i) {
        return statements.get(i);
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        return statements.get(listPosition);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean isExpanded, View convertView, ViewGroup parent) {
        StatementHistory transactionDetails = (StatementHistory) getGroup(i);
        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_transaction_option, parent, false);

        TextView tvOption = convertView.findViewById(R.id.tv_card_transaction_option);
        TextView tvDate = convertView.findViewById(R.id.tv_card_transaction_date);
        TextView tvAmount = convertView.findViewById(R.id.tv_card_transaction_amount);
        ImageView imageView = convertView.findViewById(R.id.image_view);
        View view = convertView.findViewById(R.id.view);
        LinearLayout linearLayout = convertView.findViewById(R.id.linear_layout);

        tvDate.setText(DateTime.Companion.parse(transactionDetails.getTransactionDateTime()));
        tvAmount.setText(transactionDetails.getTransactionAmount());
        tvOption.setText("");

        if (isExpanded) {
            imageView.setVisibility(View.GONE);
            view.setVisibility(View.GONE);
            linearLayout.setBackgroundColor(ContextCompat.getColor(convertView.getContext(), R.color.grey_300));
        } else {
            imageView.setVisibility(View.VISIBLE);
            view.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    @Override
    public View getChildView(int listPosition, int expandedListPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        StatementHistory transactionDetails = (StatementHistory) getGroup(listPosition);
        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card_transaction_detail, parent, false);

        TextView tvDate = convertView.findViewById(R.id.tv_date);
        TextView tvBank = convertView.findViewById(R.id.tv_beneficiary_bank);
        TextView tvName = convertView.findViewById(R.id.tv_beneficiary_name);
        TextView tvAccountNumber = convertView.findViewById(R.id.tv_account_no);
        TextView tvAmount = convertView.findViewById(R.id.tv_amount);

        tvDate.setText(DateTime.Companion.parse(transactionDetails.getTransactionDateTime()));
        tvAmount.setText(String.format(convertView.getContext().getString(R.string.amount_in_aed),
                NumberFormatterKt.getDecimalValue(transactionDetails.getTransactionAmount())));
        tvBank.setText("-");
        tvName.setText("-");
        tvAccountNumber.setText("-");

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}
