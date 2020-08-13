package com.fh.payday.datasource.models.payments

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Operator(
        @SerializedName("serviceProvider") @Expose val serviceProvider: String,
        @SerializedName("accessKey") @Expose val accessKey: String,
        @SerializedName("serviceImage") @Expose val serviceImage: String?,
        @SerializedName("isFlexiAvailable") @Expose val isFlexAvailable: Int,
        @SerializedName("isFixedAvailable") @Expose val isFixedAvailable: Int,
        @SerializedName("isbalanceMethod") @Expose val isBalanceMethod: Int,
        @SerializedName("serviceCategory") @Expose val serviceCategory: String,
        @SerializedName("type") @Expose val type: String
) : Parcelable {
    val maxLenAccountNo: Int
        get() = when (serviceCategory) {
            ServiceCategory.PREPAID, ServiceCategory.POSTPAID -> 10
            ServiceCategory.LANDLINE -> 7
            ServiceCategory.DTH, ServiceCategory.INSURANCE,
            ServiceCategory.ELECTRICITY, ServiceCategory.GAS -> 16
            else -> 16
        }

    val minLenAccountNo: Int
        get() = when (serviceCategory) {
            ServiceCategory.LANDLINE, ServiceCategory.DTH, ServiceCategory.INSURANCE -> 5
            ServiceCategory.GAS, ServiceCategory.ELECTRICITY -> 4
            else -> 10
        }
}

data class OperatorDetail(
        @SerializedName("minDenomination") @Expose val minDenomination: String,
        @SerializedName("maxDenomination") @Expose val maxDenomination: String,
        @SerializedName("baseCurrency") @Expose val baseCurrency: String,
        @SerializedName("planCurrency") @Expose val planCurrency: String,
        @SerializedName("flexiKey") @Expose val flexiKey: String,
        @SerializedName("typeKey") @Expose val typeKey: String,
        @SerializedName("validationRegex") @Expose val validationRegex: String
)

data class OperatorDetailFixed(
        @SerializedName("planId") @Expose val planId: String,
        @SerializedName("baseCurrency") @Expose val baseCurrency: String,
        @SerializedName("baseAmount") @Expose val baseAmount: Double,
        @SerializedName("planCurrency") @Expose val planCurrency: String,
        @SerializedName("planCost") @Expose val planCost: Double,
        @SerializedName("typeKey") @Expose val typeKey: Int,
        @SerializedName("validationRegex") @Expose val validationRegex: String,
        @SerializedName("rechargeDescription") @Expose val rechargeDescription: String
)

data class PayableAmount(
        @SerializedName("payableAmountInAED") @Expose val amountInAED: Double
)

data class RechargeDetail(
        @SerializedName("ResponseCode") @Expose val ResponseCode: String,
        @SerializedName("ResponseMessage") @Expose val ResponseMessage: String,
        @SerializedName("ServiceType") @Expose val ServiceType: String,
        @SerializedName("TransactionType") @Expose val TransactionType: String,
        @SerializedName("TransactionID") @Expose val TransactionID: String,
        @SerializedName("ProviderTransactionId") @Expose val ProviderTransactionId: String,
        @SerializedName("TransactionDateStamp") @Expose val TransactionDateStamp: String,
        @SerializedName("ReplyDateStamp") @Expose val ReplyDateStamp: String,
        @SerializedName("MinimumAmount") @Expose val MinimumAmount: String,
        @SerializedName("MaximumAmount") @Expose val MaximumAmount: String,
        @SerializedName("MinimumAmountInAED") @Expose val MinimumAmountInAED: String,
        @SerializedName("MaximumAmountInAED") @Expose val MaximumAmountInAED: String,
        @SerializedName("RenewalAmount") @Expose val RenewalAmount: String,
        @SerializedName("RenewalAmountInAed") @Expose val RenewalAmountInAed: String
)

