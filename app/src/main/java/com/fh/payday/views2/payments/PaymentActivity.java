package com.fh.payday.views2.payments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

import com.fh.payday.BaseActivity;
import com.fh.payday.R;
import com.fh.payday.views2.dashboard.MainActivity;
import com.fh.payday.views2.help.HelpActivity;
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity;
import com.fh.payday.views2.locator.LocatorActivity;
import com.fh.payday.views2.message.ContactUsActivity;
import com.fh.payday.views2.payments.countries.CountryActivity;

public class PaymentActivity extends BaseActivity {

    private TextView mToolbarTitle;
    private int index;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        index = getIntent().getIntExtra("index", 11);

        if (index != 11) {
            inflateFragment(index);
        } else {
            mToolbarTitle.setText(R.string.payments);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, new PaymentsFragment(), "Payment Fragment")
                    .commit();
        }

        handleBottomBar();
    }

    @Override
    public int getLayout() {
        return R.layout.activity_payment;
    }

    @Override
    public void init() {
        mToolbarTitle = findViewById(R.id.toolbar_title);
        findViewById(R.id.toolbar_back).setOnClickListener(view -> onBackPressed());
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

        findViewById(R.id.toolbar_help).setOnClickListener(v -> startHelpActivity("paymentScreenHelp"));
    }

    @Override
    public void onBackPressed() {
        if (index == 3) {
            finish();
            return;
        }

        super.onBackPressed();
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            mToolbarTitle.setText(R.string.payments);
        }
    }

    public void onServiceSelected(int position) {
        switch (position) {
            case 0:
                mToolbarTitle.setText(R.string.mobile_service_provider);
                inflateFragment(0);
                break;
            case 1:
                mToolbarTitle.setText(R.string.utilities);
                inflateFragment(1);
                break;
            case 2:
                // mToolbarTitle.setText(R.string.international_topup);
                inflateFragment(2);
                break;
            case 3:
                mToolbarTitle.setText(R.string.indian_bill_payment);
                inflateFragment(3);
                break;
            case 4:
                mToolbarTitle.setText(R.string.road_transport);
                inflateFragment(4);
                break;
            default:
                mToolbarTitle.setText(R.string.payments);
                break;
        }
    }

    private void inflateFragment(int position) {
        if (position == 3)
            mToolbarTitle.setText(R.string.indian_bill_payment);
        if (position == 2) {
            startActivity(new Intent(PaymentActivity.this, CountryActivity.class));
        } else if (position != 5) {

            if (position == 0)
                findViewById(R.id.toolbar_help).setOnClickListener(v -> {
                    Intent intent = new Intent(v.getContext(), HelpActivity.class);
                    intent.putExtra("anchor", "mobileRechargeHelp");
                    startActivity(intent);
                });

            Bundle bundle = new Bundle();
            bundle.putInt("position", position);
            PaymentsFragment fragment = new PaymentsFragment();
            fragment.setArguments(bundle);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }



}
