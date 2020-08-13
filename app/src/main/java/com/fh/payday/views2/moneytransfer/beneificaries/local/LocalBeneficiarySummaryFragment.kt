package com.fh.payday.views2.moneytransfer.beneificaries.local

import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.os.LocaleList
import android.support.design.button.MaterialButton
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.views.adapter.moneytransfer.BeneficiaryDetailAdapter
import com.fh.payday.views2.moneytransfer.beneificaries.creditCard.CreditCardBeneficiaryActivity
import java.util.*
import kotlin.collections.LinkedHashMap

class LocalBeneficiarySummaryFragment : Fragment() {
    private lateinit var activity: BaseActivity
    private lateinit var rvSummary: RecyclerView
    private lateinit var btnConfirm: MaterialButton
    val list: MutableList<Map.Entry<String, String>> = ArrayList()


    override fun onAttach(context: Context?) {
        super.onAttach(context)

        activity = when(context) {
            is LocalBeneficiaryActivity -> context
            is CreditCardBeneficiaryActivity -> context
            else -> return
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if (isVisibleToUser) {
            rvSummary ?: return
            setUpRecyclerView()
            activity.hideKeyboard()
        }
    }
    private fun setUpRecyclerView() {
        val activity = activity

        list.clear()

        val map = when(activity) {
            is LocalBeneficiaryActivity -> activity.getViewModel().localBeneficiaryMap
            is CreditCardBeneficiaryActivity -> activity.getViewModel().ccBeneficiaryMap
            else -> return
        }

        for (key in map.entries) {
            list.add(key)
        }
        rvSummary.adapter = BeneficiaryDetailAdapter(list)
        rvSummary.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_local_beneficiary_summary, container, false)
        init(view)
        when(val activity = activity) {
            is LocalBeneficiaryActivity -> addObserver(activity)
            is CreditCardBeneficiaryActivity -> addObserver(activity)
        }
        return view
    }

    private fun addObserver(activity: BaseActivity) {

        when(activity) {
            is LocalBeneficiaryActivity ->
                activity.getViewModel().otpState.observe(this, Observer {
                it ?: return@Observer

                val otpState = it.getContentIfNotHandled() ?: return@Observer

                if (otpState is NetworkState2.Loading) {
                    activity.showProgress(getString(R.string.processing))
                    btnConfirm.isEnabled = false
                    return@Observer
                }

                activity.hideProgress()
                btnConfirm.isEnabled = true

                when (otpState) {
                    is NetworkState2.Success -> activity.navigateUp()
                    is NetworkState2.Error -> {
                        if(otpState.isSessionExpired)
                            return@Observer activity.onSessionExpired(otpState.message)

                        activity.onError(otpState.message)
                    }
                    is NetworkState2.Failure -> activity.onFailure(activity.findViewById(R.id.root_view), otpState.throwable)
                    else -> activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
                }
            })
            is CreditCardBeneficiaryActivity ->
                activity.getViewModel().otpState.observe(this, Observer {
                    it ?: return@Observer

                    val otpState = it.getContentIfNotHandled() ?: return@Observer

                    if (otpState is NetworkState2.Loading) {
                        activity.showProgress(getString(R.string.processing))
                        btnConfirm.isEnabled = false
                        return@Observer
                    }

                    activity.hideProgress()
                    btnConfirm.isEnabled = true

                    when (otpState) {
                        is NetworkState2.Success -> activity.navigateUp()
                        is NetworkState2.Error -> {
                            if(otpState.isSessionExpired)
                                return@Observer activity.onSessionExpired(otpState.message)

                            activity.onError(otpState.message)
                        }
                        is NetworkState2.Failure -> activity.onFailure(activity.findViewById(R.id.root_view), otpState.throwable)
                        else -> activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
                    }
                })

        }


    }

    fun init(view: View) {
        rvSummary = view.findViewById(R.id.rvLocalSummary)
        btnConfirm = view.findViewById(R.id.btn_confirm)

        btnConfirm.setOnClickListener {
            val user = UserPreferences.instance.getUser(activity) ?: return@setOnClickListener
            val id = user.customerId.toLong()

            when(val activity = activity) {
                is LocalBeneficiaryActivity -> activity.getViewModel().getOtp(user.token, user.sessionId, user.refreshToken, id)
                is CreditCardBeneficiaryActivity -> activity.getViewModel().getOtp(user.token, user.sessionId, user.refreshToken, id)
                else -> return@setOnClickListener
            }
        }
    }
}