package com.fh.payday.views2.dashboard;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.fh.payday.R;
import com.fh.payday.utilities.AlertDialog;

public class LoanOfferDetailsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_loan_offers_verify, parent, false);
        Button btnConfirm = view.findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(view1 -> {
            AlertDialog.showDialog(getFragmentManager(),"Your Loan has been dispensed", R.drawable.ic_success_checked);
        });
        return view;
    }
}
