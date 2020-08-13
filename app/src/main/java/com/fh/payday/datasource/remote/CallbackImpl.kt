package com.fh.payday.datasource.remote

import com.fh.payday.utilities.CONNECTION_ERROR
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

abstract class Callback<T, R>(
    private val callback: ApiCallback<R>
) : Callback<ApiResult<T>> {
    override fun onFailure(call: Call<ApiResult<T>>, t: Throwable) {
        callback.onFailure(t)
    }

    override fun onResponse(call: Call<ApiResult<T>>, response: Response<ApiResult<T>>) {
        if (!response.isSuccessful && response.errorBody() != null) {
            val errorResult = parseErrorBody(response.errorBody()?.string())
            val message = when {
                errorResult?.errors != null && !errorResult.errors.isNullOrEmpty() -> errorResult.errors!![0]
                else -> response.message() ?: CONNECTION_ERROR
            }
            return callback.onError(message, errorResult?.code ?: response.code(),
                isSessionExpired(errorResult?.code))
        }

        val result = response.body() ?: return callback.onFailure(Throwable(CONNECTION_ERROR))
        val warning = response.headers().get("Warning")
        onResponse(result, warning)
    }

    open fun onResponse(result: ApiResult<T>, warning: String?) {
        when {
            "success" == result.status.toLowerCase() && result.data != null -> onSuccess(result.data, warning)
            "error" == result.status && !result.errors.isNullOrEmpty() -> {
                callback.onError(result.errors[0], result.code, isSessionExpired(result.code))
            }
            else -> callback.onFailure(Throwable(CONNECTION_ERROR))
        }
    }

    abstract fun onSuccess(data: T, warning: String?)
}

open class CallbackImpl<T> (
    private val callback: ApiCallback<T>
) : com.fh.payday.datasource.remote.Callback<T, T>(callback) {

    override fun onSuccess(data: T, warning: String?) {
        when {
            warning.isNullOrEmpty() -> callback.onSuccess(data)
            else -> callback.onSuccess(data, warning)
        }
    }
}

open class CallbackStringImpl<T> (
    private val callback: ApiCallback<String>
) : com.fh.payday.datasource.remote.Callback<T, String>(callback) {

    override fun onResponse(result: ApiResult<T>, warning: String?) {
        when {
            "success" == result.status.toLowerCase() && !result.message.isNullOrEmpty() -> onSuccess(result.message, warning)
            "error" == result.status && !result.errors.isNullOrEmpty() -> {
                callback.onError(result.errors[0], result.code, isSessionExpired(result.code))
            }
            else -> callback.onFailure(Throwable(CONNECTION_ERROR))
        }
    }

    override fun onSuccess(data: T, warning: String?) {}

    fun onSuccess(data: String, warning: String?) {
        when {
            warning.isNullOrEmpty() -> callback.onSuccess(data)
            else -> callback.onSuccess(data, warning)
        }
    }

    /*override fun onResponse(result: ApiResult<String>, warning: String?) {
        when {
            "success" == result.status && !result.message.isNullOrEmpty() -> onSuccess(result.message, warning)
            "error" == result.status && !result.errors.isNullOrEmpty() -> {
                callback.onError(result.errors[0], result.code, isSessionExpired(result.code))
            }
            else -> callback.onFailure(Throwable(CONNECTION_ERROR))
        }
    }

    override fun onSuccess(data: String, warning: String?) {
        when {
            warning.isNullOrEmpty() -> callback.onSuccess(data)
            else -> callback.onSuccess(data, warning)
        }
    }*/
}

fun  parseErrorBody(errorBody: String?): ApiResult<*>? {
    errorBody ?: return null
    return try {
        Gson().fromJson(errorBody, object : TypeToken<ApiResult<*>>() {}.type)
    } catch (e: Exception) {
        null
    }
}

fun isSessionExpired(code: Int?): Boolean {
    return ErrorCodes.SESSION_EXPIRED == code || ErrorCodes.CUSTOMER_DISABLED == code
}

fun isTokenExpired(code: Int?): Boolean {
    return ErrorCodes.TOKEN_EXPIRED == code
}