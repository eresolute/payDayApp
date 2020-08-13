package com.fh.payday.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.os.Bundle
import com.fh.payday.datasource.models.CustomerSummary
import com.fh.payday.datasource.models.Item
import com.fh.payday.datasource.remote.ApiCallbackImpl
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.datasource.remote.customer.CustomerService
import com.fh.payday.utilities.Event
import com.fh.payday.utilities.isNotEmptyList
import com.fh.payday.views2.offer.OfferActivity
import java.util.*

/**
 * PayDayFH
 * Created by EResolute on 9/4/2018.
 */
class MainViewModel : ViewModel() {

    var customerSummary: CustomerSummary? = null
    var drawerItems: List<Item>? = null
        get() {

            if (field == null) {
                synchronized(MainViewModel::class.java) {
                    if (field == null) {
                        this.drawerItems = ArrayList()
                    }
                }
            }
            return field
        }
    var services: List<Item>? = null
        get() {
            if (field == null) {
                synchronized(MainViewModel::class.java) {
                    if (field == null) {
                        this.services = ArrayList()
                    }
                }
            }

            return field
        }
    private val customerSummaryState = MutableLiveData<Event<NetworkState2<CustomerSummary>>>()
    private val _readNotificationState = MutableLiveData<Event<NetworkState2<String>>>()

    val networkState: LiveData<Event<NetworkState2<CustomerSummary>>>
        get() = customerSummaryState

    val readNotificationState: LiveData<Event<NetworkState2<String>>>
        get() = _readNotificationState

    fun fetchCustomerSummary(token: String, sessionId: String, refreshToken: String, customerId: Long) {
        customerSummaryState.value = Event<NetworkState2<CustomerSummary>>(NetworkState2.Loading())

        CustomerService.getInstance(token, sessionId, refreshToken)
            .getSummary(customerId, object : ApiCallbackImpl<CustomerSummary>(customerSummaryState) {
                override fun onSuccess(data: CustomerSummary, message: String?) {
                    customerSummary = data
                    customerSummary?.apply {
                        if (isNotEmptyList(cards)) {
                            cards = cards.filter { true }
                        }

                        if (isNotEmptyList(loans)) {
                            loans = loans.filter { true }
                        }
                    }
                    customerSummaryState.value = Event<NetworkState2<CustomerSummary>>(NetworkState2.Success(data,message))
                }

                override fun onSuccess(data: CustomerSummary) { onSuccess(data, null) }
            })
    }

    fun readNotifications(token: String, sessionId: String, refreshToken: String, customerId: Long, notificationList: List<String>) {
        _readNotificationState.value = Event(NetworkState2.Loading())
        CustomerService.getInstance(token, sessionId, refreshToken).readNotifications(customerId, notificationList,
            object : ApiCallbackImpl<String>(_readNotificationState) {
                override fun onSuccess(data: String) {
                    _readNotificationState.value = Event(NetworkState2.Success(data))
                }
            })
    }


    fun hasLoanOffer(bundle: Bundle?): Boolean {
        bundle ?: return false
        val title: String? = bundle.getString("title")
        val description: String? = bundle.getString("body")
        val expiryDays: String? = bundle.getString("expiryDays")
        val notificationId: String? = bundle.getString("notificationId")
        val productType: String? = bundle.getString("productType")

        return if (OfferActivity.BANNER.equals(productType, true)) {
            !title.isNullOrEmpty() && !productType.isNullOrEmpty()
        } else {
            !title.isNullOrEmpty() && !description.isNullOrEmpty() &&/* !expiryDays.isNullOrEmpty()
                && !notificationId.isNullOrEmpty() &&*/ !productType.isNullOrEmpty()
        }
    }

}
