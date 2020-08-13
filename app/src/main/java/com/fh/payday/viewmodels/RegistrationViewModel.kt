package com.fh.payday.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.fh.payday.datasource.models.CardLoanResult
import com.fh.payday.datasource.models.customer.CustomerResponse
import com.fh.payday.datasource.models.customer.RegisterCustomerRequest
import com.fh.payday.datasource.models.ui.RegisterInputWrapper
import com.fh.payday.datasource.models.ui.RegistrationWrapper
import com.fh.payday.datasource.remote.ApiCallback
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.asMultipartBody
import com.fh.payday.datasource.remote.asRequestBody
import com.fh.payday.datasource.remote.auth.RegistrationService
import com.fh.payday.datasource.remote.otp.OtpService
import com.fh.payday.utilities.Event
import com.fh.payday.utilities.encryption.aes.encryptWithAES
import com.fh.payday.utilities.encryption.aes.generateSecretKey
import com.fh.payday.utilities.encryption.rsa.encryptWithRSA
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream


class RegistrationViewModel : ViewModel() {

    companion object {
        const val CARD_CUSTOMER = "Payday_Customer"
        const val LOAN_CUSTOMER = "Loan_Customer"
    }

    var wrapper = RegistrationWrapper()
    var inputWrapper = RegisterInputWrapper()

    var isEmiratesDetailSet = false
    var isPaydayDetailSet = false

    private val _emiratesIdState = MutableLiveData<Event<NetworkState2<CardLoanResult>>>()
    private val _createCustomerState = MutableLiveData<Event<NetworkState2<CustomerResponse>>>()
    private val _otpGenerationState = MutableLiveData<Event<NetworkState2<String>>>()
    private val _verifyOtpState = MutableLiveData<Event<NetworkState2<String>>>()
    private val _dialogState = MutableLiveData<Event<NetworkState2<String>>>()
    private val _credentialsState = MutableLiveData<Event<NetworkState2<String>>>()
    private val _searchUserIdState = MutableLiveData<Event<NetworkState2<String>>>()
    /*private val _countriesState = MutableLiveData<Event<NetworkState2<List<IsoAlpha3>>>>()*/

    var day: Int = -1
    var month: Int = -1
    var year: Int = -1

    var expiryDay: Int = -1
    var expiryMonth: Int = -1
    var expiryYear: Int = -1

    val emiratesIdState: LiveData<Event<NetworkState2<CardLoanResult>>>
        get() = _emiratesIdState
    val createCustomerState: LiveData<Event<NetworkState2<CustomerResponse>>>
        get() = _createCustomerState
    val otpGenerationState: LiveData<Event<NetworkState2<String>>>
        get() = _otpGenerationState
    val verifyOtpState: LiveData<Event<NetworkState2<String>>>
        get() = _verifyOtpState
    val dialogState: LiveData<Event<NetworkState2<String>>>
        get() = _dialogState
    val credentialsState: LiveData<Event<NetworkState2<String>>>
        get() = _credentialsState
    val searchUserIdState: LiveData<Event<NetworkState2<String>>>
        get() = _searchUserIdState
    /*var countries: List<IsoAlpha3>? = null
        private set*/

    private val _uploadSelfieState = MutableLiveData<Event<NetworkState2<String>>>()
    @Deprecated("Upload Selfie is not required in registration")
    val uploadSelfieState: LiveData<Event<NetworkState2<String>>>
        get() = _uploadSelfieState

    private val disposable = CompositeDisposable()
    private val publishSubject = PublishRelay.create<String>()

    init {
        disposable.add(addTextObserver())
    }

    override fun onCleared() {
        disposable.dispose()
        super.onCleared()
    }

