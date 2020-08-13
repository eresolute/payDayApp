package com.fh.payday.views.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.fh.payday.R;
import com.fh.payday.datasource.models.moneytransfer.Beneficiary;
import com.fh.payday.utilities.TextInputUtilsKt;
import com.fh.payday.utilities.TextViewUtilsKt;
import com.fh.payday.views2.moneytransfer.InternationalMoneyTransfer;

public class BeneficiaryDialog extends DialogFragment {
    static Beneficiary mBeneficiary;
    LayoutInflater inflater;
    private AlertDialog.Builder builder;
    private static BeneficiaryDialog.OnBeneficiaryClickListener mConfirmListener;
    private InternationalMoneyTransfer activity;

    public static BeneficiaryDialog newInstance(Beneficiary beneficiary, OnBeneficiaryClickListener confirmListener) {

        mBeneficiary = beneficiary;
        mConfirmListener = confirmListener;
        return new BeneficiaryDialog();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (InternationalMoneyTransfer) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View v = inflater.inflate(R.layout.dialog_beneficiary, null);
        TextView tvName = v.findViewById(R.id.tv_name);
        TextView tvMobile = v.findViewById(R.id.tv_mobile);
        TextView tvConfirm = v.findViewById(R.id.tvConfirm);
        CheckBox checkBox = v.findViewById(R.id.checkBox);

        builder = new AlertDialog.Builder(getActivity());
        Dialog dialog = builder.create();

        if (!mBeneficiary.getEnabled()) {
            activity.showMessage(getString(R.string.invalid_beneficiary),
                    R.drawable.ic_error,
                    R.color.colorError,
                    Dialog::dismiss);
        } else {
            tvName.setText(mBeneficiary.getBeneficiaryName());
            TextViewUtilsKt.replaceZero(tvMobile, mBeneficiary.getMobileNumber());

            builder = new AlertDialog.Builder(getActivity());
            builder.setView(v).setCancelable(true);
            dialog = builder.create();
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


            Dialog finalDialog = dialog;
            tvConfirm.setOnClickListener(view -> {
                mConfirmListener.onBeneficiaryClick(mBeneficiary, checkBox.isChecked());
                finalDialog.dismiss();
            });
        }

        return dialog;
    }

    public interface OnBeneficiaryClickListener {
        void onBeneficiaryClick(Beneficiary beneficiary, boolean isChecked);
    }
}
