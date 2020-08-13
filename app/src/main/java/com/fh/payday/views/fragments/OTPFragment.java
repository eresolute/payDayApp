package com.fh.payday.views.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.fh.payday.BaseActivity;
import com.fh.payday.BaseFragment;
import com.fh.payday.R;
import com.fh.payday.datasource.models.login.User;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.preferences.UserPreferences;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.utilities.OnOTPConfirmListener;
import com.fh.payday.utilities.TextViewUtilsKt;
import com.fh.payday.viewmodels.OtpViewModel;
import com.fh.payday.views2.auth.forgotcredentials.ForgotCredentailsActivity;
import com.fh.payday.views2.auth.forgotcredentials.loancustomer.LoanCustomerActivity;
import com.fh.payday.views2.kyc.update.KycOptionActivity;
import com.fh.payday.views2.servicerequest.ServiceRequestOptionActivity;
import com.mukesh.OtpView;

public class OTPFragment extends Fragment {

    private OnOTPConfirmListener otpListener;
    private TextView tvTitle, tvInstruction, tvOtpError;
    private OtpView pinview;
    private MaterialButton btnSubmit;
    private OnResumeListener resumeListener;
    private OnResendOtpListener resendOtpListener;
    @Nullable
    private BaseActivity activity;
    private OtpViewModel viewModel;
    @LayoutRes
    private int layout = R.layout.layout_otp_fragment;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (BaseActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        pinview.setText(null);
        activity = null;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && activity != null) {
            pinview.requestFocus();
            InputMethodManager imgr = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }

        if (!isVisibleToUser && pinview != null) {
            pinview.setText(null);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        if (!(activity instanceof ServiceRequestOptionActivity))
//            activity.hideKeyboard();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(layout, container, false);
        TextView tvResendOpt = view.findViewById(R.id.resendOtp);
        TextViewUtilsKt.setTextWithUnderLine(tvResendOpt, tvResendOpt.getText().toString());

        init(view);
        viewModel = ViewModelProviders.of(this).get(OtpViewModel.class);

        addOtpObserver();

        Bundle args = getArguments();
        if (args == null) {
            throw new IllegalArgumentException("required arguments are null");
        }

        if (activity instanceof ServiceRequestOptionActivity || activity instanceof KycOptionActivity) {
            InputMethodManager imgr = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            pinview.requestFocus();
            imgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }




        int pinLength = args.getInt("pin_length", 4);
        String title = args.getString("title");
        String instructions = args.getString("instruction");
        String btnTitle = args.getString("button_title");

        pinview.setItemCount(pinLength);
        tvTitle.setText(title);
        tvInstruction.setText(instructions != null && !instructions.equals("") ? instructions : getString(R.string.otp_sent_to_registered_number));
        btnSubmit.setText(btnTitle);

        btnSubmit.setOnClickListener(view1 -> {
            String otp = pinview.getText() != null ? pinview.getText().toString() : "";
            if (TextUtils.isEmpty(otp) || otp.length() < pinLength) {
                tvOtpError.setVisibility(View.VISIBLE);
                pinview.requestFocus();
                return;
            } else {
                tvOtpError.setVisibility(View.GONE);
            }
            pinview.setText(null);

            if (activity != null && !activity.isNetworkConnected()) {
                View rootView = activity.findViewById(R.id.root_view);
                if (rootView != null) {
                    activity.onFailure(rootView, getString(R.string.no_internet_connectivity));
                    return;
                }
            }

            otpListener.onOtpConfirm(otp);
        });

        tvResendOpt.setOnClickListener(v -> {
            pinview.setText(null);
            if (activity == null) return;

            if (resendOtpListener != null) {
                resendOtpListener.onConfirm();
            } else {
                if (!activity.isNetworkConnected()) {
                    View rootView = activity.findViewById(R.id.root_view);
                    if (rootView != null) {
                        activity.onFailure(rootView, getString(R.string.no_internet_connectivity));
                        return;
                    }
                }
                if (activity instanceof ForgotCredentailsActivity) {
                    Long userId = ((ForgotCredentailsActivity) activity).getViewModel().getCustomerId();
                    String mobileNo = ((ForgotCredentailsActivity) activity).getViewModel().getMobileNumber();
                    viewModel.credentialsResendOtp(Long.parseLong(String.valueOf(userId)), mobileNo);
                } else if (activity instanceof LoanCustomerActivity) {
                    String mobileNo = ((LoanCustomerActivity) activity).getViewModel().getMobileNumber();
                    Long userId = ((LoanCustomerActivity) activity).getViewModel().getCustomerId();
                    viewModel.credentialsResendOtp(Long.parseLong(String.valueOf(userId)), mobileNo);
                } else {
                    User user = UserPreferences.Companion.getInstance().getUser(activity);

                    if (user != null) {
                        viewModel.getOtp(user.getToken(), user.getSessionId(), user.getRefreshToken(), user.getCustomerId());
                    }
                }
            }
        });

        pinview.setOtpCompletionListener(otp -> {
            if (getActivity() != null) {
                ((BaseActivity) getActivity()).hideKeyboard();
                tvOtpError.setVisibility(View.GONE);
                pinview.setText(null);

                if (activity != null && !activity.isNetworkConnected()) {
                    View rootView = activity.findViewById(R.id.root_view);
                    if (rootView != null) {
                        activity.onFailure(rootView, getString(R.string.no_internet_connectivity));
                        return;
                    }
                }

                if (otp != null) {
                    otpListener.onOtpConfirm(otp);
                }
            }
        });

        return view;
    }

