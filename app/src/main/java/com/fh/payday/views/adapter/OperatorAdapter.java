package com.fh.payday.views.adapter;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fh.payday.R;
import com.fh.payday.databinding.OperatorItemBinding;
import com.fh.payday.datasource.models.Item;
import com.fh.payday.utilities.OnItemClickListener;

import java.util.List;

public class OperatorAdapter extends RecyclerView.Adapter<OperatorAdapter.ViewHolder> {
    private final List<Item> itemList;
    private final OnItemClickListener listener;
    private final int rows;

    public OperatorAdapter(OnItemClickListener listener, List<Item> itemList, int rows) {
        this.listener = listener;
        this.itemList = itemList;
        this.rows = rows;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        OperatorItemBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.operator_item, parent, false);
        return new ViewHolder(binding, parent.getHeight(), rows);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindTo(view -> listener.onItemClick(position), itemList.get(position));
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private OperatorItemBinding binding;

        public ViewHolder(@NonNull OperatorItemBinding binding, int height, int rows) {
            super(binding.getRoot());
            this.binding = binding;
            this.itemView.setMinimumHeight(height / rows);
        }

        void bindTo(View.OnClickListener listener, Item item) {
            binding.setItem(item);
            binding.setListener(listener);
        }
    }

}
