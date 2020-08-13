package com.fh.payday.viewmodels.payments.utilities

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.fh.payday.datasource.models.payments.Beneficiaries
import com.fh.payday.datasource.models.payments.RecentAccount
import com.fh.payday.datasource.models.payments.utilities.*
import com.fh.payday.datasource.remote.ApiCallback
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.payments.PaymentService
import com.fh.payday.datasource.remote.payments.utilities.UtilitiesService
import com.fh.payday.utilities.Event

class UtilitiesViewModel : ViewModel() {

    private val _operatorRequest = MutableLiveData<Event<NetworkState2<List<Operator>>>>()
    private val _operatorDetail = MutableLiveData<Event<NetworkState2<OperatorDetails>>>()

    private val _billFewa = MutableLiveData<Event<NetworkState2<BillDetails>>>()
    private val _billAadc = MutableLiveData<Event<NetworkState2<AadcBillResponse>>>()

    private val _paymentResponse = MutableLiveData<Event<NetworkState2<BillPaymentResponse>>>()

    private val _otpResponse = MutableLiveData<Event<NetworkState2<String>>>()

    private val _recentAccountState = MutableLiveData<Event<NetworkState2<ArrayList<RecentAccount>>>>()
    val recentAccountState: LiveData<Event<NetworkState2<ArrayList<RecentAccount>>>>
        get() = _recentAccountState

    private val _beneficiariesState = MutableLiveData<Event<NetworkState2<ArrayList<Beneficiaries>>>>()
    val beneficiariesState: LiveData<Event<NetworkState2<ArrayList<Beneficiaries>>>>
        get() = _beneficiariesState

    //var SERVICE = MutableLiveData<String>()

    var operatorDetails = MutableLiveData<OperatorDetails>()

    var billDetails = MutableLiveData<BillDetails>()

    var aadcBillDetail = MutableLiveData<AadcBillResponse>()

    var dataClear: Boolean = false

    var aadcSelectedService: String?= null
    //var aadcSelectedLang: String?= null

    var flexKey: String? = null
    var typeKey: Int? = null
    var accessKey: String? = null
    var operator: String? = null
    var fewaBill: BillDetails? = null
    var amount = MutableLiveData<String>()
    var accountNumber: String? = null
    var selectedAccessKey: String? = null

    val operatorState: LiveData<Event<NetworkState2<List<Operator>>>>
        get() = _operatorRequest

    val operatorDetailState: LiveData<Event<NetworkState2<OperatorDetails>>>
        get() = _operatorDetail

    val fewaBillState: LiveData<Event<NetworkState2<BillDetails>>>
        get() = _billFewa

    val aadcBillState: LiveData<Event<NetworkState2<AadcBillResponse>>>
        get() = _billAadc

    val paymentState: LiveData<Event<NetworkState2<BillPaymentResponse>>>
        get() = _paymentResponse

    val optResponseState: LiveData<Event<NetworkState2<String>>>
        get() = _otpResponse

    fun operatorRequest(token: String, sessionId: String, refreshToken: String,
                        customerId: Long, typeId: Int, countryCode: String) {
        _operatorRequest.value = Event(NetworkState2.Loading())

        UtilitiesService.getInstance(token, sessionId, refreshToken)
                .getOperators(customerId, typeId, countryCode,
                        object : ApiCallbackImpl<List<Operator>>(_operatorRequest) {
                            override fun onSuccess(data: List<Operator>) {
                                _operatorRequest.value = Event(NetworkState2.Success(data))
                            }

                            override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                                _operatorRequest.value  = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                            }
                        })
    }

    fun operatorDetails(token: String, sessionId: String, refreshToken: String,
                        customerId: Long, accessKey: String, typeId: Int, planType: String, countryCode: String) {
        _operatorDetail.value = Event(NetworkState2.Loading())

        UtilitiesService.getInstance(token, sessionId, refreshToken)
                .getOperatorDetail(customerId, accessKey, typeId, planType, countryCode,
                        object : ApiCallbackImpl<OperatorDetails>(_operatorDetail) {
                            override fun onSuccess(data: OperatorDetails) {
                                flexKey = data.flexiKey
                                this@UtilitiesViewModel.accessKey = accessKey
                                typeKey = data.typeKey.toInt()
                                operatorDetails.value = data

                                _operatorDetail.value = Event(NetworkState2.Success(data))
                            }

                            override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                                _operatorDetail.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                            }
                        })
    }

    fun billBalanceFewa(token: String, sessionId: String, refreshToken: String,
                        customerId: Long, accessKey: String, method: String, typeId: Int,
                        account: String, flexKey: String, typeKey: Int) {
        _billFewa.value = Event(NetworkState2.Loading())

        UtilitiesService.getInstance(token, sessionId, refreshToken)
                .getBillBalance(customerId, accessKey, method, typeId, account, flexKey, typeKey,
                        object : ApiCallbackImpl<BillDetails>(_billFewa) {
                            override fun onSuccess(data: BillDetails) {
                                //MOBILE.value = account
                                billDetails.value = data
                                fewaBill = data
                                _billFewa.value = Event(NetworkState2.Success(data))
                            }

                            override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                                _billFewa.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                            }
                        })
    }

    fun billAadc(token: String, sessionId: String, refreshToken: String,
                 customerId: Long, accessKey: String, method: String, typeId: Int, account: String,
                 flexKey: String, typeKey: Int, optional1: String, optional2: String) {

        _billAadc.value = Event(NetworkState2.Loading())

        UtilitiesService.getInstance(token, sessionId, refreshToken)
                .getAadcBill(customerId, accessKey, method, typeId, account, flexKey, typeKey, optional1, optional2,
                        object : ApiCallbackImpl<AadcBillResponse>(_billAadc) {
                            override fun onSuccess(data: AadcBillResponse) {
                                //MOBILE.value = account
                                aadcBillDetail.value = data
                                _billAadc.value = Event(NetworkState2.Success(data))
                            }

                            override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                                _billAadc.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                            }
                        })
    }

    fun payBill(token: String, sessionId: String, refreshToken: String, accessKey: String,
                customerId: Int, typeKey: Int, flexKey: String, transactionId: String,
                account: String, amount: String, optional1: String, otp: String) {
        _paymentResponse.value = Event(NetworkState2.Loading())

        UtilitiesService.getInstance(token, sessionId, refreshToken)
                .payBill(accessKey, customerId, typeKey, flexKey, transactionId, account, amount, optional1, otp,
                        object : ApiCallbackImpl<BillPaymentResponse>(_paymentResponse) {
                            override fun onSuccess(data: BillPaymentResponse) {
                                _paymentResponse.value = Event(NetworkState2.Success(data))
                            }

                            override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                                _paymentResponse.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                            }
                        })
    }


    fun getOtp(token: String, sessionId: String, refreshToken: String, customerId: Int) {
        _otpResponse.value = Event(NetworkState2.Loading())

        UtilitiesService.getInstance(token, sessionId, refreshToken).getOtp(customerId,
                object : ApiCallbackImpl<String>(_otpResponse) {
                    override fun onSuccess(data: String) {
                        _otpResponse.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _otpResponse.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })

    }


    fun getBeneficiaryAccounts(token: String, sessionId: String, refreshToken: String, customerId: Long, typeId: Int, operator: String) {

        UtilitiesService.getInstance(token, sessionId, refreshToken).getOperators(customerId, typeId, "971",
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
                        _beneficiariesState.value = Event(NetworkState2.Success(data))
                    }
                })
    }

}