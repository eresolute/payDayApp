package com.fh.payday.datasource.remote.payments.utilities

import com.fh.payday.datasource.models.payments.utilities.*
import com.fh.payday.datasource.remote.ApiCallback
import com.fh.payday.datasource.remote.ApiClient
import com.fh.payday.datasource.remote.CallbackImpl
import com.fh.payday.datasource.remote.CallbackStringImpl

class UtilitiesService private constructor(
        var token: String? = null,
        var sessionId: String? = null,
        var refreshToken: String? = null
) {

    companion object {
        private val instance: UtilitiesService by lazy { UtilitiesService() }

        fun getInstance(token: String, sessionId: String, refreshToken: String): UtilitiesService {
            return instance.also { it.token = token; it.sessionId = sessionId; it.refreshToken = refreshToken }
        }
    }

    fun getOperators(customerId: Long,typeId: Int, countryCode:String, callback: ApiCallback<List<Operator>>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
                .create(UtilitiesApiInterface::class.java)
        val call = service.getOperators(customerId,typeId,countryCode)
        call.enqueue(CallbackImpl(callback))
    }

    fun getOperatorDetail(customerId: Long,accessKey: String, typeId: Int, planType: String,
                          countryCode: String, callback: ApiCallback<OperatorDetails>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
                .create(UtilitiesApiInterface::class.java)
        val call = service.getOperatorDetails(customerId,accessKey, typeId, planType,countryCode)
        call.enqueue(CallbackImpl(callback))
    }

    fun getBillBalance(customerId: Long,accessKey: String, method: String, typeId: Int, account: String,
                       flexKey: String, typeKey: Int, callback: ApiCallback<BillDetails>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
                .create(UtilitiesApiInterface::class.java)
        val call = service.getBillDetails(customerId,accessKey, method, typeId, account, flexKey, typeKey)
        call.enqueue(CallbackImpl(callback))
    }

    fun getAadcBill(customerId: Long,accessKey: String, method: String, typeId: Int, account: String, flexKey: String, typeKey: Int,
                    optional1: String, optional2: String,callback: ApiCallback<AadcBillResponse>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return).create(UtilitiesApiInterface::class.java)
        val call = service.getAadcBillDetail(customerId,accessKey, method, typeId, account,
                flexKey, typeKey,optional1, optional2)
        call.enqueue(CallbackImpl(callback))
    }

    fun payBill(accessKey: String, customerId: Int, typeKey: Int, flexKey: String, transactionId: String,
                account: String, amount: String,
                optional1: String, otp: String, callback: ApiCallback<BillPaymentResponse>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return).create(UtilitiesApiInterface::class.java)
        val call = service.payBill(accessKey, customerId, typeKey, flexKey, transactionId, account, amount, optional1, otp)
        call.enqueue(CallbackImpl(callback))
    }

    fun getOtp(customerId: Int, callback: ApiCallback<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return).create(UtilitiesApiInterface::class.java)
        val call = service.getOtp(customerId)
        call.enqueue(CallbackStringImpl(callback))
    }

}