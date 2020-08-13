package com.fh.payday.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.fh.payday.datasource.models.intlRemittance.IntlTransaction
import com.fh.payday.datasource.models.transactionhistory.TransactionDate
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.intlRemittance.TransactionService
import com.fh.payday.utilities.DateTime
import com.fh.payday.utilities.Event
import com.fh.payday.views2.intlRemittance.DeliveryModes

class TrackTransactionViewModel : ViewModel() {

    private val _transactionResponse = MutableLiveData<Event<NetworkState2<List<IntlTransaction>>>>()
    val transactionResponse: LiveData<Event<NetworkState2<List<IntlTransaction>>>>
        get() { return _transactionResponse }

    var fromDate = MutableLiveData<String>()
    var toDate = MutableLiveData<String>()
    var deliveryMode = DeliveryModes.BTALTERNATE

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

    fun getTransactions(token: String, sessionId: String, refreshToken: String, customerId: String,
                        fromDate: String, toDate: String) {
        _transactionResponse.value = Event(NetworkState2.Loading())

        TransactionService.getInstance(token, sessionId, refreshToken)
                .getTransactions(customerId, fromDate, toDate,
                        object : ApiCallbackImpl<List<IntlTransaction>>(_transactionResponse) {
                            override fun onSuccess(data: List<IntlTransaction>) {
                                _transactionResponse.value = Event(NetworkState2.Success(data))
                            }
                        })
    }
}