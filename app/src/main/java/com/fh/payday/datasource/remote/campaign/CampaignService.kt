package com.fh.payday.datasource.remote.campaign

import com.fh.payday.datasource.remote.ApiCallback
import com.fh.payday.datasource.remote.ApiClient
import com.fh.payday.datasource.remote.CallbackStringImpl

class CampaignService private constructor(
        var token: String? = null,
        var sessionId: String? = null,
        var refreshToken: String? = null
) {

    companion object {
        val instance: CampaignService by lazy { CampaignService() }

        fun getInstance(token: String, sessionId: String, refreshToken: String): CampaignService {
            return instance.also { it.token = token; it.sessionId = sessionId }
        }
    }

    fun updateSnoozeDays(customerId: Long, snoozeDays: Int, callback: ApiCallback<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
                .create(CampaignApiInterface::class.java)
        val call = service.updateSnoozeDays(customerId, snoozeDays)
        call.enqueue(CallbackStringImpl(callback))
    }
}