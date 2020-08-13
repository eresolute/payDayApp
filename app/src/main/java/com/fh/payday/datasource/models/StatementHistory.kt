package com.fh.payday.datasource.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class StatementHistory(
        @SerializedName("TransactionAmount") @Expose val TransactionAmount: String,
        @SerializedName("TransactionCurrency") @Expose val TransactionCurrency: String,
        @SerializedName("TransactionDateTime") @Expose val TransactionDateTime: String,
        @SerializedName("TransactionReferenceNumber") @Expose val TransactionReferenceNumber: String,
        @SerializedName("TransactionPostingDate") @Expose val TransactionPostingDate: String
)
