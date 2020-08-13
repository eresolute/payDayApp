package com.fh.payday.datasource.remote.beneficiaries.localbeneficiary

import com.fh.payday.datasource.models.moneytransfer.LocalBeneficiary
import com.fh.payday.datasource.remote.ApiResult
import com.fh.payday.utilities.apiVersion
import retrofit2.Call
import retrofit2.http.*

interface LocalBeneficiaryApiInterfaces {
    @GET("$apiVersion/customers/{id}/bankList")
    fun getBankList(
            @Path("id") customerId: Long
    ): Call<ApiResult<List<String>>>

    @GET("$apiVersion/customers/{id}/ibanGeneration")
    fun getIban(
            @Path("id") customerId: Long,
            @Query("bankName") bankName: String,
            @Query("bankAccNumber") accountNumber: String
    ): Call<ApiResult<String>>

    @POST("$apiVersion/customers/{id}/ibanBeneficiary")
    @FormUrlEncoded
    fun addBeneficiary(
            @Path("id") id : Long,
            @Field("mobileNo") mobileNo: String,
            @Field("beneficiaryName") name: String,
            @Field("accountNo") accountNo: String,
            @Field("IBAN") iban: String,
            @Field("bank") bank: String,
            @Field("otp") otp: String
    ): Call<ApiResult<LocalBeneficiary>>

    @PUT("$apiVersion/customers/{id}/otp")
    fun getOtp(
            @Path("id") customerId : Long
    ): Call<ApiResult<Any>>

    @PUT("$apiVersion/customers/{id}/ibanBeneficiary/{beneficiaryId}")
    @FormUrlEncoded
    fun editBeneficiary(
            @Path("id")  customerId: Long,
            @Path("beneficiaryId") beneficiaryId: Int,
            @Field("mobileNo") mobileNo: String,
            @Field("beneficiaryName") name: String,
            @Field("accountNo") accountNo: String,
            @Field("IBAN") iban: String,
            @Field("bank") bankName: String,
            @Field("otp") otp: String
    ): Call<ApiResult<String>>

    @FormUrlEncoded
    @HTTP(method = "DELETE", path = "$apiVersion/customers/{id}/ibanBeneficiary/{beneficiaryId}", hasBody = true)
    fun deleteBeneficiary(
            @Path("id") customerId: Long,
            @Path("beneficiaryId") beneficiaryId: Int,
            @Field("otp") otp: String
    ): Call<ApiResult<Any>>

    @PUT("$apiVersion/customers/{id}/ibanBeneficiaryStatus/{beneficiaryId}")
    @FormUrlEncoded
    fun enableBeneficiary(
            @Path("id") customerId: Long,
            @Path("beneficiaryId") beneficiaryId: Int,
            @Field("enabled") enableStatus: Int,
            @Field("otp") otp: String
    ): Call<ApiResult<Any>>
}