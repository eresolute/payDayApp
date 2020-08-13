package com.fh.payday.views2.moneytransfer;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.datasource.models.Card;
import com.fh.payday.utilities.NumberFormatterKt;
import com.fh.payday.utilities.TextInputUtilsKt;

public class FragmentTransferPurpose extends Fragment {

    private InternationalMoneyTransfer activity;
    LinearLayout layoutSpinner;
    AppCompatSpinner spinner;
    TextInputEditText etPurpose;
    TextInputLayout textInputLayout;
    TextView tvTitle;

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

        if (isVisibleToUser && activity != null && activity.getViewModel().getDataClear()) {
            activity.hideKeyboard();
            etPurpose.setText(null);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transfer_purpose, container, false);
        view.findViewById(R.id.iv_next).setOnClickListener(v -> {
            navigateNext();
        });
        view.findViewById(R.id.iv_previous).setOnClickListener(v -> activity.onBackPressed());
        handleView(view);

        return view;
    }

    private void navigateNext() {
        if (TextUtils.isEmpty(etPurpose.getText().toString())) {
            activity.getViewModel().setPurpose("-");
        } else if (TextInputUtilsKt.isChar(etPurpose.getText().toString())){
            activity.getViewModel().setPurpose(etPurpose.getText().toString());
        } else {
            textInputLayout.setError("Invalid characters");
            return;
        }
        activity.onTransferPurpose();
    }

    private void handleView(View view) {
        Card card = activity.getCard();
        TextView tvBalance = view.findViewById(R.id.tv_available_balance);
        if(activity.getViewModel().getCardBalance() != null) {
            tvBalance.setText(String.format(getString(R.string.available_balance_in_aed),
                NumberFormatterKt.getDecimalValue(activity.getViewModel().getCardBalance())));
        } else {
            tvBalance.setText(String.format(getString(R.string.available_balance_in_aed), "0.00"));
        }

        layoutSpinner = view.findViewById(R.id.layout_spinner);
        spinner = view.findViewById(R.id.spinner);
        etPurpose = view.findViewById(R.id.et_purpose);
        textInputLayout = view.findViewById(R.id.textInputLayoutPurpose);
        tvTitle = view.findViewById(R.id.tvTitle);
        Bundle bundle = getArguments();
        if (bundle == null ) return;

        MoneyTransferType transferType = activity.getViewModel().getTransferType();
        if(transferType == null) return;

        etPurpose.setOnEditorActionListener((v, actionId, event) -> {
            navigateNext();
            return false;
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

        switch (transferType) {

            case INTERNATIONAL:
                textInputLayout.setVisibility(View.GONE);
                etPurpose.setVisibility(View.GONE);
                layoutSpinner.setVisibility(View.VISIBLE);
                tvTitle.setText(R.string.purpose_of_payment);

                ArrayAdapter adapter = ArrayAdapter.createFromResource(view.getContext(),
                    R.array.purpose_of_payment, R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                break;
            case LOCAL:
            case CC:
            case PAYDAY:
                textInputLayout.setVisibility(View.VISIBLE);
                etPurpose.setVisibility(View.VISIBLE);
                layoutSpinner.setVisibility(View.GONE);
                tvTitle.setText(R.string.description);
                etPurpose.setHint("Optional");
                etPurpose.setImeOptions(EditorInfo.IME_ACTION_DONE);
                etPurpose.setGravity(Gravity.CENTER);
                break;
        }
    }
}
