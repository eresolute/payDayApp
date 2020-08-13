package com.fh.payday.datasource.remote.transactionhistory

import com.fh.payday.datasource.models.CardTransactionDispute
import com.fh.payday.datasource.models.CreditDebit
import com.fh.payday.datasource.models.TransactionHistory
import com.fh.payday.datasource.models.transactionhistory.LoanTransaction
import com.fh.payday.datasource.remote.ApiCallback
import com.fh.payday.datasource.remote.ApiClient
import com.fh.payday.datasource.remote.ApiResult
import com.fh.payday.datasource.remote.CallbackImpl

class TransactionHistoryService private constructor(
    var token: String? = null,
    var sessionId: String? = null,
    var refreshToken: String? = null
) {

    companion object {
        private val instance: TransactionHistoryService by lazy { TransactionHistoryService() }

        fun getInstance(token: String, sessionId: String, refreshToken: String): TransactionHistoryService {
            return instance.also { it.token = token; it.sessionId = sessionId; it.refreshToken = refreshToken }
        }
    }

    fun getLoanTransaction(customerId: Long, callback: ApiCallback<List<LoanTransaction>>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(TransactionHistoryInterface::class.java)
        val call = service.getLoanTransaction(customerId)

        call.enqueue(object : CallbackImpl<List<LoanTransaction>>(callback) {
            override fun onResponse(result: ApiResult<List<LoanTransaction>>, warning: String?) {
                when {
                    "error" == result.status && 903 == result.code -> onSuccess(ArrayList(), warning)
                    else -> super.onResponse(result, warning)
                }
            }
        })
    }

    fun getPayments(customerId: Long, fromDate: String, toDate: String, serviceType: String,
                    callback: ApiCallback<TransactionHistory>) {
        getBillTopTransaction(customerId, fromDate, toDate, serviceType, callback)
    }

    fun getBillTransactions(customerId: Long, fromDate: String, toDate: String,
                            callback: ApiCallback<TransactionHistory>) {
        getBillTopTransaction(customerId, fromDate, toDate, "Bill", callback)
    }

    fun getTopUPTransactions(customerId: Long, fromDate: String, toDate: String,
                             callback: ApiCallback<TransactionHistory>) {
        getBillTopTransaction(customerId, fromDate, toDate, "TopUp", callback)
    }

    private fun getBillTopTransaction(customerId: Long, fromDate: String, toDate: String, serviceType: String
                                      , callback: ApiCallback<TransactionHistory>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(TransactionHistoryInterface::class.java)
        val call = service.getBillTransactions(customerId, fromDate, toDate)

        call.enqueue(CallbackImpl(callback))
    }

    fun getCardTransactions(customerId: Long, fromDate: String, toDate: String, callback: ApiCallback<TransactionHistory>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(TransactionHistoryInterface::class.java)
        val call = service.getCardTransactions(customerId, fromDate, toDate)

        call.enqueue(object : CallbackImpl<TransactionHistory>(callback) {

            override fun onResponse(result: ApiResult<TransactionHistory>, warning: String?) {
                when {
                    "error" == result.status && 903 == result.code -> {
                        val cardTransactions = TransactionHistory(ArrayList(), ArrayList(), ArrayList(),ArrayList(), ArrayList(), CreditDebit(0.0, 0.0))
                        onSuccess(cardTransactions, warning)
                    }
                    else -> super.onResponse(result, warning)
                }
            }
        })
    }

    fun getCardTransactionDisputes(customerId: Long, fromDate: String, toDate: String, callback: ApiCallback<List<CardTransactionDispute>>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(TransactionHistoryInterface::class.java)
        val call = service.getCardTransactionDisputes(customerId, fromDate, toDate)

        call.enqueue(CallbackImpl(callback))
//        call.enqueue(object : CallbackImpl<CardTransactionDispute>(callback) {
//
//            override fun onResponse(result: ApiResult<CardTransactionDispute>, warning: String?) {
//                when {
//                    "error" == result.status && 903 == result.code -> {
//                        val cardTransactions = CardTransactions(ArrayList(), CreditDebit(0.0, 0.0))
//                        onSuccess(result.data, warning)
//                    }
//                    else -> super.onResponse(result, warning)
//                }
//            }
//        })
    }
}