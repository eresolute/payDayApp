package com.fh.payday.datasource.remote.intlRemittance

import com.fh.payday.datasource.models.intlRemittance.*
import com.fh.payday.datasource.remote.*
import com.fh.payday.datasource.remote.intlRemittance.ratecalculator.RateCalculatorService
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.apiVersion
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.http.*

class TransactionService private constructor(
        var token: String? = null,
        var sessionId: String? = null,
        var refreshToken: String? = null) {

    companion object {
        private val INSTANCE: TransactionService by lazy { TransactionService() }
        fun getInstance(token: String, sessionId: String, refreshToken: String): TransactionService {
            return INSTANCE.also { it.token = token; it.sessionId = sessionId; it.refreshToken = refreshToken }
        }
    }

    fun getTransactions(customerId: String, fromDate: String, toDate: String, callback: ApiCallback<List<IntlTransaction>>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
                .create(EndPoints::class.java)
        val call = service.getTransactions(customerId, fromDate, toDate)
        call.enqueue(object : CallbackImpl<List<IntlTransaction>>(callback) {
            override fun onResponse(result: ApiResult<List<IntlTransaction>>, warning: String?) {
                when {
                    "error" == result.status && !result.errors.isNullOrEmpty() -> {
                        callback.onError(result.errors[0], result.code, isSessionExpired(result.code))
                    }
                    else -> super.onResponse(result, warning)
                }
            }
        })
    }

    fun getTransactionDetail(customerId: String,accessKey: String, transactionId: String, dealerTxnId: String, callback: ApiCallback<TransactionDetail>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
                .create(EndPoints::class.java)
        val call = service.getTransactionDetail(customerId, accessKey, transactionId, dealerTxnId)
        call.enqueue(object : CallbackImpl<TransactionDetail>(callback) {
            override fun onResponse(result: ApiResult<TransactionDetail>, warning: String?) {
                when {
                    "error" == result.status && !result.errors.isNullOrEmpty() -> {
                        callback.onError(result.errors[0], result.code, isSessionExpired(result.code))
                    }
                    else -> super.onResponse(result, warning)
                }
            }
        })
    }

    fun trackSharedTransaction(customerId: String, transactionId: String, callback: ApiCallbackImpl<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId
                ?: return).create(EndPoints::class.java)
        val call = service.trackSharedTransaction(customerId, transactionId)
        call.enqueue(object : CallbackStringImpl<String>(callback) {
            override fun onResponse(result: ApiResult<String>, warning: String?) {
                when {
                    "error" == result.status && !result.errors.isNullOrEmpty() -> {
                        callback.onError(result.errors[0], result.code, isSessionExpired(result.code))
                    }
                    else -> super.onResponse(result, warning)
                }
            }
        })

    }

    fun getTransDetailsAndCon(
            customerId: String,
            transactionId: String,
            partnerTxnRefNo: String,
            beneficiaryReferenceNumber: String,
            payInAmount: String,
            payOutAmount: String,
            payOutCurr: String,
            receiverCountryCode: String,
            payInCurrency: String,
            accessKey: String,
            trackingId: String?,
            purposeCode: String,
            deliveryMode: String,
            callback: ApiCallback<CombinedTransDetailsAndRate>
    ): Disposable? {
        val token = token ?: return null
        val sessionId = sessionId ?: return null
        val refreshToken = refreshToken ?: return null

        val service = ApiClient.getInstance(token, sessionId)
                .create(EndPoints::class.java)
        val tranDetailsObservable = service.getTransactionDetailRx(customerId, accessKey, transactionId,partnerTxnRefNo)
        val rateConObservable = RateCalculatorService.getInstance(token, sessionId, refreshToken)
                .getObservableConversionRate(customerId, beneficiaryReferenceNumber,
                    payInAmount.replace(",", ""), payOutAmount.replace(",", ""),
                    payOutCurr, receiverCountryCode, payInCurrency, accessKey, trackingId, purposeCode, deliveryMode) ?: return null

        return Single.zip(
                tranDetailsObservable.subscribeOn(Schedulers.io()),
                rateConObservable.subscribeOn(Schedulers.io()),
                BiFunction { r1: ApiResult<TransactionDetail>, r2: ApiResult<RateConversion> ->
                    CombinedTransDetailsAndRate(r1, r2)
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer { response ->
                    val (r1, r2) = response
                    when {
                        "success".equals(r1.status, true) && "success".equals(r2.status, true) -> callback.onSuccess(response)
                        "error".equals(r1.status, true) && !r1.errors.isNullOrEmpty() -> callback.onError(r1.errors[0], r1.code, isSessionExpired(r1.code))
                        "error".equals(r2.status, true) && !r2.errors.isNullOrEmpty() -> callback.onError(r2.errors[0], r2.code, isSessionExpired(r2.code))
                        else -> callback.onFailure(Throwable(CONNECTION_ERROR))
                    }
                }, rxErrorHandler(callback))

    }

    fun addFavourite(customerId: String, transId: String, isFavorite: Int, callback: ApiCallbackImpl<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
                .create(EndPoints::class.java)
        val call = service.addFavourite(customerId, transId, isFavorite)
        call.enqueue(object : CallbackStringImpl<String>(callback) {
            override fun onResponse(result: ApiResult<String>, warning: String?) {
                when {
                    "error" == result.status && !result.errors.isNullOrEmpty() -> {
                        callback.onError(result.errors[0], result.code, isSessionExpired(result.code))
                    }
                    else -> super.onResponse(result, warning)
                }
            }
        })
    }

    fun getTransactionDetailsPDF(customerId: String, transId: String, callback: ApiCallbackImpl<TransDetailsPDFFile>) {

        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
                .create(EndPoints::class.java)
        val call = service.getTransactionDetailsPDF(customerId, transId)
        call.enqueue(CallbackImpl(callback))
    }
}

private interface EndPoints {
    @GET("$apiVersion/customers/{id}/remittance/orders")
    fun getTransactions(
            @Path("id") customerId: String,
            @Query("fromDate") fromDate: String,
            @Query("toDate") toDate: String
    ): Call<ApiResult<List<IntlTransaction>>>

    @GET("$apiVersion/customers/{id}/remittance/{accessKey}/transaction")
    fun getTransactionDetail(
            @Path("id") customerId: String,
            @Path("accessKey") accessKey: String,
            @Query("transactionId") transactionId: String,
            @Query("dealerTxnId") dealerTxnId: String
    ): Call<ApiResult<TransactionDetail>>

    @GET("$apiVersion/customers/{id}/remittance/{accessKey}/transaction")
    fun getTransactionDetailRx(
            @Path("id") customerId: String,
            @Path("accessKey") accessKey: String,
            @Query("transactionId") transactionId: String,
            @Query("dealerTxnId") dealerTxnId: String
    ): Single<ApiResult<TransactionDetail>>

    @POST("$apiVersion/customers/{id}/remittance/transaction/favourities")
    @FormUrlEncoded
    fun addFavourite(
            @Path("id") customerId: String,
            @Field("transId") transactionId: String,
            @Field("flag") isFavorite: Int
    ): Call<ApiResult<String>>

    @GET("$apiVersion/customers/{id}/getTransactionDetails/{transactionID}")
    fun getTransactionDetailsPDF(
            @Path("id") customerId: String,
            @Path("transactionID") transactionId: String
    ): Call<ApiResult<TransDetailsPDFFile>>

    @POST("$apiVersion/customers/{id}/remittance/transaction/share")
    @FormUrlEncoded
    fun trackSharedTransaction(
            @Path("id") customerId: String,
            @Field("transactionID") transactionId: String
    ): Call<ApiResult<String>>

}