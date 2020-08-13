package com.fh.payday.datasource.remote

import com.fh.payday.utilities.CONNECTION_ERROR
import io.reactivex.functions.Consumer
import retrofit2.HttpException

fun <T> rxErrorHandler(callback: ApiCallback<T>): Consumer<Throwable> {
    return Consumer { e ->
        when(e) {
            is HttpException -> {
                val errorResult = parseErrorBody(e.response()?.errorBody()?.string())
                val message = when {
                    errorResult?.errors != null && !errorResult.errors.isNullOrEmpty() -> errorResult.errors[0]
                    else -> e.message() ?: CONNECTION_ERROR
                }
                callback.onError(message, errorResult?.code ?: e.code(),
                    isSessionExpired(errorResult?.code))
            }
            else-> callback.onFailure(Throwable(e))
        }
    }
}