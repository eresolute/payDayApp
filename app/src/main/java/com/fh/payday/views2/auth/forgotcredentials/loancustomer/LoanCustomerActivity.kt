package com.fh.payday.views2.auth.forgotcredentials.loancustomer

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.View
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.NonSwipeableViewPager
import com.fh.payday.utilities.OnOTPConfirmListener
import com.fh.payday.utilities.PUBLIC_KEY
import com.fh.payday.viewmodels.ForgotCredentialViewModel
import com.fh.payday.views2.shared.custom.AlertDialogFragment
import kotlinx.android.synthetic.main.bottombar.*
import kotlinx.android.synthetic.main.toolbar.*

class LoanCustomerActivity : BaseActivity() {
    private lateinit var mPager: NonSwipeableViewPager
    private lateinit var viewModel: ForgotCredentialViewModel
    private lateinit var forgotType: String

    private val otpConfirmListener = object : OnOTPConfirmListener {
        override fun onOtpConfirm(otp: String) {
            if (otp.length < 6) return

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
                var userName = ""
                if (forgotType == "forgotUserID")
                    userName = viewModel.username ?: return
               // val userName = viewModel.username ?: return

                userPassword?.let {
                    viewModel.forgotPassword(keyBytes, userName, userId, it, otp)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ForgotCredentialViewModel::class.java)
        addObservers()
        addPasswordObservers()
    }

    private fun handleToolBarTitle(forgotType: String?) {
        toolbar_help.visibility = View.INVISIBLE
        if (forgotType == "forgotUserID") {
            toolbar_title.setText(R.string.forgot_user_id)
        } else {
            toolbar_title.setText(R.string.forgot_password)
        }
    }

    override fun getLayout(): Int {
        return R.layout.activity_loan_customer_credential
    }

    override fun init() {
        toolbar_back.setOnClickListener { onBackPressed() }
        toolbar_title.text = getString(R.string.forgot_user_id_password)
        mPager = findViewById(R.id.view_pager)
        attachListeners()
        forgotType = intent.getStringExtra("forgotType")
        handleToolBarTitle(forgotType)

        val mPagerAdapter = SwipeAdapter(supportFragmentManager,
                getString(R.string.enter_otp),
                getString(R.string.submit),
                forgotType,
                otpConfirmListener
        )

        mPager.adapter = mPagerAdapter
        mPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(position: Int) {
            }

            override fun onPageScrolled(position: Int, p1: Float, p2: Int) {
            }

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
                   /* 2 -> {
                        setToolbarTitle(getString(R.string.forgot_password))
                    }*/
                    2 -> {
                        setToolbarTitle(getString(R.string.verify_otp))
                    }
                    else -> {
                        throw IllegalStateException("Invalid page position")
                    }
                }
            }

        })

    }

    override fun onBackPressed() {
        if (mPager.currentItem == 0) {
            super.onBackPressed()
        }
        mPager.currentItem = mPager.currentItem - 1
    }

    fun onSubmitAccountNo() {
        mPager.currentItem = 1
    }

    fun onResetPassword() {
        mPager.currentItem = 2
    }

    fun onUserIdExist() {
        //mPager.currentItem = 2
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

    fun getViewModel(): ForgotCredentialViewModel {
        return viewModel
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
                    val (msg) = credentialsState
                    val dialogFragment = com.fh.payday.views.fragments.AlertDialogFragment.newInstance(
                            msg.toString(),
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
                    val (msg) = forgotPasswordState
                    val dialogFragment = com.fh.payday.views.fragments.AlertDialogFragment.newInstance(
                            msg.toString(),
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
                    onFailure(findViewById(R.id.root_view), getString(R.string.request_error))

                }
                else -> onFailure(findViewById<View>(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    private val passwordErrorListener = AlertDialogFragment.OnConfirmListener {
        it.dismiss()
        onUserIdExist()
    }
}