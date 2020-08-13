package com.fh.payday.views2.payments.recharge;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.fh.payday.BaseActivity;
import com.fh.payday.R;
import com.fh.payday.datasource.models.login.User;
import com.fh.payday.datasource.models.payments.BillDetailDU;
import com.fh.payday.datasource.models.payments.BillDetailEtisalat;
import com.fh.payday.datasource.models.payments.BillPaymentDuResponse;
import com.fh.payday.datasource.models.payments.BillPaymentResponse;
import com.fh.payday.datasource.models.payments.RechargeDetail;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.preferences.UserPreferences;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.utilities.NonSwipeableViewPager;
import com.fh.payday.utilities.OnOTPConfirmListener;
import com.fh.payday.viewmodels.payments.billpayment.MobileRechargeViewModel;
import com.fh.payday.views.fragments.OTPFragment;
import com.fh.payday.views.fragments.PaymentSuccessfulDialog;
import com.fh.payday.views2.dashboard.MainActivity;
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity;
import com.fh.payday.views2.locator.LocatorActivity;
import com.fh.payday.views2.message.ContactUsActivity;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;


public class MobileRechargeActivity extends BaseActivity implements OnOTPConfirmListener, ViewPager.OnPageChangeListener {
    private NonSwipeableViewPager mPager;
    private NonSwipeableViewPager mPagerPrepaid;
    private static final int NUM_PAGES = 4;
    private ImageView ivOperator;
    private RadioGroup rgPrepaid, rgPostpaid, rgPayment, rgPrepaidDu;

    private MobileRechargeViewModel viewModel;
    String operator;
    private RechargeDetail rechargeDetail;
    private BillDetailEtisalat billDetailEtisalat;
    private BillDetailDU billDetailDU;
    TextView toolbar_title;
    private String toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(MobileRechargeViewModel.class);
        viewModel.setUser(UserPreferences.Companion.getInstance().getUser(this));

        viewModel.getTYPE_ID().setValue(0);

        handleBottomBar();
        clearRadioButtons();
        clearService();
        addPaymentObservable();
        addDuPaymentObservable();

        operator = getIntent().getStringExtra("operator");
        handleOperator(operator);

        setToolbarTitle(toolbarTitle);

        PagerAdapter pagerAdapter = new SliderPageAdapter(this, getSupportFragmentManager());
        mPager.setAdapter(pagerAdapter);

        PagerAdapter pagerAdapterPrepaid = new SliderPageAdapterPrepaid(this, getSupportFragmentManager());
        mPagerPrepaid.setAdapter(pagerAdapterPrepaid);

        mPager.addOnPageChangeListener(this);
        mPagerPrepaid.addOnPageChangeListener(this);

        ((RadioButton) rgPayment.getChildAt(0)).setChecked(true);

        if (operator.equalsIgnoreCase("etisalat")) {
            ((RadioButton) rgPrepaid.getChildAt(0)).setChecked(true);
            ((RadioButton) rgPostpaid.getChildAt(0)).setChecked(true);
            viewModel.getSERVICE().setValue("WaselRecharge");
        }
        if (operator.equalsIgnoreCase("du")) {
            ((RadioButton) rgPrepaidDu.getChildAt(0)).setChecked(true);
            viewModel.getSERVICE().setValue("time");
        }

        enablePaymentMenu(R.id.rb_prepaid);
        rgPayment.setOnCheckedChangeListener((group, checkedId) -> {
            clearRadioButtons();
            clearService();

            enablePaymentMenu(checkedId);
            String oper = operator;
            if (checkedId == R.id.rb_prepaid && oper.equalsIgnoreCase("etisalat")) {
                ((RadioButton) rgPrepaid.getChildAt(0)).setChecked(true);
                viewModel.getSERVICE().setValue("WaselRecharge");
            } else if (checkedId == R.id.rb_postpaid && oper.equalsIgnoreCase("etisalat")) {
                ((RadioButton) rgPostpaid.getChildAt(0)).setChecked(true);
                viewModel.getSERVICE().setValue("ELIFE");
            }
            if (operator.equalsIgnoreCase("du") && checkedId == R.id.rb_prepaid) {
                ((RadioButton) rgPrepaidDu.getChildAt(0)).setChecked(true);
                viewModel.getSERVICE().setValue("time");
            }

            PagerAdapter pagerAdapter1 = new SliderPageAdapter(this, getSupportFragmentManager());
            mPager.setAdapter(pagerAdapter1);
        });

