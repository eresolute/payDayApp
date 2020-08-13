package com.fh.payday.views2.payments.transport

import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.design.button.MaterialButton
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.InputFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.datasource.models.payments.transport.BalanceDetails
import com.fh.payday.datasource.models.payments.transport.OperatorDetails
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.*
import com.mukesh.OtpView
import kotlinx.android.synthetic.main.fragment_salik_pin.*

class SalikPinFragment : Fragment() {

    private var activity: TransportActivity? = null
    private lateinit var tvPinError: TextView
    private lateinit var pinview: OtpView
    private lateinit var btnNext: MaterialButton
    private lateinit var operatorDetails: OperatorDetails

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context as TransportActivity?
    }

    override fun onDetach() {
        super.onDetach()
        activity = null
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        activity ?: return
        val activity = activity as TransportActivity

        if (isVisibleToUser && activity != null) {
            if (activity.transportViewModel.salikAccPin != null) {
                pinview.setText(activity.transportViewModel.salikAccPin)
                et_amount.requestFocus()
            } else if (isVisibleToUser && activity.transportViewModel.dataClear) {
                pinview.text = null
                et_amount.text = null
            }
            val imgr = getActivity()!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_salik_pin, container, false)
        initView(view)
        addBillStateObserver()
        pinview.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus)
                pinview.setLineColor(ContextCompat.getColor(context!!, R.color.colorAccent))
            else
                pinview.setLineColor(ContextCompat.getColor(context!!, R.color.grey_600))
        }
        pinview.setOtpCompletionListener {
            et_amount.requestFocus()
        }
        btnNext.setOnClickListener {

            val activity = activity ?: return@setOnClickListener
            activity.transportViewModel.operatorDetails.observe(this, Observer {
                operatorDetails = it ?: return@Observer
            })

            if (validatePin(pinview.text.toString().trim())) {

                val user = UserPreferences.instance.getUser(activity) ?: return@setOnClickListener
                val accessKey = activity.transportViewModel.accessKey ?: return@setOnClickListener
                val typeId = activity.transportViewModel.salikTypeId ?: return@setOnClickListener
                val accountNo = activity.transportViewModel.accountNumber
                        ?: return@setOnClickListener

                activity.transportViewModel?.salikAccPin = pinview.text.toString().trim()
                activity.transportViewModel.amount.value = et_amount.text.toString().trim()

                activity.transportViewModel.billBalanceSalik(user.token, user.sessionId,
                        user.refreshToken, user.customerId.toLong(), accessKey, "getBalance", typeId,
                        accountNo, operatorDetails.flexiKey, Integer.parseInt(operatorDetails.typeKey), pinview.text.toString().trim())
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addTextWatcher()
        et_amount.hint = getString(R.string.aed)
        et_amount.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(10, 2))
    }

    private fun initView(view: View) {
        tvPinError = view.findViewById(R.id.tv_pin_error)
        pinview = view.findViewById(R.id.pin_view)
        btnNext = view.findViewById(R.id.btn_next)
    }

    private fun addTextWatcher() {
        et_amount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                textInputTransportAmount.clearErrorMessage()
                if (et_amount.length() == 1 && et_amount.text.toString() == ".") {
                    et_amount.setText("0.")
                    et_amount.setSelection(et_amount.text.toString().length)
                }
                if (et_amount.length() == 2 && et_amount.text.toString() == "00") {
                    et_amount.setText("0")
                    et_amount.setSelection(et_amount.text.toString().length)
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })

        pinview.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                tvPinError.visibility = View.GONE
            }
        })
    }

    private fun validatePin(pin: String): Boolean {
        val amount = et_amount.text.toString().trim()
        val minVal = operatorDetails.minDenomination.toFloat()
        val maxVal = operatorDetails.maxDenomination.toFloat()
        return when {
            pin.isEmpty() -> {
                tvPinError.visibility = View.VISIBLE
                pinview.requestFocus()
                false
            }
            pin.length < 4 -> {
                tvPinError.visibility = View.VISIBLE
                pinview.requestFocus()
                false
            }
            TextUtils.isEmpty(amount) -> {
                textInputTransportAmount.setErrorMessage(getString(R.string.invalid_amount))
                et_amount.requestFocus()
                false
            }
            amount.toFloat() < minVal || amount.toFloat() > maxVal -> {
                textInputTransportAmount.setErrorMessage(String.format(getString(R.string.invalid_float_amount_ranged),
                        minVal.getDecimalValue(), maxVal.getDecimalValue()))
                et_amount.requestFocus()
                false
            }
            amount.toFloat() % 50 != 0f -> {
                textInputTransportAmount.setErrorMessage(getString(R.string.multiple_of_50))
                et_amount.requestFocus()
                false
            }
            else -> {
                tvPinError.visibility = View.GONE
                textInputTransportAmount.clearErrorMessage()
                true
            }
        }
    }

    private fun addBillStateObserver() {

        val activity = activity ?: return
        activity.transportViewModel.balanceDetailState.observe(this, Observer {
            if (it == null) {
                return@Observer
            }
            val billState = it.getContentIfNotHandled() ?: return@Observer

            if (billState is NetworkState2.Loading<*>) {
                activity.showProgress("Processing")
                btnNext.isEnabled = false
                return@Observer
            }

            activity.hideProgress()
            btnNext.isEnabled = true

            if (billState is NetworkState2.Success<*>) {
                val data = (billState as NetworkState2.Success<BalanceDetails>).data

                if (data != null) {
                    if (data.Balance != null) {
                        activity.pinAmountSelected()
                    } else {
                        activity.onError(getString(R.string.generic_payment_error))
                    }
                }
            } else if (billState is NetworkState2.Error<*>) {
                val (message, errorCode, isSessionExpired) = billState
                if (isSessionExpired) {
                    activity.onSessionExpired(message)
                    return@Observer
                }
                activity.handleErrorCode(Integer.parseInt(errorCode), message)

            } else if (billState is NetworkState2.Failure<*>) {
                val (throwable) = billState
                activity.onFailure(activity.findViewById(R.id.parent_view), throwable)

            } else {
                activity.onFailure(activity.findViewById(R.id.parent_view), CONNECTION_ERROR)
            }
        })
    }
}