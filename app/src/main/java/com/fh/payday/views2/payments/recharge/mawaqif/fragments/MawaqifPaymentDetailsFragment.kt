package com.fh.payday.views2.payments.recharge.mawaqif.fragments


import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fh.payday.BaseFragment
import com.fh.payday.R
import com.fh.payday.datasource.models.shared.ListModel
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.getDecimalValue
import com.fh.payday.views.shared.ListAdapter
import com.fh.payday.views2.payments.recharge.mawaqif.MawaqifTopUpActivity
import kotlinx.android.synthetic.main.fragment_mawaqif_payment_details.*

class MawaqifPaymentDetailsFragment : BaseFragment() {

    private fun getViewModel() = when (activity) {
        is MawaqifTopUpActivity -> (activity as MawaqifTopUpActivity).viewModel
        else -> null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_mawaqif_payment_details, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler_view.adapter = ListAdapter(arrayListOf(), activity ?: return, getString(R.string.payment_details), true)

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
    private fun attachObservers() {
        getViewModel()?.data?.observe(this, Observer {
            it ?: return@Observer
            handleView(it)
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
                    activity?.handleErrorCode(state.errorCode.toInt(), state.message)
                }
                else -> activity?.onFailure(activity?.findViewById(R.id.root_view)
                        ?: return@Observer, CONNECTION_ERROR)
            }
        })
    }

    private fun handleView(map: Map<String, String>) {
        val accountLabel =  getString(R.string.account_no_label).capitalize()
        val adapter = recycler_view.adapter

        if(adapter is ListAdapter) {

            val payableAmount = getString(R.string.payable_amount)
            val billedAmount = getString(R.string.mawaqif_card_balance)
            val mobileNo = if ((map["mobile_no"] ?: return ).startsWith("0")) (map["mobile_no"] ?: return ).substring(1) else map["mobile_no"]
            val list = arrayListOf(
                    ListModel(accountLabel, String.format(getString(R.string.plus_971_sign), mobileNo ?: return)),
                    ListModel(billedAmount, String.format(getString(R.string.amount_in_aed), map["bill_amount"]?.getDecimalValue() ?: return)),
                    ListModel(payableAmount, String.format(getString(R.string.amount_in_aed), map["payable_amount"]?.getDecimalValue() ?: return))
            )
            adapter.addAll(list)
        }
    }
    private fun onSuccess(activity: FragmentActivity?) {
        when (activity) {
            is MawaqifTopUpActivity? -> activity?.navigateUp()
        }
    }
}
