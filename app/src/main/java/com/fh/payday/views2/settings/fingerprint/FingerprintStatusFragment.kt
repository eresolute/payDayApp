package com.fh.payday.views2.settings.fingerprint

import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import com.fh.payday.BaseFragment
import com.fh.payday.R
import com.fh.payday.preferences.BiometricPreferences
import com.fh.payday.utilities.setTextColorSpan

@RequiresApi(Build.VERSION_CODES.M)
class FingerprintStatusFragment : BaseFragment() {

    private val biometricPref = BiometricPreferences.instance
    private lateinit var tvStatus: TextView
    private lateinit var status: Switch

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_fingerprint_status, container, false)

        tvStatus = view.findViewById(R.id.tv_status)
        status = view.findViewById(R.id.status)

        val activity = getActivity() ?: return null

        val isAuthEnabled = biometricPref.isBiometricAuthEnabled(activity)
        status.isChecked = isAuthEnabled

        if (isAuthEnabled)
            tvStatus.setTextColorSpan(getString(R.string.enabled), ContextCompat.getColor(activity, R.color.verified))
        else
            tvStatus.setTextColorSpan(getString(R.string.disabled), ContextCompat.getColor(activity, R.color.colorError))

        attachListener()

        return view
    }

    private fun attachListener() {
        status.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                status.isChecked = false
                enableBiometricAuth()
            } else {
                status.isChecked = true
                disableBiometricAuth()
            }
        }
    }

    private fun enableBiometricAuth() {
        when (activity) {
            is FingerprintSettings -> (activity as FingerprintSettings).enableBiometricAuth()
        }
    }

    private fun disableBiometricAuth() {
        if (biometricPref.clearPreferences(activity ?: return)) {
            status.isChecked = false
            tvStatus.setTextColorSpan(getString(R.string.disabled),
                    ContextCompat.getColor(activity ?: return, R.color.colorError))
        }
    }
}
