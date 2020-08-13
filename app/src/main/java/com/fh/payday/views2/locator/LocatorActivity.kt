package com.fh.payday.views2.locator

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.DataGenerator
import com.fh.payday.datasource.models.Item
import com.fh.payday.utilities.OnItemClickListener
import com.fh.payday.views.shared.IconListAdapter
import kotlinx.android.synthetic.main.bottombar_dashboard.*
import kotlinx.android.synthetic.main.toolbar.*


class LocatorActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var optionList: List<Item>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        btm_home.setOnClickListener(this)
        btm_menu_support.setOnClickListener(this)
        btm_menu_loan_calc.setOnClickListener(this)
        toolbar_back.setOnClickListener(this)
        toolbar_help.setOnClickListener(this)

        val action = intent.getStringExtra("action") ?: ""

        if (action == "") {
            optionList = DataGenerator.getLocatorOptions(this)
            recyclerView.adapter = IconListAdapter(optionList, itemListener)
        } else {
            optionList = DataGenerator.getIntlRemittanceLocator(this)
            recyclerView.adapter = IconListAdapter(optionList, intlItemListener)
        }
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    override fun getLayout(): Int {
        return R.layout.activity_locator
    }

    override fun init() {
        recyclerView = findViewById(R.id.recycler_view)
        setToolbarTitle(getString(R.string.locator))

    }

    private fun setToolbarTitle(title: String) {
        toolbar_title.text = title
    }

    val intlItemListener = object : OnItemClickListener{
        override fun onItemClick(index: Int) {
            when (index) {
                0 -> {
                    val intent = Intent(this@LocatorActivity, BranchLocationActivity::class.java)
                    intent.putExtra("issue", "uae_exchange")
                    intent.putExtra("allowed", false)
                    startActivity(intent)
                }
             /*   1 -> {
//                    val intent = Intent(this@LocatorActivity, BranchLocationActivity::class.java)
//                    intent.putExtra("issue", "al_ansari")
//                    startActivity(intent)
                }*/
            }
        }
    }

    val itemListener = object : OnItemClickListener{
        override fun onItemClick(index: Int) {
            when (index) {
                0 -> {
                    val intent = Intent(this@LocatorActivity, BranchLocationActivity::class.java)
                    intent.putExtra("issue", "branch")
                    startActivity(intent)
                }
                2 -> {
                    val intent = Intent(this@LocatorActivity, BranchLocationActivity::class.java)
                    intent.putExtra("issue", "atm")
                    startActivity(intent)
                }

                1 -> {
                    startActivity(Intent(this@LocatorActivity, SMSBankingActivity::class.java))
                }
            }
        }
    }
}
