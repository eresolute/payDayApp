package com.fh.payday.views2.transactionhistory.paydaycard;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.TextView;

import com.fh.payday.BaseActivity;
import com.fh.payday.R;
import com.fh.payday.datasource.models.CustomerSummary;
import com.fh.payday.viewmodels.AccountSummaryViewModel;
import com.fh.payday.views2.accountsummary.CardDetailsFragment;
import com.fh.payday.views2.dashboard.MainActivity;
import com.fh.payday.views2.help.HelpActivity;
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity;
import com.fh.payday.views2.locator.LocatorActivity;
import com.fh.payday.views2.message.ContactUsActivity;

public class PaydayCardActivity extends BaseActivity {
    AccountSummaryViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView toolbar_title = findViewById(R.id.toolbar_title);
        toolbar_title.setText(getString(R.string.payday_card_title));

        handleBottomBar();
    }

    @Override
    public int getLayout() {
        return R.layout.activity_payday_card;
    }

    @Override
    public void init() {
        viewModel = ViewModelProviders.of(this).get(AccountSummaryViewModel.class);
        CustomerSummary summary = getIntent().getParcelableExtra("summary");
        viewModel.setSummary(summary);
        showFragment(new CardDetailsFragment());
        findViewById(R.id.toolbar_back).setOnClickListener(view -> onBackPressed());
    }

    public AccountSummaryViewModel getViewModel() {
        return viewModel;
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, fragment)
                .commit();
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
            startActivity(new Intent(v.getContext(), HelpActivity.class));
        });
    }

}
