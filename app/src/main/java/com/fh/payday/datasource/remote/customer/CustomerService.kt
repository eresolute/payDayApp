package com.fh.payday.datasource.remote.customer

import com.fh.payday.datasource.models.CustomerSummary
import com.fh.payday.datasource.remote.*
import com.fh.payday.services.InteractionObserver
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

class CustomerService private constructor(
    var token: String? = null,
    var sessionId: String? = null,
    var refreshToken: String? = null
) {

    private val disposable by lazy { CompositeDisposable() }

    companion object {

        fun clearDisposable() {
            instance.clearDisposable()
        }

        private val instance: CustomerService by lazy { CustomerService() }

        fun getInstance(token: String?, sessionId: String?, refreshToken: String?): CustomerService {
            return instance.also { it.token = token; it.sessionId = sessionId; it.refreshToken = refreshToken }
        }
    }

    fun getSummary(customerId: Long, callback: ApiCallback<CustomerSummary>) {
        val holder = getServiceHolder(customerId, refreshToken ?: return)
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return, holder)
            .create(CustomerApiInterface::class.java)

        val call = service.getSummary(customerId)
        call.enqueue(object: CallbackImpl<CustomerSummary>(callback) {
            override fun onResponse(result: ApiResult<CustomerSummary>, warning: String?) {
                when {
                    result.code == 399 || result.code == 203 -> callback.onError(result.code.toString())
                    else -> super.onResponse(result, warning)
                }
            }
        })
    }

    fun getSummaryRx(customerId: Long): Single<ApiResult<CustomerSummary>>? {
        val holder = getServiceHolder(customerId, refreshToken ?: return null)
        val service = ApiClient.getInstance(token ?: return null,
            sessionId ?: return null, holder).create(CustomerApiInterface::class.java)

        return service.getSummaryRx(customerId)
    }

    fun resetSession(
        customerId: Long,
        sessionTimeout: Long,
        callback: ApiCallback<String>
    ) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
            .create(CustomerApiInterface::class.java)

        val d = Observable.interval(sessionTimeout, TimeUnit.SECONDS, Schedulers.io())
            .subscribe {
                if (InteractionObserver.status) {
                    service.resetSession(customerId)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            InteractionObserver.status = false
                            when {
                                "error" == it.status && !it.errors.isNullOrEmpty() ->
                                    callback.onError(it.errors[0], it.code, isSessionExpired(it.code))
                            }
                        }, {
                            InteractionObserver.status = false
                            when(it) {
                                is HttpException -> {
                                    val errorResult = parseErrorBody(it.response()?.errorBody()?.string())
                                    if (errorResult?.errors != null && !errorResult.errors.isNullOrEmpty()) {
                                        val message = errorResult.errors[0]
                                        callback.onError(message, errorResult.code, isSessionExpired(errorResult.code))
                                    }
                                }
                            }
                        })
                }
            }

        disposable.add(d)
    }

    fun readNotifications(customerId: Long, notificationId: List<String>, callback: ApiCallback<String>){
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return).create(CustomerApiInterface::class.java)
        val call = service.readNotifications(customerId, notificationId)
        call.enqueue(CallbackStringImpl(callback))
    }

    fun clearDisposable() {
        disposable.clear()
    }
}