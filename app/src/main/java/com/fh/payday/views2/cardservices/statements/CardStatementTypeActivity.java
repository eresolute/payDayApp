package com.fh.payday.views2.cardservices.statements;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.TextView;

import com.fh.payday.BaseActivity;
import com.fh.payday.R;
import com.fh.payday.utilities.DateTime;
import com.fh.payday.viewmodels.cardservices.TransactionViewModel;
import com.fh.payday.views2.dashboard.MainActivity;
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity;
import com.fh.payday.views2.locator.LocatorActivity;
import com.fh.payday.views2.message.ContactUsActivity;
import com.fh.payday.views2.transactionhistoryv2.TransactionHistoryV2Activity;

import java.util.Calendar;

public class CardStatementTypeActivity extends BaseActivity implements MiniStatementsFragment.OnItemClick {
    TextView toolbar_title;
    TransactionViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(TransactionViewModel.class);

        int index = getIntent().getIntExtra("index", 0);
        getFragment(index);
        handleBottomBar();
    }

    public TransactionViewModel getViewModel() {
        return viewModel;
    }

    private void getFragment(int index) {
        switch (index) {
            case 0:
                setToolbarTitle(getResources().getString(R.string.card_statement));
                showFragment(new MonthlyStatementsFragment());
                break;
            case 1:
                setToolbarTitle(getResources().getString(R.string.statement_type));
                showFragment(MiniStatementsFragment.newInstance());
                break;
            case 2:
                setToolbarTitle(getResources().getString(R.string.statement_type));
                showFragment(new CardEmailSubscriptionFragment());
                break;
        }
    }

    @Override
    public int getLayout() {
        return R.layout.activity_card_statement_type;
    }

    @Override
    public void init() {
        findViewById(R.id.toolbar_back).setOnClickListener(view -> onBackPressed());
        toolbar_title = findViewById(R.id.toolbar_title);


    }

    public void setToolbarTitle(String title) {
        toolbar_title.setText(title);

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

    @Override
    public void onItemClick(String title, int index) {
        Bundle bundle = getIntent().getExtras();

        switch (index) {
            case 0: {
                Calendar c = Calendar.getInstance();
                c.add(Calendar.MONTH, -1);
                Intent intent = new Intent(this, TransactionHistoryV2Activity.class);
                intent.putExtra("min_date", DateTime.Companion.convertToString(c));
                intent.putExtra("max_date", DateTime.Companion.convertToString(Calendar.getInstance()));
                intent.putExtra("transaction_type", TransactionHistoryV2Activity.THREE_MONTH_STATEMENT);
                if (bundle != null) {
                    intent.putExtras(bundle);
                }
                startActivity(intent);
                break;
            }

            case 1: {
                Calendar c = Calendar.getInstance();
                c.add(Calendar.MONTH, -2);

                Intent intent = new Intent(this, TransactionHistoryV2Activity.class);
                intent.putExtra("min_date", DateTime.Companion.convertToString(c));
                intent.putExtra("max_date", DateTime.Companion.convertToString(Calendar.getInstance()));
                intent.putExtra("transaction_type", TransactionHistoryV2Activity.THREE_MONTH_STATEMENT);
                if (bundle != null) {
                    intent.putExtras(bundle);
                }
                startActivity(intent);
                break;
            }

            case 2: {
                Calendar c = Calendar.getInstance();
                c.add(Calendar.MONTH, -5);

                Intent intent = new Intent(this, TransactionHistoryV2Activity.class);
                intent.putExtra("min_date", DateTime.Companion.convertToString(c));
                intent.putExtra("max_date", DateTime.Companion.convertToString(Calendar.getInstance()));
                intent.putExtra("transaction_type", TransactionHistoryV2Activity.SIX_MONTH_STATEMENT);
                if (bundle != null) {
                    intent.putExtras(bundle);
                }
                startActivity(intent);
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

        findViewById(R.id.toolbar_help).setOnClickListener(v -> startHelpActivity("cardServicesHelp"));
    }
}
