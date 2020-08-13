package com.fh.payday.datasource.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class PaymentHoliday(
        @SerializedName("title") @Expose val title: String,
        @SerializedName("emi") @Expose val emi: Float,
        @SerializedName("due") @Expose val due: Float,
        @SerializedName("loanNumber") @Expose val loanNumber: String?
)