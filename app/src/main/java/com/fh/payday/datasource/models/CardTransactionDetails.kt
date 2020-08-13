package com.fh.payday.datasource.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class CardTransactionDetails(
        @SerializedName("transactionType") @Expose val transactionType: String,
        @SerializedName("transactionDate") @Expose val transactionDate: String,
        @SerializedName("transactionAmount") @Expose val transactionAmount: String,
        @SerializedName("statements") @Expose var statements: List<String>
)