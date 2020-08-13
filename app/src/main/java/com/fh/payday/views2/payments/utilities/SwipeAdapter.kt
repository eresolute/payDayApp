package com.fh.payday.views2.payments.utilities

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.fh.payday.datasource.models.payments.UtilityServiceType
import com.fh.payday.utilities.OnOTPConfirmListener
import com.fh.payday.views.fragments.OTPFragment

class SwipeAdapter(
        fm: FragmentManager,
        private val items: List<Fragment>
) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int) = items[position]

    override fun getCount() = items.size
}

fun getFewaItems(
        otpTitle: String,
        otpButton: String,
        otpInstructions :String,
        confirmListener: OnOTPConfirmListener
) = arrayListOf(
        UtilitiesAccountFragment().apply {
            val bundle = Bundle()
            bundle.putString("operator", UtilityServiceType.FEWA)
            arguments = bundle
        },
//        UtilitiesDetailsFragment(),
        UtilitiesAmountFragment(),
        UtilitiesSummaryFragment(),
        OTPFragment.Builder(confirmListener)
                .setPinLength(6)
                .setHasCardView(false)
                .setTitle(otpTitle)
                .setButtonTitle(otpButton)
                .setInstructions(otpInstructions)
                .build()
)

fun getAadcItems(
        otpTitle: String,
        otpButton: String,
        otpInstructions: String,

        confirmListener: OnOTPConfirmListener
) = arrayListOf(
        UtilitiesServiceTypeFragment(),
        UtilitiesAccountFragment().apply {
            val bundle = Bundle()
            bundle.putString("operator", UtilityServiceType.AADC)
            arguments = bundle
        },
        UtilitiesAmountFragment(),
        UtilitiesSummaryFragment(),
        OTPFragment.Builder(confirmListener)
                .setPinLength(6)
                .setHasCardView(false)
                .setInstructions(otpInstructions)
                .setTitle(otpTitle)
                .setButtonTitle(otpButton)
                .build()
)



