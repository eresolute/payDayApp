package com.fh.payday.views2.auth.forgotcredentials

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
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.datasource.models.forgotcredential.ValidateCardResponse
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.utilities.*
import com.mukesh.OtpView

class CardNumberPinFragment : Fragment() {
    private lateinit var activity: ForgotCredentailsActivity
    private lateinit var textInputLayoutcardNo: TextInputLayout
    private lateinit var etCardNo: TextInputEditText
    private lateinit var pinView: OtpView
    private lateinit var tvError: TextView
    private var fragmentType: String? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as ForgotCredentailsActivity
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if (isVisibleToUser && getActivity() != null) {
            val imgr = getActivity()!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_card_number_pin, container, false)
        textInputLayoutcardNo = view.findViewById(R.id.textInputLayout_card_no)
        etCardNo = view.findViewById(R.id.et_card_number)
        pinView = view.findViewById(R.id.pin_view)
        tvError = view.findViewById(R.id.tv_error)
        fragmentType = arguments?.getString("fragmentType")

        pinView.setOtpCompletionListener { tvError.visibility = View.GONE }
        val cardFormatter = CardNumberFormatter(etCardNo)
        etCardNo.addTextChangedListener(cardFormatter)    //Card number formatter

        adjustListener(etCardNo)
        addObservers()
        addOtpObserver()

        etCardNo.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                }

                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

                    if (etCardNo.text!!.toString().trim().replace(" ", "").length == 16) {
                        pinView.requestFocus()
                    }

                    if (etCardNo.text!!.toString().trim().replace(" ", "").length == 16) {
                        if (!(pinView.text.isNullOrEmpty() || pinView.text.toString().length < 4)) {
                            activity.hideKeyboard()
                            if (!activity.isNetworkConnected()) {
                                activity.onFailure(activity.findViewById(R.id.root_view), getString(R.string.no_internet_connectivity))
                                return
                            }
                            val cardNumber = etCardNo.text.toString().trim().replace(" ", "")
                            val keyBytes = activity.assets.open(PUBLIC_KEY).use {
                                it.readBytes()
                            }
                            activity.hideKeyboard()
                            activity.getViewModel().pinVerification(keyBytes, cardNumber, pinView.text.toString())
                        }

                    }
                }

                override fun afterTextChanged(editable: Editable) {
                }
            })

        view.findViewById<MaterialButton>(R.id.btn_submit).setOnClickListener {
            if (validateEditText()) {
                if (!activity.isNetworkConnected()) {
                    activity.onFailure(activity.findViewById(R.id.root_view), getString(R.string.no_internet_connectivity))
                    return@setOnClickListener
                }
                val cardNumber = etCardNo.text.toString().trim().replace(" ", "")
                val keyBytes = activity.assets.open(PUBLIC_KEY).use {
                    it.readBytes()
                }
                activity.getViewModel().pinVerification(keyBytes, cardNumber, pinView.text.toString())
            }
        }

        pinView.setOtpCompletionListener {
            if (validateEditText()) {
                if (!activity.isNetworkConnected()) {
                    activity.onFailure(activity.findViewById(R.id.root_view), getString(R.string.no_internet_connectivity))
                    return@setOtpCompletionListener
                }
                val cardNumber = etCardNo.text.toString().trim().replace(" ", "")
                val keyBytes = activity.assets.open(PUBLIC_KEY).use {
                    it.readBytes()
                }

                val pin = pinView.text.toString()
                pinView.setText("")
                activity.hideKeyboard()
                activity.getViewModel().pinVerification(keyBytes, cardNumber, pin)
            }
        }

        return view
    }

    private fun addObservers() {

        activity.getViewModel().pinVerificationState.observe(this, Observer {
            val forgotUserIDState = it?.getContentIfNotHandled() ?: return@Observer

            if (forgotUserIDState is NetworkState2.Loading<ValidateCardResponse>) {
                activity.showProgress(getString(R.string.processing))
                return@Observer
            }
            activity.hideProgress()

            if (forgotUserIDState is NetworkState2.Success) {

                if (fragmentType != null && fragmentType == "forgotUserID") {

                    val mobileNo = activity.getViewModel().mobileNumber ?: return@Observer
                    val userId = activity.getViewModel().customerId
                    userId?.let {
                        activity.getViewModel().getOtp(it, mobileNo)
                    }
                } else {
                    activity.onCardNumberPin()
                }

            } else if (forgotUserIDState is NetworkState2.Error) {
                val (message) = forgotUserIDState
                activity.onError(message)
                pinView.text = null

            } else if (forgotUserIDState is NetworkState2.Failure) {
                //val (throwable) = forgotUserIDState
                activity.onFailure(activity.findViewById(R.id.root_view), forgotUserIDState.throwable)

            } else {
                activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    private fun addOtpObserver() {
        activity.getViewModel().otpGenerationState.observe(this, Observer {
            val state = it?.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading<*>) {
                if (fragmentType == "forgotUserID") {
                    activity.hideProgress()
                }
                return@Observer
            }
            activity.hideProgress()

            if (state is NetworkState2.Success<*>) {
                if (fragmentType == "forgotUserID") {
                    activity.onCardNumberPin()
                    //activity.onResetPassword()
                }
            } else if (state is NetworkState2.Error<*>) {
                val (message) = state
                activity.onError(message)
            } else if (state is NetworkState2.Failure<*>) {
                //val (throwable) = state
                activity.onFailure(activity.findViewById(R.id.root_view), state.throwable)
            } else {
                activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    private fun adjustListener(editText: TextInputEditText) {
        editText.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                }

                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                    textInputLayoutcardNo.clearErrorMessage()
                }

                override fun afterTextChanged(editable: Editable) {
                }
            })
    }

    private fun validateEditText(): Boolean {

        if (etCardNo.text.toString().isEmpty() || TextUtils.isEmpty(etCardNo.text.toString())) {
            textInputLayoutcardNo.setErrorMessage(getString(R.string.empty_card_no))
            etCardNo.requestFocus()
            return false
        } else if (etCardNo.text!!.toString().trim().replace(" ", "").length < 16) {
            textInputLayoutcardNo.setErrorMessage(getString(R.string.invalid_card_no))
            etCardNo.requestFocus()
            return false
        } else if (pinView.text.isNullOrEmpty() || pinView.text.toString().length < 4) {
            tvError.visibility = View.VISIBLE
            return false
        } else {
            textInputLayoutcardNo.clearErrorMessage()
            tvError.visibility = View.GONE
            return true
        }
    }
}