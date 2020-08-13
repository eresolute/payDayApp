package com.fh.payday.datasource.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Option(
        @SerializedName("option") @Expose val option: String,
        @SerializedName("image") @Expose val image: Int,
        @SerializedName("btn") @Expose val btn: String
)