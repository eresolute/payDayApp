package com.fh.payday.views2.auth.forgotcredentials.loancustomer

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.fh.payday.utilities.OnOTPConfirmListener
import com.fh.payday.views.fragments.OTPFragment

class SwipeAdapter(
        fragmentManager: FragmentManager,
        private val otpTitle: String,
        private val otpButton: String,
        private val fragmentType: String,
        private val confirmListener: OnOTPConfirmListener

) : FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment {

        return when (position) {
            0 -> {
                val bundle = Bundle()
                bundle.putString("fragmentType", fragmentType)
                val mFragment = LoanAccountFragment()
                mFragment.arguments = bundle
                mFragment
            }
            1 -> {
                if (fragmentType == "forgotUserID") {

                    OTPFragment.Builder(confirmListener)
                            .setPinLength(6)
                            .setHasCardView(false)
                            .setTitle(otpTitle)
                            .setButtonTitle(otpButton)
                            .build()
                } else {
                    PasswordResetFragment()
                }
            }
           /* 2 -> PasswordResetFragment()*/
            2 -> OTPFragment.Builder(confirmListener)
                    .setPinLength(6)
                    .setHasCardView(false)
                    .setTitle(otpTitle)
                    .setButtonTitle(otpButton)
                    .build()
            else -> throw IllegalStateException("Invalid page position")
        }
    }

    override fun getCount(): Int {
        return 3
    }

}