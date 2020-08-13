package com.fh.payday.preferences

import android.content.SharedPreferences
import com.fh.payday.utilities.encryption.Encryption

fun SharedPreferences.Editor.putString(
    encryption: Encryption,
    key: String,
    value: String
) {
    val cipherText = encryption.encrypt(value)
    putString(key, cipherText)
}

fun SharedPreferences.getString(
    encryption: Encryption,
    key: String,
    defValue: String? = null
): String? {
    val cipherText = getString(key, defValue) ?: return null
    return encryption.decrypt(cipherText)
}