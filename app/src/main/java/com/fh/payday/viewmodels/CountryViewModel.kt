package com.fh.payday.viewmodels

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.fh.payday.datasource.models.Country2
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.country.CountryService
import com.fh.payday.utilities.Event

class CountryViewModel : ViewModel() {
    var countries: List<Country2>? = null

    private val _countryState = MutableLiveData<Event<NetworkState2<List<Country2>>>>()
    val countryState: MutableLiveData<Event<NetworkState2<List<Country2>>>>
        get() {
            return _countryState
        }

    fun getCountries(token: String, sessionId: String, refreshToken: String) {
        _countryState.value = Event(NetworkState2.Loading())
        CountryService.getInstance(token, sessionId, refreshToken)
            .getCountry(object : ApiCallbackImpl<List<Country2>>(_countryState) {
                override fun onSuccess(data: List<Country2>) {
                    val mCountries = data.filterNot { (it.countryCode == 91) || (it.countryCode == 971) }
                    countries = mCountries
                    _countryState.value = Event(NetworkState2.Success(countries))
                }
            })
    }

}