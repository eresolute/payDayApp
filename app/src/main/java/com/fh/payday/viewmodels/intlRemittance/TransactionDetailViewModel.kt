package com.fh.payday.viewmodels.intlRemittance

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.fh.payday.datasource.models.intlRemittance.IntlTransaction
import com.fh.payday.datasource.models.intlRemittance.TransactionDetail
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.intlRemittance.TransactionService
import com.fh.payday.utilities.Event

class TransactionDetailViewModel : ViewModel() {
    private val _transactionDetailResponse = MutableLiveData<Event<NetworkState2<TransactionDetail>>>()
    val transactionDetailResponse: LiveData<Event<NetworkState2<TransactionDetail>>>
        get() {
            return _transactionDetailResponse
        }

    fun getTransactionDetail(token: String, sessionId: String, refreshToken: String, customerId: String,
                        accessKey:String,transactionId: String, partnerTxnRefNo: String) {
        _transactionDetailResponse.value = Event(NetworkState2.Loading())

        TransactionService.getInstance(token, sessionId, refreshToken)
                .getTransactionDetail( customerId,accessKey, transactionId, partnerTxnRefNo,
                        object : ApiCallbackImpl<TransactionDetail>(_transactionDetailResponse) {
                            override fun onSuccess(data: TransactionDetail) {
                                _transactionDetailResponse.value = Event(NetworkState2.Success(data))
                            }
                        })
    }
}