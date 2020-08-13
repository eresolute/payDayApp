package com.fh.payday.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.fh.payday.datasource.models.AmountLimit
import com.fh.payday.datasource.models.Card
import com.fh.payday.datasource.models.CustomerSummary
import com.fh.payday.datasource.models.TransactionHistory
import com.fh.payday.datasource.models.login.User
import com.fh.payday.datasource.models.moneytransfer.*
import com.fh.payday.datasource.models.transactionhistory.BillTransactions
import com.fh.payday.datasource.models.transactionhistory.TransactionDate
import com.fh.payday.datasource.remote.ApiCallback
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.beneficiaries.BeneficiaryService
import com.fh.payday.datasource.remote.customer.CustomerService
import com.fh.payday.datasource.remote.moneytransfer.TransferService
import com.fh.payday.datasource.remote.transactionhistory.TransactionHistoryService
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.DateTime
import com.fh.payday.utilities.Event
import com.fh.payday.utilities.isNotEmptyList
import com.fh.payday.views2.moneytransfer.MoneyTransferType

class TransferViewModel : ViewModel() {
    var cards = ArrayList<Card>()
        private set

    var selectedP2PBeneficiary: Beneficiary? = null
    var selectedLocalBeneficiary: LocalBeneficiary? = null
    var selectedP2CBeneficiary: P2CBeneficiary? = null


    private val _customerSummaryState = MutableLiveData<Event<NetworkState2<CustomerSummary>>>()
    val customerSummaryState: LiveData<Event<NetworkState2<CustomerSummary>>>
        get() { return _customerSummaryState }

    private val _beneficiaryState = MutableLiveData<Event<NetworkState2<List<Beneficiary>>>>()
    val beneficiaryState: LiveData<Event<NetworkState2<List<Beneficiary>>>>
        get() { return _beneficiaryState }

    private val _addPayday2PaydayBeneficiaryState = MutableLiveData<Event<NetworkState2<AddP2PBeneficiaryResponse>>>()
    val addPayday2PaydayBeneficiaryState: LiveData<Event<NetworkState2<AddP2PBeneficiaryResponse>>>
        get() = _addPayday2PaydayBeneficiaryState

    private val _localBeneficiaryState = MutableLiveData<Event<NetworkState2<List<LocalBeneficiary>>>>()
    val localBeneficiaryState: LiveData<Event<NetworkState2<List<LocalBeneficiary>>>>
        get() { return _localBeneficiaryState }

    private val _otpResponse = MutableLiveData<Event<NetworkState2<String>>>()
    val optResponseState : LiveData<Event<NetworkState2<String>>>
        get() = _otpResponse

    private val _p2pResponse = MutableLiveData<Event<NetworkState2<P2PTransferResponse>>>()
    val p2pResponseState : LiveData<Event<NetworkState2<P2PTransferResponse>>>
        get() = _p2pResponse

    private val _localTransferResponse = MutableLiveData<Event<NetworkState2<LocalTransferResponse>>>()
    val localTransferResponse : LiveData<Event<NetworkState2<LocalTransferResponse>>>
        get() = _localTransferResponse

    private val _ccTransferResponse = MutableLiveData<Event<NetworkState2<LocalTransferResponse>>>()
    val ccTransferResponse : LiveData<Event<NetworkState2<LocalTransferResponse>>>
        get() = _ccTransferResponse

    private val _p2cBeneficiaryState = MutableLiveData<Event<NetworkState2<List<P2CBeneficiary>>>>()
    val p2cBeneficiaryState : LiveData<Event<NetworkState2<List<P2CBeneficiary>>>>
        get() = _p2cBeneficiaryState

    private val _amountLimitResponse = MutableLiveData<Event<NetworkState2<AmountLimit>>>()
    val amountLimitResponse : LiveData<Event<NetworkState2<AmountLimit>>>
        get() = _amountLimitResponse

