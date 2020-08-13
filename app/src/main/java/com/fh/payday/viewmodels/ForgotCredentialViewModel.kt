package com.fh.payday.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Base64
import com.fh.payday.datasource.models.forgotcredential.ValidateCardResponse
import com.fh.payday.datasource.remote.ApiCallback
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.forgotcredential.ForgotUserIDService
import com.fh.payday.utilities.Event
import com.fh.payday.utilities.encryption.aes.encryptWithAES
import com.fh.payday.utilities.encryption.aes.generateSecretKey
import com.fh.payday.utilities.encryption.rsa.encryptWithRSA

class ForgotCredentialViewModel : ViewModel() {

    var customerId: Long? = null
    var mobileNumber: String? = null
    var username: String? = null
    var newUserPassword: String? = null

    private val _pinVerificationState = MutableLiveData<Event<NetworkState2<ValidateCardResponse>>>()
    private val _validateLoanAccountNoState = MutableLiveData<Event<NetworkState2<ValidateCardResponse>>>()
    private val _credentialsState = MutableLiveData<Event<NetworkState2<String>>>()
    private val _otpGenerationState = MutableLiveData<Event<NetworkState2<String>>>()
    private val _otpGenerationPasswordState = MutableLiveData<Event<NetworkState2<String>>>()
    private val _forgotPasswordState = MutableLiveData<Event<NetworkState2<String>>>()
    private val _userNameState = MutableLiveData<Event<NetworkState2<String>>>()

    val credentialsState: LiveData<Event<NetworkState2<String>>>
        get() {
            return _credentialsState
        }
    val forgotPasswordState: LiveData<Event<NetworkState2<String>>>
        get() {
            return _forgotPasswordState
        }
    val otpGenerationState: LiveData<Event<NetworkState2<String>>>
        get() {
            return _otpGenerationState
        }
    val otpGenerationPasswordState: LiveData<Event<NetworkState2<String>>>
        get() {
            return _otpGenerationPasswordState
        }
    val pinVerificationState: LiveData<Event<NetworkState2<ValidateCardResponse>>>
        get() {
            return _pinVerificationState
        }
    val validateLoanAccountNo: LiveData<Event<NetworkState2<ValidateCardResponse>>>
        get() {
            return _validateLoanAccountNoState
        }
    val userNameState: LiveData<Event<NetworkState2<String>>>
        get() {
            return _userNameState
        }

    fun forgotPassword(keyBytes: ByteArray, userName: String, customerId: Long, userPassword: String, otp: String) {
        _forgotPasswordState.value = Event(NetworkState2.Loading())

        val secret = Base64.encodeToString(generateSecretKey().encoded, Base64.DEFAULT).substring(0, 32)
        val eUserPassword = encryptWithAES(secret, userPassword)
        val encryptedSecret = encryptWithRSA(keyBytes, secret)

        ForgotUserIDService.instance.forgotPassword(userName, customerId, eUserPassword, otp, encryptedSecret, object : ApiCallback<String> {
            override fun onFailure(t: Throwable) {
                _forgotPasswordState.value = Event(NetworkState2.Failure(t))
            }

            override fun onError(message: String) {
                _forgotPasswordState.value = Event(NetworkState2.Error(message))
            }

            override fun onError(message: String, code: Int) {
                _forgotPasswordState.value = Event(NetworkState2.Error(message, code.toString()))
            }

            override fun onSuccess(data: String) {
                _credentialsState.value = Event(NetworkState2.Success(data))
                newUserPassword = eUserPassword
            }
        })
    }

    fun forgotUserId(customerId: Long, otp: String) {
        _credentialsState.value = Event(NetworkState2.Loading())

        ForgotUserIDService.instance.forgotUserID(customerId, otp, object : ApiCallback<String> {
            override fun onFailure(t: Throwable) {
                _credentialsState.value = Event(NetworkState2.Failure(t))
            }

            override fun onError(message: String) {
                _credentialsState.value = Event(NetworkState2.Error(message))
            }

            override fun onSuccess(data: String) {
                _credentialsState.value = Event(NetworkState2.Success(data))
            }
        })
    }

