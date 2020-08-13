package com.fh.payday.views2.loan.apply;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.TextView;

import com.fh.payday.BaseActivity;
import com.fh.payday.R;
import com.fh.payday.utilities.NonSwipeableViewPager;
import com.fh.payday.views2.dashboard.LoanOfferDetailsFragment;
import com.fh.payday.views2.dashboard.MainActivity;
import com.fh.payday.views2.help.HelpActivity;
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity;
import com.fh.payday.views2.locator.LocatorActivity;
import com.fh.payday.views2.message.ContactUsActivity;

public class LoanOfferActivity extends BaseActivity {
    private NonSwipeableViewPager mPager;
    private TextView mToolbarTitle;
    private static final int NUM_PAGES = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleBottomBar();
    }

    @Override
    public int getLayout() {
        return R.layout.activity_loan_offer;
    }

    @Override
    public void init() {
        initViews();
        PagerAdapter mPagerAdapter = new LoanOfferActivity.SlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mToolbarTitle = findViewById(R.id.toolbar_title);
        setToolbarTitle(R.string.apply_for_loan);

        findViewById(R.id.toolbar_back).setOnClickListener(view -> onBackPressed());

        setToolbarTitle(0);

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                setToolbarTitle(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

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

        findViewById(R.id.toolbar_help).setOnClickListener(v -> startActivity(new Intent(v.getContext(), HelpActivity.class)));
    }

    public void setToolbarTitle(int position) {
        switch (position) {
            case 0:
                mToolbarTitle.setText(getResources().getString(R.string.loan_offer_details));
                break;
            case 1:
                mToolbarTitle.setText(getResources().getString(R.string.verify_otp));
                break;
            default:
                mToolbarTitle.setText(getResources().getString(R.string.loan_offer_details));
        }
    }

    public void onLoanAccept() {
        setToolbarTitle(R.string.verify_otp);
        mPager.setCurrentItem(1);
    }

    private void initViews() {
        mPager = findViewById(R.id.view_pager);

    }

    private static class SlidePagerAdapter extends FragmentStatePagerAdapter {
        SlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }


        // todo use strings.xml for string resource
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new LoanOfferFragment();
                case 1:
                    return new LoanOfferDetailsFragment();
                default:
                    throw new IllegalStateException("Invalid page position");
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }
}