    private val _transactionHistoryState = MutableLiveData<Event<NetworkState2<TransactionHistory>>>()
    val transactionHistoryState: LiveData<Event<NetworkState2<TransactionHistory>>>
        get() { return _transactionHistoryState }

    var transactionDate = MutableLiveData<TransactionDate>()
        private set

    var selectedCard: Card? = null
    var amount: String? = null
    var amountLeft: String? = null
    var purpose: String? = null
    var transferType: MoneyTransferType? = null
    var amountLimit: AmountLimit? = null
    var user: User? = null
    var isChecked: Boolean? = false
    var otp: String? = null
    var cardBalance : String? = null
    var dataClear: Boolean = false

    init {
        transactionDate. value = TransactionDate(DateTime.firstDayOfCurrentMonth("yyyy-MM-dd"),
            DateTime.currentDate("yyyy-MM-dd"))
    }

    fun setDate(fromDate: String, toDate: String) {
        this.transactionDate.value = TransactionDate(fromDate, toDate)
    }

    fun fetchCustomerSummary(token: String, sessionId: String, refreshToken: String, customerId: Long) {
        _customerSummaryState.value = Event(NetworkState2.Loading())

        CustomerService.getInstance(token, sessionId, refreshToken).getSummary(customerId,
            object : ApiCallbackImpl<CustomerSummary>(_customerSummaryState) {
                override fun onSuccess(data: CustomerSummary) {
                    if (isNotEmptyList(data.cards)) {
                        cards.clear()
                        cards.addAll(data.cards)
                        selectedCard = cards[0]
                    }
                    _customerSummaryState.value = Event(NetworkState2.Success(data))
                }
            })
    }

    fun getP2PBeneficiaries(token: String, sessionId: String, refreshToken: String, customerId: Long) {
        _beneficiaryState.value = Event(NetworkState2.Loading())

        TransferService.getInstance(token, sessionId, refreshToken).getBeneficiary(customerId, object : ApiCallbackImpl<List<Beneficiary>>(_beneficiaryState) {
            override fun onSuccess(data: List<Beneficiary>) {
                _beneficiaryState.value = Event(NetworkState2.Success(data))
            }
        })
    }

    fun addP2PBeneficiary(token: String, sessionId: String, refreshToken: String, id: Long, mobileNumber: String, otp: String) {
        _addPayday2PaydayBeneficiaryState.value = Event(NetworkState2.Loading())

        BeneficiaryService.getInstance(token, sessionId, refreshToken).addP2PBeneficiaryService(id, mobileNumber, otp,
                object : ApiCallbackImpl<AddP2PBeneficiaryResponse>(_addPayday2PaydayBeneficiaryState) {
                    override fun onSuccess(data: AddP2PBeneficiaryResponse) {
                        _addPayday2PaydayBeneficiaryState.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _addPayday2PaydayBeneficiaryState.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })
    }

    fun getLocalBeneficiaries(token: String, sessionId: String, refreshToken: String, customerId: Long) {
        _localBeneficiaryState.value = Event(NetworkState2.Loading())

        TransferService.getInstance(token, sessionId, refreshToken).getLocalBeneficiary(customerId,
            object : ApiCallbackImpl<List<LocalBeneficiary>>(_localBeneficiaryState) {
                override fun onSuccess(data: List<LocalBeneficiary>) {
                    _localBeneficiaryState.value = Event(NetworkState2.Success(data))
                }

                override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                    _localBeneficiaryState.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                }
            })
    }


