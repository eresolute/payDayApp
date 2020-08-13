package com.fh.payday.views2.moneytransfer.beneificaries.shared;

import android.arch.lifecycle.ViewModelProviders;
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
import com.fh.payday.utilities.TextInputUtilsKt;
import com.fh.payday.viewmodels.SharedViewModel;
import com.fh.payday.views2.moneytransfer.beneificaries.EditBeneficiaryActivity;

public class AccountNumberFragment extends Fragment {

    SharedViewModel model;
    int index;
    TextInputEditText etAccount;
    TextInputLayout layoutAccount;
    EditBeneficiaryActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (EditBeneficiaryActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    public static AccountNumberFragment newInstance(int position) {

        Bundle args = new Bundle();
        AccountNumberFragment fragment = new AccountNumberFragment();

        args.putInt("index", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(isVisibleToUser && layoutAccount != null) {
            TextInputUtilsKt.clearErrorMessage(layoutAccount);
        }
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
        View view = inflater.inflate(R.layout.fragment_account_number, container, false);
        init(view);

        return view;
    }

    private void init(View view) {
        etAccount = view.findViewById(R.id.et_account);
        layoutAccount = view.findViewById(R.id.textInputLayoutAccount);

        view.findViewById(R.id.img_next).setOnClickListener( v-> {
            if (!TextUtils.isEmpty(etAccount.getText().toString()) &&  etAccount.getText().toString().length() == 16) {
                activity.getViewModel().setAccountNumber(etAccount.getText().toString());
                model.setMap("Account Number", etAccount.getText().toString());
                model.setSelected(index + 1);
            } else {
                TextInputUtilsKt.setErrorMessage(layoutAccount, getString(R.string.invalid_account_no));
            }
        });

        view.findViewById(R.id.img_previous).setOnClickListener( v-> {
            model.setSelected(index - 1);
        });

        etAccount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextInputUtilsKt.clearErrorMessage(layoutAccount);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
