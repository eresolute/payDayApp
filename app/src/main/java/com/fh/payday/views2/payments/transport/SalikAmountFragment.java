package com.fh.payday.views2.payments.transport;

import android.content.Context;
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

import com.fh.payday.R;
import com.fh.payday.datasource.models.payments.transport.OperatorDetails;
import com.fh.payday.utilities.DecimalDigitsInputFilter;
import com.fh.payday.utilities.NumberFormatterKt;

import static com.fh.payday.utilities.TextInputUtilsKt.clearErrorMessage;
import static com.fh.payday.utilities.TextInputUtilsKt.setErrorMessage;

public class SalikAmountFragment extends Fragment {
    private TransportActivity activity;
    private OperatorDetails operatorDetails;
    private TextInputLayout textInputLayout;
    private TextInputEditText etAmount;
    private Button btnNext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (TransportActivity) context;
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
        if (activity.getTransportViewModel() == null) return;
        if (isVisibleToUser && activity.getTransportViewModel().getDataClear()) {
            etAmount.setText(null);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transport_amount, container, false);
        init(view);
        addTextWatcher();
        if (activity.getTransportViewModel() != null) {

            activity.getTransportViewModel().getOperatorDetails().observe(this, operatorDetail1 -> operatorDetails = operatorDetail1);
        }

        btnNext.setOnClickListener(v -> {
            if (validateEditText(etAmount.getText().toString().trim())) {
                activity.getTransportViewModel().getAmount().setValue(etAmount.getText().toString());
                activity.onTransportAmount("salik");
            }
        });
        return view;
    }

    private boolean validateEditText(String amount) {
        float minVal = Float.parseFloat(operatorDetails.getMinDenomination());
        float maxVal = Float.parseFloat(operatorDetails.getMaxDenomination());

        if (TextUtils.isEmpty(amount)) {
            setErrorMessage(textInputLayout, getString(R.string.invalid_amount));
            etAmount.requestFocus();
            return false;
        } else if (Float.parseFloat(amount) < minVal || Float.parseFloat(amount) > maxVal) {
            setErrorMessage(textInputLayout,String.format(getString(R.string.invalid_float_amount_ranged),
                    NumberFormatterKt.getDecimalValue(minVal), NumberFormatterKt.getDecimalValue( maxVal)));
            etAmount.requestFocus();
            return false;
        } else if ((Float.parseFloat(amount) % 50 != 0)) {
            setErrorMessage(textInputLayout, getString(R.string.multiple_of_50));
            etAmount.requestFocus();
            return false;
        } else {
            clearErrorMessage(textInputLayout);
            return true;
        }
    }

    private void init(View view) {

        textInputLayout = view.findViewById(R.id.textInputTransportAmount);
        etAmount = view.findViewById(R.id.et_amount);
        btnNext = view.findViewById(R.id.btn_next);
        etAmount.setHint(getString(R.string.aed));
        etAmount.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(10, 2)});

    }

    private void addTextWatcher() {
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearErrorMessage(textInputLayout);

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
}