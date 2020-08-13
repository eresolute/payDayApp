package com.fh.payday.datasource.models.loan

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class LoanRequest(
        @SerializedName("requestId") @Expose val requestId: String,
        @SerializedName("loanNumber") @Expose val loanNumber: String,
        @SerializedName("dateTime") @Expose val dateTime: String,
        @SerializedName("status") @Expose val status: String,
        @SerializedName("category") @Expose val category: String,
        @SerializedName("file") @Expose val file: String?)