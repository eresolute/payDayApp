package com.fh.payday.datasource.remote.country

import com.fh.payday.datasource.models.Country2
import com.fh.payday.datasource.remote.ApiCallback
import com.fh.payday.datasource.remote.ApiClient
import com.fh.payday.datasource.remote.CallbackImpl

class CountryService private constructor(
    var token: String? = null,
    var sessionId: String? = null,
    var refreshToken: String? = null
) {
    companion object {
        private val instance: CountryService by lazy { CountryService() }

        fun getInstance(token: String, sessionId: String, refreshToken: String): CountryService {
            return instance.also { it.token = token; it.sessionId = sessionId; it.refreshToken = refreshToken}
        }
    }

    fun getCountry(callback: ApiCallback<List<Country2>>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(CountryApiInterface::class.java)
        val call = service.getCountry()
        call.enqueue(CallbackImpl(callback))
    }
}