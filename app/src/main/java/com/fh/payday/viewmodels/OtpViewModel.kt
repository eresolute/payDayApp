package com.fh.payday.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.otp.OtpService
import com.fh.payday.utilities.Event

class OtpViewModel : ViewModel() {
    private val _otpState = MutableLiveData<Event<NetworkState2<String>>>()

    val otpState: LiveData<Event<NetworkState2<String>>>
        get() {
            return _otpState
        }


    fun getOtp(token: String, sessionId: String, refreshToken: String, customerId: Int) {
        _otpState.value = Event(NetworkState2.Loading())

        OtpService.getInstance(token, sessionId, refreshToken).getOtp(customerId,
                object : ApiCallbackImpl<String>(_otpState) {
                    override fun onSuccess(data: String) {
                        _otpState.value = Event(NetworkState2.Success(data))
                    }
                })
    }

    fun getOtp(customerId: Int) {
        _otpState.value = Event(NetworkState2.Loading())

        OtpService().getOtp(customerId,
                object : ApiCallbackImpl<String>(_otpState) {
                    override fun onSuccess(data: String) {
                        _otpState.value = Event(NetworkState2.Success(data))
                    }
                })
    }

    fun credentialsResendOtp(customerId: Long, mobileNumber: String) {
        _otpState.value = Event(NetworkState2.Loading())

        OtpService().credentialResendOtp(customerId, mobileNumber,
                object : ApiCallbackImpl<String>(_otpState) {
                    override fun onSuccess(data: String) {
                        _otpState.value = Event(NetworkState2.Success(data))
                    }
                })
    }
}