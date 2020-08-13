package com.fh.payday.views2.cardservices.statements;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.fh.payday.BaseActivity;
import com.fh.payday.R;
import com.fh.payday.views2.dashboard.MainActivity;
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity;
import com.fh.payday.views2.locator.LocatorActivity;
import com.fh.payday.views2.message.ContactUsActivity;

public class CardStatementActivity extends BaseActivity implements View.OnClickListener {

    TextView tvMonthly, tvMiniStatement, tvEmailSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        tvMonthly.setOnClickListener(this);
        tvMiniStatement.setOnClickListener(this);
        tvEmailSubscription.setOnClickListener(this);

        handleBottomBar();
    }

    @Override
    public int getLayout() {
        return R.layout.activity_card_statement;
    }

    @Override
    public void init() {
        findViewById(R.id.toolbar_back).setOnClickListener(view -> onBackPressed());
        TextView toolbar_title = findViewById(R.id.toolbar_title);
        toolbar_title.setText(getString(R.string.statement_type));

        tvMonthly = findViewById(R.id.tv_monthly);
        tvMiniStatement = findViewById(R.id.tv_mini_statement);
        tvEmailSubscription = findViewById(R.id.tv_email_subscription);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, CardStatementTypeActivity.class);
        switch (view.getId()) {
            case R.id.tv_monthly:
                intent.putExtra("index", 0);
                startActivity(intent);
                break;
            case R.id.tv_mini_statement:
                intent.putExtra("index", 1);
                Bundle bundle = getIntent().getExtras();
                if (bundle != null) {
                    intent.putExtras(bundle);
                }
                startActivity(intent);
                break;
            case R.id.tv_email_subscription:
                intent.putExtra("index", 2);
                startActivity(intent);
                break;
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

        findViewById(R.id.toolbar_help).setOnClickListener(v -> startHelpActivity("cardServicesHelp"));
    }

}
