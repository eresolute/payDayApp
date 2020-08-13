package com.fh.payday.datasource.remote.cardservices

import com.fh.payday.datasource.models.MonthlyStatement
import com.fh.payday.datasource.models.StatementHistory
import com.fh.payday.datasource.models.transactionhistory.CardTransactions
import com.fh.payday.datasource.models.transactionhistory.CreditDebit
import com.fh.payday.datasource.remote.ApiCallback
import com.fh.payday.datasource.remote.ApiClient
import com.fh.payday.datasource.remote.ApiResult
import com.fh.payday.datasource.remote.CallbackImpl

class CardService private constructor(
   var token: String? = null,
   var sessionId: String? = null,
   var refreshToken: String? = null
) {

    companion object {
        val instance: CardService by lazy { CardService() }

        fun getInstance(token: String, sessionId: String, refreshToken: String): CardService {
            return instance.also { it.token = token; it.sessionId = sessionId; it.refreshToken = refreshToken }
        }
    }

    fun getTransactionDetails(customerId: Long, callback: ApiCallback<CardTransactions>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(CardServicesApiInterface::class.java)
        val call = service.getTransactionDetails(customerId)

        call.enqueue(object : CallbackImpl<CardTransactions>(callback) {

            override fun onResponse(result: ApiResult<CardTransactions>, warning: String?) {
                when {
                    "error" == result.status && 903 == result.code -> {
                        val cardTransactions = CardTransactions(ArrayList(), CreditDebit(0.0, 0.0))
                        onSuccess(cardTransactions, warning)
                    }
                    else -> super.onResponse(result, warning)
                }
            }
        })
    }

    fun getSalariesCredited(customerId: Long, callback: ApiCallback<CardTransactions>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(CardServicesApiInterface::class.java)
        val call = service.getSalariesCredited(customerId)

        call.enqueue(object : CallbackImpl<CardTransactions>(callback) {

            override fun onResponse(result: ApiResult<CardTransactions>, warning: String?) {
                when {
                    "error" == result.status && 903 == result.code -> {
                        val cardTransactions = CardTransactions(ArrayList(), CreditDebit(0.0, 0.0))
                        onSuccess(cardTransactions, warning)
                    }
                    else -> super.onResponse(result, warning)
                }
            }
        })
    }

    fun getStatementHistory(customerId: Long, type: String, callback: ApiCallback<List<StatementHistory>>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(CardServicesApiInterface::class.java)
        val call = service.getStatementHistory(customerId, type)

        call.enqueue(object : CallbackImpl<List<StatementHistory>>(callback) {
            override fun onResponse(result: ApiResult<List<StatementHistory>>, warning: String?) {
                when {
                    "error" == result.status && 903 == result.code -> {
                        callback.onSuccess(ArrayList())
                    }
                    else -> super.onResponse(result, warning)
                }
            }
        })
    }

    fun monthlyStatement(customerId: Long, callback: ApiCallback<List<MonthlyStatement>>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(CardServicesApiInterface::class.java)
        val call = service.monthlyStatement(customerId)

        call.enqueue(object : CallbackImpl<List<MonthlyStatement>>(callback) {
            override fun onResponse(result: ApiResult<List<MonthlyStatement>>, warning: String?) {
                when {
                    "error" == result.status && 903 == result.code -> {
                        callback.onSuccess(ArrayList())
                    }
                    else -> super.onResponse(result, warning)
                }
            }
        })
    }
}