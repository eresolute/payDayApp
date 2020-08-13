package com.fh.payday.views2.payments.transport;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.ImageView;
import android.widget.TextView;

import com.fh.payday.BaseActivity;
import com.fh.payday.R;
import com.fh.payday.datasource.models.login.User;
import com.fh.payday.datasource.models.payments.transport.BalanceDetails;
import com.fh.payday.datasource.models.payments.transport.BillPaymentResponse;
import com.fh.payday.datasource.models.payments.transport.MawaqifBillDetail;
import com.fh.payday.datasource.models.payments.transport.OperatorDetails;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.preferences.UserPreferences;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.utilities.NonSwipeableViewPager;
import com.fh.payday.utilities.OnOTPConfirmListener;
import com.fh.payday.viewmodels.payments.transport.TransportViewModel;
import com.fh.payday.views.fragments.OTPFragment;
import com.fh.payday.views.fragments.PaymentSuccessfulDialog;
import com.fh.payday.views2.dashboard.MainActivity;
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity;
import com.fh.payday.views2.locator.LocatorActivity;
import com.fh.payday.views2.message.ContactUsActivity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TransportActivity extends BaseActivity implements OnOTPConfirmListener {

    public enum Transport {DUBAI_POLICE, MAWAQIF, SALIK, NOL}

    private TransportViewModel transportViewModel;
    private BalanceDetails balanceDetails;
    private MawaqifBillDetail mawaqifBillDetail;
    private OperatorDetails operatorDetails;
    private NonSwipeableViewPager mPager;
    private ImageView ivOperator;
    private Transport transport;
    TextView toolbar_title;
    private String toolbarTitle;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        transport = (Transport) getIntent().getSerializableExtra("operator");
        transportViewModel = ViewModelProviders.of(this).get(TransportViewModel.class);
        addPaymentObservable();

        PagerAdapter pagerAdapter = new SliderPageAdapter(this, getSupportFragmentManager(), transport);
        mPager.setAdapter(pagerAdapter);
        handleOperator(transport);
        setToolbarTitle(toolbarTitle);
        handleBottomBar();
    }

    @Override
    public int getLayout() {
        return R.layout.activity_transport;
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
    public void init() {
        findViewById(R.id.toolbar_back).setOnClickListener(view -> onBackPressed());
        toolbar_title = findViewById(R.id.toolbar_title);
        toolbar_title.setText(getString(R.string.transport));
        ivOperator = findViewById(R.id.iv_operator);
        mPager = findViewById(R.id.view_pager);

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int position) {
                setToolbarTitle(toolbarTitle);

                if (position == 0) {
                    transportViewModel.setDataClear(true);
                } else if (position == 1 && transportViewModel.getDataClear()) {
                    transportViewModel.setDataClear(true);
                } else {
                    transportViewModel.setDataClear(false);
                }

                switch (transport) {
                    case MAWAQIF:
                        if (position == 4)
                            setToolbarTitle(getString(R.string.verify_otp));
                        break;
                    case SALIK:
                        if (position == 3)
                            setToolbarTitle(getString(R.string.verify_otp));

                        break;
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

    public TransportViewModel getTransportViewModel() {
        return transportViewModel;
    }

    private void handleOperator(Transport operator) {
        switch (operator) {
            case DUBAI_POLICE:
                toolbarTitle = getString(R.string.dubai_police);
                ivOperator.setImageResource(R.drawable.ic_dubai_police);
                break;
            case MAWAQIF:
                toolbarTitle = getString(R.string.mawaqif);
                ivOperator.setImageResource(R.drawable.ic_mawaqif);
                break;
            case SALIK:
                toolbarTitle = getString(R.string.salik);
                ivOperator.setImageResource(R.drawable.ic_salik);
                break;
            case NOL:
                toolbarTitle = getString(R.string.nol);
                ivOperator.setImageResource(R.drawable.ic_nol);
                break;
        }
    }

    public void onPaymentNumber() {
        mPager.setCurrentItem(1);
    }

    public void pinSelected() {
        mPager.setCurrentItem(1);
    }

    public void pinAmountSelected() {
        mPager.setCurrentItem(2);
    }

    public void onBillDetails() {
        mPager.setCurrentItem(2);
    }

    public void onTransportAmount(String operator) {
        if (operator != null && operator.equals("salik"))
            mPager.setCurrentItem(3);
        else
            mPager.setCurrentItem(3);
    }

    public void onTransportSummary() {
        mPager.setCurrentItem(4);
    }

    private static class SliderPageAdapter extends FragmentPagerAdapter {

        private WeakReference<TransportActivity> weakReference;
        private final List<Fragment> selectedItems = new ArrayList<>();

        SliderPageAdapter(TransportActivity mContext, FragmentManager fm, Transport transport) {
            super(fm);
            this.weakReference = new WeakReference<>(mContext);
            switch (transport) {
                case MAWAQIF:
                    selectedItems.addAll(getMawaqifItem());
                    break;
                case SALIK:
                    selectedItems.addAll(getSalikItem());
                    break;
            }
        }

        @Override
        public Fragment getItem(int position) {
            return selectedItems.get(position);
        }

        @Override
        public int getCount() {
            return selectedItems.size();
        }

        private List<Fragment> getMawaqifItem() {

            Bundle bundle = new Bundle();
            bundle.putString("title", "mawaqif");
            TransportSummaryFragment fragment = new TransportSummaryFragment();
            fragment.setArguments(bundle);

            return Arrays.asList(
                    new TransportPaymentNumberFragment(),
                    new TransportDetailsFragment(),
                    new TransportAmountFragment(),
                    fragment,
                    new OTPFragment.Builder(weakReference.get())
                            .setPinLength(6)
                            .setTitle(weakReference.get().getString(R.string.enter_otp))
                            .setInstructions(null)
                            .setInstructions(weakReference.get().getString(R.string.otp_sent_to_registered_number))
                            .setButtonTitle(weakReference.get().getString(R.string.submit))
                            .build()
            );
        }

        private List<Fragment> getSalikItem() {
            Bundle bundle = new Bundle();
            bundle.putString("title", "salik");
            TransportSummaryFragment fragment = new TransportSummaryFragment();
            fragment.setArguments(bundle);

            return Arrays.asList(
                    new SalikPaymentNumberFragment(),
                    new SalikPinFragment(),
                    fragment,
                    new OTPFragment.Builder(weakReference.get())
                            .setPinLength(6)
                            .setTitle(weakReference.get().getString(R.string.enter_otp))
                            .setButtonTitle(weakReference.get().getString(R.string.submit))
                            .setInstructions(weakReference.get().getString(R.string.otp_sent_to_registered_number))
                            .build()
            );
        }
    }

    void setToolbarTitle(String title) {
        toolbar_title.setText(title);
    }

    @Override
    public void onOtpConfirm(@NotNull String otp) {
        if (otp.equals("")) {
            onFailure(findViewById(R.id.card_view), getString(R.string.please_enter_otp));
            return;
        }
        if (transportViewModel == null) {
            return;
        }

        User user = UserPreferences.Companion.getInstance().getUser(this);
        if (user == null) return;

        switch (transport) {
            case MAWAQIF:
                payMawaqifBill(otp, user);
                break;
            case SALIK:
                paySalikBill(otp, user);
                break;
        }
    }

    private void paySalikBill(String otp, User user) {

        getTransportViewModel().getBillDetails().observe(this, balanceDetails1 -> balanceDetails = balanceDetails1);
        String transId = balanceDetails.getTransactionId();
        String pin = getTransportViewModel().getSalikAccPin();
        String providerTransaction = balanceDetails.getProviderTransactionId();

        transportViewModel.payBill(user.getToken(), user.getSessionId(), user.getRefreshToken(),
                getTransportViewModel().getAccessKey(), user.getCustomerId(), transportViewModel.getTypeKey(),
                getTransportViewModel().getFlexKey(), transId, getTransportViewModel().getAccountNumber(),
                getTransportViewModel().getAmount().getValue(), pin, providerTransaction, otp);
    }

    private void payMawaqifBill(String otp, User user) {

        getTransportViewModel().getMawaqifBillDetail().observe(this, billDetail -> mawaqifBillDetail = billDetail);
        String transId = mawaqifBillDetail.getTransactionId();
        transportViewModel.payBill(user.getToken(), user.getSessionId(), user.getRefreshToken(),
                getTransportViewModel().getAccessKey(), user.getCustomerId(), transportViewModel.getTypeKey(),
                getTransportViewModel().getFlexKey(), transId, getTransportViewModel().getAccountNumber(),
                getTransportViewModel().getAmount().getValue(), "1", "", otp);
    }

    private void addPaymentObservable() {

        getTransportViewModel().getPaymentState().observe(this, event -> {
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
                                String.format(getString(R.string.amount_in_aed), response.getAvailableBalance()),
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
                onFailure(findViewById(R.id.card_view), ConstantsKt.CONNECTION_ERROR);
            } else {
                onFailure(findViewById(R.id.card_view), ConstantsKt.CONNECTION_ERROR);
            }

        });

        getTransportViewModel().getSalikPinState().observe(this, event -> {

            if (event != null && event) {

                User user = UserPreferences.Companion.getInstance().getUser(this);
                if (user == null) return;
                /*String accessKey = getTransportViewModel().getAccessKey();
                Integer typeId = getTransportViewModel().getSalikTypeId();
                String pin = getTransportViewModel().getSalikAccPin();
                String accountNo = getTransportViewModel().getAccountNumber();

                getTransportViewModel().getOperatorDetails().observe(this, operatorDetail1 ->
                        operatorDetails = operatorDetail1
                );
                getTransportViewModel().billBalanceSalik(user.getToken(), user.getSessionId(),
                        user.getRefreshToken(), user.getCustomerId(), accessKey, "getBalance", typeId,
                        accountNo, operatorDetails.getFlexiKey(), Integer.parseInt(operatorDetails.getTypeKey()), pin);

                 */

                pinSelected();
            }

        });
    }

}
