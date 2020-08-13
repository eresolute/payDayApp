package com.fh.payday.datasource.models.intlRemittance

import com.google.gson.annotations.SerializedName

data class PayoutAgentBranches(

        @SerializedName("bankTransferStatus")
        val bankTransferStatus: String,
        @SerializedName("cashPickupStatus")
        val cashPickupStatus: String,
        @SerializedName("contactNo")
        val contactNo: String,
        @SerializedName("payOutAgentId")
        val payOutAgentId: Long,
        @SerializedName("payOutBranchAddress")
        val payOutBranchAddress: String?,
        @SerializedName("payOutBranchCity")
        val payOutBranchCity: String?,
        @SerializedName("payOutBranchDistrict")
        val payOutBranchDistrict: String,
        @SerializedName("payOutBranchId")
        val payOutBranchId: Long,
        @SerializedName("payOutBranchName")
        val payOutBranchName: String,
        @SerializedName("payOutBranchState")
        val payOutBranchState: String?,
        @SerializedName("status")
        val status: String,
        @SerializedName("ifscCode")
        val ifscCode: String?,
        @SerializedName("ibanNo")
        val ibanNo: String?,
        @SerializedName("swiftCode")
        val swiftCode: String?
) {
    override fun toString(): String = payOutBranchState!!

}


