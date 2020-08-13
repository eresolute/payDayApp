package com.fh.payday.datasource.models.customer

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Profile(
        @SerializedName("selfie")
        @Expose
        val imageUrl: String
)