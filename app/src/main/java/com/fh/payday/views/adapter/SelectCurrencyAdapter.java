package com.fh.payday.views.adapter;

import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fh.payday.R;
import com.fh.payday.databinding.SelectCurrItemBinding;
import com.fh.payday.datasource.models.intlRemittance.CountryCurrency;
import com.fh.payday.utilities.OnItemClickListener;

import java.util.List;

import static com.fh.payday.utilities.UrlUtilsKt.BASE_URL;

/**
 * PayDayFH
 * Created by EResolute on 9/10/2018.
 */
public class SelectCurrencyAdapter extends RecyclerView.Adapter<SelectCurrencyAdapter.ViewHolder>{

    private final List<CountryCurrency> languages;
    private final OnItemClickListener listener;

    public SelectCurrencyAdapter(@NonNull OnItemClickListener listener, @NonNull List<CountryCurrency> languages) {
        this.languages = languages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        SelectCurrItemBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.select_curr_item,
                parent,
                false
        );

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindTo(languages.get(position).getCurrency()+" - "+languages.get(position).getCountry(),
                languages.get(position).getImagePath(),view -> listener.onItemClick(position));
    }

    @Override
    public int getItemCount() {
        return languages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private SelectCurrItemBinding binding;

        ViewHolder(@NonNull SelectCurrItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bindTo(String language, String imageUrl, View.OnClickListener listener) {
            binding.setLanguage(language);
            binding.setListener(listener);
            binding.setImageUrl(imageUrl);
        }
    }
}
