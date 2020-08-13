package com.fh.payday.datasource.models

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Plan @JvmOverloads constructor(
        @SerializedName("amount") @Expose val amount: Double,
        @SerializedName("cost") @Expose val cost: Double,
        @SerializedName("currency") @Expose val currency: String = "AED",
        @SerializedName("planId") @Expose val planId: String? = null,
        @SerializedName("typeKey") @Expose val typeKey: Int? = null,
        @SerializedName("validationRegex ") @Expose val validationRegex: String? = null,
        @SerializedName("rechargeDescription") @Expose val rechargeDescription: String? = null
) : Parcelable