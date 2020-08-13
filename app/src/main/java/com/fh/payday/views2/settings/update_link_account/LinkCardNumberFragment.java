package com.fh.payday.views2.settings.update_link_account;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fh.payday.R;
import com.fh.payday.utilities.OnAtmPinConfirmListener;
import com.fh.payday.views.fragments.AlertDialogFragment;
import com.fh.payday.views.shared.AtmCommonPinFragment;
import com.fh.payday.views2.settings.SettingOptionActivity;

import java.lang.ref.WeakReference;

public class LinkCardNumberFragment extends Fragment implements AlertDialogFragment.OnConfirmListener {

    private SettingOptionActivity activity;

    private OnAtmPinConfirmListener pinConfirmListener = pin ->{
        DialogFragment newFragment = AlertDialogFragment
                .newInstance("Account Linking Successful",
                        R.drawable.ic_success_checked,
                        R.color.colorAccent,
                        this);
        newFragment.show(activity.getSupportFragmentManager(), "dialog");
    };
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

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


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_link_card_number, container, false);
        WeakReference<LinkCardNumberFragment> weakReference = new WeakReference<>(this);

        view.findViewById(R.id.btn_confirm).setOnClickListener(v-> {
            activity.setToolbarTitle(getString(R.string.atm_pin));
            activity.replaceFragment(new AtmCommonPinFragment.Builder(pinConfirmListener)
                    .setPinLength(4)
                    .setTitle(weakReference.get().getString(R.string.enter_atm_pin))
                    .build());
        });

        return view;
    }


    @Override
    public void onConfirm(Dialog dialog) {
        activity.finish();
    }
}
