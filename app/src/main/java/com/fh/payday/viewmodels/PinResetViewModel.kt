package com.fh.payday.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Base64
import com.fh.payday.datasource.models.ResetPin
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.cardpin.CardPinService
import com.fh.payday.utilities.Event
import com.fh.payday.utilities.encryption.aes.encryptWithAES
import com.fh.payday.utilities.encryption.aes.generateSecretKey
import com.fh.payday.utilities.encryption.rsa.encryptWithRSA

class PinResetViewModel : ViewModel() {

    private val _changePinResponse = MutableLiveData<Event<NetworkState2<ResetPin>>>()
    private val _otpResponse = MutableLiveData<Event<NetworkState2<String>>>()

    val optResponseState : LiveData<Event<NetworkState2<String>>>
        get() = _otpResponse

    val pinResponseState : LiveData<Event<NetworkState2<ResetPin>>>
        get() = _changePinResponse

    var pin = MutableLiveData<String>()
    var oldPin = MutableLiveData<String>()

    fun changePin(token: String, sessionId: String, refreshToken: String, byteArray: ByteArray, customerId: Long, pin: String, oldPin: String, otp: String) {

        _changePinResponse.value = Event(NetworkState2.Loading())
        val secret = Base64.encodeToString(generateSecretKey().encoded, Base64.DEFAULT).substring(0, 32)
        val encryptedPin = encryptWithAES(secret, pin)
        val encryptedOldPin = encryptWithAES(secret, oldPin)

        val encryptedSecret = encryptWithRSA(byteArray, secret)

        CardPinService.getInstance(token, sessionId, refreshToken).changePin(customerId, encryptedSecret, encryptedPin, encryptedOldPin, otp,
            object : ApiCallbackImpl<ResetPin>(_changePinResponse) {

                override fun onSuccess(data: ResetPin, message: String?) {
                    _changePinResponse.value = Event(NetworkState2.Success(data, message))
                }
                override fun onSuccess(data: ResetPin) { onSuccess(data, "") }
                override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                    _changePinResponse.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                }
            })
    }



    fun getOtp(token: String, sessionId: String, refreshToken: String, customerId: Long) {
        _otpResponse.value = Event(NetworkState2.Loading())

        CardPinService.getInstance(token, sessionId, refreshToken).getOtp( customerId,
            object : ApiCallbackImpl<String>(_otpResponse) {
                override fun onSuccess(data: String) {
                    _otpResponse.value = Event(NetworkState2.Success(data))
                }
            })

    }
}