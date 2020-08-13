package com.fh.payday.views.adapter;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fh.payday.R;
import com.fh.payday.databinding.ItemAtmAddressBinding;
import com.fh.payday.datasource.models.AtmLocator;
import com.fh.payday.utilities.OnItemClickListener;

import java.util.List;

public class AtmLocationsAdapter extends RecyclerView.Adapter<AtmLocationsAdapter.ViewHolder> {

    private List<AtmLocator> locators;
    private OnItemClickListener listener;

    public AtmLocationsAdapter(List<AtmLocator> locators, OnItemClickListener listener) {
        this.locators = locators;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        ItemAtmAddressBinding binder = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.item_atm_address,
                parent,
                false);
        return new ViewHolder(binder);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindTo(locators.get(position), view -> listener.onItemClick(position));
    }

    @Override
    public int getItemCount() {
        return locators.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemAtmAddressBinding binding;

        public ViewHolder(@NonNull ItemAtmAddressBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bindTo(AtmLocator atmData, View.OnClickListener listener) {
            binding.setAtmData(atmData);
            binding.setListener(listener);
        }
    }
}
