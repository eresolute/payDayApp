package com.fh.payday.datasource.remote.intlRemittance

import android.text.TextUtils
import com.fh.payday.datasource.models.intlRemittance.*
import com.fh.payday.datasource.remote.*
import com.fh.payday.utilities.CONNECTION_ERROR
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class IntlRemittanceService private constructor(
        var token: String? = null,
        var sessionId: String? = null,
        var refreshToken: String? = null) {

    companion object {
        private val INSTANCE: IntlRemittanceService by lazy { IntlRemittanceService() }
        fun getInstance(token: String, sessionId: String, refreshToken: String): IntlRemittanceService {
            return INSTANCE.also { it.token = token; it.sessionId = sessionId; it.refreshToken = refreshToken }
        }
    }

    fun getBeneficiaries(customerId: String, accessKey : String, deliveryMode: String, callback: ApiCallback<List<IntlBeneficiary>>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
                .create(IntlRemittanceApiInterface::class.java)
        val call = service.getBeneficiaries(customerId, accessKey, deliveryMode)
        call.enqueue(CallbackImpl(callback))
    }

    @Suppress("UNCHECKED_CAST")
    fun getBeneficiariesRx(
        customerId: String,
        exchanges: List<Exchange>,
        deliveryMode: String,
        callback: ApiCallback<List<IntlBeneficiary>>
    ): Disposable? {
        val service = ApiClient.getInstance(token ?: return null, sessionId ?: return null)
            .create(IntlRemittanceApiInterface::class.java)

        val requests = exchanges.map { e ->
            service.getBeneficiariesRx(customerId, e.accessKey, deliveryMode).subscribeOn(Schedulers.io())
        }


        return Single.zip(requests) { array -> array.map { it as? ApiResult<List<IntlBeneficiary>>? } }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe (Consumer { result ->
                val r1 = if (!result.isNullOrEmpty()) result[0] else null
                val data = result.mapNotNull { it?.data }.flatten()
                when {
                    "success".equals(r1?.status, true) -> callback.onSuccess(data)
                    r1 != null && "error".equals(r1.status, true)  && !r1.errors.isNullOrEmpty() -> {
                        callback.onError(r1.errors[0], r1.code)
                    }
                    else -> callback.onFailure(Throwable(CONNECTION_ERROR))
                }
            }, rxErrorHandler(callback))
    }

    /*fun getBeneficiariesRx(customerId: String, exchanges : List<Exchange>, callback: ApiCallback<List<IntlBeneficiary>>) : Disposable? {
        val service = ApiClient.getInstance(token ?: return null, sessionId ?: return null)
            .create(IntlRemittanceApiInterface::class.java)
        val observableList = ArrayList<Observable<ApiResult<List<IntlBeneficiary>>>>()
        exchanges.forEach {
            //observableList.add(service.getBeneficiariesRx(customerId, it.accessKey))
        }
       return Observable
            .zip(observableList) {
                // do something with those results and emit new event
                Any() // <-- Here we emit just new empty Object(), but you can emit anything
            }
            // Will be triggered if all requests will end successfully (4xx and 5xx also are successful requests too)
            .subscribe({
                //Do something on successful completion of all requests
            }) {
                //Do something on error completion of requests
            }
      *//*  Observable.zip(observableList) {

        }.subscribe(Consumer { r ->
            val data = r.data
            when {
                "success".equals(r.status, true) && data != null  -> callback.onSuccess(data)
                "error".equals(r.status, true)  && !r.errors.isNullOrEmpty() -> callback.onError(r.errors[0], r.code)
                else -> callback.onFailure(Throwable(CONNECTION_ERROR))
            }
        }, rxErrorHandler(callback))
        val observable = service.getBeneficiariesRx(customerId, "accessKey")

        return observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer { r ->
                val data = r.data
                when {
                    "success".equals(r.status, true) && data != null  -> callback.onSuccess(data)
                    "error".equals(r.status, true)  && !r.errors.isNullOrEmpty() -> callback.onError(r.errors[0], r.code)
                    else -> callback.onFailure(Throwable(CONNECTION_ERROR))
                }
            }, rxErrorHandler(callback))*//*
    }*/

    fun deleteBeneficiary(customerId: Long, accessKey: String, receiverRefNumber: String,
                          callback: ApiCallback<DeleteBeneficiary>){
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
                .create(AddBeneficiaryApiInterface::class.java)
        val call = service.deleteBeneficiary(customerId, accessKey, receiverRefNumber)
        call.enqueue(object : Callback<ApiResult<DeleteBeneficiary>>{
            override fun onFailure(call: Call<ApiResult<DeleteBeneficiary>>, t: Throwable) {
                callback.onFailure(t)
            }
            override fun onResponse(call: Call<ApiResult<DeleteBeneficiary>>, response: Response<ApiResult<DeleteBeneficiary>>) {
                val result = response.body()
                        ?: return callback.onFailure(Throwable(CONNECTION_ERROR))
                when {
                    "success".equals(result.status, true) && !TextUtils.isEmpty(result.message) ->
                        callback.onSuccess(result.data!!, result.message)
                    "error".equals(result.status, true) && !result.errors.isNullOrEmpty() ->
                        callback.onError(result.errors[0], result.code, isSessionExpired(result.code))
                    else -> callback.onFailure(Throwable(CONNECTION_ERROR))
                }
            }
        })


    }
    fun getCustomerState(customerId: String, confirmationStatus: String?, callback: ApiCallback<List<Exchange>>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
                .create(IntlRemittanceApiInterface::class.java)
        val call = service.getCustomerState(customerId, confirmationStatus)
        call.enqueue(CallbackImpl(callback))
    }

    fun generateOtp(customerId: Long, accessKey: String, sendOtp: String,  callback: ApiCallbackImpl<Otp>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(IntlRemittanceApiInterface::class.java)
        val call = service.generateOtp(customerId, accessKey, sendOtp)
        call.enqueue(CallbackImpl(callback))
    }

    fun activateCustomer(customerId: Long, accessKey: String, otp: String, callback: ApiCallbackImpl<Any>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(IntlRemittanceApiInterface::class.java)
        val call = service.activateCustomer(customerId, accessKey, otp)
        call.enqueue(CallbackImpl(callback))
    }

    fun searchCustomer(customerId: String, emiratesCardNumber: String, callback: ApiCallbackImpl<CustomerDetail>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
                .create(IntlRemittanceApiInterface::class.java)
        val call = service.searchCustomer(customerId, emiratesCardNumber)
        call.enqueue(CallbackImpl(callback))
    }

    fun purposeOfPayment(customerId: Long, accessKey : String, beneficiaryRefNumber: String, callback: ApiCallback<InitializeTransaction>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(IntlRemittanceApiInterface::class.java)
        val call = service.purposeOfPayment(customerId, accessKey, beneficiaryRefNumber)
        call.enqueue(CallbackImpl(callback))
    }


}