    fun pinVerification(keyBytes: ByteArray, cardNumber: String, cardPin: String) {
        _pinVerificationState.value = Event(NetworkState2.Loading())

        val secret = Base64.encodeToString(generateSecretKey().encoded, Base64.DEFAULT).substring(0, 32)
        //val eCardNumber = encryptWithAES(secret, cardNumber)
        val eCardPin = encryptWithAES(secret, cardPin)

        val encryptedSecret = encryptWithRSA(keyBytes, secret)
        ForgotUserIDService.instance.pinVerification(encryptedSecret, cardNumber, eCardPin, object : ApiCallback<ValidateCardResponse> {
            override fun onFailure(t: Throwable) {
                _pinVerificationState.value = Event(NetworkState2.Failure(t))
            }

            override fun onError(message: String) {
                _pinVerificationState.value = Event(NetworkState2.Error(message))
            }

            override fun onSuccess(data: ValidateCardResponse) {
                customerId = data.customerId
                mobileNumber = data.mobileNumber
                _pinVerificationState.value = Event(NetworkState2.Success(data))

            }
        })

    }

    fun validateLoanAccountNo(emiratesId: String) {
        _validateLoanAccountNoState.value = Event(NetworkState2.Loading())

        ForgotUserIDService.instance.validateLoanAccountNo(emiratesId, object : ApiCallback<ValidateCardResponse> {
            override fun onFailure(t: Throwable) {
                _validateLoanAccountNoState.value = Event(NetworkState2.Failure(t))
            }

            override fun onError(message: String) {
                _validateLoanAccountNoState.value = Event(NetworkState2.Error(message))
            }

            override fun onSuccess(data: ValidateCardResponse) {
                customerId = data.customerId
                mobileNumber = data.mobileNumber
                _validateLoanAccountNoState.value = Event(NetworkState2.Success(data))

            }
        })
    }

    fun getUserName(customerId: Long, userName: String) {
        _userNameState.value = Event(NetworkState2.Loading())
        ForgotUserIDService.instance.getUserName(customerId, userName, object : ApiCallback<String> {
            override fun onFailure(t: Throwable) {
                _userNameState.value = Event(NetworkState2.Failure(t))
            }

            override fun onError(message: String) {
                _userNameState.value = Event(NetworkState2.Error(message))
            }

            override fun onSuccess(data: String) {
                username = userName
                _userNameState.value = Event(NetworkState2.Success(data))
            }

        })
    }

    fun getOtp(customerId: Long, mobileNo: String) {
        _otpGenerationState.value = Event(NetworkState2.Loading())

        ForgotUserIDService.instance.getOtp(customerId, mobileNo, object : ApiCallback<String> {
            override fun onFailure(t: Throwable) {
                _otpGenerationState.value = Event(NetworkState2.Failure(t))
            }

            override fun onError(message: String) {
                _otpGenerationState.value = Event(NetworkState2.Error(message))
            }

            override fun onSuccess(data: String) {
                _otpGenerationState.value = Event(NetworkState2.Success(data))

            }

        })
    }

    fun getOtpPassword(customerId: Long, mobileNo: String) {
        _otpGenerationPasswordState.value = Event(NetworkState2.Loading())

        ForgotUserIDService.instance.getOtp(customerId, mobileNo, object : ApiCallback<String> {
            override fun onFailure(t: Throwable) {
                _otpGenerationPasswordState.value = Event(NetworkState2.Failure(t))
            }

            override fun onError(message: String) {
                _otpGenerationPasswordState.value = Event(NetworkState2.Error(message))
            }

            override fun onSuccess(data: String) {
                _otpGenerationPasswordState.value = Event(NetworkState2.Success(data))

            }

        })
    }
}