    private void addOtpObserver() {
        if (activity == null) return;

        viewModel.getOtpState().observe(activity, event -> {
            if (event == null) return;

            NetworkState2<String> otpResponse = event.getContentIfNotHandled();

            if (otpResponse == null) return;

            if (otpResponse instanceof NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing));
                btnSubmit.setEnabled(false);
                return;
            }

            activity.hideProgress();
            btnSubmit.setEnabled(true);

            if (otpResponse instanceof NetworkState2.Success) {
                String message = ((NetworkState2.Success<String>) otpResponse).getData();
                if (message != null) {
                    activity.showMessage(message);
                }
            } else if (otpResponse instanceof NetworkState2.Error) {
                NetworkState2.Error<String> error = (NetworkState2.Error<String>) otpResponse;
                pinview.setText(null);
                if (error.isSessionExpired()) {
                    activity.onSessionExpired(error.getMessage());
                    return;
                }
                activity.onError(error.getMessage());
            } else if (otpResponse instanceof NetworkState2.Failure) {
                NetworkState2.Failure<String> error = (NetworkState2.Failure<String>) otpResponse;
                pinview.setText(null);
                if (activity != null) {
                    View rootView = activity.findViewById(R.id.root_view);
                    if (rootView != null) {
                        activity.onFailure(rootView, error.getThrowable());
                    }
                }
            } else {
                pinview.setText(null);
                if (activity != null) {
                    View rootView = activity.findViewById(R.id.root_view);
                    if (rootView != null) {
                        activity.onFailure(rootView, ConstantsKt.CONNECTION_ERROR);
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        tvOtpError.setVisibility(View.GONE);
        if (resumeListener != null) {
            resumeListener.onResume();
        }
    }

    @Override
    public void onPause() {
        tvOtpError.setVisibility(View.GONE);
        super.onPause();
    }

    private void init(View view) {
        tvTitle = view.findViewById(R.id.tv_title);
        tvInstruction = view.findViewById(R.id.tv_instruction);
        tvOtpError = view.findViewById(R.id.tv_opt_error);
        pinview = view.findViewById(R.id.pin_view);
        btnSubmit = view.findViewById(R.id.btn_submit);

        pinview.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                tvOtpError.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void setOtpListener(OnOTPConfirmListener listener) {
        otpListener = listener;
    }

    private void setLayout(@LayoutRes int layout) {
        this.layout = layout;
    }

    private void setOnResumeListener(OnResumeListener listener) {
        resumeListener = listener;
    }

    private void setOnResendOtpListener(OnResendOtpListener listener) {
        resendOtpListener = listener;
    }

    public static class Builder {

        private OnOTPConfirmListener mOtpListener;
        private int mPinLength = 4;
        private String mTitle, mInstruction, mBtnTitle;
        private boolean mHasCardView = true;
        private OnResumeListener mResumeListener;
        private OnResendOtpListener mResendOtpListener;

        public Builder(@NonNull OnOTPConfirmListener otpListener) {
            mOtpListener = otpListener;
        }

        public Builder setPinLength(int length) {
            mPinLength = length;
            return this;
        }

        public Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        public Builder setInstructions(String instruction) {
            mInstruction = instruction;
            return this;
        }

        public Builder setButtonTitle(String btnTitle) {
            mBtnTitle = btnTitle;
            return this;
        }

        public Builder setHasCardView(boolean hasCardView) {
            mHasCardView = hasCardView;
            return this;
        }

        public Builder setOnResumeListener(OnResumeListener listener) {
            mResumeListener = listener;
            return this;
        }

        public Builder setOnResendOtpListener(OnResendOtpListener listener) {
            mResendOtpListener = listener;
            return this;
        }

        public OTPFragment build() {
            OTPFragment fragment = new OTPFragment();

            Bundle bundle = new Bundle();
            bundle.putInt("pin_length", mPinLength);
            bundle.putString("title", mTitle != null ? mTitle : "");
            bundle.putString("instruction", mInstruction != null ? mInstruction : "");
            bundle.putString("button_title", mBtnTitle != null ? mBtnTitle : "Verify");

            fragment.setArguments(bundle);
            fragment.setOtpListener(mOtpListener);
            if (mHasCardView) {
                fragment.setLayout(R.layout.layout_otp_fragment);
            } else {
                fragment.setLayout(R.layout.main_otp_fragment);
            }

            if (mResumeListener != null) {
                fragment.setOnResumeListener(mResumeListener);
            }

            if (mResendOtpListener != null) {
                fragment.setOnResendOtpListener(mResendOtpListener);
            }

            return fragment;
        }
    }

    public interface OnResumeListener {
        void onResume();
    }

    public interface OnResendOtpListener {
        void onConfirm();
    }
}
