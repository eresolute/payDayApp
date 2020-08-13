package com.fh.payday.datasource.models

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CustomerSummary(
        @SerializedName("cards") var cards: List<Card>,
        @SerializedName("loans") var loans: List<Loan>,
        @SerializedName("selfie") var selfie: String?,
        @SerializedName("unreadBellNotifications") var unreadBellNotifications: String?,
        @SerializedName("userName") var username: String?,
        @SerializedName("lastLogin") var loginDate: String?,
        @SerializedName("emiratesUpperLimitWarning") var emiratesUpperLimitWarning: String?
) : Parcelable

@Parcelize
data class Card(
        @SerializedName("AvailableBalance") val availableBalance: String,
        @SerializedName("OutstandingBalance") var outstandingBalance: String? = null,
        @SerializedName("LastStatementDate") var lastStatementDate: String? = null,
        @SerializedName("MinimumPayment") val minimumPayment: String? = null,
        @SerializedName("PaymentDueDate") var paymentDueDate: String? = null,
        @SerializedName("CardType") val cardType: String,
        @SerializedName("CardNumber") val cardNumber: String,
        @SerializedName("CustomerName") val cardName: String,
        @SerializedName("CardStatus") val cardStatus: String?,
        @SerializedName("LastSalaryCreditAmount") var lastSalaryCredit: String? = "",
        @SerializedName("CardProduct") var cardProduct: String? = "",
        @SerializedName("AccountAvailableBalance") var accountBalance: String? = "",
        @SerializedName("LastSalaryCreditDate") var lastSalaryCreditDate: String? = "",
        @SerializedName("LastPaymentReceivedDate") var lastPaymentReceivedDate: String? = "",
        @SerializedName("NextSalaryDate") var nextSalaryDate: String? = "",
        @SerializedName("AvailableOverdraftLimit") var overdraftLimit: String? = "",
        @SerializedName("TotalOverdraftLimit") var totalOverdraftLimit: String? = "-",
        @SerializedName("AmountOnHold") var amountOnHold: String? = "-"
) : Parcelable

@Parcelize
data class Loan(
        @SerializedName("LoanNumber") val loanNumber: String,
        @SerializedName("LoanType") val loanType: String,
        @SerializedName("OriginalLoanAmount") val originalLoanAmount: String,
        @SerializedName("OutStandingInstallments") val outstandingInstallments: String,
        @SerializedName("InterestRate") val interestRate: String,
        @SerializedName("LoanDisbursalDate") val loanDisbursalDate: String,
        @SerializedName("CustomerName") var customerName: String? = null,
        @SerializedName("LoanStatus") var loanStatus: String? = null,
        @SerializedName("PrincipalOutstanding") var principalOutstanding: String? = null,
        @SerializedName("OutStandingAmount") var outstandingAmount: String? = null,
        @SerializedName("NextEMIDueAmount") var nextEMIDueAmount: String? = null,
        @SerializedName("NextEMIDueDate") var nextEMIDueDate: String? = null,
        @SerializedName("MonthlyInstallment") var monthlyInstallment: String? = null,
        @SerializedName("NumberofInstalmentsPaid") var noOfInstallmentsPaid: String? = null,
        @SerializedName("NextInstallmentDate") var nextInstallmentDate: String? = null,
        @SerializedName("MaturityDate") var maturityDate: String? = null
) : Parcelable

data class CardScanData(
    @SerializedName("cardNumber") @Expose val cardNumber: String,
    @SerializedName("cardName") @Expose val cardName: String,
    @SerializedName("expiry") @Expose val expiry: String
)

data class PaydayCard(
    @SerializedName("cardNumber")  @Expose var cardNumber: String = "",
    @SerializedName("cardName")  @Expose var cardName: String = "",
    @SerializedName("expiry")  @Expose var expiry: String = ""
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "")

    fun isValid() = !TextUtils.isEmpty(cardNumber) || !TextUtils.isEmpty(cardName) || !TextUtils.isEmpty(expiry)
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(cardNumber)
        parcel.writeString(cardName)
        parcel.writeString(expiry)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PaydayCard> {
        override fun createFromParcel(parcel: Parcel): PaydayCard {
            return PaydayCard(parcel)
        }

        override fun newArray(size: Int): Array<PaydayCard?> {
            return arrayOfNulls(size)
        }
    }
}