    fun getP2CBeneficiaries(token: String, sessionId: String, refreshToken: String, customerId: Long) {
        _p2cBeneficiaryState.value = Event(NetworkState2.Loading())

        TransferService.getInstance(token, sessionId, refreshToken).getP2CBeneficiary(customerId,
                object : ApiCallbackImpl<List<P2CBeneficiary>> (_p2cBeneficiaryState) {
                    override fun onSuccess(data: List<P2CBeneficiary>) {
                        _p2cBeneficiaryState.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _p2cBeneficiaryState.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })
    }

    fun getOtp(token: String, sessionId: String, refreshToken: String, customerId: Int) {
        _otpResponse.value = Event(NetworkState2.Loading())

        TransferService.getInstance(token, sessionId, refreshToken).getOtp(customerId, object : ApiCallbackImpl<String>(_otpResponse) {
            override fun onSuccess(data: String) {
                _otpResponse.value = Event(NetworkState2.Success(data))
            }

            override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                _otpResponse.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
            }
        })
    }

    fun sendP2P(token: String, sessionId: String, refreshToken: String, customerId: Long,  mobileNumber: String, amount: String, otp: String) {
        _p2pResponse.value = Event(NetworkState2.Loading())

        TransferService.getInstance(token, sessionId, refreshToken).p2pTransfer(customerId, mobileNumber, amount, otp,
                object : ApiCallbackImpl<P2PTransferResponse>(_p2pResponse) {
            override fun onSuccess(data: P2PTransferResponse) {
                _p2pResponse.value = Event(NetworkState2.Success(data))
            }

            override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                _p2pResponse.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
            }
        })
    }

    fun sendLocalMoney(token: String, sessionId: String, refreshToken: String, customerId: Long, accountNo: String,
                       beneficiaryName: String, amount: String, bank: String, otp: String) {
        _localTransferResponse.value = Event(NetworkState2.Loading())

        TransferService.getInstance(token, sessionId, refreshToken).localTransfer(customerId, accountNo,  beneficiaryName, amount, bank, otp,
            object : ApiCallbackImpl<LocalTransferResponse>(_localTransferResponse) {
                override fun onSuccess(data: LocalTransferResponse) {
                    _localTransferResponse.value = Event(NetworkState2.Success(data))
                }

                override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                    _localTransferResponse.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                }
            })
    }

    fun getAmountLimit(token: String, sessionId: String, refreshToken: String, customerId: Long, accessKey: String) {
        _amountLimitResponse.value = Event(NetworkState2.Loading())

        TransferService.getInstance(token, sessionId, refreshToken).getAmountLimit(customerId, accessKey,
            object : ApiCallbackImpl<AmountLimit>(_amountLimitResponse) {
                override fun onSuccess(data: AmountLimit) {
                    _amountLimitResponse.value = Event(NetworkState2.Success(data))
                }

                override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                    _amountLimitResponse.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                }
            })
    }

    fun fetchTransactions(token: String, sessionId: String, refreshToken: String,
                          customerId: Long, fromDate: String, toDate: String) {
        _transactionHistoryState.value = Event(NetworkState2.Loading())

        TransactionHistoryService.getInstance(token, sessionId, refreshToken)
            .getPayments(customerId, fromDate, toDate, "moneytransfer",
                object : ApiCallbackImpl<TransactionHistory> (_transactionHistoryState) {
                    override fun onSuccess(data: TransactionHistory) {
                        _transactionHistoryState.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _transactionHistoryState.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })
    }

    fun sendCCMoney(token: String, sessionId: String, refreshToken: String, customerId: Int,
                    creditCardNo: String, shortName: String, amount: String, bankName: String, otp: String) {
        _ccTransferResponse.value = Event(NetworkState2.Loading())

        TransferService.getInstance(token, sessionId, refreshToken).ccTransfer(customerId, creditCardNo,
                shortName, amount, bankName, otp, object : ApiCallbackImpl<LocalTransferResponse>(_ccTransferResponse) {
            override fun onSuccess(data: LocalTransferResponse) {
                _ccTransferResponse.value = Event(NetworkState2.Success(data))
            }

            override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                _ccTransferResponse.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
            }
        })

    }

    companion object {
        const val P2P = "p2p"
        const val P2IBAN = "p2iban"
        const val P2INTL = "p2intl"
        const val P2CC = "p2cc"
    }

}