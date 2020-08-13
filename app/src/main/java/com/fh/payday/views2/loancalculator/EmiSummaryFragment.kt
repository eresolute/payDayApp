package com.fh.payday.views2.loancalculator

import android.content.Context
import android.os.Bundle
import android.support.design.button.MaterialButton
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.utilities.getDecimalValue

class EmiSummaryFragment : Fragment() {

    lateinit var activity: LoanCalculatorActivity
    lateinit var tvEmi: TextView
    lateinit var tvPrincipalAmount: TextView
    lateinit var tvInterestPayable: TextView
    lateinit var tvTotalPayableAmount: TextView

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = when (context) {
            is LoanCalculatorActivity -> context
            else -> throw IllegalArgumentException("Illegal context")
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            calculateEmi()
        }
    }

    private fun calculateEmi() {
        val principalAmount = activity.viewModel.loanAmount
        val tenure = activity.viewModel.tenure
        val interest = activity.viewModel.interestRate

        val emi = activity.viewModel.calculateLoanEmi(principalAmount.toDouble(), interest.toDouble(), tenure * 12)
        val payableAmount = emi * tenure * 12
        val interestAmount = payableAmount - principalAmount

        tvEmi.text = String.format(getString(R.string.amount_in_aed, emi.toString().getDecimalValue()))
        tvInterestPayable.text = String.format(getString(R.string.amount_in_aed, interestAmount.toString().getDecimalValue()))
        tvTotalPayableAmount.text = String.format(getString(R.string.amount_in_aed, payableAmount.toString().getDecimalValue()))
        tvPrincipalAmount.text = String.format(getString(R.string.amount_in_aed, principalAmount.toString().getDecimalValue()))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(container?.context).inflate(R.layout.fragment_emi_summary, container, false)
        init(view)
        return view
    }

    fun init(view: View) {
        tvEmi = view.findViewById(R.id.tv_emi)
        tvPrincipalAmount = view.findViewById(R.id.tv_principal_amount)
        tvInterestPayable = view.findViewById(R.id.tv_interest_amount)
        tvTotalPayableAmount = view.findViewById(R.id.tv_total_amount)
        view.findViewById<MaterialButton>(R.id.btn_emi_again).setOnClickListener {
            activity.onCalculateAgain()
        }

    }
}