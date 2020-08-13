package com.fh.payday.utilities

import java.util.*

class PasswordValidator {
    companion object {
        private val regx1 = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[!\"#$%&'()*+,-.\\\\/:;<=>?@\\[\\]^_`{|}~])(?=\\S+$).{8,32}$".toRegex()
        private val regx2 = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[!\"#$%&'()*+,-.\\\\/:;<=>?@\\[\\]^_`{|}~])(?=\\S+$).{8,32}$".toRegex()
        private val regx3 = "^(?=.*[0-9])(?=.*[a-z])(?=.*[!\"#$%&'()*+,-.\\\\/:;<=>?@\\[\\]^_`{|}~])(?=\\S+$).{8,32}$".toRegex()
        private val regx4 = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,32}$".toRegex()

        val promoPolicy = "^FH([a-zA-Z0-9]{1,14})\$".toRegex()

        fun validate(password: String) = password.matches(regx1) && password.matches(regx2)
                && password.matches(regx3) && password.matches(regx4)
    }
}

class UsernameValidator {
    companion object {
        private val regx = "^[a-zA-Z]+(?=.*[a-zA-Z_0-9])(?=\\S+$).{7,32}$".toRegex()

        fun validate(username: String) = username.matches(regx)
    }
}
class AddressValidator {
    companion object {
        private val regx = "^[A-Za-z0-9-_., ]+$".toRegex()
        fun validate(address: String) = address.matches(regx)
    }
}
class MobileNoValidator{
    companion object {
        private val regx = "^05[0-9]{8}\$".toRegex()
        fun  validate(mobileNo : String) = mobileNo.matches(regx)

        private val regex = "^0[0-9]{8}\$".toRegex()
        fun  validateElifeNo(mobileNo : String) = mobileNo.matches(regex)

        private val regex2 = "^49[0-9]{7}\$".toRegex()
        fun  validateBroadbandNo(mobileNo : String) = mobileNo.matches(regex2)
    }
}

fun isValidPromoCode(text: String?): Boolean {
    val regex = "^FH([a-zA-Z0-9]{1,14})\$".toRegex()
    return text?.toUpperCase(Locale.getDefault())?.matches(regex) ?: false
}