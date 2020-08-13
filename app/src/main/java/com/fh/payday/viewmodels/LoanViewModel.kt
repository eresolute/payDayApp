package com.fh.payday.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.fh.payday.datasource.models.loan.LoanAcceptance
import com.fh.payday.datasource.models.loan.LoanOffer
import com.fh.payday.datasource.models.loan.LoanRequest
import com.fh.payday.datasource.models.login.User
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.loan.LoanService
import com.fh.payday.datasource.remote.otp.OtpService
import com.fh.payday.utilities.Event
import java.io.Serializable
import kotlin.math.roundToInt

const val MIN_LOAN_AMOUNT = 1500

class LoanViewModel : ViewModel() {

    var loanOffer: LoanOffer? = null
    var customizedLoanOffer: LoanOffer? = null
    var minEMI = 100; private set
    var minLoanAmount: String? = null

    var user: User? = null

    var productType: String = LOAN
    var loanNumber: String? = null
    var filePath: String? = null

    var fromPN = false
    var selectedLoanAmount = 0.0
        private set

    private val _loanRequest = MutableLiveData<Event<NetworkState2<LoanOffer>>>()
    private val _rescheduleLoan = MutableLiveData<Event<NetworkState2<List<LoanRequest>>>>()
    private val _requestRescheduleLoan = MutableLiveData<Event<NetworkState2<LoanRequest>>>()
    private val _customizeLoan = MutableLiveData<Event<NetworkState2<LoanOffer>>>()
    private val _generateOtp = MutableLiveData<Event<NetworkState2<String>>>()
    private val _acceptLoanOffer = MutableLiveData<Event<NetworkState2<LoanAcceptance>>>()
    val loanRequestState: LiveData<Event<NetworkState2<LoanOffer>>>
        get() = _loanRequest
    val customizeLoanState: LiveData<Event<NetworkState2<LoanOffer>>>
        get() = _customizeLoan
    val generateOtpState: LiveData<Event<NetworkState2<String>>>
        get() = _generateOtp
    val acceptLoanOfferState: LiveData<Event<NetworkState2<LoanAcceptance>>>
        get() = _acceptLoanOffer
    val rescheduleLoanState : LiveData<Event<NetworkState2<List<LoanRequest>>>>
        get() = _rescheduleLoan

    val requestRescheduleLoanState : LiveData<Event<NetworkState2<LoanRequest>>>
        get() = _requestRescheduleLoan

    fun setSelectedLoanAmount(loanAmount: Double) {
        selectedLoanAmount = loanAmount
    }

    fun loanRequest(token: String, sessionId: String, refreshToken: String, customerId: Long) {
        _loanRequest.value = Event(NetworkState2.Loading())

        LoanService.getInstance(token, sessionId, refreshToken).requestLoan(customerId, productType,
            object : ApiCallbackImpl<LoanOffer> (_loanRequest) {
                override fun onSuccess(data: LoanOffer) {
                    minEMI = calcLoanEmi(1000.toDouble(), data.interestRate.toDouble(), data.tenure.toInt()).roundToInt()
                    loanOffer = data
                    minLoanAmount = data.minAmount
                    customizedLoanOffer = data
                    _loanRequest.value = Event(NetworkState2.Success(data))
                }

                override fun onSuccess(data: LoanOffer, message: String?) {
                    minEMI = calcLoanEmi(1000.toDouble(), data.interestRate.toDouble(), data.tenure.toInt()).roundToInt()
                    loanOffer = data
                    customizedLoanOffer = data
                    _loanRequest.value = Event(NetworkState2.Success(data, message))
                }
            })
    }

    fun customizeLoan(token: String, sessionId: String, refreshToken: String, customerId: Long, loanAmount: String, loanTenure: String) {
        _customizeLoan.value = Event(NetworkState2.Loading())

        LoanService.getInstance(token, sessionId, refreshToken).customizeLoan(customerId, loanAmount, loanTenure, productType,
            object : ApiCallbackImpl<LoanOffer>(_customizeLoan) {
                override fun onSuccess(data: LoanOffer) {
                    customizedLoanOffer = data
                    _customizeLoan.value = Event(NetworkState2.Success(data))
                }
            })
    }

    fun generateOtp(token: String, sessionId: String, refreshToken: String, customerId: Long) {
        _generateOtp.value = Event(NetworkState2.Loading())

        OtpService.getInstance(token, sessionId, refreshToken).getOtp(customerId.toInt(),
            object : ApiCallbackImpl<String>(_generateOtp) {
                override fun onSuccess(data: String) {
                    _generateOtp.value = Event(NetworkState2.Success(data))
                }
            })
    }

