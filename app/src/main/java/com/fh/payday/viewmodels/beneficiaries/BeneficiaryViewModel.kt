package com.fh.payday.viewmodels.beneficiaries

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.fh.payday.datasource.models.moneytransfer.AddP2CBeneficiaryResponse
import com.fh.payday.datasource.models.moneytransfer.AddP2PBeneficiaryResponse
import com.fh.payday.datasource.models.moneytransfer.Beneficiary
import com.fh.payday.datasource.models.moneytransfer.PaydayBeneficiary
import com.fh.payday.datasource.remote.ApiCallback
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.beneficiaries.BeneficiaryService
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.Event
import com.fh.payday.utilities.TYPE
import com.fh.payday.views2.moneytransfer.beneificaries.EditBeneficiaryActivity

class BeneficiaryViewModel : ViewModel() {

    private val _addPayday2CardBeneficiaryState = MutableLiveData<Event<NetworkState2<AddP2CBeneficiaryResponse>>>()
    val addPayday2CardBeneficiaryState: LiveData<Event<NetworkState2<AddP2CBeneficiaryResponse>>>
        get() = _addPayday2CardBeneficiaryState

    private val _addPayday2PaydayBeneficiaryState = MutableLiveData<Event<NetworkState2<AddP2PBeneficiaryResponse>>>()
    val addPayday2PaydayBeneficiaryState: LiveData<Event<NetworkState2<AddP2PBeneficiaryResponse>>>
        get() = _addPayday2PaydayBeneficiaryState


    private val _beneficiaryState = MutableLiveData<Event<NetworkState2<PaydayBeneficiary>>>()
    val beneficiary : LiveData<Event<NetworkState2<PaydayBeneficiary>>>
        get() = _beneficiaryState

    private val _editPaydayBeneficiaryState = MutableLiveData<Event<NetworkState2<String>>>()
    val editPaydayBeneficiaryState: LiveData<Event<NetworkState2<String>>>
        get() = _editPaydayBeneficiaryState

    private val _editP2PBeneficiaryState = MutableLiveData<Event<NetworkState2<String>>>()
    val editP2PBeneficiaryState: LiveData<Event<NetworkState2<String>>>
        get() = _editP2PBeneficiaryState

    private val _otpState = MutableLiveData<Event<NetworkState2<String>>>()
    val otpState: LiveData<Event<NetworkState2<String>>>
        get() = _otpState

    private val _localOtpState = MutableLiveData<Event<NetworkState2<String>>>()
    val localOtpState: LiveData<Event<NetworkState2<String>>>
        get() = _localOtpState

    private val _p2cOtpState = MutableLiveData<Event<NetworkState2<String>>>()
    val p2cOtpState: LiveData<Event<NetworkState2<String>>>
        get() = _p2cOtpState

    private val _deleteOtpState = MutableLiveData<Event<NetworkState2<String>>>()
    val deleteOtpState: LiveData<Event<NetworkState2<String>>>
        get() = _deleteOtpState

    private val _enableOtpState = MutableLiveData<Event<NetworkState2<String>>>()
    val enableOtpState: LiveData<Event<NetworkState2<String>>>
        get() = _enableOtpState

    private val _deleteBeneficiaryState = MutableLiveData<Event<NetworkState2<String>>>()
    val deleteBeneficiaryState: LiveData<Event<NetworkState2<String>>>
        get() = _deleteBeneficiaryState

    private val _changeBeneficiaryState = MutableLiveData<Event<NetworkState2<String>>>()
    val changeBeneficiaryState: LiveData<Event<NetworkState2<String>>>
        get() = _changeBeneficiaryState

    private val _bankListState = MutableLiveData<Event<NetworkState2<List<String>>>>()
    val bankListState: LiveData<Event<NetworkState2<List<String>>>>
        get() = _bankListState

    var shortName: String? = null
    var name: String? = null
    var cardNumber: String? = null
    var paydayBeneficiary: PaydayBeneficiary? = null
    var selectedBeneficiary: Beneficiary? = null
    var selectedOptionBeneficiary: Any? = null
    var optionType: TYPE? = null
    var beneficiaryOption: EditBeneficiaryActivity.OPTION? = null
    var bankArrayList: List<String> = ArrayList()

    //Add IBAN Beneficiary
    var bankList: List<String> = ArrayList()
    var selectedBank: String? = null
    var accountNumber: String? = null

