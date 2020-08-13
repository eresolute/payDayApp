package com.fh.payday.views2.servicerequest.activatecard;

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

public class ActivateCardPinFragment extends Fragment {

    private TextView tvTitle;
    private TextView tvTitleConfirm;
    private OtpView pinview;
    private OtpView pinViewConfirm;
    private MaterialButton btnSubmit;
    private OnAtmPinConfirmListener mListener;
    private OnAtmPinConfirmListener mListenerConfirm;
    private TextView tvPinError;
    private TextView tvPinErrorConfirm;
    private OnResumeListener resumeListener;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activate_card_pin, container, false);
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
        String titleConfirm = args.getString("titleConfirm");

        pinview.setItemCount(pinLength);
        pinViewConfirm.setItemCount(pinLength);
        tvTitle.setText(title);
        tvTitleConfirm.setText(titleConfirm);

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
            if (!pinview.getText().toString().equals(pinViewConfirm.getText().toString())) {
                tvPinErrorConfirm.setText(getString(R.string.pin_does_not_match));
                tvPinErrorConfirm.setVisibility(View.VISIBLE);
                pinViewConfirm.setText(null);
                return;
            }

            mListenerConfirm.onAtmConfirm(pinview.getText().toString());
            pinview.setText(null);
            pinViewConfirm.setText(null);
            tvPinError.setVisibility(View.INVISIBLE);
            tvPinErrorConfirm.setVisibility(View.INVISIBLE);
        });
        pinview.setOtpCompletionListener(otp -> {
            mListener.onAtmConfirm(pinview.getText().toString());
            pinViewConfirm.requestFocus();
            tvPinError.setVisibility(View.INVISIBLE);

        });

        pinViewConfirm.setOtpCompletionListener(otp -> {
            if (TextUtils.isEmpty(pinview.getText().toString())
                || pinview.getText().toString().length() < pinLength
                || !pinview.getText().toString().matches("^[0-9]{" + pinLength + "}$")
            ) {
                tvPinError.setText(getString(R.string.invalid_pin));
                tvPinError.setVisibility(View.VISIBLE);
                pinview.setText(null);
                return;
            }
            if (!pinview.getText().toString().equals(pinViewConfirm.getText().toString())) {
                tvPinErrorConfirm.setText(getString(R.string.pin_does_not_match));
                tvPinErrorConfirm.setVisibility(View.VISIBLE);
                if (getActivity() != null)
                    ((BaseActivity) getActivity()).hideKeyboard();
                return;
            }
            if (getActivity() != null) {
                ((BaseActivity) getActivity()).hideKeyboard();
                mListenerConfirm.onAtmConfirm(otp);
                pinview.setText(null);
                pinViewConfirm.setText(null);
                tvPinError.setVisibility(View.INVISIBLE);
                tvPinErrorConfirm.setVisibility(View.INVISIBLE);
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

        if (isVisibleToUser && getActivity() != null) {
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
        pinViewConfirm = view.findViewById(R.id.pin_view_confirm);
        tvTitle = view.findViewById(R.id.tv_title);
        tvTitleConfirm = view.findViewById(R.id.tv_title_confirm);
        tvPinError = view.findViewById(R.id.tv_pin_error);
        tvPinErrorConfirm = view.findViewById(R.id.tv_pin_error_confirm);
        btnSubmit = view.findViewById(R.id.btn_submit);
    }

    private void setPinListener(OnAtmPinConfirmListener mPinListener) {
        mListener = mPinListener;
    }

    private void setConfirmPinListener(OnAtmPinConfirmListener pinConfirmListener) {
        mListenerConfirm = pinConfirmListener;
    }

    private void setOnResumeListener(OnResumeListener listener) {
        resumeListener = listener;
    }

    public void setError(String message) {
        tvPinError.setText(message);
        tvPinError.setVisibility(View.VISIBLE);
    }

    public void setConfirmError(String message) {
        tvPinErrorConfirm.setText(message);
        tvPinErrorConfirm.setVisibility(View.VISIBLE);
    }

    public static class Builder {

        private OnAtmPinConfirmListener mPinConfirmListener;
        private OnAtmPinConfirmListener mPinConfirmListener1;
        private int mPinLength = 4;
        private String mTitle;
        private String mTitleConfirm;
        private OnResumeListener mResumeListener;

        public Builder(@NonNull OnAtmPinConfirmListener pinListener) {
            mPinConfirmListener = pinListener;
        }

        public ActivateCardPinFragment.Builder setConfirmListener(OnAtmPinConfirmListener confirmPinListener) {
            mPinConfirmListener1 = confirmPinListener;
            return this;
        }

        public ActivateCardPinFragment.Builder setPinLength(int length) {
            mPinLength = length;
            return this;
        }

        public ActivateCardPinFragment.Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        ActivateCardPinFragment.Builder setConfirmTitle(String title) {
            mTitleConfirm = title;
            return this;
        }

        ActivateCardPinFragment.Builder setOnResumeListener(OnResumeListener listener) {
            mResumeListener = listener;
            return this;
        }

        public ActivateCardPinFragment build() {
            ActivateCardPinFragment fragment = new ActivateCardPinFragment();

            Bundle bundle = new Bundle();
            bundle.putInt("pin_length", mPinLength);
            bundle.putString("title", mTitle != null ? mTitle : "");
            bundle.putString("titleConfirm", mTitleConfirm != null ? mTitleConfirm : "");
            fragment.setArguments(bundle);
            fragment.setPinListener(mPinConfirmListener);
            fragment.setConfirmPinListener(mPinConfirmListener1);
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
