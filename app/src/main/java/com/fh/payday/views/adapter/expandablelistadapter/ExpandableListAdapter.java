package com.fh.payday.views.adapter.expandablelistadapter;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.datasource.models.QuestionBank;

import java.util.List;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<QuestionBank> questionBanks;
    private ImageView imgExpand;

    public ExpandableListAdapter(Context context, List<QuestionBank> questionBanks) {
        this.context = context;
        this.questionBanks = questionBanks;
    }

    @Override
    public int getGroupCount() {
        return questionBanks.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return 1;
    }

    @Override
    public Object getGroup(int i) {
        return questionBanks.get(i).getName();
    }

    @Override
    public Object getChild(int listPostion, int expandedListPosition) {
        return questionBanks.get(listPostion).getDetail();
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
        String listTitle = (String) getGroup(i);

        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_office_addr, parent, false);

        TextView tvOfficeTitle = convertView.findViewById(R.id.tvOfficeTitle);
        tvOfficeTitle.setText(listTitle);

        imgExpand = convertView.findViewById(R.id.img_expand);
        ConstraintLayout layoutOfficeTitle = convertView.findViewById(R.id.layout_office_title);


        if (isExpanded) {
            imgExpand.setImageResource(R.drawable.ic_collapse);
            layoutOfficeTitle.setBackground(parent.getContext().getResources().getDrawable(R.drawable.bg_grey));
        }
        else {
            imgExpand.setImageResource(R.drawable.ic_arrow_down);
            layoutOfficeTitle.setBackground(parent.getContext().getResources().getDrawable(R.drawable.bg_grey));
        }
        return convertView;
    }

    @Override
    public View getChildView(int listPosition, int expandedListPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String expandedListText =(String) getChild(listPosition, expandedListPosition);

        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_office_address_detail, parent, false);


        TextView addressDetail = convertView.findViewById(R.id.tv_beneficiary_name);

        addressDetail.setText(expandedListText);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}
