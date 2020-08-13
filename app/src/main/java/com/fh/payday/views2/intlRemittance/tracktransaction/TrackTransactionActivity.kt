package com.fh.payday.views2.intlRemittance.tracktransaction

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
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
import com.fh.payday.datasource.models.Item
import com.fh.payday.datasource.models.intlRemittance.IntlTransaction
import com.fh.payday.datasource.models.transactionhistory.TransactionDate
import com.fh.payday.utilities.DateTime
import com.fh.payday.viewmodels.TrackTransactionViewModel
import com.fh.payday.views2.dashboard.MainActivity
import com.fh.payday.views2.help.HelpActivity
import com.fh.payday.views2.intlRemittance.*
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity
import com.fh.payday.views2.locator.LocatorActivity
import com.fh.payday.views2.message.ContactUsActivity
import com.fh.payday.views2.shared.fragments.DatePickerFragment
import kotlinx.android.synthetic.main.activity_track_transaction.*
import kotlinx.android.synthetic.main.layout_payment_option.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*

class TrackTransactionActivity : BaseActivity() {
    private lateinit var viewModel: TrackTransactionViewModel
    private lateinit var itemList: List<Item>
    private var type: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbar_back.setOnClickListener(this)
        handleBottomBar()
    }

    override fun getLayout(): Int = R.layout.activity_track_transaction

    fun getViewModel(): TrackTransactionViewModel {
        return viewModel
    }

    override fun init() {

        viewModel = ViewModelProviders.of(this).get(TrackTransactionViewModel::class.java)
        tv_paymentOptiion.visibility = View.GONE
        tv_select_beneficiary.visibility = View.GONE
        toolbar_title.setText(R.string.track_trans_status)
        tv_from_date.setOnClickListener { showDatePicker(it) }
        tv_to_date.setOnClickListener { showDatePicker(it) }
        setupTabs(view_pager)
        setInitialDate()
//        tab_layout.setupWithViewPager(view_pager)


        viewModel.transactionDate.observe(this, android.arch.lifecycle.Observer {
            it ?: return@Observer
            tv_from_date.text = DateTime.parse(it.fromDate, outputFormat = "dd MMM yyyy")
            tv_to_date.text = DateTime.parse(it.toDate, outputFormat = "dd MMM yyyy")
        })

        tv_bank_transfer.setOnClickListener {
            type = 0
            viewModel.deliveryMode = DeliveryModes.BTALTERNATE
            getBankTransferList()
            setupTabs(view_pager)
        }

        tv_cash_payout.setOnClickListener {
            type = 0
            viewModel.deliveryMode = DeliveryModes.CPALTERNATE
            getCashPayoutList()
            setupTabs(view_pager)
        }





//        tab_layout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
//            override fun onTabReselected(tab: TabLayout.Tab?) {
//                tab ?: return
//                val view = tab.customView
//                view ?: return
//                val textView = view.findViewById<TextView>(R.id.textView)
//                textView.findViewById<TextView>(R.id.textView).setTextColor(ContextCompat.getColor(textView.context, R.color.white))
//                val drawable = textView.compoundDrawables
//                drawable[0].colorFilter = PorterDuffColorFilter(ContextCompat.getColor(textView.context, R.color.white), PorterDuff.Mode.SRC_IN)
//            }
//
//            override fun onTabUnselected(tab: TabLayout.Tab?) {
//                tab ?: return
//                val view = tab.customView
//                view ?: return
//                val textView = view.findViewById<TextView>(R.id.textView)
//                textView.setTextColor(ContextCompat.getColor(textView.context, R.color.black))
//                val drawable = textView.compoundDrawables
//                drawable[0].colorFilter = PorterDuffColorFilter(ContextCompat.getColor(textView.context, R.color.colorAccent), PorterDuff.Mode.SRC_IN)
//            }
//
//            override fun onTabSelected(tab: TabLayout.Tab?) {
//                tab ?: return
//                val view = tab.customView
//                view ?: return
//
//                val textView = view.findViewById<TextView>(R.id.textView)
//                textView.findViewById<TextView>(R.id.textView).setTextColor(ContextCompat.getColor(textView.context, R.color.white))
//
//                val drawable = textView.compoundDrawables
//                drawable[0].colorFilter = PorterDuffColorFilter(ContextCompat.getColor(textView.context, R.color.white), PorterDuff.Mode.SRC_IN)
//            }
//
//        })
//        setInitialTab()
    }

    private fun getBankTransferList() {
        setBackGround(tv_cash_payout,tv_bank_transfer,this)
        setTextColor(tv_cash_payout,tv_bank_transfer,this)
        setDrawableBT(tv_cash_payout,tv_bank_transfer)

    }

    private fun getCashPayoutList() {
        setBackGround(tv_bank_transfer,tv_cash_payout,this)
        setTextColor(tv_bank_transfer,tv_cash_payout,this)
        setDrawableCP(tv_bank_transfer,tv_cash_payout)


    }

    @SuppressLint("InflateParams")
    private fun setupTabs(viewPager: ViewPager) {
//        itemList = DataGenerator.getTrackTransaction(this)
        val adapter = ViewPagerAdapter(supportFragmentManager)

            val fragment = TrackTransactionFragment()
            val bundle = Bundle()
            bundle.putInt("index", type)
            bundle.putParcelable("card", intent.getParcelableExtra("card"))
            fragment.arguments = bundle
            adapter.addFrag(fragment)

         viewPager.adapter = adapter

//        for ((index) in itemList.withIndex()) {
//            val tabItem = LayoutInflater.from(this).inflate(R.layout.custom_tab_intl_beneficiary, null) as TextView
//            tabItem.text = itemList[index].name
//            tabItem.setTextColor(ContextCompat.getColor(tabItem.context, R.color.textDarkColor))
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                tabItem.setCompoundDrawablesRelativeWithIntrinsicBounds(itemList[index].res, 0, 0, 0)
//            } else {
//                tabItem.setCompoundDrawablesWithIntrinsicBounds(itemList[index].res, 0, 0, 0)
//            }
//            tabItem.setCompoundDrawablesWithIntrinsicBounds(itemList[index].res, 0, 0, 0)
//            //tab_layout.getTabAt(index)?.customView = tabItem
//        }

    }

    private fun setInitialTab() {
      //  val view = tab_layout.getTabAt(0)?.customView
//
//        view ?: return
//
//        val textView = view.findViewById<TextView>(R.id.textView)
//        textView.setTextColor(ContextCompat.getColor(this, R.color.white))
//
//        val drawable = textView.compoundDrawables
//        drawable[0].colorFilter = PorterDuffColorFilter(ContextCompat.getColor(textView.context, R.color.white), PorterDuff.Mode.SRC_IN)
    }

    private fun setInitialDate() {

        val fromDate = DateTime.parse(DateTime.currentDayOfLastSixMonths("yyyy-MM-dd"), outputFormat = "dd MMM yyyy")
        val toDate = DateTime.parse(DateTime.currentDate("yyyy-MM-dd"), outputFormat = "dd MMM yyyy")

        viewModel.fromDate.value = DateTime.parse(DateTime.currentDayOfLastSixMonths("yyyy-MM-dd"), outputFormat = "yyyy-MM-dd")
        viewModel.toDate.value = DateTime.parse(DateTime.currentDate("yyyy-MM-dd"), outputFormat = "yyyy-MM-dd")

        viewModel.transactionDate.value = TransactionDate(DateTime.firstDayOfCurrentMonth("yyyy-MM-dd"), DateTime.currentDate("yyyy-MM-dd"))

        tv_from_date.text = fromDate
        tv_to_date.text = toDate
    }

    private fun showDatePicker(view: View) {
        val maxDate = Calendar.getInstance()
        val minDate = Calendar.getInstance().apply { add(Calendar.YEAR, -1) }

        val dateTime = when (view.id) {
            tv_to_date.id -> DateTime.parseDate(viewModel.transactionDate.value?.toDate)
            else -> DateTime.parseDate(viewModel.transactionDate.value?.fromDate)
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

                    val (fromDate, toDate) = viewModel.transactionDate.value
                            ?: return@OnDateSetListener
                    when (view.id) {
                        tv_from_date.id -> {
                            if (DateTime.isValidDateRange(d, toDate))
                                viewModel.setDate(d, toDate)
                            else
                                onFailure(findViewById(R.id.root_view), getString(R.string.invalid_date))
                        }
                        tv_to_date.id -> {
                            if (DateTime.isValidDateRange(fromDate, d))
                                viewModel.setDate(fromDate, d)
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

        fun addFrag(fragment: Fragment) {
            fragmentList.add(fragment)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return fragmentTitleList[position]
        }
    }

    private fun handleBottomBar() {

        findViewById<TextView>(R.id.btm_home).setOnClickListener { view ->
            val i = Intent(view.context, MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(i)
        }

        findViewById<TextView>(R.id.btm_menu_branch).setOnClickListener { view ->
            val i = Intent(view.context, LocatorActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(i)
        }

        findViewById<TextView>(R.id.btm_menu_support).setOnClickListener { v ->
            val i = Intent(v.context, ContactUsActivity::class.java)
            startActivity(i)
        }

        findViewById<TextView>(R.id.btm_menu_loan_calc).setOnClickListener { v ->
            val i = Intent(v.context, LoanCalculatorActivity::class.java)
            startActivity(i)
        }

        findViewById<TextView>(R.id.toolbar_help).setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java)
                    .putExtra("anchor", "transactionHistoryHelp"))
        }
    }

}

interface OnTransactionItemClickListener {
    fun onTransactionClick(transaction: IntlTransaction)
}