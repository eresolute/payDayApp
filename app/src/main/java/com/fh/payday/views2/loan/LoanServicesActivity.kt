package com.fh.payday.views2.loan

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.models.CustomerSummary
import com.fh.payday.datasource.models.Item
import com.fh.payday.utilities.*
import com.fh.payday.utilities.AccessMatrix.Companion.CLEARANCE_LETTER
import com.fh.payday.utilities.AccessMatrix.Companion.EARLY_SETTLEMENT
import com.fh.payday.utilities.AccessMatrix.Companion.LIABILITY_LETTER
import com.fh.payday.utilities.AccessMatrix.Companion.PARTIAL_SETTLEMENT
import com.fh.payday.utilities.AccessMatrix.Companion.PAYMENT_HOLIDAY
import com.fh.payday.utilities.AccessMatrix.Companion.RESCHEDULE
import com.fh.payday.viewmodels.LoanViewModel
import com.fh.payday.views.adapter.OperatorAdapter
import com.fh.payday.views2.loan.apply.ApplyLoanActivity
import com.fh.payday.views2.loan.apply.TermsAndConditionsDialog
import com.fh.payday.views2.servicerequest.ServiceRequestOptionActivity
import com.fh.payday.views2.shared.fragments.TermsConditionsDialogFragment
import kotlinx.android.synthetic.main.activity_loan_services.*
import kotlinx.android.synthetic.main.bottombar_dashboard.*
import kotlinx.android.synthetic.main.toolbar.*

