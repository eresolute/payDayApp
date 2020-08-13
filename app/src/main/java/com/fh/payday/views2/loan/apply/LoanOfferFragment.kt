package com.fh.payday.views2.loan.apply

import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.models.loan.LoanOffer
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.LOAN_TC_URL
import com.fh.payday.utilities.getDecimalValue
import com.fh.payday.utilities.setTextWithUnderLine
import com.fh.payday.viewmodels.LoanViewModel
import com.fh.payday.views2.shared.fragments.TermsConditionsDialogFragment

class LoanOfferFragment : Fragment() {

    private var activity: BaseActivity? = null
    private lateinit var btnAccept: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var checkBox: CheckBox
    private lateinit var tvLoanType: TextView
    private lateinit var tvLoanNumber: TextView
    private lateinit var tvMonthlyInstallments: TextView
    private lateinit var tvOriginalLoanAmount: TextView
    private lateinit var tvInterestRate: TextView

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context as BaseActivity?
    }

    override fun onDetach() {
        super.onDetach()
        activity = null
    }

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_loan_offers, parent, false)
        btnAccept = view.findViewById(R.id.btn_accept)
        progressBar = view.findViewById(R.id.progress_bar)
        checkBox = view.findViewById(R.id.cb_term_conditions)
        tvLoanType = view.findViewById(R.id.tv_loanType)
        tvLoanNumber = view.findViewById(R.id.tv_loanNumber)
        tvMonthlyInstallments = view.findViewById(R.id.tv_monthlyInstallment)
        tvOriginalLoanAmount = view.findViewById(R.id.tv_originalLoanAmount)
        tvInterestRate = view.findViewById(R.id.tv_interestRate)

        view.findViewById<TextView>(R.id.tv_terms_conditions).apply {
            setTextWithUnderLine(text.toString())
            setOnClickListener {
                val dialogFragment: DialogFragment = TermsConditionsDialogFragment
                        .newInstance(LOAN_TC_URL, getString(R.string.close))
                if (activity == null) return@setOnClickListener
                dialogFragment.show(activity?.supportFragmentManager, "dialog")
            }
        }

        attachObserver()

        return view
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if (isVisibleToUser) {
            val loanOffer = getLoanOffer() ?: return

            tvLoanType.text = getString(R.string.personal_loan)
            tvLoanNumber.text = loanOffer.applicationId
            tvOriginalLoanAmount.text = String.format(getString(R.string.amount_in_aed), loanOffer.approvedAmount.getDecimalValue())
            tvMonthlyInstallments.text = loanOffer.tenure
            tvInterestRate.text = String.format(getString(R.string.percentage), loanOffer.interestRate)

            btnAccept.setOnClickListener {
                if (!checkBox.isChecked) {
                    val view = activity?.findViewById<View>(R.id.root_view) ?: return@setOnClickListener
                    activity?.onFailure(view, getString(R.string.terms_conds_error))
                    return@setOnClickListener
                }

                generateOtp()
            }
        }
    }

    private fun getViewModel(): LoanViewModel? {
        val activity = activity ?: return null
        return when(activity) {
            is ApplyLoanActivity -> activity.viewModel
            else -> null
        }
    }

    private fun getLoanOffer(): LoanOffer? {
        val activity = activity ?: return null
        return when (activity) {
            is ApplyLoanActivity -> activity.viewModel.customizedLoanOffer
            else -> null
        }
    }

    private fun generateOtp() {
        val activity = activity ?: return

        when(activity) {
            is ApplyLoanActivity -> {
                val user = activity.viewModel.user ?: return
                activity.viewModel.generateOtp(user.token, user.sessionId, user.refreshToken, user.customerId.toLong())
            }
        }
    }

    private fun attachObserver() {
        getViewModel()?.generateOtpState?.observe(this, Observer {
            it ?: return@Observer
            val activity = activity ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                btnAccept.visibility = View.INVISIBLE
                progressBar.visibility = View.VISIBLE
                return@Observer
            }

            btnAccept.visibility = View.VISIBLE
            progressBar.visibility = View.GONE

            when (state) {
                is NetworkState2.Success -> onSuccess()
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        return@Observer activity.onSessionExpired(state.message)
                    }
                    activity.onError(state.message)
                }
                is NetworkState2.Failure -> activity.onFailure(activity.findViewById(R.id.root_view),
                    getString(R.string.request_error))
                else -> activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    private fun onSuccess() {
        val activity = activity ?: return
        when (activity) {
            is ApplyLoanActivity -> activity.navigateUp()
        }
    }

}
