package com.fh.payday.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PageKeyedDataSource
import android.arch.paging.PagedList
import com.fh.payday.datasource.models.OfferDetail
import com.fh.payday.datasource.remote.offer.OfferDataSource
import com.fh.payday.datasource.remote.offer.OfferDataSourceFactory
import com.fh.payday.views2.offer.OfferState

class OfferViewModel(token: String, session: String, refreshToken: String, userId: Long) : ViewModel() {

    private val _onError = MutableLiveData<Pair<Int, String>>()
    val onError: LiveData<Pair<Int, String>>
    get() = _onError

    //creating livedata for PagedList  and PagedKeyedDataSource
    var itemPagedList: LiveData<PagedList<OfferDetail>>

    var liveDataSource: LiveData<PageKeyedDataSource<Int, OfferDetail>>

    private val _offerState = MutableLiveData<OfferState>()
    val offerState: LiveData<OfferState> get() = _offerState

    //constructor
    init {
        //getting our data source factory
        val offerDataSourceFactory = OfferDataSourceFactory(token, session, refreshToken, userId, _offerState) { code, message ->
            _onError.value = Pair(code, message)
        }

        //getting the live data source from data source factory
        liveDataSource = offerDataSourceFactory.itemLiveDataSource

        //Getting PagedList config
        val pagedListConfig = PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(OfferDataSource.PAGE_SIZE)
                .build()

        itemPagedList = LivePagedListBuilder(offerDataSourceFactory, pagedListConfig)
                .build()

        _offerState.value = OfferState.LOADING
    }

//    fun getOffers(customerId: Long) {
//        _offerRequest.value = Event(NetworkState2.Loading())
//
//        OfferService.instance.getOffers(customerId, object : ApiCallback<List<OfferResponse>> {
//            override fun onFailure(t: Throwable) {
//                _offerRequest.value = Event(NetworkState2.Failure(t))
//            }
//
//            override fun onError(message: String) {
//                _offerRequest.value = Event(NetworkState2.Error(message))
//            }
//
//            override fun onSuccess(data: List<OfferResponse>) {
//                _offerRequest.value = Event(NetworkState2.Success(data))
//            }
//
//        })
//    }
}

