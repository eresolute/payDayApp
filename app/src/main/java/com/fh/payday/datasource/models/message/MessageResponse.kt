package com.fh.payday.datasource.models.message

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class MessageResponse(
        @SerializedName("status") @Expose val status: String,
        @SerializedName("requestNumber") @Expose val requestNumber: Long,
        @SerializedName("username") @Expose val username: String,
        @SerializedName("customerId") @Expose val customerId: String,
        @SerializedName("subject") @Expose val subject: String,
        @SerializedName("body") @Expose val body: String,
        @SerializedName("updatedAt") @Expose val updatedAt: String,
        @SerializedName("createdAt") @Expose val createdAt: String
)