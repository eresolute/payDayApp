package com.fh.payday.views2.payments.internationaltopup

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.fh.payday.utilities.OnOTPConfirmListener
import com.fh.payday.views.fragments.OTPFragment

class SwipeAdapter(
        fm: FragmentManager,
        private val otpTitle: String,
        private val otpButton: String,
        private val confirmListener: OnOTPConfirmListener
) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> MobileNoFragment()
            1 -> PaymentAmountFragment()
            2 -> PaymentDetailsFragment()
            3 -> OTPFragment.Builder(confirmListener)
                    .setPinLength(6)
                    .setHasCardView(false)
                    .setTitle(otpTitle)
                    .setButtonTitle(otpButton)
                    .build()
            else -> throw IllegalStateException("Invalid page position")
        }
    }

    override fun getCount(): Int {
        return 4
    }
}
