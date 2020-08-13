package com.fh.payday.views2.shared.utils

import android.view.View
import java.lang.IllegalStateException

fun setVisibility(visibility: Int, vararg views: View) {
    if(visibility != View.VISIBLE && visibility != View.INVISIBLE && visibility != View.GONE) {
        throw IllegalStateException("Invalid visibility value")
    }

    for (view in views) {
        view.visibility = visibility
    }
}