    fun acceptLoanOffer(token: String, sessionId: String, refreshToken: String, customerId: Long,
                        otp: String, loanAmount: String, loanTenure: String, interestRate: String,
                        applicationId: String, lastSalaryCredit: String) {

        when (productType) {
            LOAN -> applyLoan(token, sessionId, refreshToken, customerId, otp, loanAmount,
                loanTenure, interestRate, applicationId, lastSalaryCredit)
            TOPUP_LOAN -> applyLoanTopUp(token, sessionId, refreshToken,  customerId, otp,
                loanAmount, loanTenure, interestRate, applicationId, lastSalaryCredit)
        }
    }

    private fun applyLoan(token: String, sessionId: String, refreshToken: String, customerId: Long,
                          otp: String, loanAmount: String, loanTenure: String, interestRate: String,
                          applicationId: String, lastSalaryCredit: String) {
        _acceptLoanOffer.value = Event(NetworkState2.Loading())
        LoanService.getInstance(token, sessionId, refreshToken).loanAcceptance(customerId, otp,
            loanAmount, loanTenure, interestRate, applicationId, lastSalaryCredit, fromPN, productType,
            object : ApiCallbackImpl<LoanAcceptance>(_acceptLoanOffer) {
                override fun onSuccess(data: LoanAcceptance) {
                    _acceptLoanOffer.value = Event(NetworkState2.Success(data))
                }

                override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                    _acceptLoanOffer.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                }
            })
    }

    private fun applyLoanTopUp(token: String, sessionId: String, refreshToken: String, customerId: Long,
                               otp: String, loanAmount: String, loanTenure: String, interestRate: String,
                               applicationId: String, lastSalaryCredit: String) {
        val loanNumber = loanNumber ?: return

        _acceptLoanOffer.value = Event(NetworkState2.Loading())
        LoanService.getInstance(token, sessionId, refreshToken).loanTopUpAcceptance(customerId, otp,
            loanAmount, loanTenure, interestRate, applicationId, lastSalaryCredit, fromPN, productType, loanNumber,
            object : ApiCallbackImpl<LoanAcceptance>(_acceptLoanOffer) {
                override fun onSuccess(data: LoanAcceptance) {
                    _acceptLoanOffer.value = Event(NetworkState2.Success(data))
                }

                override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                    _acceptLoanOffer.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                }
            })
    }

    fun rescheduleLoan(token: String, sessionId: String, refreshToken: String, customerId: Long, label: Serializable){
        _rescheduleLoan.value = Event(NetworkState2.Loading())
        LoanService.getInstance(token, sessionId, refreshToken).rescheduleLoan(customerId, label,
            object : ApiCallbackImpl<List<LoanRequest>>(_rescheduleLoan) {
                override fun onSuccess(data: List<LoanRequest>) {
                    _rescheduleLoan.value = Event(NetworkState2.Success(data))
                }
            })
    }

    fun requestRescheduleLoan(token: String, sessionId: String, refreshToken: String, customerId: Long, loanNumber: String, label: Serializable){
        _requestRescheduleLoan.value = Event(NetworkState2.Loading())
        LoanService.getInstance(token, sessionId, refreshToken).requestRescheduleLoan(customerId,loanNumber, label,
            object : ApiCallbackImpl<LoanRequest>(_requestRescheduleLoan) {
                override fun onSuccess(data: LoanRequest) {
                    _requestRescheduleLoan.value = Event(NetworkState2.Success(data))
                }

                override fun onSuccess(data: LoanRequest, message: String?) {
                    _requestRescheduleLoan.value = Event(NetworkState2.Success(data, message))
                }
            })
    }

    fun isValidLoanOffer(loanOffer: LoanOffer): Boolean {
        val (approvedAmount, tenure, emi, interestRate, applicationId) = loanOffer
        return approvedAmount.isNotEmpty() && tenure.isNotEmpty() && emi.isNotEmpty()
                && interestRate.isNotEmpty() && applicationId.isNotEmpty()
    }

    /**
     * @param amount loan amount
     * @param interest interest rate per annum
     * @param tenure total number of months / no. of installments
     */
    fun calcLoanEmi(amount: Double, interest: Double, tenure: Int): Double {
        val r = interest / (12 * 100)
        val x = Math.pow((1 + r), tenure.toDouble())
        return amount * r * x / (x - 1)
    }

    companion object {
        const val LOAN = "NEW LOAN"
        const val TOPUP_LOAN = "TOPUP LOAN"
    }
}