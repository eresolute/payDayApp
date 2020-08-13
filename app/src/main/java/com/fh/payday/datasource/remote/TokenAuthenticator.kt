package com.fh.payday.datasource.remote

import com.fh.payday.datasource.remote.ErrorCodes.Companion.TOKEN_EXPIRED
import com.fh.payday.datasource.remote.refreshtoken.ServiceHolder
import com.google.gson.Gson
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator (
    private val serviceHolder: ServiceHolder?
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val responseBody = try {
            Gson().fromJson(response.body().asString(), ApiResult::class.java)
        } catch (e: Exception) {
            null
        }

        val errorCode: Int = responseBody?.code ?: return null
        if (errorCode != TOKEN_EXPIRED) return null

        val retrofitResponse = serviceHolder?.service
            ?.refreshToken(serviceHolder.customerId, serviceHolder.refreshToken)
            ?.execute()

        retrofitResponse?.body()?.let { body ->
            val newRefreshToken = body.data?.refreshToken ?: return@let
            val newToken = body.data.token
            return response.request()
                .newBuilder()
                .header("x-access-token", newToken)
                .header("refresh-token", newRefreshToken)
                .header("token-status", "updated")
                .build()
        }

        return null
    }

}