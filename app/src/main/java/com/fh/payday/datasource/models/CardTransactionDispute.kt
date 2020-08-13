package com.fh.payday.datasource.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class CardTransactionDispute(
        @SerializedName("TransactionAmount") @Expose val TransactionAmount: String,
        @SerializedName("TransactionCurrency") @Expose val TransactionCurrency: String,
        @SerializedName("TransactionType") @Expose val TransactionType: String,
        @SerializedName("TransactionDateTime") @Expose val TransactionDateTime: String,
        @SerializedName("TransactionPostingDate") @Expose val TransactionPostingDate: String,
        @SerializedName("TransactionDescription") @Expose val TransactionDescription: String,
        @SerializedName("TransactionReferenceNumber") @Expose val TransactionReferenceNumber: String,
        @SerializedName("MerchantName") @Expose val MerchantName: String,
        @SerializedName("AuthenticationCode") @Expose val AuthenticationCode: String,
        @SerializedName("TransactionCategory") @Expose val TransactionCategory: String,
        @SerializedName("remarks") @Expose val remarks: String?,
        @SerializedName("status") @Expose val status: String?
)