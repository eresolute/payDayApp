package com.fh.payday.views2.intlRemittance.mybeneficiaries.addbeneficiary

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.support.v4.app.Fragment
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.models.intlRemittance.Exchange
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.OnOTPConfirmListener
import com.fh.payday.viewmodels.intlRemittance.IntlRemmittanceViewModel
import com.fh.payday.views.fragments.OTPFragment
import com.fh.payday.views2.intlRemittance.ExchangeAccessKey
import com.fh.payday.views2.intlRemittance.ExchangeContainer
import com.fh.payday.views2.shared.custom.AlertDialogFragment
import kotlinx.android.synthetic.main.toolbar.*

class CustomerActivationOTPActivity : BaseActivity() {

    var flag = false
    private val viewModel: IntlRemmittanceViewModel by lazy {
        ViewModelProviders.of(this).get(IntlRemmittanceViewModel::class.java)
    }

    override fun onBackPressed() {
        sendResult(-1)
    }

    override fun getLayout(): Int = R.layout.activity_customer_activation_otp

    override fun init() {
        setUpToolbar()
        handleBottomBar()
        addObserver()
        generateOtp("y")
        showFragment(OTPFragment.Builder(object : OnOTPConfirmListener {
            override fun onOtpConfirm(otp: String) {
                if (ExchangeContainer.exchanges().isEmpty()) return
                val user = UserPreferences.instance.getUser(this@CustomerActivationOTPActivity)
                user?.apply {
                    ExchangeContainer.exchanges().map {
                        if (it.accessKey.equals(ExchangeAccessKey.FARD, true)) {
                            viewModel.activateCustomer(
                                token,
                                sessionId,
                                refreshToken,
                                customerId.toLong(),
                                it.accessKey,
                                otp
                            )

                        }
                    }
                }
            }
        })
            .setPinLength(4)
            .setTitle(getString(R.string.enter_otp))
            .setInstructions(getString(R.string.otp_sent_to_registered_number))
            .setButtonTitle(getString(R.string.submit))
            .setHasCardView(false)
            .setOnResendOtpListener {
                flag = true
                generateOtp("r")
            }
            .build())
    }

    private fun setUpToolbar() {
        toolbar_back.setOnClickListener(this)
        toolbar_title.text = getString(R.string.international_money_transfer)
    }

    private fun handleBottomBar() {
        findViewById<TextView>(R.id.btm_home).setOnClickListener(this)
        findViewById<TextView>(R.id.btm_menu_branch).setOnClickListener(this)
        findViewById<TextView>(R.id.btm_menu_support).setOnClickListener(this)
        findViewById<TextView>(R.id.btm_menu_loan_calc).setOnClickListener(this)
        findViewById<TextView>(R.id.toolbar_help).setOnClickListener(this)
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .add(R.id.container, fragment)
            .commit()
    }

    private fun generateOtp(sendOtp: String) {
        if (ExchangeContainer.exchanges().isEmpty()) return
        val user = UserPreferences.instance.getUser(this)
        user?.apply {
            ExchangeContainer.exchanges().map {
                if (it.accessKey.equals(ExchangeAccessKey.FARD, true)) {
                    viewModel.generateOtp(
                        token,
                        sessionId,
                        refreshToken,
                        customerId.toLong(),
                        it.accessKey,
                        sendOtp
                    )
                }
            }
        }
    }

    private fun addObserver() {
        viewModel.otpState.observe(this, Observer {
            it ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                showProgress(getString(R.string.processing))
                return@Observer
            }
            hideProgress()

            when (state) {
                is NetworkState2.Success -> {
                    state.data ?: return@Observer
                    if (flag)
                        showMessage(state.data.responseDesc)
                }
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        return@Observer onSessionExpired(state.message)
                    }
                    handleErrorCode(state.message, state.errorCode.toInt())
                }
                is NetworkState2.Failure -> onFailure(
                    findViewById(R.id.root_view),
                    getString(R.string.request_error)
                )
                else -> onFailure(findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })


        viewModel.activateCustomerState.observe(this, Observer {
            it ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                showProgress(getString(R.string.processing))
                return@Observer
            }

            when (state) {
                is NetworkState2.Success -> {
                    val user = UserPreferences.instance.getUser(this) ?: return@Observer
                    viewModel.getCustomerState(
                        user.token,
                        user.sessionId,
                        user.refreshToken,
                        user.customerId.toString()
                    )
                }
                is NetworkState2.Error -> {
                    hideProgress()
                    if (state.isSessionExpired) {
                        return@Observer onSessionExpired(state.message)
                    }
                    handleErrorCode(state.message, state.errorCode.toInt())
                }
                is NetworkState2.Failure -> {
                    hideProgress()
                    onFailure(
                        findViewById(R.id.root_view),
                        getString(R.string.request_error)
                    )
                }
                else -> {
                    hideProgress()
                    onFailure(findViewById(R.id.root_view), CONNECTION_ERROR)
                }
            }
        })

        viewModel.customerState.observe(this, Observer {
            it ?: return@Observer

            val customerState = it.getContentIfNotHandled() ?: return@Observer

            if (customerState is NetworkState2.Loading<List<Exchange>>) {
                return@Observer
            }

            hideProgress()

            when (customerState) {
                is NetworkState2.Success -> {
                    sendResult(ACTIVATION_SUCCESS)
                    return@Observer
                }
                is NetworkState2.Error -> {
                    if (customerState.isSessionExpired) {
                        onSessionExpired(customerState.message)
                        return@Observer
                    }
                    handleErrorCode(customerState.message, customerState.errorCode.toInt())
                }
                is NetworkState2.Failure -> {
                    onFailure(findViewById(R.id.root_view), customerState.throwable)
                }
            }
        })
    }

    private fun handleErrorCode(message: String, errorCode: Int) {
        showMessage(message,
            R.drawable.ic_error,
            R.color.colorError,
            AlertDialogFragment.OnConfirmListener {
                it.dismiss()
                when (errorCode) {
                    //903 code is for incorrect otp
                    850, 851, 903 -> return@OnConfirmListener
                    else -> sendResult(-1)
                }
            }
        )
        return
    }


    private fun sendResult(code: Int) {
        val returnIntent = Intent()
        returnIntent.apply {
            putExtra(KEY_ACTIVATION_STATUS, code)
            setResult(Activity.RESULT_OK, returnIntent)
        }
        finish()
    }

    companion object {
        const val KEY_ACTIVATION_STATUS = "code"
        const val ACTIVATION_SUCCESS = 43
    }

}
