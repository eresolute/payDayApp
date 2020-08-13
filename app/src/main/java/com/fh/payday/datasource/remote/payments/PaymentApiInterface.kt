package com.fh.payday.datasource.remote.payments

import com.fh.payday.datasource.models.IndianState
import com.fh.payday.datasource.models.payments.*
import com.fh.payday.datasource.remote.ApiResult
import com.fh.payday.utilities.apiVersion
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.*

interface PaymentApiInterface {

    @GET("$apiVersion/customers/{customerId}/operators")
    fun getOperators(
            @Path("customerId") customerId: Long,
            @Query("method") method: String,
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
    ): Call<ApiResult<OperatorDetail>>

    @GET("$apiVersion/customers/{customerId}/operators/{access_key}")
    fun observableOperatorDetails(
            @Path("customerId") customerId: Long,
            @Path("access_key") accessKey: String,
            @Query("typeId") typeId: Int,
            @Query("planType") planType: String,
            @Query("countryCode") countryCode: String
    ): Single<ApiResult<OperatorDetail>>

    @GET("$apiVersion/customers/{customerId}/operators/{access_key}")
    fun getOperatorDetailFixed(
            @Path("customerId") customerId: Long,
            @Path("access_key") accessKey: String,
            @Query("typeId") typeId: Int,
            @Query("planType") planType: String,
            @Query("optional1") optional1: String,
            @Query("countryCode") countryCode: String
    ): Call<ApiResult<List<OperatorDetailFixed>>>

    @GET("$apiVersion/customers/{customerId}/operators/{access_key}/payable")
    fun getPayableAmount(
            @Path("customerId") customerId: Long,
            @Path("access_key") accessKey: String,
            @Query("typeKey") typeKey: Int,
            @Query("flexiKey") flexiKey: String,
            @Query("amount") amount: Double
    ): Call<ApiResult<PayableAmount>>

    @POST("$apiVersion/customers/{customerId}/operators/balance")
    @FormUrlEncoded
    fun getRechargeDetail(
            @Path("customerId") customerId: Long,
            @Field("accessKey") accessKey: String,
            @Field("method") method: String,
            @Field("typeId") typeId: Int,
            @Field("account") account: String,
            @Field("flexiKey") flexiKey: String,
            @Field("typeKey") typeKey: Int,
            @Field("optional1") option: String

    ): Call<ApiResult<RechargeDetail>>

    @POST("$apiVersion/customers/{customer_id}/operators/balance")
    @FormUrlEncoded
    fun getBalance(
            @Path("customer_id") customerId: Long,
            @Field("accessKey") accessKey: String,
            @Field("typeId") typeId: Int,
            @Field("account") account: String,
            @Field("flexiKey") flexKey: String?,
            @Field("typeKey") typeKey: Int?,
            @Field("optional1") optional1: String,
            @Field("method") method: String = "getBalance"
    ): Single<ApiResult<Bill>>

    @POST("$apiVersion/customers/{customerId}/operators/balance")
    @FormUrlEncoded
    fun getBillAmountEtisalat(
            @Path("customerId") customerId: Long,
            @Field("accessKey") accessKey: String,
            @Field("method") method: String,
            @Field("typeId") typeId: Int,
            @Field("account") account: String,
            @Field("flexiKey") flexKey: String,
            @Field("typeKey") typeKey: Int,
            @Field("optional1") option: String
    ): Call<ApiResult<BillDetailEtisalat>>

