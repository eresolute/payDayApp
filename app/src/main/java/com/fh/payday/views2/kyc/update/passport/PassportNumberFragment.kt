package com.fh.payday.views2.kyc.update.passport

import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.design.button.MaterialButton
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fh.payday.R
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.clearErrorMessage

class PassportNumberFragment : Fragment() {
    lateinit var activity: PassportUpdateActivity
    lateinit var btnNext: MaterialButton
    lateinit var etPassportNumber: TextInputEditText
    lateinit var textInputLayout: TextInputLayout

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context as PassportUpdateActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addObserver()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_passport_number, container, false)
        btnNext = view.findViewById(R.id.btn_next)
        etPassportNumber = view.findViewById(R.id.et_passport_number)
        textInputLayout = view.findViewById(R.id.textInputLayout)

        view.findViewById<MaterialButton>(R.id.btn_next).setOnClickListener {
            if(!TextUtils.isEmpty(etPassportNumber.text.toString()) &&  etPassportNumber.text.toString().length >= 4) {
                if (!activity.isNetworkConnected()) {
                    activity.onFailure(activity.findViewById(R.id.root_view), getString(R.string.no_internet_connectivity))
                    return@setOnClickListener
                }
                val user = UserPreferences.instance.getUser(activity) ?: return@setOnClickListener
                activity.viewModel.passportNumber = etPassportNumber.text.toString()
                activity.viewModel.generateOtp(user.token, user.sessionId,user.refreshToken, user.customerId.toLong())
            } else {
                view.findViewById<TextInputLayout>(R.id.textInputLayout).error = getString(R.string.invalid_passport_number)
            }
        }

        etPassportNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textInputLayout.clearErrorMessage()
            }

        })

        return view
    }

    private fun addObserver() {
        activity.viewModel.otpRequest.observe(this, Observer {
            it ?: return@Observer

            val otpState = it.getContentIfNotHandled() ?: return@Observer

            if (otpState is NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing))
                btnNext.isEnabled = false
                return@Observer
            }

            activity.hideProgress()
            btnNext.isEnabled = true

            when (otpState) {
                is NetworkState2.Success -> activity.onPassportNumber()
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
}