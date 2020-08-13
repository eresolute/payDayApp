package com.fh.payday.datasource.remote.forgotcredential

import com.fh.payday.datasource.models.forgotcredential.ValidateCardResponse
import com.fh.payday.datasource.remote.ApiResult
import com.fh.payday.utilities.apiVersion
import retrofit2.Call
import retrofit2.http.*

interface ForgotUserIDApiInterface {

    @POST("$apiVersion/users/{id}/forgetPassword")
    @FormUrlEncoded
    fun forgotPassword(
            @Field("userName") userName: String,
            @Path("id") customerId: Long,
            @Field("userPassword") userPassword: String,
            @Field("otp") otp: String,
            @Field("encryptedSecret") secret: String)
            : Call<ApiResult<Any>>

    @POST("$apiVersion/users/{id}/forgetUserId")
    @FormUrlEncoded
    fun forgotUserID(
            @Path("id") customerId: Long,
            @Field("otp") otp: String
    ): Call<ApiResult<Any>>

    @POST("$apiVersion/users/validateCard")
    @FormUrlEncoded
    fun pinVerification(
            @Field("cardNumber") cardNumber: String,
            @Field("cardPin") pin: String,
            @Field("encryptedSecret") secret: String
    ): Call<ApiResult<ValidateCardResponse>>

    @POST("$apiVersion/users/validateCustomer")
    @FormUrlEncoded
    fun accountNoVerification(
            @Field("emiratesId") emiratesId: String
    ): Call<ApiResult<ValidateCardResponse>>

    @GET("$apiVersion/customers/{id}/userExist")
    fun userName(
            @Path("id") customerId: Long,
            @Query("userName") userName: String
    ): Call<ApiResult<Any>>

    @PUT("$apiVersion/customers/{id}/otp")
    @FormUrlEncoded
    fun getOtp(
            @Path("id") customerId: Long,
            @Field("mobile") mobileNumber: String
    ): Call<ApiResult<Any>>

    @PUT("$apiVersion/customers/{id}/otp/forgotCredentials")
    @FormUrlEncoded
    fun getCredentialOtp(
            @Path("id") customerId: Long,
            @Field("mobile") mobileNumber: String
    ): Call<ApiResult<Any>>
}