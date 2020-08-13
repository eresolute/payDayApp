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
import com.fh.payday.preferences.UserPreferences;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.utilities.PasswordValidator;
import com.fh.payday.viewmodels.SettingOptionViewModel;
import com.fh.payday.views2.settings.SettingOptionActivity;

import static com.fh.payday.utilities.TextInputUtilsKt.clearErrorMessage;
import static com.fh.payday.utilities.TextInputUtilsKt.setErrorMessage;

public class CurrentPasswordFragment extends Fragment {

    SettingOptionViewModel viewModel;
    MaterialButton btnConfirm;
    SettingOptionActivity activity;
    private TextInputLayout textInputLayout;
    private TextInputLayout textInputLayoutNew;
    private TextInputLayout textInputLayoutConfirm;
    private TextInputEditText etOldPassword;
    private TextInputEditText etNewPassword;
    private TextInputEditText etConfirmPassword;

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

        if (isVisibleToUser && etOldPassword != null) {
            etOldPassword.setText(null);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(activity).get(SettingOptionViewModel.class);
        addObservers();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_current_password, container, false);
        init(view);

        etOldPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                clearErrorMessage(textInputLayout);
            }
        });

        btnConfirm.setOnClickListener(view1 -> {
            if (validateEditText()) {
                //viewModel.setSelected(1);
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


    private boolean validateEditText() {
        User user = UserPreferences.Companion.getInstance().getUser(requireContext());
        if (user == null) return false;

        if (etOldPassword.getText() != null && TextUtils.isEmpty(etOldPassword.getText().toString().trim())) {
            etOldPassword.requestFocus();
            setErrorMessage(textInputLayout, getString(R.string.empty_password));
            return false;
        } else if (etNewPassword.getText() != null && TextUtils.isEmpty(etNewPassword.getText().toString().trim())) {
            etNewPassword.requestFocus();
            setErrorMessage(textInputLayoutNew, getString(R.string.empty_password));
            return false;
        } else if (etNewPassword.getText() != null && !PasswordValidator.Companion
            .validate(etNewPassword.getText().toString().trim())) {
            setErrorMessage(textInputLayoutNew, getString(R.string.password_pattern_info));
            etNewPassword.requestFocus();
            return false;
        }else if (etNewPassword.getText() != null && etNewPassword.getText().toString().trim().equals(etOldPassword.getText().toString().trim())) {
            setErrorMessage(textInputLayoutNew, getString(R.string.password_not_same_as_old));
            etNewPassword.requestFocus();
            return false;
        } else if (etNewPassword.getText() != null && etNewPassword.getText().toString()
            .contains(user.getUsername().replaceAll("\\d", ""))) {
            setErrorMessage(textInputLayoutNew, getString(R.string.password_contains_userid));
            etNewPassword.requestFocus();
            return false;
        } else if (etConfirmPassword.getText() != null && TextUtils.isEmpty(etConfirmPassword.getText().toString().trim())) {
            etConfirmPassword.requestFocus();
            setErrorMessage(textInputLayoutConfirm, getString(R.string.confirm_password));
            return false;
        } else if (etConfirmPassword.getText() != null && !PasswordValidator.Companion
            .validate(etConfirmPassword.getText().toString().trim())) {
            setErrorMessage(textInputLayoutConfirm, getString(R.string.password_pattern_info));
            etConfirmPassword.requestFocus();
            return false;

        } else if (etConfirmPassword.getText() != null && !etConfirmPassword.getText().toString().trim().equals(etNewPassword.getText().toString().trim())) {
            setErrorMessage(textInputLayoutConfirm, getString(R.string.password_not_match));
            etConfirmPassword.requestFocus();
            return false;
        } else {
            activity.getChangePasswordViewModel().setNewPassword(etNewPassword.getText().toString().trim());
            activity.getChangePasswordViewModel().setOldPassword(etOldPassword.getText().toString().trim());
            return true;
        }

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
                etOldPassword.setText(null);
                etNewPassword.setText(null);
                etConfirmPassword.setText(null);
                viewModel.setSelected(1);
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
    private void init(View view) {
        btnConfirm = view.findViewById(R.id.btn_confirm);
        textInputLayout = view.findViewById(R.id.textInputLayout);
        textInputLayoutNew = view.findViewById(R.id.textInputLayoutNew);
        textInputLayoutConfirm = view.findViewById(R.id.textInputLayoutConfirm);
        etOldPassword = view.findViewById(R.id.et_current_password);
        etNewPassword = view.findViewById(R.id.et_new_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        attachListener(textInputLayout, etOldPassword);
        attachListener(textInputLayoutNew, etNewPassword);
        attachListener(textInputLayoutConfirm, etConfirmPassword);
    }

    private void attachListener(TextInputLayout textInputLayout, TextInputEditText editText){
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                clearErrorMessage(textInputLayout);
            }
        });
    }
}
