package com.fh.payday.views2.message.adapter;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.datasource.models.message.MessageBody;
import com.fh.payday.datasource.models.message.MessageOption;
import com.fh.payday.utilities.DateTime;

import java.util.List;

public class MessageAdapter extends BaseExpandableListAdapter {
    private List<MessageOption> messageOptions;

    public MessageAdapter(List<MessageOption> messageOptions) {
        this.messageOptions = messageOptions;
    }

    @Override
    public int getGroupCount() {
        return messageOptions.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return messageOptions.get(groupPosition).getMessage().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return messageOptions.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return messageOptions.get(groupPosition).getMessage();
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
        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_group, parent, false);
        MessageOption messageOption = (MessageOption) getGroup(groupPosition);

        ConstraintLayout constraintLayout = convertView.findViewById(R.id.parent_view);
        TextView tvTitle = convertView.findViewById(R.id.tv_title);
        ImageView ivArrow = convertView.findViewById(R.id.iv_arrow);
        tvTitle.setText(messageOption.getTitle());
        tvTitle.setCompoundDrawablesWithIntrinsicBounds(messageOption.getDrawableIcon(),0,0,0);

        if (isExpanded) {
            Drawable[] drawable = tvTitle.getCompoundDrawables();
            drawable[0].setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(tvTitle.getContext(), R.color.white), PorterDuff.Mode.SRC_IN));
            tvTitle.setTextColor(parent.getContext().getResources().getColor(R.color.white));
            ivArrow.setImageResource(R.drawable.ic_arrow_down);
            constraintLayout.setBackground(parent.getContext().getResources().getDrawable(R.drawable.bg_blue_gradient));
        } else {
            Drawable[] drawable = tvTitle.getCompoundDrawables();
            drawable[0].setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(tvTitle.getContext(), R.color.blue), PorterDuff.Mode.SRC_IN));
            ivArrow.setImageResource(R.drawable.ic_collapse);
            constraintLayout.setBackground(parent.getContext().getResources().getDrawable(R.drawable.bg_grey));
        }
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_child, parent, false);
        List<MessageBody> msgList = (List<MessageBody>) getChild(groupPosition, childPosition);

        TextView tvMsgId = convertView.findViewById(R.id.tv_id);
        TextView tvMsgDate = convertView.findViewById(R.id.tv_date);
        TextView tvMsgBody = convertView.findViewById(R.id.tv_msg_body);

        String date = DateTime.Companion.parse(msgList.get(childPosition).getCreatedAt(),"yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        tvMsgId.setText(String.valueOf(msgList.get(childPosition).getId()));
        tvMsgDate.setText(date);
        if (msgList.get(childPosition).getIssue().equals("inbox")) {
            tvMsgBody.setText(msgList.get(childPosition).getReply());
        } else {
            tvMsgBody.setText(msgList.get(childPosition).getBody());
        }


        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
