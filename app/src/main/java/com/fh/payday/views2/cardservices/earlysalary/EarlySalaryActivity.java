package com.fh.payday.views2.cardservices.earlysalary;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fh.payday.BaseActivity;
import com.fh.payday.R;
import com.fh.payday.datasource.models.Card;
import com.fh.payday.utilities.NumberFormatterKt;
import com.fh.payday.views2.dashboard.MainActivity;
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity;
import com.fh.payday.views2.locator.LocatorActivity;
import com.fh.payday.views2.message.ContactUsActivity;

public class EarlySalaryActivity extends BaseActivity implements View.OnClickListener {

    TextView toolbarTitle;
    ImageView toolbarBack, toolbarHome;
    private Card card;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        finish();
        return;

        //showFragment(new EarlySalaryFragment());

    }

    @Override
    public int getLayout() {
        return R.layout.activity_early_salary;
    }

    @Override
    public void init() {
        card = getIntent().getParcelableExtra("card");
        if (card == null) {
            finish();
            return;
        }

        toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarBack = findViewById(R.id.toolbar_back);
        toolbarTitle.setText(getResources().getString(R.string.early_salary));

        toolbarBack.setOnClickListener(this);

        TextView tvBalance = findViewById(R.id.tv_balance);
        tvBalance.setText(String.format(getString(R.string.available_balance_in_aed),
                NumberFormatterKt.getDecimalValue(card.getAvailableBalance())));

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

        findViewById(R.id.toolbar_help).setOnClickListener(v -> startHelpActivity("cardServicesHelp"));
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toolbar_back:
                onBackPressed();
                break;
        }
    }
}
