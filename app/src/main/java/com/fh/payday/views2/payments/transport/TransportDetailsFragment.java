package com.fh.payday.views2.payments.transport;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.utilities.NumberFormatterKt;

public class TransportDetailsFragment extends Fragment {

    private TransportActivity activity;
    private Button btnNext;
    private TextView tvLocation;
    private TextView tvAmount;
    private TextView tvCarDetail;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String operator = getArguments().getString("operator");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (TransportActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transport_details, container, false);
        init(view);

        if (activity.getTransportViewModel() != null) {
            activity.getTransportViewModel().getMawaqifBillDetail().observe(activity, mawaqifBill -> {
                if (mawaqifBill == null) return;
                tvLocation.setText(mawaqifBill.getLocation());
                if (mawaqifBill.getPvtAmount() != null){
                    tvAmount.setText(String.format(getString(R.string.amount_in_aed),
                        NumberFormatterKt.getDecimalValue(mawaqifBill.getPvtAmount())));
                }

                tvCarDetail.setText(activity.getTransportViewModel().getAccountNumber());
            });
        }
        btnNext.setOnClickListener(v -> {
            activity.onBillDetails();
        });
        return view;
    }

    private void init(View view) {
        tvLocation = view.findViewById(R.id.tv_location);
        tvAmount = view.findViewById(R.id.tv_amount);
        tvCarDetail = view.findViewById(R.id.tv_car_details);
        btnNext = view.findViewById(R.id.btn_next);
    }
}
