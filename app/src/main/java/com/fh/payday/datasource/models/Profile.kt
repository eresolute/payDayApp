package com.fh.payday.datasource.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class EmergencyContact(
    @SerializedName("mobileNumber") @Expose val mobileNumber: String?,
    @SerializedName("name") @Expose val name: String?,
    @SerializedName("relation") @Expose val relation: String?
)

data class Profile(
    @SerializedName("passportVerified") @Expose val passportVerified: Boolean,
    @SerializedName("emiratesVerified") @Expose val emiratesVerified: String,
    @SerializedName("selfie") @Expose val selfie: String,
    @SerializedName("mobile") @Expose val mobile: String,
    @SerializedName("emailId") @Expose val emailId: String,
    @SerializedName("dob") @Expose val dob: String,
    @SerializedName("cardName") @Expose val cardName: String?,
    @SerializedName("passportNo") @Expose val passportNo: String,
    @SerializedName("emergencyContacts") @Expose val emergencyContacts: List<EmergencyContact>?
)
