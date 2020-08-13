package com.fh.payday.datasource.models.intlRemittance

import android.databinding.BindingAdapter
import android.net.Uri
import android.os.Parcelable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.fh.payday.datasource.models.Card
import com.fh.payday.datasource.remote.ApiResult
import com.fh.payday.utilities.BASE_URL
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize


@Parcelize
data class AlFardanIntlBeneficiary(
        @SerializedName("accessKey")
        val accessKey: String,
        @SerializedName("flagPath")
        val flagPath: String? = null,
        @SerializedName("payoutAgentBranchId")
        val payoutAgentBranchId: Int,
        @SerializedName("payoutAgentId")
        val payoutAgentId: Int,
        @SerializedName("payoutCcyCode")
        val payoutCcyCode: String,
        @SerializedName("receiverBankAccountName")
        val receiverBankAccountName: String,
        @SerializedName("receiverBankAccountNo")
        val receiverBankAccountNo: String,
        @SerializedName("receiverBankBranchName")
        val receiverBankBranchName: String,
        @SerializedName("receiverBankName")
        val receiverBankName: String,
        @SerializedName("receiverCountryCode")
        val receiverCountryCode: String,
        @SerializedName("receiverFirstName")
        val receiverFirstName: String,
        @SerializedName("receiverLastName")
        val receiverLastName: String,
        @SerializedName("receiverMiddleName")
        val receiverMiddleName: String?,
        @SerializedName("receiverMobile")
        val receiverMobile: String?,
        @SerializedName("receiverStatus")
        val receiverStatus: String,
        @SerializedName("recieverRefNumber")
        val recieverRefNumber: Int
) : Parcelable

@Parcelize
data class IntlBeneficiary(
        @SerializedName("recieverRefNumber")
        @Expose
        val recieverRefNumber: String,

        @SerializedName("receiverFirstName")
        @Expose
        val receiverFirstName: String,

        @SerializedName("receiverMiddleName")
        @Expose
        val receiverMiddleName: String?,

        @SerializedName("receiverLastName")
        @Expose
        val receiverLastName: String,

        @SerializedName("receiverBankAccountNo")
        @Expose
        val receiverBankAccountNo: String?,

        @SerializedName("receiverCountryCode")
        @Expose
        val receiverCountryCode: String,

        @SerializedName("receiverMobile")
        @Expose
        val receiverMobile: String?,

        @SerializedName("receiverStatus")
        @Expose
        val receiverStatus: String,

        @SerializedName("receiverBankAccountName")
        @Expose
        val receiverBankAccountName: String,

        @SerializedName("flagPath")
        @Expose
        val flagPath: String? = null,

        @SerializedName("receiverBankName")
        @Expose
        val receiverBankName: String,

        @SerializedName("payoutCcyCode")
        @Expose
        val payoutCcyCode: String,
        @SerializedName("accessKey")
        @Expose
        val accessKey: String,
        @SerializedName("favourite")
        @Expose
        val favourite: String

) : Parcelable

@Parcelize
data class Exchange(
        @SerializedName("accessKey")
        val accessKey: String,
        @SerializedName("emailId")
        val emailId: String?,
        @SerializedName("remitterId")
        val remitterId: String?,
        @SerializedName("isActive")
        val isActive: String
) : Parcelable

@Parcelize
data class FardanResponse(
        @SerializedName("accessKey")
        val accessKey: String?,
        @SerializedName("emailId")
        val emailId: String?,
        @SerializedName("remitterId")
        val remitterId: String?
) : Parcelable

data class RateConversion(
        @SerializedName("responseCode")
        @Expose
        val responseCode: String,
        @SerializedName("responseDesc")
        @Expose
        val responseDesc: String,
        @SerializedName("payInAmount")
        @Expose
        val payInAmount: String,
        @SerializedName("payOutAmount")
        @Expose
        val payOutAmount: String,
        @SerializedName("payInCurrency")
        @Expose
        val payInCurrency: String,
        @SerializedName("payOutCurrency")
        @Expose
        val payOutCurrency: String,
        @SerializedName("exchangeRate")
        @Expose
        val exchangeRate: String,
        @SerializedName("commission")
        @Expose
        val commission: String,
        @SerializedName("vat")
        @Expose
        val vat: String,
        @SerializedName("totalPayableAmount")
        @Expose
        val totalPayableAmount: String,
        @SerializedName("emailId")
        @Expose
        val emailId: String,
        @SerializedName("payInFlagPath")
        @Expose
        val payInFlag: String? = null,
        @SerializedName("payOutFlagPath")
        @Expose
        val payOutFlag: String? = null,
        @SerializedName("fx_QuoteNo")
        @Expose
        val fx_QuoteNo: String? = null
)

