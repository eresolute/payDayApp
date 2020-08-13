package com.fh.payday.utilities

import com.fh.payday.datasource.models.moneytransfer.LocalBeneficiary

interface OnLocalBeneficiaryClickListener {
    fun onBeneficiaryClick(beneficiary: LocalBeneficiary)
}