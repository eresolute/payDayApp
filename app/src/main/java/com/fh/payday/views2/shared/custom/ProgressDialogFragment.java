package com.fh.payday.views2.shared.custom;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.fh.payday.BuildConfig;
import com.fh.payday.R;

public class ProgressDialogFragment extends DialogFragment {

    private TextView tvTitle;
    private OnDismissListener dismissListener;
    private boolean cancellable = false;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        if(getActivity() == null) {
            throw new IllegalStateException("Invalid State; getActivity() is null");
        }

        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.dialog_progress_bar, null);
        init(view);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view).setCancelable(cancellable);
        Dialog dialog = builder.create();

        Bundle args = getArguments();
        if (args == null) {
            throw new IllegalArgumentException("required arguments are null");
        }

        String title = args.getString("title");
        if (TextUtils.isEmpty(title)) {
            tvTitle.setVisibility(View.GONE);
        } else {
            tvTitle.setText(title);
        }
        dialog.setOnDismissListener(dialogInterface -> dismissListener.onDismiss(dialogInterface));

        if (dialog.getWindow() != null && !BuildConfig.DEBUG)
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        return  dialog;
    }

    private void init(View view) {
        tvTitle = view.findViewById(R.id.text_view);
    }

    private void setDismissListener(OnDismissListener listener) {
        dismissListener = listener;
    }

    private void isCancelable(boolean cancelable) {
        this.cancellable = cancelable;
    }

    public static class Builder {

        private OnDismissListener mDismissListener;
        private String mTitle;
        private boolean mIsCancelable = false;

        public Builder(@NonNull OnDismissListener dismissListener){
            mDismissListener = dismissListener;
        }

        public Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            mIsCancelable = cancelable;
            return this;
        }

        public ProgressDialogFragment build() {
            ProgressDialogFragment fragment = new ProgressDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putString("title", mTitle != null ? mTitle : "" );
            fragment.setArguments(bundle);
            fragment.setDismissListener(mDismissListener);
            fragment.isCancelable(mIsCancelable);
            fragment.setCancelable(mIsCancelable);
            return fragment;
        }
    }

    public interface OnDismissListener {
        void onDismiss(DialogInterface dialog);
    }

}
