package com.fh.payday.views2.payments.recharge.mawaqif

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.fh.payday.utilities.OnOTPConfirmListener
import com.fh.payday.views.fragments.OTPFragment
import com.fh.payday.views2.payments.recharge.mawaqif.fragments.MawaqifAmountFragment
import com.fh.payday.views2.payments.recharge.mawaqif.fragments.MawaqifMobileFragment
import com.fh.payday.views2.payments.recharge.mawaqif.fragments.MawaqifPaymentDetailsFragment

class SwipeAdapter(
        fm: FragmentManager,
        private var items: ArrayList<Fragment>
) : FragmentStatePagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
            return  items[position]
    }

    override fun getCount(): Int {
        return items.size
    }
}
fun getItems(
        otpTitle: String,
        otpButton: String,
        confirmListener: OnOTPConfirmListener,
        otpInstructions: String
) = arrayListOf(
        MawaqifMobileFragment(),
        MawaqifAmountFragment(),
        MawaqifPaymentDetailsFragment(),
        OTPFragment.Builder(confirmListener)
                .setPinLength(6)
                .setHasCardView(false)
                .setTitle(otpTitle)
                .setInstructions(otpInstructions)
                .setButtonTitle(otpButton)
                .build()
)
