package com.fh.payday.views2.payments.utilities;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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
import com.fh.payday.datasource.models.payments.UtilityServiceType;
import com.fh.payday.datasource.models.payments.utilities.OperatorDetails;
import com.fh.payday.utilities.DecimalDigitsInputFilter;
import com.fh.payday.utilities.NumberFormatterKt;
import com.fh.payday.utilities.TextInputUtilsKt;
import com.fh.payday.views2.shared.custom.EligibilityDialogFragment;

import static com.fh.payday.utilities.TextInputUtilsKt.clearErrorMessage;
import static com.fh.payday.utilities.TextInputUtilsKt.setErrorMessage;

public class UtilitiesAmountFragment extends Fragment {
    private UtilitiesActivity activity;
    private OperatorDetails operatorDetails;
    private TextInputLayout tilAmount;
    private TextInputEditText etAmount;
    private TextView tvAmount;
    private ImageView imgEditAmount;
    private Button bttnNext;
    private TextView tvError;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (UtilitiesActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (activity == null) return;
        if (activity.getViewModel() == null) return;
        if (activity.getViewModel().getOperator() == null) return;
        if (isVisibleToUser && activity.getViewModel().getDataClear() && activity.getViewModel().getOperator().equals(UtilityServiceType.AADC)) {
            etAmount.setText(null);
        }

        if (activity.getViewModel().getOperator().equals(UtilityServiceType.AADC) && activity.getViewModel().getAmount().getValue() == null) {
            if (activity.getViewModel().getOperator().equals(UtilityServiceType.AADC)) {
                tvAmount.setVisibility(View.VISIBLE);
                imgEditAmount.setVisibility(View.VISIBLE);
                tilAmount.setVisibility(View.GONE);
                tvError.setVisibility(View.GONE);
//                    etAmount.setText(NumberFormatterKt.getDecimalValue(bill.getOutstandingAmountInAED()));
                TextInputUtilsKt.extractAmount(etAmount, NumberFormatterKt.getDecimalValue(activity.getViewModel().getAadcBillDetail().getValue().getDueBalanceInAed()));
//                    etAmount.setText(bill.getOutstandingAmountInAED());
//                    tvLastBillAmount.setText(String.format(getString(R.string.amount_in_aed), NumberFormatterKt.getDecimalValue(String.valueOf(bill.getPreviousBalanceInAED()))));
                tvAmount.setText(String.format(getString(R.string.amount_in_aed), NumberFormatterKt.getDecimalValue(activity.getViewModel().getAadcBillDetail().getValue().getDueBalanceInAed())));
//                    tvDueDate.setText(DateTime.Companion.parse(bill.getBillDate(), "dd/MM/yyyy"));
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_utilities_amount, container, false);
        init(view);

        addTextWatcher();

        if (activity.getViewModel() != null) {

            activity.getViewModel().getBillDetails().observe(this, bill -> {
                if (bill != null && activity.getViewModel().getAmount().getValue() == null) {
                    tvAmount.setVisibility(View.VISIBLE);
                    imgEditAmount.setVisibility(View.VISIBLE);
                    tilAmount.setVisibility(View.GONE);
                    tvError.setVisibility(View.GONE);
//                    etAmount.setText(NumberFormatterKt.getDecimalValue(bill.getOutstandingAmountInAED()));
                    TextInputUtilsKt.extractAmount(etAmount, NumberFormatterKt.getDecimalValue(bill.getOutstandingAmountInAED()));
//                    etAmount.setText(bill.getOutstandingAmountInAED());
//                    tvLastBillAmount.setText(String.format(getString(R.string.amount_in_aed), NumberFormatterKt.getDecimalValue(String.valueOf(bill.getPreviousBalanceInAED()))));
                    tvAmount.setText(String.format(getString(R.string.amount_in_aed), NumberFormatterKt.getDecimalValue(bill.getOutstandingAmountInAED())));
//                    tvDueDate.setText(DateTime.Companion.parse(bill.getBillDate(), "dd/MM/yyyy"));
                }
            });

            activity.getViewModel().getOperatorDetails().observe(this, operatorDetail1 -> operatorDetails = operatorDetail1);
        }
        bttnNext.setOnClickListener(v -> {
            if (validateEditText(etAmount.getText().toString().trim())) {
                activity.getViewModel().getAmount().setValue(etAmount.getText().toString());
                onSuccess(activity);
            }
        });

        imgEditAmount.setOnClickListener(v -> {
            tvAmount.setVisibility(View.GONE);
            tvError.setVisibility(View.GONE);
            imgEditAmount.setVisibility(View.GONE);
            tilAmount.setVisibility(View.VISIBLE);
        });

        return view;
    }

    private void init(View view) {
        tilAmount = view.findViewById(R.id.til_amount);
        etAmount = view.findViewById(R.id.et_amount);
        bttnNext = view.findViewById(R.id.btn_next);
        tvAmount = view.findViewById(R.id.tv_payable_amount);
        imgEditAmount = view.findViewById(R.id.img_edit_amount);
        tvError = view.findViewById(R.id.tv_error_label);
        etAmount.setHint(getString(R.string.aed));
        etAmount.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(10, 2)});
    }

    private Boolean validateEditText(String amount) {

        if (activity.getViewModel() == null) {
            return false;
        }
        if (TextUtils.isEmpty(amount)) {
            setErrorMessage(tilAmount, getString(R.string.invalid_amount));
            if (tvAmount.getVisibility() == View.VISIBLE)
                tvError.setVisibility(View.VISIBLE);
            else
                tvError.setVisibility(View.GONE);

            tvError.setText(getString(R.string.invalid_amount));
            etAmount.requestFocus();
            return false;
        } else if (Float.parseFloat(amount) < Float.parseFloat(operatorDetails.getMinDenomination())
                || Float.parseFloat(amount) > Float.parseFloat(operatorDetails.getMaxDenomination())) {
            setErrorMessage(tilAmount,String.format(getString(R.string.invalid_amount_ranged),
                    operatorDetails.getMinDenomination(), operatorDetails.getMaxDenomination()));

            etAmount.requestFocus();
            if (tvAmount.getVisibility() == View.VISIBLE)
                tvError.setVisibility(View.VISIBLE);
            else
                tvError.setVisibility(View.GONE);

            setErrorMessage(tilAmount,String.format(getString(R.string.invalid_amount_ranged),
                    operatorDetails.getMinDenomination(), operatorDetails.getMaxDenomination()));

            return false;
        } else if (activity.getViewModel().getOperator().equalsIgnoreCase("aadc")
                && parseAmount(activity.getViewModel().getAadcBillDetail().getValue().getDueBalanceInAed())
                < parseAmount(etAmount.getText().toString())){
            new EligibilityDialogFragment.Builder(dialog -> {

            }).setBtn1Text(getString(R.string._continue))
                    .setBtn2Text(getString(R.string.cancel))
                    .setConfirmListener(dialog -> {
                        activity.getViewModel().getAmount().setValue(etAmount.getText().toString());
                        onSuccess(activity);
                        dialog.dismiss();
                    })
                    .setCancelListener(DialogInterface::dismiss)
                    .setTitle(getDisplayMessage(etAmount.getText().toString(),
                            activity.getViewModel().getAadcBillDetail().getValue().getDueBalanceInAed()))
                    .build()
                    .show(getFragmentManager(), "PasswordUpdate");
            return false;
        } else if (activity.getViewModel().getOperator().equalsIgnoreCase("fewa")
                && parseAmount(activity.getViewModel().getFewaBill().getBalanceInAED())
                < parseAmount(etAmount.getText().toString())){
            new EligibilityDialogFragment.Builder(dialog -> {

            }).setBtn1Text(getString(R.string._continue))
                    .setBtn2Text(getString(R.string.cancel))
                    .setConfirmListener(dialog -> {
                        activity.getViewModel().getAmount().setValue(etAmount.getText().toString());
                        onSuccess(activity);
                        dialog.dismiss();
                    })
                    .setCancelListener(DialogInterface::dismiss)
                    .setTitle(getDisplayMessage(etAmount.getText().toString(),
                            activity.getViewModel().getFewaBill().getOutstandingAmountInAED()))
                    .build()
                    .show(getFragmentManager(), "PasswordUpdate");
            return false;
        } else {
            clearErrorMessage(tilAmount);
            tvError.setVisibility(View.GONE);
            return true;
        }
    }

    private Double parseAmount(String amountDueInAED) {
        String amount = amountDueInAED.replace(',', ' ');
        return Double.parseDouble(amount);
    }

    private String getDisplayMessage(String etAmount, String billAmount) {
        String billedAmount = String.format(getString(R.string.amount_in_aed), NumberFormatterKt.getDecimalValue(billAmount));
        String actualAmount = String.format(getString(R.string.amount_in_aed), NumberFormatterKt.getDecimalValue(etAmount));
        return  String.format(getString(R.string.bill_info), billedAmount, actualAmount);
    }

    private void addTextWatcher() {

        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearErrorMessage(tilAmount);

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
            public void afterTextChanged(Editable s) { }

        });
    }
    private void onSuccess(FragmentActivity activity) {
        if (activity instanceof UtilitiesActivity){
            if (getActivity() != null)
                ((UtilitiesActivity)getActivity()).navigateUp();
        }
    }
}
