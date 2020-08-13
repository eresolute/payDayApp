package com.fh.payday.datasource.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ResetPin(
        @SerializedName("secret") @Expose val secret: String
)