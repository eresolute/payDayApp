package com.fh.payday.preferences

import android.content.Context
import android.preference.PreferenceManager
import com.fh.payday.R


/**
 * PayDayFH
 * Created by EResolute on 9/11/2018.
 */
class LocalePreferences private constructor() {
    private val default = "en"

    fun getLocale(context: Context): String {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val language = preferences.getString(keySelected, default)

        return language ?: default
    }

    fun setLocale(context: Context, language: String) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putString(keySelected, language)
        editor.apply()
    }

    fun getSelectedLang(context: Context): String {
        val language = getLocale(context)

        val array = context.resources.getStringArray(R.array.languages)
        return try {
            when (language) {
                "ar" -> array[1]
                "hi" -> array[2]
                "ml" -> array[3]
                "ur" -> array[4]
                "bn" -> array[5]
                "ta" -> array[6]
                "tl" -> array[7]
                else -> array[0]
            }
        } catch (e: Exception) {
            "English"
        }
    }

    companion object {
        const val keySelected = "Locale.Helper.Selected.Language"
        val instance by lazy {
            LocalePreferences()
        }
    }
}