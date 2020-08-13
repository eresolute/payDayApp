package com.fh.payday.viewmodels.intlRemittance

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.fh.payday.datasource.models.Card
import com.fh.payday.datasource.models.intlRemittance.IntlTransaction
import com.fh.payday.datasource.models.intlRemittance.TransDetailsPDFFile
import com.fh.payday.datasource.models.intlRemittance.TransactionDetail
import com.fh.payday.datasource.models.transactionhistory.TransactionDate
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.intlRemittance.TransactionService
import com.fh.payday.utilities.DateTime
import com.fh.payday.utilities.Event

class TransactionHistoryViewModel : ViewModel() {

    var card: Card? = null
    private val _transactionResponse = MutableLiveData<Event<NetworkState2<List<IntlTransaction>>>>()
    val transactionResponse: LiveData<Event<NetworkState2<List<IntlTransaction>>>>
        get() {
            return _transactionResponse
        }

    private val _addFavouriteResponse = MutableLiveData<Event<NetworkState2<String>>>()
    val addFavouriteResponse: LiveData<Event<NetworkState2<String>>>
        get() = _addFavouriteResponse

    private val _transactionDetail = MutableLiveData<Event<NetworkState2<TransactionDetail>>>()
    val transactionDetail: LiveData<Event<NetworkState2<TransactionDetail>>>
        get() = _transactionDetail

    private val _trackSharedTransaction = MutableLiveData<Event<NetworkState2<String>>>()
    val trackSharedTransaction : LiveData<Event<NetworkState2<String>>>
        get() = _trackSharedTransaction

    private val _transDetailsPDFResponse = MutableLiveData<Event<NetworkState2<TransDetailsPDFFile>>>()
    val transDetailsPDF: LiveData<Event<NetworkState2<TransDetailsPDFFile>>>
        get() = _transDetailsPDFResponse

    var filePath: String? = null

    var fromDate = MutableLiveData<String>()
    var toDate = MutableLiveData<String>()

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

    fun addFavourite(token: String, sessionId: String, refreshToken: String, customerId: String,
                     transId: String, isFavorite: Int) {
        _addFavouriteResponse.value = Event(NetworkState2.Loading())

        TransactionService.getInstance(token, sessionId, refreshToken).addFavourite(customerId, transId, isFavorite,
                object : ApiCallbackImpl<String>(_addFavouriteResponse) {
                    override fun onSuccess(data: String) {
                        _addFavouriteResponse.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _addFavouriteResponse.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })
    }

    fun getTransactionDetail(token: String, sessionId: String, refreshToken: String, customerId: String,
                             accessKey: String,dealerTxnId: String, partnerTxnRefID: String) {

        _transactionDetail.value = Event(NetworkState2.Loading())

        TransactionService.getInstance(token, sessionId, refreshToken)
                .getTransactionDetail(customerId,accessKey, dealerTxnId, partnerTxnRefID,
                        object : ApiCallbackImpl<TransactionDetail>(_transactionDetail) {
                            override fun onSuccess(data: TransactionDetail) {
                                _transactionDetail.value = Event(NetworkState2.Success(data))
                            }
                        })
    }

    fun trackSharedTransaction(token: String, sessionId: String, refreshToken: String, customerId: String, transId: String){

        _trackSharedTransaction.value = Event(NetworkState2.Loading())
        TransactionService.getInstance(token,sessionId,refreshToken).trackSharedTransaction(customerId, transId,
                object : ApiCallbackImpl<String>(_trackSharedTransaction){
                    override fun onSuccess(data: String) {
                        _trackSharedTransaction.value = Event(NetworkState2.Success(data))
                    }
                })
    }

    fun getTransactionDetailsPDF(token: String, sessionId: String, refreshToken: String, customerId: String, transactionId: String) {

        TransactionService.getInstance(token, sessionId, refreshToken).getTransactionDetailsPDF(customerId, transactionId,
                object : ApiCallbackImpl<TransDetailsPDFFile>(_transDetailsPDFResponse) {
                    override fun onSuccess(data: TransDetailsPDFFile) {
                        _transDetailsPDFResponse.value = Event(NetworkState2.Success(data))
                    }
                })
    }
}