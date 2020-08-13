package com.fh.payday.viewmodels.payments.transport

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.fh.payday.datasource.models.payments.Beneficiaries
import com.fh.payday.datasource.models.payments.ServiceCategory
import com.fh.payday.datasource.models.payments.transport.*
import com.fh.payday.datasource.remote.ApiCallback
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.payments.PaymentService
import com.fh.payday.datasource.remote.payments.transport.TransportService
import com.fh.payday.utilities.Event

class TransportViewModel : ViewModel() {

    private val _operatorRequest = MutableLiveData<Event<NetworkState2<List<Operator>>>>()
    private val _operatorDetail = MutableLiveData<Event<NetworkState2<OperatorDetails>>>()
    private val _salikBalanceDetail = MutableLiveData<Event<NetworkState2<BalanceDetails>>>()
    private val _mawaqifBillDetail = MutableLiveData<Event<NetworkState2<MawaqifBillDetail>>>()
    private val _paymentResponse = MutableLiveData<Event<NetworkState2<BillPaymentResponse>>>()
    private val _otpResponse = MutableLiveData<Event<NetworkState2<String>>>()

    private val _beneficiariesState = MutableLiveData<Event<NetworkState2<ArrayList<Beneficiaries>>>>()
    val beneficiariesState: LiveData<Event<NetworkState2<ArrayList<Beneficiaries>>>>
        get() = _beneficiariesState

    private val _salikPinState = MutableLiveData<Boolean>()
    val salikPinState: LiveData<Boolean> get() = _salikPinState
    fun setSalikPINSelected() {
        _salikPinState.value = true
    }

    var operatorDetails = MutableLiveData<OperatorDetails>()

    var billDetails = MutableLiveData<BalanceDetails>()
    var mawaqifBillDetail = MutableLiveData<MawaqifBillDetail>()

    var salikTypeId: Int? = null
    var salikAccPin: String? = null

    var salikBalance = MutableLiveData<String>()

    var flexKey: String? = null
    var typeKey: Int? = null
    var accessKey: String? = null
    var operator: String? = null
    var amount = MutableLiveData<String>()
    var accountNumber: String? = null
    var selectedAccessKey: String? = null
    var dataClear: Boolean = false

    val operatorState: LiveData<Event<NetworkState2<List<Operator>>>>
        get() = _operatorRequest

    val operatorDetailState: LiveData<Event<NetworkState2<OperatorDetails>>>
        get() = _operatorDetail

    val balanceDetailState: LiveData<Event<NetworkState2<BalanceDetails>>>
        get() = _salikBalanceDetail

    val mawaqifBillState: LiveData<Event<NetworkState2<MawaqifBillDetail>>>
        get() = _mawaqifBillDetail

    val paymentState: LiveData<Event<NetworkState2<BillPaymentResponse>>>
        get() = _paymentResponse

    val optResponseState: LiveData<Event<NetworkState2<String>>>
        get() = _otpResponse

    fun operatorRequest(token: String, sessionId: String, refreshToken: String,
                        customerId: Long, typeId: Int, countryCode: String) {
        _operatorRequest.value = Event(NetworkState2.Loading())

        TransportService.getInstance(token, sessionId, refreshToken).getOperators(customerId, typeId, countryCode,
                object : ApiCallbackImpl<List<Operator>>(_operatorRequest) {
                    override fun onSuccess(data: List<Operator>) {
                        _operatorRequest.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _operatorRequest.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })
    }

    fun operatorDetails(token: String, sessionId: String, refreshToken: String,
                        customerId: Long, accessKey: String, typeId: Int, planType: String, countryCode: String) {
        _operatorDetail.value = Event(NetworkState2.Loading())

        TransportService.getInstance(token, sessionId, refreshToken)
                .getOperatorDetail(customerId, accessKey, typeId, planType, countryCode,
                        object : ApiCallbackImpl<OperatorDetails>(_operatorDetail) {
                            override fun onSuccess(data: OperatorDetails) {
                                flexKey = data.flexiKey
                                this@TransportViewModel.accessKey = accessKey
                                typeKey = data.typeKey.toInt()
                                operatorDetails.value = data

                                _operatorDetail.value = Event(NetworkState2.Success(data))
                            }

                            override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                                _operatorDetail.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                            }
                        })
    }

