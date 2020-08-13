package com.fh.payday.views2.moneytransfer

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.arch.lifecycle.ViewModelProviders
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.models.Card
import com.fh.payday.datasource.models.Item
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.DateTime
import com.fh.payday.utilities.getDecimalValue
import com.fh.payday.viewmodels.TransferViewModel
import com.fh.payday.views2.shared.fragments.DatePickerFragment
import com.fh.payday.views2.transactionhistoryv2.TransactionHistoryV2Activity
import com.fh.payday.views2.transactionhistoryv2.getColoredSpan
import kotlinx.android.synthetic.main.activity_transaction_history_transfer.*
import kotlinx.android.synthetic.main.bottombar_dashboard.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*

class TransactionHistoryActivity : BaseActivity() {

    val viewModel: TransferViewModel by lazy {
        ViewModelProviders.of(this).get(TransferViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        toolbar_back.setOnClickListener(this)
        findViewById<View>(R.id.toolbar_help).setOnClickListener {
            startHelpActivity("transactionHistoryHelp")
        }
        btm_home.setOnClickListener(this)
        btm_menu_branch.setOnClickListener(this)
        btm_menu_support.setOnClickListener(this)
        btm_menu_loan_calc.setOnClickListener(this)

        viewModel.user = UserPreferences.instance.getUser(this)
        attachObservers()
    }

    override fun getLayout() = R.layout.activity_transaction_history_transfer

    override fun init() {
        toolbar_title.setText(R.string.transaction_history)

        val availableBalance = intent?.getStringExtra("balanceLeft")?.let {
            String.format(getString(R.string.amount_in_aed), it.getDecimalValue())
        } ?: intent?.getParcelableExtra<Card>("card")?.let {
            String.format(getString(R.string.amount_in_aed), it.availableBalance.getDecimalValue())
        } ?: "0.00"

        tv_available_balance.text = getColoredSpan(getString(R.string.available_balance_text),
                availableBalance, ContextCompat.getColor(this, R.color.grey_700))

        pb_credit.progressDrawable.setColorFilter(ContextCompat.getColor(this, R.color.credit_color), PorterDuff.Mode.SRC_IN)
        pb_debit.progressDrawable.setColorFilter(ContextCompat.getColor(this, R.color.debit_color), PorterDuff.Mode.SRC_IN)

        tv_from_date.setOnClickListener { showDatePicker(it) }
        tv_to_date.setOnClickListener { showDatePicker(it) }

        tab_layout.setupWithViewPager(view_pager)
        setupTabs()

        setInitialTab()
        tab_layout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
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
                textView.setTextColor(ContextCompat.getColor(textView.context, R.color.black))
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
    }

    private fun setInitialTab() {
        val view = tab_layout.getTabAt(0)?.customView

        view ?: return

        val textView = view.findViewById<TextView>(R.id.textView)
        textView.setTextColor(ContextCompat.getColor(this, R.color.white))

        val drawable = textView.compoundDrawables
        drawable[0].colorFilter = PorterDuffColorFilter(ContextCompat.getColor(textView.context, R.color.white), PorterDuff.Mode.SRC_IN)
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

    @SuppressLint("InflateParams")
    private fun setupTabs() {
        val items = getTabItems()
        val adapter = TransactionHistoryV2Activity.ViewPagerAdapter(supportFragmentManager)

        for ((index, item) in items.withIndex()) {
            val fragment = TransferTHFragment()
            fragment.arguments = Bundle().apply {
                when (index) {
                    0 -> putString("issue", TransferViewModel.P2P)
                    1 -> putString("issue", TransferViewModel.P2IBAN)
                    2 -> putString("issue", TransferViewModel.P2CC)
                }
            }
            adapter.addFrag(fragment, item.name)
        }
        view_pager.adapter = adapter

        for ((index, item) in items.withIndex()) {
            val tabItem = LayoutInflater.from(this).inflate(R.layout.custom_tab, null) as TextView
            tabItem.text = item.name
            tabItem.setTextColor(ContextCompat.getColor(tabItem.context, R.color.textDarkColor))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                tabItem.setCompoundDrawablesRelativeWithIntrinsicBounds(item.res, 0, 0, 0)
            } else {
                tabItem.setCompoundDrawablesWithIntrinsicBounds(item.res, 0, 0, 0)
            }
            tabItem.setCompoundDrawablesWithIntrinsicBounds(item.res, 0, 0, 0)
            tab_layout.getTabAt(index)?.customView = tabItem
        }
    }

    private fun getTabItems(): List<Item> {
        val items = ArrayList<Item>()
        val transactionHistory = resources.getStringArray(R.array.transaction_history_transfer)
        val transactionHistoryIcons = resources.obtainTypedArray(R.array.transaction_history_transfer_icons)
        for (i in transactionHistory.indices) {
            items.add(Item(transactionHistory[i], transactionHistoryIcons.getResourceId(i, 0)))
        }

        transactionHistoryIcons.recycle()
        return items
    }

    private fun fetchTransactions() {
        val (fromDate, toDate) = viewModel.transactionDate.value ?: return
        if (!isNetworkConnected()) {
            return showNoInternetView { fetchTransactions() }
        }

        viewModel.user?.let {
            viewModel.fetchTransactions(it.token, it.sessionId, it.refreshToken,
                    it.customerId.toLong(), fromDate, toDate
            )
        }
    }

    override fun showNoInternetView(message: String, retryAction: () -> Unit) {
        container.visibility = View.GONE
        super.showNoInternetView(message, retryAction)
    }

    override fun hideNoInternetView() {
        container.visibility = View.VISIBLE
        super.hideNoInternetView()
    }

    private fun attachObservers() {
        viewModel.transactionDate.observe(this, android.arch.lifecycle.Observer {
            it ?: return@Observer
            tv_from_date.text = DateTime.parse(it.fromDate, outputFormat = "dd MMM yyyy")
            tv_to_date.text = DateTime.parse(it.toDate, outputFormat = "dd MMM yyyy")
            fetchTransactions()
        })

        viewModel.transactionHistoryState.observe(this, android.arch.lifecycle.Observer {
            it ?: return@Observer

            when (val state = it.getContentIfNotHandled() ?: return@Observer) {
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        return@Observer onSessionExpired(state.message)
                    }

//                    onError(state.message)
                    handleErrorCode(state.errorCode.toInt(), state.message)
                }
                is NetworkState2.Failure -> onFailure(findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    fun setCreditDebit(creditDebit: com.fh.payday.datasource.models.CreditDebit) {
        pb_credit.progress = creditDebit.TotalCredit.toInt()
        pb_credit.progress = calcProgressValue(creditDebit.TotalDebit, creditDebit.TotalCredit)
        tv_money_in_value.text = String.format(getString(R.string.amount_in_aed), creditDebit.TotalCredit.toString().getDecimalValue())
        pb_debit.progress = calcProgressValue(creditDebit.TotalCredit, creditDebit.TotalDebit)
        tv_money_out_value.text = String.format(getString(R.string.amount_in_aed), creditDebit.TotalDebit.toString().getDecimalValue())
    }

    private fun calcProgressValue(value: Double, progressValue: Double): Int {
        val totalValue = value + progressValue
        return ((progressValue / totalValue) * 100).toInt()
    }

}
