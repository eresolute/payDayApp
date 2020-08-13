package com.fh.payday.datasource.remote.auth

import com.fh.payday.datasource.models.CardLoanResult
import com.fh.payday.datasource.models.CardScanData
import com.fh.payday.datasource.models.IsoAlpha3
import com.fh.payday.datasource.models.customer.CustomerResponse
import com.fh.payday.datasource.models.customer.EmiratesID
import com.fh.payday.datasource.models.customer.RegisterCustomerRequest
import com.fh.payday.datasource.models.login.RefreshToken
import com.fh.payday.datasource.models.login.User
import com.fh.payday.datasource.remote.ApiResult
import com.fh.payday.utilities.apiVersion
import com.fh.payday.utilities.apiVersionLogin
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface AuthApiInterface {

    @GET("$apiVersionLogin/{id}/refreshToken")
    fun refreshToken(@Path("id") customerId: Long): Call<ApiResult<RefreshToken>>

    @POST("$apiVersionLogin/appSecureAuth")
    @FormUrlEncoded
    fun login(@Field("username") username: String, @Field("password") password: String,
              @Field("deviceId") deviceId: String, @Field("osVersion") osVersion: String,
              @Field("appVersion") appVersion: String): Call<ApiResult<User>>

    @POST("$apiVersion/customers/{id}/logout")
    fun logout(
        @Path("id") customerId: Long
    ): Call<ApiResult<Nothing>>

    @POST("$apiVersion/customers/emirates")
    @Multipart
    fun submitEmiratesId(
            @Part front: MultipartBody.Part?,
            @Part back: MultipartBody.Part?,
            @Part("emiratesId") emiratesId: RequestBody,
            @Part("expiry") expiry: RequestBody,
            @Part("dob") dob: RequestBody,
            @Part("nationality") nationality: RequestBody,
            @Part("gender") gender: RequestBody,
            @Part("ocrFlag") isExpiryFromOCR: Boolean
    ): Call<ApiResult<CardLoanResult>>

    @POST("$apiVersion/customers")
    @FormUrlEncoded
    fun createCustomer(
            @Field("encryptedSecret") secret: String,
            @Field("cardNumber") cardNumber: String,
            @Field("cardName") cardName: String,
            @Field("expiryDate") cardExpiry: String,
            @Field("cardPin") pin: String,
            @Field("emiratesId") emiratesId: String
    ): Call<ApiResult<CustomerResponse>>

    @PUT("$apiVersion/customers/{id}/otp")
    @FormUrlEncoded
    fun generateOTP(@Path("id") customerId: Long, @Field("mobile") mobile: String): Call<ApiResult<Any>>

    @PUT("$apiVersion/customers/{id}/mobile")
    @FormUrlEncoded
    fun verifyOtp(@Path("id") customerId: Long, @Field("pin") otp: String): Call<ApiResult<Any>>


    @GET("$apiVersion/customers/search")
    fun searchUsername(@Query("userName") userId: String): Call<ApiResult<Any>>

    @PUT("$apiVersion/customers/{id}")
    fun registerCustomer(@Path("id") customerId: Long, @Body request: RegisterCustomerRequest): Call<ApiResult<Any>>

    @PUT("$apiVersion/customers/{id}")
    fun submitCredentials(@Path("id") customerId: Long, @Body request: RegisterCustomerRequest): Single<ApiResult<Any>>

    @POST("$apiVersion/customers/{id}/emirates")
    @Multipart
    fun uploadEmirates(
            @Path("id") customerId: Long,
            @Part front: MultipartBody.Part?,
            @Part back: MultipartBody.Part?,
            @Part("emiratesId") emiratesId: RequestBody,
            @Part("expiry") expiry: RequestBody,
            @Part("dob") dob: RequestBody,
            @Part("nationality") nationality: RequestBody,
            @Part("gender") gender: RequestBody
    ): Call<ApiResult<Any>>

    @POST("$apiVersion/customers/{id}/selfie")
    @Multipart
    fun uploadSelfie(@Path("id") customerId: Long, @Part selfie: MultipartBody.Part): Call<ApiResult<Any>>

    @GET("$apiVersion/customers/search")
    fun searchUserId(@Query("userName") userId: String): Observable<ApiResult<Any>>

    @POST("$apiVersion/ocr/payday/scan")
    @Multipart
    fun scanCard(@Part card: MultipartBody.Part): Call<ApiResult<CardScanData>>

    @POST("$apiVersion/customers/{id}/emirates/scan")
    @Multipart
    fun scanEmiratesId(
            @Path("id") customerId: Long,
            @Part card: MultipartBody.Part
    ): Call<ApiResult<EmiratesID>>

    @POST("$apiVersionLogin/appSecureAuth")
    @FormUrlEncoded
    fun observableLogin(@Field("username") username: String, @Field("password") password: String): Single<ApiResult<User>>

    @POST("$apiVersion/customers/{id}/emirates")
    @Multipart
    @Deprecated("observableUploadEmirates is deprecated")
    fun observableUploadEmirates(
            @Path("id") customerId: Long,
            @Part front: MultipartBody.Part?,
            @Part back: MultipartBody.Part?,
            @Part("emiratesId") emiratesId: RequestBody,
            @Part("expiry") expiry: RequestBody,
            @Part("dob") dob: RequestBody,
            @Part("nationality") nationality: RequestBody,
            @Part("gender") gender: RequestBody
    ): Single<ApiResult<Any>>


    @GET("$apiVersion/countryCode")
    fun getCountries(): Single<ApiResult<List<IsoAlpha3>>>
}