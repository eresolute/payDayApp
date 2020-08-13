package com.fh.payday.datasource.remote.cardservices

import com.fh.payday.datasource.models.MonthlyStatement
import com.fh.payday.datasource.models.StatementHistory
import com.fh.payday.datasource.models.transactionhistory.CardTransactions
import com.fh.payday.datasource.remote.ApiResult
import com.fh.payday.utilities.DateTime
import com.fh.payday.utilities.apiVersion
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CardServicesApiInterface {

    @GET("$apiVersion/customers/{id}/transactions")
    fun getTransactionDetails(
            @Path("id") id: Long,
            @Query("fromDate") fromDate: String = DateTime.firstDayOfCurrentMonth("yyyy-MM-dd"),
            @Query("toDate") toDate: String = DateTime.lastDayOfCurrentMonth("yyyy-MM-dd")
    ): Call<ApiResult<CardTransactions>>

    @GET("$apiVersion/customers/{id}/transactions?transactionType=6S")
    fun getSalariesCredited(
            @Path("id") customerId: Long,
            @Query("fromDate") fromDate: String = DateTime.currentDayOfLastSixMonths("yyyy-MM-dd"),
            @Query("toDate") toDate: String = DateTime.currentDate("yyyy-MM-dd")
    ): Call<ApiResult<CardTransactions>>

    @GET("$apiVersion/customers/{id}/statementHistory")
    fun getStatementHistory(
            @Path("id")id: Long,
            @Query("issue")type: String
    ): Call<ApiResult<List<StatementHistory>>>

    @GET("$apiVersion/customers/{id}/statementHistory")
    fun monthlyStatement(
            @Path("id")id: Long
    ): Call<ApiResult<List<MonthlyStatement>>>
}