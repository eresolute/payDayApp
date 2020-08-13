package com.fh.payday.views2.payments.recharge.mawaqif.fragments


import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.text.Editable
import android.text.InputFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fh.payday.BaseFragment
import com.fh.payday.R
import com.fh.payday.datasource.models.payments.Bill
import com.fh.payday.utilities.DecimalDigitsInputFilter
import com.fh.payday.utilities.clearErrorMessage
import com.fh.payday.utilities.getDecimalValue
import com.fh.payday.utilities.onTextChanged
import com.fh.payday.views2.payments.recharge.mawaqif.MawaqifTopUpActivity
import com.fh.payday.views2.shared.custom.EligibilityDialogFragment
import kotlinx.android.synthetic.main.fragment_payment_amount.*

class MawaqifAmountFragment : BaseFragment() {

    private fun getViewModel() = when (activity) {
        is MawaqifTopUpActivity -> (activity as MawaqifTopUpActivity).viewModel
        else -> null
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_payment_amount, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        et_amount.onTextChanged { _, _, _, _ -> til_amount.clearErrorMessage() }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            attachObserver()
            attachListeners()

            btn_next?.setOnClickListener {

                val amount = et_amount.text?.toString() ?: ""
                val viewModel = getViewModel() ?: return@setOnClickListener


                if (isValid(amount, viewModel.operatorDetailsFlexi?.minDenomination?.toDouble(),
                                viewModel.operatorDetailsFlexi?.maxDenomination?.toDouble())) {
                    viewModel.setPayableAmount(amount.getDecimalValue())
                    viewModel.enteredAmount = amount
                    onSuccess(activity)
                }
            }
        }
    }

    private fun attachListeners() {
        et_amount.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(8, 2))
        et_amount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                til_amount.clearErrorMessage()

                if (TextUtils.isEmpty(et_amount.text.toString()))
                    if (et_amount.length() == 1 && et_amount.text.toString() == ".") {
                        et_amount.setText("0.")
                        et_amount.setSelection(et_amount.text!!.length)
                    }
                if (et_amount.length() == 2 && et_amount.text.toString() == "00") {
                    et_amount.setText("0")
                    et_amount.setSelection(et_amount.text!!.length)
                }
            }
        })
    }

    private fun getBill() {

        //if (getViewModel()?.enteredAmount != null) return
        val (_, _, token, refreshToken, customerId, _, sessionId) = getViewModel()?.user ?: return
        val (_, _, _, _, flexiKey, typeKey) = getViewModel()?.operatorDetailsFlexi ?: return
        val typeId = getViewModel()?.typeId ?: return
        val accountNo = getViewModel()?.data?.value?.get("mobile_no") ?: return
        val accessKey = getViewModel()?.accessKey ?: return

//        btn_next.visibility = View.GONE
//        til_amount.visibility = View.INVISIBLE
//        tv_amount_label.text = getString(R.string.mawaqif_card_balance)
//        tv_bill_amount.visibility = View.VISIBLE
//        iv_edit.visibility = View.GONE

        getViewModel()?.getBalance(token, sessionId, refreshToken, customerId.toLong(),
                accessKey, typeId, accountNo, flexiKey, typeKey.toInt())
    }

    private fun attachObserver() {


    }

    private fun isValid(amount: String, min: Double? = 1.toDouble(), max: Double? = Double.MAX_VALUE) =
            when {
                amount.trim().isEmpty() -> {
                    til_amount.error = getString(R.string.invalid_amount)
                    /*tv_error_label.visibility = View.VISIBLE
                    tv_error_label.text = getString(R.string.invalid_amount)*/
                    false
                }
                else -> {
                    val minAmount = min ?: 1.toDouble()
                    val maxAmount = max ?: Double.MAX_VALUE
                    try {
                        if (amount.toDouble() !in minAmount..maxAmount) {
                            /*til_amount.error
                            tv_error_label.visibility = View.VISIBLE*/
                            til_amount.error = getString(R.string.invalid_amount_ranged_with_currency)
                                    .format(minAmount.toInt(), " AED", maxAmount.toInt(), "AED")
                            false
                        } else if (getViewModel() != null && getViewModel()!!.billAmount != null
                                && amount.toDouble() > getViewModel()!!.billAmount!!.toDouble()) {
                            EligibilityDialogFragment.Builder(object : EligibilityDialogFragment.OnDismissListener {
                                        override fun onDismiss(dialog: DialogInterface) {
                                            dialog.dismiss()
                                        }
                                    })
                                    .setBtn1Text(getString(R.string._continue))
                                    .setBtn2Text(getString(R.string.cancel))
                                    .setCancelable(false)
                                    .setCancelListener(object : EligibilityDialogFragment.OnCancelListener {
                                        override fun onCancel(dialog: DialogInterface) {
                                            dialog.dismiss()
                                        }

                                    })
                                    .setTitle(getDisplayMessage(amount, getViewModel()!!.billAmount!!))
                                    .setConfirmListener(object : EligibilityDialogFragment.OnConfirmListener {
                                        override fun onConfirm(dialog: DialogInterface) {
                                            val amount = et_amount.text?.toString() ?: return
                                            val viewModel = getViewModel() ?: return
                                            viewModel.enteredAmount = amount
                                            viewModel.setPayableAmount(amount.getDecimalValue())
                                            onSuccess(activity)
                                            dialog.dismiss()
                                        }
                                    }).build().show(fragmentManager, "AdvanceAmountPayment")
                            false
                        } else {
                            true
                        }
                    } catch (e: NumberFormatException) {
                        til_amount.error = getString(R.string.invalid_amount)
                        false
                    }
                }
            }

    private fun onSuccess(activity: FragmentActivity?) {
        when (activity) {
            is MawaqifTopUpActivity? -> activity?.navigateUp()
        }
    }

    private fun onSuccess(bill: Bill?) {
        val amount = getViewModel()?.enteredAmount ?: bill?.balanceAmount.toString() ?: "0.0"
        val currency = getViewModel()?.operatorDetailsFlexi?.baseCurrency ?: ""
        val amountWithCurr = "$currency ${amount.getDecimalValue()}"

        tv_bill_amount.text = amountWithCurr
        iv_edit.visibility = View.VISIBLE
        iv_edit.setOnClickListener {
            tv_error_label.visibility = View.GONE
            showAmountView()
        }
        et_amount.setText(amount.getDecimalValue())
    }

    private fun getDisplayMessage(etAmount: String, billAmount: String): String {
        val billedAmount = String.format(getString(R.string.amount_in_inr), billAmount.getDecimalValue())
        val actualAmount = String.format(getString(R.string.amount_in_inr), etAmount.getDecimalValue())
        return getString(R.string.bill_info).format(billedAmount, actualAmount)
    }

    private fun showAmountView() {
        tv_bill_amount.visibility = View.GONE
        iv_edit.visibility = View.GONE
        til_amount.visibility = View.VISIBLE
        tv_amount_label.text = getString(R.string.enter_amount)
    }
}