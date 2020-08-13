package com.fh.payday.utilities

/*
import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import com.fh.payday.preferences.LocalePreferences
import java.util.*


*/
/**
 * PayDayFH
 * Created by EResolute on 9/11/2018.
 *//*


class LocaleManager {
    fun onAttach(context: Context): Context {
        val language = LocalePreferences.instance.getLocale(context)
        return setLocale(context, language)
    }

    fun setLocale(context: Context, language: String): Context {
        LocalePreferences.instance.setLocal(context, language)


        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            updateResources(context, language)
            updateResourcesLegacy(context, language)
        } else {
            updateResourcesLegacy(context, language)
        }
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }

    @Suppress("DEPRECATION")
    private fun updateResourcesLegacy(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources = context.resources

        val configuration = resources.configuration
        configuration.locale = locale

        resources.updateConfiguration(configuration, resources.displayMetrics)
        return context
    }

    companion object {
        val instance: LocaleManager by lazy {
            LocaleManager()
        }
    }
}*/
