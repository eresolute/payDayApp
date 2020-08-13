package com.fh.payday.views.shared;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.datasource.models.Item;
import com.fh.payday.utilities.OnItemClickListener;

import java.util.List;

public class IconListAdapter extends RecyclerView.Adapter<IconListAdapter.ViewHolder> {

    private final List<Item> itemList;
    OnItemClickListener listener;

    public IconListAdapter(List<Item> itemList, OnItemClickListener listener) {
        this.itemList = itemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindTo(itemList.get(position));

        holder.layout.setOnClickListener(view -> listener.onItemClick(position));
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvItem;
        private LinearLayout layout;

        ViewHolder(@NonNull View view) {
            super(view);
            tvItem = view.findViewById(R.id.tv_item);
            layout = view.findViewById(R.id.linear_layout);
        }

        void bindTo(Item item) {
            tvItem.setText(item.getName());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                tvItem.setCompoundDrawablesRelativeWithIntrinsicBounds(item.getRes(), 0, 0, 0);
            } else {
                tvItem.setCompoundDrawablesWithIntrinsicBounds(item.getRes(), 0, 0, 0);
            }
        }
    }
}
