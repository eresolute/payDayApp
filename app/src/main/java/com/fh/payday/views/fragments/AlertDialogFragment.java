package com.fh.payday.views.fragments;


import android.annotation.SuppressLint;
import android.app.Dialog;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.fh.payday.R;

public class AlertDialogFragment extends DialogFragment {
    static String mMessage;
    static int mIcon;
    LayoutInflater inflater;
    private AlertDialog.Builder builder;
    private static OnConfirmListener mConfirmListener;
    private static int mTintColor;

    public static AlertDialogFragment newInstance(String message, int icon) {
        mMessage = message;
        mIcon = icon;
        mConfirmListener = null;
        mTintColor = R.color.blue;
        return new AlertDialogFragment();
    }

    public static AlertDialogFragment newInstance(String message, int icon, int tintColor, OnConfirmListener confirmListener) {
        mMessage = message;
        mIcon = icon;
        mTintColor = tintColor;
        mConfirmListener = confirmListener;
        return new AlertDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        View v = inflater.inflate(R.layout.dialog_layout_alert, null);
        TextView tvMessage = v.findViewById(R.id.tv_alert_message);
        TextView tvAction = v.findViewById(R.id.tv_action);
        ImageView icon = v.findViewById(R.id.img_alert);

        tvMessage.setText(mMessage);
        icon.setImageResource(mIcon);
        icon.setColorFilter(ContextCompat.getColor(getActivity(), mTintColor), android.graphics.PorterDuff.Mode.MULTIPLY);

        builder = new AlertDialog.Builder(getActivity());
        builder.setView(v).setCancelable(true);
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (mConfirmListener == null) {
            tvAction.setOnClickListener(view -> getActivity().finish());
        } else {
            tvAction.setOnClickListener(view -> mConfirmListener.onConfirm(dialog));
        }
        return dialog;
    }

    public interface OnConfirmListener {
        void onConfirm(Dialog dialog);
    }
}