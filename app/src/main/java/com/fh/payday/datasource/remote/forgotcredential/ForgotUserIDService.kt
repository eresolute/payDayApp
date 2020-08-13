package com.fh.payday.datasource.remote.forgotcredential

import android.text.TextUtils
import com.fh.payday.datasource.models.forgotcredential.ValidateCardResponse
import com.fh.payday.datasource.remote.ApiCallback
import com.fh.payday.datasource.remote.ApiClient
import com.fh.payday.datasource.remote.ApiResult
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.isNotEmptyList
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotUserIDService private constructor() {
    companion object {
        val instance: ForgotUserIDService by lazy { ForgotUserIDService() }
    }

    fun forgotUserID(customerId: Long, otp: String,
                     callback: ApiCallback<String>) {
        val service = ApiClient.retrofit.create(ForgotUserIDApiInterface::class.java)
        val call = service.forgotUserID(customerId, otp)
        call.enqueue(object : Callback<ApiResult<Any>> {
            override fun onFailure(call: Call<ApiResult<Any>>, t: Throwable) {
                callback.onFailure(t)
            }

            override fun onResponse(call: Call<ApiResult<Any>>, response: Response<ApiResult<Any>>) {
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

    fun forgotPassword(userName: String,customerId: Long, userPassword: String, otp: String, encryptedSecret: String,
                       callback: ApiCallback<String>) {
        val service = ApiClient.retrofit.create(ForgotUserIDApiInterface::class.java)
        val call = service.forgotPassword(userName,customerId, userPassword, otp, encryptedSecret)
        call.enqueue(object : Callback<ApiResult<Any>> {
            override fun onFailure(call: Call<ApiResult<Any>>, t: Throwable) {
                callback.onFailure(t)
            }

            override fun onResponse(call: Call<ApiResult<Any>>, response: Response<ApiResult<Any>>) {
                val result = response.body()
                        ?: return callback.onFailure(Throwable(CONNECTION_ERROR))

                if ("success" == result.status && !TextUtils.isEmpty(result.message)) {
                    callback.onSuccess(result.message!!)
                } else if ("error" == result.status && isNotEmptyList(result.errors)) {
                    callback.onError(result.errors!![0], result.code)
                } else {
                    callback.onFailure(Throwable(CONNECTION_ERROR))
                }
            }

        })

    }

    fun pinVerification(secret: String, cardNumber: String, cardPin: String,
                        callback: ApiCallback<ValidateCardResponse>) {
        val service = ApiClient.retrofit.create(ForgotUserIDApiInterface::class.java)
        val call = service.pinVerification(cardNumber, cardPin, secret)
        call.enqueue(object : Callback<ApiResult<ValidateCardResponse>> {
            override fun onFailure(call: Call<ApiResult<ValidateCardResponse>>, t: Throwable) {
                callback.onFailure(t)
            }

            override fun onResponse(call: Call<ApiResult<ValidateCardResponse>>, response: Response<ApiResult<ValidateCardResponse>>) {
                val result = response.body()
                        ?: return callback.onFailure(Throwable(CONNECTION_ERROR))

                if ("success" == result.status && result.data != null) {
                    callback.onSuccess(result.data)
                } else if ("error" == result.status && isNotEmptyList(result.errors)) {
                    callback.onError(result.errors!![0])
                } else {
                    callback.onFailure(Throwable(CONNECTION_ERROR))
                }
            }

        })
    }

    fun validateLoanAccountNo(emiratesId: String,
                              callback: ApiCallback<ValidateCardResponse>) {
        val service = ApiClient.retrofit.create(ForgotUserIDApiInterface::class.java)
        val call = service.accountNoVerification(emiratesId)
        call.enqueue(object : Callback<ApiResult<ValidateCardResponse>> {
            override fun onFailure(call: Call<ApiResult<ValidateCardResponse>>, t: Throwable) {
                callback.onFailure(t)
            }
            override fun onResponse(call: Call<ApiResult<ValidateCardResponse>>, response: Response<ApiResult<ValidateCardResponse>>) {
                val result = response.body()
                        ?: return callback.onFailure(Throwable(CONNECTION_ERROR))

                if ("success" == result.status && result.data != null) {
                    callback.onSuccess(result.data)
                } else if ("error" == result.status && isNotEmptyList(result.errors)) {
                    callback.onError(result.errors!![0])
                } else {
                    callback.onFailure(Throwable(CONNECTION_ERROR))
                }
            }

        })
    }

    fun getUserName(customerId: Long,userName: String, callback: ApiCallback<String>) {
        val service = ApiClient.retrofit.create(ForgotUserIDApiInterface::class.java)
        val call = service.userName(customerId,userName)
        call.enqueue(object : Callback<ApiResult<Any>> {
            override fun onFailure(call: Call<ApiResult<Any>>, t: Throwable) {
                callback.onFailure(t)
            }
            override fun onResponse(call: Call<ApiResult<Any>>, response: Response<ApiResult<Any>>) {
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

    fun getOtp(customerId: Long, mobileNo: String,callback: ApiCallback<String>) {
        val service = ApiClient.retrofit.create(ForgotUserIDApiInterface::class.java)
        val call = service.getCredentialOtp(customerId,mobileNo)

        call.enqueue(object : Callback<ApiResult<Any>> {
            override fun onFailure(call: Call<ApiResult<Any>>, t: Throwable) {
                callback.onFailure(t)
            }

            override fun onResponse(call: Call<ApiResult<Any>>, response: Response<ApiResult<Any>>) {
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
}