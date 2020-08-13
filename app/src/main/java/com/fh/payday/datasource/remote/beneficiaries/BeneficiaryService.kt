package com.fh.payday.datasource.remote.beneficiaries

import android.text.TextUtils
import com.fh.payday.datasource.models.moneytransfer.AddP2CBeneficiaryResponse
import com.fh.payday.datasource.models.moneytransfer.AddP2PBeneficiaryResponse
import com.fh.payday.datasource.models.moneytransfer.PaydayBeneficiary
import com.fh.payday.datasource.remote.*
import com.fh.payday.datasource.remote.moneytransfer.TransferService
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.isNotEmptyList
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BeneficiaryService private constructor(
        var token: String? = null,
        var sessionId: String? = null,
        var refreshToken: String? = null
) {
    companion object {
        val instance: BeneficiaryService by lazy { BeneficiaryService() }
        fun getInstance(token: String, sessionId: String, refreshToken: String): BeneficiaryService {
            return instance.also { it.token = token; it.sessionId = sessionId; it.refreshToken = refreshToken }
        }
    }

    fun addP2CBeneficiaryService(id: Long, cardNumber: String, name: String, shortName: String, otp: String, callback: ApiCallback<AddP2CBeneficiaryResponse>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return).create(BeneficiaryApiInterface::class.java)
        val call = service.addP2CBeneficiary(id, cardNumber, name, shortName, otp)

        call.enqueue(object : Callback<ApiResult<AddP2CBeneficiaryResponse>> {
            override fun onFailure(call: Call<ApiResult<AddP2CBeneficiaryResponse>>, t: Throwable) {
                callback.onFailure(t)
            }

            override fun onResponse(call: Call<ApiResult<AddP2CBeneficiaryResponse>>, response: Response<ApiResult<AddP2CBeneficiaryResponse>>) {
                val result = response.body() ?: return callback.onFailure(Throwable(CONNECTION_ERROR))

                if ("success" == result.status && result.data != null) {
                    callback.onSuccess(result.data)
                } else if ("error" == result.status && isNotEmptyList(result.errors)) {
                    callback.onError(result.errors!![0])
                }  else {
                    callback.onFailure(Throwable(CONNECTION_ERROR))
                }
            }

        })
    }

    fun addP2PBeneficiaryService(id: Long, mobileNo: String, otp: String, callback: ApiCallback<AddP2PBeneficiaryResponse>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return).create(BeneficiaryApiInterface::class.java)
        val call = service.addP2PBeneficiary(id,  mobileNo, otp)

