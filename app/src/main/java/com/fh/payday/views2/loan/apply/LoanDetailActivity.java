package com.fh.payday.views2.loan.apply;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.TextView;

import com.fh.payday.BaseActivity;
import com.fh.payday.R;
import com.fh.payday.datasource.models.CustomerSummary;
import com.fh.payday.viewmodels.AccountSummaryViewModel;
import com.fh.payday.views2.accountsummary.LoanDetailsFragment;

public class LoanDetailActivity extends BaseActivity {

    private AccountSummaryViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findViewById(R.id.btm_home).setOnClickListener(this);
        findViewById(R.id.btm_menu_branch).setOnClickListener(this);
        findViewById(R.id.btm_menu_support).setOnClickListener(this);
        findViewById(R.id.btm_menu_loan_calc).setOnClickListener(this);
        findViewById(R.id.toolbar_help).setOnClickListener(this);
    }

    @Override
    public int getLayout() {
        return R.layout.activity_loan_detail;
    }

    @Override
    public void init() {
        viewModel = ViewModelProviders.of(this).get(AccountSummaryViewModel.class);
        CustomerSummary summary = getIntent().getParcelableExtra("summary");
        viewModel.setSummary(summary);
        findViewById(R.id.toolbar_back).setOnClickListener(view -> onBackPressed());
        TextView toolbar_title = findViewById(R.id.toolbar_title);
        toolbar_title.setText(R.string.loans);

        showFragment(new LoanDetailsFragment());
    }

    public AccountSummaryViewModel getViewModel () {
        return viewModel;
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, fragment)
                .commit();
    }
}
