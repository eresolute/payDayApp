package com.fh.payday.datasource.models.payments.transport

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

data class BalanceDetails(
        @SerializedName("ResponseCode") @Expose val ResponseCode: String,
        @SerializedName("ResponseMessage") @Expose val ResponseMessage: String,
        @SerializedName("Balance") @Expose val Balance: String,
        @SerializedName("TransactionId") @Expose val TransactionId: String,
        @SerializedName("dueBalanceInAed") @Expose val dueBalanceInAed: String,
        @SerializedName("ProviderTransactionId") @Expose val ProviderTransactionId: String
)

data class MawaqifBillDetail(
        @SerializedName("ResponseCode") @Expose val ResponseCode: String,
        @SerializedName("OrderId") @Expose val OrderId: String,
        @SerializedName("ResponseLevel") @Expose val ResponseLevel: String,
        @SerializedName("ResponseMessage") @Expose val ResponseMessage: String,
        @SerializedName("AccountNumber") @Expose val AccountNumber: String,
        @SerializedName("IssueDate") @Expose val IssueDate: String,
        @SerializedName("Stage") @Expose val Stage: String,
        @SerializedName("Country") @Expose val Country: String,
        @SerializedName("PlateCategory") @Expose val PlateCategory: String,
        @SerializedName("PlateType") @Expose val PlateType: String,
        @SerializedName("Reg") @Expose val Reg: String,
        @SerializedName("Location") @Expose val Location: String,
        @SerializedName("PvtAmount") @Expose val PvtAmount: String?,
        @SerializedName("CanBePaid") @Expose val CanBePaid: String,
        @SerializedName("Discount") @Expose val Discount: String,
        @SerializedName("TransactionId") @Expose val TransactionId: String,
        @SerializedName("dueBalanceInAed") @Expose val dueBalanceInAed: String
)

data class BillPaymentResponse(
        @SerializedName("ResponseCode") @Expose val ResponseCode: String,
        @SerializedName("ResponseMessage") @Expose val ResponseMessage: String,
        @SerializedName("Amount") @Expose val Amount: String,
        @SerializedName("datetime") @Expose val datetime: String,
        @SerializedName("paidAmount") @Expose val paidAmount: String,
        @SerializedName("supplierId") @Expose val supplierId: String,
        @SerializedName("TransactionType") @Expose val TransactionType: String,
        @SerializedName("paidAmountInAed") @Expose val paidAmountInAed: String,
        @SerializedName("ProviderTransactionId") @Expose val ProviderTransactionId: String,
        @SerializedName("orderId") @Expose val orderId: String,
        @SerializedName("availableBalance") @Expose val availableBalance: String

)