package com.fh.payday.views2.servicerequest.paymentholiday

import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.LOAN_TC_URL
import com.fh.payday.utilities.OnOTPConfirmListener
import com.fh.payday.utilities.setTextWithUnderLine
import com.fh.payday.views.fragments.OTPFragment
import com.fh.payday.views2.servicerequest.ServiceRequestOptionActivity
import com.fh.payday.views2.shared.custom.AlertDialogFragment
import com.fh.payday.views2.shared.fragments.TermsConditionsDialogFragment

class PaymentHolidayConditionFragment : Fragment(), OnOTPConfirmListener {

    private lateinit var rootView: ConstraintLayout
    private lateinit var btnAccept: Button
    private lateinit var tvInstallment: TextView
    private lateinit var cbTerms: CheckBox
    private var installments: String? = null
    private var activity: ServiceRequestOptionActivity? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context as ServiceRequestOptionActivity?
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.payment_holiday_condition_fragment, container, false)

        init(view)
        addOTPObserver()
        addHolidayObserver()
        tvInstallment.text = installments

        view.findViewById<TextView>(R.id.tv_terms_condition_link).apply {
            setTextWithUnderLine(text.toString())
            setOnClickListener {
                val dialogFragment: DialogFragment = TermsConditionsDialogFragment
                        .newInstance(LOAN_TC_URL, getString(R.string.close))
                if (activity == null) return@setOnClickListener
                dialogFragment.show(activity?.supportFragmentManager, "dialog")
            }
        }

        btnAccept.setOnClickListener {
            if (acceptTerms()) {

                val activity = activity ?: return@setOnClickListener
                val user = activity.viewModel.user ?: return@setOnClickListener
                activity.viewModel.generateOtp(user.token, user.sessionId, user.refreshToken, user.customerId.toLong())
            }
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        activity ?: return
        activity?.setToolbarText(getString(R.string.loan_services_title))
    }

    private fun addOTPObserver() {

        val activity1 = activity?: return

        activity1.viewModel.otpRequestState.observe(this, Observer {
            it ?: return@Observer
            val activity = activity ?: return@Observer

            val otpState = it.getContentIfNotHandled() ?: return@Observer

            if (otpState is NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing))
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
                    activity.onFailure(activity.findViewById(R.id.root_view),  getString(R.string.request_error))
                }
                else -> activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    private fun addHolidayObserver() {
        val activity1 = activity ?: return

        activity1.viewModel.holidayState.observe(activity1, Observer {
            it ?: return@Observer
            val activity = activity ?: return@Observer

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
                    activity.onFailure(activity.findViewById(R.id.root_view),  getString(R.string.request_error))
                }
                else -> activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    private fun init(view: View) {
        rootView = view.findViewById(R.id.root_view)
        tvInstallment = view.findViewById(R.id.tv_number)
        btnAccept = view.findViewById(R.id.btn_accept)
        cbTerms = view.findViewById(R.id.cb_term_conditions)

        val activity = activity?: return
        installments = activity.viewModel?.installment?.value

    }

    private fun acceptTerms(): Boolean {
        if (!cbTerms.isChecked) {
            activity?.onFailure(activity?.findViewById(R.id.root_view) ?: return false,
                getString(R.string.terms_conditions_error))
            return false
        }
        return true
    }

    override fun onOtpConfirm(otp: String) {
        val activity = activity ?: return
        val user = activity.viewModel.user ?: return

        if (activity.viewModel.summary == null) return
        activity.viewModel.paymentHoliday(user.token, user.sessionId, user.refreshToken, user.customerId.toLong(), otp,
            activity.viewModel.summary?.loans?.get(0)?.loanNumber ?: return,
            installments?.toInt() ?: return)
    }

}