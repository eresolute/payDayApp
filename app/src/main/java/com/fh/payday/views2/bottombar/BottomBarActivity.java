package com.fh.payday.views2.bottombar;

import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;

import com.fh.payday.BaseActivity;
import com.fh.payday.R;
import com.fh.payday.utilities.TextViewUtilsKt;

public class BottomBarActivity extends BaseActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {
    private ViewPager viewPager;
    private TextView mToolbarText;
    private TextView btmMenuCashOut;
    private TextView btmMenuCurrConv;
    private TextView btmMenuFaq;
    private TextView btmMenuHowTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int i = getIntent().getIntExtra("id", 0);
        setSelectedFragment(i);
    }

    private void setSelectedFragment(int i) {
        onPageSelected(i);
        viewPager.setCurrentItem(i);
    }

    @Override
    public int getLayout() {
        return R.layout.activity_bottom_bar;
    }

    @Override
    public void init() {
        mToolbarText = findViewById(R.id.toolbar_title);

        findViewById(R.id.toolbar_help).setVisibility(View.INVISIBLE);
        findViewById(R.id.toolbar_back).setOnClickListener(view -> onBackPressed());

        btmMenuCashOut = findViewById(R.id.btm_menu_cash_out);
        btmMenuCurrConv = findViewById(R.id.btm_menu_currency_conv);
        btmMenuFaq = findViewById(R.id.btm_menu_faq);
        btmMenuHowTo = findViewById(R.id.btm_menu_how_to_reg);

//        btmMenuCashOut.setOnClickListener(this);
//        btmMenuCurrConv.setOnClickListener(this);
//        btmMenuFaq.setOnClickListener(this);
        btmMenuHowTo.setOnClickListener(this);

        viewPager = findViewById(R.id.view_pager);
        PagerAdapter pagerAdapter = new BottomBarPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        viewPager.addOnPageChangeListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setDefaultBackground(btmMenuCashOut, R.drawable.ic_cash_out);
        setDefaultBackground(btmMenuFaq, R.drawable.ic_faq);
        setDefaultBackground(btmMenuCurrConv, R.drawable.ic_currency_converter);
        setDefaultBackground(btmMenuHowTo, R.drawable.ic_how_to_register);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btm_menu_cash_out:
                viewPager.setCurrentItem(0);
                break;

            case R.id.btm_menu_currency_conv:
                viewPager.setCurrentItem(1);
                break;

            case R.id.btm_menu_faq:
                viewPager.setCurrentItem(2);
                break;

            case R.id.btm_menu_how_to_reg:
                viewPager.setCurrentItem(1);
                break;

        }
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int position) {
        switch (position) {
//            case 0:
//                mToolbarText.setText(R.string.title_cash_out_fragment);
//                setSelectedBackground(btmMenuCashOut, R.drawable.ic_cash_out);
//                setDefaultBackground(btmMenuCurrConv, R.drawable.ic_currency_converter);
//                setDefaultBackground(btmMenuFaq, R.drawable.ic_faq);
//                setDefaultBackground(btmMenuHowTo, R.drawable.ic_how_to_register);
//                break;
//            case 1:
//                mToolbarText.setText(R.string.title_currency_converter_fragment);
//                setDefaultBackground(btmMenuCashOut, R.drawable.ic_cash_out);
//                setSelectedBackground(btmMenuCurrConv, R.drawable.ic_currency_converter);
//                setDefaultBackground(btmMenuFaq, R.drawable.ic_faq);
//                setDefaultBackground(btmMenuHowTo, R.drawable.ic_how_to_register);
//                break;
//            case 0:
//                mToolbarText.setText(R.string.faq);
//                setDefaultBackground(btmMenuCashOut, R.drawable.ic_cash_out);
//                setDefaultBackground(btmMenuCurrConv, R.drawable.ic_currency_converter);
//                setSelectedBackground(btmMenuFaq, R.drawable.ic_faq);
//                setDefaultBackground(btmMenuHowTo, R.drawable.ic_how_to_register);
//                break;
            case 0:
                mToolbarText.setText(R.string.how_to_register);
                setDefaultBackground(btmMenuCashOut, R.drawable.ic_cash_out);
                setDefaultBackground(btmMenuFaq, R.drawable.ic_faq);
                setDefaultBackground(btmMenuCurrConv, R.drawable.ic_currency_converter);
                setSelectedBackground(btmMenuHowTo, R.drawable.ic_how_to_register);
                break;
            default:
                mToolbarText.setText(R.string.title_how_to_fragment);
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    private void setSelectedBackground(TextView textView, @DrawableRes int drawableRes) {
        TextViewUtilsKt.setDrawableTopTint(textView, drawableRes, R.color.color_red);
    }

    private void setDefaultBackground(TextView textView, @DrawableRes int drawableRes) {
        TextViewUtilsKt.setDrawableTopTint(textView, drawableRes, R.color.white);
    }

}
