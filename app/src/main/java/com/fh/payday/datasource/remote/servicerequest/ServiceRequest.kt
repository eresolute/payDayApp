package com.fh.payday.datasource.remote.servicerequest

import com.fh.payday.datasource.models.CardBlockResponse
import com.fh.payday.datasource.remote.ApiCallback
import com.fh.payday.datasource.remote.ApiClient
import com.fh.payday.datasource.remote.CallbackImpl
import com.fh.payday.datasource.remote.CallbackStringImpl

class ServiceRequest private constructor(
    var token: String? = null,
    var sessionId: String? = null,
    var refreshToken: String? = null
) {

    companion object {
        private val instance: ServiceRequest by lazy { ServiceRequest() }

        fun getInstance(token: String, sessionId: String, refreshToken: String): ServiceRequest {
            return instance.also { it.token = token; it.sessionId = sessionId; it.refreshToken = refreshToken }
        }
    }

    fun blockCard(
        customerId: Long,
        encryptedPin: String,
        encryptedSecret: String,
        otp: String,
        callback: ApiCallback<CardBlockResponse>
    ) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(ServiceRequestApiInterface::class.java)
        val call = service.blockCard(customerId, encryptedPin, encryptedSecret, otp)

        call.enqueue(CallbackImpl(callback))
    }

    fun activateCard(customerId: Long, cardNumber: String, otp: String,
                     encryptedPin: String, encryptedSecret: String, callback: ApiCallback<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(ServiceRequestApiInterface::class.java)
        val call = service.activateCard(customerId, cardNumber, otp, encryptedPin, encryptedSecret)

        call.enqueue(CallbackStringImpl(callback))
    }

    fun paymentHoliday( customerId: Long, otp: String, mgNumber: String, termIncrease: Int, callback: ApiCallback<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(ServiceRequestApiInterface::class.java)
        val call = service.paymentHoliday(customerId,otp, mgNumber, termIncrease)
        call.enqueue(CallbackStringImpl(callback))
    }

    fun generateOtp( customerId: Long, callback: ApiCallback<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(ServiceRequestApiInterface::class.java)
        val call = service.generateOtp(customerId)

        call.enqueue(CallbackStringImpl(callback))
    }

    fun deferLoan(customerId: Long, callback: ApiCallback<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(ServiceRequestApiInterface::class.java)
        val call = service.deferLoan(customerId)

        call.enqueue(CallbackStringImpl(callback))
    }

    fun createDispute(
        customerId: Long,
        transAmount: String,
        transDate: String,
        merchantName: String,
        transNumber: String,
        callback: ApiCallback<String>
    ) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(ServiceRequestApiInterface::class.java)
        val call = service.createDispute(customerId, transAmount, transDate, merchantName, transNumber)
        call.enqueue(CallbackStringImpl(callback))
    }
}