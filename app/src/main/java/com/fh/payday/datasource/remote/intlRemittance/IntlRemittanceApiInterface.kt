package com.fh.payday.datasource.remote.intlRemittance

import com.fh.payday.datasource.models.intlRemittance.*
import com.fh.payday.datasource.remote.ApiResult
import com.fh.payday.utilities.apiVersion
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.*

interface IntlRemittanceApiInterface {

    @GET("$apiVersion/customers/{id}/remittance/{accessKey}/beneficiaries")
    fun getBeneficiaries(
            @Path("id") id: String,
            @Path("accessKey") accessKey : String,
            @Query("deliveryMode") deliveryMode: String
    ): Call<ApiResult<List<IntlBeneficiary>>>

    @GET("$apiVersion/customers/{id}/remittance/{accessKey}/beneficiaries")
    fun getBeneficiariesRx(
        @Path("id") id: String,
        @Path("accessKey") accessKey : String,
        @Query("deliveryMode") deliveryMode : String
    ): Single<ApiResult<List<IntlBeneficiary>>>

    @GET("$apiVersion/customers/{id}/remittance/customerExists")
    fun getCustomerState(
            @Path("id") id: String,
            @Query("confirmationStatus") confirmationStatus: String?
    ): Call<ApiResult<List<Exchange>>>


    @POST("$apiVersion/customers/{id}/remittance/searchCustomer")
    @FormUrlEncoded
    fun searchCustomer(
            @Path("id") id: String,
            @Field("emiratesId") emiratesCardNumber: String
    ): Call<ApiResult<CustomerDetail>>

    @POST("$apiVersion/customers/{id}/remittance/{accessKey}/activateCustomer")
    @FormUrlEncoded
    fun generateOtp(
        @Path("id") id: Long,
        @Path("accessKey") accessKey: String,
        @Field("sendOtp") sendOtp: String = "y"
    ): Call<ApiResult<Otp>>

    @POST("$apiVersion/customers/{id}/remittance/{accessKey}/activateCustomer")
    @FormUrlEncoded
    fun activateCustomer(
        @Path("id") id: Long,
        @Path("accessKey") accessKey: String,
        @Field("otp") otp: String
    ): Call<ApiResult<Any>>

    @POST("$apiVersion/customers/{id}/remittance/{accessKey}/initializeTransaction")
    @FormUrlEncoded
    fun purposeOfPayment(
        @Path("id") id: Long,
        @Path("accessKey") accessKey : String,
        @Field("receiverRefNumber")  beneficiaryRefNumber: String
    ): Call<ApiResult<InitializeTransaction>>

}