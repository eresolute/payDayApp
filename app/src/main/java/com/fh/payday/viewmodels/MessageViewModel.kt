package com.fh.payday.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.fh.payday.datasource.models.message.MessageBody
import com.fh.payday.datasource.models.message.MessageResponse
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.message.MessageService
import com.fh.payday.utilities.Event

class MessageViewModel: ViewModel() {

    private val _messageState = MutableLiveData<Event<NetworkState2<MessageResponse>>>()
    private val _getMessagesState = MutableLiveData<Event<NetworkState2<List<MessageBody>>>>()
    private val _issueState = MutableLiveData<Event<NetworkState2<List<String>>>>()
    private val _issueSelectedState = MutableLiveData<Event<NetworkState2<List<String>>>>()

    val messageState:  LiveData<Event<NetworkState2<MessageResponse>>>
        get() {return _messageState}

    val getMessageState:  LiveData<Event<NetworkState2<List<MessageBody>>>>
        get() {return _getMessagesState}

    val issueState:  LiveData<Event<NetworkState2<List<String>>>>
        get() {return _issueState}

    val issueSelectedState:  LiveData<Event<NetworkState2<List<String>>>>
        get() {return _issueSelectedState}

    fun sendMessage(token: String, sessionId: String, refreshToken: String, customerId: Long, subject: String, issue: String, body: String) {

        _messageState.value = Event(NetworkState2.Loading())
        MessageService.getInstance(token, sessionId, refreshToken).sendMessage(customerId, subject, issue, body,
            object : ApiCallbackImpl<MessageResponse>(_messageState) {
                override fun onSuccess(data: MessageResponse) {
                    _messageState.value = Event(NetworkState2.Success(data))
                }

                override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                    _messageState.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                }
            })
    }

    fun getMessages(token: String, sessionId: String, refreshToken: String, customerId: Long) {

        _getMessagesState.value = Event(NetworkState2.Loading())
        MessageService.getInstance(token, sessionId, refreshToken).getMessages(customerId,
            object : ApiCallbackImpl<List<MessageBody>>(_getMessagesState) {
                override fun onSuccess(data: List<MessageBody>) {
                    _getMessagesState.value = Event(NetworkState2.Success(data))
                }
            })
    }

    fun getIssueAreas(token: String, sessionId: String, refreshToken: String, customerId: Long) {

        _issueState.value = Event(NetworkState2.Loading())
        MessageService.getInstance(token, sessionId, refreshToken).getIssueAreas(customerId,
            object : ApiCallbackImpl<List<String>>(_issueState) {
                override fun onSuccess(data: List<String>) {
                    _issueState.value = Event(NetworkState2.Success(data))
                }

            })
    }

    fun getIssues(token: String, sessionId: String, refreshToken: String, customerId: Long, issueArea: String) {

        _issueSelectedState.value = Event(NetworkState2.Loading())
        MessageService.getInstance(token, sessionId, refreshToken).getIssues(customerId, issueArea,
            object : ApiCallbackImpl<List<String>>(_issueSelectedState) {
                override fun onSuccess(data: List<String>) {
                    _issueSelectedState.value = Event(NetworkState2.Success(data))
                }

            })
    }
}