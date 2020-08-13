package com.fh.payday.datasource.remote.transactionhistory

import com.fh.payday.datasource.models.CardTransactionDispute
import com.fh.payday.datasource.models.TransactionHistory
import com.fh.payday.datasource.models.transactionhistory.LoanTransaction
import com.fh.payday.datasource.remote.ApiResult
import com.fh.payday.utilities.DateTime
import com.fh.payday.utilities.apiVersion
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TransactionHistoryInterface {

    @GET("$apiVersion/customers/{id}/loan")
    fun getLoanTransaction(
            @Path("id") customerId: Long
    ): Call<ApiResult<List<LoanTransaction>>>

    @GET("$apiVersion/customers/{id}/newTransactions")
    fun getBillTransactions(
            @Path("id") customerId: Long,
            @Query("fromDate") fromDate: String,
            @Query("toDate") toDate: String
    ): Call<ApiResult<TransactionHistory>>

    @GET("$apiVersion/customers/{id}/newTransactions")
    fun getCardTransactions(
            @Path("id") customerId: Long,
            @Query("fromDate") fromDate: String = DateTime.firstDayOfCurrentMonth("yyyy-MM-dd"),
            @Query("toDate") toDate: String = DateTime.lastDayOfCurrentMonth("yyyy-MM-dd")
    ): Call<ApiResult<TransactionHistory>>

    @GET("$apiVersion/customers/{id}/disputeTransactions")
    fun getCardTransactionDisputes(
            @Path("id") customerId: Long,
            @Query("fromDate") fromDate: String = DateTime.firstDayOfCurrentMonth("yyyy-MM-dd"),
            @Query("toDate") toDate: String = DateTime.lastDayOfCurrentMonth("yyyy-MM-dd")
    ): Call<ApiResult<List<CardTransactionDispute>>>
}