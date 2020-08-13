package com.fh.payday.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.fh.payday.datasource.models.CustomerSummary
import com.fh.payday.datasource.models.login.User
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.customer.CustomerService
import com.fh.payday.utilities.Event

class SettingOptionViewModel : ViewModel() {

    var user: User? = null
    val selected = MutableLiveData<Int>()
    fun setSelected(i: Int?) {
        selected.value = i
    }

    private val _customerSummaryState = MutableLiveData<Event<NetworkState2<CustomerSummary>>>()
    val customerSummaryState: LiveData<Event<NetworkState2<CustomerSummary>>>
        get() { return _customerSummaryState }

    fun fetchCustomerSummary(token: String, sessionId: String, refreshToken: String, customerId: Long) {
        _customerSummaryState.value = Event(NetworkState2.Loading())

        CustomerService.getInstance(token, sessionId, refreshToken).getSummary(customerId,
                object : ApiCallbackImpl<CustomerSummary>(_customerSummaryState) {
                    override fun onSuccess(data: CustomerSummary) {
                        _customerSummaryState.value = Event(NetworkState2.Success(data))
                    }
                })
    }

}