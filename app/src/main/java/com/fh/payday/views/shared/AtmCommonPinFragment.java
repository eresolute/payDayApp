package com.fh.payday.views.shared;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.fh.payday.BaseActivity;
import com.fh.payday.R;
import com.fh.payday.utilities.OnAtmPinConfirmListener;
import com.mukesh.OtpView;

public class AtmCommonPinFragment extends Fragment {

    private TextView tvTitle;
    private OtpView pinview;
    private MaterialButton btnSubmit;
    private OnAtmPinConfirmListener mListener;
    private TextView tvPinError;
    private OnResumeListener resumeListener;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_common_atm_pin, container, false);
        init(view);

        pinview.requestFocus();
        InputMethodManager imgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        Bundle args = getArguments();
        if (args == null) {
            throw new IllegalArgumentException("required arguments are null");
        }

        int pinLength = args.getInt("pin_length", 4);
        String title = args.getString("title");

        pinview.setItemCount(pinLength);
        tvTitle.setText(title);
        tvPinError = view.findViewById(R.id.tv_pin_error);

        btnSubmit.setOnClickListener(v -> {
            if (TextUtils.isEmpty(pinview.getText().toString())
                    || pinview.getText().toString().length() < pinLength
                    || !pinview.getText().toString().matches("^[0-9]{" + pinLength + "}$")
            ) {
                tvPinError.setText(getString(R.string.invalid_pin));
                tvPinError.setVisibility(View.VISIBLE);
                pinview.setText(null);
                return;
            }
            mListener.onAtmConfirm(pinview.getText().toString());
        });

        pinview.setOtpCompletionListener(otp -> {
            if (TextUtils.isEmpty(pinview.getText().toString()) ||
                    pinview.getText().toString().length() < pinLength ||
                    !pinview.getText().toString().matches("^[0-9]{" + pinLength + "}$"))
            {
                tvPinError.setText(getString(R.string.invalid_pin));
                tvPinError.setVisibility(View.VISIBLE);
                pinview.setText(null);
                return;
            }
            if (getActivity() != null) {
                ((BaseActivity) getActivity()).hideKeyboard();
                mListener.onAtmConfirm(otp);
                pinview.setText(null);
                // tvPinError.setVisibility(View.GONE);
            }
        });

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser && pinview != null) {
            pinview.setText(null);
        }

        if (isVisibleToUser && getActivity() != null){
            pinview.requestFocus();
            InputMethodManager imgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (resumeListener != null) {
            resumeListener.onResume();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        pinview.setText(null);
    }

    private void init(View view) {
        pinview = view.findViewById(R.id.pin_view);
        tvTitle = view.findViewById(R.id.tv_title);
        btnSubmit = view.findViewById(R.id.btn_submit);
    }

    private void setPinListener(OnAtmPinConfirmListener mPinListener) {
        mListener = mPinListener;
    }

    private void setOnResumeListener(OnResumeListener listener) {
        resumeListener = listener;
    }

    public void setError(String message) {
        tvPinError.setText(message);
        tvPinError.setVisibility(View.VISIBLE);
    }

    public static class Builder {

        private OnAtmPinConfirmListener mPinConfirmListener;
        private int mPinLength = 4;
        private String mTitle;
        private OnResumeListener mResumeListener;

        public Builder(@NonNull OnAtmPinConfirmListener pinListener) {
            mPinConfirmListener = pinListener;
        }

        public AtmCommonPinFragment.Builder setPinLength(int length) {
            mPinLength = length;
            return this;
        }

        public AtmCommonPinFragment.Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        public AtmCommonPinFragment.Builder setOnResumeListener(OnResumeListener listener) {
            mResumeListener = listener;
            return this;
        }

        public AtmCommonPinFragment build() {
            AtmCommonPinFragment fragment = new AtmCommonPinFragment();

            Bundle bundle = new Bundle();
            bundle.putInt("pin_length", mPinLength);
            bundle.putString("title", mTitle != null ? mTitle : "");
            fragment.setArguments(bundle);
            fragment.setPinListener(mPinConfirmListener);
            if (mResumeListener != null) {
                fragment.setOnResumeListener(mResumeListener);
            }

            return fragment;
        }
    }

    public interface OnResumeListener {
        void onResume();
    }
}
