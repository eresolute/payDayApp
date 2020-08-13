package com.fh.payday.views2.auth.forgotcredentials.loancustomer

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
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.PasswordValidator
import com.fh.payday.utilities.clearErrorMessage
import com.fh.payday.utilities.setErrorMessage

class PasswordResetFragment : Fragment() {
    private lateinit var tilNewPassword: TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var etNewPassword: TextInputEditText
    private lateinit var etConfirm: TextInputEditText
    private lateinit var activity: LoanCustomerActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as LoanCustomerActivity
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            etNewPassword.text = null
            etConfirm.text = null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.reset_password_fragment, container, false)
        tilNewPassword = view.findViewById(R.id.textInputLayout_new_password)
        tilConfirmPassword = view.findViewById(R.id.textInputLayout_confirm_password)
        etNewPassword = view.findViewById(R.id.et_new_password)
        etConfirm = view.findViewById(R.id.et_confirm_password)
        attachListener(etNewPassword, tilNewPassword)
        attachListener(etConfirm, tilConfirmPassword)

        view.findViewById<MaterialButton>(R.id.btn_submit).setOnClickListener {
            if (validateEditText()) {
                if (!activity.isNetworkConnected()) {
                    activity.onFailure(activity.findViewById(R.id.root_view), getString(R.string.no_internet_connectivity))
                    return@setOnClickListener
                }
                addOtpObserver()
                val userId = activity.getViewModel().customerId ?: return@setOnClickListener
                val mobileNo = activity.getViewModel().mobileNumber ?: return@setOnClickListener
                activity.getViewModel().getOtpPassword(userId, mobileNo)
                activity.getViewModel().newUserPassword = etNewPassword.text.toString()
            }
        }
        return view
    }

    private fun addOtpObserver() {
        activity.getViewModel().otpGenerationPasswordState.observe(this, Observer {
            val state = it?.getContentIfNotHandled() ?: return@Observer
            if (state is NetworkState2.Loading<*>) {
                activity.showProgress(getString(R.string.processing))
                return@Observer
            }
            activity.hideProgress()

            when (state) {
                is NetworkState2.Success<*> -> {
                    activity.onResetPassword()
                }
                is NetworkState2.Error<*> -> {
                    val (message) = state
                    activity.onError(message)
                }
                is NetworkState2.Failure<*> -> {
                    //val (throwable) = state
                    activity.onFailure(activity.findViewById(R.id.root_view), state.throwable)
                }
                else -> activity.onFailure(activity.findViewById<View>(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    private fun attachListener(editText: TextInputEditText, textInputLayout: TextInputLayout) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                textInputLayout.clearErrorMessage()
            }
        })
    }

    private fun validateEditText(): Boolean {

        if (etNewPassword.text.isNullOrEmpty() || TextUtils.isEmpty(etNewPassword.text.toString())) {
            tilNewPassword.setErrorMessage(getString(R.string.empty_password))
            etNewPassword.requestFocus()
            return false
        } else if (etNewPassword.text.isNullOrEmpty() || !PasswordValidator
                        .validate(etNewPassword.text!!.toString().trim { it <= ' ' })) run {
            tilNewPassword.setErrorMessage(getString(R.string.password_pattern_info))
            etNewPassword.requestFocus()
            return false
        }
        else if (etNewPassword.text!!.toString().trim { it <= ' ' }.length < 8) {
            tilNewPassword.setErrorMessage(getString(R.string.invalid_password_len))
            etNewPassword.requestFocus()
            return false
        } else if (etConfirm.text.isNullOrEmpty() || TextUtils.isEmpty(etConfirm.text!!.toString().trim())) {
            tilConfirmPassword.setErrorMessage(getString(R.string.confirm_password))
            etConfirm.requestFocus()
            return false
        } else if (etConfirm.text!!.toString() != etNewPassword.text.toString().trim()) {
            tilConfirmPassword.setErrorMessage(getString(R.string.password_not_match))
            etConfirm.requestFocus()
            return false
        } else {
            tilNewPassword.clearErrorMessage()
            tilConfirmPassword.clearErrorMessage()
            return true
        }
    }

}