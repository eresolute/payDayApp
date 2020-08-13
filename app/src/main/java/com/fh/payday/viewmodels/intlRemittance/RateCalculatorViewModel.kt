package com.fh.payday.viewmodels.intlRemittance

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.fh.payday.datasource.models.Country
import com.fh.payday.datasource.models.CurrencyConv
import com.fh.payday.datasource.models.intlRemittance.CountryCurrency
import com.fh.payday.datasource.models.intlRemittance.RateConversion
import com.fh.payday.datasource.models.login.User
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.intlRemittance.ratecalculator.RateCalculatorService
import com.fh.payday.utilities.Event
import com.fh.payday.utilities.Validator
import io.reactivex.disposables.CompositeDisposable

class RateCalculatorViewModel : ViewModel() {

    private val _currencyResponse = MutableLiveData<Event<NetworkState2<RateConversion>>>()
    val currencyResponse: LiveData<Event<NetworkState2<RateConversion>>>
        get() {
            return _currencyResponse
        }

    private val _countryResponse = MutableLiveData<Event<NetworkState2<List<CountryCurrency>>>>()
    val countryResponse: LiveData<Event<NetworkState2<List<CountryCurrency>>>>
        get() {
            return _countryResponse
        }

    private val disposable = CompositeDisposable()

    var payoutCurrency: String? = null
    var payoutCountryCode: String? = null
    var payInCurrency: String? = null
    var etTo: Int = 0
    var etFrom: Int = 0

    var selectedAccessKey : String? = null

    var countries: List<CountryCurrency> = emptyList()

    override fun onCleared() {
        disposable.dispose()
        super.onCleared()
    }

    fun getConversionRate(
        user: User,
        payInAmount: String,
        payOutAmount: String,
        payOutCurr: String,
        receiverCountryCode: String,
        payInCurrency: String,
        accessKey: String
    ) {
        disposable.clear()

        _currencyResponse.value = Event(NetworkState2.Loading())

        val callback = object : ApiCallbackImpl<RateConversion>(_currencyResponse) {
            override fun onSuccess(data: RateConversion) {
                _currencyResponse.value = Event(NetworkState2.Success(data))
            }
        }

        val d = RateCalculatorService.getInstance(user.token, user.sessionId, user.refreshToken)
            .getConversionRate(user.customerId.toString(), payInAmount, payOutAmount, payOutCurr,
                receiverCountryCode, payInCurrency, accessKey, callback)

        d?.let { disposable.add(it) }
    }

    fun getCountries(token: String, sessionId: String, refreshToken: String, customerId: String) {
        _countryResponse.value = Event(NetworkState2.Loading())

        RateCalculatorService.getInstance(token, sessionId, refreshToken)
                .getCountries(customerId, object : ApiCallbackImpl<List<CountryCurrency>>(_countryResponse) {
                    override fun onSuccess(data: List<CountryCurrency>) {
                        countries = data
                        _countryResponse.value = Event(NetworkState2.Success(data))
                    }
                })
    }

    fun cancelRequest() {
        disposable.clear()
    }

    fun isValidAmount(s: CharSequence?): Boolean = Validator.isValidAmount(s)


}