package com.fh.payday.views2.settings.update_mobile_number;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fh.payday.R;
import com.fh.payday.viewmodels.SettingOptionViewModel;

public class NewMobileFragment extends Fragment {

    MaterialButton btnConfirm;
    SettingOptionViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(SettingOptionViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_mobile_number, container, false);
        init(view);

        btnConfirm.setOnClickListener(v-> viewModel.setSelected(1));

        return view;
    }

    private void init(View view) {
        btnConfirm = view.findViewById(R.id.btn_confirm);
    }
}
