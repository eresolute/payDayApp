package com.fh.payday.views2.loan;

import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.datasource.models.loan.LoanRequest;
import com.fh.payday.utilities.DateTime;

import java.util.List;

public class LoanRequestAdapter extends BaseExpandableListAdapter {

    private List<LoanRequest> list;
    private LoanServiceRequestActivity activity;
    private LoanServiceRequestActivity.TYPE label;

    public LoanRequestAdapter(
        LoanServiceRequestActivity activity,
        List<LoanRequest> list,
        LoanServiceRequestActivity.TYPE label
    ) {
        this.activity = activity;
        this.list = list;
        this.label = label;
    }

    public void addItem(LoanRequest loanRequest){
        list.add(0, loanRequest);
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return list.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return list.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return list.get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        LoanRequest request = (LoanRequest) getGroup(groupPosition);
        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_loan_request_group, parent, false);

        TextView tvRequestId = convertView.findViewById(R.id.tv_loan_request_id);
        TextView tvDate = convertView.findViewById(R.id.tv_loan_date);
        ImageView imgArrow = convertView.findViewById(R.id.img_arrow);

        if (isExpanded) {
            imgArrow.setImageDrawable(ContextCompat.getDrawable(parent.getContext(), R.drawable.ic_arrow_down_blue));
        } else {
            imgArrow.setImageDrawable(ContextCompat.getDrawable(parent.getContext(), R.drawable.ic_arrow_right));
        }

        tvRequestId.setText(request.getRequestId());
        tvDate.setText(DateTime.Companion.parse(request.getDateTime(),"yyyy-MM-dd'T'HH:mm:ss.SSSZ","dd-MM-yyyy"));

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        LoanRequest request = (LoanRequest) getChild(groupPosition, childPosition);
        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_loan_request_child, parent, false);

        TextView tvTime = convertView.findViewById(R.id.tv_loan_time);
        TextView tvStatus = convertView.findViewById(R.id.tv_loan_status);

        tvTime.setText(DateTime.Companion.parse(request.getDateTime(),"yyyy-MM-dd'T'HH:mm:ss.SSSZ","hh:mm a"));

        if (label == LoanServiceRequestActivity.TYPE.LIABILITY
            || label == LoanServiceRequestActivity.TYPE.CLEARANCE) {
            if (request.getFile() != null && request.getStatus().equalsIgnoreCase("Accepted")) {
                tvStatus.setText(convertView.getContext().getString(R.string.download));
                tvStatus.setOnClickListener(view -> activity.downloadFile(request.getFile()));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    tvStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_downloads,0);
                } else {
                    tvStatus.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_downloads,0);
                }
                return convertView;
            }
        }

        tvStatus.setText(request.getStatus());

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
