package com.fh.payday.views2.settings.update_link_account;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fh.payday.R;
import com.fh.payday.views2.settings.SettingOptionActivity;

public class LinkAccountNumberFragment extends Fragment {

    MaterialButton btnConfirm;

    SettingOptionActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (SettingOptionActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_link_account_number, container, false);
        btnConfirm = view.findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(v-> {
            activity.replaceFragment(new LinkCardNumberFragment());
        });
        return view;
    }
}
