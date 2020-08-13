package com.fh.payday.viewmodels.beneficiaries

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.fh.payday.datasource.models.moneytransfer.AddP2CBeneficiaryResponse
import com.fh.payday.datasource.models.moneytransfer.P2CBeneficiary
import com.fh.payday.datasource.models.moneytransfer.P2CBeneficiaryResponse
import com.fh.payday.datasource.remote.ApiCallback
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.beneficiaries.BeneficiaryService
import com.fh.payday.datasource.remote.beneficiaries.ccbeneficiary.CCBeneficiaryService
import com.fh.payday.datasource.remote.beneficiaries.localbeneficiary.LocalBeneficiaryService
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.Event

class CCBeneficiaryViewModel : ViewModel() {


    private val _bankListState = MutableLiveData<Event<NetworkState2<List<String>>>>()
    val bankListState: LiveData<Event<NetworkState2<List<String>>>>
        get() = _bankListState

    private val _addBeneficiaryState = MutableLiveData<Event<NetworkState2<P2CBeneficiaryResponse>>>()
    val addBeneficiaryState: LiveData<Event<NetworkState2<P2CBeneficiaryResponse>>>
        get() = _addBeneficiaryState

    private val _editBeneficiaryState = MutableLiveData<Event<NetworkState2<String>>>()
    val editBeneficiaryState: LiveData<Event<NetworkState2<String>>>
        get() = _editBeneficiaryState

    private val _deleteBeneficiaryState = MutableLiveData<Event<NetworkState2<String>>>()
    val deleteBeneficiaryState: LiveData<Event<NetworkState2<String>>>
        get() = _deleteBeneficiaryState

    private val _enableBeneficiaryState = MutableLiveData<Event<NetworkState2<String>>>()
    val enableBeneficiaryState: LiveData<Event<NetworkState2<String>>>
        get() = _enableBeneficiaryState

    private val _otpState = MutableLiveData<Event<NetworkState2<String>>>()
    val otpState: LiveData<Event<NetworkState2<String>>>
        get() = _otpState

    var bankList: List<String> = ArrayList()
    var selectedBank: String? = null
    val ccBeneficiaryMap = LinkedHashMap<String, String>()
    var cardNumber : String? = null
    var name: String? = null
    var selectedBeneficiary: P2CBeneficiary? = null

    fun getOtp(token: String, sessionId: String, refreshToken: String, customerId: Long) {
        _otpState.value = Event(NetworkState2.Loading())

        CCBeneficiaryService.getInstance(token, sessionId, refreshToken).getOtp( customerId,
                object : ApiCallbackImpl<String>(_otpState) {
            override fun onSuccess(data: String) {
                _otpState.value = Event(NetworkState2.Success(data))
            }
        })
    }

    fun addP2CBeneficiary(token: String, sessionId: String, refreshToken: String, id:Long,
                          cardNumber: String, name: String, bank: String, otp: String) {
        _addBeneficiaryState.value = Event(NetworkState2.Loading())

        CCBeneficiaryService.getInstance(token, sessionId, refreshToken).addP2CBeneficiaryService(
                id, cardNumber, name, bank, otp, object : ApiCallbackImpl<P2CBeneficiaryResponse>(_addBeneficiaryState) {
            override fun onSuccess(data: P2CBeneficiaryResponse) {
                _addBeneficiaryState.value = Event(NetworkState2.Success(data))
            }

            override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                _addBeneficiaryState.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
            }
        })
    }

    fun editBeneficiary(token: String, sessionId: String, refreshToken: String, id:Long, beneficiaryId: Int,
                          cardNumber: String, name: String, bankName: String, otp: String) {
        _editBeneficiaryState.value = Event(NetworkState2.Loading())

        CCBeneficiaryService.getInstance(token, sessionId, refreshToken).editBeneficiaryService(
                id, beneficiaryId, cardNumber, name, bankName, otp, object : ApiCallbackImpl<String>(_editBeneficiaryState) {
            override fun onSuccess(data: String) {
                _editBeneficiaryState.value = Event(NetworkState2.Success(data))
            }
        })
    }

    fun deleteBeneficiary(token: String, sessionId: String, refreshToken: String, id:Long, beneficiaryId: Int, otp: String) {
        _deleteBeneficiaryState.value = Event(NetworkState2.Loading())

        CCBeneficiaryService.getInstance(token, sessionId, refreshToken).deleteBeneficiary(
                id, beneficiaryId, otp, object : ApiCallbackImpl<String>(_deleteBeneficiaryState) {
            override fun onSuccess(data: String) {
                _deleteBeneficiaryState.value = Event(NetworkState2.Success(data))
            }
        })
    }

    fun enableBeneficiary(token: String, sessionId: String, refreshToken: String, id: Long, beneficiaryId: Int, enabled: Int, otp: String) {
        _enableBeneficiaryState.value = Event(NetworkState2.Loading())

        CCBeneficiaryService.getInstance(token, sessionId, refreshToken).enableBeneficiary(id, beneficiaryId, enabled, otp,
                object: ApiCallbackImpl<String>(_enableBeneficiaryState) {
                    override fun onSuccess(data: String) {
                        _enableBeneficiaryState.value = Event(NetworkState2.Success(data))
                    }

                })
    }

    fun getBankList(token: String, sessionId: String, refreshToken: String, customerId: Long) {
        _bankListState.value = Event(NetworkState2.Loading())

        LocalBeneficiaryService.getInstance(token, sessionId, refreshToken).getBankList(customerId, object : ApiCallbackImpl<List<String>>(_bankListState) {
            override fun onSuccess(data: List<String>) {
                bankList = data
                _bankListState.value = Event(NetworkState2.Success(data))
            }
        })
    }

}