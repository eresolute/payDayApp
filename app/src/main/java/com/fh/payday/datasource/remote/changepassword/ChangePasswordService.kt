package com.fh.payday.datasource.remote.changepassword

import com.fh.payday.datasource.remote.ApiCallback
import com.fh.payday.datasource.remote.ApiClient
import com.fh.payday.datasource.remote.CallbackStringImpl

class ChangePasswordService private constructor(
    var token: String? = null,
    var sessionId: String? = null,
    var refreshToken: String? =  null
) {

    companion object {
        val instance: ChangePasswordService by lazy { ChangePasswordService() }

        fun getInstance(token: String, sessionId: String, refreshToken: String): ChangePasswordService {
            return instance.also { it.token = token; it.sessionId = sessionId; it.refreshToken = refreshToken }
        }
    }

    fun updatePassword(customerId: Long, oldPassword: String, newPassword: String, pin: String,
                       callback: ApiCallback<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(ChangePasswordApiInterface::class.java)
        val call = service.updatePassword(customerId, oldPassword, newPassword, pin)
        call.enqueue(CallbackStringImpl(callback))
    }

    fun getOtp(customerId: Long, callback: ApiCallback<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(ChangePasswordApiInterface::class.java)
        val call = service.getOtp(customerId)
        call.enqueue(CallbackStringImpl(callback))
    }
}