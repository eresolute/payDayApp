package com.fh.payday.datasource.models.message

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class MessageBody(
        @SerializedName("id") @Expose val id: Long,
        @SerializedName("username") @Expose val username: String,
        @SerializedName("customerId") @Expose val customerId: String,
        @SerializedName("subject") @Expose val subject: String,
        @SerializedName("body") @Expose val body: String,
        @SerializedName("issue") @Expose val issue: String,
        @SerializedName("reply") @Expose val reply: String,
        @SerializedName("status") @Expose val status: String,
        @SerializedName("updatedByUserId") @Expose val updatedByUserId: String,
        @SerializedName("updatedByUsername") @Expose val updatedByUsername: String,
        @SerializedName("createdAt") @Expose val createdAt: String,
        @SerializedName("updatedAt") @Expose val updatedAt: String
)
