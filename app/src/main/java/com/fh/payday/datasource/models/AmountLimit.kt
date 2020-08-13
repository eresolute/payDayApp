package com.fh.payday.datasource.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class AmountLimit(
    @SerializedName("MinAmount") @Expose val MinAmount: String,
    @SerializedName("MaxAmount") @Expose val MaxAmount: String
)