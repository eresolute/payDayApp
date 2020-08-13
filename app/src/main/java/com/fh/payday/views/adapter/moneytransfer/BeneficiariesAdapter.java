package com.fh.payday.views.adapter.moneytransfer;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.datasource.models.moneytransfer.Beneficiary;
import com.fh.payday.datasource.models.moneytransfer.PaydayBeneficiary;
import com.fh.payday.utilities.OnItemClickListener;
import com.fh.payday.utilities.TextViewUtilsKt;
import com.fh.payday.views2.moneytransfer.beneificaries.EditBeneficiaryActivity;

import java.util.List;

public class BeneficiariesAdapter extends RecyclerView.Adapter<BeneficiariesAdapter.ViewHolder> {
    private final EditBeneficiaryActivity.OnBeneficiaryClick listener;
    private final EditBeneficiaryActivity.OnBeneficiaryOptionClick<Beneficiary> optionListener;
    List<Beneficiary> list;

    public BeneficiariesAdapter(List<Beneficiary> list, EditBeneficiaryActivity.OnBeneficiaryClick listener, EditBeneficiaryActivity.OnBeneficiaryOptionClick optionListener) {
        this.listener = listener;
        this.list = list;
        this.optionListener = optionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.registered_beneficiaries_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindTo(list.get(position));
        holder.rootView.setOnClickListener(view -> listener.onBeneficiaryClickListener(position, list.get(position)));
        holder.imgOption.setOnClickListener(v -> optionListener.onOptionClick(list.get(position), v));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout rootView;
        TextView name, cardNumber;
        ImageView imgOption;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rootView = itemView.findViewById(R.id.root_view);
            name = itemView.findViewById(R.id.tv_user_name);
            cardNumber = itemView.findViewById(R.id.account_no);
            imgOption = itemView.findViewById(R.id.img_option);
        }

        public void bindTo(Beneficiary beneficiary) {
            name.setText(beneficiary.getBeneficiaryName());
            TextViewUtilsKt.replaceZero(cardNumber,beneficiary.getMobileNumber());
        }
    }

}
