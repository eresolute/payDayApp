package com.fh.payday.viewmodels.payments.internationaltopup

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.fh.payday.datasource.models.Plan
import com.fh.payday.datasource.models.login.User
import com.fh.payday.datasource.models.payments.*
import com.fh.payday.datasource.remote.ApiCallback
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.payments.PaymentService
import com.fh.payday.utilities.Event

class InternationalTopupViewModel : ViewModel() {

    private val _operatorDetailFlexiState = MutableLiveData<Event<NetworkState2<OperatorDetail>>>()
    val operatorDetailFlexiState: LiveData<Event<NetworkState2<OperatorDetail>>>
        get() = _operatorDetailFlexiState

    private val _operatorDetailFixedState = MutableLiveData<Event<NetworkState2<List<OperatorDetailFixed>>>>()
    val operatorDetailFixedState: LiveData<Event<NetworkState2<List<OperatorDetailFixed>>>>
        get() = _operatorDetailFixedState

    private val _payableAmountState = MutableLiveData<Event<NetworkState2<PayableAmount>>>()
    val payableAmountState: LiveData<Event<NetworkState2<PayableAmount>>>
        get() = _payableAmountState

    private val _otpState = MutableLiveData<Event<NetworkState2<String>>>()
    val otpState: LiveData<Event<NetworkState2<String>>>
        get() = _otpState

    private val _paymentState = MutableLiveData<Event<NetworkState2<PaymentResult>>>()
    val paymentState: LiveData<Event<NetworkState2<PaymentResult>>>
        get() = _paymentState

    private val _beneficiariesState = MutableLiveData<Event<NetworkState2<ArrayList<Beneficiaries>>>>()
    val beneficiariesState: LiveData<Event<NetworkState2<ArrayList<Beneficiaries>>>>
        get() = _beneficiariesState

    private val _data = MutableLiveData<HashMap<String, String>>()
    val data: LiveData<HashMap<String, String>> get() = _data

    var user: User? = null

    var validationRegex : String ? = null
    var selectedOperator: Operator? = null
    var typeId: Int? = null
    var planType: String? = null
    var plans: ArrayList<Plan>? = null
    var countryCode: Int? = null
    var operatorDetailsFlexi: OperatorDetail? = null
    var operatorDetailsFixed: OperatorDetailFixed? = null
    var selectedPlan: Plan? = null
    var dataClear: Boolean = false

    fun setAccountNo(accountNo: String, currency: String = "") {
        _data.value = hashMapOf("account_no" to accountNo, "currency" to currency)
    }

    fun setAmount(amount: String) {
        _data.value = data.value?.apply { putAll(hashMapOf("amount" to amount)) }
    }

    fun setPayableAmount(payableAmount: String) {
        _data.value = data.value?.apply { putAll(hashMapOf("payable_amount" to payableAmount)) }
    }

    fun operatorDetails(token: String, sessionId: String, refreshToken: String, customerId: Long, accessKey: String,
                        typeId: Int, planType: String, countryCode: String = "971") {
        this.planType = planType
        when (planType) {
            PlanType.FLEXI -> operatorDetailsFlexi(token, sessionId, refreshToken, customerId, accessKey, typeId, countryCode)
            PlanType.FIXED -> operatorDetailsFixed(token, sessionId,  refreshToken, customerId,  accessKey, typeId, countryCode)
        }
    }

    private fun operatorDetailsFlexi(token: String, sessionId: String, refreshToken: String, customerId: Long,
                                     accessKey: String, typeId: Int, countryCode: String) {
        _operatorDetailFlexiState.value = Event(NetworkState2.Loading())

        PaymentService.getInstance(token, sessionId, refreshToken).getOperatorDetail(customerId, accessKey,
            typeId, PlanType.FLEXI, countryCode,
            object : ApiCallbackImpl<OperatorDetail>(_operatorDetailFlexiState) {
                override fun onSuccess(data: OperatorDetail) {
                    validationRegex = data.validationRegex
                    operatorDetailsFlexi = data
                   // setAccountNo(accountNo, data.baseCurrency)
                    _operatorDetailFlexiState.value = Event(NetworkState2.Success(data))
                }

                override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                    _operatorDetailFlexiState.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                }
            })
    }

    private fun operatorDetailsFixed(token: String, sessionId: String, refreshToken: String, customerId: Long,
                                     accessKey: String, typeId: Int, countryCode: String) {
        _operatorDetailFixedState.value = Event(NetworkState2.Loading())

        PaymentService.getInstance(token, sessionId, refreshToken).getOperatorDetailFixed(customerId, accessKey,
            typeId, PlanType.FIXED, countryCode, "",
            object : ApiCallbackImpl<List<OperatorDetailFixed>>(_operatorDetailFixedState) {
                override fun onSuccess(data: List<OperatorDetailFixed>) {
                    plans = ArrayList(data.map {
                        Plan(it.baseAmount, it.planCost, it.baseCurrency, it.planId, it.typeKey, it.validationRegex)
                    })
                    validationRegex = plans!![0].validationRegex

                   // if (!plans.isNullOrEmpty()) setAccountNo(accountNo, plans!![0].currency)
                    _operatorDetailFixedState.value = Event(NetworkState2.Success(data))
                    operatorDetailsFixed = data[0]
                }
            })
    }

    fun getPayableAmount(token: String, sessionId: String, refreshToken: String, customerId: Long, accessKey: String,
                         typeKey: Int, flexiKey: String, amount: Double) {
        _payableAmountState.value = Event(NetworkState2.Loading())

        PaymentService.getInstance(token, sessionId, refreshToken).getPayableAmount(customerId, accessKey, typeKey, flexiKey, amount,
            object : ApiCallbackImpl<PayableAmount>(_payableAmountState) {
                override fun onSuccess(data: PayableAmount) {
                    setAmount(amount.toString())
                    setPayableAmount(data.amountInAED.toString())
                    _payableAmountState.value = Event(NetworkState2.Success(data))
                }

                override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                    _payableAmountState.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                }
            })
    }

    fun generateOtp(token: String, sessionId: String, refreshToken: String, customerId: Int) {
        _otpState.value = Event(NetworkState2.Loading())

        PaymentService.getInstance(token, sessionId, refreshToken).getOtp(customerId,
            object : ApiCallbackImpl<String>(_otpState) {
                override fun onSuccess(data: String) {
                    _otpState.value = Event(NetworkState2.Success(data))
                }
            })
    }

    fun makePayment(token: String, sessionId: String, refreshToken: String, customerId: Long, paymentReq: PaymentRequest) {
        _paymentState.value = Event(NetworkState2.Loading())

        PaymentService.getInstance(token, sessionId, refreshToken).makePayment(customerId, paymentReq,
            object : ApiCallbackImpl<PaymentResult>(_paymentState) {
                override fun onSuccess(data: PaymentResult) {
                    _paymentState.value = Event(NetworkState2.Success(data))
                }

                override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                    _paymentState.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                }
            })
    }

    fun getBeneficiaries(token: String, sessionId: String, refreshToken: String,
                         customerId: Long, accessKey: String) {

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