@file:JvmName("ServiceUtils")
package com.fh.payday.utilities

import android.app.Activity
import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

fun isGooglePlayServicesAvailable(activity: Activity?): Boolean {
    val googleApiAvailability = GoogleApiAvailability.getInstance()
    val status = googleApiAvailability.isGooglePlayServicesAvailable(activity)
    if (status != ConnectionResult.SUCCESS) {
        if (googleApiAvailability.isUserResolvableError(status)) {
            val dialog = googleApiAvailability.getErrorDialog(activity, status, 2404) { dialog ->
                dialog?.dismiss()
                activity?.finish()
            }
            dialog.show()
        }
        return false
    }
    return true
}

fun isGooglePlayServicesAvailable(context: Context): Boolean {
    val googleApiAvailability = GoogleApiAvailability.getInstance()
    val status = googleApiAvailability.isGooglePlayServicesAvailable(context)
    if (status != ConnectionResult.SUCCESS) {
        return false
    }
    return true
}