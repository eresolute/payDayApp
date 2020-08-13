package com.fh.payday.views2.bottombar.location

import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import kotlinx.android.synthetic.main.bottombar.*

class LocationActivity : BaseActivity() {

    override fun init() {
        attachListeners()
        findViewById<TextView>(R.id.toolbar_title).apply {
            text = getString(R.string.locator)
        }

        findViewById<View>(R.id.toolbar_help).visibility = View.INVISIBLE
        findViewById<View>(R.id.toolbar_back).setOnClickListener { onBackPressed() }

        findViewById<LinearLayout>(R.id.linear_layout_branch_location).setOnClickListener {
            val intent = Intent(this, BranchATMActivity::class.java)
            intent.putExtra("issue", "branch")
            startActivity(intent)
        }
        findViewById<LinearLayout>(R.id.linear_layout_atm_location).setOnClickListener {
            val intent = Intent(this, BranchATMActivity::class.java)
            intent.putExtra("issue", "atm")
            startActivity(intent)
        }

    }

    override fun getLayout(): Int {
        return R.layout.activity_location
    }

    private fun attachListeners() {
        btm_menu_currency_conv.setOnClickListener(this)
        btm_menu_faq.setOnClickListener(this)
        btm_menu_how_to_reg.setOnClickListener(this)
    }
}
