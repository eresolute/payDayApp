package com.fh.payday.datasource.models.intlRemittance

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class PayoutCountries(
    @SerializedName("countryCode") @Expose val countryCode: String,
    @SerializedName("countryName") @Expose val countryName: String,
    @SerializedName("deliveryModes") @Expose val deliveryModes: List<DeliveryModes>,
    @SerializedName("payOutCurrencies") @Expose val payOutCurrencies: List<PayOutCurrencies>,
    @SerializedName("nationality") @Expose val nationality: String,
    @SerializedName("flag") @Expose val flag: String?,
    @SerializedName("dailCode") @Expose val dialCode: String
) {
    override fun toString(): String = countryName
}

data class DeliveryModes(
    @SerializedName("code")
    val code: String,
    @SerializedName("name")
    val name: String
)

data class PayOutCurrencies(
    @SerializedName("code")
    val code: String,
    @SerializedName("name")
    val name: String
) {
    override fun toString(): String {
        return code
    }
}

