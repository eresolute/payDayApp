package com.fh.payday.datasource.remote.offer

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.PageKeyedDataSource
import android.util.Log
import com.fh.payday.datasource.models.OfferDetail
import com.fh.payday.datasource.models.OfferResponse
import com.fh.payday.datasource.remote.ApiClient
import com.fh.payday.datasource.remote.ErrorCodes
import com.fh.payday.datasource.remote.isSessionExpired
import com.fh.payday.datasource.remote.parseErrorBody
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.views2.offer.OfferState
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OfferDataSource(
    private val token: String,
    private val sessionId: String,
    private val refreshToken: String,
    private val userId: Long,
    private val offerState: MutableLiveData<OfferState>,
    private val onError: (Int, String) -> Unit
) : PageKeyedDataSource<Int, OfferDetail>() {
    companion object {
        const val PAGE_SIZE = 1
        const val PER_PAGE = 3
    }

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, OfferDetail>) {
        val service = ApiClient.getInstance(token, sessionId).create(OfferApiInterface::class.java)
        val call = service.getOffers(userId, PER_PAGE, PAGE_SIZE)
        call.enqueue(object : Callback<OfferResponse> {
            override fun onFailure(call: Call<OfferResponse>, t: Throwable) {
                Log.d("Error", t.message)
                offerState.value = OfferState.error(t.message)
            }

            override fun onResponse(call: Call<OfferResponse>, response: Response<OfferResponse>) {
                if (!response.isSuccessful && response.errorBody() != null) {
                    val errorResult = parseErrorBody(response.errorBody()?.string())
                    val message = when {
                        errorResult?.errors != null && !errorResult.errors.isNullOrEmpty() -> errorResult.errors!![0]
                        else -> response.message() ?: CONNECTION_ERROR
                    }
                    offerState.value = OfferState.error(message)
                    return onError(errorResult?.code ?: 0, message)
                }

                val body = response.body() ?: return callback.onResult(ArrayList(), null, PAGE_SIZE + 1)
                if (isSessionExpired(body.code) && "error" == body.status && !body.errors.isNullOrEmpty()) {
                    offerState.value = OfferState.error(body.errors[0])
                    return onError(body.code, body.errors[0])
                }

                if (body.offerDetail.isNullOrEmpty()) {
                    offerState.value = OfferState.EMPTY
                } else {
                    offerState.value = OfferState.LOADED
                }

                when {
                    response.body()?.code == ErrorCodes.SESSION_EXPIRED -> return
                    response.body()?.status == "error" -> return
                    else -> callback.onResult(body.offerDetail, null, PAGE_SIZE + 1)
                }
            }

        })
    }

    //this will load the previous page
    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, OfferDetail>) {
        //not required yet
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, OfferDetail>) {
        val service = ApiClient.getInstance(token, sessionId).create(OfferApiInterface::class.java)
        val call = service.getOffers(userId, PER_PAGE, params.key)
        call.enqueue(object : Callback<OfferResponse> {
            override fun onResponse(call: Call<OfferResponse>, response: Response<OfferResponse>) {
                if (!response.isSuccessful && response.errorBody() != null) {
                    val errorResult = parseErrorBody(response.errorBody()?.string())
                    val message = when {
                        errorResult?.errors != null && !errorResult.errors.isNullOrEmpty() -> errorResult.errors!![0]
                        else -> response.message() ?: CONNECTION_ERROR
                    }
                    return onError(errorResult?.code ?: 0, message)
                }

                val body = response.body() ?: return callback.onResult(ArrayList(), params.key + 1)
                if (isSessionExpired(body.code) && "error" == body.status && !body.errors.isNullOrEmpty()) {
                    return onError(body.code, body.errors[0])
                }

                callback.onResult(body.offerDetail, params.key + 1)
            }

            override fun onFailure(call: Call<OfferResponse>, t: Throwable) {
                Log.d("Error", t.message)
            }
        })


    }

}