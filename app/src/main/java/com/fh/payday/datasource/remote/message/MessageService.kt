package com.fh.payday.datasource.remote.message

import com.fh.payday.datasource.models.message.MessageBody
import com.fh.payday.datasource.models.message.MessageResponse
import com.fh.payday.datasource.remote.ApiCallback
import com.fh.payday.datasource.remote.ApiClient
import com.fh.payday.datasource.remote.CallbackImpl

class MessageService private constructor(
    var token: String? = null,
    var sessionId: String? = null,
    var refreshToken: String? = null
) {

    companion object {
        private val instance: MessageService by lazy { MessageService() }

        fun getInstance(token: String, sessionId: String, refreshToken: String): MessageService {
            return instance.also { it.token = token; it.sessionId = sessionId; it.refreshToken = refreshToken }
        }
    }

    fun sendMessage(customerId: Long, subject: String, issue: String, body: String, callback: ApiCallback<MessageResponse>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return).create(MessageApiInterface::class.java)
        val call = service.sendMessage(customerId, subject, issue, body)
        call.enqueue(CallbackImpl(callback))
    }

    fun getMessages(customerId: Long,  callback: ApiCallback<List<MessageBody>>){
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return).create(MessageApiInterface::class.java)
        val call = service.getMessages(customerId)
        call.enqueue(CallbackImpl(callback))
    }

    fun getIssueAreas(customerId: Long, callback: ApiCallback<List<String>>){
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return).create(MessageApiInterface::class.java)
        val call = service.getIssueAreas(customerId)
        call.enqueue(CallbackImpl(callback))
    }

    fun getIssues(customerId: Long, issueArea: String, callback: ApiCallback<List<String>>){
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return).create(MessageApiInterface::class.java)
        val call = service.getIssues(customerId, issueArea)
        call.enqueue(CallbackImpl(callback))
    }
}