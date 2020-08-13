package com.fh.payday.views2.settings;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import com.fh.payday.BaseActivity;
import com.fh.payday.R;
import com.fh.payday.preferences.UserPreferences;
import com.fh.payday.viewmodels.ChangePasswordViewModel;
import com.fh.payday.viewmodels.PinResetViewModel;
import com.fh.payday.viewmodels.SettingOptionViewModel;
import com.fh.payday.views2.dashboard.MainActivity;
import com.fh.payday.views2.help.HelpActivity;
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity;
import com.fh.payday.views2.locator.LocatorActivity;
import com.fh.payday.views2.message.ContactUsActivity;
import com.fh.payday.views2.settings.update_card_pin.EditCardPinFragment;
import com.fh.payday.views2.settings.update_link_account.LinkAccountFragment;
import com.fh.payday.views2.settings.update_mobile_number.UpdateMobileFragment;
import com.fh.payday.views2.settings.update_password.EditPasswordFragment;

public class SettingOptionActivity extends BaseActivity {

    TextView toolbarTitle;
    SettingOptionViewModel viewModel;
    PinResetViewModel pinResetViewModel;
    private ChangePasswordViewModel changePasswordViewModel;
    private TextView textView;
    private int index;
    private boolean isExpired;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(SettingOptionViewModel.class);
        pinResetViewModel = ViewModelProviders.of(this).get(PinResetViewModel.class);
        changePasswordViewModel = ViewModelProviders.of(this).get(ChangePasswordViewModel.class);
        changePasswordViewModel.setUser(UserPreferences.Companion.getInstance().getUser(this));

        index = getIntent().getIntExtra("index", 0);
        isExpired = getIntent().getBooleanExtra("isExpired", false);

        setFragment(index);
        handleBottomBar();
    }

    public PinResetViewModel getPinResetViewModel() {
        return pinResetViewModel;
    }

    public ChangePasswordViewModel getChangePasswordViewModel() {
        return changePasswordViewModel;
    }

    @Override
    public int getLayout() {
        return R.layout.activity_setting_option;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //viewModel.setSelected(0);
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

    private void setFragment(int index) {
        viewModel.setSelected(0);
        switch (index) {
            case 0:
                setToolbarTitle(getString(R.string.change_password));
                showFragment(new EditPasswordFragment());
                break;
            case 1:
                setToolbarTitle(getString(R.string.set_pin));
                EditCardPinFragment fragment1 = new EditCardPinFragment();
                Bundle bundle1 = new Bundle();
                bundle1.putInt("index", 1);
                fragment1.setArguments(bundle1);
                showFragment(fragment1);
                break;
            case 12:
                textView.setVisibility(View.VISIBLE);
                setToolbarTitle(getString(R.string.service_requests));
                EditCardPinFragment fragment = new EditCardPinFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("index", 12);
                fragment.setArguments(bundle);
                showFragment(fragment);
                break;
            case 2:
                setToolbarTitle(getString(R.string.link_accounts));
                showFragment(new LinkAccountFragment());
                break;
            case 3:
                setToolbarTitle(getString(R.string.upload_mobile_number));
                showFragment(new UpdateMobileFragment());
                break;
            case 4:
                break;
            case 5:
                break;
        }
    }

    @Override
    public void init() {
        toolbarTitle = findViewById(R.id.toolbar_title);
        textView = findViewById(R.id.tv_title);

        findViewById(R.id.toolbar_back).setOnClickListener(v -> onBackPressed());
    }

    public void setToolbarTitle(String title) {
        toolbarTitle.setText(title);
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
    public void onBackPressed() {
        if (Integer.valueOf(0).equals(viewModel.getSelected().getValue())) {
            if (index == 0 && isExpired) {
                logout();
            } else {
                super.onBackPressed();
            }
        } else {
            if (viewModel.getSelected().getValue() == null) super.onBackPressed();
            viewModel.setSelected(viewModel.getSelected().getValue() - 1);
        }
    }
}
