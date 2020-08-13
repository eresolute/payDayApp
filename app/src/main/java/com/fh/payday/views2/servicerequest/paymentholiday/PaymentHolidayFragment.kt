package com.fh.payday.views2.servicerequest.paymentholiday

import android.app.Activity
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.datasource.models.CustomerSummary
import com.fh.payday.datasource.models.Loan
import com.fh.payday.datasource.models.PaymentHoliday
import com.fh.payday.datasource.models.shared.ListModel
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.*
import com.fh.payday.views.fragments.OTPFragment
import com.fh.payday.views.shared.ListAdapter
import com.fh.payday.views2.loan.apply.ApplyLoanActivity
import com.fh.payday.views2.loan.apply.TermsAndConditionsDialog
import com.fh.payday.views2.servicerequest.ServiceRequestOptionActivity
import com.fh.payday.views2.shared.custom.AlertDialogFragment
import com.fh.payday.views2.shared.fragments.TermsConditionsDialogFragment
import java.util.*

class PaymentHolidayFragment : Fragment(), OnOTPConfirmListener {

    private lateinit var cbTerms: CheckBox
    private lateinit var activity: ServiceRequestOptionActivity

    private var recyclerView: RecyclerView? = null
    private lateinit var tvNoLoan: TextView
    private lateinit var tvTermsCodn: TextView
    private lateinit var btnApplyLoan: Button
    private lateinit var summary: CustomerSummary

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context as ServiceRequestOptionActivity

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_payment_holiday, container, false)

        init(view)
        addDefermentObserver()
        addOTPObserver()
        addHolidayObserver()

        val user = activity.viewModel.user
        if (user != null && activity.viewModel.summary == null) {
            activity.viewModel.fetchCustomerSummary(user.token, user.sessionId, user.refreshToken, user.customerId.toLong())
            addObserver()
        } else {
            activity.viewModel.summary?.let {
                summary = it
                handleLoanInfo(it.loans)
            }
        }
        return view
    }

    private fun init(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view)
        tvNoLoan = view.findViewById(R.id.tv_no_loan)
        tvTermsCodn = view.findViewById(R.id.tv_terms_condition_link)
        btnApplyLoan = view.findViewById(R.id.btn_apply_loan)
        cbTerms = view.findViewById(R.id.cb_term_conditions)

        view.findViewById<TextView>(R.id.tv_terms_condition_link).apply {
            setTextWithUnderLine(text.toString())
            setOnClickListener {
                val dialogFragment: DialogFragment = TermsConditionsDialogFragment
                        .newInstance(LOAN_TC_URL, getString(R.string.close))
                dialogFragment.show(activity.supportFragmentManager, "dialog")
            }
        }

        btnApplyLoan.setOnClickListener {

            val termsAndCondDialog = TermsAndConditionsDialog.Builder()
                    .setTermsConditionsLink(true)
                    .attachPositiveListener {
                        activity.finish()
                        startActivity(Intent(activity, ApplyLoanActivity::class.java))
                    }
                    .attachLinkListener {
                        val dialogFragment: DialogFragment = TermsConditionsDialogFragment
                                .newInstance(LOAN_TC_URL, getString(R.string.close))
                        dialogFragment.show(activity.supportFragmentManager, "dialog")
                    }

                    .build()

            val supportFragmentManager = activity.supportFragmentManager

            termsAndCondDialog.apply {
                isCancelable = false
                show(supportFragmentManager, termsAndCondDialog.tag)
            }
        }
    }

    private fun acceptTerms(): Boolean {
        if (!cbTerms.isChecked) {
            activity.onFailure(activity.findViewById(R.id.root_view) ?: return false,
                    getString(R.string.terms_conditions_error))
            return false
        }
        return true
    }

    private fun addObserver() {
        activity.viewModel.customerSummaryState.observe(this, Observer {
            it ?: return@Observer

            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing))
                return@Observer
            }

            activity.hideProgress()

            when (state) {
                is NetworkState2.Success -> {
                    state.data?.loans?.let { loans ->
                        handleLoanInfo(loans)
                    }
                }
                is NetworkState2.Error -> {
                    val error = state as NetworkState2.Error<*>
                    if (error.isSessionExpired) {
                        return@Observer activity.onSessionExpired(error.message)
                    }
                    activity.handleErrorCode(error.errorCode.toInt(), error.message)
                }
                is NetworkState2.Failure -> {
                    val error = state as NetworkState2.Failure<*>
                    activity.onFailure(activity.findViewById(R.id.root_view)!!, error.throwable)
                }
                else -> {
                    activity.onFailure(activity.findViewById(R.id.root_view)!!, CONNECTION_ERROR)
                }
            }
        })
    }

    private fun addDefermentObserver() {
        activity.viewModel.deferLoanState.observe(this, Observer {
            it ?: return@Observer

            val state = it.getContentIfNotHandled() ?: return@Observer
            if (state is NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing))
                return@Observer
            }

//            activity.hideProgress()

            when (state) {
                is NetworkState2.Success -> {
                    val activity = activity
                    val user = activity.viewModel.user ?: return@Observer
                    activity.viewModel.generateOtp(user.token, user.sessionId, user.refreshToken, user.customerId.toLong())
                }
                is NetworkState2.Error -> {
                    val error = state as NetworkState2.Error<*>
                    if (error.isSessionExpired) {
                        return@Observer activity.onSessionExpired(error.message)
                    }

                    activity.handleErrorCode(error.errorCode.toInt(), error.message)
                }
                is NetworkState2.Failure -> {
                    val error = state as NetworkState2.Failure<*>
                    activity.onFailure(activity.findViewById(R.id.root_view)!!, error.throwable)
                }
                else -> {
                    activity.onFailure(activity.findViewById(R.id.root_view)!!, CONNECTION_ERROR)
                }
            }

        })
    }

    override fun onOtpConfirm(otp: String) {
        val activity = activity
        val user = activity.viewModel.user ?: return

        if (activity.viewModel.summary == null) return
        activity.viewModel.paymentHoliday(user.token, user.sessionId, user.refreshToken, user.customerId.toLong(), otp,
                activity.viewModel.summary?.loans?.get(0)?.loanNumber ?: return,
                1)
    }

    private fun addOTPObserver() {

        val activity1 = activity

        activity1.viewModel.otpRequestState.observe(this, Observer {
            it ?: return@Observer
            val activity = activity

            val otpState = it.getContentIfNotHandled() ?: return@Observer

            if (otpState is NetworkState2.Loading) {
//                activity.showProgress(getString(R.string.processing))
                return@Observer
            }

            activity.hideProgress()

            when (otpState) {
                is NetworkState2.Success -> {
                    activity.replaceFragment(
                            OTPFragment.Builder(this)
                                    .setPinLength(6)
                                    .setTitle(getString(R.string.enter_otp))
                                    .setInstructions(null)
                                    .setHasCardView(false)
                                    .setOnResumeListener { activity.setToolbarText(getString(R.string.verify_otp)) }
                                    .setButtonTitle(getString(R.string.submit))
                                    .build())
                }
                is NetworkState2.Error -> {
                    val (message) = otpState
                    if (otpState.isSessionExpired) {
                        return@Observer activity.onSessionExpired(otpState.message)
                    }
                    activity.onError(message)
                }
                is NetworkState2.Failure -> {
                    activity.onFailure(activity.findViewById(R.id.root_view), getString(R.string.request_error))
                }
                else -> activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    private fun addHolidayObserver() {
        val activity1 = activity

        activity1.viewModel.holidayState.observe(activity1, Observer {
            it ?: return@Observer
            val activity = activity

            val holidayState = it.getContentIfNotHandled() ?: return@Observer

            if (holidayState is NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing))
                return@Observer
            }

            activity.hideProgress()

            when (holidayState) {
                is NetworkState2.Success -> {
                    AlertDialogFragment.Builder()
                            .setMessage(holidayState.data!!)
                            .setIcon(R.drawable.ic_success_checked_blue)
                            .setCancelable(false)
                            .setConfirmListener { dialog ->
                                dialog.dismiss()
                                activity.finish()
                            }
                            .build()
                            .show(fragmentManager, "")
                }
                is NetworkState2.Error -> {
                    if (holidayState.isSessionExpired) {
                        return@Observer activity.onSessionExpired(holidayState.message)
                    }

                    activity.handleErrorCode(holidayState.errorCode.toInt(), holidayState.message)
                }
                is NetworkState2.Failure -> {
                    activity.onFailure(activity.findViewById(R.id.root_view), getString(R.string.request_error))
                }
                else -> activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == ServiceRequestOptionActivity().REQUEST_UPLOAD_TICKET) {
            activity.replaceFragment(PaymentHolidayInstallmentFragment())
        }
    }

    private fun handleLoanInfo(loans: List<Loan>) {
        if (isEmptyList(loans)) {
            tvNoLoan.visibility = View.VISIBLE
            btnApplyLoan.visibility = View.GONE
            recyclerView?.visibility = View.GONE
            cbTerms.visibility = View.GONE
            tvTermsCodn.visibility = View.GONE
            return
        }

        tvTermsCodn.visibility = View.VISIBLE
        cbTerms.visibility = View.VISIBLE
        btnApplyLoan.visibility = View.VISIBLE
        btnApplyLoan.text = getString(R.string.apply_for_deferment)
        val data = loans.map {
            val emi: Float = if (it.nextEMIDueAmount.isNullOrEmpty()) 0.00F else it.nextEMIDueAmount?.toFloat()
                    ?: 0.00F
            val due: Float = if (it.monthlyInstallment.isNullOrEmpty()) 0.00F else it.monthlyInstallment?.toFloat()
                    ?: 0.00F
            val loanNumber = if (TextUtils.isEmpty(it.loanNumber)) null else it.loanNumber
            PaymentHoliday(it.loanType, emi, due, loanNumber)
        }

        btnApplyLoan.setOnClickListener {

            if (acceptTerms()) {
                val viewModel = activity.viewModel ?: return@setOnClickListener
                val summary = viewModel.summary ?: return@setOnClickListener
                if (TextUtils.isEmpty(summary.loans[0].loanNumber)) return@setOnClickListener
                val user = UserPreferences.instance.getUser(activity) ?: return@setOnClickListener
                activity.viewModel.deferLoan(user.token, user.sessionId, user.refreshToken, user.customerId.toLong())
            }
        }

        recyclerView?.adapter = ListAdapter(getLoanDetails(loans[0]), context, getString(R.string.loan_details))
        recyclerView?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    private fun getLoanDetails(loan: Loan): List<ListModel> {
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
}