package com.fh.payday.views2.payments.recharge;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.datasource.models.login.User;
import com.fh.payday.datasource.models.payments.BillDetailDU;
import com.fh.payday.datasource.models.payments.BillDetailEtisalat;
import com.fh.payday.datasource.models.payments.RechargeDetail;
import com.fh.payday.datasource.models.payments.UAEServiceType;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.preferences.UserPreferences;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.utilities.DateTime;
import com.fh.payday.utilities.NumberFormatterKt;
import com.fh.payday.utilities.TextViewUtilsKt;

public class TopupDetailsFragment extends Fragment {
    Button btnNext;
    private MobileRechargeActivity activity;
    private TextView tvBillAmount, tvBillAmountLabel, tvDueDate, tvNumber, tvPaidAmount, tvPaidAmountLabel;
    private TextView tvNumberLabel;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MobileRechargeActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && tvNumberLabel != null) {
            String operator = activity.getViewModel().getOperator();
            String service = activity.getViewModel().getSERVICE().getValue();
            if (operator != null && service != null && operator.equalsIgnoreCase("etisalat") &&
                    !("GSM".equalsIgnoreCase(service) || "WaselRecharge".equalsIgnoreCase(service))) {
                tvNumberLabel.setText(getString(R.string.account_no_label));
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_topup_details, container, false);
        tvBillAmount = view.findViewById(R.id.tv_bill_amount_val);
        tvBillAmountLabel = view.findViewById(R.id.tv_bill_amount);
        tvPaidAmount = view.findViewById(R.id.tv_paid_amount_val);
        tvPaidAmountLabel = view.findViewById(R.id.tv_paid_amount);
        tvDueDate = view.findViewById(R.id.tv_due_date_val);
        tvNumber = view.findViewById(R.id.tv_mobile_number_val);
        tvNumberLabel = view.findViewById(R.id.tv_mobile_number);
        addOtpObserver();
        addOtpObserver2();
//
//        if (Integer.valueOf(2).equals(activity.getViewModel().getTYPE_ID().getValue())) {
////            addOtpObserver();
//        }

        activity.getViewModel().getRechargeState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<RechargeDetail> billState = event.peekContent();

            if (billState == null) return;

            if (billState instanceof NetworkState2.Success) {
                RechargeDetail data = ((NetworkState2.Success<RechargeDetail>) billState).getData();
                if (data == null) return;
                String date = TextViewUtilsKt.formatDate(data.getReplyDateStamp());

                if (activity.getViewModel().getMOBILE().getValue() == null) return;
                //TextViewUtilsKt.addHypen(tvNumber, activity.getViewModel().getMOBILE().getValue());
                String mobileNo = activity.getViewModel().getMOBILE().getValue();
                tvNumber.setText(String.format(getString(R.string.plus_971_sign),
                        mobileNo.startsWith("0") ? mobileNo.substring(1) : mobileNo));
                tvDueDate.setText(date);

                if (TextUtils.isEmpty(data.getMaximumAmount())) return;
                tvBillAmount.setText(String.format(getString(R.string.amount_in_aed), NumberFormatterKt.getDecimalValue(data.getMaximumAmount())));
            }
        });

        activity.getViewModel().getBillStateEtisalat().observe(this, event -> {
            if (event == null) return;

            NetworkState2<BillDetailEtisalat> billState = event.peekContent();

            if (billState == null) return;

            if (billState instanceof NetworkState2.Success) {
                BillDetailEtisalat data = ((NetworkState2.Success<BillDetailEtisalat>) billState).getData();
                if (data == null) return;
                String date = DateTime.Companion.parse(data.getReplyDateStamp(), "MM/dd/yyyy HH:mm:ss");
                String service = activity.getViewModel().getSERVICE().getValue();

                if (activity.getViewModel().getMOBILE().getValue() == null || service == null)
                    return;

                if (service.equalsIgnoreCase("GSM")) {
                    //TextViewUtilsKt.addHypen(tvNumber, activity.getViewModel().getMOBILE().getValue());
                    String mobileNo = activity.getViewModel().getMOBILE().getValue();
                    tvNumber.setText(String.format(getString(R.string.plus_971_sign),
                            mobileNo.startsWith("0") ? mobileNo.substring(1) : mobileNo));
                } else {
                    tvNumber.setText(activity.getViewModel().getMOBILE().getValue());
                }

                tvDueDate.setText(date);

                if (TextUtils.isEmpty(data.getAmountDue()))
                    return;
                tvBillAmount.setText(String.format(getString(R.string.amount_in_aed), NumberFormatterKt.getDecimalValue(data.getAmountDue())));
            }
        });

        activity.getViewModel().getBillStateDu().observe(this, event -> {
            if (event == null) return;

            NetworkState2<BillDetailDU> billState = event.peekContent();

            if (billState == null) return;

            if (billState instanceof NetworkState2.Success) {
                BillDetailDU data = ((NetworkState2.Success<BillDetailDU>) billState).getData();

                view.findViewById(R.id.tv_due_date).setVisibility(View.GONE);
                tvDueDate.setVisibility(View.GONE);

                //tvNumber.setText(activity.getViewModel().getMOBILE().getValue());
                if (activity.getViewModel().getMOBILE().getValue() == null) return;
                //TextViewUtilsKt.addHypen(tvNumber, activity.getViewModel().getMOBILE().getValue());
                String mobileNo = activity.getViewModel().getMOBILE().getValue();
                tvNumber.setText(String.format(getString(R.string.plus_971_sign),
                        mobileNo.startsWith("0") ? mobileNo.substring(1) : mobileNo));
                if (data == null) return;
                if (TextUtils.isEmpty(data.getBalance())) return;

                tvBillAmount.setText(String.format(getString(R.string.amount_in_aed), NumberFormatterKt.getDecimalValue(data.getBalance())));
            }
        });

        activity.getViewModel().getAMOUNT().observe(this, amount -> {
            if (activity.getViewModel().getTYPE_ID().getValue() ==  null || activity.getViewModel().getOperator() == null ) return;
            if (activity.getViewModel().getTYPE_ID().getValue() == 2
                    || activity.getViewModel().getOperator().equalsIgnoreCase("du")) {
                view.findViewById(R.id.tv_due_date).setVisibility(View.GONE);
                tvDueDate.setVisibility(View.GONE);
            }

            if (activity.getViewModel().getSERVICE().getValue() != null) {
                String service = activity.getViewModel().getSERVICE().getValue();

                switch (service.toUpperCase()) {
                    case UAEServiceType.ELIFE:
                    case UAEServiceType.BROADBAND:
                    case UAEServiceType.EVISION:
                        tvNumber.setText(activity.getViewModel().getMOBILE().getValue());
                        break;
                    default:
                        //TextViewUtilsKt.addHypen(tvNumber, activity.getViewModel().getMOBILE().getValue());
                        String mobileNo = activity.getViewModel().getMOBILE().getValue();
                        if (mobileNo == null) return;
                        tvNumber.setText(String.format(getString(R.string.plus_971_sign),
                                mobileNo.startsWith("0") ? mobileNo.substring(1) : mobileNo));
                }
            }

            if (TextUtils.isEmpty(amount)) return;

            if (activity.getViewModel().getTYPE_ID().getValue() == 1) {
                tvBillAmountLabel.setVisibility(View.VISIBLE);
                tvBillAmount.setVisibility(View.VISIBLE);
            }

            if (activity.getViewModel().getTYPE_ID().getValue() == 2 || activity.getViewModel().getTYPE_ID().getValue() == 1)
                if (amount != null) {
                    tvPaidAmountLabel.setVisibility(View.VISIBLE);
                    tvPaidAmount.setVisibility(View.VISIBLE);
                    tvPaidAmount.setText(String.format(getString(R.string.amount_in_aed), NumberFormatterKt.getDecimalValue(amount)));
                }
        });

        btnNext = view.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(v -> {
            if (activity.getViewModel().getTYPE_ID().getValue() == null) return;
            if (UserPreferences.Companion.getInstance().getUser(activity) == null
                    || (TextUtils.isEmpty(tvBillAmount.getText().toString())
                    && activity.getViewModel().getTYPE_ID().getValue() == 1))
                return;

            User user = UserPreferences.Companion.getInstance().getUser(activity);
            if (!activity.isNetworkConnected()) {
                activity.onFailure(activity.findViewById(R.id.card_view), getString(R.string.no_internet_connectivity));
                return;
            }

            if (activity.getViewModel().getTYPE_ID().getValue() == 1 && user != null) {
                activity.getViewModel().getOtp2(user.getToken(), user.getSessionId(), user.getRefreshToken(), user.getCustomerId());
//                ((MobileRechargeActivity) getActivity()).onTopupDetails();
            } else {
                if (user == null) return;
                activity.getViewModel().getOtp(user.getToken(), user.getSessionId(), user.getRefreshToken(), user.getCustomerId());
//                activity.onTopupDetails();
            }
        });
        return view;
    }

    private void addOtpObserver() {
        activity.getViewModel().getOptResponseState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<String> otpResponse = event.getContentIfNotHandled();


            if (otpResponse == null) return;

            if (otpResponse instanceof NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing));
                btnNext.setEnabled(false);
                return;
            }

            activity.hideProgress();
            btnNext.setEnabled(true);

            if (otpResponse instanceof NetworkState2.Success) {
                if (Integer.valueOf(2).equals(activity.getViewModel().getTYPE_ID().getValue())) {
                    activity.onTopupDetails();
                } else {
                    activity.onTopupDetails();
                }
            } else if (otpResponse instanceof NetworkState2.Error) {

                NetworkState2.Error<String> error = (NetworkState2.Error<String>) otpResponse;
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

    private void addOtpObserver2() {
        activity.getViewModel().getOptResponseState2().observe(this, event -> {
            if (event == null) return;

            NetworkState2<String> otpResponse = event.getContentIfNotHandled();


            if (otpResponse == null) return;

            if (otpResponse instanceof NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing));
                btnNext.setEnabled(false);
                return;
            }

            activity.hideProgress();
            btnNext.setEnabled(true);

            if (otpResponse instanceof NetworkState2.Success) {
//                if (Integer.valueOf(2).equals(activity.getViewModel().getTYPE_ID().getValue())) {
//                    activity.onTopupDetails();
//                }
//                else {
//                    activity.onTopupDetails();
//                }
                activity.onTopupDetails();
            } else if (otpResponse instanceof NetworkState2.Error) {

                NetworkState2.Error<String> error = (NetworkState2.Error<String>) otpResponse;
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
