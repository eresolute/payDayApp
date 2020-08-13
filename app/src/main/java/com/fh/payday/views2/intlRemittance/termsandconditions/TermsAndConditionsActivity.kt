package com.fh.payday.views2.intlRemittance.termsandconditions

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.utilities.DIGITAL_BANKING_TC_URL
import com.fh.payday.utilities.UAEX_TERMS_CONDITIONS
import kotlinx.android.synthetic.main.activity_terms_and_conditions.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar_main.toolbar_title

class TermsAndConditionsActivity : BaseActivity() {
    override fun getLayout(): Int = R.layout.activity_terms_and_conditions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {
            }
            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
            }
            override fun onPageSelected(position: Int) {
                selectedTab(position)
            }
        })
    }
    override fun init() {
        setUpToolbar()
        handleBottomBar()
        toolbar_title.text = getString(R.string.terms_and_Conditions)

        tv_link_fh.setOnClickListener {
            view_pager.currentItem = 0
        }
        tv_link_exchange.setOnClickListener {
            view_pager.currentItem = 1
        }

        val adapter = SlidePagerAdapter(supportFragmentManager)
        view_pager.adapter = adapter
        view_pager.currentItem = 0
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }


    private fun selectedTab(position: Int) {
        when (position) {
            0 -> {
                changeExchangeColor(ContextCompat.getColor(this, R.color.white), ContextCompat.getColor(this, R.color.colorAccent))
                changeFHColor(ContextCompat.getColor( this, R.color.textColor), ContextCompat.getColor(this, R.color.white))
                //tv_link_fh.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
                //tv_link_exchange.setTextColor(ContextCompat.getColor(this, R.color.textDisabled))
            }
            1 -> {
                changeExchangeColor(ContextCompat.getColor(this, R.color.textColor), ContextCompat.getColor(this, R.color.white) )
                changeFHColor(ContextCompat.getColor(this, R.color.white), ContextCompat.getColor(this, R.color.colorAccent) )
                //tv_link_fh.setTextColor(ContextCompat.getColor(this, R.color.textDisabled))
                //tv_link_exchange.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
            }
            else -> {
                tv_link_fh.setTextColor(ContextCompat.getColor(this, R.color.textDisabled))
                tv_link_exchange.setTextColor(ContextCompat.getColor(this, R.color.textDisabled))

            }
        }
    }

    private class SlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        private val mPAGES = 2

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> FHFragment().apply {
                    arguments = Bundle().apply {
                        putString("url", DIGITAL_BANKING_TC_URL)
                    }
                }
                1 -> FHFragment().apply {
                    arguments = Bundle().apply {
                        putString("url", UAEX_TERMS_CONDITIONS)
                    }
                }
//                1 -> UAEXchangeFragment()
                else -> throw IllegalStateException("Invalid page position")
            }
        }

        override fun getCount(): Int {
            return mPAGES
        }
    }

    private fun changeExchangeColor(textColor: Int, bgColor: Int) {
        tv_link_exchange.setTextColor(textColor)
        tv_link_exchange.setBackgroundColor(bgColor)
    }

    private fun changeFHColor(textColor: Int, bgColor: Int) {
        tv_link_fh.setTextColor(textColor)
        tv_link_fh.setBackgroundColor(bgColor)
    }

    private fun setUpToolbar() {
        toolbar_back.setOnClickListener(this)
        toolbar_title.text = getString(R.string.intl_money_transfer)
    }

    private fun handleBottomBar() {
        findViewById<TextView>(R.id.btm_home).setOnClickListener(this)
        findViewById<TextView>(R.id.btm_menu_branch).setOnClickListener(this)
        findViewById<TextView>(R.id.btm_menu_support).setOnClickListener(this)
        findViewById<TextView>(R.id.btm_menu_loan_calc).setOnClickListener(this)
        findViewById<TextView>(R.id.toolbar_help).setOnClickListener(this)
    }
}
