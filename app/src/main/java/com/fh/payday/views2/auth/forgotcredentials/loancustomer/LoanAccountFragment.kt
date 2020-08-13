package com.fh.payday.views2.auth.forgotcredentials.loancustomer

import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.design.button.MaterialButton
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fh.payday.R
import com.fh.payday.datasource.models.forgotcredential.ValidateCardResponse
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.utilities.*
import com.fh.payday.utilities.maskededittext.MaskedEditText


class LoanAccountFragment : Fragment() {

    private lateinit var etEmiratesId: MaskedEditText
    private lateinit var textInputLayout: TextInputLayout
    private lateinit var fragmentType: String
    // private var flag = false

    private var activity = LoanCustomerActivity()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as LoanCustomerActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_loan_account_no, container, false)
        etEmiratesId = view.findViewById(R.id.et_emirates_id)
        textInputLayout = view.findViewById(R.id.textInputLayout_emirates_id)
        fragmentType = arguments?.getString("fragmentType") ?: return null
        //attachListener(etEmiratesId)
        addObservers()
        addOtpObserver()
        etEmiratesId.setOnEditorActionListener { _, _, _ -> false }
        etEmiratesId.hint = getString(R.string.placeholder)
        view.findViewById<MaterialButton>(R.id.btn_submit).setOnClickListener {
            if (validateEditText()) {
                if (!activity.isNetworkConnected()) {
                    activity.onFailure(activity.findViewById(R.id.root_view), getString(R.string.no_internet_connectivity))
                    return@setOnClickListener
                }
                val emiratesID = etEmiratesId.text.toString().trim().replace("-", "")
                activity.getViewModel().validateLoanAccountNo(emiratesID)
                //etEmiratesId.cleanUp()
            }
        }

        etEmiratesId.onTextChanged { _, _, _, _ ->
            textInputLayout.clearErrorMessage()
            if (etEmiratesId.text.toString().trim().replace("-", "").replace("x", "").length == 15) {
                if (!Validator.isValidEmiratesId(etEmiratesId.text.toString().trim().replace("-", ""))) {
                    textInputLayout.error = getString(R.string.invalid_emirate_id)
                    etEmiratesId.requestFocus()
                    return@onTextChanged
                }
                activity.hideKeyboard()
                if (!activity.isNetworkConnected()) {
                    activity.onFailure(activity.findViewById(R.id.root_view), getString(R.string.no_internet_connectivity))
                    return@onTextChanged
                }
                val emiratesId = etEmiratesId.text.toString().trim().replace("-", "")
                activity.getViewModel().validateLoanAccountNo(emiratesId)
                //etEmiratesId.cleanUp()
            }
        }
        return view
    }

    private fun attachListener(editText: MaskedEditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                textInputLayout.clearErrorMessage()
            }

        })

    }

    private fun addObservers() {

        activity.getViewModel().validateLoanAccountNo.observe(viewLifecycleOwner, Observer {
            val forgotUserIDState = it?.getContentIfNotHandled()
                    ?: return@Observer activity.hideProgress()

            if (forgotUserIDState is NetworkState2.Loading<ValidateCardResponse>) {
                activity.showProgress(getString(R.string.processing))
                return@Observer
            }
            activity.hideProgress()

            if (forgotUserIDState is NetworkState2.Success) {

                if (fragmentType == "forgotUserID") {
                    val mobileNo = activity.getViewModel().mobileNumber ?: return@Observer
                    val userId = activity.getViewModel().customerId
                    userId?.let {
                        activity.getViewModel().getOtp(it, mobileNo)
                    }
                } else {
                    activity.onSubmitAccountNo()
                }
            } else if (forgotUserIDState is NetworkState2.Error) {
                val (message) = forgotUserIDState
                activity.onError(message)

            } else if (forgotUserIDState is NetworkState2.Failure) {
                //val (throwable) = forgotUserIDState
                activity.onFailure(activity.findViewById(R.id.root_view), forgotUserIDState.throwable)

            } else {
                activity.onFailure(activity.findViewById<View>(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    private fun addOtpObserver() {
        activity.getViewModel().otpGenerationState.observe(this, Observer {
            val state = it?.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading<*>) {

                activity.hideProgress()
                return@Observer
            }
            activity.hideProgress()

            when (state) {
                is NetworkState2.Success<*> -> activity.onSubmitAccountNo()
                is NetworkState2.Error<*> -> {
                    val (message) = state
                    activity.onError(message)
                }
                is NetworkState2.Failure<*> -> //val (throwable) = state
                    activity.onFailure(activity.findViewById(R.id.root_view), state.throwable)
                else -> activity.onFailure(activity.findViewById<View>(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    private fun validateEditText(): Boolean {

        val emiratesID = etEmiratesId.text.toString().trim().replace("-", "")

        if (TextUtils.isEmpty(emiratesID)) {
            textInputLayout.setErrorMessage(getString(R.string.empty_emirate_id))
            etEmiratesId.requestFocus()
            return false
        } else if (emiratesID.length < 15) {
            textInputLayout.error = getString(R.string.invalid_emirate_id)
            etEmiratesId.requestFocus()
            return false
        } else if (!Validator.isValidEmiratesId(emiratesID)) {
            textInputLayout.error = getString(R.string.invalid_emirate_id)
            etEmiratesId.requestFocus()
            return false
        } else {
            textInputLayout.clearErrorMessage()
            return true
        }
    }

}