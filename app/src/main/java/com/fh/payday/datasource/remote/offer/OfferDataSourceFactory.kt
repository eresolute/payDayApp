package com.fh.payday.datasource.remote.offer

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.DataSource
import android.arch.paging.PageKeyedDataSource
import com.fh.payday.datasource.models.OfferDetail
import com.fh.payday.views2.offer.OfferState

class OfferDataSourceFactory(
        private val token : String,
        private val sessionId:String,
        private val refreshToken:String,
        private val userId: Long,
        private val offerState: MutableLiveData<OfferState>,
        private val onError: (Int, String) -> Unit
) : DataSource.Factory<Int, OfferDetail>() {

    val itemLiveDataSource = MutableLiveData<PageKeyedDataSource<Int, OfferDetail>>()
    override fun create(): DataSource<Int, OfferDetail> {
        val offerDataSource = OfferDataSource(token, sessionId, refreshToken, userId, offerState, onError)
        itemLiveDataSource.postValue(offerDataSource)
        return offerDataSource
    }
}