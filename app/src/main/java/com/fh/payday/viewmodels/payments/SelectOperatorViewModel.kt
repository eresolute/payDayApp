package com.fh.payday.viewmodels.payments

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.fh.payday.datasource.models.login.User
import com.fh.payday.datasource.models.payments.Operator
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.payments.PaymentService
import com.fh.payday.utilities.Event

class SelectOperatorViewModel : ViewModel() {

    companion object {
        const val METHOD_OPERATORS = "operators"
    }

    var user: User? = null
    var operators: List<Operator>? = null

    private val _operatorState = MutableLiveData<Event<NetworkState2<List<Operator>>>>()
    val operatorState: LiveData<Event<NetworkState2<List<Operator>>>>
        get() {
            return _operatorState
        }

    fun getOperators(token: String, sessionId: String, refreshToken: String, customerId: Long, typeId: Int,
                     category: String, countryCode: String = "971") {
        _operatorState.value = Event(NetworkState2.Loading())

        PaymentService.getInstance(token, sessionId, refreshToken).getOperators(customerId,METHOD_OPERATORS, typeId, countryCode,
                object : ApiCallbackImpl<List<Operator>>(_operatorState) {
                    override fun onSuccess(data: List<Operator>) {
                        if (category == "NA") {
                            operators = data
                            _operatorState.value = Event(NetworkState2.Success(data))
                        } else {
                            operators = data.filter {
                                it.serviceCategory == category
                            }
                            _operatorState.value = Event(NetworkState2.Success(operators))
                        }
                    }
                })
    }

}