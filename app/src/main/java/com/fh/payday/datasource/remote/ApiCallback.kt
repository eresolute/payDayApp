package com.fh.payday.datasource.remote

import android.arch.lifecycle.MutableLiveData
import com.fh.payday.utilities.Event

interface ApiCallback<T> {
    fun onFailure(t: Throwable)
    fun onError(message: String)
    fun onError(message: String, code: Int = -1) { onError(message) }
    fun onError(message: String, code: Int = -1, isSessionExpired: Boolean = false) { onError(message) }
    fun onSuccess(data: T)
    fun onSuccess(data: T, message: String? = null) { onSuccess(data) }
}

abstract class ApiCallbackImpl<T> (
    private val state: MutableLiveData<Event<NetworkState2<T>>>
) : ApiCallback<T> {

    override fun onFailure(t: Throwable) {
        state.value = Event(NetworkState2.Failure(t))
    }

    override fun onError(message: String) {
        state.value = Event(NetworkState2.Error(message))
    }

    override fun onError(message: String, code: Int) {
        state.value = Event(NetworkState2.Error(message, code.toString()))
    }

    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
        state.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
    }
}