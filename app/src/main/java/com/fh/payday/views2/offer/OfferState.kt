package com.fh.payday.views2.offer

@Suppress("DataClassPrivateConstructor")
data class OfferState private constructor(
    val status: Status,
    val msg: String? = null
) {
    enum class Status {
        LOADING,
        LOADED,
        EMPTY,
        FAILED
    }

    companion object {
        val EMPTY = OfferState(Status.EMPTY)
        val LOADED = OfferState(Status.LOADED)
        val LOADING = OfferState(Status.LOADING)
        fun error(msg: String?) = OfferState(Status.FAILED, msg)
    }
}