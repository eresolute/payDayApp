package com.fh.payday.datasource.remote.auth

import android.text.TextUtils
import com.fh.payday.datasource.models.CardLoanResult
import com.fh.payday.datasource.models.IsoAlpha3
import com.fh.payday.datasource.models.customer.CustomerResponse
import com.fh.payday.datasource.models.customer.RegisterCustomerRequest
import com.fh.payday.datasource.models.login.User
import com.fh.payday.datasource.remote.ApiCallback
import com.fh.payday.datasource.remote.ApiClient
import com.fh.payday.datasource.remote.ApiResult
import com.fh.payday.datasource.remote.asRequestBody
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.isNotEmptyList
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit

class RegistrationService private constructor() {

    companion object {
        val instance: RegistrationService by lazy { RegistrationService() }
    }

    fun submitEmiratesId(
        front: MultipartBody.Part,
        back: MultipartBody.Part,
        emiratesId: String,
        expiry: String,
        dob: String,
        nationality: String,
        isExpiryFromOCR: Boolean,
        gender: String = "",
        callback: ApiCallback<CardLoanResult>
    ) {
        val service = ApiClient.retrofit.create(AuthApiInterface::class.java)
        val call = service.submitEmiratesId(front, back, emiratesId.asRequestBody(),
            expiry.asRequestBody(), dob.asRequestBody(), nationality.asRequestBody(), gender.asRequestBody(), isExpiryFromOCR)

        call.enqueue(object : Callback<ApiResult<CardLoanResult>> {
            override fun onFailure(call: Call<ApiResult<CardLoanResult>>, t: Throwable) = callback.onFailure(t)

            override fun onResponse(call: Call<ApiResult<CardLoanResult>>, res: Response<ApiResult<CardLoanResult>>) {
                val result = res.body() ?: return callback.onFailure(Throwable(CONNECTION_ERROR))
                when {
                    "success" == result.status && result.data != null -> callback.onSuccess(result.data)
                    "error" == result.status && !result.errors.isNullOrEmpty() ->
                        callback.onError(result.errors[0], result.code)
                    else -> callback.onFailure(Throwable(CONNECTION_ERROR))
                }
            }
        })

        /*return single.subscribeOn(Schedulers.io())
            .timeout(3000, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val data = it.data ?: return@subscribe callback.onFailure(Throwable(CONNECTION_ERROR))
                when {
                    "success" == it.status -> callback.onSuccess(data)
                    "error" == it.status && !it.errors.isNullOrEmpty() -> callback.onError(it.errors[0])
                    else -> callback.onFailure(Throwable(CONNECTION_ERROR))
                }
            }, { callback.onFailure(it) })*/
    }

    fun createCustomer(secret: String, cardNumber: String, cardName: String, cardExpiry: String,
                       pin: String, emiratesId: String, callback: ApiCallback<CustomerResponse>) {
        val service = ApiClient.retrofit.create(AuthApiInterface::class.java)
        val call = service.createCustomer(secret, cardNumber, cardName, cardExpiry, pin, emiratesId)

        call.enqueue(object : Callback<ApiResult<CustomerResponse>> {
            override fun onFailure(call: Call<ApiResult<CustomerResponse>>, t: Throwable) = callback.onFailure(t)

            override fun onResponse(call: Call<ApiResult<CustomerResponse>>, response: Response<ApiResult<CustomerResponse>>) {
                val result = response.body() ?: return callback.onFailure(Throwable(CONNECTION_ERROR))

                when {
                    "success" == result.status && result.data != null -> callback.onSuccess(result.data)
                    "error" == result.status && !result.errors.isNullOrEmpty() -> callback.onError(result.errors[0],
                        result.code)
                    else -> callback.onFailure(Throwable(CONNECTION_ERROR))
                }
            }
        })
    }

    fun generateOtp(customerId: Long, mobile: String, callback: ApiCallback<String>) {
        val service = ApiClient.retrofit.create(AuthApiInterface::class.java)
        val call = service.generateOTP(customerId, mobile)

        call.enqueue(object : Callback<ApiResult<Any>> {
            override fun onFailure(call: Call<ApiResult<Any>>, t: Throwable) {
                callback.onFailure(t)
            }

            override fun onResponse(call: Call<ApiResult<Any>>, response: Response<ApiResult<Any>>) {
                val result = response.body() ?: return callback.onFailure(Throwable(CONNECTION_ERROR))

                if ("success" == result.status && !TextUtils.isEmpty(result.message)) {
                    callback.onSuccess(result.message!!)
                } else if ("error" == result.status && !result.errors.isNullOrEmpty()) {
                    callback.onError(result.errors[0], result.code)
                } else {
                    callback.onFailure(Throwable(CONNECTION_ERROR))
                }
            }

        })
    }

