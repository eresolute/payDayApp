package com.fh.payday.views2.kyc.update.emergencycontact;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.widget.TextView;

import com.fh.payday.BaseActivity;
import com.fh.payday.R;
import com.fh.payday.datasource.models.login.User;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.preferences.UserPreferences;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.utilities.NonSwipeableViewPager;
import com.fh.payday.utilities.OnOTPConfirmListener;
import com.fh.payday.viewmodels.kyc.KycViewModel;
import com.fh.payday.views2.dashboard.MainActivity;
import com.fh.payday.views2.help.HelpActivity;
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity;
import com.fh.payday.views2.locator.LocatorActivity;
import com.fh.payday.views2.message.ContactUsActivity;
import com.fh.payday.views2.shared.custom.AlertDialogFragment;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

public class EditEmergencyActivity extends BaseActivity implements OnOTPConfirmListener {

    private NonSwipeableViewPager viewPager;
    private TextView toolbarTitle;
    private KycViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(KycViewModel.class);
        viewPager.setAdapter(new SliderPageAdapter(this, getSupportFragmentManager()));
        setToolbarTitle(getString(R.string.emergency_contact));
        handleBottomBar();
        addObserver();
       // addOtpObserver();
    }

    @Override
    public int getLayout() {
        return R.layout.activity_edit_emergency;
    }

    public KycViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public void init() {
        viewPager = findViewById(R.id.view_pager);
        toolbarTitle = findViewById(R.id.toolbar_title);

        findViewById(R.id.toolbar_back).setOnClickListener(v -> onBackPressed());

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 1) {
                    setToolbarTitle(getString(R.string.verify_otp));
                } else {
                    setToolbarTitle(getString(R.string.emergency_contact));
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    public void setToolbarTitle(String title) {
        toolbarTitle.setText(title);
    }

    public void onNameSubmit() {
        viewPager.setCurrentItem(1);
    }

    public void onNumberSubmit() {
        viewPager.setCurrentItem(2);
    }

    public void onRelationSubmit() {

        User user = UserPreferences.Companion.getInstance().getUser(this);
        if (user != null) {
            if (viewModel.getEmergencyName() == null) return;
            if (viewModel.getEmergencyMobile() == null) return;
            if (viewModel.getEmergencyRelation() == null) return;
            viewModel.updateEmergency(user.getToken(), user.getSessionId(), user.getRefreshToken(), user.getCustomerId(),
                viewModel.getEmergencyName(), viewModel.getEmergencyMobile(), viewModel.getEmergencyRelation());
            //viewModel.generateOtp(user.getToken(), user.getSessionId(), user.getRefreshToken(), user.getCustomerId());
        }

//        viewPager.setCurrentItem(3);
    }

    @Override
    public void onOtpConfirm(@NotNull String otp) {
        if (TextUtils.isEmpty(otp)) return;

        User user = UserPreferences.Companion.getInstance().getUser(this);
        if (user != null) {
            if (viewModel.getEmergencyName() == null) return;
            if (viewModel.getEmergencyMobile() == null) return;
            if (viewModel.getEmergencyRelation() == null) return;
            viewModel.updateEmergency(user.getToken(), user.getSessionId(), user.getRefreshToken(), user.getCustomerId(),
                    viewModel.getEmergencyName(), viewModel.getEmergencyMobile(), viewModel.getEmergencyRelation());
        }
    }

    private void addOtpObserver() {
        viewModel.getOtpRequest().observe(this, otp -> {
            if (otp == null) return;

            NetworkState2<String> state = otp.getContentIfNotHandled();

            if (state == null) return;
            if (state instanceof NetworkState2.Loading) {
                showProgress(getString(R.string.processing));
                return;
            }

            hideProgress();

            if (state instanceof NetworkState2.Success) {
                viewPager.setCurrentItem(3);
            } else if (state instanceof NetworkState2.Error) {

                NetworkState2.Error<String> error = (NetworkState2.Error<String>) state;
                if (error.isSessionExpired()) {
                    onSessionExpired(error.getMessage());
                    return;
                }
                onError(error.getMessage());
            } else if (state instanceof NetworkState2.Failure) {
                NetworkState2.Failure<String> error = (NetworkState2.Failure<String>) state;
                onFailure(findViewById(R.id.root_view), error.getThrowable());
            } else {
                onFailure(findViewById(R.id.root_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }

    private void addObserver() {
        viewModel.getUpdateEmergency().observe(this, event -> {
            if (event == null) return;

            NetworkState2<String> state = event.getContentIfNotHandled();

            if (state == null) return;

            if (state instanceof NetworkState2.Loading) {
                showProgress(getString(R.string.processing));
                return;
            }

            hideProgress();

            if (state instanceof NetworkState2.Success) {
                new AlertDialogFragment.Builder()
                        .setMessage(getString(R.string.contact_updated))
                        .setIcon(R.drawable.ic_success_checked_blue)
                        .setCancelable(false)
                        .setConfirmListener(dialog -> {
                            dialog.dismiss();
                            finish();
                        }).build().show(getSupportFragmentManager(), "");

            } else if (state instanceof NetworkState2.Error) {
                NetworkState2.Error<String> error = (NetworkState2.Error<String>) state;
                if (error.isSessionExpired()) {
                    onSessionExpired(error.getMessage());
                    return;
                }
                handleErrorCode(Integer.parseInt(error.getErrorCode()), error.getMessage());
            } else if (state instanceof NetworkState2.Failure) {

                NetworkState2.Failure<String> error = (NetworkState2.Failure<String>) state;
                onFailure(findViewById(R.id.root_view), error.getThrowable());

            } else {
                onFailure(findViewById(R.id.root_view), ConstantsKt.CONNECTION_ERROR);
            }


        });
    }


    private static class SliderPageAdapter extends FragmentPagerAdapter {
        private final WeakReference<EditEmergencyActivity> weakReference;
        private int NUM_PAGES = 1;

        SliderPageAdapter(EditEmergencyActivity context, FragmentManager fm) {
            super(fm);
            this.weakReference = new WeakReference<>(context);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return new EmergencyNameFragment();
          /*      case 1:
                    return new EmergencyNumberFragment();
                case 2:
                    return new EmergencyRelationFragment();*/
              /*  case 1:
                    return new OTPFragment.Builder(weakReference.get())
                            .setPinLength(6)
                            .setTitle(weakReference.get().getString(R.string.enter_otp))
                            .setInstructions(weakReference.get().getString(R.string.otp_sent_to_registered_number))
                            .setButtonTitle(weakReference.get().getString(R.string.submit))
                            .setHasCardView(false)
                            .build();*/
                default:
                    throw new IllegalStateException("Invalid Page Position");
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0)
            super.onBackPressed();
        else {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
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

        findViewById(R.id.toolbar_help).setOnClickListener(v -> startActivity(new Intent(v.getContext(), HelpActivity.class)));
    }
}
