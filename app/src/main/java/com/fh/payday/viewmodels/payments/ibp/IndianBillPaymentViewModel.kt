package com.fh.payday.viewmodels.payments.ibp

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.fh.payday.datasource.models.IndianState
import com.fh.payday.datasource.models.Plan
import com.fh.payday.datasource.models.login.User
import com.fh.payday.datasource.models.payments.*
import com.fh.payday.datasource.remote.ApiCallback
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.payments.PaymentService
import com.fh.payday.utilities.Event
import com.fh.payday.utilities.getDecimalValue
import io.reactivex.disposables.CompositeDisposable
import java.math.BigInteger
import java.util.*
import kotlin.collections.ArrayList

class IndianBillPaymentViewModel : ViewModel() {

    var user: User? = null
    private val disposable = CompositeDisposable()

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
    private val _balanceState = MutableLiveData<Event<NetworkState2<Bill>>>()
    val balanceState: LiveData<Event<NetworkState2<Bill>>>
        get() = _balanceState
    private val _recentAccountState = MutableLiveData<Event<NetworkState2<ArrayList<RecentAccount>>>>()
    val recentAccountState: LiveData<Event<NetworkState2<ArrayList<RecentAccount>>>>
        get() = _recentAccountState

    private val _indianStates = MutableLiveData<Event<NetworkState2<List<IndianState>>>>()
    val indianStates: LiveData<Event<NetworkState2<List<IndianState>>>>
        get() = _indianStates

    private val _beneficiariesState = MutableLiveData<Event<NetworkState2<ArrayList<Beneficiaries>>>>()
    val beneficiariesState: LiveData<Event<NetworkState2<ArrayList<Beneficiaries>>>>
        get() = _beneficiariesState
    private val _data = MutableLiveData<HashMap<String, String>>()
    val data: LiveData<HashMap<String, String>> get() = _data

    private val _blbState = MutableLiveData<Boolean>()
    val blbState: LiveData<Boolean> get() = _blbState
    fun setBLBSelected() {
        _blbState.value = true
    }

    private val _landLinebState = MutableLiveData<Boolean>()
    val landLineState: LiveData<Boolean> get() = _landLinebState
    fun setLandLineSelected() {
        _landLinebState.value = true
    }

    var selectedOperator: Operator? = null
    var typeId: Int? = null
    var planType: String? = null
    var plans: ArrayList<Plan>? = null
    var operatorDetailsFlexi: OperatorDetail? = null
    var selectedPlan: Plan? = null
    var enteredAmount: String? = null
    var dataClear: Boolean = false
    var billAmount: String? = null

    var selectedIndianState: List<IndianState>? = null

    override fun onCleared() {
        disposable.clear()
        super.onCleared()
    }

    fun setSTDCode(stdCode: String) {
        _data.value = hashMapOf("std_code" to stdCode)
    }

    fun setStateName(stateName: String) {
        _data.value = hashMapOf("state_name" to stateName)
    }

    fun setAccountNumber(accountNo: String) {
        _data.value = hashMapOf("account_no" to accountNo)
    }

    fun setBSNLAccount(account: String) {
        _data.value = data.value?.apply {
            putAll(hashMapOf("bsnl_account" to account))
        }
    }

    fun setAccountNo(accountNo: String, currency: String = "") {
        if (data.value?.containsKey("std_code") == true) {
            _data.value = data.value?.apply { putAll(hashMapOf("account_no" to accountNo, "currency" to currency)) }
        } else {
            _data.value = hashMapOf("account_no" to accountNo, "currency" to currency)
        }
    }

    fun setAmount(amount: String) {
        _data.value = data.value?.apply { putAll(hashMapOf("amount" to amount)) }

    }

    fun setBilledAmount(amount: String) {
        _data.value = data.value?.apply { putAll(hashMapOf("bill_amount" to amount)) }
    }

    fun setPayableAmount(payableAmount: String) {
        _data.value = data.value?.apply { putAll(hashMapOf("payable_amount" to payableAmount)) }
    }

    fun setReferenceId(refId: String) {
        _data.value = data.value?.apply { putAll(hashMapOf("reference_id" to refId)) }
    }

    fun setDob(dob: String) {
        _data.value = data.value?.apply { putAll(hashMapOf("dob" to dob)) }
    }

    fun operatorDetails(
            token: String, sessionId: String, refreshToken: String, customerId: Long, accountNo: String,
            accessKey: String, typeId: Int, planType: String, countryCode: String = "971", optional1: String = ""
    ) {
        this.planType = planType
        when (planType) {
            PlanType.FLEXI -> operatorDetailsFlexi(token, sessionId, refreshToken, customerId, accountNo, accessKey, typeId, countryCode)
            PlanType.FIXED -> operatorDetailsFixed(token, sessionId, refreshToken, customerId, accountNo, accessKey, typeId, countryCode, optional1)
        }
    }