    @POST("$apiVersion/customers/{customerId}/operators/balance")
    @FormUrlEncoded
    fun getBillAmountDu(
            @Path("customerId") customerId: Long,
            @Field("accessKey") accessKey: String,
            @Field("method") method: String,
            @Field("typeId") typeId: Int,
            @Field("account") account: String,
            @Field("flexiKey") flexiKey: String,
            @Field("typeKey") typeKey: Int

    ): Call<ApiResult<BillDetailDU>>


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
            @Field("accessKey") keyAccess: String,
            @Field("optional1") serviceType: String,
            @Field("optional2") providerId: String,
            @Field("optional3") enteredAmount: String,
            @Field("otp") otp: String
    ): Call<ApiResult<BillPaymentResponse>>

    @POST("$apiVersion/customers/{customerId}/operators/{accessKey}/payment")
    @FormUrlEncoded
    fun payBillDu(
            @Path("accessKey") accessKey: String,
            @Path("customerId") customerId: Int,
            @Field("typeKey") typeKey: Int,
            @Field("flexiKey") flexiKey: String,
            @Field("transId") transactionId: String,
            @Field("account") account: String,
            @Field("amount") amount: String,
            @Field("optional1") serviceType: String,
            @Field("optional2") providerId: String?,
            @Field("otp") otp: String
    ): Call<ApiResult<BillPaymentDuResponse>>

    @PUT("$apiVersion/customers/{id}/otp")
    fun getOtp(
            @Path("id") customerId: Int
    ): Call<ApiResult<Any>>

    @POST("$apiVersion/customers/{customerId}/operators/{accessKey}/payment")
    @FormUrlEncoded
    fun makePaymentFlexi(
            @Path("accessKey") accessKey: String,
            @Path("customerId") customerId: Long,
            @Field("typeKey") typeKey: Int,
            @Field("flexiKey") flexiKey: String,
            @Field("transId") transactionId: String,
            @Field("account") account: String,
            @Field("amount") amount: String,
            @Field("otp") otp: String,
            @Field("optional1") optional1: String = "",
            @Field("optional2") optional2: String = "",
            @Field("optional3") optional3: String = "",
            @Field("optional4") optional4: String = ""
    ): Call<ApiResult<PaymentResult>>


    @POST("$apiVersion/customers/{customerId}/operators/{accessKey}/payment")
    @FormUrlEncoded
    fun makePaymentFixed(
            @Path("accessKey") accessKey: String,
            @Path("customerId") customerId: Long,
            @Field("typeKey") typeKey: Int,
            @Field("planId") planId: String,
            @Field("transId") transactionId: String,
            @Field("account") account: String,
            @Field("amount") amount: String,
            @Field("otp") otp: String,
            @Field("optional1") optional1: String = ""
    ): Call<ApiResult<PaymentResult>>

    @GET("$apiVersion/customers/{customerId}/operators/{accessKey}/recents")
    fun getRecentAccounts(
            @Path("customerId") customerId: Long,
            @Path("accessKey") accessKey: String
    ): Observable<ApiResult<ArrayList<RecentAccount>>>

    @GET("$apiVersion/customers/{customerId}/operators/beneficiary")
    fun getBeneficiaries(
            @Path("customerId") customerId: Long,
            @Query("accessKey") accessKey: String
    ): Call<ApiResult<ArrayList<Beneficiaries>>>

    @POST("$apiVersion/customers/{customerId}/operators/beneficiary")
    @FormUrlEncoded
    fun add2Beneficiary(
            @Path("customerId") customerId: Long,
            @Field("mobileNumber") mobileNumber: String,
            @Field("shortName") shortName: String,
            @Field("accessKey") accessKey: String,
            @Field("optional1") optional1: String? = null,
            @Field("optional2") optional2: String? = null,
            @Field("type") type: String? = null
    ): Call<ApiResult<String>>

    @PUT("$apiVersion/customers/{customerId}/operators/beneficiary/{beneficiaryId}")
    @FormUrlEncoded
    fun editBeneficiary(
            @Path("customerId") customerId: Long,
            @Path("beneficiaryId") beneficiaryId: Long,
            @Field("mobileNumber") mobileNumber: String,
            @Field("shortName") shortName: String,
            @Field("accessKey") accessKey: String
    ): Call<ApiResult<String>>

    @DELETE("$apiVersion/customers/{customerId}/operators/beneficiary/{beneficiaryId}")
    fun deleteBeneficiary(
            @Path("customerId") customerId: Long,
            @Path("beneficiaryId") beneficiaryId: Long
    ): Call<ApiResult<String>>

    @PUT("$apiVersion/customers/{customerId}/operators/beneficiary/{beneficiaryId}")
    @FormUrlEncoded
    fun enableBeneficiary(
            @Path("customerId") customerId: Long,
            @Path("beneficiaryId") beneficiaryId: Long,
            @Field("enabled") enable: Boolean
    ): Call<ApiResult<String>>

    @GET("$apiVersion/customers/{customerId}/operators/indianStates")
    fun getIndianStates(
            @Path("customerId") customerId: Long,
            @Query("method") method: String
    ): Call<ApiResult<List<IndianState>>>
}