//        call.enqueue(CallbackImpl(callback))

        call.enqueue(object : CallbackImpl<AddP2PBeneficiaryResponse>(callback) {
            override fun onResponse(result: ApiResult<AddP2PBeneficiaryResponse>, warning: String?) {
                when {
                    "error" == result.status && !result.errors.isNullOrEmpty()-> {
                        callback.onError(result.errors[0], result.code, isSessionExpired(result.code))
                    }
                    else -> super.onResponse(result, warning)
                }
            }
        })
    }

    fun getBeneficiaryDetail(id: Long, mobileNo: String, callback: ApiCallback<PaydayBeneficiary>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return).create(BeneficiaryApiInterface::class.java)
        val call = service.getBeneficiaryDetail(id, mobileNo)

        call.enqueue(object : Callback<ApiResult<PaydayBeneficiary>> {
            override fun onFailure(call: Call<ApiResult<PaydayBeneficiary>>, t: Throwable) {
                callback.onFailure(t)
            }

            override fun onResponse(call: Call<ApiResult<PaydayBeneficiary>>, response: Response<ApiResult<PaydayBeneficiary>>) {
                val result = response.body() ?: return callback.onFailure(Throwable(CONNECTION_ERROR))

                if ("success" == result.status && result.data != null) {
                    callback.onSuccess(result.data)
                } else if("error" == result.status && isNotEmptyList(result.errors)) {
                    callback.onError(result.errors!![0], result.code)
                } else {
                    callback.onFailure(Throwable(CONNECTION_ERROR))
                }
            }

        })
    }

    fun generateOtp(customerId: Int, callback: ApiCallback<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return).create(BeneficiaryApiInterface::class.java)
        val call = service.getOtp(customerId)

        call.enqueue(object : Callback<ApiResult<Any>> {
            override fun onFailure(call: Call<ApiResult<Any>>, t: Throwable) = callback.onFailure(t)

            override fun onResponse(call: Call<ApiResult<Any>>, response: Response<ApiResult<Any>>) {
                val result = response.body()?: return callback.onFailure(Throwable(CONNECTION_ERROR))
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

    fun editBeneficiaryService(customerId: Long, beneficiaryId: Int, cardNumber: String, shortName: String, otp: String, callback: ApiCallback<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return).create(BeneficiaryApiInterface::class.java)
        val call = service.editBeneficiary(customerId, beneficiaryId, cardNumber, shortName, otp)

        call.enqueue(object : Callback<ApiResult<Any>> {
            override fun onFailure(call: Call<ApiResult<Any>>, t: Throwable) {
                callback.onFailure(t)
            }

            override fun onResponse(call: Call<ApiResult<Any>>, response: Response<ApiResult<Any>>) {
                val result = response.body() ?: return callback.onFailure(Throwable(CONNECTION_ERROR))

                if ("success" == result.status) {
                    callback.onSuccess(result.message!!)
                } else if ("error" == result.status && isNotEmptyList(result.errors)) {
                    callback.onError(result.errors!![0])
                }  else {
                    callback.onFailure(Throwable(CONNECTION_ERROR))
                }
            }
        })
    }

    fun editP2PBeneficiaryService(customerId: Long, beneficiaryId: Int, beneficiaryName: String, otp: String, callback: ApiCallback<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return).create(BeneficiaryApiInterface::class.java)
        val call = service.editP2PBeneficiary(customerId, beneficiaryId, beneficiaryName, otp)

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
//                }  else {
//                    callback.onFailure(Throwable(CONNECTION_ERROR))
//                }
//            }
//        })
    }

    fun deleteBeneficiary(customerId: Long, beneficiaryId: Long,otp: String, callback: ApiCallback<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return).create(BeneficiaryApiInterface::class.java)
        val call = service.deleteBeneficiary(customerId, beneficiaryId, otp)

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
//            override fun onResponse(call: Call<ApiResult<Any>>, response: Response<ApiResult<Any>>) {
//                val result = response.body() ?: return callback.onFailure(Throwable(CONNECTION_ERROR))
//
//                if ("success" == result.status) {
//                    callback.onSuccess(result.message!!)
//                } else if ("error" == result.status && isNotEmptyList(result.errors)) {
//                    callback.onError(result.errors!![0])
//                }  else {
//                    callback.onFailure(Throwable(CONNECTION_ERROR))
//                }
//            }
//        })
    }

    fun changeBeneficiaryStatus(customerId: Long, beneficiaryId: Long,isEnabled: Boolean,otp: String, callback: ApiCallback<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return).create(BeneficiaryApiInterface::class.java)
        val call = service.changeBeneficiaryStatus(customerId, beneficiaryId, isEnabled, otp)

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
//            override fun onResponse(call: Call<ApiResult<Any>>, response: Response<ApiResult<Any>>) {
//                val result = response.body() ?: return callback.onFailure(Throwable(CONNECTION_ERROR))
//
//                if ("success" == result.status) {
//                    callback.onSuccess(result.message!!)
//                } else if ("error" == result.status && isNotEmptyList(result.errors)) {
//                    callback.onError(result.errors!![0])
//                }  else {
//                    callback.onFailure(Throwable(CONNECTION_ERROR))
//                }
//            }
//        })
    }

    fun getBankList(customerId: Long, callback: ApiCallback<List<String>>) {
        val service = ApiClient.getInstance(token ?: return, sessionId ?: return).create(BeneficiaryApiInterface::class.java)
        val call = service.getBankList(customerId)

        call.enqueue(object : Callback<ApiResult<List<String>>> {
            override fun onFailure(call: Call<ApiResult<List<String>>>, t: Throwable) {
                callback.onFailure(t)
            }

            override fun onResponse(call: Call<ApiResult<List<String>>>, response: Response<ApiResult<List<String>>>) {
                val result = response.body() ?: return callback.onFailure(Throwable(CONNECTION_ERROR))

                if ("success" == result.status && result.data != null) {
                    callback.onSuccess(result.data)
                } else if ("error" == result.status && isNotEmptyList(result.errors)) {
                    callback.onError(result.errors!![0])
                }  else {
                    callback.onFailure(Throwable(CONNECTION_ERROR))
                }
            }
        })
    }

}