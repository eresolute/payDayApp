package com.fh.payday.views2.moneytransfer.beneificaries;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.fh.payday.BaseActivity;
import com.fh.payday.R;
import com.fh.payday.datasource.models.moneytransfer.Beneficiary;
import com.fh.payday.viewmodels.TransferViewModel;
import com.fh.payday.viewmodels.beneficiaries.BeneficiaryViewModel;
import com.fh.payday.views.fragments.AlertDialogFragment;
import com.fh.payday.views.fragments.OTPFragment;
import com.fh.payday.views2.dashboard.MainActivity;
import com.fh.payday.views2.help.HelpActivity;
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity;
import com.fh.payday.views2.locator.LocatorActivity;
import com.fh.payday.views2.message.ContactUsActivity;
import com.fh.payday.views2.moneytransfer.MoneyTransferType;
import com.fh.payday.views2.moneytransfer.beneificaries.payday.toPayday.PaydayToPaydayBeneficiaryFragment;

import java.util.List;

public class EditBeneficiaryActivity extends BaseActivity implements AlertDialogFragment.OnConfirmListener {

    TextView toolbarTitle;
    BeneficiaryViewModel viewModel;

    public enum OPTION {EDIT, ADD, DELETE, ENABLE}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        viewModel = ViewModelProviders.of(this).get(BeneficiaryViewModel.class);

        if (getIntent().getSerializableExtra("label") == MoneyTransferType.PAYDAY) {
            viewModel.setBeneficiaryOption(OPTION.ADD);
            callPayday(getIntent().getSerializableExtra("label").toString());
            return;
        }

        if (getIntent().getSerializableExtra("index") == OPTION.EDIT) {
            viewModel.setBeneficiaryOption(OPTION.EDIT);
            showFragment(EditBeneficiaryFragment.newInstance());
        } else {
            viewModel.setBeneficiaryOption(OPTION.ADD);
            showFragment(new AddBeneficiaryFragment());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void callPayday(String label) {
        if (label.equals(MoneyTransferType.PAYDAY.toString())) {
            showFragment(PaydayToPaydayBeneficiaryFragment.newInstance());
        } else {
            replaceFragment(PaydayToPaydayBeneficiaryFragment.newInstance());
        }
    }

    public void setToolbarTitle(String title) {
        toolbarTitle.setText(title);
    }

    @Override
    public int getLayout() {
        return R.layout.activity_edit_beneficiary;
    }

    @Override
    public void init() {
        toolbarTitle = findViewById(R.id.toolbar_title);
        findViewById(R.id.toolbar_back).setOnClickListener(v -> onBackPressed());

        handleBottomBar();
    }

    public BeneficiaryViewModel getViewModel() {
        return viewModel;
    }

    public TransferViewModel getTransferViewModel() {
        return ViewModelProviders.of(this).get(TransferViewModel.class);
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
    public void onConfirm(Dialog dialog) {
        finish();
    }

    @Override
    public void onBackPressed() {
        if (!tellFragments())
            finish();
    }

    private boolean tellFragments() {
        boolean flag = false;
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment f : fragments) {
            Log.e("fragment", "is" + f);
            if (f instanceof PaydayToPaydayBeneficiaryFragment) {
                ((PaydayToPaydayBeneficiaryFragment) f).backPress();
                flag = true;
                break;
            } else if (f instanceof OTPFragment && (getViewModel().getBeneficiaryOption() == OPTION.DELETE || getViewModel().getBeneficiaryOption() == OPTION.ENABLE)) {
                replaceFragment(EditBeneficiaryFragment.newInstance());
                flag = true;
                break;
            }
        }
        return flag;
    }

    public interface OnBeneficiaryClick {
        void onBeneficiaryClickListener(int position, Beneficiary beneficiary);

        void onBeneficiaryClickListener(int position, Beneficiary beneficiary, boolean enabled);
    }

    public interface OnBeneficiaryOptionClick<T> {
        void onOptionClick(T t, View view);
    }
}