@Parcelize
data class CountryCurrency(
        @SerializedName("id")
        @Expose
        val id: String,
        @SerializedName("country")
        @Expose
        val country: String,
        @SerializedName("countryCode")
        @Expose
        val countryCode: String,
        @SerializedName("currency")
        @Expose
        val currency: String,
        @SerializedName("dialCode")
        @Expose
        val dialCode: String,
        @SerializedName("imagePath")
        @Expose
        val imagePath: String,
        @SerializedName("createdAt")
        @Expose
        val createdAt: String,
        @SerializedName("updatedAt")
        @Expose
        val updatedAt: String
) : Parcelable {
    companion object {
        @BindingAdapter("profileImage")
        @JvmStatic
        fun loadImage(view: ImageView, imageUrl: String) {
            Glide.with(view.context)
                    .load(Uri.parse("$BASE_URL/$imageUrl"))
                    .into(view)
        }
    }

}

data class FundTransferResponse(
        @SerializedName("ResponseCode")
        @Expose
        val responseCode: String,
        @SerializedName("ResponseMessage")
        @Expose
        val responseMessage: String,
        @SerializedName("PayInAmount")
        @Expose
        val payInAmount: String,
        @SerializedName("PayInCurrency")
        @Expose
        val payInCurrency: String,
        @SerializedName("PayOutCurrency")
        @Expose
        val payOutCurrency: String,
        @SerializedName("PayOutAmount")
        @Expose
        val payOutAmount: String,
        @SerializedName("PaymentMode")
        @Expose
        val paymentMode: String,
        @SerializedName("Commission")
        @Expose
        val commission: String,
        @SerializedName("Vat")
        @Expose
        val vat: String,
        @SerializedName("ExchangeRatePayIn2PayOut")
        @Expose
        val exchangeRatePayIn2PayOut: String,
        @SerializedName("OrderId")
        @Expose
        val orderId: String,
        @SerializedName("ProviderTransactionId")
        @Expose
        val providerTransactionId: String,
        @SerializedName("fhTransactionRef")
        @Expose
        val fhTransactionRef: String,
        @SerializedName("TaxInvoiceNo")
        @Expose
        val taxInvoiceNo: String,
        @SerializedName("totalPayinAmount")
        @Expose
        val totalPayInAmount: String,
        @SerializedName("availableBalance")
        @Expose
        val availableBalance: String
)

@Parcelize
data class IntlTransaction(
        @SerializedName("dealerTxnId")
        @Expose
        val dealerTxnId: String,
        @SerializedName("partnerTxnRefNo")
        @Expose
        val partnerTxnRefNo: String,
        @SerializedName("tranDate")
        @Expose
        val tranDate: String,
        @SerializedName("serviceType")
        @Expose
        val serviceType: String,
        @SerializedName("amountInAed")
        @Expose
        val amountInAed: String,
        @SerializedName("baseAmount")
        @Expose
        val baseAmount: String,
        @SerializedName("payOutAmount")
        @Expose
        val payOutAmount: String,
        @SerializedName("receiverRefNumber")
        @Expose
        val receiverRefNumber: String?,
        @SerializedName("payOutCurrency")
        @Expose
        val payOutCurrency: String,
        @SerializedName("exchangeRate")
        @Expose
        val exchangeRate: String,
        @SerializedName("status")
        @Expose
        val status: String,
        @SerializedName("message")
        @Expose
        val message: String,
        @SerializedName("beneficiaryName")
        @Expose
        val beneficiaryName: String,
        @SerializedName("receiverCountryCode")
        @Expose
        val receiverCountryCode: String?,
        @SerializedName("accountNo")
        @Expose
        val beneficiaryAccountNumber: String,
        @SerializedName("receiverBankName")
        @Expose
        val beneficiaryBankName: String?,
        @SerializedName("favourite")
        @Expose
        val favourite: String,
        @SerializedName("receiverFlag")
        @Expose
        val receiverFlag: String? = null,
        @SerializedName("accessKey")
        @Expose
        val accessKey: String,
        @SerializedName("paymentMode")
        @Expose
        val paymentMode: String
) : Parcelable

