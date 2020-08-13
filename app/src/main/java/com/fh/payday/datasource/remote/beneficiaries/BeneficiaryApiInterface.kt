package com.fh.payday.datasource.remote.beneficiaries

import com.fh.payday.datasource.models.moneytransfer.AddP2CBeneficiaryResponse
import com.fh.payday.datasource.models.moneytransfer.AddP2PBeneficiaryResponse
import com.fh.payday.datasource.models.moneytransfer.PaydayBeneficiary
import com.fh.payday.datasource.remote.ApiResult
import com.fh.payday.utilities.apiVersion
import retrofit2.Call
import retrofit2.http.*
import retrofit2.http.HTTP
import retrofit2.http.FormUrlEncoded



interface BeneficiaryApiInterface {

    @GET("$apiVersion/customers/{id}/m2mDetails")
    fun getBeneficiaryDetail(
            @Path("id") id : Long,
            @Query("mobileNo") mobile : String
    ): Call<ApiResult<PaydayBeneficiary>>

    @POST("$apiVersion/customers/{id}/paydayBeneficiary")
    @FormUrlEncoded
    fun addP2CBeneficiary(
            @Path("id") id: Long,
            @Field("cardNumber") cardNumber: String,
            @Field("beneficiaryName") name: String,
            @Field("shortName") shortName: String,
            @Field("otp") otp: String
    ): Call<ApiResult<AddP2CBeneficiaryResponse>>

    @POST("$apiVersion/customers/{id}/paydayBeneficiary")
    @FormUrlEncoded
    fun addP2PBeneficiary(
            @Path("id") id: Long,
            @Field("mobileNo") cardNumber: String,
            @Field("otp") otp: String
    ): Call<ApiResult<AddP2PBeneficiaryResponse>>

    @PUT("$apiVersion/customers/{id}/otp")
    fun getOtp(
            @Path("id") customerId : Int
    ): Call<ApiResult<Any>>

    @PUT("$apiVersion/customers/{id}/paydayBeneficiary/{beneficiaryId}")
    @FormUrlEncoded
    fun editBeneficiary(
            @Path("id") customerId: Long,
            @Path("beneficiaryId") beneficiaryId: Int,
            @Field("cardNumber") cardNumber: String,
            @Field("shortName") shortName: String,
            @Field("otp") otp: String
    ): Call<ApiResult<Any>>

    @PUT("$apiVersion/customers/{id}/paydayBeneficiary/{beneficiaryId}")
    @FormUrlEncoded
    fun editP2PBeneficiary(
            @Path("id") customerId: Long,
            @Path("beneficiaryId") beneficiaryId: Int,
            @Field("beneficiaryName") beneficiaryName: String,
            @Field("otp") otp: String
    ): Call<ApiResult<String>>

    @FormUrlEncoded
    @HTTP(method = "DELETE", path = "$apiVersion/customers/{id}/paydayBeneficiary/{beneficiaryId}", hasBody = true)
    fun deleteBeneficiary(
            @Path("id") customerId: Long,
            @Path("beneficiaryId") beneficiaryId: Long,
            @Field("otp") otp: String
    ): Call<ApiResult<String>>

    @PUT("$apiVersion/customers/{id}/beneficiaryStatus/{beneficiaryId}")
    @FormUrlEncoded
    fun changeBeneficiaryStatus(
            @Path("id") customerId: Long,
            @Path("beneficiaryId") beneficiaryId: Long,
            @Field("enabled") isEnabled: Boolean,
            @Field("otp") otp: String
    ): Call<ApiResult<String>>

    @GET("$apiVersion/customers/{id}/bankList")
    fun getBankList(
            @Path("id") customerId: Long
    ): Call<ApiResult<List<String>>>
}