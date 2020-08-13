package com.fh.payday.datasource.models.moneytransfer

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class AddP2CBeneficiaryResponse(
        @SerializedName("beneficiaryId") @Expose val beneficiaryId: Int,
        @SerializedName("cardNumber") @Expose val cardNumber: String,
        @SerializedName("beneficiaryName") @Expose val beneficiaryName: String,
        @SerializedName("shortName") @Expose val shortName: String,
        @SerializedName("addedBy") @Expose val addedBy: String,
        @SerializedName("updatedAt") @Expose val updatedAt: String,
        @SerializedName("createdAt") @Expose val createdAt: String
)

data class AddP2PBeneficiaryResponse(
        @SerializedName("beneficiaryId") @Expose val beneficiaryId: Int,
        @SerializedName("mobileNumber") @Expose val mobileNumber: String,
        @SerializedName("cardAccountNo") @Expose val cardAccountNo: String,
        @SerializedName("beneficiaryName") @Expose val beneficiaryName: String,
        @SerializedName("customerId") @Expose val customerId: String,
        @SerializedName("updatedAt") @Expose val updatedAt: String,
        @SerializedName("createdAt") @Expose val createdAt: String
)

@Parcelize
data class Beneficiary(
        @SerializedName("beneficiaryId") @Expose val beneficiaryId: Int,
        @SerializedName("cardAccountNo") @Expose val cardAccountNo: String,
        @SerializedName("beneficiaryName") @Expose val beneficiaryName: String,
        @SerializedName("MobileNumber") @Expose val MobileNumber: String,
        @SerializedName("enabled") @Expose val enabled: Boolean
) : Parcelable

@Parcelize
data class PaydayBeneficiary(
        @SerializedName("CIFId") @Expose val CIFId: String,
        @SerializedName("CustomerName") @Expose val CustomerName: String,
        @SerializedName("CardAccountNumber") @Expose val CardAccountNumber: String,
        @SerializedName("AccountStatus") @Expose val AccountStatus: String,
        @SerializedName("MobileNumber") @Expose var MobileNumber: String,
        @SerializedName("ResponseCode") @Expose val ResponseCode: String,
        @SerializedName("ResponseDesc") @Expose val ResponseDesc: String
) : Parcelable

@Parcelize
data class LocalBeneficiary(
        @SerializedName("beneficiaryId") @Expose val beneficiaryId: Int,
        @SerializedName("mobileNumber") @Expose val mobileNumber: String,
        @SerializedName("beneficiaryName") @Expose val beneficiaryName: String,
        @SerializedName("accountNo") @Expose val accountNo: String,
        @SerializedName("enabled") @Expose val enabled: Boolean,
        @SerializedName("IBAN") @Expose val IBAN: String,
        @SerializedName("bank") @Expose val bank: String
) : Parcelable

@Parcelize
data class P2CBeneficiary(
        @SerializedName("beneficiaryId") @Expose val beneficiaryId: Int,
        @SerializedName("shortName") @Expose val shortName: String,
        @SerializedName("beneficiaryName") @Expose val beneficiaryName: String,
        @SerializedName("creditCardNo") @Expose val creditCardNo: String,
        @SerializedName("bankName") @Expose val bankName: String,
        @SerializedName("enabled") @Expose val enabled: Boolean
) : Parcelable