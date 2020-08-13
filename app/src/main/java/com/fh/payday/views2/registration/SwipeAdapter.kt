package com.fh.payday.views2.registration

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import android.util.SparseArray
import android.view.ViewGroup
import com.fh.payday.utilities.OnOTPConfirmListener
import com.fh.payday.views.fragments.OTPFragment
import com.fh.payday.views2.settings.fingerprint.FingerPrintFragment


class SwipeAdapter(
    fm: FragmentManager,
    private var items: ArrayList<Fragment>
) : FragmentStatePagerAdapter(fm) {

    var fragments = SparseArray<Fragment>()

    private val indexes = ArrayList<Int>().apply {
        items.forEachIndexed { index, _ ->
            add(index)
        }
    }

    override fun getItem(position: Int) = items[indexes[position]]

    override fun getCount() = indexes.size

    override fun getItemPosition(`object`: Any) = PagerAdapter.POSITION_NONE

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position) as Fragment
        fragments.put(position, fragment)
        return fragment
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        fragments.remove(position)
        super.destroyItem(container, position, `object`)
    }

    fun getFragment(position: Int): Fragment? = fragments.get(position)

    fun deletePage(index: Int) {
        if (canDelete(index)) {
            indexes.removeAt(index)
            notifyDataSetChanged()
        }
    }

    fun reset() {
        if(count < items.size) {
            indexes.apply {
                clear()
                items.forEachIndexed { index, _ ->
                    add(index)
                }
            }
            notifyDataSetChanged()
        }
    }

    private fun canDelete(index: Int) = indexes.size > 0 && indexes.size > index

}

fun defaultItems(
    otpConfirmListener: OnOTPConfirmListener,
    resendOtpListener: OTPFragment.OnResendOtpListener,
    otpTitle: String,
    otpInstructions: String = ""
) = getItems(otpConfirmListener, resendOtpListener, otpTitle, otpInstructions)


private fun getOtpFragment(
    otpConfirmListener: OnOTPConfirmListener,
    resendOtpListener: OTPFragment.OnResendOtpListener,
    otpTitle: String,
    otpInstructions: String
): Fragment {
    return OTPFragment.Builder(otpConfirmListener)
        .setPinLength(6)
        .setTitle(otpTitle)
        .setInstructions(otpInstructions)
        .setOnResendOtpListener(resendOtpListener)
        .build()
}

/*private fun getTermsCondFragment(
    termCondListener: TermsConditionsFragment.OnAcceptanceListener,
    title: String,
    instructions: String
): Fragment {
    return TermsConditionsFragment.Builder()
        .setTitle(title)
        .setInstructions(instructions)
        .attachListener(termCondListener)
        .build()
}*/

private fun getItems(
    otpConfirmListener: OnOTPConfirmListener,
    resendOtpListener: OTPFragment.OnResendOtpListener,
    otpTitle: String,
    otpInstructions: String
) = arrayListOf(
    ScanEmiratesFragment(),
    EmiratesIdDetailsFragment(),
    RegisterCardFragment(),
    RegisterCardDetailsFragment(),
    CardPinFragment(),
    MobileNumberFragment(),
    getOtpFragment(otpConfirmListener, resendOtpListener, otpTitle, otpInstructions),
    RegisterLoginDetailsFragment(),
    FingerPrintFragment()
)