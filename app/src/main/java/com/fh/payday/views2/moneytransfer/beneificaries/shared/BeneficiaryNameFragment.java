package com.fh.payday.views2.moneytransfer.beneificaries.shared;

import android.arch.lifecycle.ViewModelProviders;
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

public class BeneficiaryNameFragment extends Fragment {

    SharedViewModel model;
    int index;
    private TextInputEditText etName;
    private TextInputLayout layoutName;

    public static BeneficiaryNameFragment newInstance(int position) {

        Bundle args = new Bundle();
        BeneficiaryNameFragment fragment = new BeneficiaryNameFragment();
        args.putInt("index", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && layoutName != null) {
            TextInputUtilsKt.clearErrorMessage(layoutName);
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
        View view = inflater.inflate(R.layout.fragment_beneficiary_name, container, false);
        init(view);

        return view;
    }

    private void init(View view) {
        etName = view.findViewById(R.id.et_name);
        layoutName = view.findViewById(R.id.textInputLayoutName);

        view.findViewById(R.id.img_next).setOnClickListener( v-> {

            if (!TextUtils.isEmpty(etName.getText().toString())) {
                model.setMap("Name", etName.getText().toString());
                model.setSelected(index+1);
            } else {
                TextInputUtilsKt.setErrorMessage(layoutName, getString(R.string.invalid_name));
            }

        });

        view.findViewById(R.id.img_previous).setOnClickListener( v-> {
            model.setSelected(index-1);
        });


        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                TextInputUtilsKt.clearErrorMessage(layoutName);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
