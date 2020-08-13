package com.fh.payday.viewmodels.intlRemittance

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.fh.payday.datasource.models.intlRemittance.*
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.NetworkState2.Loading
import com.fh.payday.datasource.remote.intlRemittance.AddBeneficiaryService
import com.fh.payday.datasource.remote.intlRemittance.ratecalculator.RateCalculatorService
import com.fh.payday.datasource.remote.otp.OtpService
import com.fh.payday.utilities.Event

class AddBeneficiaryViewModel : ViewModel() {

    val inputWrapper by lazy { AlFardanIntlAddBeneficiary() }
    var nationality: String? = null

    private val _countryResponse = MutableLiveData<Event<NetworkState2<List<CountryCurrency>>>>()
    val countryResponse: LiveData<Event<NetworkState2<List<CountryCurrency>>>> = _countryResponse

    private val _payoutCountriesState =
        MutableLiveData<Event<NetworkState2<List<PayoutCountries>>>>()
    val payoutCountriesState: LiveData<Event<NetworkState2<List<PayoutCountries>>>> =
        _payoutCountriesState

    private val _payoutRelationsState =
        MutableLiveData<Event<NetworkState2<List<RelationsItem>>>>()
    val payoutRelationsState: LiveData<Event<NetworkState2<List<RelationsItem>>>> =
            _payoutRelationsState

    private val _payoutAccountDetailsState =
        MutableLiveData<Event<NetworkState2<List<RelationsItem>>>>()
    val payoutAccountDetailsState: LiveData<Event<NetworkState2<List<RelationsItem>>>> =
        _payoutAccountDetailsState

    private val _createBeneficiary = MutableLiveData<Event<NetworkState2<CreateBeneficiaries>>>()
    val createBeneficiaries: LiveData<Event<NetworkState2<CreateBeneficiaries>>> =
        _createBeneficiary

    private val _generateOtp = MutableLiveData<Event<NetworkState2<String>>>()
    val generateOtpState: LiveData<Event<NetworkState2<String>>> = _generateOtp

    private val _payoutAgentState = MutableLiveData<Event<NetworkState2<List<PayoutAgent>>>>()
    val payoutAgentState: LiveData<Event<NetworkState2<List<PayoutAgent>>>> = _payoutAgentState


    private val _payoutAgentBranchesState =
        MutableLiveData<Event<NetworkState2<List<PayoutAgentBranches>>>>()
    val payoutAgentBranchesState: LiveData<Event<NetworkState2<List<PayoutAgentBranches>>>> =
        _payoutAgentBranchesState

    private val _payoutAgentBranchesLocation =
        MutableLiveData<Event<NetworkState2<List<PayoutAgentBranches>>>>()
    val payoutAgentBranchesLocation: LiveData<Event<NetworkState2<List<PayoutAgentBranches>>>> =
        _payoutAgentBranchesLocation

    private val _addToFavBeneficiary = MutableLiveData<Event<NetworkState2<String>>>()
    val addToFavBeneficiary: LiveData<Event<NetworkState2<String>>> = _addToFavBeneficiary


    private val _submitBeneficiaryDetails = MutableLiveData<AlFardanIntlAddBeneficiary>()
    val submitBeneficiaryDetails: LiveData<AlFardanIntlAddBeneficiary> = _submitBeneficiaryDetails

    var countries: List<CountryCurrency> = emptyList()
    var payOutCountries: List<PayoutCountries> = emptyList()

    var payOutCurrencies: List<PayOutCurrencies> = emptyList()
    var payoutAgents: List<PayoutAgent> = emptyList()
    var payoutAgentBranches: List<PayoutAgentBranches> = emptyList()
    var payoutAgentId: Long? = null
    var payoutBranchId: Long? = null
    var flag = 0
    var selectedRadioType: String? = null
    var clearBankDetails: Boolean = false
    var clearData: Boolean = false
    var clearLocationData = false
    var clearBankData = false
    var selectedDeliveryMode: String? = null


    fun getCountries(token: String, sessionId: String, refreshToken: String, customerId: String) {
        _countryResponse.value = Event(Loading())

        RateCalculatorService.getInstance(token, sessionId, refreshToken)
            .getCountries(
                customerId,
                object : ApiCallbackImpl<List<CountryCurrency>>(_countryResponse) {
                    override fun onSuccess(data: List<CountryCurrency>) {
                        countries = data
                        _countryResponse.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _countryResponse.value =
                            Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }

                })
    }

