package com.fh.payday.datasource.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * PayDayFH
 * Created by EResolute on 10/27/2018.
 */
data class CurrencyConv(
        @SerializedName("fromCountry") @Expose val fromCountry: Country?,
        @SerializedName("toCountry") @Expose val toCountry: Country?,
        @SerializedName("fromValue") @Expose val fromValue: Double,
        @SerializedName("toValue") @Expose val toValue: Double,
        @SerializedName("date") @Expose val date: String
)