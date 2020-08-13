package com.fh.payday.views2.payments.utilities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.utilities.DateTime;
import com.fh.payday.utilities.NumberFormatterKt;

public class UtilitiesDetailsFragment extends Fragment {
    private UtilitiesActivity activity;
    private TextView tvLastBillAmount;
    private TextView tvBillAmount;
    private TextView tvDueDate;
    private Button bttnNext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (UtilitiesActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_utilities_details, container, false);
        init(view);

        if (activity.getViewModel() != null) {

            activity.getViewModel().getBillDetails().observe(this, bill -> {

                if (bill != null) {
                    tvLastBillAmount.setText(String.format(getString(R.string.amount_in_aed), NumberFormatterKt.getDecimalValue(String.valueOf(bill.getPreviousBalanceInAED()))));
                    tvBillAmount.setText(String.format(getString(R.string.amount_in_aed), NumberFormatterKt.getDecimalValue(String.valueOf(bill.getOutstandingAmountInAED()))));
                    tvDueDate.setText(DateTime.Companion.parse(bill.getBillDate(), "dd/MM/yyyy"));
                }
            });
        }
        bttnNext.setOnClickListener(v -> onSuccess(activity));

        return view;
    }

    private void init(View view) {

        bttnNext = view.findViewById(R.id.btn_next);
        tvLastBillAmount = view.findViewById(R.id.tv_last_bill_val);
        tvBillAmount = view.findViewById(R.id.tv_bill_amount_val);
        tvDueDate = view.findViewById(R.id.tv_due_date_val);
    }

    private void onSuccess(FragmentActivity activity) {
        if (activity instanceof UtilitiesActivity){
            if (getActivity() != null)
                ((UtilitiesActivity)getActivity()).navigateUp();
        }
    }
}