    fun verifyOtp(customerId: Long, pin: String, callback: ApiCallback<String>) {
        val service = ApiClient.retrofit.create(AuthApiInterface::class.java)
        val call = service.verifyOtp(customerId, pin)

        call.enqueue(object : Callback<ApiResult<Any>> {
            override fun onFailure(call: Call<ApiResult<Any>>, t: Throwable) {
                callback.onFailure(t)
            }

            override fun onResponse(call: Call<ApiResult<Any>>, response: Response<ApiResult<Any>>) {
                val result = response.body() ?: return callback.onFailure(Throwable(CONNECTION_ERROR))

                if ("success" == result.status && !TextUtils.isEmpty(result.message)) {
                    callback.onSuccess(result.message!!)
                } else if ("error" == result.status && isNotEmptyList(result.errors)) {
                    callback.onError(result.errors!![0])
                } else {
                    callback.onFailure(Throwable(CONNECTION_ERROR))
                }
            }
        })
    }

    fun searchUserId(userId: String, callback: ApiCallback<String>) {
        val service = ApiClient.retrofit.create(AuthApiInterface::class.java)
        val call = service.searchUsername(userId)

        call.enqueue(object : Callback<ApiResult<Any>> {
            override fun onFailure(call: Call<ApiResult<Any>>, t: Throwable) {
                callback.onFailure(t)
            }

            override fun onResponse(call: Call<ApiResult<Any>>, response: Response<ApiResult<Any>>) {
                val result = response.body() ?: return callback.onFailure(Throwable(CONNECTION_ERROR))

                if ("success" == result.status && !TextUtils.isEmpty(result.message)) {
                    callback.onSuccess(result.message!!)
                } else if ("error" == result.status && isNotEmptyList(result.errors)) {
                    callback.onError(result.errors!![0])
                } else {
                    callback.onFailure(Throwable(CONNECTION_ERROR))
                }
            }
        })
    }

    fun registerCredentials(customerId: Long, request: RegisterCustomerRequest, callback: ApiCallback<String>) {
        val service = ApiClient.retrofit.create(AuthApiInterface::class.java)
        val call = service.registerCustomer(customerId, request)

        call.enqueue(object : Callback<ApiResult<Any>> {
            override fun onFailure(call: Call<ApiResult<Any>>, t: Throwable) {
                callback.onFailure(t)
            }

            override fun onResponse(call: Call<ApiResult<Any>>, response: Response<ApiResult<Any>>) {
                val result = response.body() ?: return callback.onFailure(Throwable(CONNECTION_ERROR))

                if ("success" == result.status && !TextUtils.isEmpty(result.message)) {
                    callback.onSuccess(result.message!!)
                } else if ("error" == result.status && isNotEmptyList(result.errors)) {
                    callback.onError(result.errors!![0])
                }  else {
                    callback.onFailure(Throwable(CONNECTION_ERROR))
                }
            }
        })
    }

