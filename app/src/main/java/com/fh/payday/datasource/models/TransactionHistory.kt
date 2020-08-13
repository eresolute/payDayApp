package com.fh.payday.datasource.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class TransactionHistory(
    @SerializedName("Transactions") @Expose val Transactions: List<Transactions>,
    @SerializedName("P2P") @Expose val P2P: List<Transactions>,
    @SerializedName("P2CC") @Expose val P2CC: List<Transactions>,
    @SerializedName("P2IBAN") @Expose val P2IBAN: List<Transactions>,
    @SerializedName("BillPayments") @Expose val BillPayments: List<Transactions>,
    @SerializedName("CreditDebit") @Expose val CreditDebit: CreditDebit
)
data class CreditDebit(
    @SerializedName("TotalCredit") @Expose val TotalCredit: Double,
    @SerializedName("TotalDebit") @Expose val TotalDebit: Double
)
data class Transactions(
    @SerializedName("TransactionAmount") @Expose val TransactionAmount: String,
    @SerializedName("TransactionCurrency") @Expose val TransactionCurrency: String,
    @SerializedName("TransactionType") @Expose val TransactionType: String,
    @SerializedName("TransactionDateTime") @Expose val TransactionDateTime: String,
    @SerializedName("TransactionPostingDate") @Expose val TransactionPostingDate: String,
    @SerializedName("TransactionDescription") @Expose val TransactionDescription: String,
    @SerializedName("TransactionReferenceNumber") @Expose val TransactionReferenceNumber: String,
    @SerializedName("MerchantName") @Expose val MerchantName: String,
    @SerializedName("AuthenticationCode") @Expose val AuthenticationCode: String,
    @SerializedName("EPPFlag") @Expose val EPPFlag: String,
    @SerializedName("ReferenceSequence") @Expose val ReferenceSequence: String,
    @SerializedName("TransactionCategory") @Expose val TransactionCategory: String
)
