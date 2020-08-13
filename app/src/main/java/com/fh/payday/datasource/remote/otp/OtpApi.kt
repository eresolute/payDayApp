package com.fh.payday.datasource.remote.otp

import com.fh.payday.datasource.remote.ApiResult
import com.fh.payday.utilities.apiVersion
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.PUT
import retrofit2.http.Path

interface OtpApi {

    @PUT("$apiVersion/customers/{id}/otp")
    fun getOtp(
            @Path("id") customerId : Int
    ): Call<ApiResult<Any>>

    @PUT("$apiVersion/customers/{id}/resendOtp")
    @FormUrlEncoded
    fun resendOtp(
            @Path("id") customerId : Long,
            @Field("mobile") mobile: String
    ): Call<ApiResult<Nothing>>

    @PUT("$apiVersion/customers/{id}/otp/forgotCredentials")
    @FormUrlEncoded
    fun credentialsResendOtp(
            @Path("id") customerId : Long,
            @Field("mobile") mobile: String
    ): Call<ApiResult<Nothing>>
}