package com.fh.payday.datasource.models.intlRemittance

import com.google.gson.annotations.SerializedName

data class PayoutAgent(
        @SerializedName("agentType")
        val agentType: String,
        @SerializedName("bankTransferStatus")
        val bankTransferStatus: String,
        @SerializedName("cashPickupStatus")
        val cashPickupStatus: String?,
        @SerializedName("countryCode")
        val countryCode: String,
        @SerializedName("payOutAgentId")
        val payOutAgentId: Int,
        @SerializedName("payOutAgentName")
        val payOutAgentName: String,
        @SerializedName("payOutCurrency")
        val payOutCurrency: String
) {
    override fun toString(): String = payOutAgentName
}

