package com.fh.payday.utilities

class PhoneUtils {

    companion object {
        fun extractNumber(phone: String): String {
            val phoneNumber: ArrayList<Char> = ArrayList()
            for (digit in phone.toCharArray()) {
                if (digit.isDigit()) {
                    phoneNumber.add(digit)
                }
            }

            val dummy = phoneNumber.toCharArray().reversedArray()
            val extractedArray = ArrayList<Char>()
            for ((index, value) in dummy.withIndex()) {
                if (index < 9)
                    extractedArray.add(value)
            }
            return String(extractedArray.reversed().toCharArray())
        }

        fun extractMobileNo(phone: String, prefix: String): String {
            val phoneNumber = phone.replace("+","")
            if (phoneNumber.startsWith(prefix)) return phoneNumber.replaceFirst(prefix, "")
            return phoneNumber
        }
    }

}