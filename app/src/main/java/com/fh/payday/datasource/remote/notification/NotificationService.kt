package com.fh.payday.datasource.remote.notification

import NotificationApiInterface
import com.fh.payday.datasource.remote.ApiCallback
import com.fh.payday.datasource.remote.ApiClient
import com.fh.payday.datasource.remote.CallbackImpl
import com.fh.payday.datasource.models.Notification

class NotificationService  private constructor(
    var token: String? = null,
    var sessionId: String? = null,
    var refreshToken: String? = null
) {

    companion object {
        private val instance: NotificationService by lazy { NotificationService() }

        fun getInstance(token: String, sessionId: String, refreshToken: String): NotificationService {
            return instance.also { it.token = token; it.sessionId = sessionId; it.refreshToken = refreshToken }
        }
    }


    fun notifications(customerId: Long,  callback: ApiCallback<List<Notification>>){
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return).create(NotificationApiInterface::class.java)
        val call = service.notifications(customerId)
        call.enqueue(CallbackImpl(callback))
    }
}