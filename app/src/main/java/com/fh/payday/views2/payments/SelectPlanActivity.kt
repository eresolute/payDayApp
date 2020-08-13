package com.fh.payday.views2.payments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.models.Plan
import com.fh.payday.utilities.OnItemClickListener
import com.fh.payday.views.adapter.IBPPlanAdapter
import com.fh.payday.views.adapter.PlanAdapter
import kotlinx.android.synthetic.main.activity_select_plan.*
import kotlinx.android.synthetic.main.bottombar_dashboard.*
import kotlinx.android.synthetic.main.toolbar.*

class SelectPlanActivity : BaseActivity(), OnItemClickListener {

    private var plans: ArrayList<Plan>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        toolbar_back.setOnClickListener(this)
        btm_home.setOnClickListener(this)
        btm_menu_branch.setOnClickListener(this)
        btm_menu_support.setOnClickListener(this)
        btm_menu_loan_calc.setOnClickListener(this)

        toolbar_help.setOnClickListener {
            startHelpActivity("paymentScreenHelp")
        }
    }

    override fun getLayout() = R.layout.activity_select_plan

    override fun init() {
        toolbar_back.setOnClickListener { onBackPressed() }
        toolbar_title.text = getString(R.string.select_plan)
        plans = intent.getParcelableArrayListExtra<Plan>("plans") ?: return finish()
        val planProvider = intent.getStringExtra("plan_provider") ?: null
        if ("ibp".equals(planProvider, true)) {
            recycler_view.layoutManager = LinearLayoutManager(this)
            recycler_view.adapter = IBPPlanAdapter(this, plans ?: return, -1)
        } else {
            recycler_view.layoutManager = GridLayoutManager(this, 3)
            recycler_view.adapter = PlanAdapter(this, plans ?: return, -1)
        }
    }

    override fun onItemClick(index: Int) {
        plans?.let {
            val returnIntent = Intent()
            returnIntent.apply {
                putExtra("data", it[index])
                setResult(Activity.RESULT_OK, returnIntent)
            }
            finish()
        }
    }
}
