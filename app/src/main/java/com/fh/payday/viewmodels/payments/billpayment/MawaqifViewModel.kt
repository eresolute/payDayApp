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
import com.fh.payday.utilities.getDecimalValue
import java.math.BigInteger
import java.util.*

class MawaqifViewModel : ViewModel() {
    companion object {
        const val MAWAQIF_TOPUP = "mawaqif"
    }

    var user: User? = null
    private val _operatorRequest = MutableLiveData<Event<NetworkState2<List<Operator>>>>()
    val operatorRequest: LiveData<Event<NetworkState2<List<Operator>>>>
        get() = _operatorRequest
    private val _operatorDetail = MutableLiveData<Event<NetworkState2<OperatorDetail>>>()
    val operatorDetail: LiveData<Event<NetworkState2<OperatorDetail>>>
        get() = _operatorDetail

    private val _balanceState = MutableLiveData<Event<NetworkState2<Bill>>>()
    val balanceState: LiveData<Event<NetworkState2<Bill>>>
        get() = _balanceState

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

    var typeId: Int? = null
    var planType: String? = null
    var accessKey: String? = null

    var selectedOperator: Operator? = null
    var operatorDetailsFlexi: OperatorDetail? = null
    var enteredAmount: String? = null
    var billAmount: String? = null

    fun setMobileNo(accountNo: String, currency: String = "") {
        _data.value = hashMapOf("mobile_no" to accountNo, "currency" to currency)
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

    fun setTransactionId(transId: String) {
        _data.value = data.value?.apply { putAll(hashMapOf("transaction_id" to transId)) }
    }

    fun getOperatorRequest(token: String, sessionId: String, refreshToken: String, customerId: Long, method: String,
                           typeId: Int, countryCode: String) {
        _operatorRequest.value = Event(NetworkState2.Loading())

        PaymentService.getInstance(token, sessionId, refreshToken).getOperators(customerId, method, typeId, countryCode,
                object : ApiCallbackImpl<List<Operator>>(_operatorRequest) {
                    override fun onSuccess(data: List<Operator>) {
                        selectedOperator = data.single { MAWAQIF_TOPUP.equals(it.serviceProvider, true) }
                        _operatorRequest.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _operatorRequest.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })
    }

    fun getOperatorDetails(token: String, sessionId: String, refreshToken: String, customerId: Long, accessKey: String,
                           typeId: Int, planType: String, countryCode: String) {
        _operatorDetail.value = Event(NetworkState2.Loading())

        PaymentService.getInstance(token, sessionId, refreshToken).getOperatorDetail(customerId, accessKey,
                typeId, planType, countryCode,
                object : ApiCallbackImpl<OperatorDetail>(_operatorDetail) {
                    override fun onSuccess(data: OperatorDetail) {
                        operatorDetailsFlexi = data

                        _operatorDetail.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _operatorDetail.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })
    }

    fun getBalance(token: String, sessionId: String, refreshToken: String, customerId: Long, accessKey: String, typeId: Int,
                   account: String, flexiKey: String, typeKey: Int, optional1: String = "") {
        _balanceState.value = Event(NetworkState2.Loading())

        PaymentService.getInstance(token, sessionId, refreshToken).getBalance(customerId, accessKey, typeId, account, flexiKey, typeKey, optional1,
                object : ApiCallbackImpl<Bill>(_balanceState) {
                    override fun onSuccess(data: Bill) {
                        //billAmount = data.balanceAmount.toString()
                        setBilledAmount(data.balanceAmount.toString().getDecimalValue())
                        setTransactionId(data.transactionId ?: return)
                        _balanceState.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _balanceState.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })
    }

    fun makePayment(token: String, sessionId: String, refreshToken: String, customerId: Long, paymentReq: PaymentRequest) {

        _paymentState.value = Event(NetworkState2.Loading())

        PaymentService.getInstance(token, sessionId, refreshToken).makePaymentMawaqif(customerId, paymentReq,
                object : ApiCallbackImpl<PaymentResult>(_paymentState) {
                    override fun onSuccess(data: PaymentResult) {
                        _paymentState.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _paymentState.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
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

    fun generateTransId() = String.format("%040d", BigInteger(UUID.randomUUID().toString()
            .replace("-", ""), 16)).substring(0, 19)
}