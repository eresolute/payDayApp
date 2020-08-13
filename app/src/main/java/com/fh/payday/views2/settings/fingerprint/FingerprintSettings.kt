package com.fh.payday.views2.settings.fingerprint

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.preferences.BiometricPreferences
import com.fh.payday.views2.dashboard.MainActivity
import com.fh.payday.views2.help.HelpActivity
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity
import com.fh.payday.views2.locator.LocatorActivity
import com.fh.payday.views2.message.ContactUsActivity
import com.fh.payday.views2.shared.custom.AlertDialogFragment
import kotlinx.android.synthetic.main.activity_fingerprint_settings.*

@RequiresApi(Build.VERSION_CODES.M)
class FingerprintSettings : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findViewById<View>(R.id.toolbar_back).setOnClickListener { onBackPressed() }
        handleBottomBar()
    }

    override fun getLayout() = R.layout.activity_fingerprint_settings

    override fun init() {
        findViewById<TextView>(R.id.toolbar_title).text = getString(R.string.biometric_auth)
        view_pager.adapter = SwipePageAdapter(supportFragmentManager)

        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {}

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}

            override fun onPageSelected(p0: Int) {}
        })
    }

    override fun onBackPressed() {
        if (view_pager.currentItem == 0) {
            super.onBackPressed()
        } else {
            view_pager.currentItem = view_pager.currentItem - 1
        }
    }

    fun enableBiometricAuth() {
        view_pager.currentItem = 1
    }

    fun onBiometricAuthEnabled() {
        val dismissListener = AlertDialogFragment.OnDismissListener {
            it.dismiss()
            finish()
        }

        val cancelListener = AlertDialogFragment.OnCancelListener {
            it.dismiss()
            finish()
        }

        val confirmListener = AlertDialogFragment.OnConfirmListener {
            it.dismiss()
            finish()
        }

        showMessage(getString(R.string.biometric_auth_enabled),
                successIcon, tintColorSuccess, confirmListener, dismissListener, cancelListener)
    }

    fun onAuthenticated(userId: String, password: String) {
        if (BiometricPreferences.instance.setBiometricCredentials(this, userId, password, false))
            view_pager.currentItem = 2
    }

    private fun handleBottomBar() {

        findViewById<View>(R.id.btm_home).setOnClickListener { view ->
            val i = Intent(view.context, MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(i)
        }

        findViewById<View>(R.id.btm_menu_branch).setOnClickListener { v ->
            val i = Intent(v.context, LocatorActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(i)
        }

        findViewById<View>(R.id.btm_menu_support).setOnClickListener { v ->
            val i = Intent(v.context, ContactUsActivity::class.java)
            startActivity(i)
        }

        findViewById<View>(R.id.btm_menu_loan_calc).setOnClickListener { v ->
            val i = Intent(v.context, LoanCalculatorActivity::class.java)
            startActivity(i)
        }

        findViewById<View>(R.id.toolbar_help).setOnClickListener {
            startActivity(Intent(it.context, HelpActivity::class.java))
        }
    }

    private class SwipePageAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        companion object {
            const val PAGES = 3
        }

        override fun getItem(position: Int) =
                when (position) {
                    0 -> FingerprintStatusFragment()
                    1 -> PasswordFragment()
                    2 -> FingerPrintFragment()
                    else -> throw IllegalStateException("invalid position $position")
                }

        override fun getCount() = PAGES

    }

}