data class BillDetailEtisalat(
        @SerializedName("ResponseCode") @Expose val ResponseCode: String,
        @SerializedName("ResponseMessage") @Expose val ResponseMessage: String,
        @SerializedName("ServiceType") @Expose val ServiceType: String,
        @SerializedName("TransactionType") @Expose val TransactionType: String,
        @SerializedName("TransactionID") @Expose val TransactionID: String,
        @SerializedName("ProviderTransactionId") @Expose val ProviderTransactionId: String,
        @SerializedName("TransactionDateStamp") @Expose val TransactionDateStamp: String,
        @SerializedName("ReplyDateStamp") @Expose val ReplyDateStamp: String,
        @SerializedName("AmountDue") @Expose val AmountDue: String,
        @SerializedName("AmountDueInAED") @Expose val AmountDueInAED: String
)

data class BillDetailDU(
        @SerializedName("ResponseCode") @Expose val ResponseCode: String,
        @SerializedName("TransactionId") @Expose val TransactionId: String,
        @SerializedName("Balance") @Expose val Balance: String,
        @SerializedName("OrderId") @Expose val OrderId: String,
        @SerializedName("ProviderTransactionId") @Expose val ProviderTransactionId: String,
        @SerializedName("Area") @Expose val Area: String,
        @SerializedName("PoboxNo") @Expose val PoboxNo: String,
        @SerializedName("PremiseType") @Expose val PremiseType: String,
        @SerializedName("dueBalanceInAed") @Expose val dueBalanceInAed: String
)

data class BillPaymentResponse(
        @SerializedName("Status") @Expose val Status: String,
        @SerializedName("ResponseCode") @Expose val ResponseCode: String,
        @SerializedName("ResponseMessage") @Expose val ResponseMessage: String,
        @SerializedName("TransactionId") @Expose val TransactionId: String,
        @SerializedName("AmountPaid") @Expose val AmountPaid: String,
        @SerializedName("ServiceType") @Expose val ServiceType: String,
        @SerializedName("TransactionType") @Expose val TransactionType: String,
        @SerializedName("OrderId") @Expose val OrderId: String,
        @SerializedName("ProviderTransactionId") @Expose val ProviderTransactionId: String,
        @SerializedName("TransactionDateStamp") @Expose val TransactionDateStamp: String,
        @SerializedName("ReplyDateStamp") @Expose val ReplyDateStamp: String,
        @SerializedName("availableBalance") @Expose val availableBalance: String

)

data class BillPaymentDuResponse(
        @SerializedName("status") @Expose val status: String,
        @SerializedName("datetime") @Expose val datetime: String,
        @SerializedName("ResponseCode") @Expose val ResponseCode: String,
        @SerializedName("paidAmountInAed") @Expose val paidAmountInAed: String,
        @SerializedName("paidAmount") @Expose val paidAmount: String,
        @SerializedName("supplierId") @Expose val supplierId: String,
        @SerializedName("operatorId") @Expose val operatorId: String,
        @SerializedName("orderId") @Expose val orderId: String,
        @SerializedName("availableBalance") @Expose val availableBalance: String
)

data class PaymentRequest(
        @SerializedName("accessKey") @Expose val accessKey: String,
        @SerializedName("typeKey") @Expose val typeKey: Int,
        @SerializedName("flexiKey") @Expose val flexiKey: String?,
        @SerializedName("planId") @Expose val planId: String?,
        @SerializedName("transId") @Expose val transId: String,
        @SerializedName("account") @Expose var account: String,
        @SerializedName("amount") @Expose val amount: String,
        @SerializedName("otp") @Expose val otp: String,
        @SerializedName("optional1") @Expose val optional1: String? = null,
        @SerializedName("optional4") @Expose val optional2: String? = null,
        @SerializedName("optional3") @Expose val optional3: String? = null,
        @SerializedName("optional4") @Expose val optional4: String? = null
)

data class PaymentResult(
        @SerializedName("status") @Expose val status: String,
        @SerializedName("datetime") @Expose val datetime: String,
        @SerializedName("ResponseCode") @Expose val ResponseCode: String,
        @SerializedName("paidAmountInAed") @Expose val paidAmountInAed: Double,
        @SerializedName("paidAmount") @Expose val paidAmount: String,
        @SerializedName("supplierId") @Expose val supplierId: String,
        @SerializedName("operatorId") @Expose val operatorId: String,
        @SerializedName("orderId") @Expose val orderId: String,
        @SerializedName("availableBalance") @Expose val availableBalance: String
)

