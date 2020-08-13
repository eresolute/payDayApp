package com.fh.payday.utilities

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

fun Int.getDecimalValue(): String {
    val formatter = DecimalFormat("#,###.##")
    return formatter.format(this.toDouble())
}

fun Double.getDecimalValue(): String {
    val formatter = DecimalFormat("#,###.##")
    return formatter.format(this)
}

fun String.getDecimalValue(): String {
    return try {
        val newValue = this.replace(",", "")
        val formatter = DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale("en")))
        formatter.format(newValue.toDouble())
    } catch (e: NumberFormatException) {
        ""
    }
}

fun Float.getDecimalValue(): String{
    val formatter = DecimalFormat("##.00", DecimalFormatSymbols.getInstance(Locale("en")))
    return formatter.format(this)
}