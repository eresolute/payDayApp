package com.fh.payday.views2.payments.internationaltopup

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
import kotlinx.android.synthetic.main.fragment_payment_details.*

class PaymentDetailsFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_payment_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler_view.adapter = ListAdapter(arrayListOf(), activity
            ?: return, getString(R.string.payment_details))

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
        when (activity) {
            is InternationalTopUpActivity -> (activity as InternationalTopUpActivity?)?.viewModel
            else -> null
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
            is InternationalTopUpActivity? -> activity?.navigateUp()
        }
    }

    private fun handleView(map: Map<String, String>) {

        val accountLabel = getString(R.string.mobile_number)
        val adapter = recycler_view.adapter ?: return

        if (adapter is ListAdapter) {
            val list = arrayListOf(
                ListModel(accountLabel, map["account_no"] ?: return),
                ListModel(getString(R.string.amount), "${map["currency"]
                    ?: return} ${map["amount"]?.getDecimalValue() ?: return}"),
                ListModel(getString(R.string.payable_amount),
                    String.format(getString(R.string.amount_in_aed),
                        map["payable_amount"]?.getDecimalValue() ?: return))
            )
            adapter.addAll(list)
        }
    }
}
