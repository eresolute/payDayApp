package com.fh.payday.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.fh.payday.datasource.models.Notification
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.notification.NotificationService
import com.fh.payday.utilities.Event

class NotificationViewModel : ViewModel() {
    private val _notificationState = MutableLiveData<Event<NetworkState2<List<Notification>>>>()

    val notificationState: LiveData<Event<NetworkState2<List<Notification>>>>
        get() {return _notificationState}


    fun notifications(token: String, sessionId: String, refreshToken: String, customerId: Long) {
        _notificationState.value = Event(NetworkState2.Loading())
        NotificationService.getInstance(token, sessionId, refreshToken).notifications(customerId,
            object : ApiCallbackImpl<List<Notification>>(_notificationState) {
                override fun onSuccess(data: List<Notification>) {
                    _notificationState.value = Event(NetworkState2.Success(data))
                }
            })
    }
}