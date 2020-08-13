package com.fh.payday.views2.loan.apply;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fh.payday.R;

public class LoanDialogFragment extends DialogFragment {
    private static String mMessage,mTitle;
    private AlertDialog.Builder builder;
    private LayoutInflater inflater;
    public static LoanDialogFragment newInstance(String title, String message) {
        mMessage = message;
        mTitle = title;
        return new LoanDialogFragment();
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_loan, null);
        TextView tvLoanTitle = v.findViewById(R.id.tv_eligible_title);
        TextView tvLoanAmount = v.findViewById(R.id.tv_eligible_amount);
        Button btnInterested = v.findViewById(R.id.btn_interested);
        Button btnNotInterested = v.findViewById(R.id.btn_not_interested);

        builder = new AlertDialog.Builder(getActivity());
        builder.setView(v).setCancelable(true);
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER | Gravity.TOP);
        tvLoanTitle.setText(mTitle);
        tvLoanAmount.setText(mMessage);
        btnInterested.setOnClickListener(view -> {
            dialog.dismiss();
            startActivity(new Intent(getActivity(), LoanOfferActivity.class));
        });
        btnNotInterested.setOnClickListener(view -> dialog.dismiss());
        return dialog;
    }
}
