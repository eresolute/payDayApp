package com.fh.payday.preferences

import android.content.Context
import com.fh.payday.datasource.models.login.User
import com.fh.payday.utilities.encryption.Encryption
import com.google.gson.Gson


class UserPreferences {

    companion object {
        val instance: UserPreferences by lazy { UserPreferences() }
        const val USER = "CUSTOMER"
        const val IS_LOGGED_IN = "IS_LOGGED_IN"
        const val PREFERENCE_NAME = "user.preferences"
    }

    fun saveUser(context: Context, user: User): Boolean {
        val preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val editor = preferences.edit()

        val gson = Gson()
        val userStr = gson.toJson(user)
        val encryption = Encryption(context).apply { createMasterKey() }
        editor.putString(encryption, USER, userStr)
        editor.putBoolean(IS_LOGGED_IN, true)
        editor.apply()
        return editor.commit()
    }

    fun getUser(context: Context): User? {
        val userStr = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
                .getString(Encryption(context), USER)
        return if (userStr != null) {
            Gson().fromJson<User>(userStr, User::class.java)
        } else null
    }

    fun clearPreferences(context: Context) {
        val preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.clear().apply()
    }

    fun isLoggedIn(context: Context) = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
            .getBoolean(IS_LOGGED_IN, false)

    fun updateProfilePic(context: Context, pic: String): Boolean {
        val user = getUser(context) ?: return false
        user.profilePic = pic

        return saveUser(context, user)
    }
}