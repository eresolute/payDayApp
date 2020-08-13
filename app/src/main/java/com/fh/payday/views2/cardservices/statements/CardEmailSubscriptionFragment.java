package com.fh.payday.views2.cardservices.statements;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.fh.payday.R;

public class CardEmailSubscriptionFragment extends Fragment {
    CardStatementTypeActivity activity;

    public static CardEmailSubscriptionFragment newInstance() {
        return new CardEmailSubscriptionFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (CardStatementTypeActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity =  null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card_email_subscription , container, false);
        Button btnConfirm = view.findViewById(R.id.btn_confirm);
        /*btnConfirm.setOnClickListener(view1 -> {
            assert getFragmentManager() != null;
            new AlertDialogFragment.Builder()
                    .setMessage(getString(R.string.email_subscription_successful))
                    .setIcon(R.drawable.ic_success_checked_blue)
                    .setCancelable(false)
                    .setConfirmListener(dialog -> {
                        dialog.dismiss();
                        activity.finish();
                    }).build().show(getFragmentManager(), "");
        });*/
        return view;
    }
}
