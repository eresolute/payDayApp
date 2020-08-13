package com.fh.payday.viewmodels.intlRemittance

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.fh.payday.datasource.models.Card
import com.fh.payday.datasource.models.CustomerSummary
import com.fh.payday.datasource.models.intlRemittance.*
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.customer.CustomerService
import com.fh.payday.datasource.remote.intlRemittance.IntlRemittanceService
import com.fh.payday.datasource.remote.intlRemittance.TransactionService
import com.fh.payday.datasource.remote.intlRemittance.fundtransfer.FundTransferService
import com.fh.payday.datasource.remote.intlRemittance.ratecalculator.RateCalculatorService
import com.fh.payday.datasource.remote.otp.OtpService
import com.fh.payday.utilities.Event
import com.fh.payday.utilities.isNotEmptyList
import com.fh.payday.views2.intlRemittance.ExchangeContainer
import io.reactivex.disposables.CompositeDisposable

class TransferViewmodel : ViewModel() {

    private val _intlBeneficiary = MutableLiveData<Event<NetworkState2<List<IntlBeneficiary>>>>()
    val intlBeneficiary: LiveData<Event<NetworkState2<List<IntlBeneficiary>>>>
        get() = _intlBeneficiary

    private val _deleteBeneficiaryState = MutableLiveData<Event<NetworkState2<DeleteBeneficiary>>>()
    val deleteBeneficiaryState: LiveData<Event<NetworkState2<DeleteBeneficiary>>>
        get() = _deleteBeneficiaryState


    private val _transDetailsState = MutableLiveData<Event<NetworkState2<CombinedTransDetailsAndRate>>>()
    val transDetailsState: LiveData<Event<NetworkState2<CombinedTransDetailsAndRate>>>
        get() = _transDetailsState

    private val _senderCurrencyResponse = MutableLiveData<Event<NetworkState2<RateConversion>>>()
    val senderCurrencyResponse: LiveData<Event<NetworkState2<RateConversion>>> get() = _senderCurrencyResponse

    private val _receiverCurrencyResponse = MutableLiveData<Event<NetworkState2<RateConversion>>>()
    val receiverCurrencyResponse: LiveData<Event<NetworkState2<RateConversion>>> get() = _receiverCurrencyResponse

    private val _funTransferResponse = MutableLiveData<Event<NetworkState2<FundTransferResponse>>>()
    val funTransferResponse: LiveData<Event<NetworkState2<FundTransferResponse>>>
        get() = _funTransferResponse


    private val _validateTransaction2 = MutableLiveData<Event<NetworkState2<RateConversion>>>()
    val validateTransaction2: LiveData<Event<NetworkState2<RateConversion>>>
        get() = _validateTransaction2

    private val _customerSummaryState = MutableLiveData<Event<NetworkState2<CustomerSummary>>>()
    val customerSummaryState: LiveData<Event<NetworkState2<CustomerSummary>>>
        get() = _customerSummaryState

    private val _generateOtp = MutableLiveData<Event<NetworkState2<String>>>()
    val generateOtpState: LiveData<Event<NetworkState2<String>>>
        get() = _generateOtp

    private val _purposeOfPaymentState = MutableLiveData<Event<NetworkState2<InitializeTransaction>>>()
    val purposeOfPaymentState: LiveData<Event<NetworkState2<InitializeTransaction>>>
        get() = _purposeOfPaymentState


    private val disposable = CompositeDisposable()

    var initializeTransaction: InitializeTransaction? = null
    var selectedPurposePaymentCode: String? = null

    var selectedBeneficiary: IntlBeneficiary? = null
    var customerSummary: CustomerSummary? = null
    var rateResponse: RateConversion? = null
    var selectedCard: Card? = null
    var toClear: Boolean = false
    var beneficiaryList: List<IntlBeneficiary>? = null
    var cards: ArrayList<Card>? = null
    var isChecked: Boolean = false

    var payoutCurrency: String? = null
    var payoutCountryCode: String? = null
    var payInCurrency: String? = null
    var transferPurpose: String? = null
    var promoCode: String? = null
    var amountFrom: String? = null
    var amountTo: String? = null


    var payInAmount: String? = null
    var payOutAmount: String? = null
    var totalAmount: String? = null
    var fxQuoteNo: String? = null
    var from: String? = null

    var filterExchangeName: String? = null
    var filterCcyCode: String? = null


    var selectedAccessKey: String? = null
    var selectedDeliveryMode: String = "BT"

    override fun onCleared() {
        disposable.dispose()
        super.onCleared()
    }

