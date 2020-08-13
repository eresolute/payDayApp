package com.fh.payday.datasource.remote.customerprofile

import com.fh.payday.datasource.models.CustomerSummary
import com.fh.payday.datasource.remote.ApiCallback
import com.fh.payday.datasource.remote.ApiClient
import com.fh.payday.datasource.remote.CallbackImpl

class ProfileService private constructor(
    var token: String? = null,
    var sessionId: String? = null,
    var refreshToken: String? = null
) {
    companion object {
        private val instance: ProfileService by lazy { ProfileService() }

        fun getInstance(token: String, sessionId: String, refreshToken: String): ProfileService {
            return instance.also { it.token = token; it.sessionId = sessionId; it.refreshToken = refreshToken }
        }
    }

    fun getCustomerProfile(customerId: Long, callback: ApiCallback<CustomerSummary>) {
        val service = ApiClient.getInstance(token ?: return , sessionId ?: return)
            .create(ProfileApiInterface::class.java)
        val call = service.getCustomerProfile(customerId)
        call.enqueue(CallbackImpl(callback))
    }
}