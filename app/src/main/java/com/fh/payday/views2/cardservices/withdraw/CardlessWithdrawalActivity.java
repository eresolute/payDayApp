package com.fh.payday.views2.cardservices.withdraw;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.widget.TextView;

import com.fh.payday.BaseActivity;
import com.fh.payday.R;
import com.fh.payday.datasource.models.Card;
import com.fh.payday.utilities.CardUtilsKt;
import com.fh.payday.utilities.NonSwipeableViewPager;
import com.fh.payday.utilities.NumberFormatterKt;
import com.fh.payday.views2.dashboard.MainActivity;
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity;
import com.fh.payday.views2.locator.LocatorActivity;
import com.fh.payday.views2.message.ContactUsActivity;

public class CardlessWithdrawalActivity extends BaseActivity {

    NonSwipeableViewPager viewPager;
    private static final int NUM_PAGES = 5;
    SlidePagerAdapter adapter;
    TextView toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new SlidePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        toolbarTitle.setText(getResources().getString(R.string.cardless_withdrawal));

        handleBottomBar();
    }

    @Override
    public int getLayout() {
        return R.layout.activity_cardless_withdrawal;
    }

    @Override
    public void init() {
        viewPager = findViewById(R.id.view_pager);
        toolbarTitle = findViewById(R.id.toolbar_title);

        handleCardInfo();
        findViewById(R.id.toolbar_back).setOnClickListener(v -> onBackPressed());
    }

    private void handleCardInfo() {
        Card card = getIntent().getParcelableExtra("card");
        if (card == null) {
            finish();
            return;
        }

        TextView tvCardName = findViewById(R.id.tv_card_name);
        TextView tvCardNumber = findViewById(R.id.tv_card_number);
        TextView tvCardBalance = findViewById(R.id.tv_card_balance);

        tvCardName.setText(card.getCardName());
        tvCardNumber.setText(CardUtilsKt.maskCardNumber(card.getCardNumber(), "#### XXXX XXXX ####"));
        tvCardBalance.setText(String.format(getString(R.string.available_balance_in_aed),
                NumberFormatterKt.getDecimalValue(card.getAvailableBalance())));
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
                startHelpActivity("cardServicesHelp")
        );
    }

    public void onAmountEntered() {
        hideKeyboard();
        viewPager.setCurrentItem(1);
    }

    public void onQRScan() {
        hideKeyboard();
        viewPager.setCurrentItem(2);
    }

    public void onPinEnter() {
        hideKeyboard();
        //viewPager.setCurrentItem(3);
    }

    public void onToken() {
        hideKeyboard();
        viewPager.setCurrentItem(4);
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
                    return new CardlessWithdrawAmountFragment();
                case 1:
                    return new CardlessWithdrawScanQRFragment();
                case 2:
                    return new CardlessWithdrawPINFragment();
                case 3:
                    return new CardlessWithdrawTokenFragment();
                case 4:
                    return new CardlessWithdrawSuccessFragment();
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
        if (viewPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }
}