    fun getPayoutCountries(
        token: String,
        sessionId: String,
        refreshToken: String,
        customerId: Long,
        accessKey: String,
        deliveryMode: String
    ) {
        _payoutCountriesState.value = Event(Loading())

        AddBeneficiaryService.getInstance(token, sessionId, refreshToken)
            .getPayoutCountries(
                customerId,
                accessKey,
                deliveryMode,
                object : ApiCallbackImpl<List<PayoutCountries>>(_payoutCountriesState) {
                    override fun onSuccess(data: List<PayoutCountries>) {
                        val sortedCountries = data.sortedBy { it.countryName }
                        payOutCountries = sortedCountries
                        _payoutCountriesState.value = Event(NetworkState2.Success(sortedCountries))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _payoutCountriesState.value =
                            Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })
    }


    fun getRelationShips(
        token: String,
        sessionId: String,
        refreshToken: String,
        customerId: Long,
        payoutAgentId: String,
        deliveryMode: String,
        relation: String
    ) {
        _payoutRelationsState.value = Event(Loading())

        AddBeneficiaryService.getInstance(token, sessionId, refreshToken)
            .getRelationShips(
                customerId,
                    payoutAgentId,
                deliveryMode,
                relation,
                object : ApiCallbackImpl<List<RelationsItem>>(_payoutRelationsState) {
                    override fun onSuccess(data: List<RelationsItem>) {
                        _payoutRelationsState.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _payoutRelationsState.value =
                            Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })
    }

    fun getAccountDetails(
        token: String,
        sessionId: String,
        refreshToken: String,
        customerId: Long,
        payoutAgentId: String,
        deliveryMode: String,
        relation: String
    ) {
        _payoutAccountDetailsState.value = Event(Loading())

        AddBeneficiaryService.getInstance(token, sessionId, refreshToken)
            .getRelationShips(
                customerId,
                payoutAgentId,
                deliveryMode,
                relation,
                object : ApiCallbackImpl<List<RelationsItem>>(_payoutAccountDetailsState) {
                    override fun onSuccess(data: List<RelationsItem>) {
                        _payoutAccountDetailsState.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _payoutAccountDetailsState.value =
                            Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })
    }

    fun createBeneficiaries(
        token: String,
        sessionId: String,
        refreshToken: String,
        id: Long,
        accessKey: String,
        alFardan: AlFardanIntlAddBeneficiary,
        payoutAgentId: Long,
        payoutBranchId: Long,
        otp: String
    ) {
        _createBeneficiary.value = Event(Loading())

        AddBeneficiaryService.getInstance(token, sessionId, refreshToken).createBeneficiaries(id,
            accessKey,
            alFardan,
            payoutAgentId,
            payoutBranchId,
            otp,
            object : ApiCallbackImpl<CreateBeneficiaries>(_createBeneficiary) {
                override fun onSuccess(data: CreateBeneficiaries) {
                    _createBeneficiary.value = Event(NetworkState2.Success(data))
                }

                override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                    _createBeneficiary.value =
                        Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                }
            })
    }

    fun generateOtp(token: String, sessionId: String, refreshToken: String, id: Long) {
        _generateOtp.value = Event(Loading())

        OtpService.getInstance(token, sessionId, refreshToken).getOtp(id.toInt(),
            object : ApiCallbackImpl<String>(_generateOtp) {
                override fun onSuccess(data: String) {
                    _generateOtp.value = Event(NetworkState2.Success(data))
                }
            })
    }

    fun getPayoutAgents(
        token: String, sessionId: String, refreshToken: String, customerId: Long,
        accessKey: String, countryCode: String, deliveryMode: String
    ) {
        _payoutAgentState.value = Event(Loading())

        AddBeneficiaryService.getInstance(token, sessionId, refreshToken)
            .getPayoutAgents(
                customerId,
                accessKey,
                countryCode,
                deliveryMode,
                object : ApiCallbackImpl<List<PayoutAgent>>(_payoutAgentState) {
                    override fun onSuccess(data: List<PayoutAgent>) {
                        payoutAgents = data
                        _payoutAgentState.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _payoutAgentState.value =
                            Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })
    }

    fun getPayoutAgentBranches(
        token: String, sessionId: String, refreshToken: String, customerId: Long,
        accessKey: String, agentId: Long, deliveryMode: String,
        optional1: String = "", optional2: String = ""
    ) {
        _payoutAgentBranchesState.value = Event(Loading())

        AddBeneficiaryService.getInstance(token, sessionId, refreshToken)
            .getPayoutAgentBranches(customerId,
                accessKey,
                agentId,
                deliveryMode,
                optional1,
                optional2,
                object : ApiCallbackImpl<List<PayoutAgentBranches>>(_payoutAgentBranchesState) {
                    override fun onSuccess(data: List<PayoutAgentBranches>) {
                        payoutAgentBranches = data
                        _payoutAgentBranchesState.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _payoutAgentBranchesState.value =
                            Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })
    }

    fun getPayoutAgentBranchLocations(
        token: String, sessionId: String, refreshToken: String, customerId: Long,
        accessKey: String, agentId: Long, deliveryMode: String,
        optional1: String = "", optional2: String = ""
    ) {
        _payoutAgentBranchesLocation.value = Event(Loading())

        AddBeneficiaryService.getInstance(token, sessionId, refreshToken)
            .getPayoutAgentBranches(customerId,
                accessKey,
                agentId,
                deliveryMode,
                optional1,
                optional2,
                object : ApiCallbackImpl<List<PayoutAgentBranches>>(_payoutAgentBranchesLocation) {
                    override fun onSuccess(data: List<PayoutAgentBranches>) {
                        payoutAgentBranches = data
                        _payoutAgentBranchesLocation.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _payoutAgentBranchesLocation.value =
                            Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })
    }

    fun addToFavBeneficiary(
        token: String, sessionId: String, refreshToken: String, customerId: Long,
        accessKey: String, receiverRefNum: String, flag: Int
    ) {
        _addToFavBeneficiary.value = Event(Loading())

        AddBeneficiaryService.getInstance(token, sessionId, refreshToken)
            .addToFavBeneficiary(
                customerId,
                accessKey,
                receiverRefNum,
                flag,
                object : ApiCallbackImpl<String>(_addToFavBeneficiary) {
                    override fun onSuccess(data: String) {
                        _addToFavBeneficiary.value = Event(NetworkState2.Success(data))
                    }

                    override fun onError(message: String, code: Int, isSessionExpired: Boolean) {
                        _addToFavBeneficiary.value =
                            Event(NetworkState2.Error(message, code.toString(), isSessionExpired))
                    }
                })
    }

    fun submitBeneficiary() {
        _submitBeneficiaryDetails.value = inputWrapper
    }
}