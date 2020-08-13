package com.fh.payday.datasource.remote.offer

import com.fh.payday.datasource.models.OfferResponse
import com.fh.payday.datasource.remote.ApiCallback
import com.fh.payday.datasource.remote.ApiClient
import com.fh.payday.datasource.remote.ApiResult
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.isNotEmptyList
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OfferService private constructor() {

    companion object {
        val instance: OfferService by lazy { OfferService() }
    }

//    fun getOffers(customerId: Long, callback : ApiCallback<List<OfferResponse>>) {
//        val service = ApiClient.retrofit.create(OfferApiInterface::class.java)
//        val call = service.getOffers(customerId)
//
//        call.enqueue(object : Callback<ApiResult<List<OfferResponse>>> {
//            override fun onFailure(call: Call<ApiResult<List<OfferResponse>>>, t: Throwable) {
//                callback.onFailure(t)
//            }
//
//            override fun onResponse(call: Call<ApiResult<List<OfferResponse>>>, response: Response<ApiResult<List<OfferResponse>>>) {
//                val result = response.body() ?: return callback.onFailure(Throwable(CONNECTION_ERROR))
//
//                if ("success" == result.status && result.data != null) {
//                    callback.onSuccess(result.data)
//                } else if ("error" == result.status && isNotEmptyList(result.errors)) {
//                    callback.onError(result.errors!![0])
//                } else {
//                    callback.onFailure(Throwable(CONNECTION_ERROR))
//                }
//            }
//
//        })
//    }
}