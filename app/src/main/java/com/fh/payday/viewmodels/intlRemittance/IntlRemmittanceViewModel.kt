package com.fh.payday.viewmodels.intlRemittance

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.fh.payday.datasource.models.intlRemittance.CustomerDetail
import com.fh.payday.datasource.models.intlRemittance.Exchange
import com.fh.payday.datasource.models.intlRemittance.Otp
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.intlRemittance.IntlRemittanceService
import com.fh.payday.utilities.Event
import com.fh.payday.views2.intlRemittance.ExchangeContainer

class IntlRemmittanceViewModel : ViewModel() {

    private val _customerState = MutableLiveData<Event<NetworkState2<List<Exchange>>>>()
    val customerState: LiveData<Event<NetworkState2<List<Exchange>>>> = _customerState

    private val _searchCustomerState = MutableLiveData<Event<NetworkState2<CustomerDetail>>>()
    val searchCustomerState: LiveData<Event<NetworkState2<CustomerDetail>>> = _searchCustomerState

    private val _otpState = MutableLiveData<Event<NetworkState2<Otp>>>()
    val otpState: LiveData<Event<NetworkState2<Otp>>> = _otpState

    private val _activateCustomerState = MutableLiveData<Event<NetworkState2<Any>>>()
    val activateCustomerState: LiveData<Event<NetworkState2<Any>>> = _activateCustomerState

    fun getCustomerState(
        token: String,
        sessionId: String,
        refreshToken: String,
        customerId: String,
        confirmationStatus: String? = null
    ) {
        _customerState.value = Event(NetworkState2.Loading())

        IntlRemittanceService.getInstance(token, sessionId, refreshToken)
            .getCustomerState(customerId, confirmationStatus, object : ApiCallbackImpl<List<Exchange>>(_customerState) {
                override fun onSuccess(data: List<Exchange>) {
                    if (!data.isNullOrEmpty()) {
                        ExchangeContainer.addAll(data)
                    }
                    _customerState.value = Event(NetworkState2.Success(data))
                }
            })
    }

    fun findCustomer(
        token: String,
        sessionId: String,
        refreshToken: String,
        customerId: String,
        emiratesCardNumber: String
    ) {
        _searchCustomerState.value = Event(NetworkState2.Loading())

        IntlRemittanceService.getInstance(token, sessionId, refreshToken)
            .searchCustomer(
                customerId,
                emiratesCardNumber,
                object : ApiCallbackImpl<CustomerDetail>(_searchCustomerState) {
                    override fun onSuccess(data: CustomerDetail) {
                        _searchCustomerState.value = Event(NetworkState2.Success(data))
                    }
                })
    }

    fun generateOtp(
        token: String,
        sessionId: String,
        refreshToken: String,
        customerId: Long,
        accessKey: String,
        sendOtp: String
    ) {
        _otpState.value = Event(NetworkState2.Loading())

        IntlRemittanceService.getInstance(token, sessionId, refreshToken)
            .generateOtp(
                customerId,
                accessKey,
                sendOtp,
                object : ApiCallbackImpl<Otp>(_otpState) {
                    override fun onSuccess(data: Otp) {
                        _otpState.value = Event(NetworkState2.Success(data))
                    }
                })
    }

    fun activateCustomer(
        token: String,
        sessionId: String,
        refreshToken: String,
        customerId: Long,
        accessKey: String,
        otp: String
    ) {
        _activateCustomerState.value = Event(NetworkState2.Loading())

        IntlRemittanceService.getInstance(token, sessionId, refreshToken)
            .activateCustomer(
                customerId,
                accessKey,
                otp,
                object : ApiCallbackImpl<Any>(_activateCustomerState) {
                    override fun onSuccess(data: Any) {
                        _activateCustomerState.value = Event(NetworkState2.Success(data))
                    }
                })
    }
}