    fun registerCredentials(customerId: Long, request: RegisterCustomerRequest,
                            callback: ApiCallback<User>): Disposable {
        return submitCredentials(customerId, request)
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

    private fun submitCredentials(
        customerId: Long,
        request: RegisterCustomerRequest
    ): Single<ApiResult<User>> {
        val service = ApiClient.retrofit.create(AuthApiInterface::class.java)
        return service.submitCredentials(customerId, request).flatMap {
            val (username, password) = request
            service.observableLogin(username, password)
        }
    }

    fun getCountries(callback: ApiCallback<List<IsoAlpha3>>): Disposable {
        val service = ApiClient.retrofit.create(AuthApiInterface::class.java)
        val single = service.getCountries()

        return single.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                when {
                    "success" == it.status && it.data != null -> callback.onSuccess(it.data)
                    "error" == it.status && !it.errors.isNullOrEmpty() -> callback.onError(it.errors[0])
                    else -> callback.onFailure(Throwable(CONNECTION_ERROR))
                }
            }, {
                callback.onFailure(it)
            })
    }

    /*-------------------------------------------*/

    fun uploadSelfie(customerId: Long, selfie: MultipartBody.Part, callback: ApiCallback<String>) {
        val service = ApiClient.retrofit.create(AuthApiInterface::class.java)
        val call = service.uploadSelfie(customerId, selfie)

        call.enqueue(object : Callback<ApiResult<Any>> {
            override fun onFailure(call: Call<ApiResult<Any>>, t: Throwable) {
                callback.onFailure(t)
            }

            override fun onResponse(call: Call<ApiResult<Any>>, response: Response<ApiResult<Any>>) {
                val result = response.body() ?: return callback.onFailure(Throwable(CONNECTION_ERROR))

                if ("success" == result.status && !TextUtils.isEmpty(result.message)) {
                    callback.onSuccess(result.message!!)
                } else if ("error" == result.status && isNotEmptyList(result.errors)) {
                    callback.onError(result.errors!![0])
                } else {
                    callback.onFailure(Throwable(CONNECTION_ERROR))
                }
            }
        })
    }

    fun configTextObserver(publishSubject: PublishRelay<String>, callback: ApiCallback<String>): Disposable {
        val service = ApiClient.retrofit.create(AuthApiInterface::class.java)

        return publishSubject.debounce(400, TimeUnit.MILLISECONDS)
            .filter {
                it.trim().length > 3
            }
            .distinctUntilChanged()
            .switchMap { service.searchUserId(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if ("success" == it.status && !TextUtils.isEmpty(it.message)) {
                    callback.onSuccess(it.message!!)
                } else if ("error" == it.status && isNotEmptyList(it.errors)) {
                    callback.onError(it.errors!![0])
                } else {
                    callback.onFailure(Throwable(CONNECTION_ERROR))
                }
            }, {
                callback.onFailure(it)
            })
    }

    @Deprecated("This function is deprecated", ReplaceWith("uploadEmirates()"))
    fun submitEmiratesAndLogin (
        username: String,
        password: String,
        customerId: Long,
        front: MultipartBody.Part? = null,
        back: MultipartBody.Part? = null,
        emiratesId: String = "",
        expiry: String = "",
        dob: String = "",
        nationality: String = "",
        gender: String = "",
        callback: ApiCallback<User>
    ): Disposable {
        val single = mapToSingleELResult(username, password, customerId, front, back, emiratesId, expiry, dob, nationality, gender)
        return single.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({result ->
                val (result1, result2) = result

                if(result1 == null || result2 == null) {
                    return@subscribe callback.onFailure(Throwable(CONNECTION_ERROR))
                }

                if ("success" == result1.status && "success" == result2.status && result2.data != null) {
                    callback.onSuccess(result2.data)
                } else if ("error" == result1.status && isNotEmptyList(result1.errors)) {
                    callback.onError(result1.errors!![0])
                } else if ("error" == result2.status && isNotEmptyList(result2.errors)) {
                    callback.onError(result2.errors!![0])
                } else {
                    callback.onFailure(Throwable(CONNECTION_ERROR))
                }
            }, {
                callback.onFailure(it)
            })
    }

    @Deprecated("This function is deprecated", ReplaceWith("uploadEmirates()"))
    private fun mapToSingleELResult (
        username: String,
        password: String,
        customerId: Long,
        front: MultipartBody.Part? = null,
        back: MultipartBody.Part? = null,
        emiratesId: String = "",
        expiry: String = "",
        dob: String = "",
        nationality: String = "",
        gender: String = ""
    ): Single<EmiratesLoginResult> {
        val service = ApiClient.retrofit.create(AuthApiInterface::class.java)
        val loginObserver = service.observableLogin(username, password)
        val emiratesIdObserver = service.observableUploadEmirates(customerId, front, back, emiratesId.asRequestBody(),
            expiry.asRequestBody(), dob.asRequestBody(), nationality.asRequestBody(), gender.asRequestBody())

        return Single.zip(emiratesIdObserver, loginObserver, BiFunction { t1, t2 ->
            EmiratesLoginResult(t1, t2)
        })
    }

    @Deprecated("This class is deprecated and must be removed")
    data class EmiratesLoginResult(val result1: ApiResult<Any>?, val result2: ApiResult<User>?)

}