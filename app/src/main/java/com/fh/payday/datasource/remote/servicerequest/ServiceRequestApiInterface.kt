package com.fh.payday.datasource.remote.servicerequest

import com.fh.payday.datasource.models.CardBlockResponse
import com.fh.payday.datasource.remote.ApiResult
import com.fh.payday.utilities.apiVersion
import retrofit2.Call
import retrofit2.http.*

interface ServiceRequestApiInterface {

    @PUT("$apiVersion/customers/{id}/card/block")
    @FormUrlEncoded
    fun blockCard(
            @Path("id") customerId: Long,
            @Field("encryptedPin") pin: String,
            @Field("encryptedSecret") secret: String,
            @Field("otp") otp: String
    ): Call<ApiResult<CardBlockResponse>>

    @PUT("$apiVersion/customers/{id}/card/activate")
    @FormUrlEncoded
    fun activateCard(
            @Path("id") customerId: Long,
            @Field("CardNumber") cardNumber: String,
            @Field("otp") otp: String,
            @Field("encryptedPin") pin: String,
            @Field("encryptedSecret") secret: String
    ): Call<ApiResult<Nothing>>


    @PUT("$apiVersion/customers/{id}/otp")
    fun generateOtp(
            @Path("id") customerId: Long
    ): Call<ApiResult<Any>>

    @POST("$apiVersion/customers/{id}/holiday")
    @FormUrlEncoded
    fun paymentHoliday(
            @Path("id") customerId: Long,
            @Field("otp") otp: String,
            @Field("MGNumber") mgNumber: String,
            @Field("TermIncrease") termIncrease: Int
    ): Call<ApiResult<Any>>

    @GET("$apiVersion/customers/{id}/loan/deferment")
    fun deferLoan(
            @Path("id") customerId: Long
    ): Call<ApiResult<Any>>

    @POST("$apiVersion/customers/{id}/card/transactionDispute")
    @FormUrlEncoded
    fun createDispute(
            @Path("id") customerId: Long,
            @Field("TransactionAmount") transAmount: String,
            @Field("TransactionDateTime") transDate: String,
            @Field("MerchantName") merchantName: String,
            @Field("TransactionReferenceNumber") transNumber: String
    ): Call<ApiResult<Nothing>>
}