        rgPostpaid.setOnCheckedChangeListener(((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_broadband:
                    viewModel.getSERVICE().setValue("BROADBAND");
                    break;
                case R.id.rb_eLife:
                    viewModel.getSERVICE().setValue("ELIFE");
                    break;
                case R.id.rb_gsm:
                    viewModel.getSERVICE().setValue("GSM");

                    break;
//                case R.id.rb_del:
//                    viewModel.getSERVICE().setValue("DEL");
//                    break;
                case R.id.rb_evision:
                    viewModel.getSERVICE().setValue("EVISION");
                    break;
//                case R.id.rb_dialup:
//                    viewModel.getSERVICE().setValue("DIALUP");
//                    break;
            }

        }));


        rgPrepaid.setOnCheckedChangeListener(((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_prepaid_1:
                    viewModel.getSERVICE().setValue("WaselRecharge");
                    break;
                case R.id.rb_prepaid_2:
                    viewModel.getSERVICE().setValue("WaselRenewal");
                    break;
            }
        }));

        rgPrepaidDu.setOnCheckedChangeListener(((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rb_credit:
                    viewModel.getSERVICE().setValue("credit");
                    break;
                case R.id.rb_time:
                    viewModel.getSERVICE().setValue("time");
                    break;
                case R.id.rb_data:
                    viewModel.getSERVICE().setValue("data");
                    break;
                case R.id.rb_international:
                    viewModel.getSERVICE().setValue("international");
                    break;
            }
        }));
    }

    private void enablePaymentMenu(int checkedId) {
        switch (checkedId) {
            case R.id.rb_postpaid:
                viewModel.getTYPE_ID().setValue(1);

                mPager.setVisibility(View.VISIBLE);
                mPagerPrepaid.setVisibility(View.GONE);

                rgPrepaid.setVisibility(View.GONE);
                rgPrepaidDu.setVisibility(View.GONE);
                if (operator.equalsIgnoreCase("etisalat"))
                    rgPostpaid.setVisibility(View.VISIBLE);
                else if (operator.equalsIgnoreCase("du")) {
                    viewModel.getSERVICE().setValue("du");
                }
                break;
            case R.id.rb_prepaid:
                viewModel.getTYPE_ID().setValue(2);

                mPager.setVisibility(View.GONE);
                mPagerPrepaid.setVisibility(View.VISIBLE);

                if (operator.equalsIgnoreCase("du")) {
                    rgPrepaidDu.setVisibility(View.VISIBLE);
                    rgPrepaid.setVisibility(View.GONE);
                } else {
                    rgPrepaid.setVisibility(View.VISIBLE);
                    rgPrepaidDu.setVisibility(View.GONE);
                    rgPostpaid.setVisibility(View.GONE);
                }


                break;
        }
    }

    public void clearService() {
        viewModel.getSERVICE().setValue(null);
    }

    public void clearRadioButtons() {
//       viewModel.getSERVICE().setValue(null);
        rgPostpaid.clearCheck();
        rgPrepaid.clearCheck();
        rgPrepaidDu.clearCheck();
    }

    @Override
    public int getLayout() {
        return R.layout.activity_mobile_recharge;
    }

    @Override
    public void init() {
        findViewById(R.id.toolbar_back).setOnClickListener(view -> onBackPressed());

        toolbar_title = findViewById(R.id.toolbar_title);
        toolbar_title.setText(getString(R.string.mobile_recharge));

        mPager = findViewById(R.id.view_pager);
        mPagerPrepaid = findViewById(R.id.view_pager_prepaid);

        ivOperator = findViewById(R.id.iv_operator);

        rgPayment = findViewById(R.id.radio_group);
        rgPostpaid = findViewById(R.id.radio_group_2);
        rgPrepaid = findViewById(R.id.radio_group_3);
        rgPrepaidDu = findViewById(R.id.radio_group_4);

    }

    @Override
    public void onBackPressed() {

        if (viewModel.getTYPE_ID().getValue() == 1) {
            if (mPager.getCurrentItem() == 0)
                super.onBackPressed();
            else
                mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        } else if (viewModel.getTYPE_ID().getValue() == 2) {
            if (mPagerPrepaid.getCurrentItem() == 0)
                super.onBackPressed();
            else
                mPagerPrepaid.setCurrentItem(mPagerPrepaid.getCurrentItem() - 1);
        } else {
            super.onBackPressed();
        }
    }

    public MobileRechargeViewModel getViewModel() {
        return viewModel;
    }

    private void handleOperator(String operator) {
        switch (operator) {
            case "du": {
                toolbarTitle = getString(R.string.du_);
                ivOperator.setImageResource(R.mipmap.ic_du_xhdpi);
                break;
            }
            case "etisalat": {
                toolbarTitle = getString(R.string.etisalat_);
                ivOperator.setImageResource(R.mipmap.ic_etisalat_xhdpi);
                break;
            }
        }
    }

    public void onMobileNoSelected() {
        if (viewModel.getTYPE_ID().getValue() == 1)
            mPager.setCurrentItem(1);
        else
            mPagerPrepaid.setCurrentItem(1);

    }

    public void onTopupDetails() {
        if (viewModel.getTYPE_ID().getValue() == 1)
            mPager.setCurrentItem(3);
        else
            mPagerPrepaid.setCurrentItem(3);
    }

    public void onTopupAmount() {
        if (viewModel.getTYPE_ID().getValue() == 1) {
            Log.i("onTopupAmount: ", "Switch to OTP");
            mPager.setCurrentItem(2);
        } else {
            Log.i("onTopupAmount: ", "Switch to Details");
            mPagerPrepaid.setCurrentItem(2);
        }
    }

    @Override
    public void onOtpConfirm(@NotNull String otp) {
        if (otp.equals("")) {
            onFailure(findViewById(R.id.card_view), "Please enter the OTP");
            return;
        }

        if (viewModel == null) {
            return;
        }

        viewModel.getTopUpDetail().observe(this, detail -> {
            Log.i("tag", detail.getTransactionID());
            rechargeDetail = detail;
        });

        viewModel.getBillDetailEtisalat().observe(this, detail -> {
            Log.i("tag", detail.getTransactionID());
            billDetailEtisalat = detail;
        });

        billDetailDU = viewModel.getBillDetailDU().getValue();

        User user = viewModel.getUser();
        if (user == null) return;

        if (viewModel.getAMOUNT().getValue() == null) return;

        if (viewModel.getOperator().equalsIgnoreCase("etisalat") && viewModel.getTYPE_ID().getValue() == 1) {

            viewModel.payBill(user.getToken(), user.getSessionId(), user.getRefreshToken(), viewModel.getAccessKey(), user.getCustomerId(),
                    viewModel.getTypeKey(), viewModel.getFlexKey(), billDetailEtisalat.getTransactionID(),
                    viewModel.getMOBILE().getValue(), viewModel.getAMOUNT().getValue(), viewModel.getSERVICE().getValue(),
                    billDetailEtisalat.getProviderTransactionId(), otp);
        } else if (viewModel.getOperator().equalsIgnoreCase("du") && viewModel.getTYPE_ID().getValue() == 1) {
            viewModel.payBillDu(user.getToken(), user.getSessionId(), user.getRefreshToken(), viewModel.getAccessKey(), user.getCustomerId(),
                    viewModel.getTypeKey(), viewModel.getFlexKey(), billDetailDU.getTransactionId(),
                    viewModel.getMOBILE().getValue(), viewModel.getAMOUNT().getValue(), viewModel.getAMOUNT().getValue(),
                    billDetailDU.getProviderTransactionId(), otp);
        } else if (viewModel.getOperator().equalsIgnoreCase("etisalat") && viewModel.getTYPE_ID().getValue() == 2) {
            viewModel.payBill(user.getToken(), user.getSessionId(), user.getRefreshToken(), viewModel.getAccessKey(), user.getCustomerId(),
                    viewModel.getTypeKey(), viewModel.getFlexKey(), rechargeDetail.getTransactionID(),
                    viewModel.getMOBILE().getValue(), viewModel.getAMOUNT().getValue(), viewModel.getSERVICE().getValue(),
                    rechargeDetail.getProviderTransactionId(), otp);

        } else if (viewModel.getOperator().equalsIgnoreCase("du") && viewModel.getTYPE_ID().getValue() == 2) {
            long transId = System.currentTimeMillis();
            viewModel.payBillDu(user.getToken(), user.getSessionId(), user.getRefreshToken(), viewModel.getAccessKey(), user.getCustomerId(),
                    viewModel.getTypeKey(), viewModel.getFlexKey(), String.valueOf(transId), viewModel.getMOBILE().getValue(),
                    viewModel.getAMOUNT().getValue(), viewModel.getSERVICE().getValue(), null, otp);
        }

    }

    public void disableButtons() {
        for (int i = 0; i < rgPayment.getChildCount(); i++) {
            rgPayment.getChildAt(i).setEnabled(false);
        }

        for (int i = 0; i < rgPostpaid.getChildCount(); i++) {
            rgPostpaid.getChildAt(i).setEnabled(false);
        }

        for (int i = 0; i < rgPrepaid.getChildCount(); i++) {
            rgPrepaid.getChildAt(i).setEnabled(false);
        }

        for (int i = 0; i < rgPrepaidDu.getChildCount(); i++) {
            rgPrepaidDu.getChildAt(i).setEnabled(false);
        }
    }

    public void enableButtons() {
        for (int i = 0; i < rgPayment.getChildCount(); i++) {
            rgPayment.getChildAt(i).setEnabled(true);
        }

        for (int i = 0; i < rgPostpaid.getChildCount(); i++) {
            rgPostpaid.getChildAt(i).setEnabled(true);
        }

        for (int i = 0; i < rgPrepaid.getChildCount(); i++) {
            rgPrepaid.getChildAt(i).setEnabled(true);
        }

        for (int i = 0; i < rgPrepaidDu.getChildCount(); i++) {
            rgPrepaidDu.getChildAt(i).setEnabled(true);
        }
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {

        if (i == 0) {
            enableButtons();
            viewModel.setDataClear(true);
        } else {
            viewModel.setDataClear(false);
        }
        switch (i) {
            case 3:
                setToolbarTitle(getString(R.string.verify_otp));
                break;
            default:
                setToolbarTitle(toolbarTitle);
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    private static class SliderPageAdapter extends FragmentPagerAdapter {
        private WeakReference<MobileRechargeActivity> weakReference;

        SliderPageAdapter(MobileRechargeActivity context, FragmentManager fm) {
            super(fm);
            this.weakReference = new WeakReference<>(context);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    Bundle bundle = new Bundle();
                    bundle.putString("operator", weakReference.get().operator);
                    MobileNoFragment fragment = new MobileNoFragment();
                    fragment.setArguments(bundle);
                    return fragment;
                case 1:
                    return new TopupAmountFargment();
                case 2:
                    return new TopupDetailsFragment();
                case 3:
                    return new OTPFragment.Builder(weakReference.get())
                            .setPinLength(6)
                            .setTitle(weakReference.get().getString(R.string.enter_otp))
                            .setInstructions(weakReference.get().getString(R.string.otp_sent_to_registered_number))
                            .setButtonTitle(weakReference.get().getString(R.string.submit))
                            .build();
                default:
                    throw new IllegalStateException("Invalid Page Position");
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    void setToolbarTitle(String title) {
        toolbar_title.setText(title);
    }

    private static class SliderPageAdapterPrepaid extends FragmentPagerAdapter {
        private WeakReference<MobileRechargeActivity> weakReference;

        SliderPageAdapterPrepaid(MobileRechargeActivity context, FragmentManager fm) {
            super(fm);
            this.weakReference = new WeakReference<>(context);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    Bundle bundle = new Bundle();
                    bundle.putString("operator", weakReference.get().operator);
                    MobileNoFragment fragment = new MobileNoFragment();
                    fragment.setArguments(bundle);
                    return fragment;
                case 1:
                    return new TopupAmountFargment();

                case 2:
                    return new TopupDetailsFragment();

                case 3:
                    return new OTPFragment.Builder(weakReference.get())
                            .setPinLength(6)
                            .setTitle(weakReference.get().getString(R.string.enter_otp))
                            .setInstructions(weakReference.get().getString(R.string.otp_sent_to_registered_number))
                            .setButtonTitle(weakReference.get().getString(R.string.submit))
                            .setHasCardView(false)
                            .build();
                default:
                    throw new IllegalStateException("Invalid Page Position");
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    private void addDuPaymentObservable() {
        getViewModel().getPaymentDuResponse().observe(this, event -> {
            if (event == null) return;

            NetworkState2<BillPaymentDuResponse> paymentResponse = event.getContentIfNotHandled();


            if (paymentResponse == null) return;

            if (paymentResponse instanceof NetworkState2.Loading) {
                showProgress(getString(R.string.processing));
                return;
            }

            hideProgress();

            if (paymentResponse instanceof NetworkState2.Success) {

                BillPaymentDuResponse response = ((NetworkState2.Success<BillPaymentDuResponse>) paymentResponse).getData();

                if (response != null) {
                    if (response.getStatus() != null) {

                        if (viewModel.getTYPE_ID().getValue() == 1) {
                            DialogFragment dialogFragment = PaymentSuccessfulDialog.newInstance(getString(R.string.successfully_paid),
                                    String.format(getString(R.string.amount_in_aed), String.valueOf(response.getAvailableBalance())),
                                    R.drawable.ic_success_checked,
                                    R.color.blue,
                                    dialog -> finish());
                            dialogFragment.setCancelable(false);

                            dialogFragment.show(getSupportFragmentManager(), null);
                        } else {
                            DialogFragment dialogFragment = PaymentSuccessfulDialog.newInstance(getString(R.string.successfully_paid),
                                    String.format(getString(R.string.amount_in_aed), String.valueOf(response.getAvailableBalance())),
                                    R.drawable.ic_success_checked,
                                    R.color.blue,
                                    dialog -> finish());
                            dialogFragment.setCancelable(false);
                            dialogFragment.show(getSupportFragmentManager(), null);
                        }
                    }

                } else
                    onError(getString(R.string.un_successfully_paid));
            } else if (paymentResponse instanceof NetworkState2.Error) {

                NetworkState2.Error<BillPaymentDuResponse> error = (NetworkState2.Error<BillPaymentDuResponse>) paymentResponse;
                if (error.isSessionExpired()) {
                    onSessionExpired(error.getMessage());
                    return;
                }

                handleErrorCode(Integer.parseInt(error.getErrorCode()), error.getMessage());

//                onError(error.getMessage());

            } else if (paymentResponse instanceof NetworkState2.Failure) {

                NetworkState2.Failure<BillPaymentDuResponse> error = (NetworkState2.Failure<BillPaymentDuResponse>) paymentResponse;
                onFailure(findViewById(R.id.card_view), ConstantsKt.CONNECTION_ERROR);

            } else {
                onFailure(findViewById(R.id.card_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }

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
                    if (response.getStatus() != null) {
                        //TODO: Change dialog for TYPE_ID 2
                        if (viewModel.getTYPE_ID().getValue() == 1) {
                            DialogFragment dialogFragment = PaymentSuccessfulDialog.newInstance(getString(R.string.successfully_paid),
                                    String.format(getString(R.string.amount_in_aed), String.valueOf(response.getAvailableBalance())),
                                    R.drawable.ic_success_checked,
                                    R.color.blue,
                                    dialog -> finish());
                            dialogFragment.setCancelable(false);

                            dialogFragment.show(getSupportFragmentManager(), null);
                        } else {
                            DialogFragment dialogFragment = PaymentSuccessfulDialog.newInstance(getString(R.string.successfully_paid),
                                    String.format(getString(R.string.amount_in_aed), String.valueOf(response.getAvailableBalance())),
                                    R.drawable.ic_success_checked,
                                    R.color.blue,
                                    dialog -> finish());
                            dialogFragment.setCancelable(false);

                            dialogFragment.show(getSupportFragmentManager(), null);
                        }
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

                NetworkState2.Failure<BillPaymentResponse> error = (NetworkState2.Failure<BillPaymentResponse>) paymentResponse;
                onFailure(findViewById(R.id.card_view), ConstantsKt.CONNECTION_ERROR);

            } else {
                onFailure(findViewById(R.id.card_view), ConstantsKt.CONNECTION_ERROR);
            }


        });
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

        findViewById(R.id.toolbar_help).setOnClickListener(v -> startHelpActivity("mobileRechargeHelp"));
    }
}