    fun submitEmiratesId(front: File, back: File, emiratesId: String, expiry: String,
                         dob: String, nationality: String, gender: String = "") {

        _emiratesIdState.value = Event(NetworkState2.Loading())

        val cFront = compressImageSize(front, 1280)
        val cBack = compressImageSize(back, 1280)
        
        val reqBodyFront = cFront.asRequestBody()
        val reqBodyBack = cBack.asRequestBody()
        val partFront = reqBodyFront.asMultipartBody("frontEmiratesID", cFront.name)
        val partBack = reqBodyBack.asMultipartBody("backEmiratesID", cBack.name)

        RegistrationService.instance.submitEmiratesId(partFront, partBack,
            emiratesId, expiry, dob, nationality, expiry == inputWrapper.emiratesIdExpiry, gender,
            object : ApiCallback<CardLoanResult> {
                override fun onFailure(t: Throwable) {
                    _emiratesIdState.value = Event(NetworkState2.Failure(t))
                }

                override fun onError(message: String) {
                    _emiratesIdState.value = Event(NetworkState2.Error(message))
                }

                override fun onError(message: String, code: Int) {
                    _emiratesIdState.value = Event(NetworkState2.Error(message, code.toString()))
                }

                override fun onSuccess(data: CardLoanResult) {
                    when(data.customerType) {
                        LOAN_CUSTOMER -> {
                            wrapper.customerId = data.id
                            if (!data.loans.isNullOrEmpty()) wrapper.loanDetail = data.loans[0]
                        }
                        else -> if (!data.cards.isNullOrEmpty()) wrapper.cardDetails = data.cards[0]
                    }
                    _emiratesIdState.value = Event(NetworkState2.Success(data))
                }
            })

        /*disposable.add(d)*/
    }

    fun createCustomer(keyBytes: ByteArray, cardNumber: String, cardName: String,
                       cardExpiry: String, pin: String, emiratesId: String) {
        val secret = Base64.encodeToString(generateSecretKey().encoded, Base64.DEFAULT).substring(0, 32)
        val unmaskedCardNo = unMaskCardNo(cardNumber) ?: return
        val eCardNumber = encryptWithAES(secret, unmaskedCardNo.replace("\\s+".toRegex(), ""))
        val eCardName = encryptWithAES(secret, cardName)
        val eCardExpiry = encryptWithAES(secret, cardExpiry)
        val ePin = encryptWithAES(secret, pin)
        val eEmiratesId = encryptWithAES(secret, emiratesId)

        val encryptedSecret = encryptWithRSA(keyBytes, secret)

        _createCustomerState.value = Event(NetworkState2.Loading())
        RegistrationService.instance.createCustomer(encryptedSecret, eCardNumber,
            eCardName, eCardExpiry, ePin, eEmiratesId,
            object : ApiCallback<CustomerResponse> {
                override fun onFailure(t: Throwable) {
                    _createCustomerState.value = Event(NetworkState2.Failure(t))
                }

                override fun onError(message: String) {
                    _createCustomerState.value = Event(NetworkState2.Error(message))
                }

                override fun onError(message: String, code: Int) {
                    _createCustomerState.value = Event(NetworkState2.Error(message, code.toString()))
                }

                override fun onSuccess(data: CustomerResponse) {
                    wrapper.customerId = data.id
                    _createCustomerState.value = Event(NetworkState2.Success(data))
                }
            })
    }

    fun generateOtp(customerId: Long, mobile: String) {
        _otpGenerationState.value = Event(NetworkState2.Loading())

        RegistrationService.instance.generateOtp(customerId, mobile,
            object : ApiCallback<String> {
                override fun onFailure(t: Throwable) {
                    _otpGenerationState.value = Event(NetworkState2.Failure(t))
                }

                override fun onError(message: String) {
                    _otpGenerationState.value = Event(NetworkState2.Error(message))
                }

                override fun onError(message: String, code: Int) {
                    _otpGenerationState.value = Event(NetworkState2.Error(message, code.toString()))
                }

                override fun onSuccess(data: String) {
                    _otpGenerationState.value = Event(NetworkState2.Success(data))
                }
            })
    }

    fun verifyOtp(customerId: Long, otp: String) {
        _verifyOtpState.value = Event(NetworkState2.Loading())

        RegistrationService.instance.verifyOtp(customerId, otp,
            object : ApiCallback<String> {
                override fun onFailure(t: Throwable) {
                    _verifyOtpState.value = Event(NetworkState2.Failure(t))
                }

                override fun onError(message: String) {
                    _verifyOtpState.value = Event(NetworkState2.Error(message))
                }

                override fun onSuccess(data: String) {
                    _verifyOtpState.value = Event(NetworkState2.Success(data))
                }
            })
    }

