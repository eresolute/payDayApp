package com.fh.payday.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.fh.payday.datasource.models.login.User
import com.fh.payday.datasource.remote.ApiCallback
import com.fh.payday.datasource.remote.ApiClient
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.auth.AuthService
import com.fh.payday.utilities.Event
import com.fh.payday.utilities.appVersion
import com.fh.payday.utilities.deviceName
import com.fh.payday.utilities.os

class LoginViewModel : ViewModel() {

    private val _loginState = MutableLiveData<Event<NetworkState2<User>>>()

    val loginState: LiveData<Event<NetworkState2<User>>>
        get() = _loginState

    var loggedUser: User? = null

    @JvmOverloads
    fun login(username: String, password: String, deviceId: String, showDialog: Boolean = true) {
        if (showDialog) _loginState.value = Event(NetworkState2.Loading())
        ApiClient.clearInstance()

        AuthService.instance.login(username, password, deviceId, "$deviceName-$os",appVersion,
                object : ApiCallback<User> {
                    override fun onFailure(t: Throwable) {
                        _loginState.value = Event(NetworkState2.Failure(t))
                    }

                    override fun onError(message: String) {
                        _loginState.value = Event(NetworkState2.Error(message))
                    }

                    override fun onError(message: String, code: Int) {
                        _loginState.value = Event(NetworkState2.Error(message, code.toString()))
                    }

                    override fun onSuccess(data: User) {
                        _loginState.value = Event(NetworkState2.Success(data))
                    }

                    override fun onSuccess(data: User, message: String?) {
                        _loginState.value = Event(NetworkState2.Success(data, message))
                    }
                })
    }

}
