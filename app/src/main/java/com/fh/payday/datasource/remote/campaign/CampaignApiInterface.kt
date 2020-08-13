package com.fh.payday.datasource.remote.campaign

import com.fh.payday.datasource.remote.ApiResult
import com.fh.payday.utilities.apiVersion
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.PUT
import retrofit2.http.Path

interface CampaignApiInterface {
    @PUT("$apiVersion/customers/{id}/cms")
    @FormUrlEncoded
    fun updateSnoozeDays(
        @Path("id") customerId: Long,
        @Field("snoozeDays") snoozeDays: Int
    ): Call<ApiResult<Any>>
}