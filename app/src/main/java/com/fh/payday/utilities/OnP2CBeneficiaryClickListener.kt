package com.fh.payday.utilities

import com.fh.payday.datasource.models.moneytransfer.P2CBeneficiary

interface OnP2CBeneficiaryClickListener {
    fun onP2CBendficiaryClick(beneficiary: P2CBeneficiary)
}