package com.fh.payday.datasource.models.payments.utilities

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Operator(

        @SerializedName("serviceProvider") @Expose val serviceProvider: String,
        @SerializedName("accessKey") @Expose val accessKey: String,
        @SerializedName("serviceImage") @Expose val serviceImage: String,
        @SerializedName("isFlexiAvailable") @Expose val isFlexAvailable: Number,
        @SerializedName("isFixedAvailable") @Expose val isFixedAvailable: Number,
        @SerializedName("isbalanceMethod") @Expose val isBalanceMethod: Number,
        @SerializedName("serviceCategory") @Expose val serviceCategory: String,
        @SerializedName("issue") @Expose val type: String
)

data class OperatorDetails(

        @SerializedName("minDenomination") @Expose val minDenomination: String,
        @SerializedName("maxDenomination") @Expose val maxDenomination: String,
        @SerializedName("baseCurrency") @Expose val baseCurrency: String,
        @SerializedName("planCurrency") @Expose val planCurrency: String,
        @SerializedName("flexiKey") @Expose val flexiKey: String,
        @SerializedName("typeKey") @Expose val typeKey: String
)

data class OwnerName(val name: String)
data class TenantName(val name: String)

data class BillDetails(
        @SerializedName ("ResponseCode") @Expose val ResponseCode: String,
        @SerializedName ("ResponseMessage") @Expose val ResponseMessage: String,
        @SerializedName ("Balance") @Expose val Balance: String,
        @SerializedName ("TransactionId") @Expose val TransactionId: String,
        @SerializedName ("PreviousBalance") @Expose val PreviousBalance: String,
        @SerializedName ("OutstandingAmount") @Expose val OutstandingAmount: String,
        @SerializedName ("BillDate") @Expose val BillDate: String,
        @SerializedName ("ProviderTransactionId") @Expose val ProviderTransactionId: String,
        @SerializedName ("OwnerName") @Expose val OwnerName: String,
        @SerializedName ("TenantName") @Expose val TenantName: String,
        @SerializedName ("BalanceInAED") @Expose val BalanceInAED: String,
        @SerializedName ("PreviousBalanceInAED") @Expose val PreviousBalanceInAED: String,
        @SerializedName ("OutstandingAmountInAED") @Expose val OutstandingAmountInAED: String
)

data class BillPaymentResponse(
        @SerializedName ("ResponseCode") @Expose val ResponseCode: String,
        @SerializedName ("datetime") @Expose val datetime: String,
        @SerializedName ("ResponseMessage") @Expose val ResponseMessage: String,
        @SerializedName ("Amount") @Expose val Amount: String,
        @SerializedName ("TransactionType") @Expose val TransactionType: String,
        @SerializedName ("paidAmountInAed") @Expose val paidAmountInAed: String,
        @SerializedName ("ProviderTransactionId") @Expose val ProviderTransactionId: String,
        @SerializedName ("orderId") @Expose val orderId: String,
        @SerializedName ("availableBalance") @Expose val availableBalance: String
)

// AADC Response

data class AadcAccountDetail(

        @SerializedName ("AccountNumber") @Expose val AccountNumber: String,
        @SerializedName ("PremiseType") @Expose val PremiseType: String,
        @SerializedName ("Area") @Expose val Area: String,
        @SerializedName ("AccountType") @Expose val AccountType: String,
        @SerializedName ("PoBox") @Expose val PoBox: String,
        @SerializedName ("Balance") @Expose val Balance: String
)

data class AadcBillResponse(
        @SerializedName ("ResponseCode") @Expose val ResponseCode: String,
        @SerializedName ("ResponseMessage") @Expose val ResponseMessage: String,
        @SerializedName ("PersonNameEnglish") @Expose val PersonNameEnglish: String,
        @SerializedName ("PersonNameArabic") @Expose val PersonNameArabic: String,
        @SerializedName ("MobileNumber") @Expose val MobileNumber: String,
        @SerializedName ("Accounts") @Expose val Accounts: List<AadcAccountDetail>,
        @SerializedName ("TransactionId") @Expose val TransactionId: String,
        @SerializedName ("dueBalanceInAed") @Expose val dueBalanceInAed: String
)

const val ACCOUNTID = "AccountID"
const val EMIRATESID = "EmiratesID"
const val MOBILENO = "MobileNo"
const val MAWAQIF_TOPUP = "mawaqif_topup"
const val PERSONID = "PersonId"

const val ACCOUNTIDLABEL = "Account Number"
const val EMIRATESIDLABEL = "Emirates ID"
const val MOBILENOLABEL = "Mobile Number"
const val PERSONIDLABEL = "Person ID"


