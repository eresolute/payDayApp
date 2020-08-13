package com.fh.payday.utilities

import com.fh.payday.datasource.models.IndianState
import com.fh.payday.datasource.models.IsoAlpha3
import com.fh.payday.datasource.models.intlRemittance.PayOutCurrencies
import com.fh.payday.datasource.models.intlRemittance.PayoutCountries

interface OnCountrySelectListener {
    fun onCountrySelect(countryName: IsoAlpha3)
}

interface OnStateSelectListener {
    fun onStateSelect(stateName: IndianState)
}

interface OnPayoutCountrySelectListener {
    fun onPayoutCountrySelect(payoutCountry: PayoutCountries)
}

interface OnPayoutCurrencySelectListener {
    fun onPayoutCurrencySelect(payoutCurrency: PayOutCurrencies)
}