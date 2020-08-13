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
import com.fh.payday.datasource.models.login.User;
import com.fh.payday.datasource.models.payments.utilities.BillDetails;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.preferences.UserPreferences;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.utilities.DateTime;
import com.fh.payday.utilities.NumberFormatterKt;

import static com.fh.payday.datasource.models.payments.utilities.UtilitiesPaymentKt.ACCOUNTID;
import static com.fh.payday.datasource.models.payments.utilities.UtilitiesPaymentKt.EMIRATESID;
import static com.fh.payday.datasource.models.payments.utilities.UtilitiesPaymentKt.MOBILENO;
import static com.fh.payday.datasource.models.payments.utilities.UtilitiesPaymentKt.PERSONID;

public class UtilitiesSummaryFragment extends Fragment {
    private UtilitiesActivity activity;
    private TextView tvAccountNo, tvAccountLabel, tvBillAmountLabel, tvDueDateLabel, tvLastBillLabel ,tvPayableAmount, tvDueDate, tvLastBillAmount, tvBilledAmount;
    private Button btnPay;

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_utilities_summary, container, false);
        init(view);

        addOtpObserver();

        handleSummary();


        btnPay.setOnClickListener(v -> {


            User user = UserPreferences.Companion.getInstance().getUser(activity);
            if (user == null) return;

            if (!activity.isNetworkConnected()) {
                activity.onFailure(activity.findViewById(R.id.card_view),
                        getString(R.string.no_internet_connectivity));
                return;
            }

            activity.getViewModel().getOtp(user.getToken(), user.getSessionId(), user.getRefreshToken(), user.getCustomerId());
        });
        return view;
    }

    private void handleSummary() {
        String aadcServiceType = activity.getViewModel().getAadcSelectedService();

        if (aadcServiceType != null) {
            switch (aadcServiceType) {
                case ACCOUNTID:
                    tvAccountLabel.setText(getString(R.string.account_no_label));
                    break;
                case EMIRATESID:
                    tvAccountLabel.setText(getString(R.string.emirates_id));
                    break;
                case MOBILENO:
                    tvAccountLabel.setText(getString(R.string.mobile_number));
                    break;
                case PERSONID:
                    tvAccountLabel.setText(getString(R.string.person_id));
                    break;
            }
        }

        activity.getViewModel().getAmount().observe(this, amount ->
        {
            if (amount != null) {
                tvPayableAmount.setText(String.format(getString(R.string.amount_in_aed), NumberFormatterKt.getDecimalValue(amount)));
            }
        });

//        tvDueDate.setText(DateTime.Companion.currentDate());
        tvAccountNo.setText(activity.getViewModel().getAccountNumber());

        if (activity.getViewModel().getFewaBill() != null) {
            BillDetails bill = activity.getViewModel().getFewaBill();
            tvDueDate.setText(DateTime.Companion.parse(bill.getBillDate(), "dd/MM/yyyy"));
            tvBilledAmount.setText(String.format(getString(R.string.amount_in_aed), NumberFormatterKt.getDecimalValue(bill.getOutstandingAmountInAED())));
            tvLastBillAmount.setText(String.format(getString(R.string.amount_in_aed), NumberFormatterKt.getDecimalValue(bill.getPreviousBalanceInAED())));
        } else if (activity.getViewModel().getAadcBillDetail().getValue() != null){
            hideFewaLabels();
            tvBilledAmount.setText(String.format(getString(R.string.amount_in_aed),
                    NumberFormatterKt.getDecimalValue(activity.getViewModel().getAadcBillDetail()
                            .getValue().getDueBalanceInAed())));
        }
    }

    private void hideFewaLabels() {
        tvDueDate.setVisibility(View.GONE);
        tvLastBillAmount.setVisibility(View.GONE);
//        tvBilledAmount.setVisibility(View.GONE);
        tvLastBillLabel.setVisibility(View.GONE);
        tvDueDateLabel.setVisibility(View.GONE);
//        tvBillAmountLabel.setVisibility(View.GONE);

    }

    private void init(View view) {

        btnPay = view.findViewById(R.id.btn_pay);
        tvAccountNo = view.findViewById(R.id.tv_account_no);
        tvPayableAmount = view.findViewById(R.id.tv_bill_amount);
        tvDueDate = view.findViewById(R.id.tv_due_date);
        tvAccountLabel = view.findViewById(R.id.textView1);
        tvLastBillAmount = view.findViewById(R.id.tv_last_bill_amount);
        tvBilledAmount = view.findViewById(R.id.tv_billed_amount);
        tvLastBillLabel = view.findViewById(R.id.textView31);
        tvDueDateLabel = view.findViewById(R.id.textView3);
        tvBillAmountLabel = view.findViewById(R.id.textView27);
    }

    private void addOtpObserver() {

        activity.getViewModel().getOptResponseState().observe(this, event -> {
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

                onSuccess(activity);

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

    private void onSuccess(FragmentActivity activity) {
        if (activity instanceof UtilitiesActivity){
            if (getActivity() != null)
                ((UtilitiesActivity)getActivity()).navigateUp();
        }
    }
}