    private fun operatorDetailsFlexi(
            token: String, sessionId: String, refreshToken: String, customerId: Long, accountNo: String,
            accessKey: String, typeId: Int, countryCode: String
    ) {
        _operatorDetailFlexiState.value = Event(NetworkState2.Loading())

        PaymentService.getInstance(token, sessionId, refreshToken).getOperatorDetail(customerId, accessKey,
                typeId, PlanType.FLEXI, countryCode,
                object : ApiCallbackImpl<OperatorDetail>(_operatorDetailFlexiState) {
                    override fun onSuccess(data: OperatorDetail) {
                        operatorDetailsFlexi = data
                        setAccountNo(accountNo, data.baseCurrency)
                        _operatorDetailFlexiState.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _operatorDetailFlexiState.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })
    }

    private fun operatorDetailsFixed(
            token: String, sessionId: String, refreshToken: String, customerId: Long, accountNo: String,
            accessKey: String, typeId: Int, countryCode: String, optional1: String
    ) {
        _operatorDetailFixedState.value = Event(NetworkState2.Loading())

        PaymentService.getInstance(token, sessionId, refreshToken).getOperatorDetailFixed(customerId, accessKey,
                typeId, PlanType.FIXED, countryCode, optional1,
                object : ApiCallbackImpl<List<OperatorDetailFixed>>(_operatorDetailFixedState) {
                    override fun onSuccess(data: List<OperatorDetailFixed>) {
                        plans = ArrayList(data.map {
                            Plan(it.baseAmount, it.planCost, it.baseCurrency, it.planId, it.typeKey, it.validationRegex, it.rechargeDescription)
                        })

                        if (!plans.isNullOrEmpty()) setAccountNo(accountNo, plans!![0].currency)
                        _operatorDetailFixedState.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _operatorDetailFixedState.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })
    }

    fun getBalance(
            token: String, sessionId: String, refreshToken: String, customerId: Long, accessKey: String, typeId: Int,
            account: String, flexiKey: String, typeKey: Int, optional1: String
    ) {
        _balanceState.value = Event(NetworkState2.Loading())

        PaymentService.getInstance(token, sessionId, refreshToken).getBalance(customerId, accessKey, typeId, account, flexiKey, typeKey, optional1,
                object : ApiCallbackImpl<Bill>(_balanceState) {
                    override fun onSuccess(data: Bill) {
                        setAmount(data.dueAmount.toString().getDecimalValue())
                        setPayableAmount(data.dueAmountInAed.toString().getDecimalValue())
                        data.referenceId?.let { setReferenceId(it) }
                        billAmount = data.dueAmount.toString()
                        _balanceState.value = Event(NetworkState2.Success(data))
                        setBilledAmount(data.dueAmount.toString().getDecimalValue())
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _balanceState.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })
    }

    fun getPayableAmount(
            token: String, sessionId: String, refreshToken: String, customerId: Long, accessKey: String,
            typeKey: Int, flexiKey: String, amount: Double
    ) {
        _payableAmountState.value = Event(NetworkState2.Loading())

        PaymentService.getInstance(token, sessionId, refreshToken).getPayableAmount(customerId, accessKey, typeKey, flexiKey, amount,
                object : ApiCallbackImpl<PayableAmount>(_payableAmountState) {
                    override fun onSuccess(data: PayableAmount) {
                        enteredAmount = amount.toString()
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

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _otpState.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })
    }

    fun makePayment(token: String, sessionId: String, refreshToken: String, customerId: Long, paymentReq: PaymentRequest) {
        _paymentState.value = Event(NetworkState2.Loading())

        val stdCode = data.value?.get("std_code")
        if (stdCode != null) {
            when (paymentReq.accessKey) {
                AIRTEL_LANDLINE_ACCESS_KEY, BSNL_LANDLINE_ACCESS_KEY, MTNL_LANDLINE_ACCESS_KEY -> {
                    paymentReq.account = stdCode + paymentReq.account
                }
            }
        }

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

    fun getBeneficiaries(
            token: String, sessionId: String, refreshToken: String,
            customerId: Long, accessKey: String
    ) {

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


    fun getRecentAccounts(token: String, sessionId: String, refreshToken: String, customerId: Long, accessKey: String) {
        PaymentService.getInstance(token, sessionId, refreshToken).getRecentAccounts(customerId, accessKey,
                object : ApiCallback<ArrayList<RecentAccount>> {
                    override fun onFailure(t: Throwable) {}

                    override fun onError(message: String) {}

                    override fun onSuccess(data: ArrayList<RecentAccount>) {
                        _recentAccountState.value = Event(NetworkState2.Success(data))
                    }
                })
    }

    fun getIndianStates(token: String, sessionId: String, refreshToken: String, customerId: Long, method: String, accessKey: String) {
        _indianStates.value = Event(NetworkState2.Loading())

        PaymentService.getInstance(token, sessionId, refreshToken).getIndianStates(customerId, method,
                object : ApiCallbackImpl<List<IndianState>>(_indianStates) {
                    override fun onSuccess(data: List<IndianState>) {
                        val filteredStates = data.filterNot { "DL".equals(it.stateAlias, true) && accessKey.equals("BGP", true) }
                        selectedIndianState = filteredStates
                        _indianStates.value = Event(NetworkState2.Success(filteredStates))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _indianStates.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })
    }


    fun generateTransId() = String.format("%040d", BigInteger(UUID.randomUUID().toString()
            .replace("-", ""), 16)).substring(0, 19)
}