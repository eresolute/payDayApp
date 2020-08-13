package com.fh.payday.preferences

import android.content.Context
import com.fh.payday.utilities.DateTime
import com.fh.payday.utilities.encryption.Encryption
import android.R.id.edit
import android.content.SharedPreferences



class AppPreferences private constructor() {
    companion object {
        val instance: AppPreferences by lazy { AppPreferences() }
        private const val PREFERENCE_NAME = "com.fh.payday"
        private const val KEY_CACHE_USER_ID = "cache.user.id"
        private const val KEY_CACHE_NAME = "cache.user.name"
        private const val KEY_IS_FIRST_LAUNCH = "is.first.launch"
        private const val KEY_LAST_APP_UPDATE_CHECK = "last.app.update.check"
        const val KEY_UNREAD_NOTIFICATION_COUNT = "unread.notification.count"
    }

    fun cacheUserId(context: Context, userId: String): Boolean {
        val preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)

        val encryption = Encryption(context).apply { createMasterKey() }
        val editor = preferences.edit().apply {
            putString(encryption, KEY_CACHE_USER_ID, userId)
        }

        editor.apply()
        return editor.commit()
    }

    fun getCachedUserId(context: Context) = try {
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
            .getString(Encryption(context), KEY_CACHE_USER_ID, "") ?: ""
    } catch (e: Exception) {
        ""
    }

    fun cacheName(context: Context, name: String): Boolean {
        val preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)

        val encryption = Encryption(context).apply { createMasterKey() }
        val editor = preferences.edit().apply {
            putString(encryption, KEY_CACHE_NAME, name)
        }

        editor.apply()
        return editor.commit()
    }

    fun getCachedName(context: Context) = try {
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
            .getString(Encryption(context), KEY_CACHE_NAME, "") ?: ""
    } catch (e: java.lang.Exception) {
        ""
    }

    private fun getLastAppUpdateDate(context: Context): String? = try {
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
            .getString(Encryption(context), KEY_LAST_APP_UPDATE_CHECK, null)
    } catch (e: Exception) {
        null
    }

    private fun setLastAppUpdateDate(context: Context): Boolean = try {
        val preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val encryption = Encryption(context).apply { createMasterKey() }
        val editor = preferences.edit().apply {
            putString(encryption, KEY_LAST_APP_UPDATE_CHECK, DateTime.currentDate("yyyy-MM-dd"))
            apply()
        }
        editor.commit()
    } catch (e: Exception) {
        false
    }
    fun newNotification(context: Context): Boolean {

        val preferences = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putInt(KEY_UNREAD_NOTIFICATION_COUNT, getNotificationCount(context) + 1)
        editor.apply()
        return editor.commit()
    }
     fun getNotificationCount(context: Context): Int {
        return context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
            .getInt(KEY_UNREAD_NOTIFICATION_COUNT, 0)
    }

    fun clearNotification(context: Context): Boolean {
        val preferences = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putInt(KEY_UNREAD_NOTIFICATION_COUNT, 0)
        editor.apply()
        return editor.commit()
    }

    fun lastAppUpdateFlag(context: Context): Boolean = try {
        val lastAppUpdateDate = getLastAppUpdateDate(context)
        setLastAppUpdateDate(context)
        DateTime.isToday(lastAppUpdateDate)
    } catch (e: Exception) {
        false
    }

    fun isFirstLaunch(context: Context) = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        .getBoolean(KEY_IS_FIRST_LAUNCH, true)

    fun setFirstLaunch(context: Context) = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        .edit().apply {
            putBoolean(KEY_IS_FIRST_LAUNCH, false)
        }.apply()

    fun clearPreferences(context: Context) = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        .edit().apply {
            putString(KEY_CACHE_NAME, "")
            putString(KEY_CACHE_USER_ID, "")
        }.apply()
}