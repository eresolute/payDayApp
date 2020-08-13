package com.fh.payday.views2.intlRemittance.transactionhistory

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.DataGenerator
import com.fh.payday.datasource.models.intlRemittance.IntlTransaction
import com.fh.payday.datasource.models.transactionhistory.TransactionDate
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.DateTime
import com.fh.payday.viewmodels.intlRemittance.TransactionHistoryViewModel
import com.fh.payday.views2.shared.fragments.DatePickerFragment
import kotlinx.android.synthetic.main.activity_intl_transaction_history.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*

class IntlTransactionHistoryActivity : BaseActivity() {

    lateinit var viewmodel: TransactionHistoryViewModel

    override fun getLayout(): Int = R.layout.activity_intl_transaction_history
    override fun init() {
        viewmodel = ViewModelProviders.of(this).get(TransactionHistoryViewModel::class.java)
        viewmodel.card =  intent.getParcelableExtra("card") ?: return
        tab_layout.visibility = View.VISIBLE

        toolbar_title.setText(R.string.transaction_history)
        toolbar_back.setOnClickListener(this)

        handleBottomBar()
        setInitialDate()

        tv_from_date.setOnClickListener { showDatePicker(it) }
        tv_to_date.setOnClickListener { showDatePicker(it) }

        tab_layout.setupWithViewPager(view_pager)
        setupTabs(view_pager)


        tab_layout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                tab ?: return
                val view = tab.customView
                view ?: return
                val textView = view.findViewById<TextView>(R.id.textView)
                textView.findViewById<TextView>(R.id.textView).setTextColor(ContextCompat.getColor(textView.context, R.color.white))
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab ?: return
                val view = tab.customView
                view ?: return
                val textView = view.findViewById<TextView>(R.id.textView)
                textView.setTextColor(ContextCompat.getColor(textView.context, R.color.black))
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab ?: return
                val view = tab.customView
                view ?: return

                val textView = view.findViewById<TextView>(R.id.textView)
                textView.findViewById<TextView>(R.id.textView).setTextColor(ContextCompat.getColor(textView.context, R.color.white))
            }
        })
        setInitialTab()

        viewmodel.transactionDate.observe(this, android.arch.lifecycle.Observer {
            it ?: return@Observer
            val user = UserPreferences.instance.getUser(this) ?: return@Observer

            tv_from_date.text = DateTime.parse(it.fromDate, outputFormat = "dd MMM yyyy")
            tv_to_date.text = DateTime.parse(it.toDate, outputFormat = "dd MMM yyyy")

            viewmodel.getTransactions(user.token, user.sessionId, user.refreshToken, user.customerId.toString(), it.fromDate, it.toDate)

        })
    }
    override fun onResume() {
        super.onResume()
        viewmodel.card = intent.getParcelableExtra("card") ?: return ?: viewmodel.card ?: return
    }

    private fun setInitialDate() {
        viewmodel.transactionDate.value = TransactionDate(DateTime.currentDayOfLastSixMonths("yyyy-MM-dd"), DateTime.currentDate("yyyy-MM-dd"))
    }

    @SuppressLint("InflateParams")
    private fun setupTabs(viewPager: ViewPager) {
        val itemList = DataGenerator.getIntlTransactionTabs(this)
        val adapter = ViewPagerAdapter(supportFragmentManager)

        for ((index) in itemList.withIndex()) {
            val fragment = TransactionHistoryFragment()
            val bundle = Bundle()
            bundle.putInt("index", index)
            bundle.putParcelable("card", viewmodel.card ?: return)
            fragment.arguments = bundle
            adapter.addFrag(fragment, itemList[index])
        }
        viewPager.adapter = adapter

        for ((index) in itemList.withIndex()) {
            val tabItem = LayoutInflater.from(this).inflate(R.layout.custom_tab_intl_beneficiary, null) as TextView
            tabItem.text = itemList[index]
            tabItem.setTextColor(ContextCompat.getColor(tabItem.context, R.color.textDarkColor))
            tab_layout.getTabAt(index)?.customView = tabItem
        }

    }

    private fun setInitialTab() {
        val view = tab_layout.getTabAt(0)?.customView

        view ?: return

        val textView = view.findViewById<TextView>(R.id.textView)
        textView.setTextColor(ContextCompat.getColor(this, R.color.white))

        //val drawable = textView.compoundDrawables
        //drawable[0].colorFilter = PorterDuffColorFilter(ContextCompat.getColor(textView.context, R.color.white), PorterDuff.Mode.SRC_IN)
    }

    private fun showDatePicker(view: View) {
        val maxDate = Calendar.getInstance()
        val minDate = Calendar.getInstance().apply { add(Calendar.YEAR, -1) }

        val dateTime = when (view.id) {
            tv_to_date.id -> DateTime.parseDate(viewmodel.transactionDate.value?.toDate)
            else -> DateTime.parseDate(viewmodel.transactionDate.value?.fromDate)
        }

        val day = dateTime.dayOfMonth
        val month = dateTime.monthOfYear - 1
        val year = dateTime.year

        DatePickerFragment.Builder()
                .setDay(day)
                .setMonth(month)
                .setYear(year)
                .setMinDate(minDate.time)
                .setMaxDate(maxDate.time)
                .attachDateSetListener(DatePickerDialog.OnDateSetListener { _, y, m, dayOfMonth ->
                    val d = String.format(
                            getString(R.string.date_format_hypen),
                            y.toString(),
                            String.format("%02d", (m + 1)),
                            String.format("%02d", dayOfMonth)
                    ).replace("\\s+".toRegex(), "")

                    val (fromDate, toDate) = viewmodel.transactionDate.value
                            ?: return@OnDateSetListener
                    when (view.id) {
                        tv_from_date.id -> {
                            if (DateTime.isValidDateRange(d, toDate))
                                viewmodel.setDate(d, toDate)
                            else
                                onFailure(findViewById(R.id.root_view), getString(R.string.invalid_date))
                        }
                        tv_to_date.id -> {
                            if (DateTime.isValidDateRange(fromDate, d))
                                viewmodel.setDate(fromDate, d)
                            else
                                onFailure(findViewById(R.id.root_view), getString(R.string.invalid_date))
                        }
                    }
                })
                .build()
                .show(supportFragmentManager, "datePicker")
    }

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
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return fragmentTitleList[position]
        }
    }

    private fun handleBottomBar() {
        findViewById<TextView>(R.id.btm_home).setOnClickListener (this)
        findViewById<TextView>(R.id.btm_menu_branch).setOnClickListener (this)
        findViewById<TextView>(R.id.btm_menu_support).setOnClickListener (this)
        findViewById<TextView>(R.id.btm_menu_loan_calc).setOnClickListener (this)
        findViewById<TextView>(R.id.toolbar_help).setOnClickListener (this)
    }
}

interface OnTransactionItemClickListener {
    fun onTransactionClick(transaction: IntlTransaction)
}