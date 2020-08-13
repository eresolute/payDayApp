package com.fh.payday.datasource.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Country(
        @SerializedName("flag") @Expose val flag: Int,
        @SerializedName("name") @Expose val name: String,
        @SerializedName("abbr") @Expose val abbr: String,
        @SerializedName("currencyCode") @Expose val currencyCode: String? = null
)

data class IsoAlpha3(
        @SerializedName("country") val country: String,
        @SerializedName("code") var code: String,
        @SerializedName("countryCode") @Expose val countryCode: String ?= null,
        @SerializedName("currency") @Expose val currency: String ? = null,
        @SerializedName("dialCode")  @Expose val dialCode: String ?= null,
        @SerializedName("imagePath") @Expose val imagePath: String ?= null
){
    override fun toString(): String =country
}

data class Country2(
        @SerializedName("countryCode") @Expose val countryCode: Int,
        @SerializedName("countryName") @Expose val countryName: String,
        @SerializedName("countryFlag") @Expose val countryFlag: String
)