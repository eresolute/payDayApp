package com.fh.payday.datasource.remote.moneytransfer

import com.fh.payday.datasource.models.AmountLimit
import com.fh.payday.datasource.models.moneytransfer.*
import com.fh.payday.datasource.remote.*

class TransferService private constructor(
        var token: String? = null,
        var sessionId: String? = null,
        var refreshToken: String? = null
){

    companion object {
        val instance : TransferService by lazy { TransferService() }
        fun getInstance(token: String, sessionId: String, refreshToken: String): TransferService {
            return instance.also { it.token = token; it.sessionId = sessionId; it.refreshToken = refreshToken }
        }

    }

    fun getBeneficiary(customerId: Long, callback: ApiCallback<List<Beneficiary>>) {
        val service = ApiClient.getInstance(token ?: return, sessionId?: return).create(TransferApiInterface::class.java)
        val call = service.getBeneficiary(customerId)

        call.enqueue(CallbackImpl(callback))

//        call.enqueue(object : Callback<ApiResult<List<Beneficiary>>> {
//            override fun onFailure(call: Call<ApiResult<List<Beneficiary>>>, t: Throwable) {
//                callback.onFailure(t)
//            }
//
//            override fun onResponse(call: Call<ApiResult<List<Beneficiary>>>, response: Response<ApiResult<List<Beneficiary>>>) {
//                val result = response.body() ?: return callback.onFailure(Throwable(CONNECTION_ERROR))
//
//                if ("success" == result.status && result.data != null) {
//                    callback.onSuccess(result.data)
//                } else if ("error" == result.status && isNotEmptyList(result.errors)) {
//                    callback.onError(result.errors!![0], result.code)
//                } else {
//                    callback.onFailure(Throwable(CONNECTION_ERROR))
//                }
//            }
//
//        })
    }

    fun getOtp(customerId: Int, callback: ApiCallback<String>) {
        val service = ApiClient.getInstance(token ?: return, sessionId?: return).create(TransferApiInterface::class.java)
        val call = service.getOtp(customerId)

        call.enqueue(CallbackStringImpl(callback))
//        call.enqueue(object : Callback<ApiResult<Any>> {
//            override fun onFailure(call: Call<ApiResult<Any>>, t: Throwable) = callback.onFailure(t)
//
//            override fun onResponse(call: Call<ApiResult<Any>>, response: Response<ApiResult<Any>>) {
//                val result = response.body()?: return callback.onFailure(Throwable(CONNECTION_ERROR))
//                if ("success" == result.status && !TextUtils.isEmpty(result.message)) {
//                    callback.onSuccess(result.message!!)
//                } else if ("error" == result.status && isNotEmptyList(result.errors)) {
//                    callback.onError(result.errors!![0], result.code)
//                } else {
//                    callback.onFailure(Throwable(CONNECTION_ERROR))
//                }
//            }
//        })
    }

    fun p2pTransfer(customerId: Long, mobileNumber: String, amount: String, otp: String, callback: ApiCallback<P2PTransferResponse>) {
        val service = ApiClient.getInstance(token ?: return, sessionId?: return).create(TransferApiInterface::class.java)
        val call = service.p2pTransfer(customerId, mobileNumber, amount, otp)

        call.enqueue(object : CallbackImpl<P2PTransferResponse>(callback) {
            override fun onResponse(result: ApiResult<P2PTransferResponse>, warning: String?) {
                when {
                    "error" == result.status && !result.errors.isNullOrEmpty()-> {
                        callback.onError(result.errors[0], result.code, isSessionExpired(result.code))
                    }
                    else -> super.onResponse(result, warning)
                }
            }
        })


//        call.enqueue(object : Callback<ApiResult<Any>> {
//            override fun onFailure(call: Call<ApiResult<Any>>, t: Throwable) = callback.onFailure(t)
//
//            override fun onResponse(call: Call<ApiResult<Any>>, response: Response<ApiResult<Any>>) {
//                val result = response.body()?: return callback.onFailure(Throwable(CONNECTION_ERROR))
//                if ("success" == result.status) {
//                    callback.onSuccess(result.message!!)
//                } else if ("error" == result.status && isNotEmptyList(result.errors)) {
//                    callback.onError(result.errors!![0], result.code)
//                } else {
//                    callback.onFailure(Throwable(CONNECTION_ERROR))
//                }
//            }
//        })
    }

    fun getAmountLimit(customerId: Long, accessKey: String, callback: ApiCallback<AmountLimit>) {
        val service = ApiClient.getInstance(token ?: return, sessionId?: return).create(TransferApiInterface::class.java)
        val call = service.getAmountLimit(customerId, accessKey)

        call.enqueue(CallbackImpl(callback))
//        call.enqueue(object : Callback<ApiResult<AmountLimit>> {
//            override fun onFailure(call: Call<ApiResult<AmountLimit>>, t: Throwable) = callback.onFailure(t)
//
//            override fun onResponse(call: Call<ApiResult<AmountLimit>>, response: Response<ApiResult<AmountLimit>>) {
//                val result = response.body()?: return callback.onFailure(Throwable(CONNECTION_ERROR))
//                if ("success" == result.status && result.data != null) {
//                    callback.onSuccess(result.data)
//                } else if ("error" == result.status && isNotEmptyList(result.errors)) {
//                    callback.onError(result.errors!![0])
//                } else {
//                    callback.onFailure(Throwable(CONNECTION_ERROR))
//                }
//            }
//
//        })
    }

    fun getLocalBeneficiary(customerId: Long, callback: ApiCallback<List<LocalBeneficiary>>) {
        val service = ApiClient.getInstance(token ?: return, sessionId?: return).create(TransferApiInterface::class.java)
        val call = service.getLocalBeneficiaries(customerId)

        call.enqueue(CallbackImpl(callback))

//        call.enqueue(object : Callback<ApiResult<List<LocalBeneficiary>>> {
//            override fun onFailure(call: Call<ApiResult<List<LocalBeneficiary>>>, t: Throwable) = callback.onFailure(t)
//
//            override fun onResponse(call: Call<ApiResult<List<LocalBeneficiary>>>, response: Response<ApiResult<List<LocalBeneficiary>>>) {
//                val result = response.body()?: return callback.onFailure(Throwable(CONNECTION_ERROR))
//                if ("success" == result.status && result.data != null) {
//                    callback.onSuccess(result.data)
//                } else if ("error" == result.status && isNotEmptyList(result.errors)) {
//                    callback.onError(result.errors!![0])
//                } else {
//                    callback.onFailure(Throwable(CONNECTION_ERROR))
//                }
//            }
//        })
    }

    fun getP2CBeneficiary(customerId: Long, callback: ApiCallbackImpl<List<P2CBeneficiary>>) {
        val service = ApiClient.getInstance(token ?: return, sessionId?: return).create(TransferApiInterface::class.java)
        val call = service.getP2CBeneficiary(customerId)
        call.enqueue(CallbackImpl(callback))
    }

    fun localTransfer(customerId: Long, accountNo: String, beneficiaryName: String, amount: String, bank: String, otp: String, callback: ApiCallback<LocalTransferResponse>) {
        val service = ApiClient.getInstance(token ?: return, sessionId?: return).create(TransferApiInterface::class.java)
        val call = service.localTransfer(customerId, accountNo, beneficiaryName,  amount, bank, otp)

        call.enqueue(object : CallbackImpl<LocalTransferResponse>(callback) {
            override fun onResponse(result: ApiResult<LocalTransferResponse>, warning: String?) {
                when {
                    "error" == result.status && !result.errors.isNullOrEmpty()-> {
                        callback.onError(result.errors[0], result.code, isSessionExpired(result.code))
                    }
                    else -> super.onResponse(result, warning)
                }
            }
        })
    }

    fun ccTransfer(customerId: Int, creditCardNo: String, shortName: String, amount: String, bankName: String, otp: String,
                   callback: ApiCallback<LocalTransferResponse>) {
        val service = ApiClient.getInstance(token ?: return, sessionId?: return).create(TransferApiInterface::class.java)
        val call = service.ccTransfer(customerId, creditCardNo, shortName, amount, bankName, otp)
//        call.enqueue(CallbackImpl(callback))

        call.enqueue(object : CallbackImpl<LocalTransferResponse>(callback) {
            override fun onResponse(result: ApiResult<LocalTransferResponse>, warning: String?) {
                when {
                    "error" == result.status && !result.errors.isNullOrEmpty()-> {
                        callback.onError(result.errors[0], result.code, isSessionExpired(result.code))
                    }
                    else -> super.onResponse(result, warning)
                }
            }
        })
    }
}