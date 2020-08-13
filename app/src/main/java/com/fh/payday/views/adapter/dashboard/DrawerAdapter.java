package com.fh.payday.views.adapter.dashboard;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fh.payday.R;
import com.fh.payday.databinding.DrawerItemBinding;
import com.fh.payday.datasource.models.Item;
import com.fh.payday.utilities.OnItemClickListener;

import java.util.List;

/**
 * PayDayFH
 * Created by EResolute on 9/3/2018.
 */
public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.ViewHolder> {

    private final List<Item> items;
    private final OnItemClickListener listener;

    public DrawerAdapter(@NonNull OnItemClickListener listener,  @NonNull List<Item> drawerItems) {
        this.listener = listener;
        this.items = drawerItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        DrawerItemBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.drawer_item,
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindTo(
                items.get(position),
                position == items.size() - 1,
                view -> listener.onItemClick(position)
        );
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private DrawerItemBinding binding;

        ViewHolder(@NonNull DrawerItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bindTo(Item item, boolean isViewVisible, View.OnClickListener listener) {
            binding.setItem(item);
            binding.setIsViewVisible(isViewVisible);
            binding.setClickListener(listener);
        }
    }
}
