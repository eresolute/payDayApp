package com.fh.payday.views2.bottombar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class BottomBarPagerAdapter extends FragmentStatePagerAdapter {
    public BottomBarPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {

//        switch (i) {
//            case 0:
//                return new CashOutFragment();
//            case 1:
//                return new CurrencyConverterFragment();
//            case 2:
//                return new FaqFragment();
//            case 3:
//                return new HowToFragment();
//            default:
//                throw new IllegalStateException("Invalid Fragment");
//        }

        switch (i) {
//            case 0:
//                return new FaqFragment();
            case 0:
                return new HowToFragment();
            default:
                throw new IllegalStateException("Invalid Fragment");
        }

    }

    @Override
    public int getCount() {
        return 1;
    }
}
