package com.fh.payday.views2.accountsummary

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.fh.payday.BaseFragment
import com.fh.payday.R
import com.fh.payday.datasource.models.Card
import com.fh.payday.datasource.models.Loan
import com.fh.payday.datasource.models.shared.ListModel
import com.fh.payday.utilities.*
import com.fh.payday.utilities.AccessMatrix.Companion.APPLY_TOPUP_LOAN
import com.fh.payday.viewmodels.LoanViewModel
import com.fh.payday.views.shared.ListAdapter
import com.fh.payday.views2.loan.apply.ApplyLoanActivity
import com.fh.payday.views2.loan.apply.LoanDetailActivity
import com.fh.payday.views2.loan.apply.TermsAndConditionsDialog
import com.fh.payday.views2.shared.fragments.TermsConditionsDialogFragment
import java.util.*

class LoanDetailsFragment : BaseFragment() {

    private lateinit var tvCustomerName: TextView
    private lateinit var tvLoanNumber: TextView
    private lateinit var tvLoanAmount: TextView
    private lateinit var btnApply: Button
    private lateinit var cards: List<Card>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_loan_details, container, false)
        init(view)
        return view
    }

    private fun init(view: View) {

        tvLoanNumber = view.findViewById(R.id.tv_loan_no)
        tvLoanAmount = view.findViewById(R.id.tv_loan_amount)
        tvCustomerName = view.findViewById(R.id.tv_customer_name)
        btnApply = view.findViewById(R.id.btn_apply_loan_topup)
        val intent = Intent(context, ApplyLoanActivity::class.java)
        var cardStatus: String? = null
        btnApply.setOnClickListener {
            if (isEmptyList(cards) || hasAccess(cardStatus, APPLY_TOPUP_LOAN))
                showTermsConditions(intent)
            else activity?.showCardStatusError(cardStatus)
        }

        getViewModel()?.summary?.observe(this, Observer {
            btnApply.isEnabled = false
            if (it == null) return@Observer
            cards = it.cards
            if (it.cards.isNotEmpty()) {
                val card = it.cards[0]
                cardStatus = card.cardStatus
                tvCustomerName.text = card.cardName
            }

            if (it.loans.isEmpty()) {
                handleNoLoanAvailable(view)
                return@Observer
            }

            val loan = it.loans[0]

            intent.putExtra("product_type", LoanViewModel.TOPUP_LOAN)
                    .putExtra("loan_number", loan.loanNumber)

            btnApply.isEnabled = true

            val rvLoanDetails = view.findViewById<RecyclerView>(R.id.rv_loan_detail)
            val adapter = ListAdapter(getLoanDetails(loan), context, getString(R.string.loan_details))
            rvLoanDetails.adapter = adapter
            rvLoanDetails.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        })
    }

    private fun handleNoLoanAvailable(view: View) {
        view.findViewById<View>(R.id.cl_no_loan_available).visibility = View.VISIBLE
    }

    private fun getLoanDetails(loan: Loan): List<ListModel> {

        tvLoanNumber.text = loan.loanNumber
        tvLoanAmount.text = String.format(getString(R.string.amount_in_aed), loan.originalLoanAmount.getDecimalValue())

        val list = ArrayList<ListModel>()
        list.add(ListModel(getString(R.string.loan_type), loan.loanType))
        list.add(ListModel(getString(R.string.loan_number), loan.loanNumber))
        list.add(ListModel(getString(R.string.loan_disbursal_date), DateTime.parse(loan.loanDisbursalDate)))
        list.add(ListModel(getString(R.string.original_loan_amount), String.format(getString(R.string.amount_in_aed), loan.originalLoanAmount.getDecimalValue())))
        list.add(ListModel(getString(R.string.no_installments_paid), (if (loan.noOfInstallmentsPaid != null) loan.noOfInstallmentsPaid else "-")!!))
        list.add(ListModel(getString(R.string.interest_rate), String.format(getString(R.string.percentage), loan.interestRate)))
        list.add(ListModel(getString(R.string.maturity_date), DateTime.parse(loan.maturityDate)))
        val outstandingAmount = loan.outstandingAmount ?: ""
        list.add(
                ListModel(getString(R.string.principal_outstanding),
                        (if (loan.outstandingAmount != null) String.format(getString(R.string.amount_in_aed,
                                outstandingAmount.getDecimalValue())) else "-"))
        )
        list.add(ListModel(getString(R.string.outstanding_installments), loan.outstandingInstallments))

        val monthlyInstallment = loan.monthlyInstallment ?: ""
        list.add(ListModel(getString(R.string.monthly_installments),
                (if (loan.monthlyInstallment != null) {
                    String.format(getString(R.string.amount_in_aed, monthlyInstallment.getDecimalValue()))
                } else
                    "-")))

        var nextInstallmentDate = loan.nextInstallmentDate ?: ""
        nextInstallmentDate = if (nextInstallmentDate.isNotEmpty()) nextInstallmentDate else "-"
        list.add(ListModel(getString(R.string.next_installment_date), nextInstallmentDate))

        return list
    }


    private fun getViewModel() = when (activity) {
        is AccountSummaryActivity -> {
            (activity as AccountSummaryActivity).viewModel
        }
        is LoanDetailActivity -> {
            (activity as LoanDetailActivity).viewModel
        }
        else -> {
            null
        }
    }

    private fun showTermsConditions(intent: Intent) {
        val fragmentManager = activity?.supportFragmentManager ?: return

        val termsAndCondDialog = TermsAndConditionsDialog.Builder()
                .attachPositiveListener {
                    startActivity(intent)
                }
                .attachLinkListener {
                    val dialogFragment: DialogFragment = TermsConditionsDialogFragment
                            .newInstance(LOAN_TC_URL, getString(R.string.close))
                    if (activity == null) return@attachLinkListener
                    dialogFragment.show(activity?.supportFragmentManager, "dialog")
                }
                .setTermsConditionsLink(true)
                .build()

        termsAndCondDialog.apply {
            isCancelable = false
            show(fragmentManager, termsAndCondDialog.tag)
        }
    }
}
