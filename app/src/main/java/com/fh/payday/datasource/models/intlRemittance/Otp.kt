package com.fh.payday.datasource.models.intlRemittance

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Otp(
    @SerializedName("responseCode")
    @Expose
    val responseCode: String,
    @SerializedName("responseDesc")
    @Expose
    val responseDesc: String)