class LoanServicesActivity : BaseActivity(), OnItemClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        toolbar_back.setOnClickListener(this)
        btm_home.setOnClickListener(this)
        btm_menu_branch.setOnClickListener(this)
        btm_menu_support.setOnClickListener(this)
        btm_menu_loan_calc.setOnClickListener(this)

        toolbar_help.setOnClickListener {
            startHelpActivity("loanServicesHelp")
        }
    }

    override fun getLayout() = R.layout.activity_loan_services

    override fun init() {
        toolbar_title.setText(R.string.loan_services_title)
        recycler_view.adapter = OperatorAdapter(this, getItems(), 3)
        recycler_view.addItemDecoration(ItemOffsetDecoration(2))
    }

    private fun getItems(): List<Item> {
        val summary = intent?.getParcelableExtra("summary") as CustomerSummary
        var textItems = resources.getStringArray(R.array.loan_services)
        var icons = resources.obtainTypedArray(R.array.loan_services_icons_topup)

        if (isEmptyList(summary.cards) || (!isEmptyList(summary.cards) && (!isEmptyList(summary.loans)))) {
            textItems = resources.getStringArray(R.array.loan_services_topup)
            icons = resources.obtainTypedArray(R.array.loan_services_icons_topup)
        }

        val items = ArrayList<Item>()

        for (i in textItems.indices) {
            items.add(Item(textItems[i], icons.getResourceId(i, 0)))
        }

        icons.recycle()
        //items.apply { add(Item("", 0)) }
        return items
    }

    override fun onItemClick(index: Int) {
        val loanNumber = intent?.getStringExtra("loan_number")
        val summary = intent?.getParcelableExtra("summary") as CustomerSummary
        val cardStatus = if (isNotEmptyList(summary.cards)) summary.cards[0].cardStatus?.toLowerCase() else null

        when (index) {
            /*0 -> {
                if (summary.cards.isNullOrEmpty() && summary.loans.isNullOrEmpty())
                    return

                val service = when {
                    !loanNumber.isNullOrEmpty() && !summary.loans.isNullOrEmpty() ->
                        APPLY_TOPUP_LOAN
                    else -> APPLY_LOAN
                }
                if (isEmptyList(summary.cards) || hasAccess(cardStatus, service))
                    showTermsConditions(summary, loanNumber)
                else
                    showCardStatusError(cardStatus)
            }*/
            0 -> {
                if (isEmptyList(summary.cards) || hasAccess(cardStatus, PAYMENT_HOLIDAY)) {
                    val intent = Intent(this, ServiceRequestOptionActivity::class.java).apply {
                        putExtra("index", 11)
                    }
                    startActivity(intent)
                } else showCardStatusError(cardStatus)
            }
            1 -> {

                if (isEmptyList(summary.cards) || hasAccess(cardStatus, RESCHEDULE)) {
                    val intent = Intent(this, LoanServiceRequestActivity::class.java)
                    intent.putExtra("label", LoanServiceRequestActivity.TYPE.RESCHEDULE)
                    intent.putExtra("loan_number", loanNumber)
                    intent.putExtra("card", cardStatus)
                    startActivity(intent)
                } else showCardStatusError(cardStatus)
            }
            2 -> {
                if (isEmptyList(summary.cards) || hasAccess(cardStatus, CLEARANCE_LETTER)) {
                    val intent = Intent(this, LoanServiceRequestActivity::class.java)
                    intent.putExtra("label", LoanServiceRequestActivity.TYPE.CLEARANCE)
                    intent.putExtra("loan_number", loanNumber)
                    intent.putExtra("card", cardStatus)
                    startActivity(intent)
                } else showCardStatusError(cardStatus)
            }
            3 -> {
                if (isEmptyList(summary.cards) || hasAccess(cardStatus, EARLY_SETTLEMENT)) {
                    val intent = Intent(this, LoanServiceRequestActivity::class.java)
                    intent.putExtra("label", LoanServiceRequestActivity.TYPE.E_SETTLEMENT)
                    intent.putExtra("loan_number", loanNumber)
                    intent.putExtra("card", cardStatus)
                    startActivity(intent)
                } else showCardStatusError(cardStatus)
            }
            5 -> {
                if (isEmptyList(summary.cards) || hasAccess(cardStatus, LIABILITY_LETTER)) {
                    val intent = Intent(this, LoanServiceRequestActivity::class.java)
                    intent.putExtra("label", LoanServiceRequestActivity.TYPE.LIABILITY)
                    intent.putExtra("loan_number", loanNumber)
                    intent.putExtra("card", cardStatus)
                    startActivity(intent)
                } else showCardStatusError(cardStatus)
            }
            4 -> {
                if (isEmptyList(summary.cards) || hasAccess(cardStatus, PARTIAL_SETTLEMENT)) {
                    val intent = Intent(this, LoanServiceRequestActivity::class.java)
                    intent.putExtra("label", LoanServiceRequestActivity.TYPE.P_SETTLEMENT)
                    intent.putExtra("loan_number", loanNumber)
                    intent.putExtra("card", cardStatus)
                    startActivity(intent)
                } else showCardStatusError(cardStatus)
            }
        }
    }

    private fun showTermsConditions(summary: CustomerSummary, loanNumber: String?) {
        val termsAndCondDialog = TermsAndConditionsDialog.Builder()
                .setTermsConditionsLink(true)
                .attachPositiveListener {
                    if (summary.cards.isNullOrEmpty() && summary.loans.isNullOrEmpty())
                        return@attachPositiveListener

                    when {
                        !loanNumber.isNullOrEmpty() && !summary.loans.isNullOrEmpty() ->
                            startLoanTopActivity(loanNumber)
                        else -> startActivity(Intent(this, ApplyLoanActivity::class.java))
                    }
                }
                .attachLinkListener {
                    val dialogFragment: DialogFragment = TermsConditionsDialogFragment
                            .newInstance(LOAN_TC_URL, getString(R.string.close))
                    dialogFragment.show(supportFragmentManager, "dialog")
                }
                .build()

        termsAndCondDialog.apply {
            isCancelable = false
            show(supportFragmentManager, termsAndCondDialog.tag)
        }
    }

    private fun startLoanTopActivity(loanNumber: String) {
        val intent = Intent(this, ApplyLoanActivity::class.java).apply {
            putExtra("product_type", LoanViewModel.TOPUP_LOAN)
            putExtra("loan_number", loanNumber)
        }
        startActivity(intent)
    }


}
