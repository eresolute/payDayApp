package com.fh.payday.datasource.remote.offer

import com.fh.payday.datasource.models.OfferResponse
import com.fh.payday.utilities.apiVersion
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OfferApiInterface {

    @GET("$apiVersion/customers/{id}/banners")
    fun getOffers(
            @Path("id") customerId: Long,
            @Query("per_page") per_page: Int,
            @Query("page") page: Int
    ): Call<OfferResponse>
}