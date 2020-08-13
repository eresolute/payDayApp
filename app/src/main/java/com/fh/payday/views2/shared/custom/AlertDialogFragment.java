package com.fh.payday.views2.shared.custom;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.fh.payday.BuildConfig;
import com.fh.payday.R;

public class AlertDialogFragment extends DialogFragment {
    private TextView tvMessage, tvAction;
    private ImageView icon;
    private AlertDialogFragment.OnDismissListener dismissListener;
    private OnCancelListener cancelListener;
    private OnConfirmListener confirmListener;
    private boolean cancellable = false;

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (getActivity() == null) {
            throw new IllegalStateException("Invalid State; getActivity() is null");
        }


        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.dialog_layout_alert, null);
        init(view);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view).setCancelable(cancellable);
        builder.setView(view).setOnCancelListener(dialogInterface -> cancelListener.onCancel(dialogInterface));
        //builder.setView(view).setOnDismissListener(dialogInterface -> dismissListener.onDismiss(dialogInterface));
        Dialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        Bundle args = getArguments();
        if (args == null) {
            throw new IllegalArgumentException("required arguments are null");
        }

        dialog.setOnDismissListener(dialogInterface -> dismissListener.onDismiss(dialogInterface));

        dialog.setOnCancelListener(dialogInterface -> cancelListener.onCancel(dialogInterface));

        String title = args.getString("message");
        int mIcon = args.getInt("icon"), mTintColor = args.getInt("tintColor");

        int mBackground = args.getInt("background");

        if (getContext() != null)
            tvAction.setBackground(ContextCompat.getDrawable(getContext(), mBackground));

        tvAction.setText(args.getInt("btnText"));

        if (TextUtils.isEmpty(title)) {
            tvMessage.setVisibility(View.GONE);
        } else {
            tvMessage.setText(title);
        }

        int visibility = args.getInt("visibility");
        tvAction.setVisibility(visibility);

        icon.setImageResource(mIcon);
        icon.setColorFilter(ContextCompat.getColor(getActivity(), mTintColor), android.graphics.PorterDuff.Mode.MULTIPLY);

        if (confirmListener == null) {
            tvAction.setOnClickListener(v -> getActivity().finish());
        } else {
            tvAction.setOnClickListener(v -> confirmListener.onConfirm(dialog));
        }

        if (!BuildConfig.DEBUG)
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        return dialog;
    }

    private void init(View view) {
        tvMessage = view.findViewById(R.id.tv_alert_message);
        icon = view.findViewById(R.id.img_alert);
        tvAction = view.findViewById(R.id.tv_action);
    }

    private void setDismissListener(OnDismissListener listener) {
        dismissListener = listener;
    }

    private void setCancelListener(OnCancelListener listener) {
        cancelListener = listener;
    }

    private void setConfirmListener(OnConfirmListener listener) {
        confirmListener = listener;
    }

    private void isCancelable(boolean cancelable) {
        this.cancellable = cancelable;
    }

    public static class Builder {

        private OnDismissListener mDismissListener = DialogInterface::dismiss;
        private OnConfirmListener mConfirmListener = Dialog::dismiss;
        private OnCancelListener mCancelListener = DialogInterface::dismiss;
        private String mMessage;
        private int visibility = 0;
        private int mIcon, mColor = R.color.colorPrimary;
        private boolean mIsCancelable = false;
        private int mBackground = R.drawable.bottom_rounded;
        private @StringRes
        int mBtnText = R.string.ok;

        public Builder setDismissListener(@NonNull OnDismissListener dismissListener) {
            mDismissListener = dismissListener;
            return this;
        }

        public Builder setCancelListener(@NonNull OnCancelListener cancelListener) {
            mCancelListener = cancelListener;
            return this;
        }

        public Builder setConfirmListener(@NonNull OnConfirmListener confirmListener) {
            mConfirmListener = confirmListener;
            return this;
        }

        public Builder setMessage(@NonNull String message) {
            mMessage = message;
            return this;
        }

        public Builder setIcon(int icon) {
            mIcon = icon;
            return this;
        }

        public Builder setBackground(int background) {
            mBackground = background;
            return this;
        }

        public Builder setTintColor(int color) {
            mColor = color;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            mIsCancelable = cancelable;
            return this;
        }

        public Builder setButtonText(@StringRes int resourceId) {
            mBtnText = resourceId;
            return this;
        }

        public Builder setButtonVisibility(int visibility) {
            this.visibility = visibility;
            return this;
        }

        public AlertDialogFragment build() {
            AlertDialogFragment fragment = new AlertDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putString("message", mMessage != null ? mMessage : "");
            bundle.putInt("icon", mIcon);
            bundle.putInt("tintColor", mColor);
            bundle.putInt("btnText", mBtnText);
            bundle.putInt("background", mBackground);
            bundle.putInt("visibility", visibility);
            fragment.setArguments(bundle);
            fragment.setDismissListener(mDismissListener);
            fragment.setConfirmListener(mConfirmListener);
            fragment.setCancelListener(mCancelListener);
            fragment.isCancelable(mIsCancelable);
            fragment.setCancelable(mIsCancelable);
            return fragment;
        }
    }

    public interface OnDismissListener {
        void onDismiss(DialogInterface dialog);
    }

    public interface OnConfirmListener {
        void onConfirm(Dialog dialog);
    }

    public interface OnCancelListener {
        void onCancel(DialogInterface dialog);
    }

}
