package com.fh.payday.datasource.remote.beneficiaries.localbeneficiary

import com.fh.payday.datasource.models.moneytransfer.LocalBeneficiary
import com.fh.payday.datasource.remote.*

class LocalBeneficiaryService private constructor(
        var token: String? = null,
        var sessionId: String? = null,
        var refreshToken: String? = null
){

    companion object {
        private val instance: LocalBeneficiaryService by lazy { LocalBeneficiaryService() }

        fun getInstance(token: String, sessionId: String, refreshToken: String): LocalBeneficiaryService {
            return instance.also { it.token = token; it.sessionId = sessionId; it.refreshToken = refreshToken }
        }
    }

    fun getBankList(customerId: Long, callback: ApiCallback<List<String>>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return).create(LocalBeneficiaryApiInterfaces::class.java)
        val call = service.getBankList(customerId)

        call.enqueue(CallbackImpl(callback))

//        call.enqueue(object : Callback<ApiResult<List<String>>> {
//            override fun onFailure(call: Call<ApiResult<List<String>>>, t: Throwable) {
//                callback.onFailure(t)
//            }
//
//            override fun onResponse(call: Call<ApiResult<List<String>>>, response: Response<ApiResult<List<String>>>) {
//                val result = response.body() ?: return callback.onFailure(Throwable(CONNECTION_ERROR))
//
//                if ("success" == result.status && result.data != null) {
//                    callback.onSuccess(result.data)
//                } else if ("error" == result.status && isNotEmptyList(result.errors)) {
//                    callback.onError(result.errors!![0])
//                }  else {
//                    callback.onFailure(Throwable(CONNECTION_ERROR))
//                }
//            }
//        })
    }

    fun getIban(customerId: Long, bankName: String, accountNumber: String, callback: ApiCallback<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return).create(LocalBeneficiaryApiInterfaces::class.java)
        val call = service.getIban(customerId, bankName, accountNumber)

        call.enqueue(CallbackImpl(callback))

//        call.enqueue(object : Callback<ApiResult<String>> {
//            override fun onFailure(call: Call<ApiResult<String>>, t: Throwable) {
//                callback.onFailure(t)
//            }
//
//            override fun onResponse(call: Call<ApiResult<String>>, response: Response<ApiResult<String>>) {
//                val result = response.body() ?: return callback.onFailure(Throwable(CONNECTION_ERROR))
//
//                if ("success" == result.status && result.data != null) {
//                    callback.onSuccess(result.data)
//                } else if ("error" == result.status && !result.errors.isNullOrEmpty()) {
//                    callback.onError(result.errors[0])
//                }  else {
//                    callback.onFailure(Throwable(CONNECTION_ERROR))
//                }
//            }
//
//        })
    }

    fun addBeneficiary(customerId: Long, mobileNo: String, name: String, accountNo: String, iban: String, bankName: String, otp: String,
                       callback: ApiCallback<LocalBeneficiary>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return).create(LocalBeneficiaryApiInterfaces::class.java)
        val call = service.addBeneficiary(customerId, mobileNo, name, accountNo, iban, bankName, otp)

//        call.enqueue(CallbackImpl(callback))

        call.enqueue(object : CallbackImpl<LocalBeneficiary>(callback) {
            override fun onResponse(result: ApiResult<LocalBeneficiary>, warning: String?) {
                when {
                    "error" == result.status && !result.errors.isNullOrEmpty()-> {
                        callback.onError(result.errors[0], result.code, isSessionExpired(result.code))
                    }
                    else -> super.onResponse(result, warning)
                }
            }
        })

//        call.enqueue(object : Callback<ApiResult<LocalBeneficiary>> {
//            override fun onFailure(call: Call<ApiResult<LocalBeneficiary>>, t: Throwable) {
//                callback.onFailure(t)
//            }
//
//            override fun onResponse(call: Call<ApiResult<LocalBeneficiary>>, response: Response<ApiResult<LocalBeneficiary>>) {
//                val result = response.body() ?: return callback.onFailure(Throwable(CONNECTION_ERROR))
//
//                if ("success" == result.status && result.data != null) {
//                    callback.onSuccess(result.data)
//                } else if ("error" == result.status && isNotEmptyList(result.errors)) {
//                    callback.onError(result.errors!![0])
//                }  else {
//                    callback.onFailure(Throwable(CONNECTION_ERROR))
//                }
//            }
//
//        })
    }

    fun editBeneficiary(customerId: Long, beneficiaryId: Int, mobileNo: String, name: String, accountNo: String, iban: String, bankName: String, otp: String, callback: ApiCallback<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return).create(LocalBeneficiaryApiInterfaces::class.java)
        val call = service.editBeneficiary(customerId, beneficiaryId, mobileNo, name, accountNo, iban, bankName, otp)

