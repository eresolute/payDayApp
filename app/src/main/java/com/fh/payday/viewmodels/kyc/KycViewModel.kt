package com.fh.payday.viewmodels.kyc

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.fh.payday.datasource.models.IsoAlpha3
import com.fh.payday.datasource.models.Profile
import com.fh.payday.datasource.models.customer.EmiratesID
import com.fh.payday.datasource.models.ui.RegisterInputWrapper
import com.fh.payday.datasource.remote.ApiCallback
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.auth.RegistrationService
import com.fh.payday.datasource.remote.kyc.KycService
import com.fh.payday.utilities.Event
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class KycViewModel : ViewModel() {

    private val _profile = MutableLiveData<Event<NetworkState2<Profile>>>()
    private val _updateEmail = MutableLiveData<Event<NetworkState2<String>>>()
    private val _updateMobile = MutableLiveData<Event<NetworkState2<String>>>()
    private val _updatePassport = MutableLiveData<Event<NetworkState2<String>>>()
    private val _otpRequest = MutableLiveData<Event<NetworkState2<String>>>()
    private val _uploadEmiratesState = MutableLiveData<Event<NetworkState2<String>>>()
    private val _scanEmiratesIdState = MutableLiveData<Event<NetworkState2<EmiratesID>>>()
    private val _emiratesDetail = MutableLiveData<Event<NetworkState2<String>>>()
    private val _uploadPassport = MutableLiveData<Event<NetworkState2<String>>>()
    private val _updateEmergency = MutableLiveData<Event<NetworkState2<String>>>()
    private val _countriesState = MutableLiveData<Event<NetworkState2<List<IsoAlpha3>>>>()
    var inputWrapper = RegisterInputWrapper()

    var day: Int = -1
    var month: Int = -1
    var year: Int = -1

    var expiryDay: Int = -1
    var expiryMonth: Int = -1
    var expiryYear: Int = -1

    val profileState: LiveData<Event<NetworkState2<Profile>>>
        get() {
            return _profile
        }

    val updateEmail: LiveData<Event<NetworkState2<String>>>
        get() {
            return _updateEmail
        }

    val updateMobile: LiveData<Event<NetworkState2<String>>>
        get() {
            return _updateMobile
        }

    val updatePassport: LiveData<Event<NetworkState2<String>>>
        get() {
            return _updatePassport
        }

    val otpRequest: LiveData<Event<NetworkState2<String>>>
        get() {
            return _otpRequest
        }

    val scanEmiratesIdState: LiveData<Event<NetworkState2<EmiratesID>>>
        get() {
            return _scanEmiratesIdState
        }

    val uploadEmirates: LiveData<Event<NetworkState2<String>>>
        get() {
            return _uploadEmiratesState
        }

    val emiratesDetail: LiveData<Event<NetworkState2<String>>>
        get() {
            return _emiratesDetail
        }

    val uploadPassport: LiveData<Event<NetworkState2<String>>>
        get() {
            return _uploadPassport
        }

    val updateEmergency: LiveData<Event<NetworkState2<String>>>
        get() {
            return _updateEmergency
        }

    private val _uploadSelfieState = MutableLiveData<Event<NetworkState2<String>>>()
    val uploadSelfieState: LiveData<Event<NetworkState2<String>>>
        get() = _uploadSelfieState

    var countries: List<IsoAlpha3>? = null
        private set

    var email: String? = null
    var mobileNumber: String? = null
    var passportNumber: String? = null

    var passportPhoto: File? = null
    var passportSignature: File? = null

    var emergencyName: String? = null
    var emergencyMobile: String? = null
    var emergencyRelation: String? = null
    var isEmirateDetailSet: Boolean = false

    var emiratesScanCount: Int = 0
        private set

    fun incrementEScanCount() = ++emiratesScanCount


    fun getProfile(token: String, sessionId: String, refreshToken: String, customerId: Long) {
        _profile.value = Event(NetworkState2.Loading())

        KycService.getInstance(token, sessionId, refreshToken).getProfile(customerId,
                object : ApiCallbackImpl<Profile>(_profile) {
                    override fun onSuccess(data: Profile) {
                        _profile.value = Event(NetworkState2.Success(data))
                    }

                })
    }

    fun updateEmail(token: String, sessionId: String, refreshToken: String, customerId: Long, email: String, otp: String) {
        _updateEmail.value = Event(NetworkState2.Loading())

        KycService.getInstance(token, sessionId, refreshToken).updateEmail(customerId, email, otp,
                object : ApiCallbackImpl<String>(_updateEmail) {
                    override fun onSuccess(data: String) {
                        _updateEmail.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _updateEmail.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))

                    }
                })
    }

    fun updateMobile(token: String, sessionId: String, refreshToken: String, customerId: Long, mobileNumber: String, otp: String) {
        _updateMobile.value = Event(NetworkState2.Loading())

        KycService.getInstance(token, sessionId, refreshToken).updateMobile(customerId, mobileNumber, otp,
                object : ApiCallbackImpl<String>(_updateMobile) {
                    override fun onSuccess(data: String) {
                        _updateMobile.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _updateMobile.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })
    }

    fun updatePassport(token: String, sessionId: String, refreshToken: String, customerId: Long, passportNumber: String, otp: String) {
        _updatePassport.value = Event(NetworkState2.Loading())

        KycService.getInstance(token, sessionId, refreshToken).updatePassport(customerId, passportNumber, otp,
                object : ApiCallbackImpl<String>(_updatePassport) {
                    override fun onSuccess(data: String) {
                        _updatePassport.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _updatePassport.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })
    }

    fun generateOtp(token: String, sessionId: String, refreshToken: String, customerId: Long) {
        _otpRequest.value = Event(NetworkState2.Loading())

        KycService.getInstance(token, sessionId, refreshToken).getOtp(customerId,
                object : ApiCallbackImpl<String>(_otpRequest) {
                    override fun onSuccess(data: String) {
                        _otpRequest.value = Event(NetworkState2.Success(data))
                    }
                })
    }

    /*fun scanEmiratesId(customerId: Long, cardImage: File) {
        _scanEmiratesIdState.value = Event(NetworkState2.Loading())
        val reqBody = RequestBody.create(MediaType.parse("image/jpeg"), cardImage)
        val multiPart = MultipartBody.Part.createFormData("scan", cardImage.name, reqBody)

        KycService.instance.scanEmiratesId(customerId, multiPart, object : ApiCallback<EmiratesID> {
            override fun onFailure(t: Throwable) {
                _scanEmiratesIdState.value = Event(NetworkState2.Failure(t))
            }

            override fun onError(message: String) {
                _scanEmiratesIdState.value = Event(NetworkState2.Error(message))
            }

            override fun onSuccess(data: EmiratesID) {
                _scanEmiratesIdState.value = Event(NetworkState2.Success(data))
            }
        })
    }*/

    /*fun uploadEmirates(customerId: Long, front: File?, back: File?) {
        val reqBodyFront = RequestBody.create(MediaType.parse("image/jpeg"), front)
        val partFront = MultipartBody.Part.createFormData("frontEmiratesID", front?.name, reqBodyFront)
        val reqBodyBack = RequestBody.create(MediaType.parse("image/jpeg"), back)
        val partBack = MultipartBody.Part.createFormData("backEmiratesID", back?.name, reqBodyBack)

        _uploadEmiratesState.value = Event(NetworkState2.Loading())

        KycService.instance.uploadEmiratesId(customerId, partFront, partBack, object : ApiCallback<String> {
            override fun onFailure(t: Throwable) {
                _uploadEmiratesState.value = Event(NetworkState2.Failure(t))
            }

            override fun onError(message: String) {
                _uploadEmiratesState.value = Event(NetworkState2.Error(message))
            }

            override fun onSuccess(data: String) {
                _uploadEmiratesState.value = Event(NetworkState2.Success(data))
            }
        })

    } */

    fun updateEmirates(token: String, sessionId: String, refreshToken: String, customerId: Long, front: File, back: File, emiratesId: String,
                       emiratesExpiry: String, country: String, gender: String, dob: String, otp: String) {
         _emiratesDetail.value = Event(NetworkState2.Loading())

        val reqFrontPhoto = RequestBody.create(MediaType.parse("image/jpeg"), front)
        val frontImage = MultipartBody.Part.createFormData("frontEmiratesID", front.name, reqFrontPhoto)

        val reqBackPhoto = RequestBody.create(MediaType.parse("image/jpeg"), back)
        val backImage = MultipartBody.Part.createFormData("backEmiratesID", back.name, reqBackPhoto)


        KycService.getInstance(token, sessionId, refreshToken).updateEmiratesId(customerId, frontImage,
                backImage, emiratesId, emiratesExpiry, country, gender, dob, otp,
                object : ApiCallbackImpl<String>(_emiratesDetail) {

                    override fun onSuccess(data: String) {
                        _emiratesDetail.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _emiratesDetail.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })
    }


    fun uploadPassport(token: String, sessionId: String, refreshToken: String, customerId: Long, passportPhoto: File?, passportSignature: File?, passportAddress: File) {
        val reqBodyPhoto = RequestBody.create(MediaType.parse("image/jpeg"), passportPhoto!!)
        val partPhoto = MultipartBody.Part.createFormData("photo", passportPhoto.name, reqBodyPhoto)
        val reqBodySign = RequestBody.create(MediaType.parse("image/jpeg"), passportSignature!!)
        val partSign = MultipartBody.Part.createFormData("signature", passportSignature.name, reqBodySign)
        val reqBodyAddress = RequestBody.create(MediaType.parse("image/jpeg"), passportAddress)
        val partAddress = MultipartBody.Part.createFormData("address", passportAddress.name, reqBodyAddress)

        _uploadPassport.value = Event(NetworkState2.Loading())

        KycService.getInstance(token, sessionId, refreshToken).uploadPassport(customerId, partPhoto, partSign, partAddress,
                object : ApiCallbackImpl<String>(_uploadPassport) {
                    override fun onSuccess(data: String) {
                        _uploadPassport.value = Event(NetworkState2.Success(data))
                    }
                })
    }

    fun updateEmergency(token: String, sessionId: String, refreshToken: String, customerId: Long, name: String, mobile: String, relation: String) {
        _updateEmergency.value = Event(NetworkState2.Loading())

        KycService.getInstance(token, sessionId, refreshToken).updateEmergencyContact(customerId, name, mobile, relation,
                object : ApiCallbackImpl<String>(_updateEmergency) {
                    override fun onSuccess(data: String) {
                        _updateEmergency.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _updateEmergency.value = Event(NetworkState2.Error(message, code.toString()))
                    }
                })
    }

    fun uploadSelfie(token: String, sessionId: String, refreshToken: String, customerId: Long, selfie: File) {
        _uploadSelfieState.value = Event(NetworkState2.Loading())

        val selfieReqBody = RequestBody.create(MediaType.parse("image/jpeg"), selfie)
        val partSelfie = MultipartBody.Part.createFormData("selfie", selfie.name, selfieReqBody)

        KycService.getInstance(token, sessionId, refreshToken).uploadSelfie(customerId, partSelfie,
                object : ApiCallbackImpl<String>(_uploadSelfieState) {
                    override fun onSuccess(data: String) {
                        _uploadSelfieState.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _uploadSelfieState.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })
    }

    fun getCountries() {
        _countriesState.value = Event(NetworkState2.Loading())

        RegistrationService.instance.getCountries(
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
    }


}