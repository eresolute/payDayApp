package com.fh.payday.viewmodels.beneficiaries

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.fh.payday.datasource.models.moneytransfer.Beneficiary
import com.fh.payday.datasource.models.moneytransfer.PaydayBeneficiary
import com.fh.payday.datasource.remote.ApiCallback
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.beneficiaries.BeneficiaryService
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.Event
import java.util.*

class PaydayBeneficiaryViewModel : ViewModel() {

    var selectedBeneficiary: Beneficiary? = null
    var paydayBeneficiary: PaydayBeneficiary? = null
    var beneficiaryName: String? = null
    val paydayBeneficiaryMap = HashMap<String, String>()


    private val _editP2PBeneficiaryState = MutableLiveData<Event<NetworkState2<String>>>()
    val editP2PBeneficiaryState: LiveData<Event<NetworkState2<String>>>
        get() = _editP2PBeneficiaryState

    private val _deleteBeneficiaryState = MutableLiveData<Event<NetworkState2<String>>>()
    val deleteBeneficiaryState: LiveData<Event<NetworkState2<String>>>
        get() = _deleteBeneficiaryState


    private val _changeBeneficiaryState = MutableLiveData<Event<NetworkState2<String>>>()
    val changeBeneficiaryState: LiveData<Event<NetworkState2<String>>>
        get() = _changeBeneficiaryState

    private val _beneficiaryState = MutableLiveData<Event<NetworkState2<PaydayBeneficiary>>>()
    val beneficiary : LiveData<Event<NetworkState2<PaydayBeneficiary>>>
        get() = _beneficiaryState

    private val _otpState = MutableLiveData<Event<NetworkState2<String>>>()
    val otpState: LiveData<Event<NetworkState2<String>>>
        get() = _otpState

    fun getPaydayBeneficiary(id: Long, mobileNo: String) {
        _beneficiaryState.value = Event(NetworkState2.Loading())

        BeneficiaryService.instance.getBeneficiaryDetail(id, mobileNo, object : ApiCallback<PaydayBeneficiary> {
            override fun onFailure(t: Throwable) {
                _beneficiaryState.value = Event(NetworkState2.Failure(Throwable(CONNECTION_ERROR)))
            }

            override fun onError(message: String) {
                _beneficiaryState.value = Event(NetworkState2.Error(message))
            }

            override fun onSuccess(data: PaydayBeneficiary) {
                _beneficiaryState.value = Event(NetworkState2.Success(data))
            }

        })
    }

    fun editP2PBeneficiary(token: String, sessionId: String, refreshToken: String, customerId: Long, beneficiaryId: Int, beneficiaryName: String, otp: String) {
        _editP2PBeneficiaryState.value = Event(NetworkState2.Loading())

        BeneficiaryService.getInstance(token, sessionId, refreshToken).editP2PBeneficiaryService(customerId, beneficiaryId,  beneficiaryName,
                otp, object : ApiCallbackImpl<String>(_editP2PBeneficiaryState) {
            override fun onSuccess(data: String) {
                _editP2PBeneficiaryState.value = Event(NetworkState2.Success(data))
            }

            override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                _editP2PBeneficiaryState.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
            }

            //            override fun onFailure(t: Throwable) {
//                _editP2PBeneficiaryState.value = Event(NetworkState2.Failure(t))
//            }
//
//            override fun onError(message: String) {
//                _editP2PBeneficiaryState.value = Event(NetworkState2.Error(message))
//            }
//
//            override fun onSuccess(data: String) {
//                _editP2PBeneficiaryState.value = Event(NetworkState2.Success(data))
//            }


        })
    }

    fun getOtp(token: String, sessionId: String, refreshToken: String, customerId: Int) {
        _otpState.value = Event(NetworkState2.Loading())

        BeneficiaryService.getInstance(token, sessionId, refreshToken).generateOtp( customerId ,object : ApiCallback<String> {
            override fun onFailure(t: Throwable) {
                _otpState.value = Event(NetworkState2.Failure(t))
            }

            override fun onError(message: String) {
                _otpState.value = Event(NetworkState2.Error(message))
            }

            override fun onSuccess(data: String) {
                _otpState.value = Event(NetworkState2.Success(data))
            }

        })

    }

    fun deleteBeneficiary(token: String, sessionId: String, refreshToken: String, customerId: Long, beneficiaryId: Long, otp: String) {
        _deleteBeneficiaryState.value = Event(NetworkState2.Loading())

        BeneficiaryService.getInstance(token, sessionId, refreshToken).deleteBeneficiary(customerId, beneficiaryId, otp,
                object : ApiCallbackImpl<String>(_deleteBeneficiaryState) {

                    override fun onSuccess(data: String) {
                        _deleteBeneficiaryState.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _deleteBeneficiaryState.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
//            override fun onFailure(t: Throwable) {
//                _deleteBeneficiaryState.value = Event(NetworkState2.Failure(t))
//            }
//
//            override fun onError(message: String) {
//                _deleteBeneficiaryState.value = Event(NetworkState2.Error(message))
//            }
//
//            override fun onSuccess(data: String) {
//                _deleteBeneficiaryState.value = Event(NetworkState2.Success(data))
//            }

        })
    }

    fun changeBeneficiaryStatus(token: String, sessionId: String, refreshToken: String, customerId: Long, beneficiaryId: Long, isEnabled: Boolean, otp: String) {
        _changeBeneficiaryState.value = Event(NetworkState2.Loading())

        BeneficiaryService.getInstance(token, sessionId, refreshToken).changeBeneficiaryStatus(customerId, beneficiaryId, isEnabled, otp,
                object : ApiCallbackImpl<String>(_changeBeneficiaryState) {
                    override fun onSuccess(data: String) {
                        _changeBeneficiaryState.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _changeBeneficiaryState.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
        })
    }
}