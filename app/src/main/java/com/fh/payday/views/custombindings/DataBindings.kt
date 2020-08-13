package com.fh.payday.views.custombindings

import android.databinding.BindingAdapter
import android.os.Build
import android.support.design.widget.TextInputEditText
import android.widget.ImageView
import android.widget.TextView
import com.fh.payday.utilities.MaxLinesToggleClickListener

/**
 * PayDayFH
 * Created by EResolute on 9/6/2018.
 */
@BindingAdapter("setDrawableTop")
fun setDrawableTop(view: TextView, res: Int) {
    view.setCompoundDrawablesWithIntrinsicBounds(0, res, 0, 0)
}

@BindingAdapter("setDrawableLeft")
fun setDrawableLeft(view: TextView, res: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        view.setCompoundDrawablesRelativeWithIntrinsicBounds(res, 0, 0, 0)
    } else {
        view.setCompoundDrawablesWithIntrinsicBounds(res, 0, 0, 0)
    }
}

@BindingAdapter("setDrawableLeft")
fun setDrawableLeft(view: TextInputEditText, res: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        view.setCompoundDrawablesRelativeWithIntrinsicBounds(res, 0, 0, 0)
    } else {
        view.setCompoundDrawablesWithIntrinsicBounds(res, 0, 0, 0)
    }
}

@BindingAdapter("src")
fun setSrc(view: ImageView, res: Int) {
    view.setImageResource(res)
}

@BindingAdapter("maxLinesToggle")
fun setMaxLinesToggleListener(
    view: TextView,
    previousCollapsedMaxLines: Int,
    newCollapsedMaxLines: Int
) {
    if (previousCollapsedMaxLines != newCollapsedMaxLines) {
        view.maxLines = newCollapsedMaxLines
        view.setOnClickListener(MaxLinesToggleClickListener(newCollapsedMaxLines))
    }
}