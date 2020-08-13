package com.fh.payday.datasource.remote.kyc

import com.fh.payday.datasource.models.Profile
import com.fh.payday.datasource.models.customer.EmiratesID
import com.fh.payday.datasource.remote.*
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.isNotEmptyList
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class KycService private constructor(
    var token: String? = null,
    var sessionId: String? = null,
    var refreshToken: String? = null
) {

    companion object {
        private val instance: KycService by lazy { KycService() }

        fun getInstance(token: String, sessionId: String, refreshToken: String): KycService {
            return instance.also { it.token = token; it.sessionId = sessionId; it.refreshToken = refreshToken }
        }
    }

    fun getProfile(customerId: Long, callback: ApiCallback<Profile>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(KycApiInterface::class.java)
        val call = service.getProfile(customerId)
        call.enqueue(CallbackImpl(callback))
    }

    fun updateEmail(customerId: Long, email: String, otp: String, callback: ApiCallback<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(KycApiInterface::class.java)
        val call = service.updateEmail(customerId, email, otp)
        call.enqueue(CallbackStringImpl(callback))
    }

    fun updateMobile(customerId: Long, mobileNumber: String, otp: String, callback: ApiCallback<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(KycApiInterface::class.java)
        val call = service.updateMobile(customerId, mobileNumber, otp)

        call.enqueue(CallbackStringImpl(callback))
    }

    fun updatePassport(customerId: Long, passportNumber: String, otp: String, callback: ApiCallback<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(KycApiInterface::class.java)
        val call = service.updatePassport(customerId, passportNumber, otp)
        call.enqueue(CallbackStringImpl(callback))
    }

    fun getOtp(customerId: Long, callback: ApiCallback<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(KycApiInterface::class.java)
        val call = service.getOtp(customerId)
        call.enqueue(CallbackStringImpl(callback))
    }

    fun updateEmiratesId(customerId: Long,
                         front: MultipartBody.Part,
                         back: MultipartBody.Part,
                         emiratesId: String,
                         emiratesExpiry: String,
                         country: String,
                         gender: String,
                         dob: String,
                         otp: String, callback: ApiCallback<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(KycApiInterface::class.java)
        val call = service.updateEmiratesId(customerId, front, back, emiratesId.asRequestBody(), emiratesExpiry.asRequestBody(), dob.asRequestBody(), gender.asRequestBody(), otp.asRequestBody())
//        call.enqueue(CallbackStringImpl(callback))

        call.enqueue(object : CallbackStringImpl<String>(callback) {
            override fun onResponse(result: ApiResult<String>, warning: String?) {
                when {
                    "error" == result.status && !result.errors.isNullOrEmpty()-> {
                        callback.onError(result.errors[0], result.code, isSessionExpired(result.code))
                    }
                    else -> super.onResponse(result, warning)
                }
            }
        })
    }

    fun uploadPassport(customerId: Long, partPhoto: MultipartBody.Part, partSign: MultipartBody.Part, partAddress: MultipartBody.Part, callback: ApiCallback<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(KycApiInterface::class.java)
        val call = service.uploadPassport(customerId, partPhoto, partSign, partAddress)

        call.enqueue(CallbackStringImpl(callback))
    }

    fun updateEmergencyContact(customerId: Long, name: String, mobile: String, relation: String, callback: ApiCallback<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(KycApiInterface::class.java)
        val call = service.updateEmergency(customerId, name, mobile, relation)

        call.enqueue(CallbackStringImpl(callback))
    }

    fun uploadSelfie(customerId: Long, selfie: MultipartBody.Part, callback: ApiCallback<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(KycApiInterface::class.java)
        val call = service.uploadSelfie(customerId, selfie)

        call.enqueue(CallbackImpl(callback))
    }


}