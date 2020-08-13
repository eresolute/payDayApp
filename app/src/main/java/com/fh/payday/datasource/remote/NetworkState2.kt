package com.fh.payday.datasource.remote

sealed class NetworkState2<out T> {
    class Loading<out T> : NetworkState2<T>()
    data class Success<out T>(val data: T?, val message: String? = null) : NetworkState2<T>()
    data class Failure<out T>(val throwable: Throwable) : NetworkState2<T>()
    data class Error<out T>(
        val message: String,
        val errorCode: String = "",
        val isSessionExpired: Boolean = false
    ) : NetworkState2<T>()
}