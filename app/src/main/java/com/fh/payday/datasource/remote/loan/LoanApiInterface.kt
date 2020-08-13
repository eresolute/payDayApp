package com.fh.payday.datasource.remote.loan

import com.fh.payday.datasource.models.loan.LoanAcceptance
import com.fh.payday.datasource.models.loan.LoanOffer
import com.fh.payday.datasource.models.loan.LoanRequest
import com.fh.payday.datasource.remote.ApiResult
import com.fh.payday.utilities.apiVersion
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.*

interface LoanApiInterface {
    @GET("$apiVersion/customers/{id}/loan/offer")
    fun requestLoan(
            @Path("id") customerId: Long,
            @Query("productType") productType: String = "NEW LOAN"
    ): Call<ApiResult<LoanOffer>>

    @POST("$apiVersion/customers/{id}/loan/request")
    @FormUrlEncoded
    fun customizeLoan(
            @Path("id") customerId: Long,
            @Field("requestedAmount") loanAmount: String,
            @Field("requestedTenure") loanTenure: String,
            @Field("productType") productType: String = "NEW LOAN"
    ) : Single<ApiResult<LoanOffer>>

    @POST("$apiVersion/customers/{id}/loan/apply")
    @FormUrlEncoded
    fun acceptLoanOffer(
            @Path("id") customerId: Long,
            @Field("otp") otp: String,
            @Field("loanAmount") loanAmount: String,
            @Field("loanTenure") loanTenure: String,
            @Field("interestRate") interestRate: String,
            @Field("applicationId") applicationId: String,
            @Field("lastSalaryCredit") lastSalaryCredit: String,
            @Field("pushNotification") fromPushNotification: Boolean,
            @Field("productType") productType: String = "NEW LOAN"
    ): Call<ApiResult<LoanAcceptance>>

    @POST("$apiVersion/customers/{id}/loan/apply")
    @FormUrlEncoded
    fun acceptLoanTopUpOffer(
            @Path("id") customerId: Long,
            @Field("otp") otp: String,
            @Field("loanAmount") loanAmount: String,
            @Field("loanTenure") loanTenure: String,
            @Field("interestRate") interestRate: String,
            @Field("applicationId") applicationId: String,
            @Field("lastSalaryCredit") lastSalaryCredit: String,
            @Field("pushNotification") fromPushNotification: Boolean,
            @Field("productType") productType: String,
            @Field("mgNumber") loanNumber: String
    ): Call<ApiResult<LoanAcceptance>>

    @GET("$apiVersion/customers/{id}/loan/rescheduleLoan")
    fun rescheduleLoan(
            @Path("id") customerId: Long
    ): Call<ApiResult<List<LoanRequest>>>

    @GET("$apiVersion/customers/{id}/loan/liability")
    fun liability(
            @Path("id") customerId: Long
    ): Call<ApiResult<List<LoanRequest>>>

    @GET("$apiVersion/customers/{id}/loan/clearanceLetter")
    fun clearanceLetter(
            @Path("id") customerId: Long
    ): Call<ApiResult<List<LoanRequest>>>

    @GET("$apiVersion/customers/{id}/loan/partialSettlement")
    fun partialSettlement(
            @Path("id") customerId: Long
    ): Call<ApiResult<List<LoanRequest>>>

    @GET("$apiVersion/customers/{id}/loan/earlySettlement")
    fun earlySettlement(
            @Path("id") customerId: Long
    ): Call<ApiResult<List<LoanRequest>>>

    @POST("$apiVersion/customers/{id}/loan/rescheduleLoan")
    @FormUrlEncoded
    fun requestRescheduleLoan(
            @Path("id") customerId: Long,
            @Field("loanNumber") loanNumber: String
    ): Call<ApiResult<LoanRequest>>

    @POST("$apiVersion/customers/{id}/loan/liability")
    @FormUrlEncoded
    fun requestLiability(
            @Path("id") customerId: Long,
            @Field("loanNumber") loanNumber: String
    ): Call<ApiResult<LoanRequest>>

    @POST("$apiVersion/customers/{id}/loan/earlySettlement")
    @FormUrlEncoded
    fun requestEarlySettlement(
            @Path("id") customerId: Long,
            @Field("loanNumber") loanNumber: String
    ): Call<ApiResult<LoanRequest>>

    @POST("$apiVersion/customers/{id}/loan/clearanceLetter")
    @FormUrlEncoded
    fun requestClearanceLetter(
            @Path("id") customerId: Long,
            @Field("loanNumber") loanNumber: String
    ): Call<ApiResult<LoanRequest>>

    @POST("$apiVersion/customers/{id}/loan/partialSettlement")
    @FormUrlEncoded
    fun requestPartialSettlement(
            @Path("id") customerId: Long,
            @Field("loanNumber") loanNumber: String
    ): Call<ApiResult<LoanRequest>>
}