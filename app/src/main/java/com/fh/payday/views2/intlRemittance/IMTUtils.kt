package com.fh.payday.views2.intlRemittance

import android.content.Context
import com.fh.payday.R
import java.util.*

const val FAMILY_MAINTENANCE = "family maintenance/savings"

fun getTransPurpose(context: Context, purpose: String): String {
    return when (purpose.toLowerCase(Locale.ENGLISH)) {
        FAMILY_MAINTENANCE -> context.resources.getStringArray(R.array.imt_transfer_purpose)[0]
        else -> purpose
    }
}