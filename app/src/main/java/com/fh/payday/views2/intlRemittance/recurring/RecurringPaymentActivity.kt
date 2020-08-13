package com.fh.payday.views2.intlRemittance.recurring

import android.content.Intent
import android.support.design.widget.BottomSheetDialog
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.models.intlRemittance.RecurringPayment
import com.fh.payday.utilities.OnItemClickListener
import com.fh.payday.views.adapter.SelectLanguageAdapter
import com.fh.payday.views2.intlRemittance.transfer.TransferActivity
import com.fh.payday.views2.loan.apply.TermsAndConditionsDialog
import kotlinx.android.synthetic.main.activity_recurring_payment.*
import kotlinx.android.synthetic.main.toolbar.*

class RecurringPaymentActivity : BaseActivity(), OnItemClickListener {

    var bottomSheet: BottomSheetDialog? = null
    override fun getLayout(): Int = R.layout.activity_recurring_payment

    override fun init() {
        setUpToolbar()
        handleBottomBar()
        setUpRecyclerView()

        btn_make_payment.setOnClickListener {
            startActivity(Intent(this, TransferActivity::class.java).also {
                it.putExtra("type", "recurring")
            })
        }
    }

    private fun setUpToolbar() {
        toolbar_back.setOnClickListener(this)
        toolbar_title.text = getString(R.string.recurring_payment)
    }

    private fun setUpRecyclerView() {
        rvTransactions.layoutManager = LinearLayoutManager(this)
        rvTransactions.adapter = RecurringPaymentsAdapter(listPayments, listPaymentHistory, this)
    }


    private fun handleBottomBar() {
        findViewById<TextView>(R.id.btm_home).setOnClickListener(this)
        findViewById<TextView>(R.id.btm_menu_branch).setOnClickListener(this)
        findViewById<TextView>(R.id.btm_menu_support).setOnClickListener(this)
        findViewById<TextView>(R.id.btm_menu_loan_calc).setOnClickListener(this)
        findViewById<TextView>(R.id.toolbar_help).setOnClickListener(this)
    }


    private val listPayments = listOf(
            RecurringPayment("Amir Paul", "J&K Bank", "22000"),
            RecurringPayment("Sajid Khaki", "J&K Bank", "23000"),
            RecurringPayment("Muheeb Mehraj", "J&K Bank", "24000")
    )

    private val listPaymentHistory = listOf(
            RecurringPayment("Amir Paul", "IDFC", "22000"),
            RecurringPayment("Sajid Khaki", "IDFC Bank", "23000"),
            RecurringPayment("Muheeb Mehraj", "IDFC Bank", "24000")
    )

    override fun onItemClick(index: Int) {
        showBottomSheet()
    }

    val listener: OnItemClickListener = object : OnItemClickListener {
        override fun onItemClick(index: Int) {
            if (index == 0) {
                showConfirmationDialog()
            }
        }
    }

    private fun showBottomSheet() {

        val view = LayoutInflater.from(this).inflate(R.layout.layout_select_exchange, findViewById(android.R.id.content), false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)

        recyclerView.adapter = SelectLanguageAdapter(listener, getOptions())

        bottomSheet = BottomSheetDialog(this)
        bottomSheet?.setContentView(view)
        (view.parent as View).setBackgroundColor(ContextCompat.getColor(this, R.color.transparent))
        bottomSheet?.show()

        bottomSheet?.setOnDismissListener {
            bottomSheet = BottomSheetDialog(this)
            bottomSheet?.dismiss()
        }
        bottomSheet?.setOnCancelListener {
            bottomSheet = BottomSheetDialog(this)
            bottomSheet?.dismiss()
        }
    }

    private fun showConfirmationDialog() {

        val termsAndCondDialog = TermsAndConditionsDialog.Builder()
                .setMessage(getString(R.string.cancel_recurring_payment))
                .setMessageGravity(Gravity.CENTER)
                .setPositiveText(getString(R.string.yes))
                .setNegativeText(getString(R.string.cancel))
                .attachPositiveListener {
                    bottomSheet?.dismiss()
                }
                .build()

        val fm = supportFragmentManager ?: return

        termsAndCondDialog.apply {
            isCancelable = false
            show(fm, termsAndCondDialog.tag)
        }
    }

    private fun getOptions(): Array<String> {
        return arrayOf("Cancel Payment", "View Details")
    }
}
