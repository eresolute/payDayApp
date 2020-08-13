package com.fh.payday.datasource.remote.locator

import com.fh.payday.datasource.models.BranchLocator
import com.fh.payday.datasource.models.IntlBranchLocator
import com.fh.payday.datasource.remote.ApiResult
import com.fh.payday.utilities.apiVersion
import com.fh.payday.views2.intlRemittance.ExchangeAccessKey
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LocatorApiInterface {

    @GET("$apiVersion/customers/{id}/branches")
    fun locators(
            @Path("id") customerId: Long,
            @Query("locatorType") locatorType: String
    ): Call<ApiResult<List<BranchLocator>>>

    @GET("$apiVersion/customers/{id}/remittance/{accessKey}/branches")
    fun locateExchange(
            @Path("id") customerId: Long,
            @Path("accessKey") accessKey: String = ExchangeAccessKey.FARD
    ): Call<ApiResult<List<IntlBranchLocator>>>

    @GET("$apiVersion/customers/{id}/remittance/{accessKey}/branches")
    fun locateExchangeRx(
        @Path("id") customerId: Long,
        @Path("accessKey") accessKey: String
    ): Single<ApiResult<List<IntlBranchLocator>>>
}

interface BranchATMApiInterface {
    @GET("$apiVersion/customers/branches")
    fun locators(
            @Query("locatorType") locatorType: String
    ): Call<ApiResult<List<BranchLocator>>>
}