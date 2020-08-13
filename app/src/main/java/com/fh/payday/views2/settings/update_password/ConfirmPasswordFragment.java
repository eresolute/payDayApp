package com.fh.payday.views2.settings.update_password;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fh.payday.R;
import com.fh.payday.datasource.models.login.User;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.utilities.PasswordValidator;
import com.fh.payday.viewmodels.SettingOptionViewModel;
import com.fh.payday.views2.settings.SettingOptionActivity;

import static com.fh.payday.utilities.TextInputUtilsKt.clearErrorMessage;
import static com.fh.payday.utilities.TextInputUtilsKt.setErrorMessage;

public class ConfirmPasswordFragment extends Fragment {
    MaterialButton btnConfirm;
    SettingOptionViewModel viewModel;
    @Nullable
    private SettingOptionActivity activity;
    private TextInputLayout textInputLayout;
    private TextInputEditText etPassword;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (SettingOptionActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && etPassword != null) {
            etPassword.setText(null);
        }
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (activity == null) return;
        viewModel = ViewModelProviders.of(activity).get(SettingOptionViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_confirm_password_fragment, container, false);
        init(view);

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                clearErrorMessage(textInputLayout);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        addObservers();

        btnConfirm.setOnClickListener(view1 -> {
            if (activity == null) return;

            if (validateEditText()) {
                if (!activity.isNetworkConnected()) {
                    activity.onFailure(activity.findViewById(R.id.card_view), getString(R.string.no_internet_connectivity));
                    return;
                }
                User user = activity.getChangePasswordViewModel().getUser();
                if (user != null) {
                    activity.getChangePasswordViewModel().getOtp(user.getToken(), user.getSessionId(), user.getRefreshToken(),
                        user.getCustomerId());
                }
            }
        });

        return view;
    }

    private void init(View view) {
        btnConfirm = view.findViewById(R.id.btn_confirm);
        textInputLayout = view.findViewById(R.id.textInputLayout);
        etPassword = view.findViewById(R.id.et_confirm_password);
    }

    private void addObservers() {
        if (activity == null) return;

        activity.getChangePasswordViewModel().getOtpGenerationState().observe(activity, event -> {
            if (event == null || activity == null) return;

            NetworkState2<?> state = event.getContentIfNotHandled();

            if (state == null) return;

            if (state instanceof NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing));
                return;
            }

            activity.hideProgress();

            if (state instanceof NetworkState2.Success) {
                viewModel.setSelected(3);
            } else if (state instanceof NetworkState2.Error) {
                NetworkState2.Error<?> error = (NetworkState2.Error<?>) state;
                if (error.isSessionExpired()) {
                    activity.onSessionExpired(error.getMessage());
                    return;
                }
                activity.onError(error.getMessage());
            } else if (state instanceof NetworkState2.Failure) {
                activity.onFailure(activity.findViewById(R.id.card_view), ((NetworkState2.Failure<?>) state).getThrowable());
            } else {
                activity.onFailure(activity.findViewById(R.id.card_view), new Throwable(ConstantsKt.CONNECTION_ERROR));
            }
        });
    }

    private boolean validateEditText() {
        if (activity == null) return false;
        String password = activity.getChangePasswordViewModel().getNewPassword();

        if (etPassword.getText() != null && TextUtils.isEmpty(etPassword.getText().toString().trim())) {
            etPassword.requestFocus();
            setErrorMessage(textInputLayout, getString(R.string.confirm_password));
            return false;
        } else if (etPassword.getText() != null && !PasswordValidator.Companion
                .validate(etPassword.getText().toString().trim())) {
            setErrorMessage(textInputLayout, getString(R.string.password_pattern_info));
            etPassword.requestFocus();
            return false;

        } else if (etPassword.getText() != null && !etPassword.getText().toString().trim().equals(password)) {
            setErrorMessage(textInputLayout, getString(R.string.password_not_match));
            etPassword.requestFocus();
            return false;
        } else {
            clearErrorMessage(textInputLayout);
            return true;
        }
    }
}
