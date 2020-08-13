package com.fh.payday.views2.payments.ibp

import android.app.Activity
import android.arch.lifecycle.Observer
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fh.payday.BaseFragment
import com.fh.payday.R
import com.fh.payday.datasource.models.Plan
import com.fh.payday.datasource.models.login.User
import com.fh.payday.datasource.models.payments.*
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.utilities.*
import com.fh.payday.viewmodels.payments.ibp.IndianBillPaymentViewModel
import com.fh.payday.views2.payments.SelectPlanActivity
import com.fh.payday.views2.shared.custom.EligibilityDialogFragment
import kotlinx.android.synthetic.main.fragment_payment_amount.*

class PaymentAmountFragment : BaseFragment() {

    companion object {
        private const val REQUEST_SELECT_PLAN = 2
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_payment_amount, container, false)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if (isVisibleToUser) {

            activity ?: return
            val activity = activity as IndianBillPaymentActivity
            val viewModel = getViewModel() ?: return

            tv_error_label.visibility = View.GONE

            if (viewModel.dataClear) {
                et_amount?.text = null
                viewModel.selectedPlan = null
            }

            val planType = getViewModel()?.planType ?: return

            if (1 == getViewModel()?.selectedOperator?.isFixedAvailable
                    && 0 == getViewModel()?.selectedOperator?.isFlexAvailable) {
                et_amount.isFocusable = false
                setBrowsePlans()
                et_amount?.setOnClickListener {
                    activity.hideKeyboard()
                    val intent = Intent(activity,
                            SelectPlanActivity::class.java).apply {
                        val plans = getViewModel()?.plans ?: return@setOnClickListener
                        putExtra("plans", plans)
                        putExtra("plan_provider", "ibp")
                    }
                    startActivityForResult(intent, REQUEST_SELECT_PLAN)
                }
                et_amount.hint = viewModel.plans!![0].currency
            } else {
                et_amount.hint = viewModel.operatorDetailsFlexi?.baseCurrency
                browse_plan?.visibility = View.GONE
            }

            val operator = getViewModel()?.selectedOperator ?: return
            when {
                operator.serviceCategory == ServiceCategory.ELECTRICITY
                        && operator.isBalanceMethod == 1 -> getBill(operator)
                operator.serviceCategory == ServiceCategory.LANDLINE
                        && operator.isBalanceMethod == 1
                        && operator.accessKey == BSNL_LANDLINE_ACCESS_KEY -> getBill(operator)
                operator.serviceCategory == ServiceCategory.LANDLINE
                        && operator.isBalanceMethod == 1
                        && operator.accessKey == MTNL_LANDLINE_ACCESS_KEY -> getBill(operator)
                operator.serviceCategory == ServiceCategory.POSTPAID
                        && operator.isBalanceMethod == 1
                        && operator.accessKey == BSNL_POSTPAID_ACCESS_KEY -> getBill(operator)
            }

            setAmountBehavior(planType)
            attachListeners()
            attachObserver()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            REQUEST_SELECT_PLAN -> {
                val selectedPlan = data?.getParcelableExtra<Plan>("data") ?: return
                et_amount.setText(selectedPlan.amount.toString())
                getViewModel()?.selectedPlan = selectedPlan
            }
        }
    }

    private fun getViewModel() =
            when (activity) {
                is IndianBillPaymentActivity -> (activity as IndianBillPaymentActivity?)?.viewModel
                else -> null
            }

    private fun getBill(operator: Operator) {

        if (getViewModel()?.enteredAmount != null) return

        val (_, _, token, refreshToken, customerId, _, sessionId) = getViewModel()?.user ?: return
        val (_, _, _, _, flexiKey, typeKey) = getViewModel()?.operatorDetailsFlexi ?: return
        val typeId = getViewModel()?.typeId ?: return
        var accountNo = getViewModel()?.data?.value?.get("account_no") ?: return
        val optional1 = getViewModel()?.data?.value?.get("bsnl_account") ?: ""

        if (operator.serviceCategory == ServiceCategory.LANDLINE
                && operator.isBalanceMethod == 1
                && operator.accessKey in arrayOf(BSNL_LANDLINE_ACCESS_KEY, MTNL_LANDLINE_ACCESS_KEY)) {

            val stdCode = getViewModel()?.data?.value?.get("std_code") ?: return
            accountNo = stdCode.plus(accountNo)
        }
        val (_, accessKey) = operator

        btn_next.visibility = View.GONE
        til_amount.visibility = View.INVISIBLE
        tv_amount_label.text = getString(R.string.bill_amount)
        tv_bill_amount.visibility = View.VISIBLE
        iv_edit.visibility = View.GONE

        getViewModel()?.getBalance(token, sessionId, refreshToken, customerId.toLong(),
                accessKey, typeId, accountNo, flexiKey, typeKey.toInt(), optional1)
    }

    private fun attachListeners() {
        et_amount.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(8, 2))
        et_amount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                til_amount.clearErrorMessage()

                // if (TextUtils.isEmpty(et_amount.text.toString()))
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

    private fun attachObserver() {
        getViewModel()?.payableAmountState?.observe(this, Observer {
            it ?: return@Observer

            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                btn_next.visibility = View.GONE
                progress_bar.visibility = View.VISIBLE
                return@Observer
            }

            btn_next.visibility = View.VISIBLE
            progress_bar.visibility = View.GONE

            when (state) {
                is NetworkState2.Success -> onSuccess(activity)
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        activity?.onSessionExpired(state.message)
                        return@Observer
                    }
//                    activity?.onError(state.message)
                    activity?.handleErrorCode(state.errorCode.toInt(), state.message)
                }
                is NetworkState2.Failure -> activity?.onFailure(activity?.findViewById(R.id.root_view)
                        ?: return@Observer, CONNECTION_ERROR)
                else -> activity?.onFailure(activity?.findViewById(R.id.root_view)
                        ?: return@Observer, CONNECTION_ERROR)
            }
        })

        getViewModel()?.balanceState?.observe(this, Observer {
            it ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                tv_bill_amount.text = getString(R.string.loading)
                btn_next.visibility = View.GONE
                progress_bar.visibility = View.VISIBLE
                return@Observer
            }

            tv_bill_amount.text = ""
            btn_next.visibility = View.VISIBLE
            progress_bar.visibility = View.GONE

            when (state) {
                is NetworkState2.Success -> onSuccess(state.data)
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        activity?.onSessionExpired(state.message)
                        return@Observer
                    }
//                    onError(state.message)
                    activity?.handleErrorCode(state.errorCode.toInt(), state.message)
                }
                is NetworkState2.Failure -> activity?.onFailure(activity?.findViewById(R.id.root_view)
                        ?: return@Observer, state.throwable)
                else -> activity?.onFailure(activity?.findViewById(R.id.root_view)
                        ?: return@Observer, CONNECTION_ERROR)
            }
        })
    }

    private fun setAmountBehavior(planType: String) {
        val enabled = when (planType) {
            PlanType.FLEXI -> true
            else -> true
        }
        et_amount?.isEnabled = enabled
        et_amount?.hint = getString(R.string.INR)

        if (enabled) {
            et_amount.setOnTouchListener { _, _ -> false }
        } else {
            val plans = getViewModel()?.plans ?: return
            et_amount.setOnTouchListener { _, _ ->
                val intent = Intent(activity
                        ?: return@setOnTouchListener false, SelectPlanActivity::class.java)
                        .apply {
                            putExtra("plans", plans)
                            putExtra("plan_provider", "ibp")
                        }
                startActivityForResult(intent, REQUEST_SELECT_PLAN)
                true
            }
        }

        if (getViewModel()?.planType == PlanType.FIXED) {
            et_amount.isCursorVisible = false
        }

        btn_next?.setOnClickListener {
            val amount = et_amount.text?.toString() ?: ""
            val viewModel = getViewModel() ?: return@setOnClickListener
            if (viewModel.planType == PlanType.FIXED) {
                if (isValid(amount)) {
                    val selectedPlan = viewModel.selectedPlan ?: return@setOnClickListener
                    viewModel.setAmount(selectedPlan.amount.toString())
                    viewModel.setPayableAmount(selectedPlan.cost.toString())
                    onSuccess(activity)
                }

            } else {
                if (viewModel.operatorDetailsFlexi == null) return@setOnClickListener
                if (isValid(amount, viewModel.operatorDetailsFlexi?.minDenomination?.toDouble(),
                                viewModel.operatorDetailsFlexi?.maxDenomination?.toDouble())) {
                    val user = getViewModel()?.user ?: return@setOnClickListener
                    val accessKey = viewModel.selectedOperator?.accessKey
                            ?: return@setOnClickListener
                    val typeKey = viewModel.operatorDetailsFlexi?.typeKey?.toInt()
                            ?: viewModel.selectedPlan?.typeKey ?: return@setOnClickListener

                    makePayment(viewModel, user, accessKey, typeKey, amount)
                }
            }
        }
    }

    private fun makePayment(viewModel: IndianBillPaymentViewModel, user: User, accessKey: String, typeKey: Int, amount: String) {
        when {
            viewModel.planType == PlanType.FIXED -> {
                val selectedPlan = viewModel.selectedPlan ?: return
                viewModel.setAmount(selectedPlan.amount.toString())
                viewModel.setPayableAmount(selectedPlan.cost.toString())
                onSuccess(activity)
            }
            viewModel.selectedOperator?.serviceCategory == ServiceCategory.ELECTRICITY
                    && viewModel.selectedOperator?.isBalanceMethod == 1 -> onSuccess(activity)
            else -> {
                val flexiKey = viewModel.operatorDetailsFlexi?.flexiKey ?: return

                if (activity?.isNetworkConnected() == false) {
                    activity?.onFailure(activity?.findViewById(R.id.card_view)!!, getString(R.string.no_internet_connectivity))
                    return
                }

                viewModel.getPayableAmount(user.token, user.sessionId, user.refreshToken, user.customerId.toLong(), accessKey,
                        typeKey, flexiKey, amount.toDouble())
            }
        }
    }

    private fun setBrowsePlans() {
        tv_amount_label.text = getString(R.string.choose_plan)
        et_amount.isLongClickable = false
        val text = browse_plan.text?.toString() ?: ""
        browse_plan.setTextWithUnderLine(text)
        browse_plan.visibility = View.VISIBLE
        browse_plan.setOnClickListener {
            val intent = Intent(activity ?: return@setOnClickListener,
                    SelectPlanActivity::class.java).apply {
                val plans = getViewModel()?.plans ?: return@setOnClickListener
                putExtra("plans", plans)
                putExtra("plan_provider", "ibp")
            }
            startActivityForResult(intent, REQUEST_SELECT_PLAN)
        }
    }

    private fun isValid(amount: String): Boolean {
        return if (amount.trim().isEmpty()) {
            til_amount.error
            tv_error_label.visibility = View.VISIBLE
            tv_error_label.text = getString(R.string.please_choose_plan)
            false
        } else {
            true
        }
    }

    private fun isValid(amount: String, min: Double? = 1.toDouble(), max: Double? = Double.MAX_VALUE) =
            when {
                amount.trim().isEmpty() -> {
                    til_amount.error
                    tv_error_label.visibility = View.VISIBLE
                    tv_error_label.text = getString(R.string.invalid_amount)
                    false
                }
                else -> {
                    val minAmount = min ?: 1.toDouble()
                    val maxAmount = max ?: Double.MAX_VALUE
                    try {
                        if (amount.toDouble() !in minAmount..maxAmount) {
                            til_amount.error
                            tv_error_label.visibility = View.VISIBLE
                            //tv_error_label.text = "Please enter an amount between " +
                            //      "${minAmount.toInt()} INR and ${maxAmount.toInt()} INR"

                            tv_error_label.text = getString(R.string.invalid_amount_ranged_with_currency)
                                    .format(minAmount.toInt(), " INR", maxAmount.toInt(), "INR")
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
                                            if (viewModel.operatorDetailsFlexi == null) return

                                            val user = getViewModel()?.user ?: return
                                            val accessKey = viewModel.selectedOperator?.accessKey
                                                    ?: return
                                            val typeKey = viewModel.operatorDetailsFlexi?.typeKey?.toInt()
                                                    ?: viewModel.selectedPlan?.typeKey ?: return

                                            makePayment(viewModel, user, accessKey, typeKey, amount)

                                            dialog.dismiss()
                                        }

                                    }).build().show(fragmentManager, "AdvanceAmountPayment")
                            false
                        } else {
                            true
                        }
                    } catch (e: NumberFormatException) {
                        tv_error_label.text = getString(R.string.invalid_amount)
                        false
                    }
                }
            }

    private fun onSuccess(activity: FragmentActivity?) {
        when (activity) {
            is IndianBillPaymentActivity? -> activity?.navigateUp()
        }
    }

    private fun parseAmount(amountDueInAED: String): Double {
        val amount = amountDueInAED.replace(',', ' ')
        return java.lang.Double.parseDouble(amount)
    }

    private fun getDisplayMessage(etAmount: String, billAmount: String): String {
        val billedAmount = String.format(getString(R.string.amount_in_inr), billAmount.getDecimalValue())
        val actualAmount = String.format(getString(R.string.amount_in_inr), etAmount.getDecimalValue())
        return (String.format(getString(R.string.bill_info), billedAmount, actualAmount))
        /*   return ("Your actual bill is " + billedAmount
                   + " and you are paying " + actualAmount)*/
    }

    private fun onError(message: String) {
        when (getViewModel()?.selectedOperator?.accessKey) {
            BSNL_LANDLINE_ACCESS_KEY -> showAmountView()
            MTNL_LANDLINE_ACCESS_KEY -> showAmountView()
            BSNL_POSTPAID_ACCESS_KEY -> showAmountView()
            else -> activity?.onError(message)
        }
    }

    private fun onSuccess(data: Bill?) {
        val (_, accessKey, _, _, _, _, serviceCategory)
                = getViewModel()?.selectedOperator ?: return
        when {
            serviceCategory == ServiceCategory.ELECTRICITY -> handleEletricityBillView(data)
            serviceCategory == ServiceCategory.LANDLINE
                    && accessKey in arrayOf(BSNL_LANDLINE_ACCESS_KEY, MTNL_LANDLINE_ACCESS_KEY) -> handleBSNLBillView(data)
            serviceCategory == ServiceCategory.POSTPAID
                    && accessKey == BSNL_POSTPAID_ACCESS_KEY -> handleBSNLBillView(data)
        }
    }

    private fun handleEletricityBillView(data: Bill?) {
        val amount = data?.dueAmount?.toString() ?: "0.0"
        val currency = getViewModel()?.operatorDetailsFlexi?.baseCurrency ?: ""
        val amountWithCurr = "$currency ${amount.getDecimalValue()}"
        tv_bill_amount.text = amountWithCurr

        et_amount.setText(amount)
    }

    private fun handleBSNLBillView(data: Bill?) {

        val amount = getViewModel()?.enteredAmount ?: data?.dueAmount?.toString() ?: "0.0"
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

    private fun showAmountView() {
        tv_bill_amount.visibility = View.GONE
        iv_edit.visibility = View.GONE
        til_amount.visibility = View.VISIBLE
        tv_amount_label.text = getString(R.string.enter_amount)
    }
}
