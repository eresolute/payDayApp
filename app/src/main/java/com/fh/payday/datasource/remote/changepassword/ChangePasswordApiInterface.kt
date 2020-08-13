package com.fh.payday.datasource.remote.changepassword

import com.fh.payday.datasource.remote.ApiResult
import com.fh.payday.utilities.apiVersion
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.PUT
import retrofit2.http.Path

interface ChangePasswordApiInterface {

    @PUT("$apiVersion/customers/{id}/changePassword")
    @FormUrlEncoded
    fun updatePassword(
            @Path("id") customerId: Long,
            @Field("oldPassword") oldPassword: String,
            @Field("newPassword") newPassword: String,
            @Field("pin") otp: String)
            : Call<ApiResult<Any>>

    @PUT("$apiVersion/customers/{id}/otp")
    fun getOtp(
            @Path("id") customerId: Long
    ): Call<ApiResult<Any>>

}