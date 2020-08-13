package com.fh.payday.viewmodels.beneficiaries

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.fh.payday.datasource.models.moneytransfer.LocalBeneficiary
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.beneficiaries.localbeneficiary.LocalBeneficiaryService
import com.fh.payday.utilities.Event

class LocalBeneficiaryViewModel : ViewModel() {

    private val _bankListState = MutableLiveData<Event<NetworkState2<List<String>>>>()
    val bankListState: LiveData<Event<NetworkState2<List<String>>>>
        get() = _bankListState

    private val _ibanState = MutableLiveData<Event<NetworkState2<String>>>()
    val ibanState: LiveData<Event<NetworkState2<String>>>
        get() = _ibanState

    private val _addBeneficiaryState = MutableLiveData<Event<NetworkState2<LocalBeneficiary>>>()
    val addBeneficiaryState: LiveData<Event<NetworkState2<LocalBeneficiary>>>
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
        get() { return _otpState }

    var bankList: List<String> = ArrayList()
    var selectedBank: String? = null
    var accountNumber: String? = null
    var selectedBeneficiary: LocalBeneficiary? = null

    val localBeneficiaryMap = LinkedHashMap<String, String>()

    fun getBankList(token: String, sessionId: String, refreshToken: String, customerId: Long) {
        _bankListState.value = Event(NetworkState2.Loading())

        LocalBeneficiaryService.getInstance(token, sessionId, refreshToken).getBankList(customerId, object : ApiCallbackImpl<List<String>>(_bankListState) {
            override fun onSuccess(data: List<String>) {
                bankList = data
                _bankListState.value = Event(NetworkState2.Success(data))
            }
        })
    }

    fun getIban(token: String, sessionId: String, refreshToken: String, customerId: Long, bankName: String, accountNumber: String) {
        _ibanState.value = Event(NetworkState2.Loading())

        LocalBeneficiaryService.getInstance(token, sessionId, refreshToken).getIban(customerId, bankName, accountNumber, object : ApiCallbackImpl<String>(_ibanState) {
            override fun onSuccess(data: String) {
                _ibanState.value = Event(NetworkState2.Success(data))
            }
        })
    }

    fun addBeneficiary(token: String, sessionId: String, refreshToken: String, customerId: Long, mobileNo: String, name: String, accountNo: String, iban: String, bankName: String, otp: String) {
        _addBeneficiaryState.value = Event(NetworkState2.Loading())

        LocalBeneficiaryService.getInstance(token, sessionId, refreshToken).addBeneficiary(customerId, mobileNo, name, accountNo, iban, bankName,
                otp, object : ApiCallbackImpl<LocalBeneficiary>(_addBeneficiaryState) {
            override fun onSuccess(data: LocalBeneficiary) {
                _addBeneficiaryState.value = Event(NetworkState2.Success(data))
            }

            override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                _addBeneficiaryState.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
            }
        })
    }

    fun editBeneficiary(token: String, sessionId: String, refreshToken: String, customerId: Long, beneficiaryId: Int, mobileNo: String, name: String, accountNo: String, iban: String, bankName: String, otp: String) {
        _editBeneficiaryState.value = Event(NetworkState2.Loading())

        LocalBeneficiaryService.getInstance(token, sessionId, refreshToken).editBeneficiary(customerId, beneficiaryId, mobileNo, name, accountNo, iban,
                bankName, otp, object : ApiCallbackImpl<String>(_editBeneficiaryState) {
            override fun onSuccess(data: String) {
                _editBeneficiaryState.value = Event(NetworkState2.Success(data))
            }

            override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                _editBeneficiaryState.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
            }
        })
    }

    fun getOtp(token: String, sessionId: String, refreshToken: String, customerId: Long) {
        _otpState.value = Event(NetworkState2.Loading())

        LocalBeneficiaryService.getInstance(token, sessionId, refreshToken).getOtp( customerId ,object : ApiCallbackImpl<String>(_otpState) {
            override fun onSuccess(data: String) {
                _otpState.value = Event(NetworkState2.Success(data))
            }
        })
    }

    fun deleteBeneficiary(token: String, sessionId: String, refreshToken: String, customerId: Long, beneficiaryId: Int, otp: String) {
        _deleteBeneficiaryState.value = Event(NetworkState2.Loading())

        LocalBeneficiaryService.getInstance(token, sessionId, refreshToken).deleteBeneficiary(customerId, beneficiaryId, otp,
                object: ApiCallbackImpl<String>(_deleteBeneficiaryState) {
                    override fun onSuccess(data: String) {
                        _deleteBeneficiaryState.value = Event(NetworkState2.Success(data))
                    }
                })
    }

    fun enableBeneficiary(token: String, sessionId: String, refreshToken: String, customerId: Long, beneficiaryId: Int, enable: Int, otp: String) {
        _enableBeneficiaryState.value = Event(NetworkState2.Loading())

        LocalBeneficiaryService.getInstance(token, sessionId, refreshToken).enableBeneficiary(customerId, beneficiaryId, enable, otp,
                object : ApiCallbackImpl<String>(_enableBeneficiaryState) {
                    override fun onSuccess(data: String) {
                        _enableBeneficiaryState.value = Event(NetworkState2.Success(data))                    }
                })
    }
}