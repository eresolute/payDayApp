package com.fh.payday.views2.settings.update_card_pin;

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
import com.fh.payday.viewmodels.SettingOptionViewModel;
import com.fh.payday.views2.settings.SettingOptionActivity;

import static com.fh.payday.utilities.TextInputUtilsKt.clearErrorMessage;
import static com.fh.payday.utilities.TextInputUtilsKt.setErrorMessage;

public class ConfirmCardPinFragment extends Fragment {

    SettingOptionViewModel viewModel;
    MaterialButton btnConfirm;
    @Nullable
    private User user;
    TextInputEditText etPin;
    TextInputLayout textInputLayout;
    private SettingOptionActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (SettingOptionActivity) context;
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser && etPin != null) {
            etPin.setText(null);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = UserPreferences.Companion.getInstance().getUser(activity);
        if (user == null) return;

        viewModel = ViewModelProviders.of(activity).get(SettingOptionViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_confirm_card_pin_fragment, container, false);
        init(view);

        btnConfirm.setOnClickListener(v -> {

            if (TextUtils.isEmpty(etPin.getText().toString().trim()) || etPin.getText().toString().trim().length() < 4
                    || !etPin.getText().toString().matches("^[0-9]{4}$")) {

                setErrorMessage(textInputLayout, getString(R.string.please_enter_pin));
                etPin.requestFocus();
            } else {
                if (user == null) return;
                if (activity.getPinResetViewModel().getPin().getValue().equals(etPin.getText().toString())) {
                    if (!activity.isNetworkConnected()) {
                        activity.onFailure(activity.findViewById(R.id.card_view), getString(R.string.no_internet_connectivity));
                        return;
                    }
                    activity.getPinResetViewModel().getOtp(user.getToken(), user.getSessionId(), user.getRefreshToken(),user.getCustomerId());
                    addOtpObserver();
                } else {
                    setErrorMessage(textInputLayout, getString(R.string.pin_does_not_match));
                    etPin.requestFocus();
                }
            }
        });

        etPin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                clearErrorMessage(textInputLayout);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        return view;
    }

    private void init(View view) {
        etPin = view.findViewById(R.id.et_pin);
        btnConfirm = view.findViewById(R.id.btn_confirm);
        textInputLayout = view.findViewById(R.id.textInputLayout);
    }

    private void addOtpObserver() {
        activity.getPinResetViewModel().getOptResponseState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<String> otpResponse = event.getContentIfNotHandled();


            if (otpResponse == null) return;

            if (otpResponse instanceof NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing));
                btnConfirm.setEnabled(false);
                return;
            }

            activity.hideProgress();
            btnConfirm.setEnabled(true);

            if (otpResponse instanceof NetworkState2.Success) {
                viewModel.setSelected(3);
            } else if (otpResponse instanceof NetworkState2.Error) {
                NetworkState2.Error<String> error = (NetworkState2.Error<String>) otpResponse;
                if (error.isSessionExpired()) {
                    activity.onSessionExpired(error.getMessage());
                    return;
                }
                activity.onError(error.getMessage());
            } else if (otpResponse instanceof NetworkState2.Failure) {
                activity.onFailure(activity.findViewById(R.id.card_view), ((NetworkState2.Failure<String>) otpResponse).getThrowable());
            } else {
                activity.onFailure(activity.findViewById(R.id.card_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }

}
