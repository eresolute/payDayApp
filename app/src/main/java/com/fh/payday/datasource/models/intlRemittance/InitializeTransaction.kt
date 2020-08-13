package com.fh.payday.datasource.models.intlRemittance

import com.google.gson.annotations.SerializedName

data class InitializeTransaction(
        @SerializedName("billdetails")
        val billDetails: List<Details>,
        @SerializedName("fundsources")
        val fundSources: List<Details>,
        @SerializedName("paymentmodes")
        val paymentModes: List<Details>,
        @SerializedName("transactionpurposes")
        val transactionPurposes: List<Details>,
        @SerializedName("payoutccy")
        val payoutccy: String,
        @SerializedName("payoutrate")
        val payoutrate: String,
        @SerializedName("responsecode")
        val responsecode: String,
        @SerializedName("responsedata")
        val responsedata: String,
        @SerializedName("trackingid")
        val trackingid: String? = ""
)

data class Details(
        @SerializedName("code")
        val code: String,
        @SerializedName("name")
        val name: String
)