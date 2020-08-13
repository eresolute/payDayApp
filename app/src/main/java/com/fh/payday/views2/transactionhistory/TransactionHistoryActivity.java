package com.fh.payday.views2.transactionhistory;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.TextView;

import com.fh.payday.BaseActivity;
import com.fh.payday.R;
import com.fh.payday.viewmodels.TransactionHistoryViewModel;
import com.fh.payday.views2.dashboard.MainActivity;
import com.fh.payday.views2.locator.LocatorActivity;
import com.fh.payday.views2.message.ContactUsActivity;

public class TransactionHistoryActivity extends BaseActivity {

    TransactionHistoryViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(TransactionHistoryViewModel.class);

        String loanNumber = getIntent().getStringExtra("loanNumber");
        viewModel.setLoanNumber(loanNumber);
        showFragment(new TransactionHistoryFragment());

        handleBottomBar();
    }

    public TransactionHistoryViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public int getLayout() {
        return R.layout.activity_transaction_history;
    }

    @Override
    public void init() {
        findViewById(R.id.toolbar_back).setOnClickListener(view -> onBackPressed());
        TextView toolbar_title = findViewById(R.id.toolbar_title);
        toolbar_title.setText(getString(R.string.transaction_history));
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
                .addToBackStack(fragment.getClass().getName())
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

        });

        findViewById(R.id.toolbar_help).setOnClickListener(v -> {
            startHelpActivity("transactionHistoryHelp");
        });
    }
}
