package com.fh.payday.datasource.remote.loan

import com.fh.payday.datasource.models.loan.LoanAcceptance
import com.fh.payday.datasource.models.loan.LoanOffer
import com.fh.payday.datasource.models.loan.LoanRequest
import com.fh.payday.datasource.remote.*
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.views2.loan.LoanServiceRequestActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.HttpException
import java.io.Serializable

class LoanService private constructor(
    var token: String? = null,
    var sessionId: String? = null,
    var refreshToken: String? = null
) {

    companion object {
        private val instance: LoanService by lazy { LoanService() }

        fun getInstance(token: String, sessionId: String, refreshToken: String): LoanService {
            return instance.also { it.token = token; it.sessionId = sessionId; it.refreshToken = refreshToken }
        }
    }

    fun requestLoan(customerId: Long, productType: String, callback: ApiCallback<LoanOffer>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(LoanApiInterface::class.java)
        val call = service.requestLoan(customerId, productType)
        call.enqueue(CallbackImpl(callback))
    }

    fun customizeLoan(customerId: Long, loanAmount: String, loanTenure: String, productType: String,
                      callback: ApiCallback<LoanOffer>): Disposable? {
        val loanService = ApiClient.getInstance(token ?: return null, sessionId ?: return null)
            .create(LoanApiInterface::class.java)

        return loanService.customizeLoan(customerId, loanAmount, loanTenure, productType)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val data = it.data
                            ?: return@subscribe callback.onFailure(Throwable(CONNECTION_ERROR))
                    when {
                        "success" == it.status -> callback.onSuccess(data)
                        "error" == it.status && !it.errors.isNullOrEmpty() -> callback.onError(it.errors[0])
                        else -> callback.onFailure(Throwable(CONNECTION_ERROR))
                    }
                }, {
                    when(it) {
                        is HttpException -> {
                            val errorResult = parseErrorBody(it.response()?.errorBody()?.string())
                            val message = when {
                                errorResult?.errors != null && !errorResult.errors.isNullOrEmpty() -> errorResult.errors[0]
                                else -> it.message() ?: CONNECTION_ERROR
                            }
                            callback.onError(message, errorResult?.code ?: it.code(),
                                isSessionExpired(errorResult?.code))
                        }
                        else-> callback.onFailure(Throwable(CONNECTION_ERROR))
                    }
                })
    }

    fun loanAcceptance(customerId: Long, otp: String, loanAmount: String, loanTenure: String,
                       interestRate: String, applicationId: String, lastSalaryCredit: String,
                       fromPN: Boolean, productType: String, callback: ApiCallback<LoanAcceptance>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(LoanApiInterface::class.java)
        val call = service.acceptLoanOffer(customerId, otp, loanAmount, loanTenure, interestRate,
                applicationId, lastSalaryCredit, fromPN, productType)
        call.enqueue(CallbackImpl(callback))
    }

    fun loanTopUpAcceptance(customerId: Long, otp: String, loanAmount: String, loanTenure: String,
                            interestRate: String, applicationId: String, lastSalaryCredit: String,
                            fromPN: Boolean, productType: String, loanNumber: String,
                            callback: ApiCallback<LoanAcceptance>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(LoanApiInterface::class.java)
        val call = service.acceptLoanTopUpOffer(customerId, otp, loanAmount, loanTenure, interestRate,
                applicationId, lastSalaryCredit, fromPN, productType, loanNumber)
        call.enqueue(CallbackImpl(callback))
    }

    fun rescheduleLoan(customerId: Long, label: Serializable, callback: ApiCallback<List<LoanRequest>>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(LoanApiInterface::class.java)
        val call: Call<ApiResult<List<LoanRequest>>> =
                when (label) {
                    LoanServiceRequestActivity.TYPE.RESCHEDULE -> service.rescheduleLoan(customerId)
                    LoanServiceRequestActivity.TYPE.E_SETTLEMENT -> service.earlySettlement(customerId)
                    LoanServiceRequestActivity.TYPE.LIABILITY -> service.liability(customerId)
                    LoanServiceRequestActivity.TYPE.CLEARANCE -> service.clearanceLetter(customerId)
                    LoanServiceRequestActivity.TYPE.P_SETTLEMENT -> service.partialSettlement(customerId)
                    else -> service.rescheduleLoan(customerId)
                }
        call.enqueue(CallbackImpl(callback))
    }

    fun requestRescheduleLoan(customerId: Long, loanNumber: String, label: Serializable, callback: ApiCallback<LoanRequest>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(LoanApiInterface::class.java)

        val call: Call<ApiResult<LoanRequest>> =
                when (label) {
                    LoanServiceRequestActivity.TYPE.RESCHEDULE -> service.requestRescheduleLoan(customerId,loanNumber)
                    LoanServiceRequestActivity.TYPE.E_SETTLEMENT -> service.requestEarlySettlement(customerId, loanNumber)
                    LoanServiceRequestActivity.TYPE.LIABILITY -> service.requestLiability(customerId, loanNumber)
                    LoanServiceRequestActivity.TYPE.CLEARANCE -> service.requestClearanceLetter(customerId, loanNumber)
                    LoanServiceRequestActivity.TYPE.P_SETTLEMENT -> service.requestPartialSettlement(customerId, loanNumber)
                    else -> service.requestRescheduleLoan(customerId, loanNumber)
                }
        call.enqueue(object : CallbackImpl<LoanRequest>(callback) {
            override fun onResponse(result: ApiResult<LoanRequest>, warning: String?) {
                when {
                    "success" == result.status && result.data != null -> onSuccess(result.data, result.message)
                    else -> super.onResponse(result, warning)
                }
            }

        })

    }
}