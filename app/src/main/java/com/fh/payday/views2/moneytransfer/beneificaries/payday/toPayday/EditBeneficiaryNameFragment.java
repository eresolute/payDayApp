package com.fh.payday.views2.moneytransfer.beneificaries.payday.toPayday;

import android.content.Context;
import android.os.Bundle;
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

import com.fh.payday.R;
import com.fh.payday.datasource.models.login.User;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.preferences.UserPreferences;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.viewmodels.SharedViewModel;

import static com.fh.payday.utilities.TextInputUtilsKt.clearErrorMessage;

public class EditBeneficiaryNameFragment extends Fragment {
    SharedViewModel model;
    int index;
    private TextInputEditText etName;
    private TextInputLayout textInputLayout;
    PaydayBeneficiaryActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (PaydayBeneficiaryActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        clearErrorMessage(textInputLayout);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_beneficiary_name, container, false);
        init(view);
        if (activity.getViewModel().getSelectedBeneficiary() == null)
            return view;
        etName.setText(activity.getViewModel().getSelectedBeneficiary().getBeneficiaryName());
        addOtpObserver();
        return view;
    }

    private void init(View view) {
        etName = view.findViewById(R.id.et_name);
        textInputLayout = view.findViewById(R.id.textInputLayoutName);
        view.findViewById(R.id.img_next).setOnClickListener(v -> {
            if (etName.getText() == null)
                return;
            activity.getViewModel().setBeneficiaryName(etName.getText().toString());
            User user = UserPreferences.Companion.getInstance().getUser(activity);
            if (user == null) return;
            if (TextUtils.isEmpty(etName.getText())){
                textInputLayout.setError(getString(R.string.invalid_name));
                return;
            }
            activity.getViewModel().getOtp(user.getToken(), user.getSessionId(), user.getRefreshToken(), user.getCustomerId());
        });

        etName.addTextChangedListener(new TextWatcher() {
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

        view.findViewById(R.id.img_previous).setOnClickListener((View v) -> activity.onBackPressed());
    }

    private void addOtpObserver() {
        if (activity == null) return;
        activity.getViewModel().getOtpState().observe(this, event -> {

            if(event == null) return;

            NetworkState2<String> otpResponse = event.getContentIfNotHandled();

            if (otpResponse == null) return;

            if (otpResponse instanceof NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing));
                return;
            }

            activity.hideProgress();

            if (otpResponse instanceof NetworkState2.Success) {
                activity.navigateUp();
            } else if(otpResponse instanceof NetworkState2.Error) {
                NetworkState2.Error<String> error = (NetworkState2.Error<String>) otpResponse;
                activity.onError(error.getMessage());

            } else if (otpResponse instanceof NetworkState2.Failure) {

                NetworkState2.Failure<String> error = (NetworkState2.Failure<String>) otpResponse;
                activity.onFailure(activity.findViewById(R.id.card_view), getString(R.string.request_error));

            } else {
                activity.onFailure(activity.findViewById(R.id.card_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }
}
