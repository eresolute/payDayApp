package com.fh.payday.datasource.remote.beneficiaries.ccbeneficiary

import com.fh.payday.datasource.models.moneytransfer.AddP2CBeneficiaryResponse
import com.fh.payday.datasource.models.moneytransfer.P2CBeneficiaryResponse
import com.fh.payday.datasource.remote.*
import com.fh.payday.datasource.remote.beneficiaries.BeneficiaryApiInterface
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.isNotEmptyList
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CCBeneficiaryService private constructor(
        var token: String? = null,
        var sessionId: String? = null,
        var refreshToken: String? = null
) {

    companion object {
        private val instance: CCBeneficiaryService by lazy { CCBeneficiaryService() }

        fun getInstance(token: String, sessionId: String, refreshToken: String): CCBeneficiaryService {
            return instance.also { it.token = token; it.sessionId = sessionId; it.refreshToken = refreshToken }
        }
    }

    fun getOtp(customerId: Long, callback: ApiCallback<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return).create(CCBeneficiaryApiInterface::class.java)
        val call = service.getOtp(customerId)
        call.enqueue(CallbackStringImpl(callback))
    }

    fun addP2CBeneficiaryService(id: Long, cardNumber: String, name: String, bank: String, otp: String, callback: ApiCallback<P2CBeneficiaryResponse>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return).create(CCBeneficiaryApiInterface::class.java)
        val call = service.addP2CBeneficiary(id, cardNumber, name, bank, otp)
//        call.enqueue(CallbackImpl(callback))

        call.enqueue(object : CallbackImpl<P2CBeneficiaryResponse>(callback) {
            override fun onResponse(result: ApiResult<P2CBeneficiaryResponse>, warning: String?) {
                when {
                    "error" == result.status && !result.errors.isNullOrEmpty()-> {
                        callback.onError(result.errors[0], result.code, isSessionExpired(result.code))
                    }
                    else -> super.onResponse(result, warning)
                }
            }
        })
    }

    fun editBeneficiaryService(id: Long, beneficiaryId: Int, cardNumber: String, name: String, bankName: String, otp: String, callback: ApiCallback<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return).create(CCBeneficiaryApiInterface::class.java)
        val call = service.editBeneficiary(id, beneficiaryId, cardNumber, name, bankName, otp)
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

    fun deleteBeneficiary(id: Long, beneficiaryId: Int, otp: String, callback: ApiCallbackImpl<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return).create(CCBeneficiaryApiInterface::class.java)
        val call = service.deleteBeneficiary(id, beneficiaryId, otp)
        call.enqueue(CallbackStringImpl(callback))
    }

    fun enableBeneficiary(id: Long, beneficiaryId: Int, enabled: Int, otp: String, callback: ApiCallbackImpl<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return).create(CCBeneficiaryApiInterface::class.java)
        val call = service.enableBeneficiary(id, beneficiaryId, enabled, otp)
        call.enqueue(CallbackStringImpl(callback))
    }
}