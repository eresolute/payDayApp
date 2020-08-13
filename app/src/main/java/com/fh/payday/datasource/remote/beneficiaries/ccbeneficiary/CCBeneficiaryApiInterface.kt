package com.fh.payday.datasource.remote.beneficiaries.ccbeneficiary

import com.fh.payday.datasource.models.moneytransfer.P2CBeneficiaryResponse
import com.fh.payday.datasource.remote.ApiResult
import com.fh.payday.utilities.apiVersion
import retrofit2.Call
import retrofit2.http.*

interface CCBeneficiaryApiInterface {

    @PUT("$apiVersion/customers/{id}/otp")
    fun getOtp(
            @Path("id") customerId : Long
    ): Call<ApiResult<Any>>

    @POST("$apiVersion/customers/{id}/CCBeneficiary")
    @FormUrlEncoded
    fun addP2CBeneficiary(
            @Path("id") id: Long,
            @Field("creditCardNo") cardNumber: String,
            @Field("shortName") name: String,
            @Field("bankName") bank: String,
            @Field("otp") otp: String
    ): Call<ApiResult<P2CBeneficiaryResponse>>

    @PUT("$apiVersion/customers/{id}/CCBeneficiary/{beneficiaryId}")
    @FormUrlEncoded
    fun editBeneficiary(
            @Path("id") id: Long,
            @Path("beneficiaryId") beneficiaryId: Int,
            @Field("creditCardNo") cardNumber: String,
            @Field("shortName") name: String,
            @Field("bankName") bankName: String,
            @Field("otp") otp: String
    ): Call<ApiResult<String>>

    @FormUrlEncoded
    @HTTP(method = "DELETE", path = "$apiVersion/customers/{id}/CCBeneficiary/{beneficiaryId}", hasBody = true)
    fun deleteBeneficiary(
            @Path("id") id: Long,
            @Path("beneficiaryId") beneficiaryId: Int,
            @Field("otp") otp: String
    ): Call<ApiResult<Any>>

    @PUT("$apiVersion/customers/{id}/ccBeneficiaryStatus/{beneficiaryId}")
    @FormUrlEncoded
    fun enableBeneficiary(
            @Path("id") id: Long,
            @Path("beneficiaryId") beneficiaryId: Int,
            @Field("enabled") enabled: Int,
            @Field("otp") otp: String
    ): Call<ApiResult<Any>>
}