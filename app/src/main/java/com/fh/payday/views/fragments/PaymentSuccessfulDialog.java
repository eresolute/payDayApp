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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fh.payday.R;

public class PaymentSuccessfulDialog extends DialogFragment {
    static String mMessage;
    static String mAvailbaleBal;
    static int mIcon;
    LayoutInflater inflater;
    private static OnConfirmListener mConfirmListener;
    private static int mTintColor = R.color.blue;
    static int mGravity;

    public static PaymentSuccessfulDialog newInstance(
            String message, String availableBal, int icon, int tintColor,
            OnConfirmListener confirmListener
    ) {
        mMessage = message;
        mAvailbaleBal = availableBal;
        mIcon = icon;
        mTintColor = tintColor;
        mGravity = Gravity.CENTER;
        mConfirmListener = confirmListener;
        return new PaymentSuccessfulDialog();
    }

    public static PaymentSuccessfulDialog newInstance(
            String message, String availableBal, int icon, int tintColor, int gravity,
            OnConfirmListener confirmListener
    ) {
        mMessage = message;
        mAvailbaleBal = availableBal;
        mIcon = icon;
        mTintColor = tintColor;
        mConfirmListener = confirmListener;
        mGravity = gravity;
        return new PaymentSuccessfulDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View v = inflater.inflate(R.layout.dialog_payment_successful, null);
        TextView tvMessage = v.findViewById(R.id.tv_alert_message);
        TextView tvAvailableBal = v.findViewById(R.id.tv_amount);
        TextView tvAction = v.findViewById(R.id.tv_action);
        ImageView icon = v.findViewById(R.id.img_alert);

        tvMessage.setText(mMessage);
        tvMessage.setGravity(mGravity);
        tvAvailableBal.setText(mAvailbaleBal);
        icon.setImageResource(mIcon);
        icon.setColorFilter(ContextCompat.getColor(getActivity(), mTintColor), android.graphics.PorterDuff.Mode.MULTIPLY);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v).setCancelable(true);
        Dialog dialog = builder.create();
        if (dialog.getWindow() != null)
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