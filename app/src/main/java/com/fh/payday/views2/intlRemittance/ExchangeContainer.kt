package com.fh.payday.views2.intlRemittance

import com.fh.payday.datasource.models.intlRemittance.Exchange

object ExchangeContainer {

    // todo - remove
    val accessKey = "FARD"

    private val exchanges: MutableList<Exchange> by lazy { ArrayList<Exchange>() }

    fun addAll(list: List<Exchange>): Boolean {
        clear()
        return exchanges.addAll(list)
    }

    fun exchanges(): List<Exchange> = exchanges

    private fun clear() = exchanges.clear()
}

class  ExchangeAccessKey{
   companion object {
        const val FARD = "FARD"
        const val UAEX = "UAEX"
    }
}

class DeliveryModes{
    companion object{
        const val BT = "BT"
        const val CP = "CP"
        const val BTALTERNATE = "WT"
        const val CPALTERNATE = "CD"
    }
}