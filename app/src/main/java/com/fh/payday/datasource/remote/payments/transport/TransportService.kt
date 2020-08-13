package com.fh.payday.datasource.remote.payments.transport

import com.fh.payday.datasource.models.payments.transport.*
import com.fh.payday.datasource.remote.ApiCallback
import com.fh.payday.datasource.remote.ApiClient
import com.fh.payday.datasource.remote.CallbackImpl
import com.fh.payday.datasource.remote.CallbackStringImpl

class TransportService private constructor(
        var token: String? = null,
        var sessionId: String? = null,
        var refreshToken: String? = null
) {

    companion object {
        private val instance: TransportService by lazy { TransportService() }

        fun getInstance(token: String, sessionId: String, refreshToken: String): TransportService {
            return instance.also { it.token = token; it.sessionId = sessionId; it.refreshToken = refreshToken }
        }
    }

    fun getOperators(customerId: Long,typeId: Int,countryCode:String, callback: ApiCallback<List<Operator>>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
                .create(TransportApiInterface::class.java)
        val call = service.getOperators(customerId,typeId,countryCode)
        call.enqueue(CallbackImpl(callback))
    }

    fun getOperatorDetail(customerId: Long,accessKey: String, typeId: Int, planType: String,
                          countryCode:String, callback: ApiCallback<OperatorDetails>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
                .create(TransportApiInterface::class.java)
        val call = service.getOperatorDetail(customerId,accessKey, typeId, planType,countryCode)
        call.enqueue(CallbackImpl(callback))
    }

    fun getBillBalance(customerId: Long,accessKey: String,method: String, typeId: Int,
                       account: String, flexKey: String, typeKey: Int, optional: String,
                       callback: ApiCallback<BalanceDetails>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
                .create(TransportApiInterface::class.java)
        val call = service.getSalikBalanceDetails(customerId,accessKey,method, typeId, account, flexKey, typeKey, optional)
        call.enqueue(CallbackImpl(callback))
    }

    fun getMawaqifBill(customerId: Long,accessKey: String,method: String, typeId: Int,
                       account: String, flexKey: String, typeKey: Int,
                       callback: ApiCallback<MawaqifBillDetail>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return).create(TransportApiInterface::class.java)
        val call = service.getMawaqifBillDetail(customerId,accessKey,method, typeId, account, flexKey, typeKey)
        call.enqueue(CallbackImpl(callback))
    }

    fun paySalikBill(accessKey: String, customerId: Int, typeKey: Int, flexKey: String,
                     transactionId: String, account: String, amount: String,
                     optional1: String, optional2: String, otp: String,
                     callback: ApiCallback<BillPaymentResponse>) {

        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
                .create(TransportApiInterface::class.java)
        val call = service.payBill(accessKey, customerId, typeKey, flexKey, transactionId, account,
                amount, optional1, optional2, otp)
        call.enqueue(CallbackImpl(callback))
    }

    fun getOtp(customerId: Int, callback: ApiCallback<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
                .create(TransportApiInterface::class.java)
        val call = service.getOtp(customerId)
        call.enqueue(CallbackStringImpl(callback))
    }
}