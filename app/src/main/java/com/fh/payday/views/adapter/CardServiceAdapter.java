package com.fh.payday.views.adapter;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fh.payday.R;
import com.fh.payday.databinding.CardServiceItemBinding;
import com.fh.payday.datasource.models.Item;
import com.fh.payday.utilities.OnItemClickListener;

import java.util.List;


public class CardServiceAdapter extends  RecyclerView.Adapter<CardServiceAdapter.ViewHolder>{

    private final List<Item> items;
    private OnItemClickListener clickListener;

    public CardServiceAdapter(List<Item> items, OnItemClickListener clickListener) {
        this.items = items;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardServiceItemBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.card_service_item,
                parent,
                false
        );

        return new ViewHolder(binding, parent.getHeight(), 2);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindTo(view->clickListener.onItemClick(position), items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final CardServiceItemBinding binding;

        public ViewHolder(CardServiceItemBinding binding, int height, int rows) {
            super(binding.getRoot());
            this.binding = binding;
            this.itemView.setMinimumHeight(height / rows);
        }

        public void bindTo(View.OnClickListener listener, Item item) {
            binding.setListener(listener);
            binding.setItem(item);
        }
    }

}