package com.fh.payday.views2.settings.fingerprint

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.biometricauth.BiometricPromptCompat
import com.fh.payday.preferences.BiometricPreferences
import com.fh.payday.utilities.DIGITAL_BANKING_TC_URL
import com.fh.payday.utilities.setTextWithUnderLine
import com.fh.payday.views2.registration.RegisterActivity
import com.fh.payday.views2.shared.fragments.TermsConditionsDialogFragment

class FingerPrintFragment : Fragment() {

    private var activity: BaseActivity? = null
    private var rootView: View? = null
    private var  dialog: BiometricPromptCompat? = null

    private val authListener = object : BiometricPromptCompat.OnAuthenticateListener {
        override fun onAuthSuccess() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                handleAuthSuccess()
            }
        }

        override fun onAuthFailure() {}

        override fun onAuthCancelled() {}

        override fun onAuthHelp(helpCode: Int, helpString: CharSequence) {}

        override fun onAuthError(errorCode: Int, errString: CharSequence) {}
    }

    private val callback = object : BiometricPromptCompat.Callback {
        override fun onSDKNotSupported() {
            activity?.onFailure(rootView ?: return, "SDK Not supported")
        }

        override fun onHardwareNotDetected() {
            activity?.onFailure(rootView ?: return, "Hardware not detected")
        }

        override fun onBiometricAuthNotAvailable() {
            activity?.onFailure(rootView ?: return, "No fingerprints available")
        }

        override fun onPermissionsNotGranted() {
            activity?.onFailure(rootView ?: return, "Permission not granted")
        }

        override fun onError(error: String) {
            activity?.onFailure(rootView ?: return, error)
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context as BaseActivity?
    }

    override fun onDetach() {
        super.onDetach()
        activity = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_finger_print, container, false)

        val checkBox = view.findViewById<CheckBox>(R.id.check_box)

        view.findViewById<TextView>(R.id.tv_terms_condition_link).apply {
            setTextWithUnderLine(text.toString())
            setOnClickListener {

                val dialogFragment: DialogFragment = TermsConditionsDialogFragment
                        .newInstance(DIGITAL_BANKING_TC_URL, getString(R.string.close))
                if (activity == null) return@setOnClickListener
                dialogFragment.show(activity?.supportFragmentManager, "dialog")
            }
        }

        activity?.apply {
            when (this) {
                is RegisterActivity -> {
                    rootView = activity?.findViewById(R.id.root_view)
                    view.findViewById<View>(R.id.btn_skip).setOnClickListener { onRegistrationCompleted() }
                }
                is FingerprintSettings -> {
                    rootView = activity?.findViewById(R.id.root_view)
                    view.findViewById<View>(R.id.btn_skip).visibility = View.INVISIBLE
                }
            }
        }

        view.findViewById<View>(R.id.btn_activate).setOnClickListener {
            if (container != null && !checkBox.isChecked) {
                val message = getString(R.string.terms_conditions_error)
                activity?.onFailure(container, message)
                checkBox.requestFocus()
                return@setOnClickListener
            }

            showFingerprintAuthDialog()
        }

        return view
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (dialog == null) return
            dialog?.dismiss()
        }
    }

    private fun showFingerprintAuthDialog() {
        val context = context ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
           dialog = BiometricPromptCompat.Builder(context)
                    .setTitle(getString(R.string.fingerprint_dialog_title))
                    .setSubtitle("")
                    .setDescription(getString(R.string.fingerprint_dialog_description))
                    .addOnAuthenticationListener(authListener)
                    .build()
                    dialog?.authenticate(callback)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun handleAuthSuccess() {
        activity?.apply {
            when (this) {
                is RegisterActivity -> {
                    val text1 = viewModel.inputWrapper.userId ?: return@apply
                    val text2 = viewModel.inputWrapper.password ?: return@apply

                    if (BiometricPreferences.instance.setBiometricCredentials(this, text1, text2))
                        onRegistrationCompleted()
                }
                is FingerprintSettings -> {
                    if (BiometricPreferences.instance.enableBiometric(this))
                        onBiometricAuthEnabled()
                }
            }
        }
    }

}
