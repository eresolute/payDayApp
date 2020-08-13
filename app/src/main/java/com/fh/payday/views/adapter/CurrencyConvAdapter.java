package com.fh.payday.views.adapter;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fh.payday.R;
import com.fh.payday.databinding.ItemCurrencyRowBinding;
import com.fh.payday.datasource.models.Country;
import com.fh.payday.utilities.OnItemClickListener;

import java.util.List;

public class CurrencyConvAdapter extends RecyclerView.Adapter<CurrencyConvAdapter.ViewHolder> {

    private final List<Country> currencyConvList;
    private final OnItemClickListener listener;

    public CurrencyConvAdapter(List<Country> currencyConvList, OnItemClickListener listener, int id) {
        this.currencyConvList = currencyConvList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        ItemCurrencyRowBinding itemCurrencyRowBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), R.layout.item_currency_row,
                parent, false
        );
        return new ViewHolder(itemCurrencyRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindTo(currencyConvList.get(position), view -> listener.onItemClick(position));
    }

    @Override
    public int getItemCount() {
        return currencyConvList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private ItemCurrencyRowBinding binding;

        ViewHolder(@NonNull ItemCurrencyRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bindTo(Country country, View.OnClickListener listener) {
            binding.setCountry(country);
            binding.setClickListener(listener);
        }
    }
}