    fun resendOtp(customerId: Long, mobileNo:String) {
        _dialogState.value = Event(NetworkState2.Loading())

        OtpService().resendOtp(customerId, mobileNo, object : ApiCallback<String> {
            override fun onFailure(t: Throwable) {
                _dialogState.value = Event(NetworkState2.Failure(t))
            }

            override fun onError(message: String) {
                _dialogState.value = Event(NetworkState2.Error(message))
            }

            override fun onSuccess(data: String) {
                _dialogState.value = Event(NetworkState2.Success(data))
            }
        })
    }

    fun searchUserId(userId: String) {
        RegistrationService.instance.searchUserId(userId, object : ApiCallback<String> {
            override fun onFailure(t: Throwable) {
                _searchUserIdState.value = Event(NetworkState2.Failure(t))
            }

            override fun onError(message: String) {
                _searchUserIdState.value = Event(NetworkState2.Error(message))
            }

            override fun onSuccess(data: String) {
                _searchUserIdState.value = Event(NetworkState2.Success(data))
            }
        })
    }

    fun registerCredentials(userId: String, password: String, mobile: String, deviceId: String,
                            geoLocation: String, osVersion: String, appVersion: String, languageCode: String) {
        val request = RegisterCustomerRequest(userId, password, mobile, deviceId, geoLocation, osVersion,
            appVersion, languageCode)
        _credentialsState.value = Event(NetworkState2.Loading())

        val customerId = wrapper.customerId ?: return

        RegistrationService.instance.registerCredentials(customerId, request,
            object : ApiCallback<String> {
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
        //disposable.add(d)
    }

    /*fun getCountries() {
        _countriesState.value = Event(NetworkState2.Loading())

        val d = RegistrationService.instance.getCountries(
            object : ApiCallback<List<IsoAlpha3>> {
                override fun onFailure(t: Throwable) {
                    _countriesState.value = Event(NetworkState2.Failure(t))

                }

                override fun onError(message: String) {
                    _countriesState.value = Event(NetworkState2.Error(message))
                }

                override fun onSuccess(data: List<IsoAlpha3>) {
                    countries = data
                    _countriesState.value = Event(NetworkState2.Success(data))
                }
            })
        disposable.add(d)
    }*/

    @Deprecated("uploadSelfie() is deprecated")
    fun uploadSelfie(selfie: File) {
        val customerId = wrapper.customerId ?: return
        _uploadSelfieState.value = Event(NetworkState2.Loading())

        val selfieReqBody = RequestBody.create(MediaType.parse("image/jpeg"), selfie)
        val partSelfie = MultipartBody.Part.createFormData("selfie", selfie.name, selfieReqBody)

        RegistrationService.instance.uploadSelfie(customerId, partSelfie,
            object : ApiCallback<String> {
                override fun onFailure(t: Throwable) {
                    _uploadSelfieState.value = Event(NetworkState2.Failure(t))
                }

                override fun onError(message: String) {
                    _uploadSelfieState.value = Event(NetworkState2.Error(message))
                }

                override fun onSuccess(data: String) {
                    _uploadSelfieState.value = Event(NetworkState2.Success(data))
                }
            })
    }

    fun onUserIdTextChange(query: String) {
        publishSubject.accept(query.trim())
    }

    private fun addTextObserver(): Disposable {
        return RegistrationService.instance.configTextObserver(publishSubject, object : ApiCallback<String> {
            override fun onFailure(t: Throwable) {
                _searchUserIdState.value = Event(NetworkState2.Failure(t))
            }

            override fun onError(message: String) {
                _searchUserIdState.value = Event(NetworkState2.Error(message))
            }

            override fun onSuccess(data: String) {
                _searchUserIdState.value = Event(NetworkState2.Success(data))
            }
        })
    }

    private fun unMaskCardNo(cardNumber: String) =
        if (isMasked(cardNumber))
            wrapper.cardDetails?.cardNumber
        else cardNumber

    private fun isMasked(number: String) = number.contains("XXXX", true)

    fun clearWrapper() = wrapper.clear()

    fun clearInputWrapper() = inputWrapper.clear()

    private fun compressImageSize(file: File, maxSize: Int): File {
        val compressed = compressImageSize(BitmapFactory.decodeFile(file.path), maxSize)

        FileOutputStream(file).use {
            compressed?.compress(Bitmap.CompressFormat.JPEG, 80, it)
        }

        return file
    }

    private fun compressImageSize(bitmap: Bitmap?, maxSize: Int): Bitmap? {
        bitmap ?: return null
        var width = bitmap.width
        var height = bitmap.height

        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

}