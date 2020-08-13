package com.fh.payday.datasource.models.loan

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class LoanOffer(
        @SerializedName("ApprovedAmount") @Expose val approvedAmount: String,
        @SerializedName("Tenure") @Expose val tenure: String,
        @SerializedName("EMI") @Expose val emi: String,
        @SerializedName("InterestRate") @Expose val interestRate: String,
        @SerializedName("ApplicationId") @Expose val applicationId: String,
        @SerializedName("LastSalaryCredit") @Expose val lastSalaryCredit: String,
        @SerializedName("minimumAmount") @Expose val minAmount: String
)

data class LoanAcceptance(
        @SerializedName("LoanAccountNumber") @Expose val loanAccountNo: String,
        @SerializedName("ReferenceNumber") @Expose val referenceNo: String
)