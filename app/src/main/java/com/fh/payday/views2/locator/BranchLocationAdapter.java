package com.fh.payday.views2.locator;

import android.graphics.PorterDuff;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.datasource.models.BranchLocator;
import com.fh.payday.preferences.LocalePreferences;

import java.util.List;

public class BranchLocationAdapter extends BaseExpandableListAdapter {
    private List<BranchLocator> branchLocators;

    public BranchLocationAdapter(List<BranchLocator> branchLocators) {
        this.branchLocators = branchLocators;
    }

    @Override
    public int getGroupCount() {
        return branchLocators.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return 1;
    }

    @Override
    public Object getGroup(int i) {
        return branchLocators.get(i);
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        return branchLocators.size();
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
    public View getGroupView(int listPosition, boolean isExpanded, View view, ViewGroup parent) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_branch_location, parent, false);
        ConstraintLayout rootLayout = view.findViewById(R.id.root_view);
        TextView tvAddress = view.findViewById(R.id.tv_address);
        TextView tvName = view.findViewById(R.id.tv_country);
        TextView tvTiming = view.findViewById(R.id.tv_timing);
        ImageView ivLocator = view.findViewById(R.id.iv_locator);
        ImageView ivPlus = view.findViewById(R.id.iv_plus);

        tvAddress.setText(branchLocators.get(listPosition).getBranchName());
        tvName.setText(String.format("%s - %s", branchLocators.get(listPosition).getCity(),
                branchLocators.get(listPosition).getCountry()));
        tvTiming.setText(String.format("%s (%s - %s)", branchLocators.get(listPosition).getWorkingDays(), branchLocators.get(listPosition).getFromTime(),
                branchLocators.get(listPosition).getToTime()));

        if (isExpanded) {
            ivLocator.setColorFilter(ContextCompat.getColor(parent.getContext(), R.color.white), PorterDuff.Mode.SRC_IN);
            ivPlus.setColorFilter(ContextCompat.getColor(parent.getContext(), R.color.white), PorterDuff.Mode.SRC_IN);
            rootLayout.setBackground(ContextCompat.getDrawable(parent.getContext(), R.drawable.bg_blue_gradient));
            tvAddress.setTextColor(parent.getResources().getColor(R.color.white));
            tvName.setTextColor(parent.getResources().getColor(R.color.white));
            tvTiming.setTextColor(parent.getResources().getColor(R.color.white));
        } else {
            rootLayout.setBackground(ContextCompat.getDrawable(parent.getContext(), R.drawable.bg_grey));
            ivPlus.setImageDrawable(ContextCompat.getDrawable(parent.getContext(), R.drawable.ic_plus));
            tvAddress.setTextColor(parent.getResources().getColor(R.color.textColor));
            tvName.setTextColor(parent.getResources().getColor(R.color.textColor));
            tvTiming.setTextColor(parent.getResources().getColor(R.color.textColor));
        }
        //   ivPlus.setOnClickListener(v -> listener.onItemClick(listPosition));
        return view;
    }

    @Override
    public View getChildView(int listPosition, int i1, boolean b, View view, ViewGroup parent) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_branch_location_detail, parent, false);
        TextView tvAddress = view.findViewById(R.id.tv_address_detail);
        TextView tvTollFree = view.findViewById(R.id.tv_toll_free);
        TextView tvTelephone = view.findViewById(R.id.tv_telephone);
        TextView tvFax = view.findViewById(R.id.tv_fax);
        TextView tvEmail = view.findViewById(R.id.tv_email);

        tvAddress.setText(branchLocators.get(listPosition).getAddress());
        String locale = LocalePreferences.Companion.getInstance().getLocale(view.getContext());
        if (locale.equals("ar") || locale.equals("ur")) {
            tvTollFree.append(" " + branchLocators.get(listPosition).getTollFree().replaceAll("[\\s+]", ""));
            tvTelephone.append(" " + branchLocators.get(listPosition).getTelephone().replaceAll("[\\s+]", ""));
            tvFax.append(" " + branchLocators.get(listPosition).getFax().replaceAll("[\\s+]", ""));
        } else {
            tvTollFree.append(" " + branchLocators.get(listPosition).getTollFree());
            tvTelephone.append(" " + branchLocators.get(listPosition).getTelephone());
            tvFax.append(" " + branchLocators.get(listPosition).getFax());
        }
        tvEmail.append(branchLocators.get(listPosition).getEmailID());
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}
