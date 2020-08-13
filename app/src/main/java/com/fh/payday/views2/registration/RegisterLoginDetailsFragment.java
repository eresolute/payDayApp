package com.fh.payday.views2.registration;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.fh.payday.R;
import com.fh.payday.datasource.models.ui.RegisterInputWrapper;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.utilities.PasswordValidator;
import com.fh.payday.utilities.UsernameValidator;
import com.fh.payday.viewmodels.RegistrationViewModel;

import static com.fh.payday.utilities.TextInputUtilsKt.clearErrorMessage;
import static com.fh.payday.utilities.TextInputUtilsKt.setErrorMessage;

public class RegisterLoginDetailsFragment extends Fragment {
    @Nullable
    private RegisterActivity activity;

    private TextInputLayout tilUserID;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;
    private TextInputEditText etUserID;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;

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

        if (isVisibleToUser) {
            clearData();
        }
    }

    private void clearData() {
        if (etUserID == null || etPassword == null || etConfirmPassword == null) return;

        etConfirmPassword.setText(null);
        etPassword.setText(null);
        etUserID.setText(null);
        etUserID.requestFocus();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_register_login_details, container, false);

        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        etUserID = view.findViewById(R.id.et_user_id);
        etPassword = view.findViewById(R.id.et_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        tilUserID = view.findViewById(R.id.textInputLayout_user_id);
        tilPassword = view.findViewById(R.id.textInputLayout_password);
        tilConfirmPassword = view.findViewById(R.id.textInputLayout_confirm_password);

        attachListener(etUserID, tilUserID);
        addTextWatcherListener(etPassword, tilPassword);
        addTextWatcherListener(etConfirmPassword, tilConfirmPassword);
        attachListener(view);
        attachObserver();


        return view;
    }

    private void attachListener(TextInputEditText editText, TextInputLayout textInputLayout) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (activity != null)
                    activity.getViewModel().getInputWrapper().setUserIdAvailable(false);
                clearErrorMessage(textInputLayout);
                setUsernameAvailable(etUserID, R.drawable.ic_card_name, 0);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void attachListener(View view) {
        etUserID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (activity != null && UsernameValidator.Companion.validate(s.toString().trim()))
                    activity.getViewModel().searchUserId(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        view.findViewById(R.id.btn_next).setOnClickListener(v -> {
            RegisterActivity activity = this.activity;
            if (activity == null) return;

            RegistrationViewModel viewModel = activity.getViewModel();

            RegisterInputWrapper inputWrapper = viewModel.getInputWrapper();
            String userId = etUserID.getText() != null ? etUserID.getText().toString() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
            String osVersion = ConstantsKt.getDeviceName() + "-" + ConstantsKt.getOs();
            if (validateEditText()) {
                if (!inputWrapper.isUserIdAvailable()) {
                    setErrorMessage(tilUserID, getString(R.string.user_Id_not_available));
                    return;
                }

                String mobile = inputWrapper.getMobileNo();
                if (mobile == null) return;
                inputWrapper.setUserId(userId);
                inputWrapper.setPassword(password);

                if (!activity.isNetworkConnected()) {
                    activity.onFailure(activity.findViewById(R.id.root_view), getString(R.string.no_internet_connectivity));
                    return;
                }

                viewModel.registerCredentials(userId, password, mobile, "", "",
                        osVersion, ConstantsKt.getAppVersion(), ConstantsKt.ENGLISH);
            }
        });
    }

    private void addTextWatcherListener(TextInputEditText editText, TextInputLayout textInputLayout) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (editText.getId() == R.id.et_password) {
                    if (etPassword.getText() != null && !PasswordValidator.Companion
                            .validate(etPassword.getText().toString().trim())) {
                        setErrorMessage(tilPassword, getString(R.string.password_pattern_info));
                        etPassword.requestFocus();
                        setUsernameAvailable(etPassword, R.drawable.ic_password, 0);
                        setUsernameAvailable(etConfirmPassword, R.drawable.ic_password, 0);
                    } else if (etPassword.getText() != null && etUserID.getText() != null &&
                            etPassword.getText().toString().contains(etUserID.getText()
                                    .toString().replaceAll("\\d", ""))) {
                        setErrorMessage(tilPassword, getString(R.string.password_contains_userid));
                        setUsernameAvailable(etPassword, R.drawable.ic_password, 0);
                        setUsernameAvailable(etConfirmPassword, R.drawable.ic_password, 0);
                        etPassword.requestFocus();
                    } else {
                        setUsernameAvailable(etPassword, R.drawable.ic_password, R.drawable.ic_checked);
                    }
                } else {
                    if (etConfirmPassword.getText() != null && TextUtils.isEmpty(etConfirmPassword.getText().toString())) {
                        setErrorMessage(tilConfirmPassword, getString(R.string.empty_confirm_password));
                        etConfirmPassword.requestFocus();
                        setUsernameAvailable(etConfirmPassword, R.drawable.ic_password, 0);
                    } else if (etConfirmPassword.getText() != null && etPassword.getText() != null
                            && !etConfirmPassword.getText().toString().equals(etPassword.getText().toString())) {
                        setErrorMessage(tilConfirmPassword, getString(R.string.password_not_match));
                        etConfirmPassword.requestFocus();
                        setUsernameAvailable(etConfirmPassword, R.drawable.ic_password, 0);
                    } else {
                        setUsernameAvailable(etConfirmPassword, R.drawable.ic_password, R.drawable.ic_checked);
                    }
                }

                clearErrorMessage(textInputLayout);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    private void attachObserver() {
        RegisterActivity activity = this.activity;
        if (activity == null) return;

        RegistrationViewModel viewModel = activity.getViewModel();

        viewModel.getSearchUserIdState().observe(this, event -> {

            if (!activity.isNetworkConnected()) {
                activity.onFailure(activity.findViewById(R.id.root_view), getString(R.string.no_internet_connectivity));
                return;
            }

            if (event == null) return;
            NetworkState2<String> state = event.getContentIfNotHandled();
            if (state == null) return;

            if (state instanceof NetworkState2.Success) {
                viewModel.getInputWrapper().setUserIdAvailable(true);
                setUsernameAvailable(etUserID, R.drawable.ic_card_name, R.drawable.ic_checked);
            } else if (state instanceof NetworkState2.Error) {
                NetworkState2.Error<String> error = (NetworkState2.Error<String>) state;
                setUsernameAvailable(etUserID, R.drawable.ic_card_name, 0);
                setErrorMessage(tilUserID, error.getMessage());
            } else if (state instanceof NetworkState2.Failure) {
                NetworkState2.Failure<?> failure = (NetworkState2.Failure<?>) state;
                activity.onFailure(activity.findViewById(R.id.root_view), failure.getThrowable());
            } else {
                setUsernameAvailable(etUserID, R.drawable.ic_card_name, 0);
                activity.onFailure(activity.findViewById(R.id.root_view), ConstantsKt.CONNECTION_ERROR);
            }
        });

        viewModel.getCredentialsState().observe(this, event -> {
            if (event == null) return;
            NetworkState2<String> state = event.getContentIfNotHandled();
            if (state == null) return;

            if (state instanceof NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing));
                return;
            }

            activity.hideProgress();

            if (state instanceof NetworkState2.Success) {
                activity.onCredentialsSubmitted();
//                NetworkState2.Success<User> success = (NetworkState2.Success<User>) state;
//                User user = success.getData();
//                if (user != null && UserPreferences.Companion.getInstance().saveUser(activity, user)) {
//                    AppPreferences.Companion.getInstance().cacheUserId(activity, user.getUsername());
//                    activity.onCredentialsSubmitted();
//                }
            } else if (state instanceof NetworkState2.Error) {
                NetworkState2.Error<?> error = (NetworkState2.Error<?>) state;
                activity.onError(error.getMessage());
            } else if (state instanceof NetworkState2.Failure) {
                NetworkState2.Failure<?> failure = (NetworkState2.Failure<?>) state;
                activity.onFailure(activity.findViewById(R.id.root_view), failure.getThrowable());
            } else {
                activity.onFailure(activity.findViewById(R.id.root_view), new Throwable(ConstantsKt.CONNECTION_ERROR));
            }
        });
    }

    private void setUsernameAvailable(TextInputEditText editText, @DrawableRes int leftIcon, @DrawableRes int res) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            editText.setCompoundDrawablesRelativeWithIntrinsicBounds(leftIcon, 0, res, 0);
        } else {
            editText.setCompoundDrawablesWithIntrinsicBounds(leftIcon, 0, res, 0);
        }
    }

    private boolean validateEditText() {
        if (etUserID.getText() != null && TextUtils.isEmpty(etUserID.getText().toString())) {
            setErrorMessage(tilUserID, getString(R.string.empty_user_id));
            etUserID.requestFocus();
            return false;
        } else if (etUserID.getText() != null && !UsernameValidator.Companion.validate(etUserID.getText().toString())) {
            setErrorMessage(tilUserID, getString(R.string.invalid_user_id));
            etUserID.requestFocus();
            return false;
        } else if (etPassword.getText() != null && TextUtils.isEmpty(etPassword.getText().toString())) {
            setErrorMessage(tilPassword, getString(R.string.empty_password));
            etPassword.requestFocus();
            return false;
        } else if (etPassword.getText() != null && !PasswordValidator.Companion
                .validate(etPassword.getText().toString().trim())) {
            setErrorMessage(tilPassword, getString(R.string.password_pattern_info));
            etPassword.requestFocus();
            return false;
        } else if (etPassword.getText() != null && etPassword.getText().toString()
                .contains(etUserID.getText().toString().replaceAll("\\d", ""))) {
            setErrorMessage(tilPassword, getString(R.string.password_contains_userid));
            etPassword.requestFocus();
            return false;
        } else if (etConfirmPassword.getText() != null && TextUtils.isEmpty(etConfirmPassword.getText().toString())) {
            setErrorMessage(tilConfirmPassword, getString(R.string.empty_confirm_password));
            etConfirmPassword.requestFocus();
            return false;
        } else if (etConfirmPassword.getText() != null && !etConfirmPassword.getText().toString().equals(etPassword.getText().toString())) {
            setErrorMessage(tilConfirmPassword, getString(R.string.password_not_match));
            etConfirmPassword.requestFocus();
            return false;
        } else {
            clearErrorMessage(tilUserID);
            clearErrorMessage(tilPassword);
            clearErrorMessage(tilConfirmPassword);
            return true;
        }
    }
}
