package com.fh.payday.datasource.remote.cardpin

import android.text.TextUtils
import com.fh.payday.datasource.models.ResetPin
import com.fh.payday.datasource.remote.*
import com.fh.payday.utilities.CONNECTION_ERROR
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CardPinService private constructor(
        var token: String? = null,
        var sessionId: String? = null,
        var refreshToken: String? = null
) {

    companion object {
        val instance: CardPinService by lazy { CardPinService() }

        fun getInstance(token: String, sessionId: String, refreshToken: String): CardPinService {
            return instance.also { it.token = token; it.sessionId = sessionId; it.refreshToken = refreshToken }
        }
    }

    fun changePin(customerId: Long, encryptedSecret: String, encryptedPin: String, encryptedOldPin: String, otp: String,
                  callback: ApiCallback<ResetPin>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
                .create(CardPinApiInterface::class.java)

        val call = service.changePin(customerId, encryptedSecret, encryptedPin, encryptedOldPin, otp)

        call.enqueue(object : Callback<ApiResult<ResetPin>> {
            override fun onFailure(call: Call<ApiResult<ResetPin>>, t: Throwable) {
                callback.onFailure(t)
            }

            override fun onResponse(call: Call<ApiResult<ResetPin>>, response: Response<ApiResult<ResetPin>>) {
                val result = response.body()
                        ?: return callback.onFailure(Throwable(CONNECTION_ERROR))
                when {
                    "success" == result.status && !TextUtils.isEmpty(result.message) ->
                        callback.onSuccess(result.data!!, result.message)
                    "error" == result.status && !result.errors.isNullOrEmpty() ->
                        callback.onError(result.errors[0], result.code, isSessionExpired(result.code))
                    else -> callback.onFailure(Throwable(CONNECTION_ERROR))
                }
            }
        })
        //call.enqueue(CallbackImpl(callback))
    }

    fun getOtp(customerId: Long, callback: ApiCallback<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
                .create(CardPinApiInterface::class.java)
        val call = service.getOtp(customerId)
        call.enqueue(CallbackStringImpl(callback))

    }
}