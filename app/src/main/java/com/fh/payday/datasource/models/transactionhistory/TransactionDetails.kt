package com.fh.payday.datasource.models.transactionhistory

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class LoanTransaction(
    @SerializedName("TransactionAmount") @Expose val TransactionAmount: String,
    @SerializedName("TransactionDate") @Expose val TransactionDate: String,
    @SerializedName("TransactionType") @Expose val TransactionType: String,
    @SerializedName("TransactionDescription") @Expose val TransactionDescription: String,
    @SerializedName("OutStandingLoanAmount") @Expose val OutStandingLoanAmount: String
)

data class BillTransactions (
        @SerializedName("Transactions")
        @Expose
        val billPaymentTransaction: List<BillPaymentTransaction>
)

data class BillPaymentTransaction(
    @SerializedName("orderId") @Expose val orderId: String,
    @SerializedName("baseAmount") @Expose val baseAmount: String,
    @SerializedName("dealerTxnId") @Expose val dealerTxnId: String,
    @SerializedName("customerId") @Expose val customerId: String,
    @SerializedName("customerName") @Expose val customerName: String,
    @SerializedName("tranDate") @Expose val tranDate: String,
    @SerializedName("serviceType") @Expose val serviceType: String,
    @SerializedName("accountNo") @Expose val accountNo: String,
    @SerializedName("amountInAed") @Expose val amountInAed: String,
    @SerializedName("baseCurrency") @Expose val baseCurrency: String,
    @SerializedName("serviceProviderKey") @Expose val serviceProviderKey: String,
    @SerializedName("status") @Expose val status: String,
    @SerializedName("serviceProvider") @Expose val serviceProvider: String
)

data class TransactionDate(
    @SerializedName("fromDate") @Expose var fromDate: String,
    @SerializedName("toDate") @Expose var toDate: String
)