package com.fh.payday.views.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.datasource.models.StepperItem;

import java.util.List;

public class StepperAdapter extends RecyclerView.Adapter<StepperAdapter.ViewHolder> {

    private int selectedItem;
    private final List<StepperItem> items;

    public StepperAdapter(@NonNull List<StepperItem> items, int defaultSelectedItem) {
        this.items = items;
        this.selectedItem = defaultSelectedItem;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stepper_item, parent, false);
        return new  ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindTo(items.get(position), position == selectedItem);
    }

    public void setSelectedItem(int position) {
        selectedItem = position;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        private ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text_view);
            imageView = itemView.findViewById(R.id.image_view);
        }

        private void bindTo(StepperItem item, boolean isSelected) {
            textView.setText(item.getName());
            if (isSelected) {
                imageView.setImageResource(item.getSelectedIcon());
            } else {
                imageView.setImageResource(item.getIcon());
            }
        }
    }
}