package com.fh.payday.views2.products;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.datasource.models.Option;
import com.fh.payday.utilities.OnItemClickListener;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private final List<Option> products;
    private OnItemClickListener listener;

    ProductAdapter(List<Option> products, OnItemClickListener listener){
        this.products = products;
        this.listener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindTo(products.get(position));
        holder.tvApply.setOnClickListener(view -> listener.onItemClick(position));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvProduct;
        private TextView tvApply;

        ViewHolder(@NonNull View view) {
            super(view);
            tvProduct = view.findViewById(R.id.tv_products);
            tvApply = view.findViewById(R.id.tv_apply);
        }

        void bindTo(Option product) {
            tvProduct.setText(product.getOption());
            tvApply.setText(product.getBtn());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                tvProduct.setCompoundDrawablesWithIntrinsicBounds(product.getImage(),0,0,0);
            } else {
                tvProduct.setCompoundDrawablesWithIntrinsicBounds(product.getImage(),0,0,0);
            }
        }
    }
}
