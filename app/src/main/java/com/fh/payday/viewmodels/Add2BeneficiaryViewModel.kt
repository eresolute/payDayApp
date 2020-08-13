package com.fh.payday.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.payments.PaymentService
import com.fh.payday.utilities.Event

class Add2BeneficiaryViewModel : ViewModel() {
    private val _add2BeneficiaryState = MutableLiveData<Event<NetworkState2<String>>>()
    val add2BeneficiaryState: LiveData<Event<NetworkState2<String>>>
        get() = _add2BeneficiaryState

    private val _enableBeneficiaryState = MutableLiveData<Event<NetworkState2<String>>>()
    val enableBeneficiaryState: LiveData<Event<NetworkState2<String>>> get() = _enableBeneficiaryState


    private val _deleteBeneficiaryState = MutableLiveData<Event<NetworkState2<String>>>()
    val deleteBeneficiaryState: LiveData<Event<NetworkState2<String>>> get() = _deleteBeneficiaryState

    fun add2Beneficiary(token: String, sessionId: String, refreshToken: String, customerId: Long,
                        mobileNumber: String, shortName: String, accessKey: String,
                        optional1:String? = null, optional2:String? = null, type: String? = null) {
        _add2BeneficiaryState.value = Event(NetworkState2.Loading())

        PaymentService.getInstance(token, sessionId, refreshToken)
            .add2Beneficiary(customerId, mobileNumber, shortName, accessKey, optional1, optional2, type,
                object : ApiCallbackImpl<String>(_add2BeneficiaryState) {
                    override fun onSuccess(data: String) {
                        _add2BeneficiaryState.value = Event(NetworkState2.Success(data))
                    }
                })
    }

    fun editBeneficiary(token: String, sessionId: String, refreshToken: String, customerId: Long, beneficiaryId: Long,
                        mobileNumber: String, shortName: String, accessKey: String) {
        _add2BeneficiaryState.value = Event(NetworkState2.Loading())

        PaymentService.getInstance(token, sessionId, refreshToken)
            .editBeneficiary(customerId, beneficiaryId, mobileNumber, shortName, accessKey,
                object : ApiCallbackImpl<String>(_add2BeneficiaryState) {
                    override fun onSuccess(data: String) {
                        _add2BeneficiaryState.value = Event(NetworkState2.Success(data))
                    }
                })
    }

    fun deleteBeneficiary(token: String, sessionId: String, refreshToken: String, customerId: Long, beneficiaryId: Long) {
        _deleteBeneficiaryState.value = Event(NetworkState2.Loading())

        PaymentService.getInstance(token, sessionId, refreshToken)
            .deleteBeneficiary(customerId, beneficiaryId,
                object : ApiCallbackImpl<String>(_deleteBeneficiaryState) {
                    override fun onSuccess(data: String) {
                        _deleteBeneficiaryState.value = Event(NetworkState2.Success(data))
                    }
                })
    }

    fun enableBeneficiary(token: String, sessionId: String, refreshToken: String, customerId: Long, beneficiaryId: Long, enable: Boolean) {
        _enableBeneficiaryState.value = Event(NetworkState2.Loading())

        PaymentService.getInstance(token, sessionId, refreshToken)
                .enableBeneficiary(customerId, beneficiaryId,
                        enable, object : ApiCallbackImpl<String>(_enableBeneficiaryState) {
                            override fun onSuccess(data: String) {
                                _enableBeneficiaryState.value = Event(NetworkState2.Success(data))
                            }
                        })
    }

}