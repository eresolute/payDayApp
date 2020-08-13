package com.fh.payday.views.adapter.expandablelistadapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.datasource.models.moneytransfer.Beneficiary;
import com.fh.payday.datasource.models.moneytransfer.ui.EditBeneficiaryOptions;
import com.fh.payday.views2.moneytransfer.beneificaries.EditBeneficiaryActivity;

import java.util.List;

public class EditBeneficiaryAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<EditBeneficiaryOptions> editBeneficiaryOptions;
    private ImageView imgExpand;
    private EditBeneficiaryActivity.OnBeneficiaryClick listener;
    private EditBeneficiaryActivity.OnBeneficiaryClick deleteListener;
    private EditBeneficiaryActivity.OnBeneficiaryClick isEnabledListener;


    public EditBeneficiaryAdapter(Context context, List<EditBeneficiaryOptions> editBeneficiaryOptions, EditBeneficiaryActivity.OnBeneficiaryClick listener,
                                  EditBeneficiaryActivity.OnBeneficiaryClick deleteListener, EditBeneficiaryActivity.OnBeneficiaryClick isEnabledListener ) {
        this.context = context;
        this.editBeneficiaryOptions = editBeneficiaryOptions;
        this.listener = listener;
        this.deleteListener = deleteListener;
        this.isEnabledListener = isEnabledListener;
    }

    @Override
    public int getGroupCount() {
        return editBeneficiaryOptions.size();
    }

    @Override
    public int getChildrenCount(int i) {
        if( editBeneficiaryOptions.get(i).getBeneficiaries().size() == 0 )
            return 1;
        else
            return editBeneficiaryOptions.get(i).getBeneficiaries().size();
    }

    @Override
    public Object getGroup(int i) {
        return editBeneficiaryOptions.get(i);
    }

    @Override
    public Object getChild(int listPostion, int expandedListPosition) {
        return editBeneficiaryOptions.get(listPostion).getBeneficiaries();
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
    public View getGroupView(int position, boolean isExpanded, View convertView, ViewGroup parent) {
        EditBeneficiaryOptions beneficiaryOptions = (EditBeneficiaryOptions) getGroup(position);
        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_edit_beneficiary_options, parent, false);

        ImageView iconBeneficiaryOption = convertView.findViewById(R.id.img_beneficiary_option);
        TextView tvOfficeTitle = convertView.findViewById(R.id.tv_beneficiary_option);
        imgExpand = convertView.findViewById(R.id.img_beneficiary_expand);
        LinearLayout layoutOfficeTitle = convertView.findViewById(R.id.layout_edit_beneficiary_options);

//        layoutOfficeTitle.setOnClickListener(view -> listener.onItemClick(position));

        iconBeneficiaryOption.setImageResource(beneficiaryOptions.getIcon());
        tvOfficeTitle.setText(beneficiaryOptions.getOptions());

        if (isExpanded) {
            iconBeneficiaryOption.setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_IN);
            imgExpand.setImageResource(R.drawable.ic_arrow_down);
            tvOfficeTitle.setTextColor(context.getResources().getColor(R.color.white));
            layoutOfficeTitle.setBackground(parent.getContext().getResources().getDrawable(R.drawable.bg_blue_gradient));
        } else {
            imgExpand.setImageResource(R.drawable.ic_collapse);
            iconBeneficiaryOption.setColorFilter(ContextCompat.getColor(context, R.color.blue), PorterDuff.Mode.SRC_IN);
            tvOfficeTitle.setTextColor(context.getResources().getColor(R.color.textColor));
            layoutOfficeTitle.setBackground(parent.getContext().getResources().getDrawable(R.drawable.bg_grey));
        }
        return convertView;
    }

    @Override
    public View getChildView(int listPosition, int expandedListPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final List<Beneficiary> expandedListText = (List<Beneficiary>) getChild(listPosition, expandedListPosition);

        if (expandedListText.size() == 0) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_no_beneficiaries, parent, false);
            return convertView;
        }

        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_beneficiary, parent, false);
        ImageView ivEdit = convertView.findViewById(R.id.iv_edit);
        ImageView ivDelete = convertView.findViewById(R.id.iv_delete);
        ImageView toggleStatus = convertView.findViewById(R.id.toggle_status);
        ivEdit.setOnClickListener(view -> listener.onBeneficiaryClickListener(listPosition, expandedListText.get(expandedListPosition)));
        ivDelete.setOnClickListener(view -> deleteListener.onBeneficiaryClickListener(listPosition, expandedListText.get(expandedListPosition)));

        TextView beneficiaryName = convertView.findViewById(R.id.tv_beneficiary_name);
        TextView beneficiaryAccount = convertView.findViewById(R.id.tv_beneficiary_acnt);
        beneficiaryName.setText(expandedListText.get(expandedListPosition).getBeneficiaryName());
        beneficiaryAccount.setText(expandedListText.get(expandedListPosition).getMobileNumber());
        if (expandedListText.get(expandedListPosition).getEnabled())
            toggleStatus.setImageResource(R.drawable.ic_check);
        else
            toggleStatus.setImageResource(R.drawable.ic_uncheck);

        toggleStatus.setOnClickListener(view -> {
            isEnabledListener.onBeneficiaryClickListener(listPosition,expandedListText.get(expandedListPosition), !expandedListText.get(expandedListPosition).getEnabled());
        });

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

}
