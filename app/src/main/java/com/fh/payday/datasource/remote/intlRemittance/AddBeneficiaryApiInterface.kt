package com.fh.payday.datasource.remote.intlRemittance

import com.fh.payday.datasource.models.intlRemittance.*
import com.fh.payday.datasource.remote.ApiResult
import com.fh.payday.utilities.apiVersion
import retrofit2.Call
import retrofit2.http.*

interface AddBeneficiaryApiInterface {

    @GET("$apiVersion/customers/{id}/remittance/{accessKey}/payoutCountries")
    fun getPayoutCountries(
            @Path("id") id: Long,
            @Path("accessKey") accessKey: String,
            @Query("type") deliveryMode: String = ""
    ): Call<ApiResult<List<PayoutCountries>>>

    @GET("$apiVersion/customers/{id}/remittance/FARD/payoutagentfields")
    fun getRelationShips(
            @Path("id") id: Long,
            @Query("payoutAgentId") payoutAgentId: String,
            @Query("deliveryMode") deliveryMode: String,
            @Query("field") relation: String
    ): Call<ApiResult<List<RelationsItem>>>

    @POST("$apiVersion/customers/{id}/remittance/{accessKey}/beneficiaries")
    @FormUrlEncoded
    fun createBeneficiaries(
            @Path("id") id: Long,
            @Path("accessKey") accessKey: String,
            @Field("countryName") countryName: String,
            @Field("currency") currency: String,
            @Field("firstName") firstName: String,
            @Field("lastName") lastName: String,
            @Field("contactNo") contactNo: String?,
            @Field("nationality") nationality: String,
            @Field("addressLine") addressLine: String,
            @Field("relationShip") relationShip: String,
            @Field("bankName") bankName: String,
            @Field("city") city: String,
            @Field("state") state: String,
            @Field("branchName") branchName: String,
            @Field("branchAddress") branchAddress: String,
            @Field("accountNo") accountNo: String,
            @Field("ifscCode") ifscCode: String,
            @Field("swiftCode") swiftCode: String,
            @Field("iBan") iBan: String,
            @Field("deliveryMode") deliveryMode: String,
            @Field("flagPath") flagPath: String?,
            @Field("payoutAgentId") payoutAgentId: Long,
            @Field("payoutBranchId") payoutBranchId: Long,
            @Field("idNo") IdNo: String,
            @Field("otp") otp: String
    ): Call<ApiResult<CreateBeneficiaries>>

    @FormUrlEncoded
    @HTTP(method = "DELETE", path = "$apiVersion/customers/{id}/remittance/{accessKey}/beneficiary", hasBody = true)
    fun deleteBeneficiary(
            @Path("id") customerId: Long,
            @Path("accessKey") accessKey: String,
            @Field("receiverRefNumber") receiverRefNumber: String
    ): Call<ApiResult<DeleteBeneficiary>>

    @GET("$apiVersion/customers/{id}/remittance/{accessKey}/payoutAgents")
    fun payoutAgents(
            @Path("id") customerId: Long,
            @Path("accessKey") accessKey: String,
            @Query("countryCode") countryCode: String,
            @Query("deliveryMode") deliveryMode: String
    ): Call<ApiResult<List<PayoutAgent>>>

    @GET("$apiVersion/customers/{id}/remittance/{accessKey}/payoutAgentBranches")
    fun payoutAgentBranches(
            @Path("id") customerId: Long,
            @Path("accessKey") accessKey: String,
            @Query("payoutAgentId") payoutAgentId: Long,
            @Query("deliveryMode") deliveryMode: String,
            @Query("optional1") optional1: String = "",
            @Query("optional2") optional2: String = ""
    ): Call<ApiResult<List<PayoutAgentBranches>>>

    @POST("$apiVersion/customers/{id}/remittance/{accessKey}/beneficiary/favourite")
    @FormUrlEncoded
    fun addToFavBeneficiary(
            @Path("id") customerId: Long,
            @Path("accessKey") accessKey: String,
            @Field("recieverRefNumber") recieverRefNumber: String,
            @Field("flag") flag: Int
    ): Call<ApiResult<Any>>
}