package com.fh.payday.views2.registration;


import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.datasource.models.ui.RegisterInputWrapper;
import com.fh.payday.datasource.models.ui.RegistrationWrapper;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.viewmodels.RegistrationViewModel;

import java.util.Objects;

import static com.fh.payday.utilities.TextInputUtilsKt.addErrorMessage;
import static com.fh.payday.utilities.TextInputUtilsKt.clearErrorMsg;
import static com.fh.payday.utilities.TextInputUtilsKt.onErrorMsg;
import static com.fh.payday.utilities.TextInputUtilsKt.removeErrorMessage;

public class MobileNumberFragment extends Fragment {

    @Nullable
    private RegisterActivity activity;
    private TextInputEditText etMobileNo;
    private TextInputEditText etConfirmMobileNo;
    private ConstraintLayout clMobileNo;
    private ConstraintLayout clConfirmMobileNo;
    private TextView tvPrefix;
    private TextView tvError;
    private TextView tvError2;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (RegisterActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && activity != null) {
            clearFields();
        }
    }

    private void clearFields() {
        if (etMobileNo == null || etConfirmMobileNo == null) return;

        etMobileNo.setText(null);
        etConfirmMobileNo.setText(null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_verify_mobile_number, container, false);

        etMobileNo = view.findViewById(R.id.et_mobile_number);
        etConfirmMobileNo = view.findViewById(R.id.et_confirm_mobile_no);
        Button btnNext = view.findViewById(R.id.btn_next);
        clMobileNo = view.findViewById(R.id.cl_custom_layout);
        clConfirmMobileNo = view.findViewById(R.id.cl_custom_layout_2);
        tvError = view.findViewById(R.id.tv_error_message);
        tvError2 = view.findViewById(R.id.tv_error_message_1);
        tvPrefix = view.findViewById(R.id.tv_prefix);

        addTextWatcherListener(etMobileNo, clMobileNo);
        addTextWatcherListener(etConfirmMobileNo, clConfirmMobileNo);

        if (activity != null) {

            RegistrationWrapper wrapper = activity.getViewModel().getWrapper();
            RegisterInputWrapper inputWrapper = activity.getViewModel().getInputWrapper();

            btnNext.setOnClickListener(v -> {

                String prefix = tvPrefix.getText().toString().replace("-", "");
                String mobileNum = prefix.concat(Objects.requireNonNull(
                    etMobileNo.getText()).toString().trim());

                String confirmMobileNo = prefix.concat(Objects.requireNonNull(
                    etConfirmMobileNo.getText()).toString().trim());

                mobileNum = mobileNum.replace("+971", "0");
                confirmMobileNo = confirmMobileNo.replace("+971", "0");


                if (validateEditText(mobileNum, confirmMobileNo)) {
                    if (wrapper.getCustomerId() != null) {

                        if (!activity.isNetworkConnected()) {
                            activity.onFailure(activity.findViewById(R.id.root_view), getString(R.string.no_internet_connectivity));
                            return;
                        }

                        inputWrapper.setMobileNo(mobileNum);
                        activity.getViewModel().generateOtp(wrapper.getCustomerId(), mobileNum);
                    }
                }
            });
        }
        attachObservers();

        return view;
    }

    private void addTextWatcherListener(TextInputEditText editText, ConstraintLayout constraintLayout) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                clearErrorMsg(constraintLayout);
                removeErrorMessage(tvError);
                removeErrorMessage(tvError2);

            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void attachObservers() {
        RegisterActivity activity = this.activity;
        if (activity == null) return;
        RegistrationViewModel viewModel = activity.getViewModel();

        viewModel.getOtpGenerationState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<?> state = event.peekContent();
            if (state == null) return;

            if (state instanceof NetworkState2.Error) {
                NetworkState2.Error<?> error = (NetworkState2.Error<?>) state;
                if ("854".equals(error.getErrorCode())) {
                    etMobileNo.setText("");
                    etConfirmMobileNo.setText("");
                    etMobileNo.requestFocus();
                }
            }

            /*if (state instanceof NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing));
                btnNext.setEnabled(false);
                return;
            }

            activity.hideProgress();
            btnNext.setEnabled(true);

            if (state instanceof NetworkState2.Success) {
                activity.navigateUp();
            } else if (state instanceof NetworkState2.Error) {
                NetworkState2.Error<?> error = (NetworkState2.Error<?>) state;
                activity.onError(error.getMessage());
            } else if (state instanceof NetworkState2.Failure) {
                activity.onFailure(activity.findViewById(R.id.root_view), getString(R.string.request_error));
            } else {
                activity.onFailure(activity.findViewById(R.id.root_view), ConstantsKt.CONNECTION_ERROR);
            }*/
        });
    }

    private boolean validateEditText(String mobileNo, String confirmMobileNo) {
        if (mobileNo != null && TextUtils.isEmpty(mobileNo)) {
            addErrorMessage(tvError, getString(R.string.empty_mobile_no));
            onErrorMsg(clMobileNo);
            etMobileNo.requestFocus();
            return false;
        } else if ("05".equalsIgnoreCase(mobileNo)) {
            addErrorMessage(tvError, getString(R.string.empty_mobile_no));
            onErrorMsg(clMobileNo);
            etMobileNo.requestFocus();
            return false;
        } else if (mobileNo != null && mobileNo.length() < 10) {
            addErrorMessage(tvError, getString(R.string.invalid_mobile_no));
            onErrorMsg(clMobileNo);
            etMobileNo.requestFocus();
            return false;
        } else if (mobileNo != null && !mobileNo.startsWith("05")) {
            addErrorMessage(tvError, getString(R.string.number_format_error));
            onErrorMsg(clMobileNo);
            etMobileNo.requestFocus();
            return false;
        }
  /*      else if (confirmMobileNo != null && TextUtils.isEmpty(confirmMobileNo)) {
            addErrorMessage(tvError2, getString(R.string.confirm_mobile_no));
            onErrorMsg(clConfirmMobileNo);
            etConfirmMobileNo.requestFocus();
            return false;
        } else if ("05".equalsIgnoreCase(confirmMobileNo)) {
            addErrorMessage(tvError2, getString(R.string.confirm_mobile_no));
            onErrorMsg(clConfirmMobileNo);
            etConfirmMobileNo.requestFocus();
            return false;
        } else if (confirmMobileNo != null && !confirmMobileNo.equals(mobileNo)) {
            addErrorMessage(tvError2, getString(R.string.mobile_not_match));
            onErrorMsg(clConfirmMobileNo);
            etConfirmMobileNo.requestFocus();
            return false;
        }*/
        else {
            clearErrorMsg(clMobileNo);
            clearErrorMsg(clConfirmMobileNo);
            removeErrorMessage(tvError2);
            removeErrorMessage(tvError);
        }
        return true;
    }
}
