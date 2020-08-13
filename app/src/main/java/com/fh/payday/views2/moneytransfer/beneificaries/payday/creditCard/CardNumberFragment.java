package com.fh.payday.views2.moneytransfer.beneificaries.payday.creditCard;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.fh.payday.R;
import com.fh.payday.utilities.TextInputUtilsKt;
import com.fh.payday.viewmodels.SharedViewModel;
import com.fh.payday.views2.moneytransfer.beneificaries.EditBeneficiaryActivity;

public class CardNumberFragment extends Fragment {

    SharedViewModel model;
    int index;
    TextInputEditText etCardNumber;
    EditBeneficiaryActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (EditBeneficiaryActivity) context;
    }

    public static CardNumberFragment newInstance(int position) {

        Bundle args = new Bundle();
        CardNumberFragment fragment = new CardNumberFragment();

        args.putInt("index", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        index = getArguments().getInt("index");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card_number, container, false);
        init(view);

        etCardNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextInputUtilsKt.clearErrorMessage(activity.findViewById(R.id.textInputLayoutCardNumber));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        return view;
    }

    private void init(View view) {
        etCardNumber = view.findViewById(R.id.et_card_number);

        if(activity.getViewModel().getSelectedBeneficiary() != null) {
            etCardNumber.setText(activity.getViewModel().getSelectedBeneficiary().getCardAccountNo());
        }

        view.findViewById(R.id.img_next).setOnClickListener(v -> {
            activity.hideKeyboard();
            if (!TextUtils.isEmpty(etCardNumber.getText().toString()) && etCardNumber.getText().toString().length() == 16) {
                activity.getViewModel().setCardNumber(etCardNumber.getText().toString());
                model.setMap("Card Number", etCardNumber.getText().toString());
                model.setSelected(index + 1);
            } else {
                TextInputUtilsKt.setErrorMessage(activity.findViewById(R.id.textInputLayoutCardNumber), "Invalid Card Number");
            }
        });
        view.findViewById(R.id.img_previous).setOnClickListener(v -> model.setSelected(index - 1));

    }
}
