package com.fh.payday.utilities

import android.text.TextUtils
import java.util.regex.Matcher
import java.util.regex.Pattern

object Validator {

    fun isNumber(s: String): Boolean {
        for (element in s) {
            if (!Character.isDigit(element)) return false
        }
        return true
    }

    fun validateEmail(email: String): Boolean {
        if (TextUtils.isEmpty(email)) return false
        val regExpn = ("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
            + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
            + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
            + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
            + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
            + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$")
        val inputStr: CharSequence = email
        val pattern: Pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE)
        val matcher: Matcher = pattern.matcher(inputStr)
        return matcher.matches()
    }

    fun isValidEmiratesId(emiratesId: String?): Boolean {
        val regx = "^784[0-9]{12}$"
        val pattern: Pattern = Pattern.compile(regx, Pattern.CASE_INSENSITIVE)
        val matcher: Matcher = pattern.matcher(emiratesId)
        return matcher.matches()
    }

    fun isValidDob(emiratesId: String, dob: String): Boolean {
        return try {
            val array = dob.split("/").toTypedArray()
            val year = array[array.size - 1]
            val dobFromEmirates = emiratesId.substring(3, 7)
            dobFromEmirates == year
        } catch (e: Exception) {
            false
        }
    }

    fun isValidAmount(s: CharSequence?): Boolean = try {
        !s.isNullOrEmpty() && s.toString().toFloat() > 0
    } catch (e: NumberFormatException) {
        false
    }
}