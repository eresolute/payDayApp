package com.fh.payday.views2.payments.callingcards;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.fh.payday.BaseActivity;
import com.fh.payday.R;
import com.fh.payday.utilities.NonSwipeableViewPager;
import com.fh.payday.utilities.OnOTPConfirmListener;
import com.fh.payday.viewmodels.CallingCardViewModel;
import com.fh.payday.views.fragments.OTPFragment;
import com.fh.payday.views2.dashboard.MainActivity;
import com.fh.payday.views2.help.HelpActivity;
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity;
import com.fh.payday.views2.locator.LocatorActivity;
import com.fh.payday.views2.message.ContactUsActivity;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

public class CallingCardActivity extends BaseActivity implements OnOTPConfirmListener {
    private static final int NUM_PAGES = 3;
    private NonSwipeableViewPager mPager;
    TextView toolbar_title;
    ImageView toolbarBack, imOperator;
    CallingCardViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(CallingCardViewModel.class);
        toolbar_title.setText(getString(R.string.calling_card));
        toolbarBack.setOnClickListener(v -> onBackPressed());

        PagerAdapter mPagerAdapter = new SlidePagerAdapter(getSupportFragmentManager(), this);
        mPager.setAdapter(mPagerAdapter);

        String operator = getIntent().getStringExtra("operator");
        handleOperator(operator);
        handleBottomBar();
    }

    private void handleOperator(String operator) {
        switch (operator) {
            case "hello_card": {
                imOperator.setImageResource(R.drawable.ic_hello_card);
                break;
            }
            case "five_card": {
                imOperator.setImageResource(R.drawable.ic_five_card);
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

        findViewById(R.id.toolbar_help).setOnClickListener(v ->
            startActivity(new Intent(v.getContext(), HelpActivity.class)));
    }

    private CallingCardViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public int getLayout() {
        return R.layout.activity_calling_card;
    }

    @Override
    public void init() {
        CallingCardViewModel viewModel = ViewModelProviders.of(this).get(CallingCardViewModel.class);
        toolbar_title = findViewById(R.id.toolbar_title);
        toolbarBack = findViewById(R.id.toolbar_back);
        mPager = findViewById(R.id.view_pager);
        imOperator = findViewById(R.id.iv_operator);
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    public void onTopupSelected() {
        mPager.setCurrentItem(1);
    }

    public void onConfirm() {
        mPager.setCurrentItem(2);
    }

    @Override
    public void onOtpConfirm(@NotNull String otp) {
        /*DialogFragment dialogFragment = PaymentSuccessfulDialog.
                newInstance("Successfully Paid", "AED 1,250.00",
                        R.drawable.ic_success_checked, R.color.blue,
                        (Dialog Dialog) -> finish()
                );

        dialogFragment.show(getSupportFragmentManager(), null);*/
    }

    private static class SlidePagerAdapter extends FragmentStatePagerAdapter {
        private WeakReference<CallingCardActivity> weakReference;

        SlidePagerAdapter(FragmentManager fm, CallingCardActivity context) {
            super(fm);
            weakReference = new WeakReference<>(context);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new CallingCardTopupFragment();
                case 1:
                    return new CallingCardSummaryFragment();
                case 2:
                    return new OTPFragment.Builder(weakReference.get())
                            .setPinLength(6)
                            .setTitle(weakReference.get().getString(R.string.enter_otp))
                            .setButtonTitle(weakReference.get().getString(R.string.submit))
                            .build();
                default:
                    throw new IllegalStateException("Invalid page position");
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}
