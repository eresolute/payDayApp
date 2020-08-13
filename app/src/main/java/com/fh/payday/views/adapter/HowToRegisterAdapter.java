package com.fh.payday.views.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fh.payday.R;

import java.util.List;

public class HowToRegisterAdapter extends RecyclerView.Adapter<HowToRegisterAdapter.ViewHolder> {
    private final List<String> registerSteps;

    public HowToRegisterAdapter(List<String> registerSteps) {
        this.registerSteps = registerSteps;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.register_step_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindTo(registerSteps.get(position));
    }

    @Override
    public int getItemCount() {
        return registerSteps.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvSteps;

        ViewHolder(@NonNull View view) {
            super(view);
            tvSteps = view.findViewById(R.id.tv_steps);
        }

        void bindTo(String registerStep) {
            tvSteps.setText(registerStep);
        }
    }
}
