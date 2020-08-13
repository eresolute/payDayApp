package com.fh.payday.datasource.remote.payments

import com.fh.payday.datasource.models.IndianState
import com.fh.payday.datasource.models.payments.*
import com.fh.payday.datasource.remote.*
import com.fh.payday.utilities.CONNECTION_ERROR
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException

class PaymentService private constructor(
        var token: String? = null,
        var sessionId: String? = null,
        var refreshToken: String? = null
) {

    companion object {
        private val instance: PaymentService by lazy { PaymentService() }

        fun getInstance(token: String, sessionId: String, refreshToken: String): PaymentService {
            return instance.also { it.token = token; it.sessionId = sessionId; it.refreshToken = refreshToken }
        }
    }

    fun getOperators(customerId: Long, method: String, typeId: Int, countryCode: String,
                     callback: ApiCallback<List<Operator>>) {
        val call = ApiClient.getInstance(token ?: return, sessionId
                ?: return).create(PaymentApiInterface::class.java)
                .getOperators(customerId, method, typeId, countryCode)
        call.enqueue(CallbackImpl(callback))
    }

    fun getOperatorDetail(customerId: Long, accessKey: String, typeId: Int, planType: String,
                          countryCode: String, callback: ApiCallback<OperatorDetail>) {
        val call = ApiClient.getInstance(token ?: return, sessionId
                ?: return).create(PaymentApiInterface::class.java)
                .getOperatorDetail(customerId, accessKey, typeId, planType, countryCode)
        call.enqueue(CallbackImpl(callback))
    }

    fun getOperatorDetailFixed(customerId: Long, accessKey: String, typeId: Int, planType: String,
                               countryCode: String,  optional1: String = "", callback: ApiCallback<List<OperatorDetailFixed>>) {
        val call = ApiClient.getInstance(token ?: return, sessionId
                ?: return).create(PaymentApiInterface::class.java)
                .getOperatorDetailFixed(customerId, accessKey,
                        typeId, planType, optional1, countryCode)
        call.enqueue(CallbackImpl(callback))
    }

    fun getPayableAmount(customerId: Long, accessKey: String, typeKey: Int, flexiKey: String,
                         amount: Double, callback: ApiCallback<PayableAmount>) {
        val call = ApiClient.getInstance(token ?: return, sessionId
                ?: return).create(PaymentApiInterface::class.java)
                .getPayableAmount(customerId, accessKey, typeKey,
                        flexiKey, amount)
        call.enqueue(CallbackImpl(callback))
    }

    fun getBalance(customerId: Long, accessKey: String, typeId: Int, account: String,
                   flexiKey: String, typeKey: Int, optional1: String, callback: ApiCallback<Bill>): Disposable? {
        return ApiClient.getInstance(token ?: return null, sessionId
                ?: return null).create(PaymentApiInterface::class.java)
                .getBalance(customerId, accessKey, typeId, account, flexiKey, typeKey, optional1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    when {
                        "success" == it.status && it.data != null -> callback.onSuccess(it.data)
                        "error" == it.status && !it.errors.isNullOrEmpty() -> callback.onError(it.errors[0], it.code,
                                isSessionExpired(it.code))
                        else -> callback.onFailure(Throwable(CONNECTION_ERROR))
                    }
                }, {
                    when (it) {
                        is HttpException -> {
                            val errorResult = parseErrorBody(it.response()?.errorBody()?.string())
                            val message = when {
                                errorResult?.errors != null && !errorResult.errors.isNullOrEmpty() -> errorResult.errors[0]
                                else -> it.message() ?: CONNECTION_ERROR
                            }
                            callback.onError(message, errorResult?.code ?: it.code(),
                                    isSessionExpired(errorResult?.code))
                        }
                        else -> callback.onFailure(Throwable(CONNECTION_ERROR))
                    }
                })
    }

    fun getTopUpDetail(customerId: Long, accessKey: String, method: String, typeId: Int,
                       account: String, flexKey: String, typeKey: Int, option: String,
                       callback: ApiCallback<RechargeDetail>) {
        val call = ApiClient.getInstance(token ?: return, sessionId
                ?: return).create(PaymentApiInterface::class.java)
                .getRechargeDetail(customerId, accessKey, method,
                        typeId, account, flexKey, typeKey, option)
        call.enqueue(CallbackImpl(callback))
    }

    fun getBillBalanceEtisalat(customerId: Long, accessKey: String, method: String, typeId: Int,
                               account: String, flexKey: String, typeKey: Int, option: String,
                               callback: ApiCallback<BillDetailEtisalat>) {
        val call = ApiClient.getInstance(token ?: return, sessionId
                ?: return).create(PaymentApiInterface::class.java)
                .getBillAmountEtisalat(customerId, accessKey,
                        method, typeId, account, flexKey, typeKey, option)
        call.enqueue(CallbackImpl(callback))
    }

    fun getBillBalanceDu(customerId: Long, accessKey: String, method: String, typeId: Int,
                         account: String, flexiKey: String, typeKey: Int,
                         callback: ApiCallback<BillDetailDU>) {
        val call = ApiClient.getInstance(token ?: return, sessionId
                ?: return).create(PaymentApiInterface::class.java)
                .getBillAmountDu(customerId, accessKey, method, typeId,
                        account, flexiKey, typeKey)
        call.enqueue(CallbackImpl(callback))
    }

    fun payBill(accessKey: String, customerId: Int, typeKey: Int, flexKey: String, transactionId: String,
                account: String, amount: String, serviceType: String,
                providerId: String, otp: String, callback: ApiCallback<BillPaymentResponse>) {
        val call = ApiClient.getInstance(token ?: return, sessionId
                ?: return).create(PaymentApiInterface::class.java)
                .payBill(accessKey, customerId, typeKey, flexKey,
                        transactionId, account, amount, accessKey, serviceType, providerId, amount, otp)
        call.enqueue(CallbackImpl(callback))
    }

    fun payBillDu(accessKey: String, customerId: Int, typeKey: Int, flexKey: String, transactionId: String,
                  account: String, amount: String, option1: String, option2: String?, otp: String,
                  callback: ApiCallback<BillPaymentDuResponse>) {
        val call = ApiClient.getInstance(token ?: return, sessionId
                ?: return).create(PaymentApiInterface::class.java)
                .payBillDu(accessKey, customerId, typeKey, flexKey,
                        transactionId, account, amount, option1, option2, otp)
        call.enqueue(CallbackImpl(callback))
    }

    fun getOtp(customerId: Int, callback: ApiCallback<String>) {
        val call = ApiClient.getInstance(token ?: return, sessionId
                ?: return).create(PaymentApiInterface::class.java)
                .getOtp(customerId)

        call.enqueue(CallbackStringImpl(callback))
    }

    fun makePayment(customerId: Long, paymentReq: PaymentRequest, callback: ApiCallback<PaymentResult>) {
        val (_, _, flexiKey, planId) = paymentReq
        when {
            flexiKey != null -> makePaymentFlexi(customerId, paymentReq, callback)
            planId != null -> makePaymentFixed(customerId, paymentReq, callback)
            else -> callback.onFailure(Throwable("Required field(s) missing"))
        }
    }

    private fun makePaymentFlexi(customerId: Long, paymentReq: PaymentRequest, callback: ApiCallback<PaymentResult>) {
        val (accessKey, typeKey, flexiKey, _, transId,
                account, amount, otp,
                optional1, optional2, optional3, optional4) = paymentReq
        val call = ApiClient.getInstance(token ?: return, sessionId
                ?: return).create(PaymentApiInterface::class.java)
                .makePaymentFlexi(accessKey, customerId, typeKey, flexiKey!!,
                        transId, account, amount, otp,
                        optional1 ?: "", optional2 ?: "", optional3 ?: "", optional4 ?: "")

        call.enqueue(CallbackImpl(callback))
    }

    private fun makePaymentFixed(customerId: Long, paymentReq: PaymentRequest, callback: ApiCallback<PaymentResult>) {
        val (accessKey, typeKey, _, planId, transId, account,
                amount, otp, optional1) = paymentReq
        val call = ApiClient.getInstance(token ?: return, sessionId
                ?: return).create(PaymentApiInterface::class.java)
                .makePaymentFixed(accessKey, customerId, typeKey, planId!!,
                        transId, account, amount, otp, optional1 ?: "")

        call.enqueue(CallbackImpl(callback))
    }

     fun makePaymentMawaqif(customerId: Long, paymentReq: PaymentRequest, callback: ApiCallback<PaymentResult>) {

        val (accessKey, typeKey, flexiKey, _, transId,
                account, amount, otp) = paymentReq

        val call = ApiClient.getInstance(token ?: return, sessionId
                ?: return).create(PaymentApiInterface::class.java)
                .makePaymentFlexi(accessKey, customerId, typeKey, flexiKey!!,
                        transId, account, amount, otp)
        call.enqueue(CallbackImpl(callback))
    }

    fun getRecentAccounts(
            customerId: Long,
            accessKey: String,
            callback: ApiCallback<ArrayList<RecentAccount>>
    ): Disposable? {
        return ApiClient.getInstance(token ?: return null, sessionId
                ?: return null).create(PaymentApiInterface::class.java)
                .getRecentAccounts(customerId, accessKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    when {
                        "success" == it.status && it.data != null -> callback.onSuccess(it.data)
                        "error" == it.status && !it.errors.isNullOrEmpty() -> callback.onError(it.errors[0])
                        else -> callback.onFailure(Throwable(CONNECTION_ERROR))
                    }
                }, {
                    callback.onFailure(Throwable(CONNECTION_ERROR))
                })
    }

    fun getBeneficiaries(customerId: Long, accessKey: String, callback: ApiCallback<ArrayList<Beneficiaries>>) {
        val call = ApiClient.getInstance(token ?: return, sessionId ?: return)
                .create(PaymentApiInterface::class.java)
                .getBeneficiaries(customerId, accessKey)
        call.enqueue(CallbackImpl(callback))
    }

    fun add2Beneficiary(customerId: Long, mobileNumber: String, shortName: String,
                        accessKey: String, option1: String? = null, option2: String? = null,
                        type: String? = null, callback: ApiCallback<String>) {
        val call = ApiClient.getInstance(token ?: return, sessionId ?: return)
                .create(PaymentApiInterface::class.java)
                .add2Beneficiary(customerId, mobileNumber, shortName, accessKey, option1, option2, type)
        call.enqueue(CallbackStringImpl(callback))
    }

    fun editBeneficiary(customerId: Long, beneficiaryId: Long, mobileNumber: String, shortName: String,
                        accessKey: String, callback: ApiCallback<String>) {
        val call = ApiClient.getInstance(token ?: return, sessionId ?: return)
                .create(PaymentApiInterface::class.java)
                .editBeneficiary(customerId, beneficiaryId, mobileNumber, shortName, accessKey)
        call.enqueue(CallbackStringImpl(callback))
    }

    fun deleteBeneficiary(customerId: Long, beneficiaryId: Long, callback: ApiCallback<String>) {
        val call = ApiClient.getInstance(token ?: return, sessionId ?: return)
                .create(PaymentApiInterface::class.java)
                .deleteBeneficiary(customerId, beneficiaryId)
        call.enqueue(CallbackStringImpl(callback))
    }

    fun enableBeneficiary(customerId: Long, beneficiaryId: Long, enable: Boolean,
                          callback: ApiCallbackImpl<String>) {
        val call = ApiClient.getInstance(token ?: return, sessionId ?: return)
                .create(PaymentApiInterface::class.java)
                .enableBeneficiary(customerId, beneficiaryId, enable)
        call.enqueue(CallbackStringImpl(callback))
    }

    fun getIndianStates(customerId: Long, method: String, callback: ApiCallback<List<IndianState>>) {
        val call = ApiClient.getInstance(token ?: return, sessionId
                ?: return).create(PaymentApiInterface::class.java)
                .getIndianStates(customerId, method)
        call.enqueue(CallbackImpl(callback))
    }

}