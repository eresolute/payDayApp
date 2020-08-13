package com.fh.payday.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

class CallingCardViewModel : ViewModel() {
    private var _selectedItem: MutableLiveData<Int?> = MutableLiveData()
    private val selectedItem: LiveData<Int?>
        get() {
            return _selectedItem
        }

    fun setSelectedItem(index: Int) {
        _selectedItem.value = index
    }
}