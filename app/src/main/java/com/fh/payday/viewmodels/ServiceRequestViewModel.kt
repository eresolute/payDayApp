package com.fh.payday.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Base64
import com.fh.payday.datasource.models.CardBlockResponse
import com.fh.payday.datasource.models.CardTransactionDispute
import com.fh.payday.datasource.models.CustomerSummary
import com.fh.payday.datasource.models.login.User
import com.fh.payday.datasource.models.transactionhistory.TransactionDate
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.customer.CustomerService
import com.fh.payday.datasource.remote.servicerequest.ServiceRequest
import com.fh.payday.datasource.remote.transactionhistory.TransactionHistoryService
import com.fh.payday.utilities.*
import com.fh.payday.utilities.encryption.aes.encryptWithAES
import com.fh.payday.utilities.encryption.aes.generateSecretKey
import com.fh.payday.utilities.encryption.rsa.encryptWithRSA

class ServiceRequestViewModel : ViewModel() {

    var user: User? = null
    var summary: CustomerSummary? = null
    var cardStatus: Boolean = true

    private val _cardBlockRequest = MutableLiveData<Event<NetworkState2<CardBlockResponse>>>()
    private val _activateCardState = MutableLiveData<Event<NetworkState2<String>>>()
    private val _otpRequest = MutableLiveData<Event<NetworkState2<String>>>()
    private val _customerSummary = MutableLiveData<Event<NetworkState2<CustomerSummary>>>()
    private val _holidayRequest = MutableLiveData<Event<NetworkState2<String>>>()
    private val _deferLoanRequest = MutableLiveData<Event<NetworkState2<String>>>()
    private val _cardTransactions = MutableLiveData<Event<NetworkState2<List<CardTransactionDispute>>>>()
    private val _createDisputeState = MutableLiveData<Event<NetworkState2<String>>>()

    val cardBlockState: LiveData<Event<NetworkState2<CardBlockResponse>>> get() = _cardBlockRequest
    val activateCardState: LiveData<Event<NetworkState2<String>>> get() = _activateCardState
    val otpRequestState: LiveData<Event<NetworkState2<String>>> get() = _otpRequest
    val customerSummaryState: LiveData<Event<NetworkState2<CustomerSummary>>> get() = _customerSummary
    val holidayState: LiveData<Event<NetworkState2<String>>> get() = _holidayRequest
    val deferLoanState: LiveData<Event<NetworkState2<String>>> get() = _deferLoanRequest
    val cardTransactions: LiveData<Event<NetworkState2<List<CardTransactionDispute>>>> get() = _cardTransactions
    val createDisputeState: LiveData<Event<NetworkState2<String>>> get() = _createDisputeState


    var installment = MutableLiveData<String>()

    var atmPin: String? = null
    var otp: String? = null

    var transactionDate = MutableLiveData<TransactionDate>()

    init {
        transactionDate.value = TransactionDate(DateTime.firstDayOfCurrentMonth("yyyy-MM-dd"),
            DateTime.currentDate("yyyy-MM-dd"))
    }

    fun setDate(fromDate: String, toDate: String) {
        this.transactionDate.value = TransactionDate(fromDate, toDate)
    }

