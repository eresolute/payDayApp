package com.fh.payday.utilities

import android.content.Context
import android.graphics.drawable.LayerDrawable
import com.fh.payday.R


class DrawableUtil {
    companion object {
        fun setBadgeCount(context: Context, icon: LayerDrawable, count: String) {
            val badge: BadgeDrawable
            val reuse = icon.findDrawableByLayerId(R.id.ic_badge)
            badge = if (reuse is BadgeDrawable) {
                reuse
            } else {
                BadgeDrawable(context)
            }

            badge.setCount(count)
            icon.mutate()
            icon.setDrawableByLayerId(R.id.ic_badge, badge)
        }
    }
}