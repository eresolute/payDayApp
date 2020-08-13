package com.fh.payday.datasource.models.moneytransfer.ui

import com.fh.payday.datasource.models.moneytransfer.Beneficiary
import com.fh.payday.datasource.models.moneytransfer.PaydayBeneficiary

data class EditBeneficiaryOptions(var icon: Int, var options: String, var beneficiaries: List<Beneficiary>)