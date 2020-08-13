package com.fh.payday.datasource.remote.intlRemittance.fundtransfer

import com.fh.payday.datasource.models.intlRemittance.FundTransferResponse
import com.fh.payday.datasource.remote.*
import com.fh.payday.utilities.apiVersion
import retrofit2.Call
import retrofit2.http.*

class FundTransferService private constructor(
        var token: String? = null,
        var sessionId: String? = null,
        var refreshToken: String? = null) {

    companion object {
        private val INSTANCE: FundTransferService by lazy { FundTransferService() }
        fun getInstance(token: String, sessionId: String, refreshToken: String): FundTransferService {
            return INSTANCE.also { it.token = token; it.sessionId = sessionId; it.refreshToken = refreshToken }
        }
    }

    fun fundTransfer(customerId: String, firstName: String, lastName: String, recieverRefNumber: String, receiverAccountNumber: String, receiverBankName: String,
                     receiverCountryCode: String, baseAmount: String, totalAmount: String, payoutCurrency: String,
                     promoCode: String, accessKey: String, otp: String, fxQuoteNo: String, deliveryMode: String, callback: ApiCallback<FundTransferResponse>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
                .create(EndPoints::class.java)
        val call = service.fundTransfer(customerId, accessKey, firstName, lastName, recieverRefNumber,
                receiverAccountNumber, receiverBankName, receiverCountryCode, baseAmount, totalAmount, promoCode, payoutCurrency, otp, fxQuoteNo, deliveryMode)
        call.enqueue(object : CallbackImpl<FundTransferResponse>(callback) {
            override fun onResponse(result: ApiResult<FundTransferResponse>, warning: String?) {
                when {
                    "error" == result.status && !result.errors.isNullOrEmpty()-> {
                        callback.onError(result.errors[0], result.code, isSessionExpired(result.code))
                    }
                    else -> super.onResponse(result, warning)
                }
            }
        })
    }
}

private interface EndPoints {
    @POST("$apiVersion/customers/{id}/remittance")
    @FormUrlEncoded
    fun fundTransfer(
        @Path("id") id: String,
        @Field("accessKey") accessKey: String,
        @Field("receiverFirstName") firstName: String,
        @Field("receiverLastName") lastName: String,
        @Field("receiverRefNumber") receiverRefNumber: String,
        @Field("receiverAccountNumber") receiverAccountNumber: String,
        @Field("receiverBankName") receiverBankName: String,
        @Field("receiverCountryCode") receiverCountryCode: String,
        @Field("baseAmount") baseAmount: String,
        @Field("amount") amount: String,
        @Field("promoCode") promoCode: String,
        @Field("payOutCurrency") payOutCurrency: String,
        @Field("otp") otp: String,
        @Field("optional1") fxQuoteNo: String,
        @Field("optional2") deliveryMode: String
    ): Call<ApiResult<FundTransferResponse>>
}