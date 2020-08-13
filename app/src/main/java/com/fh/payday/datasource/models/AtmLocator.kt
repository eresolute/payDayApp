package com.fh.payday.datasource.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class AtmLocator(
        @SerializedName("address ") @Expose val address: String,
        @SerializedName("name") @Expose val name: String,
        @SerializedName("lat") @Expose val lat: Double,
        @SerializedName("longitude") @Expose val longitude: Double,
        @SerializedName("icon") @Expose val icon: Int)