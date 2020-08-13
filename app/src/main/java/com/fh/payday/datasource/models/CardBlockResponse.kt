package com.fh.payday.datasource.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class CardBlockResponse(
        @SerializedName("isBlocked") @Expose val isBlocked: String)