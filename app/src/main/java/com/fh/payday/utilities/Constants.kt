package com.fh.payday.utilities

import android.os.Build
import com.fh.payday.BuildConfig


val os = Build.VERSION_CODES::class.java.fields[Build.VERSION.SDK_INT].name
val deviceName = Build.MANUFACTURER + " " + Build.MODEL
val appVersion = BuildConfig.VERSION_NAME
const val ENGLISH = "en-US"

const val PUBLIC_KEY = "public_key.der"
const val CONNECTION_ERROR = "The internet connection appears to be offline."
const val LIMITED_CONNECTIVITY = "Failed to connect. Make sure you have an active internet connection."

var token = ""
const val STATUS_CARD_NOT_ACTIVE = 866
const val STATUS_ACCOUNT_NOT_ACTIVE = 701
const val STATUS_EMIRATES_QUEUE = 398

const val CARD_ACTIVE = "Active"
const val CARD_INACTIVE = "INACTIVE"
const val CARD_STOP_LIST = "Card in Stoplist"
const val CARD_CANCELLED = "Cancelled Card"
const val CARD_REPLACED = "Replaced Card"
const val CARD_SUSPENDED = "SUSPENDED"
const val CARD_BLOCKED = "Blocked"

const val EMIRATES_REJECTED = "Rejected"
const val EMIRATES_NOT_VERIFIED = "Not Verified"

const val VAT = 0.05
const val TRANSFER_CHARGES = 1

class Action {
    companion object {
        const val ACTION_OFFER = "com.fh.payday.action.NEW_OFFER"
        const val ACTION_SESSION_ERROR = "com.fh.payday.action.SESSION_ERROR"
        const val ACTION_NOTIFICATION_READ_LIST = "com.fh.payday.action.NOTIFICATION_READ_LIST"
    }
}