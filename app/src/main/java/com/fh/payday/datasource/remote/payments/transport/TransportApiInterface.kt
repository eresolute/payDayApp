package com.fh.payday.datasource.remote.payments.transport

import com.fh.payday.datasource.models.payments.transport.*
import com.fh.payday.datasource.remote.ApiResult
import com.fh.payday.utilities.apiVersion
import retrofit2.Call
import retrofit2.http.*

interface TransportApiInterface {
    @GET("$apiVersion/customers/{customerId}/operators")
    fun getOperators(
            @Path("customerId") customerId: Long,
            @Query("typeId") typeId: Int,
            @Query("countryCode") countryCode: String
    ): Call<ApiResult<List<Operator>>>

    @GET("$apiVersion/customers/{customerId}/operators/{access_key}")
    fun getOperatorDetail(
            @Path("customerId") customerId: Long,
            @Path("access_key") accessKey: String,
            @Query("typeId") typeId: Int,
            @Query("planType") planType: String,
            @Query("countryCode") countryCode: String
    ): Call<ApiResult<OperatorDetails>>

    @POST("$apiVersion/customers/{customerId}/operators/balance")
    @FormUrlEncoded
    fun getSalikBalanceDetails(
            @Path("customerId") customerId: Long,
            @Field("accessKey") accessKey: String,
            @Field("method") method: String,
            @Field("typeId") typeId: Int,
            @Field("account") account: String,
            @Field("flexiKey") flexiKey: String,
            @Field("typeKey") typeKey: Int,
            @Field("optional1") option: String

    ): Call<ApiResult<BalanceDetails>>

    @POST("$apiVersion/customers/{customerId}/operators/balance")
    @FormUrlEncoded
    fun getMawaqifBillDetail(
            @Path("customerId") customerId: Long,
            @Field("accessKey") accessKey: String,
            @Field("method") method: String,
            @Field("typeId") typeId: Int,
            @Field("account") account: String,
            @Field("flexiKey") flexiKey: String,
            @Field("typeKey") typeKey: Int
    ): Call<ApiResult<MawaqifBillDetail>>

    @POST("$apiVersion/customers/{customerId}/operators/{accessKey}/payment")
    @FormUrlEncoded
    fun payBill(
            @Path("accessKey") accessKey: String,
            @Path("customerId") customerId: Int,
            @Field("typeKey") typeKey: Int,
            @Field("flexiKey") flexiKey: String,
            @Field("transId") transactionId: String,
            @Field("account") account: String,
            @Field("amount") amount: String,
            @Field("optional1") optional1: String,
            @Field("optional2") optional2: String,
            @Field("otp") otp: String
    ): Call<ApiResult<BillPaymentResponse>>

    @PUT("$apiVersion/customers/{id}/otp")
    fun getOtp(
            @Path("id") customerId: Int
    ): Call<ApiResult<Any>>
}