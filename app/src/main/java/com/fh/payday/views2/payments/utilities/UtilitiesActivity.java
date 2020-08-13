package com.fh.payday.views2.payments.utilities;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.widget.ImageView;
import android.widget.TextView;

import com.fh.payday.BaseActivity;
import com.fh.payday.R;
import com.fh.payday.datasource.models.login.User;
import com.fh.payday.datasource.models.payments.UtilityServiceType;
import com.fh.payday.datasource.models.payments.utilities.AadcBillResponse;
import com.fh.payday.datasource.models.payments.utilities.BillDetails;
import com.fh.payday.datasource.models.payments.utilities.BillPaymentResponse;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.preferences.UserPreferences;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.utilities.NonSwipeableViewPager;
import com.fh.payday.utilities.OnOTPConfirmListener;
import com.fh.payday.viewmodels.payments.utilities.UtilitiesViewModel;
import com.fh.payday.views.fragments.PaymentSuccessfulDialog;
import com.fh.payday.views2.dashboard.MainActivity;
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity;
import com.fh.payday.views2.locator.LocatorActivity;
import com.fh.payday.views2.message.ContactUsActivity;

public class UtilitiesActivity extends BaseActivity {

    private NonSwipeableViewPager mPager;
    TextView toolbar_title;
    ImageView toolbarBack, imOperator;
    private UtilitiesViewModel viewModel;
    private BillDetails billDetails;
    private AadcBillResponse aadcBillDetail;
    private String operator;
    private String toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        toolbarBack.setOnClickListener(v -> onBackPressed());
        viewModel = ViewModelProviders.of(this).get(UtilitiesViewModel.class);

        addPaymentObservable();

        operator = getIntent().getStringExtra("operator");
        handleOperator(operator);
        handleBottomBar();
        setToolbarTitle(toolbarTitle);