//        call.enqueue(CallbackStringImpl(callback))
        call.enqueue(object : CallbackStringImpl<String>(callback) {
            override fun onResponse(result: ApiResult<String>, warning: String?) {
                when {
                    "error" == result.status && !result.errors.isNullOrEmpty()-> {
                        callback.onError(result.errors[0], result.code, isSessionExpired(result.code))
                    }
                    else -> super.onResponse(result, warning)
                }
            }
        })

//        call.enqueue(object : Callback<ApiResult<Any>> {
//            override fun onFailure(call: Call<ApiResult<Any>>, t: Throwable) {
//                callback.onFailure(t)
//            }
//
//            override fun onResponse(call: Call<ApiResult<Any>>, response: Response<ApiResult<Any>>) {
//                val result = response.body() ?: return callback.onFailure(Throwable(CONNECTION_ERROR))
//
//                if ("success" == result.status) {
//                    callback.onSuccess(result.message!!)
//                } else if ("error" == result.status && isNotEmptyList(result.errors)) {
//                    callback.onError(result.errors!![0])
//                } else {
//                    callback.onFailure(Throwable(CONNECTION_ERROR))
//                }
//            }
//        })
    }

    fun getOtp(customerId: Long, callback: ApiCallback<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return).create(LocalBeneficiaryApiInterfaces::class.java)
        val call = service.getOtp(customerId)

        call.enqueue(CallbackStringImpl(callback))

//        call.enqueue(object : Callback<ApiResult<Any>> {
//            override fun onFailure(call: Call<ApiResult<Any>>, t: Throwable) {
//                callback.onFailure(t)
//            }
//
//            override fun onResponse(call: Call<ApiResult<Any>>, response: Response<ApiResult<Any>>) {
//                val result = response.body()?: return callback.onFailure(Throwable(CONNECTION_ERROR))
//                if ("success" == result.status && !TextUtils.isEmpty(result.message)) {
//                    callback.onSuccess(result.message!!)
//                } else if ("error" == result.status && isNotEmptyList(result.errors)) {
//                    callback.onError(result.errors!![0])
//                } else {
//                    callback.onFailure(Throwable(CONNECTION_ERROR))
//                }
//            }
//        })
    }

    fun deleteBeneficiary(customerId: Long, beneficiaryId: Int, otp: String, callback: ApiCallback<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return).create(LocalBeneficiaryApiInterfaces::class.java)
        val call = service.deleteBeneficiary(customerId, beneficiaryId, otp)
        call.enqueue(CallbackStringImpl(callback))

//        call.enqueue(object : Callback<ApiResult<Any>> {
//            override fun onFailure(call: Call<ApiResult<Any>>, t: Throwable) {
//                callback.onFailure(t)
//            }
//            override fun onResponse(call: Call<ApiResult<Any>>, response: Response<ApiResult<Any>>) {
//                val result = response.body()?: return callback.onFailure(Throwable(CONNECTION_ERROR))
//                if ("success" == result.status && !TextUtils.isEmpty(result.message)) {
//                    callback.onSuccess(result.message!!)
//                } else if ("error" == result.status && isNotEmptyList(result.errors)) {
//                    callback.onError(result.errors!![0])
//                } else {
//                    callback.onFailure(Throwable(CONNECTION_ERROR))
//                }
//            }
//        })
    }

    fun enableBeneficiary(customerId: Long, beneficiaryId: Int, enable: Int, otp: String, callback: ApiCallback<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return).create(LocalBeneficiaryApiInterfaces::class.java)
        val call = service.enableBeneficiary(customerId, beneficiaryId, enable, otp)

        call.enqueue(CallbackStringImpl(callback))

//        call.enqueue(object : Callback<ApiResult<Any>> {
//            override fun onFailure(call: Call<ApiResult<Any>>, t: Throwable) {
//                callback.onFailure(t)
//            }
//            override fun onResponse(call: Call<ApiResult<Any>>, response: Response<ApiResult<Any>>) {
//                val result = response.body()?: return callback.onFailure(Throwable(CONNECTION_ERROR))
//                if ("success" == result.status && !TextUtils.isEmpty(result.message)) {
//                    callback.onSuccess(result.message!!)
//                } else if ("error" == result.status && isNotEmptyList(result.errors)) {
//                    callback.onError(result.errors!![0])
//                } else {
//                    callback.onFailure(Throwable(CONNECTION_ERROR))
//                }
//            }
//        })
    }
}