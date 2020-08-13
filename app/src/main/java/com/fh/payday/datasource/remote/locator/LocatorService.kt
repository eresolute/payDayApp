package com.fh.payday.datasource.remote.locator

import com.fh.payday.datasource.models.BranchLocator
import com.fh.payday.datasource.models.IntlBranchLocator
import com.fh.payday.datasource.models.intlRemittance.Exchange
import com.fh.payday.datasource.remote.*
import com.fh.payday.utilities.CONNECTION_ERROR
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

class LocatorService private constructor(
        var token: String? = null,
        var sessionId: String? = null,
        var refreshToken: String? = null
) {

    companion object {
        private val instance: LocatorService by lazy { LocatorService() }

        fun getInstance(token: String, sessionId: String, refreshToken: String): LocatorService {
            return instance.also { it.token = token; it.sessionId = sessionId; it.refreshToken = refreshToken }
        }
    }

    fun locators(loginType: String,customerId: Long, locatorType: String, callback: ApiCallback<List<BranchLocator>>) {
        if (loginType == "preLogin"){
            val service = ApiClient.retrofit.create(BranchATMApiInterface::class.java)
            val call = service.locators(locatorType)
            call.enqueue(CallbackImpl(callback))
        }else {
            val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
                    .create(LocatorApiInterface::class.java)
            val call = service.locators(customerId, locatorType)
            call.enqueue(CallbackImpl(callback))
        }
    }

    fun locateExchange(customerId: Long, callback: ApiCallbackImpl<List<IntlBranchLocator>>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return)
                .create(LocatorApiInterface::class.java)
        val call = service.locateExchange(customerId)
        call.enqueue(CallbackImpl(callback))
    }

    @Suppress("UNCHECKED_CAST")
    fun locateExchangeRx(
        customerId: Long,
        exchanges: List<Exchange>,
        callback: ApiCallback<List<IntlBranchLocator>>
    ): Disposable? {
        val service = ApiClient.getInstance(token ?: return null, sessionId ?: return null)
            .create(LocatorApiInterface::class.java)

        val requests = exchanges.map { e ->
            service.locateExchangeRx(customerId, e.accessKey).subscribeOn(Schedulers.io())
        }


        return Single.zip(requests) { array -> array.map { it as? ApiResult<List<IntlBranchLocator>>? } }
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

}