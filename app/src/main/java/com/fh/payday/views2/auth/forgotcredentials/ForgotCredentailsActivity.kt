package com.fh.payday.views2.auth.forgotcredentials

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.NonSwipeableViewPager
import com.fh.payday.utilities.OnOTPConfirmListener
import com.fh.payday.utilities.PUBLIC_KEY
import com.fh.payday.viewmodels.ForgotCredentialViewModel
import com.fh.payday.views.fragments.OTPFragment
import com.fh.payday.views2.bottombar.HowToRegisterActivity
import com.fh.payday.views2.bottombar.location.BranchATMActivity
import com.fh.payday.views2.shared.custom.AlertDialogFragment
import kotlinx.android.synthetic.main.bottombar.*
import kotlinx.android.synthetic.main.toolbar.*
import java.lang.ref.WeakReference

class ForgotCredentailsActivity : BaseActivity(), OnOTPConfirmListener {

    private lateinit var mPager: NonSwipeableViewPager
    private lateinit var viewModel: ForgotCredentialViewModel
    private lateinit var forgotType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(ForgotCredentialViewModel::class.java)

        forgotType = intent.getStringExtra("forgotType")
        val mPagerAdapter = SlidePagerAdapter(this, supportFragmentManager, forgotType)
        mPager.adapter = mPagerAdapter
        handleFragment(forgotType)
        addObservers()
        addPasswordObservers()
        handleBottomBar()
    }

    override fun getLayout(): Int {
        return R.layout.activity_forgot_credentials
    }

    override fun init() {
        toolbar_back.setOnClickListener { onBackPressed() }
        toolbar_title.setText(R.string.forgot_user_id_password)
        mPager = findViewById(R.id.view_pager)
        attachListeners()
        mPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {}

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        return if (forgotType == "forgotUserID") {
                            setToolbarTitle(getString(R.string.forgot_user_id))
                        } else {
                            setToolbarTitle(getString(R.string.forgot_password))
                        }
                    }
                    1 -> {
                        return if (forgotType == "forgotUserID") {
                            setToolbarTitle(getString(R.string.verify_otp))
                        } else {
                            setToolbarTitle(getString(R.string.forgot_password))
                        }
                    }
                    2 -> {
                        setToolbarTitle(getString(R.string.forgot_password))
                    }
                    3 -> {
                        setToolbarTitle(getString(R.string.verify_otp))
                    }
                    else -> {
                        throw IllegalStateException("Invalid page position")
                    }
                }
            }
        })
    }

    private fun handleFragment(forgotType: String) {
        toolbar_help.visibility = View.INVISIBLE
        if (forgotType == "forgotUserID") {
            toolbar_title.setText(R.string.forgot_user_id)

        } else {
            toolbar_title.setText(R.string.forgot_password)
        }
    }

    private fun setToolbarTitle(title: String) {
        toolbar_title.text = title

    }

    private fun attachListeners() {
        btm_menu_cash_out.setOnClickListener(this)
        btm_menu_currency_conv.setOnClickListener(this)
        btm_menu_faq.setOnClickListener(this)
        btm_menu_how_to_reg.setOnClickListener(this)
    }

    private fun addObservers() {

        viewModel.credentialsState.observe(this, Observer {
            val credentialsState = it?.getContentIfNotHandled() ?: return@Observer

            if (credentialsState is NetworkState2.Loading<*>) {
                showProgress(getString(R.string.processing))
                return@Observer
            }
            hideProgress()

            when (credentialsState) {
                is NetworkState2.Success -> {
                    val (messageInfo) = credentialsState
                    val dialogFragment = com.fh.payday.views.fragments.AlertDialogFragment.newInstance(
                            messageInfo.toString(),
                            R.drawable.ic_success_checked, R.color.blue
                    ) { finish() }
                    dialogFragment.isCancelable = false
                    dialogFragment.show(supportFragmentManager, "Dialog")
                }
                is NetworkState2.Error -> {
                    val (message) = credentialsState
                    onError(message)

                }
                is NetworkState2.Failure -> {
                    //val (throwable) = credentialsState
                    onFailure(findViewById(R.id.root_view), getString(R.string.request_error))

                }
                else -> onFailure(findViewById<View>(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    private fun addPasswordObservers() {

        viewModel.forgotPasswordState.observe(this, Observer {
            val forgotPasswordState = it?.getContentIfNotHandled() ?: return@Observer

            if (forgotPasswordState is NetworkState2.Loading<*>) {
                showProgress(getString(R.string.processing))
                return@Observer
            }
            hideProgress()

            when (forgotPasswordState) {
                is NetworkState2.Success -> {
                    val (messageInfo) = forgotPasswordState
                    val dialogFragment = com.fh.payday.views.fragments.AlertDialogFragment.newInstance(
                            messageInfo.toString(),
                            R.drawable.ic_success_checked, R.color.blue
                    ) { finish() }
                    dialogFragment.isCancelable = false
                    dialogFragment.show(supportFragmentManager, "Dialog")
                }
                is NetworkState2.Error -> {
                    val (message, code) = forgotPasswordState
                    if (code.toInt() == 814)
                        showMessage(message, R.drawable.ic_error, R.color.colorError, passwordErrorListener)
                    else
                        onError(message)
                }
                is NetworkState2.Failure -> {
                    //val (throwable) = forgotPasswordState
                    onFailure(findViewById(R.id.root_view), getString(R.string.request_error))

                }
                else -> onFailure(findViewById<View>(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    override fun onBackPressed() {
        if (mPager.currentItem == 0) {
            super.onBackPressed()
        } else {
            mPager.currentItem = mPager.currentItem - 1
        }
    }

    fun getViewModel(): ForgotCredentialViewModel {
        return viewModel
    }

    override fun onOtpConfirm(otp: String) {

        if (forgotType == "forgotUserID") {
            val userId = viewModel.customerId
            userId?.let {
                viewModel.forgotUserId(it, otp)
            }

        } else {
            val keyBytes = assets.open(PUBLIC_KEY).use {
                it.readBytes()
            }
            val userPassword = viewModel.newUserPassword
            val userId = viewModel.customerId ?: return
            val userName = viewModel.username ?: return

            userPassword?.let {
                viewModel.forgotPassword(keyBytes, userName, userId, it, otp)
            }
        }
    }

    fun onCardNumberPin() {
        mPager.currentItem = 1
    }

    fun onResetPassword() {
        mPager.currentItem = 3
    }

    fun onUserIdExist() {
        mPager.currentItem = 2
    }

    private fun handleBottomBar() {

        findViewById<TextView>(R.id.btm_menu_how_to_reg).setOnClickListener(this)
        findViewById<TextView>(R.id.btm_menu_cash_out).setOnClickListener {
            startActivity(Intent(this, BranchATMActivity::class.java).apply {
                putExtra("issue", "branch")
            })
        }
    }

    private val passwordErrorListener = AlertDialogFragment.OnConfirmListener {
        it.dismiss()
        onUserIdExist()
    }

    class SlidePagerAdapter(mContext: ForgotCredentailsActivity, val fm: FragmentManager, private val forgotType: String) : FragmentStatePagerAdapter(fm) {
        private val NUM_PAGES = 4
        private val weakReference = WeakReference(mContext)

        override fun getItem(position: Int): Fragment {
            when (position) {
                0 -> {
                    val bundle = Bundle()
                    bundle.putString("fragmentType", forgotType)
                    val fragment = CardNumberPinFragment()
                    fragment.arguments = bundle
                    return fragment
                }
                1 -> {
                    return if (forgotType == "forgotUserID") {
                        OTPFragment.Builder(weakReference.get()!!)
                                .setPinLength(6)
                                .setTitle(weakReference.get()!!.getString(R.string.enter_otp))
                                .setInstructions(null)
                                .setButtonTitle(weakReference.get()!!.getString(R.string.submit))
                                .setHasCardView(false)
                                .build()
                    } else {
                        return CardNumberUserIDFragment()
                    }
                }
                2 -> {
                    return ResetPasswordFragment()
                }
                3 -> {
                    return OTPFragment.Builder(weakReference.get()!!)
                            .setPinLength(6)
                            .setTitle(weakReference.get()!!.getString(R.string.enter_otp))
                            .setInstructions(null)
                            .setButtonTitle(weakReference.get()!!.getString(R.string.submit))
                            .setHasCardView(false)
                            .build()
                }
                else -> {
                    throw IllegalStateException("Invalid page position")
                }
            }
        }

        override fun getCount(): Int {
            return NUM_PAGES
        }
    }
}