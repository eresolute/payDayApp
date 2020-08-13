package com.fh.payday.views2.payments.internationaltopup

import android.app.Activity
import android.arch.lifecycle.Observer
import android.content.Intent
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
import com.fh.payday.datasource.models.Plan
import com.fh.payday.datasource.models.payments.PlanType
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.utilities.*
import com.fh.payday.views2.payments.SelectPlanActivity
import kotlinx.android.synthetic.main.fragment_payment_amount.*
import kotlinx.android.synthetic.main.fragment_payment_amount.progress_bar


class PaymentAmountFragment : BaseFragment() {

    companion object {
        private const val REQUEST_SELECT_PLAN = 2
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_payment_amount, container, false)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            activity ?: return
            val activity = activity as InternationalTopUpActivity
            if (isVisibleToUser && activity.viewModel.dataClear) {
                et_amount?.text = null
            }

            val planType = getViewModel()?.planType ?: return
            val viewModel = getViewModel() ?: return

            viewModel.selectedPlan = null
            if (1 == getViewModel()?.selectedOperator?.isFixedAvailable
                            && 0 == getViewModel()?.selectedOperator?.isFlexAvailable) {
                et_amount.isFocusable = false
                setBrowsePlans()
                et_amount.setOnClickListener {
                    activity.hideKeyboard()
                    val intent = Intent(activity,
                            SelectPlanActivity::class.java).apply {
                        val plans = getViewModel()?.plans ?: return@setOnClickListener
                        putExtra("plans", plans)
                    }
                    startActivityForResult(intent, REQUEST_SELECT_PLAN)
                }
                et_amount.hint = viewModel.plans!![0].currency
            } else {
                et_amount.hint = viewModel.operatorDetailsFlexi?.baseCurrency
                browse_plan.visibility = View.GONE
            }

            setAmountBehavior(planType)
            attachListeners()
            attachObserver()
        }
    }

    private fun attachListeners() {
        et_amount.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(8, 2))

        et_amount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                til_amount.clearErrorMessage()
          //    if (TextUtils.isEmpty(et_amount.text.toString()))
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
                is InternationalTopUpActivity -> (activity as InternationalTopUpActivity?)?.viewModel
                else -> null
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
            }
            startActivityForResult(intent, REQUEST_SELECT_PLAN)
        }
    }

    private fun setAmountBehavior(planType: String) {
        val enabled = when (planType) {
            PlanType.FLEXI -> true
            else -> true
        }
        et_amount.isEnabled = enabled

        if (enabled) {
            et_amount.setOnTouchListener { _, _ -> false }
        } else {
            val plans = getViewModel()?.plans ?: return
            et_amount.setOnTouchListener { _, _ ->
                val intent = Intent(activity
                        ?: return@setOnTouchListener false, SelectPlanActivity::class.java)
                        .apply {
                            putExtra("plans", plans)
                        }
                startActivityForResult(intent, REQUEST_SELECT_PLAN)
                true
            }
        }

        if (getViewModel()?.planType == PlanType.FIXED) {
            et_amount.isCursorVisible = false
        }

        btn_next.setOnClickListener {
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

                if (isValid(amount, viewModel.operatorDetailsFlexi?.minDenomination!!.toInt(), viewModel.operatorDetailsFlexi?.maxDenomination!!.toInt())) {

                    if (activity?.isNetworkConnected() == false) {
                        activity?.onFailure(activity?.findViewById(R.id.card_view)!!, getString(R.string.no_internet_connectivity))
                        return@setOnClickListener
                    }

                    val user = viewModel.user ?: return@setOnClickListener
                    val accessKey = viewModel.selectedOperator?.accessKey
                            ?: return@setOnClickListener
                    val typeKey = viewModel.operatorDetailsFlexi?.typeKey?.toInt()
                            ?: viewModel.selectedPlan?.typeKey ?: return@setOnClickListener
                    val flexiKey = viewModel.operatorDetailsFlexi?.flexiKey
                            ?: return@setOnClickListener
                    viewModel.getPayableAmount(user.token, user.sessionId, user.refreshToken, user.customerId.toLong(),
                        accessKey, typeKey, flexiKey, amount.toDouble())
                }

            }

        }
    }
    private fun isValid(amount: String) : Boolean{
        return if (amount.trim().isEmpty()){
            til_amount.error
            tv_error_label.visibility = View.VISIBLE
            tv_error_label.text = getString(R.string.please_choose_plan)
            false
        }else {
            true
        }
    }
    private fun isValid(amount: String, minAmount: Int = 1, maxAmount: Int = Int.MAX_VALUE) =
            when {
                amount.trim().isEmpty() -> {
                    til_amount.setErrorMessage(getString(R.string.invalid_amount))
                    false
                }
                else -> {
                    try {
                        if (amount.toInt() in minAmount..maxAmount) {
                            true
                        } else {
                            val selectedPlan = getViewModel()?.planType

                            if (selectedPlan == PlanType.FLEXI) {
                                val curr = getViewModel()?.operatorDetailsFlexi?.baseCurrency ?: ""
                                //til_amount.setErrorMessage("Please enter an amount between $minAmount $curr and $maxAmount $curr")
                                til_amount.setErrorMessage(String.format(getString(R.string.invalid_amount_ranged_with_currency),
                                        minAmount, curr, maxAmount, curr))
                            }
                            false
                        }
                    } catch (e: NumberFormatException) {
                        til_amount.setErrorMessage(getString(R.string.invalid_amount))
                        false
                    }
                }
            }

    private fun onSuccess(activity: FragmentActivity?) {
        when (activity) {
            is InternationalTopUpActivity? -> activity?.navigateUp()
        }
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
                        ?: return@Observer, state.throwable)
                else -> activity?.onFailure(activity?.findViewById(R.id.root_view)
                        ?: return@Observer, CONNECTION_ERROR)
            }
        })
    }
}