package com.fh.payday.utilities

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.LocaleList
import java.util.*


class ContextWrapper(base: Context): ContextWrapper(base) {

    companion object {
        @Suppress("DEPRECATION")
        fun wrap(baseContext: Context, newLocale: Locale): ContextWrapper {
            var context = baseContext
            val res = context.resources
            val configuration = res.configuration

            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                    configuration.setLocale(newLocale)

                    val localeList = LocaleList(newLocale)
                    LocaleList.setDefault(localeList)
                    configuration.locales = localeList
                    configuration.setLayoutDirection(newLocale)
                    context = context.createConfigurationContext(configuration)
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 -> {
                    configuration.setLocale(newLocale)
                    configuration.setLayoutDirection(newLocale)
                    context = context.createConfigurationContext(configuration)
                }
                else -> {
                    configuration.locale = newLocale
                    res.updateConfiguration(configuration, res.displayMetrics)
                }
            }

            return ContextWrapper(context)
        }
    }

}