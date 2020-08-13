package com.fh.payday.views2.kyc.update.passport

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.text.TextUtils
import android.widget.ImageView
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.NonSwipeableViewPager
import com.fh.payday.utilities.OnOTPConfirmListener
import com.fh.payday.viewmodels.kyc.KycViewModel
import com.fh.payday.views.fragments.OTPFragment
import com.fh.payday.views2.dashboard.MainActivity
import com.fh.payday.views2.help.HelpActivity
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity
import com.fh.payday.views2.locator.LocatorActivity
import com.fh.payday.views2.message.ContactUsActivity
import kotlinx.android.synthetic.main.toolbar.*
import java.lang.ref.WeakReference

class PassportUpdateActivity : BaseActivity(), OnOTPConfirmListener {

    lateinit var viewPager: ViewPager
    val viewModel by lazy { ViewModelProviders.of(this).get(KycViewModel::class.java) }
    override fun getLayout(): Int {
        return R.layout.activity_passport_update
    }

    override fun init() {
        handleBottomBar()
        viewPager = findViewById<NonSwipeableViewPager>(R.id.view_pager)
        viewPager.adapter = SliderPagerAdapter(supportFragmentManager, this)
        toolbar_title.text = getString(R.string.update_passport)

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {

            }

            override fun onPageSelected(p0: Int) {
                toolbar_title.text = when (p0) {
                    0 -> getString(R.string.update_passport)
                    1 -> getString(R.string.verify_otp)
                    else -> return
                }
            }
        })

        addPasswordObserver()
    }

    fun onPassportNumber() {
        viewPager.currentItem = 1
    }

    override fun onOtpConfirm(otp: String) {
        if (TextUtils.isEmpty(otp)) return

        val user = UserPreferences.instance.getUser(this) ?: return

        val passportNumber = viewModel.passportNumber ?: return

        viewModel.updatePassport(user.token, user.sessionId, user.refreshToken, user.customerId.toLong(), passportNumber, otp)
    }

    private fun addPasswordObserver() {
        viewModel.updatePassport.observe(this, Observer {
            it ?: return@Observer

            val passwordState = it.getContentIfNotHandled() ?: return@Observer

            if (passwordState is NetworkState2.Loading) {
                showProgress(getString(R.string.processing))
                return@Observer
            }

            hideProgress()

            when (passwordState) {
                is NetworkState2.Success -> //                val message = passwordState.data ?: return@Observer
                    showMessage(getString(R.string.passport_update_succesful),
                        R.drawable.ic_success_checked_blue,
                        R.color.blue,
                        com.fh.payday.views2.shared.custom.AlertDialogFragment.OnConfirmListener { dialog->
                            dialog.dismiss()
                            finish()
                        })
                is NetworkState2.Error -> {
                    if (passwordState.isSessionExpired)
                        return@Observer onSessionExpired(passwordState.message)
                    handleErrorCode(passwordState.errorCode.toInt(), passwordState.message)
                }
                is NetworkState2.Failure -> onFailure(findViewById(R.id.root_view), passwordState.throwable)
                else -> onFailure(findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    private fun handleBottomBar() {

        findViewById<TextView>(R.id.btm_home).setOnClickListener { view ->
            val i = Intent(view.context, MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(i)
        }

        findViewById<TextView>(R.id.btm_menu_branch).setOnClickListener { v ->
            val i = Intent(v.context, LocatorActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(i)
        }

        findViewById<TextView>(R.id.btm_menu_support).setOnClickListener { v ->
            val i = Intent(v.context, ContactUsActivity::class.java)
            startActivity(i)
        }

        findViewById<TextView>(R.id.btm_menu_loan_calc).setOnClickListener { v ->
            val i = Intent(v.context, LoanCalculatorActivity::class.java)
            startActivity(i)
        }

        findViewById<TextView>(R.id.toolbar_help).setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }

        findViewById<ImageView>(R.id.toolbar_back).setOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        if (viewPager.currentItem == 0) super.onBackPressed() else viewPager.currentItem = viewPager.currentItem - 1
    }

    class SliderPagerAdapter(fragmentManager: FragmentManager, context: PassportUpdateActivity) : FragmentPagerAdapter(fragmentManager) {

        val weakReference = WeakReference<PassportUpdateActivity>(context)
        val NUM_PAGES: Int = 2

        override fun getItem(position: Int): Fragment {
            when (position) {
                0 -> {
                    return PassportNumberFragment()
                }
                1 -> {
                    return OTPFragment.Builder(weakReference.get()!!)
                            .setPinLength(6)
                            .setTitle(weakReference.get()!!.getString(R.string.enter_otp))
                            .setInstructions(weakReference.get()!!.getString(R.string.otp_sent_to_registered_number))
                            .setButtonTitle(weakReference.get()!!.getString(R.string.submit))
                            .setHasCardView(false)
                            .build()
                }

                else -> throw IllegalStateException("Invalid Page Position")
            }
        }

        override fun getCount(): Int {
            return NUM_PAGES
        }

    }


}
