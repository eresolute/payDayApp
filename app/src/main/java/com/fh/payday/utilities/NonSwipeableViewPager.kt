package com.fh.payday.utilities

import android.annotation.SuppressLint
import android.content.Context
import android.support.annotation.Nullable
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent



/**
 * PayDayFH
 * Created by EResolute on 9/1/2018.
 */
class NonSwipeableViewPager(
        @get:JvmName("getContext_") private val context: Context,
        @Nullable private val attrs: AttributeSet?
) : ViewPager( context,  attrs) {

    constructor(context: Context) : this(context, null)

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return false
    }

}