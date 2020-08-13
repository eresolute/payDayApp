package com.fh.payday.views.adapter;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fh.payday.R;
import com.fh.payday.databinding.SelectLangItemBinding;
import com.fh.payday.utilities.OnItemClickListener;

/**
 * PayDayFH
 * Created by EResolute on 9/10/2018.
 */
public class SelectLanguageAdapter extends RecyclerView.Adapter<SelectLanguageAdapter.ViewHolder>{

    private final String[] languages;
    private final OnItemClickListener listener;

    public SelectLanguageAdapter(@NonNull OnItemClickListener listener, @NonNull String[] languages) {
        this.languages = languages;
        this.listener = listener;
    }

    public SelectLanguageAdapter(@NonNull String[] languages, @NonNull OnItemClickListener listener) {
        this.listener = listener;
        this.languages = languages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        SelectLangItemBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.select_lang_item,
                parent,
                false
        );

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindTo(languages[position], view -> listener.onItemClick(position));
    }

    @Override
    public int getItemCount() {
        return languages.length;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private SelectLangItemBinding binding;

        ViewHolder(@NonNull SelectLangItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bindTo(String language, View.OnClickListener listener) {
            binding.setLanguage(language);
            binding.setListener(listener);
        }
    }
}
