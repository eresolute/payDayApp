package com.fh.payday.views2.support

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Window
import android.widget.Button
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.DataGenerator
import com.fh.payday.utilities.OnItemClickListener
import com.fh.payday.views.fragments.AlertDialogFragment
import com.fh.payday.views2.dashboard.MainActivity
import com.fh.payday.views2.help.HelpActivity
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity
import com.fh.payday.views2.locator.LocatorActivity
import com.fh.payday.views2.message.ContactUsActivity
import com.fh.payday.views2.support.adapter.SupportIssueAdapter
import kotlinx.android.synthetic.main.toolbar.*

class SupportActivity : BaseActivity(), OnItemClickListener {

    private lateinit var etMessage: TextInputEditText
    private lateinit var btnSubmit: Button
    private lateinit var tvIssue: TextView
    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tvIssue.setOnClickListener {
            showIssueDialog()
        }

        btnSubmit.setOnClickListener {
            val dialogFragment = AlertDialogFragment.newInstance(
                    "Request No. 5323422",
                    R.drawable.ic_success_checked, R.color.blue
            ) { finish() }
            dialogFragment.show(supportFragmentManager, "Dialog")
        }
    }

    override fun init() {

        toolbar_title.setText(R.string.support)
        toolbar_back.setOnClickListener { onBackPressed() }

        tvIssue = findViewById(R.id.tv_issue)
        btnSubmit = findViewById(R.id.btn_submit)
        etMessage = findViewById(R.id.et_message)

        handleBottomBar()
    }


    override fun getLayout(): Int {
        return R.layout.activity_support
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

    private fun showIssueDialog() {

        val view = LayoutInflater.from(this).inflate(R.layout.layout_issue, null, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val listOfIssues = DataGenerator.getIssues()

        val supportIssueAdapter = SupportIssueAdapter(listOfIssues, this)
        recyclerView.adapter = supportIssueAdapter

        dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(view)
        dialog.setOnCancelListener { it.dismiss() }
        dialog.show()
    }

    override fun onItemClick(index: Int) {
        val issueItem = DataGenerator.getIssues()
        tvIssue.text = issueItem[index].name
        dialog.dismiss()
    }
}
