package com.fh.payday.datasource.remote.refreshtoken

import com.fh.payday.datasource.models.login.RefreshToken
import com.fh.payday.datasource.remote.ApiResult
import com.fh.payday.utilities.apiVersionLogin
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface RefreshTokenService {
    @GET("$apiVersionLogin/{id}/refreshToken")
    fun refreshToken(
        @Path("id") customerId: Long,
        @Header("x-access-token") refreshToken: String
    ): Call<ApiResult<RefreshToken>>
}