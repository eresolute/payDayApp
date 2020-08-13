package com.fh.payday.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.fh.payday.datasource.models.BranchLocator
import com.fh.payday.datasource.models.IntlBranchLocator
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.locator.LocatorService
import com.fh.payday.utilities.Event
import com.fh.payday.utilities.maps.SphericalUtil.computeDistanceBetween
import com.fh.payday.views2.intlRemittance.ExchangeContainer
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Completable
import java.util.*
import kotlin.Comparator

class LocatorViewModel : ViewModel() {

    //private val locatorService = LocatorService.getInstance()


    private val _locators = MutableLiveData<Event<NetworkState2<List<BranchLocator>>>>()
    val locatorState: LiveData<Event<NetworkState2<List<BranchLocator>>>>
        get() = _locators

    private val _intlLocators = MutableLiveData<Event<NetworkState2<List<IntlBranchLocator>>>>()
    val intlLocatorState: LiveData<Event<NetworkState2<List<IntlBranchLocator>>>>
        get() = _intlLocators

    private var uaexBranches: List<IntlBranchLocator>? = null
    val filteredXBranches: MutableList<IntlBranchLocator> = mutableListOf()
    var userLatLng: LatLng? = null

    private var fhBranches: List<BranchLocator>? = null

    fun locators(
        loginType: String,
        token: String,
        sessionId: String,
        refreshToken: String,
        customerId: Long,
        locatorType: String
    ) {
        _locators.value = Event(NetworkState2.Loading())

        LocatorService.getInstance(token, sessionId, refreshToken)
            .locators(loginType, customerId, locatorType,
                object : ApiCallbackImpl<List<BranchLocator>>(_locators) {
                    override fun onSuccess(data: List<BranchLocator>) {
                        fhBranches = data
                        _locators.value = Event(NetworkState2.Success(data))
                    }
                })
    }

    fun getFHBranches(userLatLng: LatLng): List<BranchLocator>? =
        sortFHBranches(fhBranches, userLatLng)

    private fun sortFHBranches(
        fhBranches: List<BranchLocator>?,
        userLatLng: LatLng
    ): List<BranchLocator>? {
        val comparator = Comparator<BranchLocator> { b1, b2 ->
            val d1 = computeDistanceBetween(LatLng(b1.latitude, b1.longitude), userLatLng)
            val d2 = computeDistanceBetween(LatLng(b2.latitude, b2.longitude), userLatLng)
            d1.compareTo(d2)
        }

        return fhBranches?.sortedWith(comparator)
    }

    fun locateExchange(token: String, sessionId: String, refreshToken: String, customerId: Long) {
        _intlLocators.value = Event(NetworkState2.Loading())

        LocatorService.getInstance(token, sessionId, refreshToken).locateExchange(customerId,
            object : ApiCallbackImpl<List<IntlBranchLocator>>(_intlLocators) {
                override fun onSuccess(data: List<IntlBranchLocator>) {
                    uaexBranches = data
                    _intlLocators.value = Event(NetworkState2.Success(data))
                }
            })
    }

    fun locateExchangeRx(token: String, sessionId: String, refreshToken: String, customerId: Long) {
        _intlLocators.value = Event(NetworkState2.Loading())

        LocatorService.getInstance(token, sessionId, refreshToken).locateExchangeRx(customerId, ExchangeContainer.exchanges(),
            object : ApiCallbackImpl<List<IntlBranchLocator>>(_intlLocators) {
                override fun onSuccess(data: List<IntlBranchLocator>) {
                    uaexBranches = data
                    _intlLocators.value = Event(NetworkState2.Success(data))
                }
            })
    }

    fun getUAEXBranches(userLatLng: LatLng): List<IntlBranchLocator>? =
        sortUAEXBranches(uaexBranches, userLatLng)

    private fun sortUAEXBranches(
        uaexBranches: List<IntlBranchLocator>?,
        userLatLng: LatLng
    ): List<IntlBranchLocator>? {
        val comparator = Comparator<IntlBranchLocator> { b1, b2 ->
            val d1 = computeDistanceBetween(LatLng(b1.latitude, b1.longitude), userLatLng)
            val d2 = computeDistanceBetween(LatLng(b2.latitude, b2.longitude), userLatLng)
            d1.compareTo(d2)
        }

        return uaexBranches?.sortedWith(comparator)
    }

    fun search(query: String, emirates: String): Completable = Completable.create {
        val filtered = uaexBranches?.filter { b ->
            b.address.toLowerCase(Locale.getDefault())
                .contains(query.toLowerCase(Locale.getDefault())) &&
                    (if (emirates.isNotEmpty()) b.city.toLowerCase(Locale.getDefault()) == emirates.toLowerCase(
                        Locale.getDefault()) else true)
        }?.toList()

        filtered?.let {
            filteredXBranches.clear()
            filteredXBranches.addAll(it)
        }
        it.onComplete()
    }

    /*fun filterLocationsByRange(
        userLatLng: LatLng,
        range: Double = DEFAULT_RADIUS
    ) = uaexBranches?.filter { branch ->
        try {
            val branchLatLng = LatLng(branch.latitude, branch.longitude)
            range >= (computeDistanceBetween(branchLatLng, userLatLng) / 1000)
        } catch (e: NumberFormatException) {
            false
        }
    }

    companion object {
        private const val DEFAULT_RADIUS = 3.0
    }*/
}