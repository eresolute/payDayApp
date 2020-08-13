package com.fh.payday.datasource.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class OfferResponse(
        @SerializedName("per_page") @Expose val per_page: Number,
        @SerializedName("status") @Expose val status: String,
        @SerializedName("code") @Expose val code: Int,
        @SerializedName("page") @Expose val page: Number,
        @SerializedName("data") @Expose val offerDetail: List<OfferDetail>,
        @SerializedName("errors") @Expose val errors: List<String>? = null
)

data class OfferDetail(
        @SerializedName("Id") @Expose val Id: Number,
        @SerializedName("bannerTitle") @Expose val bannerTitle: String,
        @SerializedName("linkTo") @Expose val linkTo: String,
        @SerializedName("fileUrl") @Expose val fileUrl: String? = null,
        @SerializedName("bannerTo") @Expose val bannerTo: String,
        @SerializedName("bannerText") @Expose val bannerText: String? = null,
        @SerializedName("sms") @Expose val sms: Boolean,
        @SerializedName("email") @Expose val email: Boolean,
        @SerializedName("pushNotification") @Expose val pushNotification: Boolean,
        @SerializedName("createdAt") @Expose val createdAt: String,
        @SerializedName("updatedAt") @Expose val updatedAt: String
)