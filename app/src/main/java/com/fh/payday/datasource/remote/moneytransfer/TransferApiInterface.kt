package com.fh.payday.datasource.remote.moneytransfer

import com.fh.payday.datasource.models.AmountLimit
import com.fh.payday.datasource.models.moneytransfer.*
import com.fh.payday.datasource.remote.ApiResult
import com.fh.payday.utilities.apiVersion
import retrofit2.Call
import retrofit2.http.*

interface TransferApiInterface {

    @GET("$apiVersion/customers/{id}/paydayBeneficiary")
    fun getBeneficiary(
            @Path("id") id: Long
    ): Call<ApiResult<List<Beneficiary>>>

    @PUT("$apiVersion/customers/{id}/otp")
    fun getOtp(
            @Path("id") customerId: Int
    ): Call<ApiResult<Any>>

    @POST("$apiVersion/customers/{id}/moneyTransfer/p2p")
    @FormUrlEncoded
    fun p2pTransfer(
            @Path("id") id: Long,
            @Field("mobileNo") mobileNo: String,
            @Field("transferAmount") amount: String,
            @Field("otp") otp: String
    ): Call<ApiResult<P2PTransferResponse>>

    @GET("$apiVersion/customers/{id}/amountLimit")
    fun getAmountLimit(
            @Path("id") id: Long,
            @Query("accessKey") accessKey: String
    ): Call<ApiResult<AmountLimit>>

    @GET("$apiVersion/customers/{id}/ibanBeneficiary")
    fun getLocalBeneficiaries(
            @Path("id") customerId: Long
    ): Call<ApiResult<List<LocalBeneficiary>>>

    @GET("$apiVersion/customers/{id}/CCBeneficiaries")
    fun getP2CBeneficiary(
            @Path("id") customerId: Long
    ): Call<ApiResult<List<P2CBeneficiary>>>

    @POST("$apiVersion/customers/{id}/moneyTransfer/p2iban")
    @FormUrlEncoded
    fun localTransfer(
            @Path("id") customerId: Long,
            @Field("benAccNumber") accountNo: String,
            @Field("benCustomerName") beneficiaryName: String,
            @Field("amount") amount: String,
            @Field("benBankName") bank: String,
            @Field("otp") otp: String
    ): Call<ApiResult<LocalTransferResponse>>

    @POST("$apiVersion/customers/{id}/moneyTransfer/p2cc")
    @FormUrlEncoded
    fun ccTransfer(
            @Path("id") customerId: Int,
            @Field("benAccNumber") creditCardNo: String,
            @Field("benCustomerName") shortName: String,
            @Field("amount") amount: String,
            @Field("benBankName") bankName: String,
            @Field("otp") otp: String
    ): Call<ApiResult<LocalTransferResponse>>
}