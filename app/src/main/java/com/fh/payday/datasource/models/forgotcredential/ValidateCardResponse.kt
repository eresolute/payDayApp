package com.fh.payday.datasource.models.forgotcredential

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ValidateCardResponse(
        @SerializedName("customerId")
        @Expose
        val customerId: Long,
        @SerializedName("mobile")
        @Expose
        val mobileNumber: String

)
