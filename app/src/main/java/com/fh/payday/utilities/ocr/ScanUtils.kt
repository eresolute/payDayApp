package com.fh.payday.utilities.ocr

import com.fh.payday.datasource.models.PaydayCard
import com.fh.payday.datasource.models.customer.EmiratesID
import com.fh.payday.utilities.DateTime
import com.google.firebase.ml.vision.text.FirebaseVisionText

private val REGX1 = "^[a-zA-Z0-9]{15}+[0-9oO]{15}$".toRegex()
private val REGX2 = "^[0-9]{7}+[F|M]+[0-9]{7}+.*".toRegex()
private val REGX3 = "^[0-9]{7}+[F|M]+[0-9]{7}+[A-Z]{3}+.*".toRegex()

val emiratesIdRegex = "^[0-9]{3}-[0-9]{4}-[0-9]{7}-\\d$".toRegex()
val dateRegex = "^[0-3]?[0-9]/[0-3]?[0-9]/(?:[0-9]{2})?[0-9]{2}$".toRegex()

private val REGX_CARD_NUMBER = "^[0-9]{12,16}$".toRegex()
private val REGX_DATE_FILTER = "[^\\d/]".toRegex()

fun filterEmiratesId(blocks: List<FirebaseVisionText.TextBlock>): EmiratesID {
    val card = EmiratesID()

    for (block in blocks) {
        val texts = block.text.split("\n")
        for (item in texts) {
            val text = item.replace("\\s+".toRegex(), "")
            setEmiratesId(card, text)
            setDateAndGender(card, text)

            if (text.matches(REGX3)) {
                card.country = text.subSequence(15, 18).toString()
            }
        }
    }

    return card
}

fun setEmiratesId(card: EmiratesID, text: String) {
    if (text.matches(REGX1)) {
        card.id = text.subSequence(15, text.length).toString()
    }

    if (card.id.trim().isEmpty() && text.matches(emiratesIdRegex)) {
        card.id = text.replace("-", "")
    }
}

fun setDateAndGender(card: EmiratesID, text: String) {
    if (text.matches(REGX2)) {
        card.dob = DateTime.parse(text.subSequence(0, 6).toString(), "yyMMdd")
        val gender = text[7].toString()
        card.gender = if ("M".equals(gender, true)) "Male" else if ("F".equals(gender, true)) "Female" else ""
        card.expiry = DateTime.parse(text.subSequence(8, 14).toString(), "yyMMdd")
    }

    val dateText = text.replace(REGX_DATE_FILTER, "")

    if (card.dob.trim().isEmpty() && dateText.matches(dateRegex)
        && DateTime.isBefore(dateText, "dd/MM/yyyy")) {
        card.dob = dateText
    }

    if (card.expiry.trim().isEmpty() && dateText.matches(dateRegex)
        && DateTime.isAfter(dateText, "dd/MM/yyyy")) {
        card.expiry = dateText
    }
}

fun filterPaydayCard(blocks: List<FirebaseVisionText.TextBlock>): PaydayCard {
    val card = PaydayCard()

    for (block in blocks) {
        val texts = block.text.split("\n")
        for (item in texts) {
            val text = item.replace("\\s+".toRegex(), "")

            if (text.matches(REGX_CARD_NUMBER)) {
                card.cardNumber = text.replace("....".toRegex(), "$0 ")
            }

            if (DateTime.isValid(text.replace(REGX_DATE_FILTER, ""), "MM/YY")) {
                card.expiry = text.replace(REGX_DATE_FILTER, "")
            }
        }
    }

    return card
}

fun filterPaydayCardRegister(blocks: List<FirebaseVisionText.TextBlock>): PaydayCard {
    val card = PaydayCard()

    for (block in blocks) {
        val texts = block.text.split("\n")
        for (item in texts) {
            val text = item.replace("\\s+".toRegex(), "")

            if (text.matches(REGX_CARD_NUMBER)) {
                card.cardNumber = text.replace("....".toRegex(), "$0 ")
            }

            if (DateTime.isValid(text.replace(REGX_DATE_FILTER, ""), "MM/YY")) {
                card.expiry = text.replace(REGX_DATE_FILTER, "")
            }
        }
    }

    return card
}

fun isValidPaydayCard(blocks: List<FirebaseVisionText.TextBlock>): Boolean {

    var cardNumberStatus = false
    var expiryStatus = false
    for (block in blocks) {
        val texts = block.text.split("\n")
        for (item in texts) {
            val text = item.replace("\\s+".toRegex(), "")
            if (text.matches(REGX_CARD_NUMBER)) {
                cardNumberStatus = true
            }
            if ( DateTime.isValid(text.replace(REGX_DATE_FILTER, ""), "MM/YY")) {
                expiryStatus = true
            }
        }
    }

    return cardNumberStatus && expiryStatus

}

fun isValidEmiratesIdFront(blocks: List<FirebaseVisionText.TextBlock>): Boolean {
    var status = false
    for (block in blocks) {
        val texts = block.text.split("\n")
        for (item in texts) {
            val text = item.replace("\\s+".toRegex(), "")
            if (text.matches(emiratesIdRegex)) {
                status = true
            }
        }
    }

    return status
}

fun isValidEmiratesIdBack(blocks: List<FirebaseVisionText.TextBlock>): Boolean {
    var status = false
    for (block in blocks) {
        val texts = block.text.split("\n")
        for (item in texts) {
            val text = item.replace("\\s+".toRegex(), "")
            if (text.matches(REGX1)) {
                status = true
            }
        }
    }

    return status
}
