package com.fh.payday.datasource.remote.customer

import com.fh.payday.datasource.models.CustomerSummary
import com.fh.payday.datasource.remote.ApiResult
import com.fh.payday.utilities.apiVersion
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.*

interface CustomerApiInterface {

    @GET("$apiVersion/customers/{id}")
    fun getSummary(@Path("id") id: Long): Call<ApiResult<CustomerSummary>>

    @GET("$apiVersion/customers/{id}")
    fun getSummaryRx(@Path("id") id: Long): Single<ApiResult<CustomerSummary>>

    @POST("$apiVersion/customers/{id}/maintainSession")
    fun resetSession(
        @Path("id") customerId: Long
    ): Single<ApiResult<Nothing>>

    @PUT("$apiVersion/customers/{id}/notification")
    @FormUrlEncoded
    fun readNotifications(
        @Path("id") customerId: Long,
        @Field("notificationId") notificationId : List<String>
    ) : Call<ApiResult<Any>>
}