data class TransactionDetail(
        @SerializedName("txnRefNo")
        @Expose
        val txnRefNo: String,
        @SerializedName("partnerTxnRefNo")
        @Expose
        val partnerTxnRefNo: String,
        @SerializedName("transactionGMTDateTime")
        @Expose
        val transactionGMTDateTime: String,
        @SerializedName("senderFirstName")
        @Expose
        val senderFirstName: String,
        @SerializedName("senderMiddleName")
        @Expose
        val senderMiddleName: String,
        @SerializedName("senderLastName")
        @Expose
        val senderLastName: String,
        @SerializedName("senderPOBox")
        @Expose
        val senderPOBox: String,
        @SerializedName("senderAddress1")
        @Expose
        val senderAddress1: String,
        @SerializedName("senderAddress2")
        @Expose
        val senderAddress2: String,
        @SerializedName("senderCity")
        @Expose
        val senderCity: String,
        @SerializedName("senderState")
        @Expose
        val senderState: String,
        @SerializedName("senderCountryCode")
        @Expose
        val senderCountryCode: String,
        @SerializedName("senderZipCode")
        @Expose
        val senderZipCode: String,
        @SerializedName("receiverFirstName")
        @Expose
        val receiverFirstName: String,
        @SerializedName("receiverLastName")
        @Expose
        val receiverLastName: String,
        @SerializedName("receiverNationalityCode")
        @Expose
        val receiverNationalityCode: String,
        @SerializedName("receiverDateOfBirth")
        @Expose
        val receiverDateOfBirth: String,
        @SerializedName("receiverBankAccountNo")
        @Expose
        val receiverBankAccountNo: String,
        @SerializedName("receiverBankAccountName")
        @Expose
        val receiverBankAccountName: String,
        @SerializedName("receiverBankName")
        @Expose
        val receiverBankName: String,
        @SerializedName("receiverBankAddress1")
        @Expose
        val receiverBankAddress1: String,
        @SerializedName("receiverBankAddress2")
        @Expose
        val receiverBankAddress2: String,
        @SerializedName("receiverBankCity")
        @Expose
        val receiverBankCity: String,
        @SerializedName("receiverBankState")
        @Expose
        val receiverBankState: String,
        @SerializedName("receiverBankCountryCode")
        @Expose
        val receiverBankCountryCode: String,
        @SerializedName("sourceOfIncome")
        @Expose
        val sourceOfIncome: String,
        @SerializedName("purposeOfTxn")
        @Expose
        val purposeOfTxn: String,
        @SerializedName("xchgRatePayin2Payout")
        @Expose
        val xchgRatePayin2Payout: String,
        @SerializedName("payinCcyCode")
        @Expose
        val payinCcyCode: String,
        @SerializedName("payinAmount")
        @Expose
        val payinAmount: String,
        @SerializedName("totalPayInAmount")
        @Expose
        val totalPayInAmount: String,
        @SerializedName("payoutCcyCode")
        @Expose
        val payoutCcyCode: String,
        @SerializedName("payoutAmount")
        @Expose
        val payoutAmount: String,
        @SerializedName("paymentMode")
        @Expose
        val paymentMode: String,
        @SerializedName("commission")
        @Expose
        val commission: String,
        @SerializedName("tax")
        @Expose
        val tax: String,
        @SerializedName("transactionStatusDesc")
        @Expose
        val transactionStatusDesc: String,
        @SerializedName("transactionStatusCode")
        @Expose
        val transactionStatusCode: String,
        @SerializedName("statusRecordGMTDate")
        @Expose
        val statusRecordGMTDate: String,
        @SerializedName("taxInvoiceNo")
        @Expose
        val taxInvoiceNo: String,
        @SerializedName("senderName")
        @Expose
        val senderName: String,
        @SerializedName("emailId")
        @Expose
        val emailId: String,
        @SerializedName("promoCode")
        @Expose
        val promoCode: String,
        @SerializedName("senderCountry")
        @Expose
        val senderCountry: String,
        @SerializedName("receiverCountryCode")
        @Expose
        val receiverCountryCode: String,
        @SerializedName("receiverFlag")
        @Expose
        val receiverFlag: String,
        @SerializedName("through")
        @Expose
        val through: String
)

