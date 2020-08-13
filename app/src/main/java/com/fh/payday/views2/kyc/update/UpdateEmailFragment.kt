package com.fh.payday.views2.kyc.update

import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.OnOTPConfirmListener
import com.fh.payday.utilities.Validator
import com.fh.payday.utilities.onTextChanged
import com.fh.payday.views.fragments.OTPFragment
import com.fh.payday.views2.shared.custom.AlertDialogFragment

class UpdateEmailFragment : Fragment(), OnOTPConfirmListener {

    private lateinit var tvTitle: TextView
    private lateinit var tvError: TextView
    private lateinit var tvErrorConfirm: TextView
    private lateinit var btnNext: Button
    private lateinit var etEmail: TextInputEditText
    private lateinit var etConfirmEmail: TextInputEditText

    enum class Type { DEFAULT, CONFIRM }

    private lateinit var activity: KycOptionActivity

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context as KycOptionActivity
    }

    override fun onResume() {
        super.onResume()
        activity.setToolbarText(getString(R.string.update_email_id))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_update_email, container, false)
        init(view)

        tvError.visibility = View.GONE

//        val title = arguments?.get("title")
        addObserver()
        addOtpObserver()

        /*if (title == Type.DEFAULT) {

            tvTitle.text = resources.getString(R.string.enter_email_id)
            btnNext.text = resources.getString(R.string.next)

        } else if (title == Type.CONFIRM) {

            tvTitle.text = resources.getString(R.string.confirm_email_id)
            btnNext.text = resources.getString(R.string.confirm)

        }*/

        etEmail.onTextChanged { _, _, _, _ ->
            tvError.visibility = View.GONE
        }

        etConfirmEmail.onTextChanged { _, _, _, _ ->
            tvErrorConfirm.visibility = View.GONE
        }

        tvTitle.text = resources.getString(R.string.enter_email_id)
        btnNext.text = resources.getString(R.string.confirm)

        btnNext.setOnClickListener {

            if (!Validator.validateEmail(etEmail.text.toString())) {
                tvError.text = getString(R.string.invalid_email)
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            when {
                etEmail.text.toString() == etConfirmEmail.text.toString() -> {
                    if (!activity.isNetworkConnected()) {
                        activity.onFailure(activity.findViewById(R.id.root_view), getString(R.string.no_internet_connectivity))
                        return@setOnClickListener
                    }
                    val user = UserPreferences.instance.getUser(activity)
                        ?: return@setOnClickListener

                    activity.getViewModel().generateOtp(user.token, user.sessionId, user.refreshToken, user.customerId.toLong())

                }
                else -> tvErrorConfirm.visibility = View.VISIBLE
            }

        }

        return view
    }

    private fun addOtpObserver() {
        activity.getViewModel().otpRequest.observe(activity, Observer {
            it ?: return@Observer

            val otpState = it.getContentIfNotHandled() ?: return@Observer

            if (otpState is NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing))
                btnNext.isEnabled = false
                return@Observer
            }

            activity.hideProgress()
            btnNext.isEnabled = false

            when (otpState) {
                is NetworkState2.Success -> {

                    activity.setToolbarText(getString(R.string.verify_otp))
                    activity.replaceFragment(
                        OTPFragment.Builder(this)
                            .setPinLength(6)
                            .setTitle(activity.getString(R.string.enter_otp))
                            .setInstructions(activity.getString(R.string.otp_sent_to_registered_number))
                            .setHasCardView(false)
                            .setButtonTitle(activity.getString(R.string.submit))
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

    private fun addObserver() {
        activity.getViewModel().updateEmail.observe(activity, Observer {
            if (it == null) return@Observer

            val emailState = it.getContentIfNotHandled() ?: return@Observer

            if (emailState is NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing))
                btnNext.isEnabled = false
                return@Observer
            }

            activity.hideProgress()
            btnNext.isEnabled = true

            when (emailState) {
                is NetworkState2.Success -> {
                    emailState.data ?: return@Observer
                    AlertDialogFragment.Builder()
                        .setMessage(getString(R.string.email_update_succesful))
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
                    if (emailState.isSessionExpired)
                        return@Observer activity.onSessionExpired(emailState.message)
                    activity.handleErrorCode(emailState.errorCode.toInt(), emailState.message)
                }
                is NetworkState2.Failure -> activity.onFailure(activity.findViewById(R.id.root_view), emailState.throwable)
                else -> activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    fun init(view: View) {
        tvTitle = view.findViewById(R.id.title)
        tvError = view.findViewById(R.id.tv_error)
        tvErrorConfirm = view.findViewById(R.id.tv_error_confirm)
        btnNext = view.findViewById(R.id.btn_next)
        etEmail = view.findViewById(R.id.et_email)
        etConfirmEmail = view.findViewById(R.id.et_confirm_email)

    }

    override fun onOtpConfirm(otp: String) {

        val user = UserPreferences.instance.getUser(activity) ?: return
        if (TextUtils.isEmpty(otp)) return

        activity.getViewModel().updateEmail(
            user.token, user.sessionId, user.refreshToken, user.customerId.toLong(), etEmail.text.toString(), otp)
    }
}