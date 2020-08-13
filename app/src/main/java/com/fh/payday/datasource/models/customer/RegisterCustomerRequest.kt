package com.fh.payday.datasource.models.customer

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class RegisterCustomerRequest (
    @SerializedName("username")
    @Expose
    val userId: String,

    @SerializedName("password")
    @Expose
    val password: String,

    @SerializedName("mobile")
    @Expose
    val mobile: String,

    @SerializedName("deviceId")
    @Expose
    val deviceId: String,

    @SerializedName("geoLocation")
    @Expose
    val geoLocation: String,

    @SerializedName("osVersion")
    @Expose
    val osVersion: String,

    @SerializedName("appVersion")
    @Expose
    val appVersion: String,

    @SerializedName("languageCode")
    @Expose
    val languageCode: String
)