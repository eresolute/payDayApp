package com.fh.payday.viewmodels

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.fh.payday.datasource.models.CustomerSummary

class AccountSummaryViewModel : ViewModel() {

    var summary = MutableLiveData<CustomerSummary>()
        private set

    fun setSummary(summary: CustomerSummary) {
        this.summary.value = summary
    }
}