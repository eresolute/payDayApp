package com.fh.payday.datasource.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class MonthlyStatement(
        @SerializedName("StatementDate") @Expose val StatementDate: String,
        @SerializedName("TotalCredit") @Expose val TotalCredit: String,
        @SerializedName("TotalDebit") @Expose val TotalDebit: String
)