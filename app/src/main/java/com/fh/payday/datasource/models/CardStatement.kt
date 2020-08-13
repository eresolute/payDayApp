package com.fh.payday.datasource.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * PayDayFH
 * Created by EResolute on 10/26/2018.
 */
data class CardStatement(
        @SerializedName("month") @Expose val month: String,
        @SerializedName("year") @Expose val year: String
)