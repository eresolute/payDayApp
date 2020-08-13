package com.fh.payday.viewmodels.cardservices

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.fh.payday.datasource.models.MonthlyStatement
import com.fh.payday.datasource.models.StatementHistory
import com.fh.payday.datasource.models.transactionhistory.CardTransactions
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.cardservices.CardService
import com.fh.payday.utilities.Event

class TransactionViewModel : ViewModel() {

    private val _transactionState = MutableLiveData<Event<NetworkState2<CardTransactions>>>()
    val transactionState : LiveData<Event<NetworkState2<CardTransactions>>>
        get() = _transactionState

    private val _salariesState = MutableLiveData<Event<NetworkState2<CardTransactions>>>()
    val salariesNetworkState: LiveData<Event<NetworkState2<CardTransactions>>>
        get() { return _salariesState }

    private val _statementState = MutableLiveData<Event<NetworkState2<List<StatementHistory>>>>()
    val statementState: LiveData<Event<NetworkState2<List<StatementHistory>>>>
        get() {return _statementState}

    private val _monthlyStatementState = MutableLiveData<Event<NetworkState2<List<MonthlyStatement>>>>()
    val monthlyStatementState: LiveData<Event<NetworkState2<List<MonthlyStatement>>>>
        get() {return _monthlyStatementState}

    fun getTransactionDetail(token: String, sessionId: String, refreshToken: String, customerId: Long) {
        _transactionState.value = Event(NetworkState2.Loading())

        CardService.getInstance(token, sessionId, refreshToken).getTransactionDetails(customerId,
            object: ApiCallbackImpl<CardTransactions>(_transactionState) {
                override fun onSuccess(data: CardTransactions) {
                    _transactionState.value = Event(NetworkState2.Success(data))
                }
            })
    }

    fun getSalariesCredited(token: String, sessionId: String, refreshToken: String, customerId: Long) {
        _salariesState.value = Event(NetworkState2.Loading())

        CardService.getInstance(token, sessionId, refreshToken).getSalariesCredited(customerId,
            object : ApiCallbackImpl<CardTransactions>(_salariesState) {
                override fun onSuccess(data: CardTransactions) {
                    _salariesState.value = Event(NetworkState2.Success(data))
                }
            })
    }

    fun getStatementHistory(token: String, sessionId: String, refreshToken: String, customerId: Long, type: String) {
        _statementState.value = Event(NetworkState2.Loading())

        CardService.getInstance(token, sessionId, refreshToken).getStatementHistory(customerId, type,
            object : ApiCallbackImpl<List<StatementHistory>>(_statementState) {
                override fun onSuccess(data: List<StatementHistory>) {
                    _statementState.value = Event(NetworkState2.Success(data))
                }
            })
    }

    fun monthlyStatement(token: String, sessionId: String, refreshToken: String, customerId: Long) {
        _monthlyStatementState.value = Event(NetworkState2.Loading())

        CardService.getInstance(token, sessionId, refreshToken).monthlyStatement(customerId,
            object : ApiCallbackImpl<List<MonthlyStatement>>(_monthlyStatementState) {
                override fun onSuccess(data: List<MonthlyStatement>) {
                    _monthlyStatementState.value = Event(NetworkState2.Success(data))
                }
            })
    }
}