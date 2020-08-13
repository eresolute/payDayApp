package com.fh.payday.utilities

fun maskCardNumber(cardNumber: String, mask: String = "XXXX XXXX XXXX ####"): String {
    var index = 0

    val maskedNumber = StringBuilder()
    for (element in mask) {
        when (element) {
            '#' -> {
                maskedNumber.append(cardNumber[index])
                index++
            }
            'x' -> {
                maskedNumber.append(element)
                index++
            }
            'X' -> {
                maskedNumber.append(element)
                index++
            }
            else -> maskedNumber.append(element)
        }
    }

    return maskedNumber.toString()
}