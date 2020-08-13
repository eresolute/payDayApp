package com.fh.payday.views2.dashboard

import android.content.Intent
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.views2.help.HelpActivity
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity
import com.fh.payday.views2.locator.LocatorActivity
import com.fh.payday.views2.message.ContactUsActivity
import kotlinx.android.synthetic.main.toolbar.*

class EarlySalaryDetailActivity : BaseActivity() {
    override fun getLayout(): Int {
        return R.layout.activity_early_salry_details
    }

    override fun init() {
        toolbar_title.setText(R.string.early_salary)
        toolbar_back.setOnClickListener { onBackPressed() }

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
}