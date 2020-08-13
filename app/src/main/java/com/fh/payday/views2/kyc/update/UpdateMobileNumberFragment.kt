package com.fh.payday.views2.kyc.update

import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.TextInputEditText
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.*
import com.fh.payday.views.fragments.OTPFragment
import com.fh.payday.views2.shared.custom.AlertDialogFragment

class UpdateMobileNumberFragment : Fragment(), OnOTPConfirmListener {

    private lateinit var tvTitle: TextView
    private lateinit var tvError: TextView
    private lateinit var tvErrorConfirm: TextView
    private lateinit var tvPrefix: TextView
    private lateinit var tvPrefixConfirm: TextView
    private lateinit var etMobileNo: TextInputEditText
    private lateinit var etMobileNoConfirm: TextInputEditText
    private lateinit var btnConfirm: Button
    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var constraintLayoutConfirm: ConstraintLayout

    private lateinit var activity: KycOptionActivity

    enum class Type { DEFAULT, CONFIRM }


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context as KycOptionActivity
    }

    override fun onResume() {
        super.onResume()
        activity.setToolbarText(getString(R.string.update_mobile_no))
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_update_mobile, container, false)
        init(view)
        addOtpObserver()
        addObserver()
        tvPrefix.text = getString(R.string.uea_plus_country_dash)
        tvPrefixConfirm.text = getString(R.string.uea_plus_country_dash)
        val prefix = tvPrefix.text.toString().replace("+971-", "0")
        val prefixConfirm = tvPrefixConfirm.text.toString().replace("+971-", "0")
        etMobileNo.setMaxLength(9)
        etMobileNoConfirm.setMaxLength(9)
        tvError.visibility = View.GONE
        tvTitle.text = resources.getString(R.string.enter_new_mobile_number)

       /* if (arguments?.get("title") == Type.DEFAULT) {
            tvTitle.text = resources.getString(R.string.enter_new_mobile_number)
        } else {
            tvTitle.text = resources.getString(R.string.confirm_mobile)
        }*/

        btnConfirm.setOnClickListener {

                val mobileNo = prefix.plus(etMobileNo.text.toString().trim())
                if (!Validator.isNumber(mobileNo) || mobileNo.length != 10) {
                    tvError.addErrorMessage(getString(R.string.invalid_mobile_no))
                    constraintLayout.onErrorMsg()
                    return@setOnClickListener
                } else if (!MobileNoValidator.validate(mobileNo)) {
                    tvError.addErrorMessage(getString(R.string.number_format_error))
                    constraintLayout.onErrorMsg()
                    return@setOnClickListener
                } else if (prefix.plus(etMobileNo.text.toString().trim()) == prefixConfirm.plus(etMobileNoConfirm.text.toString().trim())) {
                    if (!activity.isNetworkConnected()) {
                        activity.onFailure(activity.findViewById(R.id.root_view), getString(R.string.no_internet_connectivity))
                        return@setOnClickListener
                    }
                    activity.getViewModel().mobileNumber = mobileNo
                    val user = UserPreferences.instance.getUser(activity)
                            ?: return@setOnClickListener
                    activity.getViewModel().generateOtp(user.token, user.sessionId, user.refreshToken, user.customerId.toLong())
                } else {
                    tvErrorConfirm.addErrorMessage(getString(R.string.mobile_not_match))
                    constraintLayoutConfirm.onErrorMsg()
                    return@setOnClickListener
                }


        }

        etMobileNo.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tvError.removeErrorMessage()
                constraintLayout.clearErrorMsg()
            }
        })

        etMobileNoConfirm.onTextChanged { _, _, _, _ ->
            tvErrorConfirm.removeErrorMessage()
            constraintLayoutConfirm.clearErrorMsg()
        }

        return view
    }

    private fun addOtpObserver() {
        activity.getViewModel().otpRequest.observe(activity, Observer {
            it ?: return@Observer

            val otpState = it.getContentIfNotHandled() ?: return@Observer

            if (otpState is NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing))
                btnConfirm.isEnabled = false
                return@Observer
            }

            activity.hideProgress()
            btnConfirm.isEnabled = false

            when (otpState) {
                is NetworkState2.Success -> {

                    activity.setToolbarText(getString(R.string.verify_otp))
                    activity.replaceFragment(
                            OTPFragment.Builder(this)
                                    .setPinLength(6)
                                    .setTitle(getString(R.string.enter_otp))
                                    .setInstructions(getString(R.string.otp_sent_to_registered_number))
                                    .setButtonTitle(getString(R.string.submit))
                                    .build())

                }
                is NetworkState2.Error -> {
                    if (otpState.isSessionExpired)
                        return@Observer activity.onSessionExpired(otpState.message)
                    activity.onError(otpState.message)
                }
                is NetworkState2.Failure -> activity.onFailure(activity.findViewById(R.id.root_view), otpState.throwable)
                else -> activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    val confirmListener = AlertDialogFragment.OnConfirmListener {
        it.dismiss()
//        activity.finish()
    }
    private fun addObserver() {
        activity.getViewModel().updateMobile.observe(activity, Observer {
            if (it == null) return@Observer

            val mobileState = it.getContentIfNotHandled() ?: return@Observer

            if (mobileState is NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing))
                btnConfirm.isEnabled = false
                return@Observer
            }

            activity.hideProgress()
            btnConfirm.isEnabled = true

            when (mobileState) {
                is NetworkState2.Success -> AlertDialogFragment.Builder()
                        .setMessage(getString(R.string.mobile_update_succesful))
                        .setIcon(R.drawable.ic_success_checked_blue)
                        .setCancelable(false)
                        .setConfirmListener { dialog ->
                            dialog.dismiss()
                            activity.finish()
                        }
                        .build()
                        .show(fragmentManager, "")
                is NetworkState2.Error -> {
                    if (mobileState.isSessionExpired)
                        return@Observer activity.onSessionExpired(mobileState.message)
                    activity.handleErrorCode(mobileState.errorCode.toInt(), mobileState.message)
                    /*activity.onError(mobileState.message,false, activity.errorIcon,
                            activity.tintColorError,
                            activity.alertDismissListener,
                            activity.alertCancelListener,
                            confirmListener,R.string.ok)*/
                }
                is NetworkState2.Failure -> activity.onFailure(activity.findViewById(R.id.root_view), mobileState.throwable)
                else -> activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
            }

        })
    }

    fun init(view: View) {
        tvTitle = view.findViewById(R.id.title)
        tvError = view.findViewById(R.id.tv_error)
        tvErrorConfirm = view.findViewById(R.id.tv_error_confirm)
        tvPrefix = view.findViewById(R.id.tv_prefix)
        tvPrefixConfirm = view.findViewById(R.id.tv_prefix_confirm)
        etMobileNo = view.findViewById(R.id.et_mobile_number)
        etMobileNoConfirm = view.findViewById(R.id.et_mobile_number_confirm)
        btnConfirm = view.findViewById(R.id.btn_confirm)
        constraintLayout = view.findViewById(R.id.cl_custom_layout)
        constraintLayoutConfirm = view.findViewById(R.id.cl_custom_layout_confirm)
    }

    override fun onOtpConfirm(otp: String) {
        if (TextUtils.isEmpty(otp)) return
        val user = UserPreferences.instance.getUser(activity) ?: return
        val prefix = tvPrefix.text.toString().replace("+971-","0").trim()
        val mobileNumber = prefix.plus(etMobileNo.text.toString())
        activity.getViewModel().updateMobile(
                user.token, user.sessionId, user.refreshToken,
                user.customerId.toLong(),
                mobileNumber, otp
        )
    }
}