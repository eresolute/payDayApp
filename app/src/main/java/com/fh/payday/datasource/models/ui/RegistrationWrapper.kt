package com.fh.payday.datasource.models.ui

import com.fh.payday.datasource.models.CardCustomer
import com.fh.payday.datasource.models.LoanCustomer
import com.fh.payday.datasource.models.customer.EmiratesID
import java.io.File

data class RegisterInputWrapper(
    var capturedEmiratesFront: File? = null,
    var capturedEmiratesBack: File? = null,
    var emiratesId: EmiratesID? = null,
    var cardNumber: String? = null,
    var cardName: String? = null,
    var cardExpiry: String? = null,
    var mobileNo: String? = null,
    var userId: String? = null,
    var isUserIdAvailable: Boolean = false,
    var password: String? = null,
    var eidFront: String? = null,
    var emiratesIdExpiry: String? = null
) {
    fun clear() {
        capturedEmiratesFront = null
        capturedEmiratesBack = null
        emiratesId = null
        cardNumber = null
        cardName = null
        cardExpiry = null
        mobileNo = null
        userId = null
        isUserIdAvailable = false
        password = null
        eidFront = null
        emiratesIdExpiry = null
    }
}

data class RegistrationWrapper(
    var customerId: Long? = null,
    var cardDetails: CardCustomer? = null,
    var loanDetail: LoanCustomer? = null
) {
    fun clear() {
        customerId = null
        cardDetails = null
        loanDetail = null
    }
}