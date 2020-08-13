package com.fh.payday.datasource.models.transactionhistory

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class CardTransactions(
    @SerializedName("Transactions") @Expose
    val Transactions: List<Transactions>,
    @SerializedName("CreditDebit") @Expose
    val CreditDebit: CreditDebit
)

data class CreditDebit(
    @SerializedName("TotalCredit") @Expose
    val TotalCredit: Double,
    @SerializedName("TotalDebit") @Expose
    val TotalDebit: Double
)

data class Transactions(
    @SerializedName("TransactionAmount") @Expose
    val TransactionAmount: String,
    @SerializedName("TransactionCurrency") @Expose
    val TransactionCurrency: String,
    @SerializedName("TransactionType") @Expose
    val TransactionType: String,
    @SerializedName("TransactionDateTime") @Expose
    val TransactionDateTime: String,
    @SerializedName("TransactionPostingDate") @Expose
    val TransactionPostingDate: String,
    @SerializedName("TransactionDescription") @Expose
    val TransactionDescription: String?,
    @SerializedName("TransactionReferenceNumber") @Expose
    val TransactionReferenceNumber: String,
    @SerializedName("EPPFlag") @Expose
    val EPPFlag: String,
    @SerializedName("ReferenceSequence") @Expose
    val ReferenceSequence: String,
    @SerializedName("TransactionCategory") @Expose
    val TransactionCategory: String,
    @SerializedName("MerchantName") @Expose
    val MerchantName: String? = null
)