    fun getBeneficiaries(token: String, sessionId: String, refreshToken: String, customerId: String, accessKey: String, deliveryMode: String) {
        _intlBeneficiary.value = Event(NetworkState2.Loading())

        IntlRemittanceService.getInstance(token, sessionId, refreshToken)
                .getBeneficiaries(customerId, accessKey,deliveryMode,  object : ApiCallbackImpl<List<IntlBeneficiary>>(_intlBeneficiary) {
                    override fun onSuccess(data: List<IntlBeneficiary>) {
                        beneficiaryList = data
                        _intlBeneficiary.value = Event(NetworkState2.Success(data))
                    }
                })
    }

    fun getBeneficiariesRx(token: String, sessionId: String, refreshToken: String, customerId: String, deliveryMode : String) {
        _intlBeneficiary.value = Event(NetworkState2.Loading())
        IntlRemittanceService.getInstance(token, sessionId, refreshToken)
            .getBeneficiariesRx(customerId, ExchangeContainer.exchanges(),deliveryMode, object : ApiCallbackImpl<List<IntlBeneficiary>>(_intlBeneficiary) {
                override fun onSuccess(data: List<IntlBeneficiary>) {
                    beneficiaryList = data
                    _intlBeneficiary.value = Event(NetworkState2.Success(data))
                }
                override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                    _intlBeneficiary.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                }
            }
            )
    }

    fun deleteBeneficiary(
            token: String, sessionId: String, refreshToken: String,
            customerId: Long, accessKey: String, receiverRefNumber: String
    ) {
        _deleteBeneficiaryState.value = Event(NetworkState2.Loading())
        IntlRemittanceService.getInstance(token, sessionId, refreshToken)
                .deleteBeneficiary(customerId, accessKey, receiverRefNumber, object : ApiCallbackImpl<DeleteBeneficiary>(_deleteBeneficiaryState) {
                    override fun onSuccess(data: DeleteBeneficiary, message: String?) {
                        _deleteBeneficiaryState.value = Event(NetworkState2.Success(data, message))
                    }
                    override fun onSuccess(data: DeleteBeneficiary) {
                        onSuccess(data,"")
                    }
                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _deleteBeneficiaryState.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })
    }

    fun getCurrencyConversion(
            token: String,
            sessionId: String,
            refreshToken: String,
            customerId: String,
            payInAmount: String,
            payOutAmount: String,
            payOutCurr: String,
            receiverCountryCode: String,
            accessKey: String,
            payInCurrency: String = "AED"
    ) {
        cancelRequest()

        if (payOutAmount.isEmpty())
            _senderCurrencyResponse.value = Event(NetworkState2.Loading())
        else
            _receiverCurrencyResponse.value = Event(NetworkState2.Loading())

        val callback = if (payOutAmount.isEmpty()) {
            object : ApiCallbackImpl<RateConversion>(_senderCurrencyResponse) {
                override fun onSuccess(data: RateConversion) {
                    rateResponse = data
                    _senderCurrencyResponse.value = Event(NetworkState2.Success(data))
                }
            }
        } else {
            object : ApiCallbackImpl<RateConversion>(_receiverCurrencyResponse) {
                override fun onSuccess(data: RateConversion) {
                    rateResponse = data
                    _receiverCurrencyResponse.value = Event(NetworkState2.Success(data))
                }
            }
        }


        val d = RateCalculatorService.getInstance(token, sessionId, refreshToken)
                .getConversionRate(customerId, payInAmount.replace(",", ""),
                        payOutAmount.replace(",", ""),
                        payOutCurr, receiverCountryCode, payInCurrency, accessKey, callback)

        disposable.addAll(d)
    }

    fun fetchCustomerSummary(token: String, sessionId: String, refreshToken: String, customerId: Long) {
        _customerSummaryState.value = Event<NetworkState2<CustomerSummary>>(NetworkState2.Loading())

        CustomerService.getInstance(token, sessionId, refreshToken)
                .getSummary(customerId, object : ApiCallbackImpl<CustomerSummary>(_customerSummaryState) {
                    override fun onSuccess(data: CustomerSummary, message: String?) {
                        customerSummary = data
                        customerSummary?.apply {
                            if (isNotEmptyList(cards)) {
                                cards = cards.filter { true }
                            }

                            if (isNotEmptyList(loans)) {
                                loans = loans.filter { true }
                            }
                        }
                        _customerSummaryState.value = Event<NetworkState2<CustomerSummary>>(NetworkState2.Success(data, message))
                    }

                    override fun onSuccess(data: CustomerSummary) {
                        onSuccess(data, null)
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _customerSummaryState.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })
    }

    fun generateOtp(token: String, sessionId: String, refreshToken: String, id: Long) {
        _generateOtp.value = Event(NetworkState2.Loading())

        OtpService.getInstance(token, sessionId, refreshToken).getOtp(id.toInt(),
                object : ApiCallbackImpl<String>(_generateOtp) {
                    override fun onSuccess(data: String) {
                        _generateOtp.value = Event(NetworkState2.Success(data))
                    }
                })
    }

    fun transferFunds(
            token: String, sessionId: String, refreshToken: String, customerId: Long, firstName: String, lastName: String,
            recieverRefNumber: String, receiverAccountNumber: String, receiverBankName: String, receiverCountryCode: String,
            baseAmount: String, totalAmount: String, payoutCurrency: String, promoCode: String, accessKey: String, otp: String,
            fxQuoteNo: String, deliveryMode: String
    ) {
        _funTransferResponse.value = Event(NetworkState2.Loading())

        FundTransferService.getInstance(token, sessionId, refreshToken).fundTransfer(customerId.toString(), firstName, lastName,
                recieverRefNumber, receiverAccountNumber, receiverBankName, receiverCountryCode,
            baseAmount, totalAmount, payoutCurrency, promoCode, accessKey, otp, fxQuoteNo, deliveryMode,
            object : ApiCallbackImpl<FundTransferResponse>(_funTransferResponse) {
            override fun onSuccess(data: FundTransferResponse) {
                _funTransferResponse.value = Event(NetworkState2.Success(data))
            }

            override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                _funTransferResponse.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
            }
        })
    }

    fun getTransDetailsAndRateCon(
            token: String,
            sessionId: String,
            refreshToken: String,
            customerId: String,
            transId: String,
            partnerTxnRefNo: String,
            beneficiaryRefNumber: String,
            payInAmount: String,
            payOutCurr: String,
            receiverCountryCode: String,
            payInCurrency: String,
            accessKey: String,
            trackingId: String?,
            purposeCode: String,
            deliveryMode: String
    ) {
        _transDetailsState.value = Event(NetworkState2.Loading())

        TransactionService.getInstance(token, sessionId, refreshToken)
                .getTransDetailsAndCon(customerId, transId, partnerTxnRefNo, beneficiaryRefNumber, payInAmount, "", payOutCurr, receiverCountryCode, payInCurrency,
                        accessKey, trackingId, purposeCode, deliveryMode,
                        object : ApiCallbackImpl<CombinedTransDetailsAndRate>(_transDetailsState) {
                            override fun onSuccess(data: CombinedTransDetailsAndRate) {
                                rateResponse = data.rateConResponse.data
                                _transDetailsState.value = Event(NetworkState2.Success(data))
                            }

                            override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                                _transDetailsState.value = Event((NetworkState2.Error(message, code.toString(), isSessionExpired)))
                            }
                        })
    }


    fun validateTransaction2(
            token: String,
            sessionId: String,
            refreshToken: String,
            customerId: String,
            beneficiaryRefNumber: String,
            payInAmount: String,
            payOutCurr: String,
            receiverCountryCode: String,
            payInCurrency: String,
            accessKey: String,
            trackingId: String?,
            purposeCode: String,
            deliveryMode: String
    ) {
        _validateTransaction2.value = Event(NetworkState2.Loading())

        RateCalculatorService.getInstance(token, sessionId, refreshToken)
                .validateConversion(customerId, beneficiaryRefNumber, payInAmount, "", payOutCurr,
                        receiverCountryCode, payInCurrency, accessKey, trackingId, purposeCode, deliveryMode, object : ApiCallbackImpl<RateConversion>(_validateTransaction2) {
                    override fun onSuccess(data: RateConversion) {
                        rateResponse = data
                        _validateTransaction2.value = Event(NetworkState2.Success(data))
                    }

                    override fun onSuccess(data: RateConversion, message: String?) {
                        rateResponse = data
                        _validateTransaction2.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _validateTransaction2.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })

    }


    fun cancelRequest() {
        disposable.clear()
    }

    fun purposeOfPayment(
            token: String,
            sessionId: String,
            refreshToken: String,
            customerId: Long,
            accessKey: String,
            beneficiaryRefNumber: String
    ) {
        _purposeOfPaymentState.value = Event(NetworkState2.Loading())

        IntlRemittanceService.getInstance(token, sessionId, refreshToken)
                .purposeOfPayment(customerId, accessKey, beneficiaryRefNumber, object : ApiCallbackImpl<InitializeTransaction>(_purposeOfPaymentState) {
                    override fun onSuccess(data: InitializeTransaction) {
                        initializeTransaction = data
                        _purposeOfPaymentState.value = Event(NetworkState2.Success(data))
                    }
                })
    }

}