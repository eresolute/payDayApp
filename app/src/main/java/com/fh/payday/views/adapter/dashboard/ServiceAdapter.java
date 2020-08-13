package com.fh.payday.views.adapter.dashboard;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fh.payday.R;
import com.fh.payday.databinding.ServiceItemBinding;
import com.fh.payday.datasource.models.Item;
import com.fh.payday.utilities.OnItemClickListener;

import java.util.List;

/**
 * PayDayFH
 * Created by EResolute on 9/5/2018.
 */
public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ViewHolder> {

    private final List<Item> items;
    private final OnItemClickListener listener;

    public ServiceAdapter(@NonNull OnItemClickListener listener, @NonNull List<Item> items) {
        this.listener = listener;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        ServiceItemBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.service_item,
                parent,
                false
        );
        return new ViewHolder(binding, parent.getWidth(), 3);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindTo(items.get(position), view -> listener.onItemClick(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private ServiceItemBinding binding;

        ViewHolder(@NonNull ServiceItemBinding binding, int width, int cols) {
            super(binding.getRoot());
            this.binding = binding;
            this.itemView.setMinimumWidth(width/cols);
        }

        void bindTo(Item item, View.OnClickListener listener) {
            binding.setItem(item);
            binding.setClickListener(listener);
            //setTintedCompoundDrawable(itemView.findViewById(R.id.text_view), item.getRes());
        }

        /*private void setTintedCompoundDrawable(TextView textView, int drawableRes) {
            textView.setCompoundDrawablesWithIntrinsicBounds(
                null,
                tintDrawable(ContextCompat.getDrawable(textView.getContext(), drawableRes),
                    ContextCompat.getColor(textView.getContext(), R.color.white)),
                null,
                null);
        }

        private Drawable tintDrawable(Drawable drawable, @ColorInt int tint) {
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable, tint);
            DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_ATOP);
            return drawable;
        }*/
    }
}
