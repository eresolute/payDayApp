package com.fh.payday.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.fh.payday.datasource.models.login.User
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.changepassword.ChangePasswordService
import com.fh.payday.utilities.Event

class ChangePasswordViewModel : ViewModel() {
    var oldPassword: String? = null
    var newPassword: String? = null
    var user : User? = null

    private val _updatePasswordState = MutableLiveData<Event<NetworkState2<String>>>()
    private val _otpGenerationState = MutableLiveData<Event<NetworkState2<String>>>()


    val updatePasswordState: LiveData<Event<NetworkState2<String>>>
        get() {
            return _updatePasswordState
        }

    val otpGenerationState: LiveData<Event<NetworkState2<String>>>
        get() {
            return _otpGenerationState
        }

    fun updatePassword(token: String, sessionId: String, refreshToken: String, customerId: Long, oldPassword: String, newPassword: String, otp: String) {
        _updatePasswordState.value = Event(NetworkState2.Loading())

        ChangePasswordService.getInstance(token, sessionId, refreshToken)
            .updatePassword(customerId, oldPassword, newPassword, otp,
                object : ApiCallbackImpl<String>(_updatePasswordState) {
                    override fun onSuccess(data: String) {
                        _updatePasswordState.value = Event(NetworkState2.Success(data))

                    }
                })
    }

    fun getOtp(token: String, sessionId: String, refreshToken: String, customerId: Long) {
        _otpGenerationState.value = Event(NetworkState2.Loading())

        ChangePasswordService.getInstance(token, sessionId, refreshToken)
            .getOtp(customerId, object : ApiCallbackImpl<String>(_otpGenerationState) {
                override fun onSuccess(data: String) {
                    _otpGenerationState.value = Event(NetworkState2.Success(data))
                }
            })
    }
}