package com.fh.payday.views2.loancalculator

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.widget.AppCompatImageView
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.utilities.NonSwipeableViewPager
import com.fh.payday.viewmodels.LoanCalculatorViewModel

class LoanCalculatorActivity : BaseActivity() {

    lateinit var viewPager: NonSwipeableViewPager
    lateinit var viewModel: LoanCalculatorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(LoanCalculatorViewModel::class.java)

    }

    override fun init() {
        viewPager = findViewById(R.id.view_pager)
        viewPager.adapter = ViewPagerAdapter(supportFragmentManager)

        findViewById<TextView>(R.id.btm_menu_support).setOnClickListener(this)
        findViewById<TextView>(R.id.btm_home).setOnClickListener(this)
        findViewById<TextView>(R.id.btm_menu_branch).setOnClickListener(this)
        findViewById<AppCompatImageView>(R.id.toolbar_back).setOnClickListener {
            onBackPressed()
        }
        findViewById<TextView>(R.id.toolbar_help).setOnClickListener {
            startHelpActivity("applicationFooterHelp")
        }

        findViewById<TextView>(R.id.toolbar_title).text = getString(R.string.loan_calculator_label)

    }

    override fun getLayout(): Int = R.layout.activity_loan_calculator

    fun onCredentialsAdded() {
        viewPager.currentItem = 1
    }

    fun onCalculateAgain() {
        viewPager.currentItem = 0
    }

    class ViewPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

        val NUM_PAGES = 2

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> EmiCredentialFragment()
                1 -> EmiSummaryFragment()
                else -> throw IllegalStateException("Invalid page position")
            }
        }

        override fun getCount(): Int = NUM_PAGES

    }

    override fun onBackPressed() {

        if (viewPager.currentItem == 0)
            super.onBackPressed()
        else
            viewPager.currentItem = viewPager.currentItem - 1
    }

    companion object {
        const val MIN_LOAN = 0
        const val MAX_LOAN = 5_00_000

        const val MIN_TENURE = 0
        const val MAX_TENURE = 5

        const val MIN_INTEREST = 0
        const val MAX_INTEREST = 40
    }
}
