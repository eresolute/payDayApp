package com.fh.payday.views2.locator

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.DataGenerator
import kotlinx.android.synthetic.main.bottombar_dashboard.*
import kotlinx.android.synthetic.main.toolbar.*

class SMSBankingActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        btm_home.setOnClickListener(this)
        btm_menu_support.setOnClickListener(this)
        btm_menu_loan_calc.setOnClickListener(this)
        toolbar_back.setOnClickListener(this)
        toolbar_help.setOnClickListener(this)

    }

    override fun getLayout(): Int {
        return R.layout.activity_smsbanking
    }

    override fun init() {
        setToolbarTitle(getString(R.string.sms_banking))
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        val adapter = SMSBankingAdapter(DataGenerator.getSMSCodes(this))
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
    }

    private fun setToolbarTitle(title: String) {
        toolbar_title.text = title
    }

}
