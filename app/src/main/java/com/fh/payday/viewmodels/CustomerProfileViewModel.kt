package com.fh.payday.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.fh.payday.datasource.models.CustomerSummary
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.customerprofile.ProfileService
import com.fh.payday.utilities.Event

class CustomerProfileViewModel : ViewModel() {
    private val _customerProfileReq = MutableLiveData<Event<NetworkState2<CustomerSummary>>>()

    val customerProfileState: LiveData<Event<NetworkState2<CustomerSummary>>>
        get() = _customerProfileReq

    fun profileRequest(token: String, sessionId: String, refreshToken: String, customerId: Long) {
        _customerProfileReq.value = Event(NetworkState2.Loading())

        ProfileService.getInstance(token, sessionId, refreshToken)
            .getCustomerProfile(customerId,
                object : ApiCallbackImpl<CustomerSummary>(_customerProfileReq) {
                    override fun onSuccess(data: CustomerSummary) {
                        _customerProfileReq.value = Event(NetworkState2.Success(data))
                    }
                })
    }

}