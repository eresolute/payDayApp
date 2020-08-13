package com.fh.payday.datasource.remote.cardpin

import com.fh.payday.datasource.models.ResetPin
import com.fh.payday.datasource.remote.ApiResult
import com.fh.payday.utilities.apiVersion
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.PUT
import retrofit2.http.Path

interface CardPinApiInterface {


    @PUT("$apiVersion/customers/{id}/card/changePin")
    @FormUrlEncoded
    fun changePin(
            @Path("id") customerId: Long,
            @Field("encryptedSecret") encryptedSecret: String,
            @Field("encryptedNewPin") encryptedPin: String,
            @Field("encryptedOldPin") encryptedOldPin: String,
            @Field("otp") otp: String
    ): Call<ApiResult<ResetPin>>

    @PUT("$apiVersion/customers/{id}/otp")
    fun getOtp(
            @Path("id") customerId : Long
    ): Call<ApiResult<Any>>
}