    fun billBalanceSalik(token: String, sessionId: String, refreshToken: String,
                         customerId: Long, accessKey: String, method: String, typeId: Int,
                         account: String, flexKey: String, typeKey: Int, optional: String) {

        _salikBalanceDetail.value = Event(NetworkState2.Loading())

        TransportService.getInstance(token, sessionId, refreshToken)
                .getBillBalance(customerId, accessKey, method, typeId, account, flexKey, typeKey, optional,
                        object : ApiCallbackImpl<BalanceDetails>(_salikBalanceDetail) {
                            override fun onSuccess(data: BalanceDetails) {
                                //MOBILE.value = account
                                billDetails.value = data
                                salikBalance.value = data.Balance
                                _salikBalanceDetail.value = Event(NetworkState2.Success(data))
                            }

                            override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                                _salikBalanceDetail.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                            }
                        })
    }

    fun mawaqifBill(token: String, sessionId: String, refreshToken: String,
                    customerId: Long, accessKey: String, method: String,
                    typeId: Int, account: String, flexKey: String, typeKey: Int) {

        _mawaqifBillDetail.value = Event(NetworkState2.Loading())

        TransportService.getInstance(token, sessionId, refreshToken)
                .getMawaqifBill(customerId, accessKey, method, typeId, account, flexKey, typeKey,
                        object : ApiCallbackImpl<MawaqifBillDetail>(_mawaqifBillDetail) {
                            override fun onSuccess(data: MawaqifBillDetail) {
                                //MOBILE.value = account
                                mawaqifBillDetail.value = data
                                _mawaqifBillDetail.value = Event(NetworkState2.Success(data))
                            }

                            override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                                _mawaqifBillDetail.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                            }
                        })
    }

    fun payBill(token: String, sessionId: String, refreshToken: String,
                accessKey: String, customerId: Int, typeKey: Int, flexKey: String,
                transactionId: String, account: String, amount: String, optional1: String,
                optional2: String, otp: String) {
        _paymentResponse.value = Event(NetworkState2.Loading())

        TransportService.getInstance(token, sessionId, refreshToken)
                .paySalikBill(accessKey, customerId, typeKey, flexKey, transactionId,
                        account, amount, optional1, optional2, otp,
                        object : ApiCallbackImpl<BillPaymentResponse>(_paymentResponse) {
                            override fun onSuccess(data: BillPaymentResponse) {
                                _paymentResponse.value = Event(NetworkState2.Success(data))
                            }
                        })
    }

    fun getOtp(token: String, sessionId: String, refreshToken: String, customerId: Int) {
        _otpResponse.value = Event(NetworkState2.Loading())

        TransportService.getInstance(token, sessionId, refreshToken).getOtp(customerId,
                object : ApiCallbackImpl<String>(_otpResponse) {
                    override fun onSuccess(data: String) {
                        _otpResponse.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _otpResponse.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })
    }

    /*fun getRecentAccounts(token: String, sessionId: String, refreshToken: String, customerId: Long, typeId: Int, operator: String) {
        TransportService.getInstance(token, sessionId, refreshToken).getOperators(customerId, typeId, "971",
                object : ApiCallback<List<Operator>> {
                    override fun onFailure(t: Throwable) {}

                    override fun onError(message: String) {}

                    override fun onSuccess(data: List<Operator>) {
                        if (data.isNullOrEmpty()) return

                        val accessKey = data.filter {
                            operator.equals(it.serviceProvider, true)
                        }.map(Operator::accessKey).fold("") { _, s -> s }

                        if (accessKey.isNotEmpty()) {
                            getRecentAccounts(token, sessionId, refreshToken, customerId, accessKey)
                        }
                    }
                })
    }

    private fun getRecentAccounts(token: String, sessionId: String, refreshToken: String, customerId: Long, accessKey: String) {
        PaymentService.getInstance(token, sessionId, refreshToken).getRecentAccounts(customerId, accessKey,
                object : ApiCallback<ArrayList<RecentAccount>> {
                    override fun onFailure(t: Throwable) {}

                    override fun onError(message: String) {}

                    override fun onSuccess(data: ArrayList<RecentAccount>) {
                        _recentAccountState.value = Event(NetworkState2.Success(data))
                    }
                })
    }*/

    fun getBeneficiaryAccounts(token: String, sessionId: String, refreshToken: String, customerId: Long, typeId: Int, operator: String) {

        TransportService.getInstance(token, sessionId, refreshToken).getOperators(customerId, typeId, "971",
                object : ApiCallback<List<Operator>> {
                    override fun onFailure(t: Throwable) {}

                    override fun onError(message: String) {}

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _beneficiariesState.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }

                    override fun onSuccess(data: List<Operator>) {
                        if (data.isNullOrEmpty()) return

                        val accessKey = data.filter {
                            operator.equals(it.serviceProvider, true)
                        }.map(Operator::accessKey).fold("") { _, s -> s }

                        if (accessKey.isNotEmpty()) {
                            selectedAccessKey = accessKey
                            getBeneficiaryAccounts(token, sessionId, refreshToken, customerId, accessKey)
                        }
                    }
                })
    }

    private fun getBeneficiaryAccounts(token: String, sessionId: String, refreshToken: String, customerId: Long, accessKey: String) {
        PaymentService.getInstance(token, sessionId, refreshToken).getBeneficiaries(customerId, accessKey,
                object : ApiCallback<ArrayList<Beneficiaries>> {
                    override fun onFailure(t: Throwable) {}

                    override fun onError(message: String) {}

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _beneficiariesState.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                    override fun onSuccess(data: ArrayList<Beneficiaries>) {
                        if ("mawaqif".equals(accessKey, true)) {

                            val filterData = data.filter {
                                ServiceCategory.BILL_PAYMENT.equals(it.type, true)
                            }
                            _beneficiariesState.value = Event(NetworkState2.Success(filterData as ArrayList<Beneficiaries>))

                        } else {

                            _beneficiariesState.value = Event(NetworkState2.Success(data))
                        }

                    }
                })
    }

}