data class CustomerDetail(
        @SerializedName("agentCode")
        @Expose
        val agentCode: String,
        @SerializedName("customerInfo")
        @Expose
        val customerInfo: List<CustomerInfo>
)

data class CustomerInfo(
        @SerializedName("customerNo")
        @Expose
        val customerNo: String,
        @SerializedName("partnerCustomerNo")
        @Expose
        val partnerCustomerNo: String,
        @SerializedName("firstName")
        @Expose
        val firstName: String,
        @SerializedName("middleName")
        @Expose
        val middleName: String,
        @SerializedName("lastName")
        @Expose
        val lastName: String,
        @SerializedName("dateOfBirth")
        @Expose
        val dateOfBirth: String,
        @SerializedName("mobile")
        @Expose
        val mobile: String,
        @SerializedName("phone")
        @Expose
        val phone: String,
        @SerializedName("email")
        @Expose
        val email: String,
        @SerializedName("employerName")
        @Expose
        val employerName: String,
        @SerializedName("crmID")
        @Expose
        val crmID: String,
        @SerializedName("status")
        @Expose
        val status: Number,
        @SerializedName("customerIDInfo")
        @Expose
        val customerIDInfo: List<CustomerIDInfo>
)

data class CustomerIDInfo(
        @SerializedName("customerID")
        @Expose
        val customerID: String,
        @SerializedName("customerIDStatus")
        @Expose
        val customerIDStatus: String
)

data class TransDetailsPDFFile(
        @SerializedName("filePath")
        @Expose
        val filePath: String
)

data class CombinedTransDetailsAndRate(
        val transDetailsResponse: ApiResult<TransactionDetail>,
        val rateConResponse: ApiResult<RateConversion>
)

data class CardInfo(
        val card: Card,
        val totalPayableAmount: String,
        val balAfterTransfer: String
)

data class GraphTransaction(
        val month: String,
        val amount: String
)

data class AlFardanIntlAddBeneficiary(
        @SerializedName("countryName")
        var countryName: String ? = null,
        @SerializedName("currency")
        var currency: String? = null,
        @SerializedName("firstName")
        var firstName: String ? = null,
        @SerializedName("lastName")
        var lastName: String ? = null,
        @SerializedName("contactNo")
        var contactNo: String? = null,
        @SerializedName("nationality")
        var nationality: String ? = null,
        @SerializedName("IdNo")
        var IdNo: String ? = "",
        @SerializedName("addressLine")
        var addressLine: String ? = null,
        @SerializedName("relationShip")
        var relationShip: String ? = null,
        @SerializedName("flagPath")
        var flagPath: String? = null,
        @SerializedName("bankName")
        var bankName: String? = null,
        @SerializedName("city")
        var city: String? = null,
        @SerializedName("state")
        var state: String? = null,
        @SerializedName("branchName")
        var branchName: String? = null,
        @SerializedName("addressLine")
        var branchAddress: String? = null,
        @SerializedName("accountNo")
        var accountNo: String? = null,
        @SerializedName("ifscCode")
        var ifscCode: String? = null,
        @SerializedName("swiftCode")
        var swiftCode: String? = null,
        @SerializedName("iBan")
        var iBan: String? = null,
        @SerializedName("deliveryMode")
        var deliveryMode: String ? = null,
        @SerializedName("countryCode")
        var countryCode: String? = null,
        @SerializedName("dialCode")
        var dialCode: String? = null


) {
    fun clear() {

    }
}

data class CreateBeneficiaries(
        @SerializedName("responseCode")
        val responseCode: String,
        @SerializedName("responseDesc")
        val responseDesc: String,
        @SerializedName("newBeneficiaryNo")
        val newBeneficiaryNo: String
)

data class DeleteBeneficiary(
        @SerializedName("responseCode")
        val responseCode: String,
        @SerializedName("responseDesc")
        val responseDesc: String
)
