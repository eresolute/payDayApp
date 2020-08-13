package com.fh.payday.datasource.remote.otp

import android.text.TextUtils
import com.fh.payday.datasource.remote.ApiCallback
import com.fh.payday.datasource.remote.ApiClient
import com.fh.payday.datasource.remote.ApiResult
import com.fh.payday.datasource.remote.CallbackStringImpl
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.isNotEmptyList
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OtpService(
        var token: String? = null,
        var sessionId: String? = null,
        var refreshToken: String? = null
) {
    companion object {
        private val instance: OtpService by lazy { OtpService() }

        fun getInstance(token: String, sessionId: String, refreshToken: String): OtpService {
            return instance.also { it.token = token; it.sessionId = sessionId; it.refreshToken = refreshToken }
        }
    }

    fun getOtp(customerId: Int, callback: ApiCallback<String>) {
        val service = getService()
        val call = service.getOtp(customerId)
        call.enqueue(CallbackStringImpl(callback))
    }

    fun resendOtp(customerId: Long, mobileNo: String, callback: ApiCallback<String>) {
        val service = ApiClient.retrofit.create(OtpApi::class.java)
        val call = service.resendOtp(customerId, mobileNo)

        call.enqueue(object : Callback<ApiResult<Nothing>> {
            override fun onFailure(call: Call<ApiResult<Nothing>>, t: Throwable) {
                callback.onFailure(t)
            }
            override fun onResponse(call: Call<ApiResult<Nothing>>, response: Response<ApiResult<Nothing>>) {
                val result = response.body()
                        ?: return callback.onFailure(Throwable(CONNECTION_ERROR))
                if ("success" == result.status && !TextUtils.isEmpty(result.message)) {
                    callback.onSuccess(result.message!!)
                } else if ("error" == result.status && isNotEmptyList(result.errors)) {
                    callback.onError(result.errors!![0])
                } else {
                    callback.onFailure(Throwable(CONNECTION_ERROR))
                }
            }
        })
    }
    fun credentialResendOtp(customerId: Long, mobileNo: String, callback: ApiCallback<String>) {
        val service = ApiClient.retrofit.create(OtpApi::class.java)
        val call = service.credentialsResendOtp(customerId, mobileNo)

        call.enqueue(object : Callback<ApiResult<Nothing>> {
            override fun onFailure(call: Call<ApiResult<Nothing>>, t: Throwable) {
                callback.onFailure(t)
            }
            override fun onResponse(call: Call<ApiResult<Nothing>>, response: Response<ApiResult<Nothing>>) {
                val result = response.body()
                        ?: return callback.onFailure(Throwable(CONNECTION_ERROR))
                if ("success" == result.status && !TextUtils.isEmpty(result.message)) {
                    callback.onSuccess(result.message!!)
                } else if ("error" == result.status && isNotEmptyList(result.errors)) {
                    callback.onError(result.errors!![0])
                } else {
                    callback.onFailure(Throwable(CONNECTION_ERROR))
                }
            }
        })
    }

    private fun getService(): OtpApi {
        val token = token
        val sessionId = sessionId
        return if (token != null && sessionId != null) {
            ApiClient.getInstance(token, sessionId).create(OtpApi::class.java)
        } else {
            ApiClient.retrofit.create(OtpApi::class.java)
        }
    }
}