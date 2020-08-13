package com.fh.payday.views2.payments.transport;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.datasource.models.login.User;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.preferences.UserPreferences;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.utilities.DateTime;
import com.fh.payday.utilities.NumberFormatterKt;

public class TransportSummaryFragment extends Fragment {
    private TransportActivity activity;
    private Button btnPay;
    private String serviceType;
    TextView tvLocationTitle, tvLocation, tvAmountTitle;
    TextView tvAmount, tvCardDetailsTitle, tvCardDetails;
    private TextView tvBalance, lblBalance;

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
        View view = inflater.inflate(R.layout.fragment_transport_summary, container, false);
        init(view);
        addOtpObserver();
        if (getArguments() != null) {
            serviceType = getArguments().getString("title");
        }

        if (("salik").equalsIgnoreCase(serviceType)) {

            if (activity.getTransportViewModel() != null) {
                activity.getTransportViewModel().getAmount().observe(this, amount -> {
                    if (amount != null) {
                        tvAmount.setText(String.format(getString(R.string.amount_in_aed), NumberFormatterKt.getDecimalValue(amount)));
                    }
                });
                tvLocation.setText(activity.getTransportViewModel().getAccountNumber());

                 activity.getTransportViewModel().getSalikBalance().observe(this, balance -> {
                     if (balance != null) {
                         tvBalance.setText(String.format(getString(R.string.amount_in_aed), NumberFormatterKt.getDecimalValue(balance)));
                     }
                 });

            }
            tvLocationTitle.setText(getString(R.string.account_number));
            tvAmountTitle.setText(getString(R.string.amount));
            tvCardDetailsTitle.setText(getString(R.string.date));
            tvCardDetails.setText(DateTime.Companion.currentDate());
            tvBalance.setVisibility(View.VISIBLE);
            lblBalance.setVisibility(View.VISIBLE);

        } else if (("mawaqif").equalsIgnoreCase(serviceType)) {

            if (activity.getTransportViewModel() != null) {
                activity.getTransportViewModel().getAmount().observe(this, amount -> {
                    if (amount != null) {
                        tvAmount.setText(String.format(getString(R.string.amount_in_aed), NumberFormatterKt.getDecimalValue(amount)));
                    }
                });
                activity.getTransportViewModel().getMawaqifBillDetail().observe(activity, mawaqifBillDetail -> {
                    if (mawaqifBillDetail != null) {
                        tvLocation.setText(mawaqifBillDetail.getLocation());
                    }
                    tvCardDetails.setText(activity.getTransportViewModel().getAccountNumber());
                });
            }
            tvLocationTitle.setText(getString(R.string.location));
            tvAmountTitle.setText(getString(R.string.amount));
            tvCardDetailsTitle.setText(getString(R.string.pvt_number));
        }

        btnPay.setOnClickListener(v -> {
            User user = UserPreferences.Companion.getInstance().getUser(activity);
            if (user == null) return;

            if (!activity.isNetworkConnected()) {
                activity.onFailure(activity.findViewById(R.id.card_view), getString(R.string.no_internet_connectivity));
                return;
            }

            activity.getTransportViewModel().getOtp(user.getToken(), user.getSessionId(), user.getRefreshToken(), user.getCustomerId());

        });
        return view;
    }

    private void init(View view) {
        btnPay = view.findViewById(R.id.btn_pay);
        tvLocationTitle = view.findViewById(R.id.location);
        tvLocation = view.findViewById(R.id.tv_loation);
        tvAmountTitle = view.findViewById(R.id.amount);
        tvAmount = view.findViewById(R.id.tv_amount);
        tvCardDetailsTitle = view.findViewById(R.id.card_details);
        tvCardDetails = view.findViewById(R.id.tv_car_details);
        lblBalance = view.findViewById(R.id.balance);
        tvBalance = view.findViewById(R.id.tv_balance);
    }

    private void addOtpObserver() {

        activity.getTransportViewModel().getOptResponseState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<String> otpResponse = event.getContentIfNotHandled();


            if (otpResponse == null) return;

            if (otpResponse instanceof NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing));
                btnPay.setEnabled(false);
                return;
            }

            activity.hideProgress();
            btnPay.setEnabled(true);

            if (otpResponse instanceof NetworkState2.Success) {
                activity.onTransportSummary();
            } else if (otpResponse instanceof NetworkState2.Error) {
                NetworkState2.Error<String> error = (NetworkState2.Error<String>) otpResponse;
                if (error.isSessionExpired()) {
                    activity.onSessionExpired(error.getMessage());
                    return;
                }
//                activity.onError(error.getMessage());
                activity.handleErrorCode(Integer.parseInt(error.getErrorCode()), error.getMessage());
            } else if (otpResponse instanceof NetworkState2.Failure) {
                NetworkState2.Failure<String> error = (NetworkState2.Failure<String>) otpResponse;
                activity.onFailure(activity.findViewById(R.id.card_view), error.getThrowable());

            } else {
                activity.onFailure(activity.findViewById(R.id.card_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }
}