data class Bill(
        @SerializedName("dueAmount") @Expose val dueAmount: Double = 0.0,
        @SerializedName("dueAmountInAed") @Expose val dueAmountInAed: Double = 0.0,
        @SerializedName("TransactionId") @Expose val transactionId: String? = null,
        @SerializedName("referenceId") @Expose val referenceId: String? = null,
        @SerializedName("BalanceAmount") @Expose val balanceAmount: Double = 0.0,
        @SerializedName("dueBalanceInAed") @Expose val dueBalanceInAed: Double = 0.0
)

@Parcelize
data class RecentAccount(
        @SerializedName("accountNo") @Expose val accountNo: String,
        @SerializedName("tranDate") @Expose val tranDate: String,
        @SerializedName("logo") @Expose var logo: String? = null) : Parcelable

@Parcelize
data class Beneficiaries(
        @SerializedName("beneficiaryId") @Expose val beneficiaryId: Number,
        @SerializedName("mobileNumber") @Expose val mobileNumber: String,
        @SerializedName("shortName") @Expose val shortName: String,
        @SerializedName("customerId") @Expose val customerId: String,
        @SerializedName("CIF") @Expose val CIF: String,
        @SerializedName("accessKey") @Expose val accessKey: String,
        @SerializedName("createdAt") @Expose val createdAt: String,
        @SerializedName("updatedAt") @Expose val updatedAt: String,
        @SerializedName("logo") @Expose var logo: String? = null,
        @SerializedName("type") @Expose var type: String? = null,
        @SerializedName("optional1") @Expose var optional1: String? = null,
        @SerializedName("optional2") @Expose var optional2: String? = null,
        @SerializedName("enabled") @Expose var enabled: Boolean
) : Parcelable

class CountryCode {
    companion object {
        const val DEFAULT = "971"
        const val INDIA = "91"
    }
}

class TypeId {
    companion object {
        const val BILL_PAYMENT = 1
        const val TOP_UP = 2
    }
}
class ServiceCategory {
    companion object {
        const val POSTPAID = "POSTPAID"
        const val LANDLINE = "LANDLINE"
        const val PREPAID = "PREPAID"
        const val DTH = "DTH"
        const val GAS = "GAS"
        const val INSURANCE = "INSURANCE"
        const val ELECTRICITY = "ELECTRICITY"
        const val TOP_UP = "TOP_UP"
        const val BILL_PAYMENT = "BILL_PAYMENT"
    }
}

class UAEServiceType {
    companion object {
        const val BROADBAND = "BROADBAND"
        const val ELIFE = "ELIFE"
        const val EVISION = "EVISION"
        const val GSM = "GSM"
        const val TIME = "TIME"
        const val CREDIT = "CREDIT"
        const val INTERNATIONAL = "INTERNATIONAL"
        const val DATA = "DATA"
        const val WASEL = "WASELRECHARGE"
    }
}

class UtilityServiceType {

    companion object {
        const val FEWA = "fewa"
        const val DEWA = "dewa"
        const val AADC = "aadc"
    }
}

class PlanType {
    companion object {
        const val FLEXI = "flexi"
        const val FIXED = "fixed"
    }
}

const val INTERNATIONAL_TOPUP = "INTERNATIONALTOPUP"
const val BSNL_LANDLINE_ACCESS_KEY = "BGL"
const val BSNL_POSTPAID_ACCESS_KEY = "BPC"
const val RELIANCE_LANDLINE_ACCESS_KEY = "RGL"
const val AIRTEL_LANDLINE_ACCESS_KEY = "ATL"
const val MTNL_LANDLINE_ACCESS_KEY = "MDL"
const val DOCOMO_LANDLINE_ACCESS_KEY = "TCL"

const val maxLenLandlineStdCode = 6
const val minLenLandlineStdCode = 2
const val maxLenBSNLAccount = 16
const val minLenBSNLAccount = 4