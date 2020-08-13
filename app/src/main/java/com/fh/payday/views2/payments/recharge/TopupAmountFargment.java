package com.fh.payday.views2.payments.recharge;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.datasource.models.login.User;
import com.fh.payday.datasource.models.payments.BillDetailDU;
import com.fh.payday.datasource.models.payments.OperatorDetail;
import com.fh.payday.datasource.models.payments.RechargeDetail;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.preferences.UserPreferences;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.utilities.DecimalDigitsInputFilter;
import com.fh.payday.utilities.NumberFormatterKt;
import com.fh.payday.viewmodels.payments.billpayment.MobileRechargeViewModel;
import com.fh.payday.views2.shared.custom.EligibilityDialogFragment;

import static com.fh.payday.utilities.TextInputUtilsKt.clearErrorMessage;
import static com.fh.payday.utilities.TextInputUtilsKt.setErrorMessage;

public class TopupAmountFargment extends Fragment {

    private MobileRechargeActivity activity;
    private TextInputEditText etAmount;
    private TextInputLayout etLayout;
    private MobileRechargeViewModel viewModel;
    private RechargeDetail rechargeDetail;
    private BillDetailDU billDetailDU;
    private Button btnConfirm;
    private OperatorDetail operatorDetail;
    private TextView tvPayableAmount, tvTitle;
    private ImageView imgEditAmount;
    private TextView tvError;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MobileRechargeActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (viewModel != null && viewModel.getTYPE_ID().getValue() == 1 && tvTitle != null && tvPayableAmount != null && isVisibleToUser) {
            if (viewModel.getBillDetailEtisalat().getValue() != null) {

                if (viewModel.getAMOUNT().getValue() == null || viewModel.getAMOUNT().getValue().isEmpty()) {
                    tvTitle.setText(getString(R.string.bill_amount));
                    tvPayableAmount.setText(String.format(getString(R.string.amount_in_aed), NumberFormatterKt.getDecimalValue(viewModel.getBillDetailEtisalat().getValue().getAmountDue())));
                    tvPayableAmount.setVisibility(View.VISIBLE);
                    imgEditAmount.setVisibility(View.VISIBLE);
                    etLayout.setVisibility(View.GONE);
                    etAmount.setText(NumberFormatterKt.getDecimalValue(viewModel.getBillDetailEtisalat().getValue().getAmountDue()));
                } else {
                    tvPayableAmount.setVisibility(View.GONE);
                    imgEditAmount.setVisibility(View.GONE);
                    etLayout.setVisibility(View.VISIBLE);
                    etAmount.setText(NumberFormatterKt.getDecimalValue(viewModel.getAMOUNT().getValue()));
                }

            } else if (viewModel.getBillDetailDU().getValue() != null) {

                if (viewModel.getTYPE_ID().getValue() == 1) {
                    tvTitle.setText(getString(R.string.bill_amount));
                    tvPayableAmount.setText(String.format(getString(R.string.amount_in_aed), NumberFormatterKt.getDecimalValue(viewModel.getAMOUNT().getValue())));
                    tvPayableAmount.setVisibility(View.VISIBLE);
                    imgEditAmount.setVisibility(View.VISIBLE);
                    etLayout.setVisibility(View.GONE);
                    etAmount.setText(NumberFormatterKt.getDecimalValue(viewModel.getAMOUNT().getValue()));
                } else {
                    tvPayableAmount.setVisibility(View.GONE);
                    imgEditAmount.setVisibility(View.GONE);
                    etLayout.setVisibility(View.VISIBLE);
                    etAmount.setText(NumberFormatterKt.getDecimalValue(viewModel.getAMOUNT().getValue()));
                }
            }
        }

        if (activity == null) return;
        if (activity.getViewModel() == null) return;

        if (isVisibleToUser && activity.getViewModel().getDataClear() && activity.getViewModel().getTYPE_ID().getValue() == 2) {
            etAmount.setText(null);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_utilities_amount, container, false);
        etAmount = view.findViewById(R.id.et_amount);
        etLayout = view.findViewById(R.id.til_amount);
        tvPayableAmount = view.findViewById(R.id.tv_payable_amount);
        tvTitle = view.findViewById(R.id.tvTitle);
        imgEditAmount = view.findViewById(R.id.img_edit_amount);
        tvError = view.findViewById(R.id.tv_error_label);

