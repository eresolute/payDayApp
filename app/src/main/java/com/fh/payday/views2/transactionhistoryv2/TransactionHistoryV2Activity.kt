package com.fh.payday.views2.transactionhistoryv2

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
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.DataGenerator
import com.fh.payday.datasource.models.Item
import com.fh.payday.datasource.models.transactionhistory.TransactionDate
import com.fh.payday.utilities.DateTime
import com.fh.payday.utilities.getDecimalValue
import com.fh.payday.viewmodels.TransactionHistoryViewModel
import com.fh.payday.views2.dashboard.MainActivity
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity
import com.fh.payday.views2.locator.LocatorActivity
import com.fh.payday.views2.message.ContactUsActivity
import com.fh.payday.views2.shared.custom.ProgressDialogFragment
import com.fh.payday.views2.shared.fragments.DatePickerFragment
import com.fh.payday.views2.transactionhistory.TransactionHistoryDetailFragment
import kotlinx.android.synthetic.main.activity_transaction_historyv2.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*
import kotlin.collections.ArrayList


class TransactionHistoryV2Activity : BaseActivity() {

    private lateinit var itemList: List<Item>
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager

    val viewModel by lazy {
        ViewModelProviders.of(this).get(TransactionHistoryViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleBottomBar()

        tv_calender.setOnClickListener {
            /*val fragment = MonthPickerFragment()
            val bundle = Bundle()
            bundle.putString("option", "from")
            fragment.arguments = bundle
            fragment.show(supportFragmentManager, "dialog")*/
            showDatePicker(it)
        }

        tv_calender_to.setOnClickListener {
            /*val fragment = MonthPickerFragment()
            val bundle = Bundle()
            bundle.putString("option", "to")
            fragment.arguments = bundle
            fragment.show(supportFragmentManager, "dialog")*/
            showDatePicker(it)
        }

        setInitialTab()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                tab ?: return
                val view = tab.customView
                view ?: return
                val textView = view.findViewById<TextView>(R.id.textView)
                textView.findViewById<TextView>(R.id.textView).setTextColor(ContextCompat.getColor(textView.context, R.color.white))
                val drawable = textView.compoundDrawables
                drawable[0].colorFilter = PorterDuffColorFilter(ContextCompat.getColor(textView.context, R.color.white), PorterDuff.Mode.SRC_IN)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab ?: return
                val view = tab.customView
                view ?: return
                val textView = view.findViewById<TextView>(R.id.textView)
                textView.setTextColor(ContextCompat.getColor(this@TransactionHistoryV2Activity, R.color.black))
                val drawable = textView.compoundDrawables
                drawable[0].colorFilter = PorterDuffColorFilter(ContextCompat.getColor(textView.context, R.color.colorAccent), PorterDuff.Mode.SRC_IN)
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab ?: return
                val view = tab.customView
                view ?: return
                val textView = view.findViewById<TextView>(R.id.textView)
                textView.findViewById<TextView>(R.id.textView).setTextColor(ContextCompat.getColor(textView.context, R.color.white))
                val drawable = textView.compoundDrawables
                drawable[0].colorFilter = PorterDuffColorFilter(ContextCompat.getColor(textView.context, R.color.white), PorterDuff.Mode.SRC_IN)
            }
        })

        if (intent?.getIntExtra("transaction_type", DEFAULT) == SALARIES_CREDITED) {
            viewPager.currentItem = 4
            tv_calender.text = DateTime.parse(DateTime.currentDayOfLastSixMonths("yyyy-MM-dd"), outputFormat = "dd MMM yyyy")
            tv_calender_to.text = DateTime.parse(DateTime.currentDate("yyyy-MM-dd"), outputFormat = "dd MMM yyyy")
        }

        if (intent?.getIntExtra("transaction_type", DEFAULT) == THREE_MONTH_STATEMENT ||
                intent?.getIntExtra("transaction_type", DEFAULT) == SIX_MONTH_STATEMENT) {
            val minDate = intent?.getStringExtra("min_date")
                    ?: DateTime.firstDayOfCurrentMonth("yyyy-MM-dd")
            viewModel.setDate(minDate, DateTime.currentDate("yyyy-MM-dd"))
        }
    }

    private fun setInitialTab() {
        val view = tabLayout.getTabAt(0)?.customView
        view ?: return
        val textView = view.findViewById<TextView>(R.id.textView)
        textView.setTextColor(ContextCompat.getColor(this, R.color.white))

        val drawable = textView.compoundDrawables
        drawable[0].colorFilter = PorterDuffColorFilter(ContextCompat.getColor(textView.context, R.color.white), PorterDuff.Mode.SRC_IN)
        for (i in 1..tabLayout.tabCount) {
            val mView = tabLayout.getTabAt(i)?.customView

            mView ?: return

            val mTextView = mView.findViewById<TextView>(R.id.textView)
            mTextView.setTextColor(ContextCompat.getColor(this, R.color.black))
            val mDrawable = mTextView.compoundDrawables
            mDrawable[0].colorFilter = PorterDuffColorFilter(ContextCompat.getColor(mTextView.context, R.color.colorAccent), PorterDuff.Mode.SRC_IN)
        }
    }
    override fun getLayout() = R.layout.activity_transaction_historyv2

    override fun init() {
        toolbar_title.setText(R.string.transaction_history)
        toolbar_back.setOnClickListener { onBackPressed() }

        pb_credit.progressDrawable.setColorFilter(ContextCompat.getColor(this, R.color.credit_color), PorterDuff.Mode.SRC_IN)
        pb_debit.progressDrawable.setColorFilter(ContextCompat.getColor(this, R.color.debit_color), PorterDuff.Mode.SRC_IN)

        val loanNumber = intent?.getStringExtra("loanNumber")

        val availableBalance = intent?.getStringExtra("availableBalance")?.let {
            String.format(getString(R.string.amount_in_aed), it.getDecimalValue())
        } ?: "0.00"

        val availableOverdraft = intent?.getStringExtra("availableOverdraft")?.let {
            String.format(getString(R.string.amount_in_aed), it.getDecimalValue())
        } ?: "0.00"

        tv_available_balance.text = getColoredSpan(getString(R.string.available_balance_text), availableBalance, ContextCompat.getColor(this, R.color.grey_700))
        tv_available_overdraft.text = getColoredSpan(getString(R.string.available_overdraft), availableOverdraft, ContextCompat.getColor(this, R.color.grey_700))

        viewModel.transactionDate.value = TransactionDate(DateTime.firstDayOfCurrentMonth("yyyy-MM-dd"), DateTime.currentDate("yyyy-MM-dd"))

        viewModel.loanNumber = loanNumber
        viewPager = findViewById(R.id.view_pager)

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(i: Int, v: Float, i1: Int) {

            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    4 -> {
                        tv_calender_to.isEnabled = false
                        tv_calender.isEnabled = false
                        tv_calender.text = DateTime.parse(DateTime.currentDayOfLastSixMonths("yyyy-MM-dd"), outputFormat = "dd MMM yyyy")
                        tv_calender_to.text = DateTime.parse(DateTime.currentDate("yyyy-MM-dd"), outputFormat = "dd MMM yyyy")
                    }
                    else -> {
                        tv_calender_to.isEnabled = true
                        tv_calender.isEnabled = true
                        viewModel.transactionDate.observe(this@TransactionHistoryV2Activity, android.arch.lifecycle.Observer {
                            it ?: return@Observer
                            tv_calender.text = DateTime.parse(it.fromDate, outputFormat = "dd MMM yyyy")
                            tv_calender_to.text = DateTime.parse(it.toDate, outputFormat = "dd MMM yyyy")
                        })
                    }
                }
            }

            override fun onPageScrollStateChanged(i: Int) {

            }
        })

        tabLayout = findViewById(R.id.tabs)
        tabLayout.setupWithViewPager(viewPager)
        setupTabs(viewPager)

        viewModel.transactionDate.observe(this, android.arch.lifecycle.Observer {
            it ?: return@Observer
            tv_calender.text = DateTime.parse(it.fromDate, outputFormat = "dd MMM yyyy")
            tv_calender_to.text = DateTime.parse(it.toDate, outputFormat = "dd MMM yyyy")
            if (intent?.getIntExtra("transaction_type", DEFAULT) == SALARIES_CREDITED) {
                tv_calender.text = DateTime.parse(DateTime.currentDayOfLastSixMonths("yyyy-MM-dd"), outputFormat = "dd MMM yyyy")
                tv_calender_to.text = DateTime.parse(DateTime.currentDate("yyyy-MM-dd"), outputFormat = "dd MMM yyyy")
            }
        })
    }

    override fun showProgress(message: String, cancellable: Boolean, dismissListener: ProgressDialogFragment.OnDismissListener) {

    }

    override fun hideProgress() {

    }

    override fun showNoInternetView(message: String, retryAction: () -> Unit) {
        container.visibility = View.GONE
        super.showNoInternetView(message, retryAction)
    }

    override fun hideNoInternetView() {
        container.visibility = View.VISIBLE
        super.hideNoInternetView()
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

//        findViewById<TextView>(R.id.toolbar_help).setOnClickListener {
//            startActivity(Intent(this, HelpActivity::class.java))
//        }

        findViewById<TextView>(R.id.toolbar_help).setOnClickListener{
            startHelpActivity("transactionHistoryHelp")
        }
    }

    @SuppressLint("InflateParams")
    private fun setupTabs(viewPager: ViewPager) {
        itemList = DataGenerator.getTransactionHistory(this)
        val adapter = ViewPagerAdapter(supportFragmentManager)

        for ((index) in itemList.withIndex()) {
            val fragment = TransactionHistoryDetailFragment()
            val bundle = Bundle()
            bundle.putInt("index", index)
            fragment.arguments = bundle
            adapter.addFrag(fragment, itemList[index].name)
        }
        viewPager.adapter = adapter
        for ((index) in itemList.withIndex()) {
            val tabItem = LayoutInflater.from(this).inflate(R.layout.custom_tab, null) as TextView
            tabItem.text = itemList[index].name
            tabItem.setTextColor(ContextCompat.getColor(tabItem.context, R.color.textDarkColor))
            //tabItem.setTextAppearance(tabItem.context, R.style.AppTheme_Text_Time)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                tabItem.setCompoundDrawablesRelativeWithIntrinsicBounds(itemList[index].res, 0, 0, 0)
            } else {
                tabItem.setCompoundDrawablesWithIntrinsicBounds(itemList[index].res, 0, 0, 0)
            }
            tabItem.setCompoundDrawablesWithIntrinsicBounds(itemList[index].res, 0, 0, 0)
            tabLayout.getTabAt(index)?.customView = tabItem
        }

    }

    /*fun setDateTime(monthIndex: Int, day: Int, month: String, year: Int, option: String) {
        if (year > org.joda.time.DateTime.now().year) {
            if (monthIndex > org.joda.time.DateTime.now().monthOfYear || day > org.joda.time.DateTime.now().dayOfMonth().get()) {
                onFailure(findViewById(R.id.root_view), getString(R.string.invalid_date))
                return
            }
        }
        var fromDate = ""
        var toDate = ""
        var finalText = ""
        if (option == "from") {
            finalText = "$day $month $year"
            val yearS = year.toString()
            val monthS = if (monthIndex < 10) {
                "0$monthIndex"
            } else {
                monthIndex.toString()
            }
            val days = if (day < 10) {
                "0$day"
            } else {
                day.toString()
            }

            fromDate = "$yearS-$monthS-$days"
            toDate = viewModel.transactionDate.value?.toDate ?: return

        } else {
            finalText = "$day $month $year"
            val yearS = year.toString()
            val monthS = if (monthIndex < 10) {
                "0$monthIndex"
            } else {
                monthIndex.toString()
            }
            val days = if (day < 10) {
                "0$day"
            } else {
                day.toString()
            }

            toDate = "$yearS-$monthS-$days"
            fromDate = viewModel.transactionDate.value?.fromDate ?: return
        }
        if (DateTime.checkDateRange(fromDate, toDate)) {
            onFailure(findViewById(R.id.root_view), getString(R.string.invalid_date))
            return
        }

        if (option == "from"){
            findViewById<TextView>(R.id.tv_calender).text = finalText
        } else {
            findViewById<TextView>(R.id.tv_calender_to).text = finalText
        }
        viewModel.transactionDate.value = TransactionDate(fromDate, toDate)
    }*/

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

    fun setCreditDebit(creditDebit: com.fh.payday.datasource.models.CreditDebit) {
        pb_credit.progress = creditDebit.TotalCredit.toInt()
        pb_credit.progress = getProgressValue(creditDebit.TotalDebit, creditDebit.TotalCredit)
        tv_money_in_value.text = String.format(getString(R.string.amount_in_aed), creditDebit.TotalCredit.toString().getDecimalValue())
        pb_debit.progress = getProgressValue(creditDebit.TotalCredit, creditDebit.TotalDebit)
        tv_money_out_value.text = String.format(getString(R.string.amount_in_aed), creditDebit.TotalDebit.toString().getDecimalValue())
    }

    private fun getProgressValue(value: Double, progressValue: Double): Int {
        val totalValue = value + progressValue
        return ((progressValue / totalValue) * 100).toInt()
    }

    private fun showDatePicker(view: View) {

        val minDate = intent?.getStringExtra("min_date")?.let {
            DateTime.parseDate(it).toDate()
        } ?: Calendar.getInstance().apply { add(Calendar.YEAR, -1) }.time

        val maxDate = intent?.getStringExtra("max_date")?.let {
            DateTime.parseDate(it).toDate()
        } ?: Calendar.getInstance().time


        val dateTime = when (view.id) {
            tv_calender_to.id -> DateTime.parseDate(viewModel.transactionDate.value?.toDate)
            else -> DateTime.parseDate(viewModel.transactionDate.value?.fromDate)
        }

        val day = dateTime.dayOfMonth
        val month = dateTime.monthOfYear - 1
        val year = dateTime.year

        DatePickerFragment.Builder()
                .setDay(day)
                .setMonth(month)
                .setYear(year)
                .setMinDate(minDate)
                .setMaxDate(maxDate)
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
                        tv_calender.id -> {
                            if (DateTime.isValidDateRange(d, toDate))
                                viewModel.setDate(d, toDate)
                            else
                                onFailure(findViewById(R.id.root_view), getString(R.string.invalid_date))
                        }
                        tv_calender_to.id -> {
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

    companion object {
        const val SALARIES_CREDITED = 9
        const val DEFAULT = 8
        const val THREE_MONTH_STATEMENT = 3
        const val SIX_MONTH_STATEMENT = 6
    }

}

fun getColoredSpan(title: String, amount: String, color: Int): SpannableString {
    val finalText = "$title\n$amount"

    val coloredSpan = SpannableString(finalText)
    coloredSpan.setSpan(ForegroundColorSpan(color), 0, title.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
    coloredSpan.setSpan(RelativeSizeSpan(0.8f), 0, title.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)

    return coloredSpan
}
