package com.fh.payday.datasource.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class CardLoanResult(
        @SerializedName("cards") @Expose val cards: List<CardCustomer>,
        @SerializedName("loans") @Expose val loans: List<LoanCustomer>,
        @SerializedName("customerType") @Expose val customerType: String,
        @SerializedName("id") @Expose val id: Long? = null
)

data class CardCustomer(
        @SerializedName("CIFID") @Expose val cifId: String? = null,
        @SerializedName("CardNumber") @Expose val cardNumber: String? = null,
        @SerializedName("CardAccountNumber") @Expose val cardAccountNumber: String? = null,
        @SerializedName("CardExpiryDate") @Expose val cardExpiryDate: String? = null,
        @SerializedName("CustomerName") @Expose val customerName: String? = null,
        @SerializedName("CardStatus") @Expose val cardStatus: String? = null,
        @SerializedName("EmiratesId") @Expose val emiratesId: String? = null,
        @SerializedName("EmiratesIdExpiryDate") @Expose val emiratesIdExpiryDate: String? = null,
        @SerializedName("MobileNumber") @Expose val mobileNumber: String? = null
)

data class LoanCustomer(
        @SerializedName("CIFID") @Expose val cIFId: String,
        @SerializedName("LoanNumber") @Expose val loanNumber: String,
        @SerializedName("LoanAccountNumber") @Expose val loanAccountNumber: String? = null,
        @SerializedName("LoanAmount") @Expose val loanAmount: String? = null,
        @SerializedName("Tenor") @Expose val tenor: String? = null,
        @SerializedName("NoofInstallmentsPaid") @Expose val noOfInstallmentsPaid: String? = null,
        @SerializedName("LoanInstallment") @Expose val loanInstallment: String? = null,
        @SerializedName("LoanInterestRate") @Expose val loanInterestRate: String? = null,
        @SerializedName("LoanOpenDate") @Expose val loanOpenDate: String? = null,
        @SerializedName("TotalDueAmount") @Expose val totalDueAmount: String? = null,
        @SerializedName("LastPaymentAmount") @Expose val lastPaymentAmount: String? = null,
        @SerializedName("NextInstallmentDate") @Expose val nextInstallmentDate: String? = null,
        @SerializedName("MobileNumber") @Expose val mobileNumber: String? = null
)
