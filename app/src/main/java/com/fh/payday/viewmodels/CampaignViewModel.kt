package com.fh.payday.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.Bundle
import com.fh.payday.datasource.models.CustomerSummary
import com.fh.payday.datasource.models.login.User
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.campaign.CampaignService
import com.fh.payday.datasource.remote.customer.CustomerService
import com.fh.payday.utilities.Event

class CampaignViewModel : ViewModel() {

    private val _customerSummaryState = MutableLiveData<Event<NetworkState2<CustomerSummary>>>()
    val customerSummaryState: LiveData<Event<NetworkState2<CustomerSummary>>> get() = _customerSummaryState

    private val _snoozeState = MutableLiveData<Event<NetworkState2<String>>>()
    val snoozeState: LiveData<Event<NetworkState2<String>>> get() = _snoozeState

    var user: User? = null

    fun fetchCustomerSummary(token: String, sessionId: String, refreshToken: String, customerId: Long) {
        _customerSummaryState.value = Event(NetworkState2.Loading())

        CustomerService.getInstance(token, sessionId, refreshToken)
            .getSummary(customerId,
                object : ApiCallbackImpl<CustomerSummary>(_customerSummaryState) {
                    override fun onSuccess(data: CustomerSummary, message: String?) {
                        _customerSummaryState.value = Event(NetworkState2.Success(data, message))
                    }

                    override fun onSuccess(data: CustomerSummary) {
                        onSuccess(data, null)
                    }
                })
    }

    fun updateSnoozeDays(
        token: String,
        sessionId: String,
        refreshToken: String,
        customerId: Long,
        snoozeDays: Int
    ) {
        _snoozeState.value = Event(NetworkState2.Loading())

        CampaignService.getInstance(token, sessionId, refreshToken)
            .updateSnoozeDays(customerId, snoozeDays,
                object : ApiCallbackImpl<String>(_snoozeState) {
                    override fun onSuccess(data: String) {
                        _snoozeState.value = Event(NetworkState2.Success(data))
                    }
                })
    }

    fun isValidOffer(bundle: Bundle?): Boolean {
        bundle ?: return false
        val title: String? = bundle.getString("title")
        val description: String? = bundle.getString("body")
        val expiryDays: String? = bundle.getString("expiryDays")
        val notificationId: String? = bundle.getString("notificationId")
        val productType: String? = bundle.getString("productType")

        return !title.isNullOrEmpty() && !description.isNullOrEmpty() && /*!expiryDays.isNullOrEmpty()
            && !notificationId.isNullOrEmpty() &&*/ !productType.isNullOrEmpty()
    }
}