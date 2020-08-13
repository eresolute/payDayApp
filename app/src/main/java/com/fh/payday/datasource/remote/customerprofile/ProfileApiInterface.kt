package com.fh.payday.datasource.remote.customerprofile

import com.fh.payday.datasource.models.CustomerSummary
import com.fh.payday.datasource.remote.ApiResult
import com.fh.payday.utilities.apiVersion
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ProfileApiInterface {

    @GET("$apiVersion/customers/{id}")
    fun getCustomerProfile(
            @Path("id") customerId: Long
    ): Call<ApiResult<CustomerSummary>>
}