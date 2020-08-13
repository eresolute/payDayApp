package com.fh.payday.views2.bottombar

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.views.adapter.HowToRegisterAdapter
import com.fh.payday.views2.bottombar.location.BranchATMActivity

class HowToRegisterActivity : BaseActivity() {
    override fun getLayout(): Int {
        return R.layout.activity_how_to_reg
    }

    override fun init() {

        findViewById<TextView>(R.id.toolbar_title).apply {
            text = getString(R.string.how_to_register)
        }

        handleBottomBar()

        findViewById<View>(R.id.toolbar_help).visibility = View.INVISIBLE
        findViewById<View>(R.id.toolbar_back).setOnClickListener { onBackPressed() }

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        val howToRegisterAdapter = HowToRegisterAdapter(listOf(*resources.getStringArray(R.array.registration_steps)))
        recyclerView.adapter = howToRegisterAdapter
    }

    private fun handleBottomBar() {

        findViewById<TextView>(R.id.btm_menu_cash_out).setOnClickListener {
            val mIntent = Intent(this, BranchATMActivity::class.java)
            mIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            mIntent.putExtra("issue", "branch")
            startActivity(mIntent)
        }

        findViewById<TextView>(R.id.btm_menu_currency_conv).setOnClickListener {
        }

        findViewById<TextView>(R.id.btm_menu_faq).setOnClickListener {
            val i = Intent(this, FaqActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(i)
        }

    }
}