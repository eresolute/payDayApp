package com.fh.payday.views2.products;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import com.fh.payday.BaseActivity;
import com.fh.payday.R;
import com.fh.payday.viewmodels.LoanViewModel;
import com.fh.payday.views2.cardservices.earlysalary.EarlySalaryActivity;
import com.fh.payday.views2.dashboard.MainActivity;
import com.fh.payday.views2.help.HelpActivity;
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity;
import com.fh.payday.views2.locator.LocatorActivity;
import com.fh.payday.views2.message.ContactUsActivity;
import com.fh.payday.views2.products.card.ApplyCCFragment;


public class ProductOptionActivity extends BaseActivity implements View.OnClickListener {

    private TextView toolbarTitle;
    private LoanViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(LoanViewModel.class);
        handleBottomBar();
    }

    @Override
    public int getLayout() {
        return R.layout.activity_product_option;
    }

    @Override
    public void init() {
        toolbarTitle = findViewById(R.id.toolbar_title);
        findViewById(R.id.toolbar_back).setOnClickListener(view -> onBackPressed());

        int index = getIntent().getIntExtra("index", 0);
        showProductFragment(index);
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

    public LoanViewModel getViewModel() {
        return viewModel;
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

    public void setToolbarTitle(String title) {
        toolbarTitle.setText(title);
    }

    private void showProductFragment(int index) {
        switch (index) {
            case 0:
                setToolbarTitle(getResources().getString(R.string.apply_cc));
                showFragment(ApplyCCFragment.newInstance());
                break;
            case 2:
                setToolbarTitle(getResources().getString(R.string.apply_dc));
                showFragment(ApplyCCFragment.newInstance());
                break;
            case 3:
                break;
            case 4:
                startActivity(new Intent(this, EarlySalaryActivity.class));
                finish();
                break;
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toolbar_back:
                onBackPressed();
                break;
        }
    }
}
