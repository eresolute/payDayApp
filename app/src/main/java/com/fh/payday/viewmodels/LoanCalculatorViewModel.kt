package com.fh.payday.viewmodels

import android.arch.lifecycle.ViewModel
import kotlin.math.pow

class LoanCalculatorViewModel : ViewModel() {
    var loanAmount: Int = 0
    var tenure: Int = 0
    var interestRate: Int = 0

    fun calculateLoanEmi(amount: Double, interest: Double, tenure: Int): Double {
        val r = interest / (12 * 100)
        val x = (1 + r).pow(tenure.toDouble())
        return amount * r * x / (x - 1)
    }
}