package com.fh.payday.views2.payments.ibp

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fh.payday.BaseFragment
import com.fh.payday.R
import com.fh.payday.datasource.models.payments.BSNL_LANDLINE_ACCESS_KEY
import com.fh.payday.datasource.models.payments.BSNL_POSTPAID_ACCESS_KEY
import com.fh.payday.datasource.models.payments.MTNL_LANDLINE_ACCESS_KEY
import com.fh.payday.datasource.models.payments.ServiceCategory
import com.fh.payday.datasource.models.shared.ListModel
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.getDecimalValue
import com.fh.payday.views.shared.ListAdapter
import kotlinx.android.synthetic.main.fragment_payment_details.*

class PaymentDetailsFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_payment_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler_view.adapter = ListAdapter(arrayListOf(), activity ?: return, getString(R.string.payment_details))

        btn_confirm.setOnClickListener {
            val user = getViewModel()?.user ?: return@setOnClickListener

            if (activity?.isNetworkConnected() == false) {
                activity?.onFailure(activity?.findViewById(R.id.card_view)!!, getString(R.string.no_internet_connectivity))
                return@setOnClickListener
            }

            getViewModel()?.generateOtp(user.token, user.sessionId, user.refreshToken, user.customerId)
        }

        attachObservers()
    }

    private fun getViewModel() =
        when(activity) {
            is IndianBillPaymentActivity -> (activity as IndianBillPaymentActivity?)?.viewModel
            else -> null
        }

    private fun attachObservers() {
        getViewModel()?.data?.observe(this, Observer {
            it ?: return@Observer
            handleView(getViewModel()?.selectedOperator?.serviceCategory, it)
        })

        getViewModel()?.otpState?.observe(this, Observer {
            it ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                activity?.showProgress(getString(R.string.processing))
                return@Observer
            }

            activity?.hideProgress()

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
                else -> activity?.onFailure(activity?.findViewById(R.id.root_view)
                    ?: return@Observer, CONNECTION_ERROR)
            }
        })
    }

    private fun onSuccess(activity: FragmentActivity?) {
        when (activity) {
            is IndianBillPaymentActivity? -> activity?.navigateUp()
        }
    }

    private fun handleView(category: String?, map: Map<String, String>) {
        val accountLabel = when(category) {
            ServiceCategory.POSTPAID, ServiceCategory.PREPAID -> getString(R.string.mobile_number)
            ServiceCategory.LANDLINE -> getString(R.string.landline_number)
            ServiceCategory.DTH -> getString(R.string.customer_id)
            ServiceCategory.INSURANCE -> getString(R.string.policy_number).capitalize()
            ServiceCategory.ELECTRICITY -> getString(R.string.consumer_number).capitalize()
            else -> getString(R.string.account_no_label).capitalize()
        }

        val adapter = recycler_view.adapter ?: return

        if(adapter is ListAdapter) {

            val operator = getViewModel()?.selectedOperator
            var amount = getString(R.string.payable_amount)
            var billedAmount = getString(R.string.bill_amount)
            var payableAmount = getString(R.string.converted_payable_amount)
            if(operator?.serviceCategory == ServiceCategory.POSTPAID && operator.accessKey == BSNL_POSTPAID_ACCESS_KEY){
                amount = getString(R.string.payable_amount)
                payableAmount = getString(R.string.converted_payable_amount)
            }
            if (operator?.serviceCategory == ServiceCategory.LANDLINE && operator.accessKey
                in arrayOf(BSNL_LANDLINE_ACCESS_KEY, MTNL_LANDLINE_ACCESS_KEY)){
                amount = getString(R.string.payable_amount)
                payableAmount = getString(R.string.converted_payable_amount)
            }
            val list = arrayListOf(
                    if (operator?.serviceCategory == ServiceCategory.POSTPAID || operator?.serviceCategory == ServiceCategory.PREPAID){
                        ListModel(accountLabel, String.format(getString(R.string.plus_91_sign),map["account_no"] ?: return))
                    }else {
                        ListModel(accountLabel, map["account_no"] ?: return)
                    },
                        ListModel(amount, String.format(getString(R.string.amount_in_inr), "${map["currency"]?.getDecimalValue()
                                ?: return} ${map["amount"]?.getDecimalValue() ?: return}")),
                        ListModel(payableAmount, String.format(getString(R.string.amount_in_aed),
                                map["payable_amount"]?.getDecimalValue() ?: return))
            )

            if (!map["bill_amount"].isNullOrEmpty() && amount != getString(R.string.amount)) {
                list.add(1, ListModel(billedAmount, String.format(getString(R.string.amount_in_inr),
                        "${map["currency"]?.getDecimalValue() ?: return} ${map["bill_amount"]?.getDecimalValue() ?: return}")))
//                list[1] = ListModel(billedAmount, String.format(getString(R.string.amount_in_inr),
//                                "${map["currency"]?.getDecimalValue() ?: return} ${map["bill_amount"]?.getDecimalValue() ?: return}"))
            }

            when (operator?.serviceCategory) {
                ServiceCategory.LANDLINE -> {
                    if (map.containsKey("std_code")) {
                        list.add(0, ListModel(getString(R.string.std_code), map["std_code"] ?: return))
                    }
                    if (map.containsKey("bsnl_account") && operator.accessKey
                        in arrayOf(BSNL_LANDLINE_ACCESS_KEY, MTNL_LANDLINE_ACCESS_KEY)) {
                        list.add(1, ListModel(getString(R.string.account_no_label), map["bsnl_account"] ?: return))
                    }
                }
                ServiceCategory.INSURANCE -> {
                    if (map.containsKey("dob")) {
                        list.add(1, ListModel(getString(R.string.dob), map["dob"] ?: return))
                    }
                }
                ServiceCategory.PREPAID -> {
                    if (map.containsKey("state_name")){
                        list.add(1, ListModel(getString(R.string.state), map["state_name"] ?: return) )
                    }
                }
            }

            adapter.addAll(list)
        }

    }
}
