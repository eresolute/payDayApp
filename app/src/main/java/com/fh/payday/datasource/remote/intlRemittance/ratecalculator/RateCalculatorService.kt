package com.fh.payday.datasource.remote.intlRemittance.ratecalculator

import com.fh.payday.datasource.models.Card
import com.fh.payday.datasource.models.CombinedResult
import com.fh.payday.datasource.models.CustomerSummary
import com.fh.payday.datasource.models.intlRemittance.CardInfo
import com.fh.payday.datasource.models.intlRemittance.CountryCurrency
import com.fh.payday.datasource.models.intlRemittance.RateConversion
import com.fh.payday.datasource.models.login.User
import com.fh.payday.datasource.remote.*
import com.fh.payday.datasource.remote.customer.CustomerService
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.apiVersion
import com.fh.payday.utilities.getDecimalValue
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.http.*

class RateCalculatorService private constructor(
    var token: String? = null,
    var sessionId: String? = null,
    var refreshToken: String? = null
) {

    companion object {
        private val INSTANCE: RateCalculatorService by lazy { RateCalculatorService() }
        fun getInstance(token: String, sessionId: String, refreshToken: String): RateCalculatorService {
            return INSTANCE.also { it.token = token; it.sessionId = sessionId; it.refreshToken = refreshToken }
        }
    }

    fun getConversionRate(
        customerId: String,
        payInAmount: String,
        payOutAmount: String,
        payOutCurr: String,
        receiverCountryCode: String,
        payInCurrency: String,
        accessKey: String,
        callback: ApiCallback<RateConversion>
    ): Disposable? {
        val service = ApiClient.getInstance(token ?: return null, sessionId ?: return null)
            .create(EndPoints::class.java)
        val observable = service.getConversionRate(customerId, accessKey, payInAmount, payOutAmount, payOutCurr,
            receiverCountryCode, payInCurrency)

        return observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer { r ->
                val data = r.data
                when {
                    "success".equals(r.status, true) && data != null  -> callback.onSuccess(data)
                    "error".equals(r.status, true)  && !r.errors.isNullOrEmpty() -> callback.onError(r.errors[0], r.code)
                    else -> callback.onFailure(Throwable(CONNECTION_ERROR))
                }
            }, rxErrorHandler(callback))
    }

    fun getObservableConversionRate(
        customerId: String,
        referenceNumber: String,
        payInAmount: String,
        payOutAmount: String,
        payOutCurr: String,
        receiverCountryCode: String,
        payInCurrency: String,
        accessKey: String,
        trackingId: String?,
        purposeCode: String,
        deliveryMode: String
    ): Single<ApiResult<RateConversion>>? {
        val service = ApiClient.getInstance(token ?: return null, sessionId ?: return null)
            .create(EndPoints::class.java)
        return service.validateRateConversion(customerId, referenceNumber, payInAmount, payOutAmount, payOutCurr,
            receiverCountryCode, payInCurrency, accessKey, trackingId, purposeCode, deliveryMode)
    }

    fun validateConversion(
            customerId: String,
            referenceNumber: String,
            payInAmount: String,
            payOutAmount: String,
            payOutCurr: String,
            receiverCountryCode: String,
            payInCurrency: String,
            accessKey: String,
            trackingId: String?,
            purposeCode: String,
            deliveryMode: String,
            callback: ApiCallback<RateConversion>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return).create(EndPoints::class.java)
        val call = service.validateRateConversionV2(customerId, referenceNumber, payInAmount, payOutAmount,
                payOutCurr, receiverCountryCode, payInCurrency, accessKey, trackingId, purposeCode,deliveryMode)
        call.enqueue(CallbackImpl(callback))
    }


    private fun transform(cards: List<Card>?, conversion: RateConversion?): List<CardInfo> {
        conversion ?: return emptyList()
        return cards?.map { card ->
            val payableAmount = conversion.totalPayableAmount
            val amountAfterTransfer = try {
                (card.availableBalance.replace(",", "").toDouble() -
                    conversion.totalPayableAmount.replace(",", "").toDouble())
                    .toString()
                    .getDecimalValue()
            } catch (e: NumberFormatException) {
                ""
            }
            CardInfo(card, payableAmount, amountAfterTransfer)
        } ?: emptyList()
    }

    fun getCountries(customerId: String, callback: ApiCallbackImpl<List<CountryCurrency>>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
                .create(EndPoints::class.java)
        val call = service.getCountries(customerId)
        call.enqueue(CallbackImpl(callback))
    }
}

private interface EndPoints {
    @GET("$apiVersion/customers/{id}/remittance/{accessKey}/rateCalculator")
    fun getConversionRate(
        @Path("id") id: String,
        @Path("accessKey") accessKey: String,
        @Query("payInAmount") payInAmount: String,
        @Query("payOutAmount") payoutAmount: String,
        @Query("payOutCurrency") payOutCurr: String,
        @Query("receiverCountryCode") receiverCountryCode: String,
        @Query("payInCurrency") payInCurrency: String
    ): Single<ApiResult<RateConversion>>

    @POST("$apiVersion/customers/{id}/remittance/validate")
    @FormUrlEncoded
    fun validateRateConversion(
            @Path("id") id: String,
            @Field("receiverRefNumber") referenceNumber: String,
            @Field("payInAmount") payInAmount: String,
            @Field("payOutAmount") payoutAmount: String,
            @Field("payOutCurrency") payOutCurr: String,
            @Field("receiverCountryCode") receiverCountryCode: String,
            @Field("payInCurrency") payInCurrency: String,
            @Field("accessKey") accessKey: String,
            @Field("optional1") trackingId: String?,
            @Field("optional2") purposeCode: String,
            @Field("optional3") deliveryMode: String
    ): Single<ApiResult<RateConversion>>

    @POST("$apiVersion/customers/{id}/remittance/validate")
    @FormUrlEncoded
    fun validateRateConversionV2(
            @Path("id") id: String,
            @Field("receiverRefNumber") referenceNumber: String,
            @Field("payInAmount") payInAmount: String,
            @Field("payOutAmount") payoutAmount: String,
            @Field("payOutCurrency") payOutCurr: String,
            @Field("receiverCountryCode") receiverCountryCode: String,
            @Field("payInCurrency") payInCurrency: String,
            @Field("accessKey") accessKey: String,
            @Field("optional1") trackingId: String?,
            @Field("optional2") purposeCode: String,
            @Field("optional3") deliveryMode: String
    ): Call<ApiResult<RateConversion>>



    @GET("$apiVersion/customers/{id}/remittance/countries")
    fun getCountries(
            @Path("id") id: String
    ): Call<ApiResult<List<CountryCurrency>>>
}