    fun addP2CBeneficiary(id:Long, cardNumber: String, name: String, shortName: String, otp: String) {
        _addPayday2CardBeneficiaryState.value = Event(NetworkState2.Loading())

        BeneficiaryService.instance.addP2CBeneficiaryService(id, cardNumber, name, shortName, otp, object : ApiCallback<AddP2CBeneficiaryResponse> {
            override fun onFailure(t: Throwable) {
                _addPayday2CardBeneficiaryState.value = Event(NetworkState2.Failure(Throwable(CONNECTION_ERROR)))
            }

            override fun onError(message: String) {
                _addPayday2CardBeneficiaryState.value = Event(NetworkState2.Error(message))
            }

            override fun onSuccess(data: AddP2CBeneficiaryResponse) {
                _addPayday2CardBeneficiaryState.value = Event(NetworkState2.Success(data))
            }
        })
    }

    fun addP2PBeneficiary(id:Long, mobileNumber: String, otp: String) {
        _addPayday2PaydayBeneficiaryState.value = Event(NetworkState2.Loading())

        BeneficiaryService.instance.addP2PBeneficiaryService(id, mobileNumber, otp, object : ApiCallback<AddP2PBeneficiaryResponse> {
            override fun onFailure(t: Throwable) {
                _addPayday2PaydayBeneficiaryState.value = Event(NetworkState2.Failure(Throwable(CONNECTION_ERROR)))
            }

            override fun onError(message: String) {
                _addPayday2PaydayBeneficiaryState.value = Event(NetworkState2.Error(message))
            }

            override fun onSuccess(data: AddP2PBeneficiaryResponse) {
                _addPayday2PaydayBeneficiaryState.value = Event(NetworkState2.Success(data))
            }
        })
    }

