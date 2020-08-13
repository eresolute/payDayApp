package com.fh.payday.views2.accountsummary

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.view.View
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.models.CustomerSummary
import com.fh.payday.viewmodels.AccountSummaryViewModel
import com.fh.payday.views2.dashboard.MainActivity
import com.fh.payday.views2.help.HelpActivity
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity
import com.fh.payday.views2.locator.LocatorActivity
import com.fh.payday.views2.message.ContactUsActivity

class AccountSummaryActivity : BaseActivity() {
    private lateinit var tvLoanDetails: TextView
    private lateinit var tvPaydayCard: TextView

    private lateinit var btmHome: TextView
    private lateinit var btmBranch: TextView
    private lateinit var btmCurrencyConv: TextView
    private lateinit var btmContactUs: TextView

    lateinit var viewModel: AccountSummaryViewModel
    lateinit var viewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val summary = intent.getParcelableExtra<CustomerSummary>("summary") ?: return finish()
        viewModel.setSummary(summary)
        selectedTab(0)
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {}

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}

            override fun onPageSelected(position: Int) {
                selectedTab(position)
            }
        })

        handleBottomBar()
    }


    private fun handleBottomBar() {

        findViewById<TextView>(R.id.btm_home).setOnClickListener { view ->
            val i = Intent(view.context, MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(i)
        }

        findViewById<TextView>(R.id.btm_menu_branch).setOnClickListener { v ->
            val i = Intent(v.context, LocatorActivity::class.java)
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
            startActivity(Intent(this, HelpActivity::class.java))
        }
    }

    private fun selectedTab(position: Int) {
        when (position) {

            0 -> {
                /*tvPaydayCard.setBackgroundResource(R.drawable.bg_light_blue)
                tvLoanDetails.setBackgroundResource(R.color.white)*/

                tvPaydayCard.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
                tvLoanDetails.setTextColor(ContextCompat.getColor(this, R.color.textDisabled))
            }
            1 -> {
                /*tvLoanDetails.setBackgroundResource(R.drawable.bg_light_blue)
                tvPaydayCard.setBackgroundResource(R.color.white)*/
                tvPaydayCard.setTextColor(ContextCompat.getColor(this, R.color.textDisabled))
                tvLoanDetails.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
            }
            else -> {
                /* tvPaydayCard.setBackgroundResource(R.drawable.bg_light_blue)
                 tvLoanDetails.setBackgroundResource(R.color.white)*/
                tvPaydayCard.setTextColor(ContextCompat.getColor(this, R.color.textDisabled))
                tvLoanDetails.setTextColor(ContextCompat.getColor(this, R.color.textDisabled))

            }
        }
    }

    override fun getLayout() = R.layout.activity_account_summary

    override fun init() {
        viewModel = ViewModelProviders.of(this).get<AccountSummaryViewModel>(AccountSummaryViewModel::class.java)
        findViewById<View>(R.id.toolbar_back).setOnClickListener { onBackPressed() }
        val toolbarTitle = findViewById<TextView>(R.id.toolbar_title)
        toolbarTitle.setText(R.string.account_summary)
        tvPaydayCard = findViewById(R.id.tv_payday_card)
        tvLoanDetails = findViewById(R.id.tv_loan_details)
        viewPager = findViewById(R.id.view_pager)

        tvPaydayCard.setOnClickListener {
            viewPager.currentItem = 0
        }
        tvLoanDetails.setOnClickListener {
            viewPager.currentItem = 1
        }

        val adapter = SlidePagerAdapter(supportFragmentManager)
        viewPager.adapter = adapter
        viewPager.currentItem = 0
    }

    private class SlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        private val NUM_PAGES = 2

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> CardDetailsFragment()
                1 -> LoanDetailsFragment()
                else -> throw IllegalStateException("Invalid page position")
            }
        }

        override fun getCount(): Int {
            return NUM_PAGES
        }
    }

    override fun onBackPressed() {
        if (viewPager.currentItem == 0) {
            super.onBackPressed()
        } else {
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }
}
