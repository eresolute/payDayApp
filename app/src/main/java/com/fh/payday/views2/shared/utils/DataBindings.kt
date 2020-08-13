package com.fh.payday.views2.shared.utils

import android.databinding.BindingAdapter
import android.widget.EditText


@BindingAdapter("error")
fun setError(editText: EditText, strOrResId: Any) {
    if (strOrResId is Int) {
        editText.error = editText.context
                .getString(strOrResId)
    } else {
        editText.error = strOrResId as String
    }
}