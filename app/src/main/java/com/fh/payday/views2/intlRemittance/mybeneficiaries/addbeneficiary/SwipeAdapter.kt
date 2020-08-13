package com.fh.payday.views2.intlRemittance.mybeneficiaries.addbeneficiary

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.fh.payday.BaseFragment

class SwipeAdapter(
        fm: FragmentManager,
        private var items: ArrayList<Fragment>
) : FragmentStatePagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return items[position]
    }

    override fun getCount(): Int {
        return items.size
    }

}