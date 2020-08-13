package com.fh.payday.views2.servicerequest;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fh.payday.BaseActivity;
import com.fh.payday.R;
import com.fh.payday.datasource.models.login.User;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.preferences.UserPreferences;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.utilities.OnAtmPinConfirmListener;
import com.fh.payday.utilities.OnOTPConfirmListener;
import com.fh.payday.viewmodels.ServiceRequestViewModel;
import com.fh.payday.views.fragments.AlertDialogFragment;
import com.fh.payday.views.fragments.OTPFragment;
import com.fh.payday.views.shared.AtmCommonPinFragment;
import com.fh.payday.views2.dashboard.MainActivity;
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity;
import com.fh.payday.views2.locator.LocatorActivity;
import com.fh.payday.views2.message.ContactUsActivity;
import com.fh.payday.views2.servicerequest.activatecard.ActivateCardFragment;
import com.fh.payday.views2.servicerequest.paymentholiday.PaymentHolidayFragment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import kotlin.Unit;

public class ServiceRequestOptionActivity extends BaseActivity implements OnAtmPinConfirmListener,
        OnOTPConfirmListener, AlertDialogFragment.OnConfirmListener {

    public int REQUEST_UPLOAD_TICKET = 1;
    private WeakReference<ServiceRequestOptionActivity> weakReference;
    private TextView tvTitle;
    private ServiceRequestViewModel viewModel;
    private ProgressBar progressBar;
    private View container;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        weakReference = new WeakReference<>(this);
        viewModel = ViewModelProviders.of(this).get(ServiceRequestViewModel.class);

        User user = UserPreferences.Companion.getInstance().getUser(this);
        viewModel.setUser(user);

        int index = getIntent().getIntExtra("index", 0);

        if (index == 11) {
            findViewById(R.id.toolbar_help).setOnClickListener(v -> startHelpActivity("loanServicesHelp"));
        } else {
            findViewById(R.id.toolbar_help).setOnClickListener(v -> startHelpActivity("serviceRequestHelp"));
        }

        getFragment(index);
        handleBottomBar();
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
    }

    public ServiceRequestViewModel getViewModel() {
        return viewModel;
    }

    private void getFragment(int index) {
        switch (index) {
            case 0:
                setOption(getString(R.string.activate_card), R.drawable.ic_card_white);
                showFragment(new ActivateCardFragment());
                break;
            case 11:
                setToolbarText(getString(R.string.payment_holiday));
                //setOption(getString(R.string.payment_holiday), R.drawable.ic_payment_holiday_white);
                findViewById(R.id.linearLayout5).setVisibility(View.GONE);
                showFragment(new PaymentHolidayFragment());
                break;
            case 1:
                setOption(getString(R.string.card_transaction_dispute), R.drawable.ic_card_transaction_dispute_white);
                showFragment(new CardTransactionDisputeFragment());
                break;
            case 3:
                if (viewModel.getUser() != null) {
                    fetchCustomerSummary(viewModel.getUser());
                }
                setOption(getString(R.string.block_card), R.drawable.ic_lock_card_white);
//                showFragment(new AtmCommonPinFragment
//                        .Builder(weakReference.get())
//                        .setTitle(getString(R.string.enter_PIN))
//                        .setOnResumeListener(() -> setToolbarText(getString(R.string.service_requests)))
//                        .setPinLength(4)
//                        .build());
                break;

      /*      case 2:
                setOption(getString(R.string.application_status), R.drawable.ic_application_status_white);
                showFragment(new ApplicationStatusFragment());
                break;*/
        }
    }

    private void fetchCustomerSummary(@NotNull User user) {
        hideNoInternetView();
        if (!isNetworkConnected()) {
            container.setVisibility(View.INVISIBLE);
            showNoInternetView(() -> {
                fetchCustomerSummary(user);
                return Unit.INSTANCE;
            });
            return;
        }

        viewModel.fetchCustomerSummary(user.getToken(), user.getSessionId(),
                user.getRefreshToken(), user.getCustomerId());
        addObservers();
    }

    private void setOption(String string, int icon) {
        tvTitle.setText(string);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            tvTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, 0, 0, 0);
        } else {
            tvTitle.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
        }
    }

    @Override
    public int getLayout() {
        return R.layout.activity_service_request_option;
    }

    @Override
    public void init() {
        tvTitle = findViewById(R.id.tv_title);
        setToolbarText(getString(R.string.service_requests));
        findViewById(R.id.toolbar_back).setOnClickListener(v -> onBackPressed());
        progressBar = findViewById(R.id.progress_bar);
        container = findViewById(R.id.container);
    }

    public void setToolbarText(String title) {
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(title);
    }

    @Override
    public void onAtmConfirm(@NotNull String pin) {
        User user = viewModel.getUser();

        if (TextUtils.isEmpty(pin) || user == null || viewModel.getSummary() == null) {
            return;
        }

        if (!isNetworkConnected()) {
            onFailure(findViewById(R.id.root_view), getString(R.string.no_internet_connectivity));
            return;
        }

        viewModel.setAtmPin(pin);
        viewModel.generateOtp(user.getToken(), user.getSessionId(), user.getRefreshToken(), user.getCustomerId());
        replaceFragment(new OTPFragment.Builder(weakReference.get())
                .setTitle(getString(R.string.enter_otp))
                .setPinLength(6)
                .setButtonTitle(weakReference.get().getString(R.string.submit))
                .setHasCardView(false)
                .setOnResumeListener(() -> setToolbarText(getString(R.string.verify_otp)))
                .build());
    }

    @Override
    public void onOtpConfirm(@NotNull String otp) {
        User user = viewModel.getUser();
        if (TextUtils.isEmpty(otp) || user == null || viewModel.getSummary() == null) {
            return;
        }

        viewModel.blockCard(user.getToken(), user.getSessionId(), user.getRefreshToken(), getKeyBytes(), otp);
    }

    public void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, fragment)
                .commit();
    }

    public void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(Fragment.class.getName())
                .commit();
    }

    @Override
    public void onConfirm(Dialog dialog) {
        finish();
    }

    private byte[] getKeyBytes() {
        try (InputStream stream = getAssets().open(ConstantsKt.PUBLIC_KEY)) {

            byte[] buffer = new byte[8192];
            int bytesRead;

            ByteArrayOutputStream output = new ByteArrayOutputStream();

            while ((bytesRead = stream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }

            return output.toByteArray();
        } catch (IOException e) {
            return new byte[8192];
        }
    }

    private void addObservers() {
        viewModel.getCustomerSummaryState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<?> state = event.getContentIfNotHandled();

            if (state == null) return;

            if (state instanceof NetworkState2.Loading) {
                container.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                return;
            }

            container.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

            if (state instanceof NetworkState2.Success) {
                showFragment(new AtmCommonPinFragment
                        .Builder(weakReference.get())
                        .setTitle(getString(R.string.enter_atm_pin_salik))
                        .setOnResumeListener(() -> setToolbarText(getString(R.string.service_requests)))
                        .setPinLength(4)
                        .build());
            } else if (state instanceof NetworkState2.Error) {
                NetworkState2.Error<?> error = (NetworkState2.Error<?>) state;
                if (error.isSessionExpired()) {
                    onSessionExpired(error.getMessage());
                    return;
                }

                handleErrorCode(Integer.parseInt(error.getErrorCode()), error.getMessage());

//                try {
//                    int code = Integer.parseInt(error.getMessage());
//                    updateCredentials(code);
//
//                } catch (NumberFormatException e) {
//                    onError(error.getMessage());
//                }

            } else if (state instanceof NetworkState2.Failure) {
                NetworkState2.Failure<?> failure = (NetworkState2.Failure<?>) state;
                onFailure(findViewById(R.id.root_view), failure.getThrowable());
            }
        });

        viewModel.getOtpRequestState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<?> state = event.getContentIfNotHandled();
            if (state == null) return;

            if (state instanceof NetworkState2.Loading) {
                showProgress(getString(R.string.processing));
                return;
            }

            hideProgress();

            if (state instanceof NetworkState2.Error) {
                NetworkState2.Error<?> error = (NetworkState2.Error<?>) state;
                if (error.isSessionExpired()) {
                    onSessionExpired(error.getMessage());
                    return;
                }
                handleErrorCode(Integer.parseInt(error.getErrorCode()), error.getMessage());

            } else if (state instanceof NetworkState2.Failure) {
                NetworkState2.Failure<?> failure = (NetworkState2.Failure<?>) state;
                onFailure(findViewById(R.id.root_view), failure.getThrowable());
            }
        });

        viewModel.getCardBlockState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<?> state = event.getContentIfNotHandled();
            if (state == null) return;

            if (state instanceof NetworkState2.Loading) {
                showProgress(getString(R.string.processing));
                return;
            }

            hideProgress();

            if (state instanceof NetworkState2.Success) {
                // NetworkState2.Success<String> success = (NetworkState2.Success<String>) state;
                showMessage(getString(R.string.card_block_success), R.drawable.ic_success_checked, R.color.colorAccent,
                        dialog -> {
                            try {
                                dialog.dismiss();
                                finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });

            } else if (state instanceof NetworkState2.Error) {
                NetworkState2.Error<?> error = (NetworkState2.Error<?>) state;
                if (error.isSessionExpired()) {
                    onSessionExpired(error.getMessage());
                    return;
                }
                handleErrorCode(Integer.parseInt(error.getErrorCode()), error.getMessage());

            } else if (state instanceof NetworkState2.Failure) {
                NetworkState2.Failure<?> failure = (NetworkState2.Failure<?>) state;
                onFailure(findViewById(R.id.root_view), failure.getThrowable());
            } else {
                onFailure(findViewById(R.id.root_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }

    private void updateCredentials(int code) {
        if (code == 399) {
            onError("Your Emirates Id is expired or about to expire");
        }
    }
}
