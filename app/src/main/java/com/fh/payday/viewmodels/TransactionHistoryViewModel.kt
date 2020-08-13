package com.fh.payday.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.fh.payday.datasource.models.TransactionHistory
import com.fh.payday.datasource.models.transactionhistory.*
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.cardservices.CardService
import com.fh.payday.datasource.remote.transactionhistory.TransactionHistoryService
import com.fh.payday.utilities.DateTime
import com.fh.payday.utilities.Event

class TransactionHistoryViewModel : ViewModel() {

    private val _loanTransaction = MutableLiveData<Event<NetworkState2<List<LoanTransaction>>>>()
    private val _billTransaction = MutableLiveData<Event<NetworkState2<TransactionHistory>>>()
    private val _moneyTransfer = MutableLiveData<Event<NetworkState2<TransactionHistory>>>()
    private val _cardTransactions = MutableLiveData<Event<NetworkState2<TransactionHistory>>>()
    private val _salariesState = MutableLiveData<Event<NetworkState2<CardTransactions>>>()

    val loanTransactionState : LiveData<Event<NetworkState2<List<LoanTransaction>>>> get() = _loanTransaction
    val billTransactionState: LiveData<Event<NetworkState2<TransactionHistory>>> get() = _billTransaction
    val moneyTransferState: LiveData<Event<NetworkState2<TransactionHistory>>> get() = _moneyTransfer
    val cardTransactions: LiveData<Event<NetworkState2<TransactionHistory>>> get() = _cardTransactions
    val salariesState: LiveData<Event<NetworkState2<CardTransactions>>> get() = _salariesState

    var loanNumber : String? = null
    var billTransaction: TransactionHistory? = null
    var moneyTransfer: TransactionHistory? = null
    var loanTransaction: List<LoanTransaction> = ArrayList()
    var cardTransaction: TransactionHistory? = null
    var salariesCredited: List<Transactions>? = null

    val transactionDate = MutableLiveData<TransactionDate>()

    init {
        transactionDate.value = TransactionDate(DateTime.firstDayOfCurrentMonth("yyyy-MM-dd"),
            DateTime.currentDate("yyyy-MM-dd"))
    }

    fun setDate(fromDate: String, toDate: String) {
        val transDate = this.transactionDate.value?.apply {
            this.fromDate = fromDate
            this.toDate = toDate
        }

        this.transactionDate.value = transDate
    }

    fun getLoanTransaction(token: String, sessionId: String,refreshToken: String, customerId: Long) {
        _loanTransaction.value = Event(NetworkState2.Loading())

        TransactionHistoryService.getInstance(token, sessionId, refreshToken).getLoanTransaction(customerId,
            object : ApiCallbackImpl<List<LoanTransaction>> (_loanTransaction) {
                override fun onSuccess(data: List<LoanTransaction>) {
                    _loanTransaction.value = Event(NetworkState2.Success(data))
                    loanTransaction = data
                }

                override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                    _loanTransaction.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                }
            })
    }

    fun getPayments(token: String, sessionId: String,refreshToken: String, customerId: Long, fromDate: String, toDate: String, serviceType: String) {
        _billTransaction.value = Event(NetworkState2.Loading())

        TransactionHistoryService.getInstance(token, sessionId, refreshToken).getPayments(customerId, fromDate, toDate, serviceType,
            object : ApiCallbackImpl<TransactionHistory> (_billTransaction) {
                override fun onSuccess(data: TransactionHistory) {
                    _billTransaction.value = Event(NetworkState2.Success(data))
                    billTransaction = data
                }

                override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                    _billTransaction.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                }
            })
    }

    fun getMoneyTransfer(token: String, sessionId: String,refreshToken: String, customerId: Long, fromDate: String,
                         toDate: String, serviceType: String) {
        _moneyTransfer.value = Event(NetworkState2.Loading())

        TransactionHistoryService.getInstance(token, sessionId, refreshToken).getPayments(customerId, fromDate, toDate, serviceType,
            object : ApiCallbackImpl<TransactionHistory> (_moneyTransfer) {
                override fun onSuccess(data: TransactionHistory) {
                    _moneyTransfer.value = Event(NetworkState2.Success(data))
                    moneyTransfer = data
                }

                override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                    _moneyTransfer.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                }
            })
    }

    @JvmOverloads
    fun getCardTransactions(
        token: String,
        sessionId: String,
        refreshToken: String,
        customerId: Long,
        fromDate: String = DateTime.firstDayOfCurrentMonth("yyyy-MM-dd"),
        toDate: String = DateTime.lastDayOfCurrentMonth("yyyy-MM-dd")
    ) {
        _cardTransactions.value = Event(NetworkState2.Loading())

        TransactionHistoryService.getInstance(token, sessionId, refreshToken).getCardTransactions(customerId, fromDate, toDate,
            object : ApiCallbackImpl<TransactionHistory> (_cardTransactions) {
                override fun onSuccess(data: TransactionHistory) {
                    _cardTransactions.value = Event(NetworkState2.Success(data))
                    cardTransaction = data
                }

                override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                    _cardTransactions.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                }
            })
    }

    fun fetchSalariesCredited(token: String, sessionId: String, refreshToken: String, customerId: Long) {
        _salariesState.value = Event(NetworkState2.Loading())

        CardService.getInstance(token, sessionId, refreshToken).getSalariesCredited(customerId,
            object : ApiCallbackImpl<CardTransactions>(_salariesState) {
                override fun onSuccess(data: CardTransactions) {
                    salariesCredited = data.Transactions
                    _salariesState.value = Event(NetworkState2.Success(data))
                }

                override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                    _salariesState.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                }
            })
    }
}