    fun fetchCustomerSummary(token: String, sessionId: String, refreshToken: String, customerId: Long) {
        _customerSummary.value = Event(NetworkState2.Loading())
        CustomerService.getInstance(token, sessionId, refreshToken)
            .getSummary(customerId, object : ApiCallbackImpl<CustomerSummary>(_customerSummary) {
                override fun onSuccess(data: CustomerSummary) {
                    //shouldBeRemoved(data)
                    summary = data
                    if (isNotEmptyList(data.cards)) {
                        cardStatus = CARD_ACTIVE.equals(data.cards[0].cardStatus, true)
                    }
                    _customerSummary.value = Event(NetworkState2.Success(data))
                }

                override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                    _customerSummary.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                }
            })
    }

    /*fun shouldBeRemoved(data: CustomerSummary) {
        data.cards[0].cardStatus = "Inactive"
    }*/

    fun blockCard(token: String, sessionId: String, refreshToken: String, byteArray: ByteArray, otp: String) {
        if (isEmptyList(summary?.cards)) return

        _cardBlockRequest.value = Event(NetworkState2.Loading())

        val secret = Base64.encodeToString(generateSecretKey().encoded, Base64.DEFAULT).substring(0, 32)
        val encryptedPin = encryptWithAES(secret, atmPin ?: return)
        val encryptedSecret = encryptWithRSA(byteArray, secret)


        ServiceRequest.getInstance(token, sessionId, refreshToken).blockCard(user?.customerId?.toLong() ?: return,
            encryptedPin, encryptedSecret, otp, object : ApiCallbackImpl<CardBlockResponse>(_cardBlockRequest) {
                override fun onSuccess(data: CardBlockResponse) {
                    _cardBlockRequest.value = Event(NetworkState2.Success(data))
                }

                override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                    _cardBlockRequest.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                }
            })
    }

    fun activateCard(token: String, sessionId: String, refreshToken: String,
                     customerId: Long, otp: String, byteArray: ByteArray) {
        if (isEmptyList(summary?.cards)) return

        _activateCardState.value = Event(NetworkState2.Loading())

        val cardNumber = summary?.cards?.get(0)?.cardNumber ?: return
        val secret = Base64.encodeToString(generateSecretKey().encoded, Base64.DEFAULT).substring(0, 32)
        val encryptedPin = encryptWithAES(secret, atmPin ?: return)
        val encryptedSecret = encryptWithRSA(byteArray, secret)

        ServiceRequest.getInstance(token, sessionId, refreshToken).
            activateCard(customerId, cardNumber, otp, encryptedPin, encryptedSecret,
                object : ApiCallbackImpl<String>(_activateCardState) {
                    override fun onSuccess(data: String) {
                        _activateCardState.value = Event(NetworkState2.Success(data))
                    }
                })
    }

    fun paymentHoliday(token: String, sessionId: String, refreshToken: String, customerId: Long, otp: String, mgNumber: String, termIncrease: Int) {
        _holidayRequest.value = Event(NetworkState2.Loading())

        /*val reqBody = RequestBody.create(MediaType.parse("image/jpeg"), ticketImage)
        val multiPart = MultipartBody.Part.createFormData("ticket", ticketImage.name, reqBody)*/

        ServiceRequest.getInstance(token, sessionId, refreshToken).paymentHoliday(customerId, otp, mgNumber, termIncrease,
            object : ApiCallbackImpl<String>(_holidayRequest) {
                override fun onSuccess(data: String) {
                    _holidayRequest.value = Event(NetworkState2.Success(data))
                }

                override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                    _holidayRequest.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                }
            })
    }

    fun generateOtp(token: String, sessionId: String, refreshToken: String, customerId: Long) {
        _otpRequest.value = Event(NetworkState2.Loading())

        ServiceRequest.getInstance(token, sessionId, refreshToken).generateOtp(customerId,
            object : ApiCallbackImpl<String>(_otpRequest) {
                override fun onSuccess(data: String) {
                    _otpRequest.value = Event(NetworkState2.Success(data))
                }

                override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                    _otpRequest.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                }
            })
    }

    fun deferLoan(token: String, sessionId: String, refreshToken: String, customerId: Long) {
        _deferLoanRequest.value = Event(NetworkState2.Loading())

        ServiceRequest.getInstance(token, sessionId, refreshToken).deferLoan(customerId,
            object : ApiCallbackImpl<String>(_deferLoanRequest) {
                override fun onSuccess(data: String) {
                    _deferLoanRequest.value = Event(NetworkState2.Success(data))
                }
            })
    }

    fun getCardTransactions(
        token: String,
        sessionId: String,
        customerId: Long,
        refreshToken: String,
        fromDate: String,
        toDate: String
    ) {
        _cardTransactions.value = Event(NetworkState2.Loading())

        TransactionHistoryService.getInstance(token, sessionId, refreshToken)
            .getCardTransactionDisputes(customerId, fromDate, toDate,
                object : ApiCallbackImpl<List<CardTransactionDispute>>(_cardTransactions) {
                    override fun onSuccess(data: List<CardTransactionDispute>) {
                        _cardTransactions.value = Event(NetworkState2.Success(data))
                    }
                })
    }

    fun createDispute(
        token: String,
        sessionId: String,
        refreshToken: String,
        customerId: Long,
        transAmount: String,
        transDate: String,
        merchantName: String,
        transNumber: String
    ) {
        _createDisputeState.value = Event(NetworkState2.Loading())

        ServiceRequest.getInstance(token, sessionId, refreshToken)
            .createDispute(customerId, transAmount, transDate, merchantName, transNumber,
                object : ApiCallbackImpl<String>(_createDisputeState) {
                    override fun onSuccess(data: String) {
                        _createDisputeState.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _createDisputeState.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })
    }
}