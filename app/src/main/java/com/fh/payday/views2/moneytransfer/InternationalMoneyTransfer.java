package com.fh.payday.views2.moneytransfer;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.fh.payday.BaseActivity;
import com.fh.payday.R;
import com.fh.payday.datasource.models.Card;
import com.fh.payday.datasource.models.login.User;
import com.fh.payday.datasource.models.moneytransfer.AddP2PBeneficiaryResponse;
import com.fh.payday.datasource.models.moneytransfer.LocalBeneficiary;
import com.fh.payday.datasource.models.moneytransfer.LocalTransferResponse;
import com.fh.payday.datasource.models.moneytransfer.P2CBeneficiary;
import com.fh.payday.datasource.models.moneytransfer.P2PTransferResponse;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.preferences.UserPreferences;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.utilities.NonSwipeableViewPager;
import com.fh.payday.utilities.NumberFormatterKt;
import com.fh.payday.utilities.OnOTPConfirmListener;
import com.fh.payday.viewmodels.TransferViewModel;
import com.fh.payday.viewmodels.beneficiaries.BeneficiaryViewModel;
import com.fh.payday.views.fragments.OTPFragment;
import com.fh.payday.views.fragments.PaymentSuccessfulDialog;
import com.fh.payday.views2.dashboard.MainActivity;
import com.fh.payday.views2.help.HelpActivity;
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity;
import com.fh.payday.views2.locator.LocatorActivity;
import com.fh.payday.views2.message.ContactUsActivity;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

public class InternationalMoneyTransfer extends BaseActivity implements OnOTPConfirmListener {

    public static final String P2P_TRANSFER_KEY = "p2p";
    public static final String LOCAL_TRANSFER_KEY = "p2iban";
    public static final String CC_TRANSFER_KEY = "p2cc";

    private static final int NUM_PAGES = 4;
    private NonSwipeableViewPager mPager;
    private TextView toolbar_title;
    private ImageView icon1, icon2, icon3, icon4;
    public static final String TAG = "InternationalMoneyTransfer";
    private TransferViewModel viewModel;
    private BeneficiaryViewModel beneficiaryViewModel;
    public MoneyTransferType selectedType;
    private Card card;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(TransferViewModel.class);
        beneficiaryViewModel = ViewModelProviders.of(this).get(BeneficiaryViewModel.class);

        MoneyTransferType transferType = (MoneyTransferType) getIntent().getSerializableExtra("title");
        card = getIntent().getParcelableExtra("card");
        String balance = getIntent().getStringExtra("balanceLeft");

        if (balance != null) {
            viewModel.setCardBalance(balance);
        } else {
            viewModel.setCardBalance(card.getAvailableBalance());
        }
        viewModel.setSelectedCard(card);


        viewModel.setTransferType(transferType);
        initView();

        PagerAdapter mPagerAdapter = new SlidePagerAdapter(this, getSupportFragmentManager(), transferType);
        mPager.setAdapter(mPagerAdapter);

