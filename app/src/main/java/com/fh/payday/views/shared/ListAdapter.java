package com.fh.payday.views.shared;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fh.payday.R;
import com.fh.payday.databinding.ListItemBinding;
import com.fh.payday.datasource.models.shared.ListModel;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private List<ListModel> items;
    private Context context;
    private String title;
    private Boolean flag = false;


    public ListAdapter(List<ListModel> items, Context context, String title) {
        this.items = items;
        this.context = context;
        this.title = title;
    }

    public ListAdapter(List<ListModel> items, Context context, String title, Boolean flag) {
        this.items = items;
        this.context = context;
        this.title = title;
        this.flag =  flag;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
        ListItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.list_item, parent, false);

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ListModel item = items.get(position);

        if (item == null) return;
        holder.bindTo(item);
    }

    @Override
    public int getItemCount() {
        if (items == null) {
            return 0;
        }
        return items.size();
    }

    public void addAll(List<ListModel> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ListItemBinding binding;

        public ViewHolder(ListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bindTo(ListModel item) {
            binding.setListItem(item);

            if (flag){
                binding.tvKey.setTextAppearance(context,R.style.AppTheme_Text_Small);
                binding.tvValue.setTextAppearance(context, R.style.AppTheme_Text_Small_Bold);
            }

            if (title.equalsIgnoreCase(context.getString(R.string.overdraft))) {
                binding.tvKey.setTextColor(context.getResources().getColor(R.color.black));
                binding.tvValue.setTextColor(context.getResources().getColor(R.color.black));
            }

            if (item.getValue().equalsIgnoreCase("InActive"))
                binding.tvValue.setTextColor(context.getResources().getColor(R.color.color_red));
            else if (item.getValue().equalsIgnoreCase("Active")) {
                binding.tvValue.setTextColor(context.getResources().getColor(R.color.verified));
            }

            if (TextUtils.isEmpty(item.getKey())) {
                binding.tvKey.setVisibility(View.GONE);
            } else {
                binding.tvKey.setVisibility(View.VISIBLE);
            }

            if (TextUtils.isEmpty(item.getValue())) {
                binding.tvValue.setVisibility(View.GONE);
            } else {
                binding.tvValue.setVisibility(View.VISIBLE);
            }

        }
    }
}