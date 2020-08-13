package com.fh.payday.views2.payments.ibp

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import android.util.SparseArray
import android.view.ViewGroup
import com.fh.payday.utilities.OnOTPConfirmListener
import com.fh.payday.views.fragments.OTPFragment

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

fun getItems(
    otpTitle: String,
    otpButton: String,
    confirmListener: OnOTPConfirmListener,
    otpInstructions: String
) = arrayListOf(
    AccountNoFragment(),
    PaymentAmountFragment(),
    PaymentDetailsFragment(),
    OTPFragment.Builder(confirmListener)
        .setPinLength(6)
        .setHasCardView(false)
        .setTitle(otpTitle)
        .setInstructions(otpInstructions)
        .setButtonTitle(otpButton)
        .build()
)

fun getFixedItems(
        otpTitle: String,
        otpButton: String,
        confirmListener: OnOTPConfirmListener,
        otpInstructions: String
) = arrayListOf(
        AccountNoFragment(),
        SelectStateFragment(),
        PaymentAmountFragment(),
        PaymentDetailsFragment(),
        OTPFragment.Builder(confirmListener)
                .setPinLength(6)
                .setHasCardView(false)
                .setTitle(otpTitle)
                .setInstructions(otpInstructions)
                .setButtonTitle(otpButton)
                .build()
)

fun getLandlineItems(
    otpTitle: String,
    otpButton: String,
    confirmListener: OnOTPConfirmListener,
    otpInstructions: String
) = getItems(otpTitle, otpButton, confirmListener, otpInstructions).apply {
    add(0, AccountNoFragment().apply {
        val bundle = Bundle()
        bundle.putInt("account_type", AccountNoFragment.STD_CODE)
        arguments = bundle
    })
}

fun getLandlineItemsWithAccountNo(
    otpTitle: String,
    otpButton: String,
    confirmListener: OnOTPConfirmListener,
    otpInstructions: String
) = getLandlineItems(otpTitle, otpButton, confirmListener,otpInstructions).apply {
    add(1, AccountNoFragment().apply {
        val bundle = Bundle()
        bundle.putInt("account_type", AccountNoFragment.BSNL_ACCOUNT)
        arguments = bundle
    })
}

fun getInsuranceItems(
    otpTitle: String,
    otpButton: String,
    confirmListener: OnOTPConfirmListener,
    otpInstructions: String
) = getItems(otpTitle, otpButton, confirmListener, otpInstructions).apply {
    add(1, SelectDobFragment())
}


