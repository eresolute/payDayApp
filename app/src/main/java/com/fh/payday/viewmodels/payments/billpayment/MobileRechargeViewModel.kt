package com.fh.payday.viewmodels.payments.billpayment

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.fh.payday.datasource.models.login.User
import com.fh.payday.datasource.models.payments.*
import com.fh.payday.datasource.remote.ApiCallback
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.payments.PaymentService
import com.fh.payday.utilities.Event

class MobileRechargeViewModel : ViewModel() {

    private val _operatorRequest = MutableLiveData<Event<NetworkState2<List<Operator>>>>()
    private val _operatorDetail = MutableLiveData<Event<NetworkState2<OperatorDetail>>>()

    private val _topUpDetail = MutableLiveData<Event<NetworkState2<RechargeDetail>>>()

    private val _billDetailEtisalat = MutableLiveData<Event<NetworkState2<BillDetailEtisalat>>>()
    private val _billDu = MutableLiveData<Event<NetworkState2<BillDetailDU>>>()

    private val _paymentResponse = MutableLiveData<Event<NetworkState2<BillPaymentResponse>>>()
    private val _paymentDuResponse = MutableLiveData<Event<NetworkState2<BillPaymentDuResponse>>>()

    private val _otpResponse = MutableLiveData<Event<NetworkState2<String>>>()
    private val _otpResponse2 = MutableLiveData<Event<NetworkState2<String>>>()
    private val _recentAccountState = MutableLiveData<Event<NetworkState2<ArrayList<RecentAccount>>>>()
    private val _beneficiariesState = MutableLiveData<Event<NetworkState2<ArrayList<Beneficiaries>>>>()


    var TYPE_ID = MutableLiveData<Int>()
    var SERVICE = MutableLiveData<String>()
    var AMOUNT = MutableLiveData<String>()
    var MOBILE = MutableLiveData<String>()
    var operatorDetail = MutableLiveData<OperatorDetail>()
    var topUpDetail = MutableLiveData<RechargeDetail>()
    var billDetailEtisalat = MutableLiveData<BillDetailEtisalat>()
    var billDetailDU = MutableLiveData<BillDetailDU>()

    var user: User? = null
    var flexKey: String? = null
    var typeKey: Int? = null
    var accessKey: String? = null
    var operator: String? = null
    var mobileNumber: String? = null
    var selectedAccessKey: String? = null
    var dataClear: Boolean = false

    val operaterState: LiveData<Event<NetworkState2<List<Operator>>>>
        get() = _operatorRequest
    val operatorDetailState: LiveData<Event<NetworkState2<OperatorDetail>>>
        get() = _operatorDetail
    val rechargeState: LiveData<Event<NetworkState2<RechargeDetail>>>
        get() = _topUpDetail
    val billStateEtisalat: LiveData<Event<NetworkState2<BillDetailEtisalat>>>
        get() = _billDetailEtisalat
    val billStateDu: LiveData<Event<NetworkState2<BillDetailDU>>>
        get() = _billDu
    val paymentState: LiveData<Event<NetworkState2<BillPaymentResponse>>>
        get() = _paymentResponse
    val paymentDuResponse: LiveData<Event<NetworkState2<BillPaymentDuResponse>>>
        get() = _paymentDuResponse
    val optResponseState: LiveData<Event<NetworkState2<String>>>
        get() = _otpResponse

    val optResponseState2: LiveData<Event<NetworkState2<String>>>
        get() = _otpResponse2

    val recentAccountState: LiveData<Event<NetworkState2<ArrayList<RecentAccount>>>>
        get() = _recentAccountState
    val beneficiariesState: LiveData<Event<NetworkState2<ArrayList<Beneficiaries>>>>
        get() = _beneficiariesState
    var isLoading = false

