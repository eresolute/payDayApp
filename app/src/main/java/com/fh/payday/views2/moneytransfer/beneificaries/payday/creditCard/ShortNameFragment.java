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
import com.fh.payday.datasource.models.moneytransfer.Beneficiary;
import com.fh.payday.utilities.TextInputUtilsKt;
import com.fh.payday.viewmodels.SharedViewModel;
import com.fh.payday.views2.moneytransfer.beneificaries.EditBeneficiaryActivity;

public class ShortNameFragment extends Fragment {

    SharedViewModel model;
    int index;
    TextInputEditText etShortName;
    EditBeneficiaryActivity activity;
    Beneficiary beneficiary = null;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (EditBeneficiaryActivity) context;
    }

    public static ShortNameFragment newInstance(int position) {

        Bundle args = new Bundle();
        ShortNameFragment fragment = new ShortNameFragment();
        args.putInt("index", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = ViewModelProviders.of(activity).get(SharedViewModel.class);

        if (getArguments() == null) return;

        index = getArguments().getInt("index");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_short_name, container, false);
        init(view);

        etShortName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextInputUtilsKt.clearErrorMessage(activity.findViewById(R.id.textInputLayout));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return view;
    }

    private void init(View view) {
        etShortName = view.findViewById(R.id.et_short_name);

        if(activity.getViewModel().getSelectedBeneficiary() != null) {
            etShortName.setText(activity.getViewModel().getSelectedBeneficiary().getBeneficiaryName());
        }

        view.findViewById(R.id.img_next).setOnClickListener(v -> {
            activity.hideKeyboard();
            if(validate(etShortName.getText().toString())) {
                activity.getViewModel().setShortName(etShortName.getText().toString());
                model.setMap("Short Name", etShortName.getText().toString());
                model.setSelected(index + 1);
            } else {
                TextInputUtilsKt.setErrorMessage(activity.findViewById(R.id.textInputLayout), "Invalid Name");
            }
        });
        view.findViewById(R.id.img_previous).setOnClickListener(v -> {
            if (index == 0)
                activity.onBackPressed();
            else
                model.setSelected(index - 1);
        });
    }

    private boolean validate(String name) {
        if(TextUtils.isEmpty(name)) {
            return false;
        }
         return true;
    }
}
