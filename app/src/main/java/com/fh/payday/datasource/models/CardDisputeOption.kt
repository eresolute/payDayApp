package com.fh.payday.datasource.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class CardDisputeOption(
        @SerializedName("option") @Expose val option: String,
        @SerializedName("date") @Expose val date: String,
        @SerializedName("amount") @Expose val amount: String
)