    fun operatorRequest(token: String, sessionId: String, refreshToken: String, customerId: Long, method: String,
                        typeId: Int, countryCode: String) {
        _operatorRequest.value = Event(NetworkState2.Loading())

        PaymentService.getInstance(token, sessionId, refreshToken).getOperators(customerId, method, typeId, countryCode,
                object : ApiCallbackImpl<List<Operator>>(_operatorRequest) {
                    override fun onSuccess(data: List<Operator>) {
                        _operatorRequest.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _operatorRequest.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })
    }

    fun operatorDetails(token: String, sessionId: String, refreshToken: String, customerId: Long, accessKey: String,
                        typeId: Int, planType: String, countryCode: String) {
        _operatorDetail.value = Event(NetworkState2.Loading())

        PaymentService.getInstance(token, sessionId, refreshToken).getOperatorDetail(customerId, accessKey,
                typeId, planType, countryCode,
                object : ApiCallbackImpl<OperatorDetail>(_operatorDetail) {
                    override fun onSuccess(data: OperatorDetail) {
                        flexKey = data.flexiKey
                        this@MobileRechargeViewModel.accessKey = accessKey
                        typeKey = data.typeKey.toInt()
                        operatorDetail.value = data

                        _operatorDetail.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _operatorDetail.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })
    }

    fun rechargeDetailEtisalat(token: String, sessionId: String, refreshToken: String, customerId: Long, accessKey: String,
                               method: String, typeId: Int, account: String, flexKey: String, typeKey: Int, option: String) {
        _topUpDetail.value = Event(NetworkState2.Loading())

        PaymentService.getInstance(token, sessionId, refreshToken).getTopUpDetail(customerId, accessKey,
                method, typeId, account, flexKey, typeKey, option,
                object : ApiCallbackImpl<RechargeDetail>(_topUpDetail) {
                    override fun onSuccess(data: RechargeDetail) {
                        topUpDetail.value = data
                        MOBILE.value = account
                        _topUpDetail.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _topUpDetail.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })
    }

    fun billDetailEtisalat(token: String, sessionId: String, refreshToken: String, customerId: Long, accessKey: String,
                           method: String, typeId: Int, account: String, flexKey: String,
                           typeKey: Int, option: String) {
        _billDetailEtisalat.value = Event(NetworkState2.Loading())

        PaymentService.getInstance(token, sessionId, refreshToken).getBillBalanceEtisalat(customerId, accessKey,
                method, typeId, account, flexKey, typeKey, option,
                object : ApiCallbackImpl<BillDetailEtisalat>(_billDetailEtisalat) {
                    override fun onSuccess(data: BillDetailEtisalat) {
                        billDetailEtisalat.value = data
                        MOBILE.value = account
                        _billDetailEtisalat.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _billDetailEtisalat.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })
    }

    fun billBalanceDu(token: String, sessionId: String, refreshToken: String, customerId: Long, accessKey: String,
                      method: String, typeId: Int, account: String, flexiKey: String, typeKey: Int) {
        _billDu.value = Event(NetworkState2.Loading())

        PaymentService.getInstance(token, sessionId, refreshToken).getBillBalanceDu(customerId, accessKey,
                method, typeId, account, flexiKey, typeKey,
                object : ApiCallbackImpl<BillDetailDU>(_billDu) {
                    override fun onSuccess(data: BillDetailDU) {
                        billDetailDU.value = data
                        AMOUNT.value = data.Balance
                        MOBILE.value = account

                        _billDu.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _billDu.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })

    }

    fun payBill(token: String, sessionId: String, refreshToken: String, accessKey: String, customerId: Int, typeKey: Int,
                flexKey: String, transactionId: String, account: String, amount: String, serviceType: String,
                providerId: String, otp: String) {
        _paymentResponse.value = Event(NetworkState2.Loading())

        PaymentService.getInstance(token, sessionId, refreshToken).payBill(accessKey, customerId, typeKey,
                flexKey, transactionId, account, amount, serviceType, providerId, otp,
                object : ApiCallbackImpl<BillPaymentResponse>(_paymentResponse) {
                    override fun onSuccess(data: BillPaymentResponse) {
                        _paymentResponse.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _paymentResponse.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })
    }

    fun payBillDu(token: String, sessionId: String, refreshToken: String, accessKey: String, customerId: Int, typeKey: Int,
                  flexKey: String, transactionId: String, account: String, amount: String, option1: String,
                  option2: String?, otp: String) {
        _paymentDuResponse.value = Event(NetworkState2.Loading())

        PaymentService.getInstance(token, sessionId, refreshToken).payBillDu(accessKey, customerId, typeKey,
                flexKey, transactionId, account, amount, option1, option2, otp,
                object : ApiCallbackImpl<BillPaymentDuResponse>(_paymentDuResponse) {
                    override fun onSuccess(data: BillPaymentDuResponse) {
                        _paymentDuResponse.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _paymentDuResponse.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })
    }

    fun getOtp(token: String, sessionId: String, refreshToken: String, customerId: Int) {
        _otpResponse.value = Event(NetworkState2.Loading())

        PaymentService.getInstance(token, sessionId, refreshToken).getOtp(customerId,
                object : ApiCallbackImpl<String>(_otpResponse) {
                    override fun onSuccess(data: String) {
                        _otpResponse.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _otpResponse.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })

    }

    fun getOtp2(token: String, sessionId: String, refreshToken: String, customerId: Int) {
        _otpResponse2.value = Event(NetworkState2.Loading())

        PaymentService.getInstance(token, sessionId, refreshToken).getOtp(customerId,
                object : ApiCallbackImpl<String>(_otpResponse) {
                    override fun onSuccess(data: String) {
                        _otpResponse2.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _otpResponse2.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })

    }

    fun getBeneficiaryAccounts(token: String, sessionId: String, refreshToken: String, customerId: Long, typeId: Int, operator: String) {
        isLoading = true

        PaymentService.getInstance(token, sessionId, refreshToken).getOperators(customerId, "operators", typeId, "971",
                object : ApiCallback<List<Operator>> {
                    override fun onFailure(t: Throwable) {
                        isLoading = false
                    }

                    override fun onError(message: String) {
                        isLoading = false
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _beneficiariesState.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }

                    override fun onSuccess(data: List<Operator>) {
                        isLoading = false
                        if (data.isNullOrEmpty()) return

                        val accessKey = data.filter {
                            operator.equals(it.serviceProvider, true)
                        }.map(Operator::accessKey).fold("") { _, s -> s }

                        if (accessKey.isNotEmpty()) {
                            selectedAccessKey = accessKey
                            getBeneficiaries(token, sessionId, refreshToken, customerId, accessKey)
                        }
                    }
                })
    }

    private fun getRecentAccounts(token: String, sessionId: String, refreshToken: String, customerId: Long, accessKey: String) {
        isLoading = true

        PaymentService.getInstance(token, sessionId, refreshToken).getRecentAccounts(customerId, accessKey,
                object : ApiCallback<ArrayList<RecentAccount>> {
                    override fun onFailure(t: Throwable) {
                        isLoading = false
                    }

                    override fun onError(message: String) {
                        isLoading = false
                    }

                    override fun onSuccess(data: ArrayList<RecentAccount>) {
                        isLoading = false
                        _recentAccountState.value = Event(NetworkState2.Success(data))
                    }
                })
    }

    fun getBeneficiaries(token: String, sessionId: String, refreshToken: String, customerId: Long, accessKey: String) {
        isLoading = true
        PaymentService.getInstance(token, sessionId, refreshToken).getBeneficiaries(customerId, accessKey,
                object : ApiCallback<ArrayList<Beneficiaries>> {
                    override fun onFailure(t: Throwable) { isLoading = false }

                    override fun onError(message: String) {
                        isLoading = false
                        _beneficiariesState.value = Event(NetworkState2.Error(message))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        isLoading = false
                        _beneficiariesState.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }

                    override fun onSuccess(data: ArrayList<Beneficiaries>) {
                        isLoading = false
                        _beneficiariesState.value = Event(NetworkState2.Success(data))
                    }
                })
    }
}