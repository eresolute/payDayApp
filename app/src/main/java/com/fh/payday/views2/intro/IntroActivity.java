package com.fh.payday.views2.intro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.LayoutRes;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fh.payday.BaseActivity;
import com.fh.payday.R;
import com.fh.payday.preferences.LocalePreferences;
import com.fh.payday.utilities.OnItemClickListener;
import com.fh.payday.utilities.TextViewUtilsKt;
import com.fh.payday.views.adapter.IntroAdapter;
import com.fh.payday.views.adapter.SelectLanguageAdapter;
import com.fh.payday.views2.auth.LoginActivity;
import com.fh.payday.views2.registration.RegisterActivity;
import com.fh.payday.views2.shared.activity.WebViewActivity;

import static com.fh.payday.utilities.UrlUtilsKt.FAQ_URL_EN;
import static com.fh.payday.utilities.UrlUtilsKt.FAQ_URL_HI;

public class IntroActivity extends BaseActivity implements SharedPreferences.OnSharedPreferenceChangeListener, OnItemClickListener {

    private BottomSheetDialog bottomSheet;

    @LayoutRes
    private int[] layouts = {
            R.layout.layout_intro1,
            R.layout.layout_intro2,
            R.layout.layout_intro3
    };

    private ImageView swipe;

    ViewPager.OnPageChangeListener viewPagerListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            switch (position) {
                case 0:
                    swipe.setImageResource(R.drawable.ic_swipe_1);
                    break;
                case 1:
                    swipe.setImageResource(R.drawable.ic_swipe_2);
                    break;
                case 2:
                    swipe.setImageResource(R.drawable.ic_swipe_3);
                    break;
                default:
                    swipe.setImageResource(R.drawable.ic_swipe_1);
                    break;
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);
        checkAppUpdate();
    }

    @Override
    protected void onDestroy() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    public int getLayout() {
        return R.layout.activity_intro;
    }

    @Override
    public void init() {
        ViewPager viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new IntroAdapter(layouts));
        viewPager.addOnPageChangeListener(viewPagerListener);
        swipe = findViewById(R.id.swipe);

        String selectedLang = LocalePreferences.Companion.getInstance().getSelectedLang(this);
        ((TextView) findViewById(R.id.tvSelectedLang)).setText(selectedLang);
        findViewById(R.id.tvSelectedLang).setOnClickListener(view -> showBottomSheet());

        findViewById(R.id.btLogin).setOnClickListener(view -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        findViewById(R.id.btRegister).setOnClickListener(view ->
                startActivity(new Intent(this, RegisterActivity.class)));

        findViewById(R.id.tv_how_to_reg).setOnClickListener(v -> {
            String locale = LocalePreferences.Companion.getInstance().getLocale(this);

            Intent faqIntent = new Intent(this, WebViewActivity.class);
            faqIntent.putExtra("title", getString(R.string.how_to_register));
            if ("en".equals(locale)) faqIntent.putExtra("url", FAQ_URL_EN);
            else if ("hi".equals(locale)) faqIntent.putExtra("url", FAQ_URL_HI);
            else faqIntent.putExtra("url", FAQ_URL_EN);

            startActivity(faqIntent);
        });

        TextViewUtilsKt.setTextWithUnderLine(findViewById(R.id.tv_how_to_reg), getString(R.string.how_to_register));

    }

    private void showBottomSheet() {
        String[] array = getResources().getStringArray(R.array.languages);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_select_language, findViewById(android.R.id.content), false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);

        recyclerView.setAdapter(new SelectLanguageAdapter(this, array));

        bottomSheet = new BottomSheetDialog(this);
        bottomSheet.setContentView(view);
        ((View) view.getParent()).setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));
        bottomSheet.show();

        view.findViewById(R.id.cancel).setOnClickListener(v -> bottomSheet.dismiss());

        bottomSheet.setOnDismissListener(v -> bottomSheet = null);
        bottomSheet.setOnCancelListener(v -> bottomSheet = null);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(LocalePreferences.keySelected)) {
            recreate();
        }
    }

    @Override
    public void onItemClick(int index) {
        switch (index) {
            case 0:
                setLocale("en");
                break;
            case 1:
                setLocale("ar");
                break;
            case 2:
                setLocale("hi");
                break;
            case 3:
                setLocale("ml");
                break;
            case 4:
                setLocale("ur");
                break;
            case 5:
                setLocale("bn");
                break;
            case 6:
                setLocale("ta");
                break;
            case 7:
                setLocale("tl");
                break;
            default:
                break;
        }
        if (bottomSheet != null) bottomSheet.dismiss();
    }
}