        if (UtilityServiceType.FEWA.equalsIgnoreCase(operator)) mPager.setAdapter(
            new SwipeAdapter(getSupportFragmentManager(),
                SwipeAdapterKt.getFewaItems(
                    getString(R.string.enter_otp),
                    getString(R.string.submit),
                    getString(R.string.otp_sent_to_registered_number),
                    onOTPConfirmListener)
            )
        );
        else {
            mPager.setAdapter(
                new SwipeAdapter(getSupportFragmentManager(),
                    SwipeAdapterKt.getAadcItems(
                        getString(R.string.enter_otp),
                        getString(R.string.submit), getString(R.string.otp_sent_to_registered_number),
                        onOTPConfirmListener)
                )
            );

        }
    }

    public UtilitiesViewModel getViewModel() {
        return viewModel;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void handleOperator(String operator) {
        switch (operator.toLowerCase()) {

            case UtilityServiceType.DEWA: {
                toolbarTitle = getString(R.string.dewa);
                imOperator.setImageResource(R.drawable.ic_dewa);
                break;
            }
            case UtilityServiceType.FEWA: {
                toolbarTitle = getString(R.string.fewa);
                imOperator.setImageResource(R.drawable.ic_fewa_);
                break;
            }
            case UtilityServiceType.AADC: {
                toolbarTitle = getString(R.string.aadc);
                imOperator.setImageResource(R.drawable.ic_addc);
                break;
            }
        }
    }

    private void handleBottomBar() {

        findViewById(R.id.btm_home).setOnClickListener(view -> {
            Intent i = new Intent(view.getContext(), MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });

        findViewById(R.id.btm_menu_branch).setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), LocatorActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        });

        findViewById(R.id.btm_menu_support).setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), ContactUsActivity.class);
            startActivity(i);
        });

        findViewById(R.id.btm_menu_loan_calc).setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), LoanCalculatorActivity.class);
            startActivity(i);
        });

        findViewById(R.id.toolbar_help).setOnClickListener(v -> startHelpActivity("paymentScreenHelp"));
    }

    @Override
    public int getLayout() {
        return R.layout.activity_utilities;
    }

    @Override
    public void init() {

        toolbar_title = findViewById(R.id.toolbar_title);
        toolbarBack = findViewById(R.id.toolbar_back);
        mPager = findViewById(R.id.view_pager);
        imOperator = findViewById(R.id.iv_operator);

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    viewModel.setDataClear(true);
                } else if (position == 1 && viewModel.getDataClear()) {
                    viewModel.setDataClear(true);
                } else {
                    viewModel.setDataClear(false);
                }

                if ((UtilityServiceType.FEWA.equalsIgnoreCase(operator) && position == 3)
                    || (UtilityServiceType.AADC.equalsIgnoreCase(operator) && position == 4)) {
                    setToolbarTitle(getString(R.string.verify_otp));
                } else {
                    setToolbarTitle(toolbarTitle);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    public void navigateUp() {
        int nextPage = mPager.getCurrentItem() + 1;
        if (mPager.getAdapter() != null && nextPage < mPager.getAdapter().getCount()) {
            mPager.setCurrentItem(nextPage);
        }
    }

    void setToolbarTitle(String title) {
        toolbar_title.setText(title);
    }

    private OnOTPConfirmListener onOTPConfirmListener = otp -> {

        if (otp.equals("")) {
            onFailure(findViewById(R.id.card_view), getString(R.string.please_enter_otp));
            return;
        }

        if (viewModel == null) {
            return;
        }

        User user = UserPreferences.Companion.getInstance().getUser(this);
        if (user == null) return;

        switch (operator.toLowerCase()) {

            case UtilityServiceType.AADC:

                getViewModel().getAadcBillDetail().observe(this, bill -> aadcBillDetail = bill);
                viewModel.payBill(user.getToken(), user.getSessionId(), user.getRefreshToken(),
                    viewModel.getAccessKey(), user.getCustomerId(), viewModel.getTypeKey(),
                    viewModel.getFlexKey(), aadcBillDetail.getTransactionId(),
                    viewModel.getAccountNumber().replace("-", ""),
                    viewModel.getAmount().getValue(), viewModel.getAadcSelectedService(), otp);
                break;
            case UtilityServiceType.FEWA:
            case UtilityServiceType.DEWA:

                getViewModel().getBillDetails().observe(this, bill -> billDetails = bill);
                viewModel.payBill(user.getToken(), user.getSessionId(), user.getRefreshToken(),
                    viewModel.getAccessKey(), user.getCustomerId(), viewModel.getTypeKey(),
                    viewModel.getFlexKey(), billDetails.getTransactionId(), viewModel.getAccountNumber().replace("-", ""),
                    viewModel.getAmount().getValue(), billDetails.getProviderTransactionId(), otp);
        }
    };

    private void addPaymentObservable() {

        getViewModel().getPaymentState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<BillPaymentResponse> paymentResponse = event.getContentIfNotHandled();

            if (paymentResponse == null) return;

            if (paymentResponse instanceof NetworkState2.Loading) {
                showProgress(getString(R.string.processing));
                return;
            }

            hideProgress();

            if (paymentResponse instanceof NetworkState2.Success) {

                BillPaymentResponse response = ((NetworkState2.Success<BillPaymentResponse>) paymentResponse).getData();
                if (response != null) {
                    if (("000").equals(response.getResponseCode())) {

                        DialogFragment dialogFragment = PaymentSuccessfulDialog.newInstance(getString(R.string.successfully_paid),
                            String.format(getString(R.string.amount_in_aed), String.valueOf(response.getAvailableBalance())),
                            R.drawable.ic_success_checked,
                            R.color.blue,
                            dialog -> finish());
                        dialogFragment.setCancelable(false);
                        dialogFragment.show(getSupportFragmentManager(), null);
                    }

                } else
                    onError(getString(R.string.un_successfully_paid));

            } else if (paymentResponse instanceof NetworkState2.Error) {
                NetworkState2.Error<BillPaymentResponse> error = (NetworkState2.Error<BillPaymentResponse>) paymentResponse;
                if (error.isSessionExpired()) {
                    onSessionExpired(error.getMessage());
                    return;
                }
//                onError(error.getMessage());
                handleErrorCode(Integer.parseInt(error.getErrorCode()), error.getMessage());
            } else if (paymentResponse instanceof NetworkState2.Failure) {

                //NetworkState2.Failure<BillPaymentResponse> error = (NetworkState2.Failure<BillPaymentResponse>) paymentResponse;
                onFailure(findViewById(R.id.card_view), ConstantsKt.CONNECTION_ERROR);

            } else {
                onFailure(findViewById(R.id.card_view), ConstantsKt.CONNECTION_ERROR);
            }


        });
    }
}
