package com.fh.payday.views2.intlRemittance.transfer.fragments

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.fh.payday.BaseFragment
import com.fh.payday.R
import com.fh.payday.datasource.DataGenerator
import com.fh.payday.views2.intlRemittance.transfer.TransferActivity
import kotlinx.android.synthetic.main.activity_my_beneficiaries.*
import kotlinx.android.synthetic.main.fragment_intl_beneficiary_fragment.*
import kotlinx.android.synthetic.main.layout_payment_option.*

class IntlBeneficiaryFragment : BaseFragment() {

    enum class PAYMENTOPTION { BT, CP }
    private  var type :Int = 0

    private var transferActivity: TransferActivity? = null

//    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
//        super.setUserVisibleHint(isVisibleToUser)
////       val item = if (isVisibleToUser) 0 else 1
//        if (view_pager != null) {
//            setupTabs(view_pager)
//            setInitialTab()
//        }
//    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_intl_beneficiary_fragment, container, false)
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {

        super.onViewCreated(v, savedInstanceState)

//    }
//
        tv_bank_transfer.setOnClickListener {
            startBankActivity()
////
        }
        tv_cash_payout.setOnClickListener {
            startCashPayoutActivity()
        }


   // tabs.setupWithViewPager(view_pager)
    setupTabs(view_pager)
    setInitialTab()



//    tabs.addOnTabSelectedListener(
//    object : TabLayout.OnTabSelectedListener {
//        override fun onTabReselected(tab: TabLayout.Tab?) {
//            tab ?: return
//            val view = tab.customView
//            view ?: return
//
//            val textView = view.findViewById<TextView>(R.id.textView)
//            textView.findViewById<TextView>(R.id.textView).setTextColor(ContextCompat.getColor(textView.context, R.color.white))
//        }
//
//        override fun onTabUnselected(tab: TabLayout.Tab?) {
//            tab ?: return
//            val view = tab.customView
//            view ?: return
//
//            val textView = view.findViewById<TextView>(R.id.textView)
//            //   textView.setTextColor(ContextCompat.getColor(activity, R.color.black))
//        }
//
//        override fun onTabSelected(tab: TabLayout.Tab?) {
//            tab ?: return
//            val view = tab.customView
//            view ?: return
//
//            val textView = view.findViewById<TextView>(R.id.textView)
//            textView.findViewById<TextView>(R.id.textView).setTextColor(ContextCompat.getColor(textView.context, R.color.white))
//        }
//    })
}

    private fun startBankActivity() {
        type = 0
        tv_cash_payout.background = ContextCompat.getDrawable(context
                ?: return, R.drawable.bg_rounded_blue_border_16dp)
        tv_bank_transfer.background = ContextCompat.getDrawable(context
                ?: return, R.drawable.bg_blue)
        transferActivity?.navigateUp()
    }

    private fun startCashPayoutActivity() {
        type = 1
        tv_bank_transfer.background = ContextCompat.getDrawable(context
                ?: return, R.drawable.bg_rounded_blue_border_16dp)
        tv_cash_payout.background = ContextCompat.getDrawable(context
                ?: return, R.drawable.bg_blue)
        getCpBeneficiaryList()
    }

    private fun getCpBeneficiaryList() {
        tv_no_beneficiary.visibility = View.VISIBLE
        recycler_view.visibility = View.GONE
    }

    @SuppressLint("InflateParams")
    private fun setupTabs(viewPager: ViewPager) {
        val itemList = DataGenerator.getIntlTransferTabs(activity)
        val adapter = ViewPagerAdapter(childFragmentManager)

        for ((type) in itemList.withIndex()) {
            val fragment = BeneficiaryFragment()
            val bundle = Bundle()
            bundle.putInt("type", type)
            bundle.putString("action", (activity as TransferActivity).viewmodel.from)
            fragment.arguments = bundle
            adapter.addFrag(fragment, itemList[type])
        }
        viewPager.adapter = adapter

        for ((index) in itemList.withIndex()) {
            val tabItem = LayoutInflater.from(activity).inflate(R.layout.custom_tab_intl_beneficiary, null) as TextView
            tabItem.text = itemList[index]
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                tabItem.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_bank_white, 0, 0, 0)
            } else {
                tabItem.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_bank_white, 0, 0, 0)
            }
            tabItem.setTextColor(ContextCompat.getColor(tabItem.context, R.color.textDarkColor))
         //   tabs.getTabAt(index)?.customView = tabItem
        }

    }

    private fun setInitialTab() {
//        val view = tabs.getTabAt(0)?.customView ?: return
//        val activity = activity as TransferActivity
//
//        val textView = view.findViewById<TextView>(R.id.textView)
//        textView.setTextColor(ContextCompat.getColor(activity, R.color.white))
//        for (i in 1..tabs.tabCount) {
//            val mView = tabs.getTabAt(i)?.customView
//
//            mView ?: return
//
//            val mTextView = mView.findViewById<TextView>(R.id.textView)
//            mTextView.setTextColor(ContextCompat.getColor(activity, R.color.black))
//        }
    }
//
    class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {
        private val fragmentList = ArrayList<Fragment>()
        private val fragmentTitleList = ArrayList<String>()

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getCount(): Int {
            return fragmentList.size
        }

        fun addFrag(fragment: Fragment, title: String) {
            fragmentList.add(fragment)
            fragmentTitleList.add(title)
            notifyDataSetChanged()
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return fragmentTitleList[position]
        }
    }
}