package com.fh.payday.datasource.remote.auth

import com.fh.payday.datasource.models.login.User
import com.fh.payday.datasource.remote.ApiCallback
import com.fh.payday.datasource.remote.ApiClient
import com.fh.payday.datasource.remote.ApiResult
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.isNotEmptyList
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class AuthService private constructor() {

    companion object {
        val instance: AuthService by lazy { AuthService() }
    }

    fun login(username: String, password: String, deviceId: String, osVersion: String
              , appVersion: String, callback: ApiCallback<User>) {
        val service = ApiClient.retrofit.create(AuthApiInterface::class.java)
        val call = service.login(username, password, deviceId, osVersion, appVersion)

        call.enqueue(object: Callback<ApiResult<User>> {
            override fun onFailure(call: Call<ApiResult<User>>, t: Throwable) {
                callback.onFailure(t)
            }

            override fun onResponse(call: Call<ApiResult<User>>, response: Response<ApiResult<User>>) {
                val result = response.body() ?: return callback.onFailure(Throwable(CONNECTION_ERROR))

                if (response.code() == 403 && response.errorBody() != null) run {
                    return try {
                        val jsonObject = JSONObject(response.errorBody()?.string())
                        callback.onError(jsonObject.getString("message"))
                    } catch (e: JSONException) {
                        callback.onFailure(Throwable(CONNECTION_ERROR))
                    } catch (e: IOException) {
                        callback.onFailure(Throwable(CONNECTION_ERROR))
                    }
                }

                if ("success" == result.status && result.data != null) {
                    val warning = response.headers().get("Warning")
                    callback.onSuccess(result.data, warning)
                } else if ("error" == result.status && isNotEmptyList(result.errors)) {
                    callback.onError(result.errors!![0], result.code)
                }  else {
                    callback.onFailure(Throwable(CONNECTION_ERROR))
                }
            }
        })
    }

    fun logout(
        token: String,
        sessionId: String,
        refreshToken: String,
        customerId: Long
    ) {
        val service = ApiClient.getInstance(token, sessionId).create(AuthApiInterface::class.java)
        val call = service.logout(customerId)
        call.enqueue(object : Callback<ApiResult<Nothing>> {
            override fun onFailure(call: Call<ApiResult<Nothing>>, t: Throwable) {}

            override fun onResponse(call: Call<ApiResult<Nothing>>, response: Response<ApiResult<Nothing>>) {}
        })
    }

}