    fun getPaydayBeneficiary(token: String, sessionId: String, refreshToken: String, id: Long, mobileNo: String) {
        _beneficiaryState.value = Event(NetworkState2.Loading())

        BeneficiaryService.getInstance(token, sessionId, refreshToken).getBeneficiaryDetail(id, mobileNo,
                object : ApiCallbackImpl<PaydayBeneficiary>(_beneficiaryState) {
                    override fun onSuccess(data: PaydayBeneficiary) {
                        _beneficiaryState.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _beneficiaryState.value = Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })

//        BeneficiaryService.instance.getBeneficiaryDetail(id, mobileNo, object : ApiCallback<PaydayBeneficiary> {
//            override fun onFailure(t: Throwable) {
//                _beneficiaryState.value = Event(NetworkState2.Failure(Throwable(CONNECTION_ERROR)))
//            }
//
//            override fun onError(message: String) {
//                _beneficiaryState.value = Event(NetworkState2.Error(message))
//            }
//
//            override fun onSuccess(data: PaydayBeneficiary) {
//                _beneficiaryState.value = Event(NetworkState2.Success(data))
//            }
//
//        })
    }

    fun generateOtp(token: String, sessionId: String, refreshToken: String, customerId: Int) {
        _otpState.value = Event(NetworkState2.Loading())

        BeneficiaryService.getInstance(token, sessionId, refreshToken).generateOtp(customerId, object : ApiCallback<String>{
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

    fun getLocalOtp(token: String, sessionId: String, refreshToken: String, customerId: Int) {
        _localOtpState.value = Event(NetworkState2.Loading())

        BeneficiaryService.getInstance(token, sessionId, refreshToken).generateOtp(customerId, object : ApiCallback<String>{
            override fun onFailure(t: Throwable) {
                _localOtpState.value = Event(NetworkState2.Failure(t))
            }

            override fun onError(message: String) {
                _localOtpState.value = Event(NetworkState2.Error(message))
            }

            override fun onSuccess(data: String) {
                _localOtpState.value = Event(NetworkState2.Success(data))
            }
        })
    }

    fun getP2COtp(token: String, sessionId: String, refreshToken: String, customerId: Int) {
        _p2cOtpState.value = Event(NetworkState2.Loading())

        BeneficiaryService.getInstance(token, sessionId, refreshToken).generateOtp(customerId, object : ApiCallbackImpl<String>(_p2cOtpState) {
            override fun onSuccess(data: String) {
                _p2cOtpState.value = Event(NetworkState2.Success(data))
            }

        })
    }

    fun generateDeleteOtp(customerId: Int) {
        _deleteOtpState.value = Event(NetworkState2.Loading())

        BeneficiaryService.instance.generateOtp(customerId, object : ApiCallback<String>{
            override fun onFailure(t: Throwable) {
                _deleteOtpState.value = Event(NetworkState2.Failure(t))
            }

            override fun onError(message: String) {
                _deleteOtpState.value = Event(NetworkState2.Error(message))
            }

            override fun onSuccess(data: String) {
                _deleteOtpState.value = Event(NetworkState2.Success(data))
            }
        })
    }

    fun generateEnableOtp(customerId: Int) {
        _enableOtpState.value = Event(NetworkState2.Loading())

        BeneficiaryService.instance.generateOtp(customerId, object : ApiCallback<String>{
            override fun onFailure(t: Throwable) {
                _enableOtpState.value = Event(NetworkState2.Failure(t))
            }

            override fun onError(message: String) {
                _enableOtpState.value = Event(NetworkState2.Error(message))
            }

            override fun onSuccess(data: String) {
                _enableOtpState.value = Event(NetworkState2.Success(data))
            }
        })
    }

    fun editPaydayBeneficiary(customerId: Long, beneficiaryId: Int, cardNumber: String, shortName: String, otp: String) {
        _editPaydayBeneficiaryState.value = Event(NetworkState2.Loading())

        BeneficiaryService.instance.editBeneficiaryService(customerId, beneficiaryId, cardNumber, shortName, otp, object : ApiCallback<String> {
            override fun onFailure(t: Throwable) {
                _editPaydayBeneficiaryState.value = Event(NetworkState2.Failure(t))
            }

            override fun onError(message: String) {
                _editPaydayBeneficiaryState.value = Event(NetworkState2.Error(message))
            }

            override fun onSuccess(data: String) {
                _editPaydayBeneficiaryState.value = Event(NetworkState2.Success(data))
            }

        })
    }

    fun editP2PBeneficiary(customerId: Long, beneficiaryId: Int, beneficiaryName: String, otp: String) {
        _editP2PBeneficiaryState.value = Event(NetworkState2.Loading())

        BeneficiaryService.instance.editP2PBeneficiaryService(customerId, beneficiaryId,  beneficiaryName, otp, object : ApiCallback<String> {
            override fun onFailure(t: Throwable) {
                _editP2PBeneficiaryState.value = Event(NetworkState2.Failure(t))
            }

            override fun onError(message: String) {
                _editP2PBeneficiaryState.value = Event(NetworkState2.Error(message))
            }

            override fun onSuccess(data: String) {
                _editP2PBeneficiaryState.value = Event(NetworkState2.Success(data))
            }

        })
    }

    fun deleteBeneficiary(customerId: Long, beneficiaryId: Long, otp: String) {
        _deleteBeneficiaryState.value = Event(NetworkState2.Loading())

        BeneficiaryService.instance.deleteBeneficiary(customerId, beneficiaryId, otp, object : ApiCallback<String> {
            override fun onFailure(t: Throwable) {
                _deleteBeneficiaryState.value = Event(NetworkState2.Failure(t))
            }

            override fun onError(message: String) {
                _deleteBeneficiaryState.value = Event(NetworkState2.Error(message))
            }

            override fun onSuccess(data: String) {
                _deleteBeneficiaryState.value = Event(NetworkState2.Success(data))
            }

        })
    }

    fun changeBeneficiaryStatus(customerId: Long, beneficiaryId: Long, isEnabled: Boolean, otp: String) {
        _changeBeneficiaryState.value = Event(NetworkState2.Loading())

        BeneficiaryService.instance.changeBeneficiaryStatus(customerId, beneficiaryId, isEnabled, otp, object : ApiCallback<String> {
            override fun onFailure(t: Throwable) {
                _changeBeneficiaryState.value = Event(NetworkState2.Failure(t))
            }

            override fun onError(message: String) {
                _changeBeneficiaryState.value = Event(NetworkState2.Error(message))
            }

            override fun onSuccess(data: String) {
                _changeBeneficiaryState.value = Event(NetworkState2.Success(data))
            }
        })
    }

    fun getBankList(customerId: Long) {
        _bankListState.value = Event(NetworkState2.Loading())

        BeneficiaryService.instance.getBankList(customerId, object : ApiCallback<List<String>> {
            override fun onFailure(t: Throwable) {
                _bankListState.value = Event(NetworkState2.Failure(t))
            }

            override fun onError(message: String) {
                _bankListState.value = Event(NetworkState2.Error(message))
            }

            override fun onSuccess(data: List<String>) {
                _bankListState.value = Event(NetworkState2.Success(data))
            }
        })
    }
}