        etAmount.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(10, 2)});

        addTextWatcher();
        addOtpObserver();

        btnConfirm = view.findViewById(R.id.btn_next);
        btnConfirm.setText(getString(R.string.pay));
        etAmount.setHint(getString(R.string.aed));

        if (activity.getViewModel() != null) {
            viewModel = activity.getViewModel();
            viewModel.getTopUpDetail().observe(this, bill -> {
                rechargeDetail = bill;
            });
            viewModel.getOperatorDetail().observe(this, operatorDetail1 ->
                    operatorDetail = operatorDetail1);

        }

        imgEditAmount.setOnClickListener(v -> {
            tvPayableAmount.setVisibility(View.GONE);
            imgEditAmount.setVisibility(View.GONE);
            etLayout.setVisibility(View.VISIBLE);
            tvError.setVisibility(View.GONE);
        });

        btnConfirm.setOnClickListener(v -> {

            if (UserPreferences.Companion.getInstance().getUser(activity) == null) return;

            User user = activity.getViewModel().getUser();
            if(user == null) return;

            if (validate(etAmount.getText().toString())) {

                if (viewModel == null) {
                    return;
                }

                if (Float.parseFloat(etAmount.getText().toString()) < Float.parseFloat(operatorDetail.getMinDenomination())
                        || Float.parseFloat(etAmount.getText().toString()) > Float.parseFloat(operatorDetail.getMaxDenomination())) {
                    setErrorMessage(etLayout,String.format(getString(R.string.invalid_amount_ranged),
                            operatorDetail.getMinDenomination(), operatorDetail.getMaxDenomination()));

                    if (tvPayableAmount.getVisibility() == View.VISIBLE) {
                        tvError.setVisibility(View.VISIBLE);
                        setErrorMessage(etLayout,String.format(getString(R.string.invalid_amount_ranged),
                                operatorDetail.getMinDenomination(), operatorDetail.getMaxDenomination()));
                    } else {
                        tvError.setVisibility(View.GONE);
                    }
                } else if (activity.getViewModel().getOperator().equalsIgnoreCase("etisalat")
                        && viewModel.getTYPE_ID().getValue() == 1
                        && parseAmount(viewModel.getBillDetailEtisalat().getValue().getAmountDueInAED())
                        < parseAmount(etAmount.getText().toString())) {
                    new EligibilityDialogFragment.Builder(dialog -> {

                    }).setBtn1Text(getString(R.string._continue))
                            .setBtn2Text(getString(R.string.cancel))
                            .setConfirmListener(dialog -> {
                                viewModel.getAMOUNT().setValue(etAmount.getText().toString());
                                activity.onTopupAmount();
                                dialog.dismiss();
                            })
                            .setCancelListener(DialogInterface::dismiss)
                            .setTitle(getDisplayMessage(etAmount.getText().toString(),
                                    viewModel.getBillDetailEtisalat().getValue().getAmountDueInAED()))
                            .build()
                            .show(getFragmentManager(), "PasswordUpdate");
                    return;
                } else if (activity.getViewModel().getOperator().equalsIgnoreCase("du")
                        && viewModel.getTYPE_ID().getValue() == 1
                        && parseAmount(viewModel.getBillDetailDU().getValue().getDueBalanceInAed())
                        < parseAmount(etAmount.getText().toString())) {
                    new EligibilityDialogFragment.Builder(dialog -> {

                    }).setBtn1Text(getString(R.string._continue))
                            .setBtn2Text(getString(R.string.cancel))
                            .setConfirmListener(dialog -> {
                                viewModel.getAMOUNT().setValue(etAmount.getText().toString());
                                activity.onTopupAmount();
                                dialog.dismiss();
                            })
                            .setCancelListener(DialogInterface::dismiss)
                            .setTitle(getDisplayMessage(etAmount.getText().toString(),
                                    viewModel.getBillDetailDU().getValue().getDueBalanceInAed()))
                            .build()
                            .show(getFragmentManager(), "PasswordUpdate");
                    return;
                } else if (activity.getViewModel().getTYPE_ID().getValue() == 2
                        && activity.getViewModel().getOperator().equalsIgnoreCase("etisalat")) {

                    viewModel.getAMOUNT().setValue(etAmount.getText().toString());
                    activity.onTopupAmount();

                } else if (activity.getViewModel().getTYPE_ID().getValue() == 2){
                    viewModel.getAMOUNT().setValue(etAmount.getText().toString());
                    activity.onTopupAmount();

                } else if (activity.getViewModel().getTYPE_ID().getValue() == 1) {
                    viewModel.getAMOUNT().setValue(etAmount.getText().toString());
                    activity.onTopupAmount();
//                    activity.getViewModel().getOtp(user.getToken(), user.getSessionId(), user.getRefreshToken(), user.getCustomerId());
                }
            }
        });


        return view;
    }

    private String getDisplayMessage(String etAmount, String billAmount) {
        String billedAmount = String.format(getString(R.string.amount_in_aed), NumberFormatterKt.getDecimalValue(billAmount));
        String actualAmount = String.format(getString(R.string.amount_in_aed), NumberFormatterKt.getDecimalValue(etAmount));
        return String.format(getString(R.string.bill_info), billedAmount, actualAmount);
    }

    private Double parseAmount(String amountDueInAED) {
        String amount = amountDueInAED.replace(',', ' ');
        return Double.parseDouble(amount);
    }

    private void addTextWatcher() {
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearErrorMessage(etLayout);
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
    }

    private boolean validate(String amount) {

        if (TextUtils.isEmpty(amount)) {
            setErrorMessage(etLayout, getString(R.string.invalid_amount));
            if (tvPayableAmount.getVisibility() == View.VISIBLE) {
                tvError.setVisibility(View.VISIBLE);
                tvError.setText(getString(R.string.invalid_amount));
            }
            return false;
        } else return !checkAmountRange(amount);

    }

    private boolean checkAmountRange(String amount) {

        if (activity == null || activity.getViewModel() == null || activity.getViewModel().getSERVICE().getValue() == null
            || rechargeDetail == null)
            return false;

        String service = activity.getViewModel().getSERVICE().getValue();

        switch (service) {

            case "WaselRecharge":
                if (Double.parseDouble(amount) < Double.parseDouble(operatorDetail.getMinDenomination())
                        || Double.parseDouble(amount) > Double.parseDouble(operatorDetail.getMaxDenomination()) ) {
                    setErrorMessage(etLayout,String.format(getString(R.string.invalid_amount_ranged),
                            operatorDetail.getMinDenomination(), operatorDetail.getMaxDenomination()));
                    return true;
                }
                break;
            case "WaselRenewal":
                if (Double.parseDouble(amount) < Double.parseDouble(operatorDetail.getMinDenomination())
                        || Double.parseDouble(amount) > Double.parseDouble(operatorDetail.getMaxDenomination()) ) {
                    setErrorMessage(etLayout, "Please enter an amount between "
                            +operatorDetail.getMaxDenomination()
                            +" AED and "+operatorDetail.getMaxDenomination()+" AED");
                    return true;
                }
                break;
        }

        return false;
    }

    private void addOtpObserver() {
        activity.getViewModel().getOptResponseState().observe(this, event -> {
            if(event == null) return;
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
                if (Integer.valueOf(2).equals(activity.getViewModel().getTYPE_ID().getValue())) {
                    activity.onTopupDetails();
                } else {
                    activity.onTopupAmount();
                }
            } else if(otpResponse instanceof NetworkState2.Error) {

                NetworkState2.Error<String> error = (NetworkState2.Error<String>) otpResponse;
                if (error.isSessionExpired()) {
                    activity.onSessionExpired(error.getMessage());
                    return;
                }
//                activity.onError(error.getMessage());
                activity.handleErrorCode(Integer.parseInt(error.getErrorCode()), error.getMessage());
            } else if (otpResponse instanceof NetworkState2.Failure) {
                NetworkState2.Failure<String> error = (NetworkState2.Failure<String>) otpResponse;
                activity.onFailure(activity.findViewById(R.id.card_view), error.getThrowable());

            } else {
                activity.onFailure(activity.findViewById(R.id.card_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }
}
