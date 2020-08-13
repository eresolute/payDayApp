package com.fh.payday.preferences

import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import com.fh.payday.utilities.encryption.Encryption

class BiometricPreferences {

    companion object {
        val instance: BiometricPreferences by lazy { BiometricPreferences() }
        private const val PREFERENCE_NAME = "com.fh.payday.biometric.auth"
        private const val HAS_BIOMETRIC_AUTH = "has.biometric.auth"
        private const val EKEY1 = "encrypted.key1"
        private const val EKEY2 = "encrypted.key2"
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun isBiometricAuthEnabled(context: Context) = try {
        context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
            .getString(Encryption(context), HAS_BIOMETRIC_AUTH, "false")?.toBoolean() ?: false
    } catch (e: Exception) {
        false
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun enableBiometric(context: Context): Boolean = try {
        val preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val editor = preferences.edit()

        val encryption = Encryption(context).apply { createMasterKey() }

        editor.putString(encryption, HAS_BIOMETRIC_AUTH, "true")
        editor.apply()
        editor.commit()
    } catch (e: Exception) {
        false
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun disableBiometric(context: Context): Boolean = try {
        val preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val editor = preferences.edit()

        val encryption = Encryption(context).apply { createMasterKey() }

        editor.putString(encryption, HAS_BIOMETRIC_AUTH, "false")
        editor.apply()
        editor.commit()
    } catch (e: Exception) {
        false
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun setBiometricCredentials(
        context: Context,
        text1: String,
        text2: String,
        enableAuth: Boolean = true
    ): Boolean {
        val preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val editor = preferences.edit()

        val encryption = Encryption(context).apply { createMasterKey() }

        editor.putString(encryption, EKEY1, text1)
        editor.putString(encryption, EKEY2, text2)
        editor.putString(encryption, HAS_BIOMETRIC_AUTH, enableAuth.toString())
        editor.apply()
        return editor.commit()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun hasBiometricCredentials(context: Context): Boolean {
        val preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)

        return try {
            val encryption = Encryption(context)

            val text1 = preferences.getString(encryption, EKEY1, "")
            val text2 = preferences.getString(encryption, EKEY2, "")

            !text1.isNullOrEmpty() && !text2.isNullOrEmpty()
        } catch (e: Exception) {
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun getEText1(context: Context) = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        .getString(Encryption(context), EKEY1, "") ?: ""

    @RequiresApi(Build.VERSION_CODES.M)
    fun getEText2(context: Context) = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        .getString(Encryption(context), EKEY2, "") ?: ""

    @RequiresApi(Build.VERSION_CODES.M)
    fun clearPreferences(context: Context): Boolean {
        val editor = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE).edit()
        editor.clear().apply()
        return editor.commit()
    }
}