package com.fh.payday.datasource.remote.kyc

import com.fh.payday.datasource.models.Profile
import com.fh.payday.datasource.models.customer.EmiratesID
import com.fh.payday.datasource.remote.ApiResult
import com.fh.payday.utilities.apiVersion
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface KycApiInterface {

    @PUT("$apiVersion/customers/{id}/kyc")
    @FormUrlEncoded
    fun updateEmail(
            @Path("id") customerId: Long,
            @Field("emailId") emailId: String,
            @Field("otp") otp: String
    ) : Call<ApiResult<Any>>

    @PUT("$apiVersion/customers/{id}/kyc")
    @FormUrlEncoded
    fun updateMobile(
            @Path("id") customerId: Long,
            @Field("mobileNumber") mobileNumber: String,
            @Field("otp") otp: String
    ): Call<ApiResult<Any>>

    @PUT("$apiVersion/customers/{id}/otp")
    fun getOtp(
            @Path("id") customerId : Long
    ): Call<ApiResult<Any>>

    @POST("$apiVersion/customers/{id}/emirates")
    @Multipart
    fun uploadEmirates(
            @Path("id") customerId: Long,
            @Part front: MultipartBody.Part,
            @Part back: MultipartBody.Part
    ): Call<ApiResult<Any>>

    @POST("$apiVersion/customers/{id}/emirates/scan")
    @Multipart
    fun scanEmiratesId(
            @Path("id") customerId: Long,
            @Part card: MultipartBody.Part
    ): Call<ApiResult<EmiratesID>>

    @POST("$apiVersion/customers/{id}/emirates")
    @Multipart
    fun updateEmiratesId(
            @Path("id") customerId: Long,
            @Part front: MultipartBody.Part,
            @Part back: MultipartBody.Part,
            @Part("emiratesId") emiratesId: RequestBody,
            @Part("expiry") expiry: RequestBody,
            @Part("dob") dob: RequestBody,
            @Part("gender") gender: RequestBody,
            @Part("otp") otp: RequestBody
    ): Call<ApiResult<String>>

    @POST("$apiVersion/customers/{id}/passport")
    @Multipart
    fun uploadPassport(
            @Path("id") customerId: Long,
            @Part partPhoto: MultipartBody.Part,
            @Part partSign: MultipartBody.Part,
            @Part partAddress: MultipartBody.Part
    ): Call<ApiResult<Any>>

    @GET("$apiVersion/customers/{id}/profile")
    fun getProfile(
            @Path("id") customerId: Long
    ): Call<ApiResult<Profile>>

    @PUT("$apiVersion/customers/{id}/profile")
    @FormUrlEncoded
    fun updateEmergency(
            @Path("id") customerId: Long,
            @Field("emergencyContactName") name: String,
            @Field("emergencyContactNumber") mobile: String,
            @Field("relation") relation: String
    ): Call<ApiResult<Any>>

    @POST("$apiVersion/customers/{id}/selfie")
    @Multipart
    fun uploadSelfie(
            @Path("id") customerId: Long,
            @Part selfie: MultipartBody.Part
    ): Call<ApiResult<String>>

    @PUT("$apiVersion/customers/{id}/kyc")
    @FormUrlEncoded
    fun updatePassport(
            @Path("id") customerId: Long,
            @Field("passportNo") passportNumber: String,
            @Field("otp") otp: String
    ): Call<ApiResult<Any>>
}