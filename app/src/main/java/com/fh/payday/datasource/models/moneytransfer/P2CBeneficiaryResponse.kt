package com.fh.payday.datasource.models.moneytransfer

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class P2CBeneficiaryResponse(
        @SerializedName("beneficiaryId") @Expose val beneficiaryId: Int,
        @SerializedName("creditCardNo") @Expose val creditCardNo: String,
        @SerializedName("shortName") @Expose val shortName: String,
        @SerializedName("beneficiaryName") @Expose val beneficiaryName: String,
        @SerializedName("customerId") @Expose val customerId: String,
        @SerializedName("enabled") @Expose val enabled: Boolean,
        @SerializedName("updatedAt") @Expose val updatedAt: String,
        @SerializedName("createdAt") @Expose val createdAt: String
)