        setSelectedTab(0);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int position) {
                setSelectedTab(position);

                if (position == 0) {
                    viewModel.setDataClear(true);
                } else if (position == 1 && viewModel.getDataClear()) {
                    viewModel.setDataClear(true);
                } else {
                    viewModel.setDataClear(false);
                }

                if (position == 3) {
                    toolbar_title.setText(getString(R.string.verify_otp));
                } else {
                    if (transferType == MoneyTransferType.PAYDAY)
                        toolbar_title.setText(getString(R.string.payday_to_payday_1));
                    else if (transferType == MoneyTransferType.LOCAL)
                        toolbar_title.setText(getString(R.string.local_money_transfer_1));
                    else if (transferType == MoneyTransferType.CC)
                        toolbar_title.setText(getString(R.string.payday_to_credit_1));
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        handleBottomBar();
        addOtpObserver();
        addP2PObserver();
        addP2PBeneficiaryObserver();
        addLocalBeneficiaryObserver();
        addCCTransferObserver();
    }

    @Override
    public int getLayout() {
        return R.layout.activity_international_money_transfer;
    }

    @Override
    public void init() {
        findViewById(R.id.toolbar_back).setOnClickListener(view -> onBackPressed());
        toolbar_title = findViewById(R.id.toolbar_title);
        icon1 = findViewById(R.id.icon1);
        icon2 = findViewById(R.id.icon2);
        icon3 = findViewById(R.id.icon3);
        icon4 = findViewById(R.id.icon4);
        mPager = findViewById(R.id.view_pager);

    }

    public Card getCard() {
        return card;
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

        findViewById(R.id.toolbar_help).setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), HelpActivity.class);
            intent.putExtra("anchor", "moneyTransferHelp");
            startActivity(intent);
        });
    }

    public TransferViewModel getViewModel() {
        return viewModel;
    }

    public BeneficiaryViewModel getBeneficiaryViewModel() {
        return beneficiaryViewModel;
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    private void initView() {
        selectedType = viewModel.getTransferType();
        switch (selectedType) {
            case INTERNATIONAL:
                toolbar_title.setText(R.string.international_remittance);
                break;
            case LOCAL:
                toolbar_title.setText(R.string.local_money_transfer_1);
                break;
            case PAYDAY:
                toolbar_title.setText(R.string.payday_to_payday_1);
                break;
            case CC:
                toolbar_title.setText(R.string.payday_to_credit_1);
                break;
        }
    }


    public void changeBeneficiary() {
        mPager.setCurrentItem(0);
    }

    public void onBeneficiarySelected() {
        mPager.setCurrentItem(1);
    }

    public void onAmount() {
        mPager.setCurrentItem(2);
    }

    public void onTransferPurpose() {
        mPager.setCurrentItem(3);
    }

    public void onConfirmTransfer() {
        User user = UserPreferences.Companion.getInstance().getUser(this);
        if (user == null) return;
        viewModel.getOtp(user.getToken(), user.getSessionId(), user.getSessionId(), user.getCustomerId());
    }


    private void setSelectedTab(int position) {
        switch (position) {
            case 0:
                icon2.setImageResource(R.drawable.ic_beneficiary_active);
                icon3.setImageResource(R.drawable.ic_transfer_details);
                icon4.setImageResource(R.drawable.ic_summary);
                break;
            case 1:
                icon2.setImageResource(R.drawable.ic_done);
                icon3.setImageResource(R.drawable.ic_transfer_details_active);
                icon4.setImageResource(R.drawable.ic_summary);
                break;

         /*   case 2:
                icon2.setImageResource(R.drawable.ic_done);
                icon3.setImageResource(R.drawable.ic_transfer_details_active);
                icon4.setImageResource(R.drawable.ic_summary);
                break;*/

            case 2:
                icon2.setImageResource(R.drawable.ic_done);
                icon3.setImageResource(R.drawable.ic_done);
                icon4.setImageResource(R.drawable.ic_summary_active);
                break;
            case 3:
                icon2.setImageResource(R.drawable.ic_done);
                icon3.setImageResource(R.drawable.ic_done);
                icon4.setImageResource(R.drawable.ic_summary_active);
                break;

            default:
                icon2.setImageResource(R.drawable.ic_beneficiary);
                icon3.setImageResource(R.drawable.ic_transfer_details);
                icon4.setImageResource(R.drawable.ic_summary);
                break;
        }
    }

    @Override
    public void onOtpConfirm(@NotNull String otp) {
        MoneyTransferType type = viewModel.getTransferType();
        User user = UserPreferences.Companion.getInstance().getUser(this);
        if (user == null) return;
        if (type == null) return;
        viewModel.setOtp(otp);

        if (!isNetworkConnected()) {
            onFailure(findViewById(R.id.root_view), getString(R.string.no_internet_connectivity));
        }
        switch (type) {
            case PAYDAY:
                if (TextUtils.isEmpty(otp) || viewModel == null || viewModel.getSelectedP2PBeneficiary() == null || viewModel.getAmount() == null) return;

                if (viewModel.isChecked()) {
                    viewModel.addP2PBeneficiary(user.getToken(), user.getSessionId(), user.getRefreshToken(),
                            user.getCustomerId(), viewModel.getSelectedP2PBeneficiary().getMobileNumber(),
                            otp);
                } else {
                    viewModel.sendP2P(user.getToken(), user.getSessionId(), user.getRefreshToken(),
                            user.getCustomerId(), viewModel.getSelectedP2PBeneficiary().getMobileNumber(),
                            viewModel.getAmount(), otp);
                }
                break;
            case LOCAL:
                if (TextUtils.isEmpty(otp) || viewModel == null || viewModel.getSelectedLocalBeneficiary() == null
                        || viewModel.getAmount() == null) return;

                LocalBeneficiary beneficiary = viewModel.getSelectedLocalBeneficiary();
                viewModel.sendLocalMoney(
                        user.getToken(),
                        user.getSessionId(),
                        user.getRefreshToken(),
                        user.getCustomerId(),
                        beneficiary.getIBAN(),
                        beneficiary.getBeneficiaryName(),
                        viewModel.getAmount(),
                        beneficiary.getBank(),
                        otp);
                break;
            case CC:
                if (TextUtils.isEmpty(otp) || viewModel == null || viewModel.getSelectedP2CBeneficiary() == null
                        || viewModel.getAmount() == null) return;
                P2CBeneficiary p2Cbeneficiary = viewModel.getSelectedP2CBeneficiary();
                viewModel.sendCCMoney(
                        user.getToken(),
                        user.getSessionId(),
                        user.getRefreshToken(),
                        user.getCustomerId(),
                        p2Cbeneficiary.getCreditCardNo(),
                        p2Cbeneficiary.getShortName(),
                        viewModel.getAmount(),
                        p2Cbeneficiary.getBankName(),
                        otp);
                break;
        }

    }

    private void addP2PBeneficiaryObserver() {
        viewModel.getAddPayday2PaydayBeneficiaryState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<AddP2PBeneficiaryResponse> response = event.getContentIfNotHandled();

            if (response == null) return;
            if (response instanceof NetworkState2.Loading) {
                showProgress(getString(R.string.processing));
                return;
            }

//            hideProgress();

            if (response instanceof NetworkState2.Success) {

                User user = UserPreferences.Companion.getInstance().getUser(this);
                String otp = viewModel.getOtp();
                if (user == null) return;
                if (otp == null) return;

                viewModel.sendP2P(user.getToken(), user.getSessionId(), user.getRefreshToken(),
                        user.getCustomerId(), viewModel.getSelectedP2PBeneficiary().getMobileNumber(),
                        viewModel.getAmount(), otp);

            } else if (response instanceof NetworkState2.Error) {
                hideProgress();
                NetworkState2.Error<?> error = (NetworkState2.Error<?>) response;

                if (error.isSessionExpired()) {
                    onSessionExpired(error.getMessage());
                    return;
                }

                handleErrorCode(Integer.parseInt(error.getErrorCode()), error.getMessage());
//                if (Integer.parseInt(error.getErrorCode()) == ConstantsKt.STATUS_ACCOUNT_NOT_ACTIVE) {
//                    onError(error.getMessage(), false,
//                            R.drawable.ic_error, R.color.color_red,
//                            dialog -> {},
//                            dialog -> {},
//                            dialog -> {
//                                dialog.dismiss();
//                                finish();
//                            });
//                    return;
//                }
//
//                onError(error.getMessage());

            } else if (response instanceof NetworkState2.Failure) {
                hideProgress();
                NetworkState2.Failure<?> error = (NetworkState2.Failure<?>) response;
                onFailure(findViewById(R.id.root_view), error.getThrowable());
            } else {
                hideProgress();
                onFailure(findViewById(R.id.root_view), new Throwable(ConstantsKt.CONNECTION_ERROR));
            }


        });
    }
    private void addP2PObserver() {
        viewModel.getP2pResponseState().observe(this, event -> {
            if(event == null) return;

            NetworkState2<P2PTransferResponse> otpResponse = event.getContentIfNotHandled();
            if (otpResponse == null) return;

            if (otpResponse instanceof NetworkState2.Loading) {
                if (!viewModel.isChecked())
                    showProgress(getString(R.string.processing));
                return;
            }

            hideProgress();

            if (otpResponse instanceof NetworkState2.Success) {

                if(card == null || viewModel.getAmount() == null) return;

                P2PTransferResponse response = ((NetworkState2.Success<P2PTransferResponse>) otpResponse).getData();
                if (response == null) return;
                DialogFragment dialogFragment = PaymentSuccessfulDialog.newInstance(
            getString(R.string.successfully_transferred),
                        String.format(getString(R.string.amount_in_aed), NumberFormatterKt.getDecimalValue(response.getBalance())),
                        R.drawable.ic_success_checked,
                        R.color.blue,
                        dialog -> {
                            Intent intent = new Intent();
                            intent.putExtra("balance", response.getBalance());
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        });
                dialogFragment.show(getSupportFragmentManager(), null);
                dialogFragment.setCancelable(false);

            } else if(otpResponse instanceof NetworkState2.Error) {
                NetworkState2.Error<P2PTransferResponse> error = (NetworkState2.Error<P2PTransferResponse>) otpResponse;

                if (error.isSessionExpired()) {
                    onSessionExpired(error.getMessage());
                    return;
                }

                handleErrorCode(
                        Integer.parseInt(error.getErrorCode()), error.getMessage()
                );
//                if (Integer.parseInt(error.getErrorCode()) == ConstantsKt.STATUS_ACCOUNT_NOT_ACTIVE
//                        || ((Integer.parseInt(error.getErrorCode()) >= 166) || Integer.parseInt(error.getErrorCode()) <= 171)) {
//                    onError(error.getMessage(), false,
//                            R.drawable.ic_error, R.color.color_red,
//                            dialog -> {},
//                            dialog -> {},
//                            dialog -> {
//                                dialog.dismiss();
//                                finish();
//                            });
//                } else {
//                    onError(error.getMessage());
//                }
            } else if (otpResponse instanceof NetworkState2.Failure) {
                NetworkState2.Failure<P2PTransferResponse> error = (NetworkState2.Failure<P2PTransferResponse>) otpResponse;
                onFailure(findViewById(R.id.card_view), error.getThrowable());

            } else {
                onFailure(findViewById(R.id.card_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }

    private void addLocalBeneficiaryObserver() {
        viewModel.getLocalTransferResponse().observe(this, event -> {
            if(event == null) return;

            NetworkState2<LocalTransferResponse> otpResponse = event.getContentIfNotHandled();
            if (otpResponse == null) return;

            if (otpResponse instanceof NetworkState2.Loading) {
                showProgress(getString(R.string.processing));
                return;
            }

            hideProgress();

            if (otpResponse instanceof NetworkState2.Success) {

                if(card == null || viewModel.getAmount() == null) return;
                LocalTransferResponse data = ((NetworkState2.Success<LocalTransferResponse>) otpResponse).getData();
                DialogFragment dialogFragment = PaymentSuccessfulDialog.newInstance(
                        getString(R.string.successfully_transferred),
                        String.format(getString(R.string.amount_in_aed), NumberFormatterKt.getDecimalValue(data.getBalance())),
                        R.drawable.ic_success_checked,
                        R.color.blue, dialog -> {
                            Intent intent = new Intent();
                            intent.putExtra("balance", data.getBalance());
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        });
                dialogFragment.show(getSupportFragmentManager(), null);
                dialogFragment.setCancelable(false);

            } else if(otpResponse instanceof NetworkState2.Error) {
                NetworkState2.Error<LocalTransferResponse> error = (NetworkState2.Error<LocalTransferResponse>) otpResponse;

                if (error.isSessionExpired()) {
                    onSessionExpired(error.getMessage());
                    return;
                }

                handleErrorCode( Integer.parseInt(error.getErrorCode()), error.getMessage());

//                if (Integer.parseInt(error.getErrorCode()) == ConstantsKt.STATUS_ACCOUNT_NOT_ACTIVE) {
//                    onError(error.getMessage(), false,
//                            R.drawable.ic_error,
//                            R.color.color_red,
//                            dialog -> {},
//                            dialog -> {},
//                            dialog -> {
//                                dialog.dismiss();
//                                finish();
//                            });
//                } else {
//                    onError(error.getMessage());
//                }
            } else if (otpResponse instanceof NetworkState2.Failure) {
                NetworkState2.Failure<LocalTransferResponse> error = (NetworkState2.Failure<LocalTransferResponse>) otpResponse;
                onFailure(findViewById(R.id.card_view), error.getThrowable());

            } else {
                onFailure(findViewById(R.id.card_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }

    private void addCCTransferObserver() {
        viewModel.getCcTransferResponse().observe(this, event -> {
            if(event == null) return;

            NetworkState2<LocalTransferResponse> otpResponse = event.getContentIfNotHandled();
            if (otpResponse == null) return;

            if (otpResponse instanceof NetworkState2.Loading) {
                showProgress(getString(R.string.processing));
                return;
            }

            hideProgress();

            if (otpResponse instanceof NetworkState2.Success) {

                if(card == null || viewModel.getAmount() == null) return;
                LocalTransferResponse data = ((NetworkState2.Success<LocalTransferResponse>) otpResponse).getData();
                DialogFragment dialogFragment = PaymentSuccessfulDialog.newInstance(
                        getString(R.string.successfully_transferred),
                        String.format(getString(R.string.amount_in_aed), NumberFormatterKt.getDecimalValue(data.getBalance())),
                        R.drawable.ic_success_checked,
                        R.color.blue, dialog -> {
                            Intent intent = new Intent();
                            intent.putExtra("balance", data.getBalance());
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        });
                dialogFragment.show(getSupportFragmentManager(), null);
                dialogFragment.setCancelable(false);

            } else if(otpResponse instanceof NetworkState2.Error) {
                NetworkState2.Error<LocalTransferResponse> error = (NetworkState2.Error<LocalTransferResponse>) otpResponse;

                if (error.isSessionExpired()) {
                    onSessionExpired(error.getMessage());
                    return;
                }

                handleErrorCode( Integer.parseInt(error.getErrorCode()), error.getMessage());


//                if (Integer.parseInt(error.getErrorCode()) == ConstantsKt.STATUS_CARD_NOT_ACTIVE) {
//                    onError(error.getMessage(), false,
//                            R.drawable.ic_error, R.color.color_red,
//                            dialog -> {},
//                            dialog -> {},
//                            dialog -> {
//                                dialog.dismiss();
//                                finish();
//                            });
//                } else {
//                    onError(error.getMessage());
//                }
            } else if (otpResponse instanceof NetworkState2.Failure) {
                NetworkState2.Failure<LocalTransferResponse> error = (NetworkState2.Failure<LocalTransferResponse>) otpResponse;
                onFailure(findViewById(R.id.card_view), error.getThrowable());

            } else {
                onFailure(findViewById(R.id.card_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }


    private void addOtpObserver() {
        viewModel.getOptResponseState().observe(this, event -> {
            if(event == null) return;

            NetworkState2<String> otpResponse = event.getContentIfNotHandled();
            if (otpResponse == null) return;

            if (otpResponse instanceof NetworkState2.Loading) {
                showProgress(getString(R.string.processing));
                return;
            }

            hideProgress();

            if (otpResponse instanceof NetworkState2.Success) {
                mPager.setCurrentItem(4);

            } else if(otpResponse instanceof NetworkState2.Error) {
                NetworkState2.Error<?> error = (NetworkState2.Error<?>) otpResponse;

                if (error.isSessionExpired()) {
                    onSessionExpired(error.getMessage());
                    return;
                }

                handleErrorCode( Integer.parseInt(error.getErrorCode()), error.getMessage());


//                if(Integer.valueOf(error.getErrorCode()) == ConstantsKt.STATUS_CARD_NOT_ACTIVE) {
//                    showMessage(error.getMessage(), R.drawable.ic_warning, R.color.colorError, dialog -> {
//                        finish();
//                    });
//                } else {
//                    onError(error.getMessage());
//                }
            } else if (otpResponse instanceof NetworkState2.Failure) {
                NetworkState2.Failure<String> error = (NetworkState2.Failure<String>) otpResponse;
                onFailure(findViewById(R.id.card_view), ((NetworkState2.Failure<String>) otpResponse).getThrowable());

            } else {
                onFailure(findViewById(R.id.card_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }

    private static class SlidePagerAdapter extends FragmentStatePagerAdapter {
        private WeakReference<InternationalMoneyTransfer> weakReference;
        private MoneyTransferType transferType;

        SlidePagerAdapter(InternationalMoneyTransfer mContext, FragmentManager fm, MoneyTransferType transferType) {
            super(fm);
            this.weakReference = new WeakReference<>(mContext);
            this.transferType = transferType;
        }

        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            switch (position) {
//                case 0:
//                    AccountsFragment fragmentAccount = new AccountsFragment();
//                    switch (transferType) {
//                        case INTERNATIONAL:
//                            bundle.putSerializable("label", MoneyTransferType.INTERNATIONAL);
//                            fragmentAccount.setArguments(bundle);
//                            return fragmentAccount;
//                        case LOCAL:
//                            bundle.putSerializable("label", MoneyTransferType.LOCAL);
//                            fragmentAccount.setArguments(bundle);
//                            return fragmentAccount;
//                        case PAYDAY:
//                            bundle.putSerializable("label", MoneyTransferType.PAYDAY);
//                            fragmentAccount.setArguments(bundle);
//                            return fragmentAccount;
//                    }
//                    return fragmentAccount;
                case 0:
                    return new BeneficiariesFragment();
                case 1:
                    TransferDetailsFragment fragment = new TransferDetailsFragment();
                    switch (transferType) {
                        case CC:
                            bundle.putSerializable("label", MoneyTransferType.CC);
                            fragment.setArguments(bundle);
                            return fragment;
                        case LOCAL:
                            bundle.putSerializable("label", MoneyTransferType.LOCAL);
                            fragment.setArguments(bundle);
                            return fragment;
                        case PAYDAY:
                            bundle.putSerializable("label", MoneyTransferType.PAYDAY);
                            fragment.setArguments(bundle);
                            return fragment;
                    }
                /*case 2:
                    FragmentTransferPurpose fragmentTransferPurpose = new FragmentTransferPurpose();
                    switch (transferType) {
                        case CC:
                            bundle.putSerializable("label", MoneyTransferType.CC);
                            fragmentTransferPurpose.setArguments(bundle);
                            return fragmentTransferPurpose;
                        case LOCAL:
                            bundle.putSerializable("label", MoneyTransferType.LOCAL);
                            fragmentTransferPurpose.setArguments(bundle);
                            return fragmentTransferPurpose;
                        case PAYDAY:
                            bundle.putSerializable("label", MoneyTransferType.PAYDAY);
                            fragmentTransferPurpose.setArguments(bundle);
                            return fragmentTransferPurpose;
                    }*/
                case 2:
                    TransferSummaryFragment summaryFragment = new TransferSummaryFragment();
                    switch (transferType) {
                        case CC:
                            bundle.putSerializable("label", MoneyTransferType.CC);
                            summaryFragment.setArguments(bundle);
                            return summaryFragment;
                        case LOCAL:
                            bundle.putSerializable("label", MoneyTransferType.LOCAL);
                            summaryFragment.setArguments(bundle);
                            return summaryFragment;
                        case PAYDAY:
                            bundle.putSerializable("label", MoneyTransferType.PAYDAY);
                            summaryFragment.setArguments(bundle);
                            return summaryFragment;
                    }
                case 3:
                    return new OTPFragment.Builder(weakReference.get())
                        .setPinLength(6)
                        .setTitle(weakReference.get().getString(R.string.enter_otp))
                        .setInstructions(weakReference.get().getString(R.string.otp_sent_to_registered_number))
                        .setButtonTitle(weakReference.get().getString(R.string.submit))
                        .build();
                default:
                    throw new IllegalStateException("Invalid page position");
            }
        }

        @Override
        public int getCount () {
            return NUM_PAGES;
        }

    }
}