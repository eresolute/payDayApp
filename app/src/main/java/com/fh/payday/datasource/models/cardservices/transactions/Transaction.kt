package com.fh.payday.datasource.models.cardservices.transactions

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Transaction(
        @SerializedName("TransactionAmount") @Expose val TransactionAmount: String,
        @SerializedName("TransactionCurrency") @Expose val TransactionCurrency: String,
        @SerializedName("TransactionType") @Expose val TransactionType: String,
        @SerializedName("TransactionDateTime") @Expose val TransactionDateTime: String,
        @SerializedName("TransactionPostingDate") @Expose val TransactionPostingDate: String,
        @SerializedName("TransactionDescription") @Expose val TransactionDescription: String,
        @SerializedName("TransactionReferenceNumber") @Expose val TransactionReferenceNumber: String,
        @SerializedName("ReferenceSequence") @Expose val ReferenceSequence: String,
        @SerializedName("TransactionCategory") @Expose val TransactionCategory: String,
        @SerializedName("MerchantName") @Expose val MerchantName: String,
        @SerializedName("AuthenticationCode") @Expose val AuthenticationCode: String,
        @SerializedName("EPPFlag") @Expose val EPPFlag: String
)
