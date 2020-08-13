package com.fh.payday.utilities

interface OnOTPConfirmListener {
    fun onOtpConfirm(otp: String)
}

interface OnAtmPinConfirmListener {
    fun onAtmConfirm(pin : String)
}

interface OnLoanClickListener {
    fun onLoanClick(loanNumber : String)
}