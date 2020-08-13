package com.fh.payday.views2.moneytransfer;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.datasource.models.AmountLimit;
import com.fh.payday.datasource.models.Card;
import com.fh.payday.datasource.models.login.User;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.preferences.UserPreferences;
import com.fh.payday.utilities.DecimalDigitsInputFilter;
import com.fh.payday.utilities.NumberFormatterKt;
import com.fh.payday.utilities.TextInputUtilsKt;
import com.fh.payday.utilities.TextViewUtilsKt;

public class TransferDetailsFragment extends Fragment {

    private InternationalMoneyTransfer activity;
    private TextView tvBalance;
    private TextView tvBeneficiary;
    private TextView tvNumber;
    private TextInputEditText etAmount, etPurpose;
    private TextInputLayout layout, textInputLayout;
    private ProgressBar progressBar;
    private Card card = null;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (InternationalMoneyTransfer) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (activity != null) {
            activity.hideKeyboard();
        }

        if(isVisibleToUser) {
          /*  if (activity != null && activity.getViewModel().getAmountLimit() == null) {
//                getAmountLimit();
            }*/


            if (layout != null) {
                TextInputUtilsKt.clearErrorMessage(layout);
                handleView();
            }
        }

        if (isVisibleToUser && activity != null && activity.getViewModel().getDataClear()) {
            etAmount.setText(null);
            etPurpose.setText(null);
        }
    }

    private void getAmountLimit() {
        if (activity.getViewModel() != null) {
            User user = UserPreferences.Companion.getInstance().getUser(activity);
            if (user == null) return;

            if (activity.getViewModel().getTransferType() == MoneyTransferType.PAYDAY)
                activity.getViewModel().getAmountLimit(user.getToken(), user.getSessionId(), user.getRefreshToken(),
                        user.getCustomerId(), InternationalMoneyTransfer.P2P_TRANSFER_KEY);
            else if (activity.getViewModel().getTransferType() == MoneyTransferType.LOCAL) {
                activity.getViewModel().getAmountLimit(user.getToken(), user.getSessionId(), user.getRefreshToken(),
                        user.getCustomerId(), InternationalMoneyTransfer.LOCAL_TRANSFER_KEY);
            } else if (activity.getViewModel().getTransferType() == MoneyTransferType.CC) {
                activity.getViewModel().getAmountLimit(user.getToken(), user.getSessionId(), user.getRefreshToken(),
                        user.getCustomerId(), InternationalMoneyTransfer.CC_TRANSFER_KEY);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        handleView();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_transfer_details, container, false);
        init(view);
        activity.setupFocusOutside(view.findViewById(R.id.constraint_layout));
        view.findViewById(R.id.iv_next).setOnClickListener(v -> {
            navigateNext();
        });

        addAmountLimitObserver();
        etAmount.setFilters(new InputFilter[] { new DecimalDigitsInputFilter(7, 2) });

        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextInputUtilsKt.clearErrorMessage(layout);
                if (etAmount.length() == 1 && etAmount.getText().toString().equals(".")) {
                    etAmount.setText("0.");
                    etAmount.setSelection(etAmount.getText().length());
                }
                if (etAmount.length() == 2 && etAmount.getText().toString().equals("00")){
                    etAmount.setText("0");
                    etAmount.setSelection(etAmount.getText().length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etPurpose.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextInputUtilsKt.clearErrorMessage(textInputLayout);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        view.findViewById(R.id.iv_previous).setOnClickListener(v -> {
            TextInputUtilsKt.clearErrorMessage(layout);
            activity.onBackPressed();
        });
        return view;
    }

    private void navigateNext() {
        if (activity.getViewModel().getAmountLimit() == null) {
            getAmountLimit();
        } else if (validate(etAmount.getText().toString())) {
            if (TextUtils.isEmpty(etPurpose.getText().toString())) {
                activity.getViewModel().setPurpose("-");
            } else if (TextInputUtilsKt.isChar(etPurpose.getText().toString())){
                activity.getViewModel().setPurpose(etPurpose.getText().toString());
            } else {
                textInputLayout.setError("Invalid characters");
                return;
            }
            TextInputUtilsKt.clearErrorMessage(layout);
            activity.getViewModel().setAmount(etAmount.getText().toString());
            activity.onAmount();
        }

    }

    private boolean validate(String amount) {


        AmountLimit amountLimit = activity.getViewModel().getAmountLimit();

        if (TextUtils.isEmpty(amount) || Double.parseDouble(amount) == 0) {
            TextInputUtilsKt.setErrorMessage(layout, getString(R.string.invalid_amount));
            return false;
        } else if (Double.parseDouble(etAmount.getText().toString()) > Double.parseDouble(card.getAvailableBalance())) {
            TextInputUtilsKt.setErrorMessage(layout, getString(R.string.insufficient_balance));
            return false;
        }

        if (activity.getViewModel().getAmountLimit() != null) {
            if (Double.parseDouble(etAmount.getText().toString()) < Double.parseDouble(amountLimit.getMinAmount())
                    || Double.parseDouble(etAmount.getText().toString()) > Double.parseDouble(amountLimit.getMaxAmount()) ) {
                TextInputUtilsKt.setErrorMessage(layout, "Please enter an amount between "+String.format(getString(R.string.amount_in_aed),NumberFormatterKt.getDecimalValue(amountLimit.getMinAmount()))+" and "
                        +String.format(getString(R.string.amount_in_aed),NumberFormatterKt.getDecimalValue(amountLimit.getMaxAmount())));
                return false;
            }
        }

        return true;
    }

    private void init(View view) {
        tvBalance = view.findViewById(R.id.tv_available_balance);
        TextView tvCurrency = view.findViewById(R.id.tv_currency);
        tvBeneficiary = view.findViewById(R.id.tv_name);
        tvNumber = view.findViewById(R.id.tv_number);
        etAmount = view.findViewById(R.id.et_amount);
        etPurpose = view.findViewById(R.id.et_purpose);
        layout = view.findViewById(R.id.textInputLayout);
        textInputLayout = view.findViewById(R.id.textInputLayout2);
        ConstraintLayout constraintLayout = view.findViewById(R.id.layout_beneficiary);
        progressBar = view.findViewById(R.id.progressBar);
        etAmount.setHint(getString(R.string.aed));

        etPurpose.setOnEditorActionListener((v, actionId, event) -> {
            navigateNext();
            return false;
        });

        LinearLayout linearLayout = view.findViewById(R.id.linear_layout);

        Bundle bundle = getArguments();
        if (bundle == null) return;

        MoneyTransferType transferType = activity.getViewModel().getTransferType();
        if (transferType == null) return;

        switch (transferType){
            case CC:
                tvCurrency.setVisibility(View.GONE);
                linearLayout.setVisibility(View.VISIBLE);
                constraintLayout.setVisibility(View.GONE);
                break;
            case LOCAL:
                tvCurrency.setVisibility(View.GONE);
                linearLayout.setVisibility(View.VISIBLE);
                constraintLayout.setVisibility(View.GONE);
                break;
            case PAYDAY:
                tvCurrency.setVisibility(View.GONE);
                linearLayout.setVisibility(View.GONE);
                break;
        }
    }

    private void handleView() {
        MoneyTransferType transferType = activity.getViewModel().getTransferType();
        Card cards = activity.getCard();

        if (transferType == null) return;
        switch (transferType) {
            case PAYDAY:
                if (activity == null || activity.getViewModel() == null || activity.getViewModel().getSelectedP2PBeneficiary() == null)
                    return;
                setBalance(cards, tvBalance);
                tvBeneficiary.setText(activity.getViewModel().getSelectedP2PBeneficiary().getBeneficiaryName());
                //tvNumber.setText(activity.getViewModel().getSelectedP2PBeneficiary().getMobileNumber());
                TextViewUtilsKt.replaceZero(tvNumber, activity.getViewModel().getSelectedP2PBeneficiary().getMobileNumber());
                break;
            case LOCAL:
                if (activity == null || activity.getViewModel() == null || activity.getViewModel().getSelectedLocalBeneficiary() == null)
                    return;
                setBalance(cards, tvBalance);
                break;
            case CC:
                if (activity == null || activity.getViewModel() == null || activity.getViewModel().getSelectedP2CBeneficiary() == null)
                    return;
                setBalance(cards, tvBalance);
                break;
        }
    }

    private void setBalance(Card cards, TextView tvBalance) {
        card = cards;

        if(activity.getViewModel().getCardBalance() != null) {
            tvBalance.setText(String.format(getString(R.string.available_balance_in_aed),
                    NumberFormatterKt.getDecimalValue(activity.getViewModel().getCardBalance())));
        } else {
            tvBalance.setText(String.format(getString(R.string.available_balance_in_aed), "0.00"));
        }
    }


    private void addAmountLimitObserver() {
        activity.getViewModel().getAmountLimitResponse().observe(this, event-> {
            if (event == null) return;

            NetworkState2<AmountLimit> response = event.getContentIfNotHandled();

            if (response == null) return;

            if (response instanceof NetworkState2.Loading) {
                etAmount.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                return;
            }

            etAmount.setEnabled(true);
            progressBar.setVisibility(View.GONE);

            if (response instanceof NetworkState2.Success) {
                activity.getViewModel().setAmountLimit(((NetworkState2.Success<AmountLimit>) response).getData());
                if(validate(etAmount.getText().toString())) {
                    TextInputUtilsKt.clearErrorMessage(layout);
                    activity.getViewModel().setAmount(etAmount.getText().toString());
                    activity.onAmount();
                }
            } else if(response instanceof NetworkState2.Error) {
                NetworkState2.Error<?> error = (NetworkState2.Error<?>) response;

                if (error.isSessionExpired()) {
                    activity.onSessionExpired(error.getMessage());
                    return;
                }

                activity.handleErrorCode(
                        Integer.parseInt(error.getErrorCode()), error.getMessage()
                );

//                if(validate(etAmount.getText().toString())) {
//                    TextInputUtilsKt.clearErrorMessage(layout);
//                    activity.getViewModel().setAmount(etAmount.getText().toString());
//                    activity.onAmount();
//                }

            } else if (response instanceof NetworkState2.Failure) {
                if(validate(etAmount.getText().toString())) {
                    TextInputUtilsKt.clearErrorMessage(layout);
                    activity.getViewModel().setAmount(etAmount.getText().toString());
                    activity.onAmount();
                }
            } else {
                if(validate(etAmount.getText().toString())) {
                    TextInputUtilsKt.clearErrorMessage(layout);
                    activity.getViewModel().setAmount(etAmount.getText().toString());
                    activity.onAmount();
                }
            }

        });
    }
}
