package com.fh.payday.views2.support.adapter;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fh.payday.R;
import com.fh.payday.databinding.ItemIssueBinding;
import com.fh.payday.datasource.models.Item;
import com.fh.payday.utilities.OnItemClickListener;

import java.util.List;

public class SupportIssueAdapter extends RecyclerView.Adapter<SupportIssueAdapter.ViewHolder> {
    private final List<Item> itemList;
    private final OnItemClickListener listener;

    public SupportIssueAdapter(List<Item> itemList, OnItemClickListener listener) {
        this.itemList = itemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        ItemIssueBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(
                        parent.getContext()),
                R.layout.item_issue, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindTo(itemList.get(position), view -> listener.onItemClick(position));
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ItemIssueBinding binding;

        public ViewHolder(@NonNull ItemIssueBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bindTo(Item item, View.OnClickListener listener) {
            binding.setIssue(item);
            binding.setClickListener(listener);
        }
    }
}
