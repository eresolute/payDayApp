package com.fh.payday.datasource.remote.country

import com.fh.payday.datasource.models.Country2
import com.fh.payday.datasource.remote.ApiResult
import com.fh.payday.utilities.apiVersion
import retrofit2.Call
import retrofit2.http.GET

interface CountryApiInterface {
    @GET("$apiVersion/countries")
    fun getCountry(): Call<ApiResult<List<Country2>>>
}