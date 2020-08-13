package com.fh.payday.views2.settings;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

import com.fh.payday.BaseActivity;
import com.fh.payday.R;
import com.fh.payday.datasource.DataGenerator;
import com.fh.payday.datasource.models.Item;
import com.fh.payday.utilities.OnItemClickListener;
import com.fh.payday.views.shared.IconListAdapter;
import com.fh.payday.views2.dashboard.MainActivity;
import com.fh.payday.views2.help.HelpActivity;
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity;
import com.fh.payday.views2.locator.LocatorActivity;
import com.fh.payday.views2.message.ContactUsActivity;
import com.fh.payday.views2.settings.fingerprint.FingerprintSettings;

import java.util.List;

public class SettingsActivity extends BaseActivity implements OnItemClickListener {

    TextView tvToolbarTitle;
    ImageView imgToolbarBack;

    RecyclerView rvSettingOptions;
    IconListAdapter adapter;
    List<Item> optionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        optionList = DataGenerator.getSettingOptions(this);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            optionList.remove(optionList.size() - 1);
        }
        rvSettingOptions.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvSettingOptions.setAdapter(new IconListAdapter(optionList, this));

        handleBottomBar();
    }


    @Override
    public int getLayout() {
        return R.layout.activity_settings;
    }

    @Override
    public void init() {
        tvToolbarTitle = findViewById(R.id.toolbar_title);
        tvToolbarTitle.setText(getString(R.string.settings));
        rvSettingOptions = findViewById(R.id.rv_settings_options);

        findViewById(R.id.toolbar_back).setOnClickListener(v -> onBackPressed());
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

    @Override
    public void onItemClick(int index) {
        switch (index) {
            case 0: {
                Intent intent = new Intent(this, SettingOptionActivity.class);
                intent.putExtra("index", index);
                startActivity(intent);
                break;
            }
           /* case 1: {
                Intent intent = new Intent(this, SettingOptionActivity.class);
                intent.putExtra("index", index);
                startActivity(intent);
                break;
            }
            case 2: {
                Intent intent = new Intent(this, SettingOptionActivity.class);
                intent.putExtra("index", index);
               // startActivity(intent);
                break;
            }
            case 3: {
                Intent intentMobile = new Intent(this, KycOptionActivity.class);
                intentMobile.putExtra("index", 1);
                startActivity(intentMobile);
                break;
            }
            case 4: {
                Intent intentEmail = new Intent(this, KycOptionActivity.class);
                intentEmail.putExtra("index", 0);
                startActivity(intentEmail);
                break;
            }*/
            case 1: {
                startActivity(new Intent(this, FingerprintSettings.class));
                break;
            }
        }
    }
}
