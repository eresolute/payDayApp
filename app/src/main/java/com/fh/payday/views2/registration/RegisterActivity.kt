package com.fh.payday.views2.registration

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.biometricauth.BiometricPromptCompat
import com.fh.payday.datasource.models.CardCustomer
import com.fh.payday.datasource.models.CardLoanResult
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.AppPreferences
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.OnOTPConfirmListener
import com.fh.payday.viewmodels.RegistrationViewModel
import com.fh.payday.views.fragments.OTPFragment
import com.fh.payday.views2.auth.LoginActivity
import com.fh.payday.views2.settings.fingerprint.FingerPrintFragment
import com.fh.payday.views2.shared.custom.AlertDialogFragment
import com.fh.payday.views2.shared.fragments.TermsConditionsFragment
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.bottombar.*
import kotlinx.android.synthetic.main.layout_stepper.*
import kotlinx.android.synthetic.main.toolbar.*

class RegisterActivity : BaseActivity() {

    val viewModel: RegistrationViewModel by lazy {
        ViewModelProviders.of(this).get(RegistrationViewModel::class.java)
    }

    private val cardListener = object : OnOTPConfirmListener {
        override fun onOtpConfirm(otp: String) {
            if (TextUtils.isEmpty(otp)) return
            val customerId = viewModel.wrapper.customerId ?: return
            viewModel.verifyOtp(customerId, otp)
        }
    }

    private val resendOtpListener = object : OTPFragment.OnResendOtpListener {
        override fun onConfirm() {
            val wrapper = viewModel.wrapper
            val inputWrapper = viewModel.inputWrapper
            if (wrapper.customerId == null) return

            if (!isNetworkConnected()) {
                return onFailure(findViewById(R.id.root_view), getString(R.string.no_internet_connectivity))
            }

            viewModel.resendOtp(wrapper.customerId ?: return,
                    inputWrapper.mobileNo ?: return)
        }
    }

    /*private val acceptanceListener = object : TermsConditionsFragment.OnAcceptanceListener {
        override fun onAccepted() {
            val front = viewModel.inputWrapper.capturedEmiratesFront ?: return
            val back = viewModel.inputWrapper.capturedEmiratesBack ?: return
            val (id, dob, gender, expiry, nationality) = viewModel.inputWrapper.emiratesId ?: return

            viewModel.submitEmiratesId(front, back, id, expiry, dob, nationality, gender)
        }
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        attachObservers()
    }

    override fun getLayout() = R.layout.activity_register

    override fun init() {
        toolbar_title.text = getString(R.string.register)
        toolbar_back.setOnClickListener { onBackPressed() }
        toolbar_help.visibility = View.INVISIBLE

        attachListeners()

        /*val items = defaultItems(cardListener, resendOtpListener, getString(R.string.verify_otp),
            getString(R.string.otp_instructions), acceptanceListener,
            getString(R.string.terms_and_Conditions), getString(R.string.lorem))*/
        val items = defaultItems(cardListener, resendOtpListener, getString(R.string.verify_otp),
                getString(R.string.otp_instructions))

        val adapter = SwipeAdapter(supportFragmentManager, items)
        view_pager.adapter = adapter

        setSelectedTab(null)

        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(i: Int, v: Float, i1: Int) {}

            override fun onPageSelected(position: Int) {
                val page = (view_pager.adapter as SwipeAdapter?)?.getFragment(position)
                setSelectedTab(page)
            }

            override fun onPageScrollStateChanged(i: Int) {}
        })
    }

    private fun attachListeners() {
        btm_menu_cash_out.setOnClickListener(this)
        btm_menu_currency_conv.setOnClickListener(this)
        btm_menu_faq.setOnClickListener(this)
        btm_menu_how_to_reg.setOnClickListener(this)
    }

    override fun onBackPressed() {
        when (view_pager.currentItem) {
            0 -> super.onBackPressed()
            (view_pager.adapter?.count ?: 0)  - 1 -> onRegistrationCompleted()
            else -> view_pager.currentItem = view_pager.currentItem - 1
        }
    }

    fun resetViewPager() {
        (view_pager.adapter as SwipeAdapter?)?.reset()
    }

    private fun onCardAvailable(cards: List<CardCustomer>?) {
        if (cards.isNullOrEmpty()) return navigateUp()

        (view_pager.adapter as SwipeAdapter?)?.deletePage(2)
        navigateUp()
    }

    private fun onLoanAvailable(mobileNo: String) {
        val adapter = (view_pager.adapter as SwipeAdapter?) ?: return
        adapter.deletePage(2)
        adapter.deletePage(2)
        adapter.deletePage(2)
        adapter.deletePage(2)

        val customerId = viewModel.wrapper.customerId ?: return

        if (!isNetworkConnected()) {
            return onFailure(findViewById(R.id.root_view), getString(R.string.no_internet_connectivity))
        }

        viewModel.generateOtp(customerId, mobileNo)
    }

    fun onCredentialsSubmitted() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return onRegistrationCompleted()
        }

        if (!BiometricPromptCompat.isPermissionsGranted(this)
                || !BiometricPromptCompat.hasHardwareSupport(this)
                || !BiometricPromptCompat.isFingerprintAvailable(this)) {
            return onRegistrationCompleted()
        }

        navigateUp()
    }

    fun navigateUp() {
        val currentPage = view_pager.currentItem
        val nextPage = currentPage + 1
        if (nextPage < view_pager.adapter?.count ?: 0) {
            val handler = Handler()
            handler.post {
                view_pager.currentItem = nextPage
            }
        }
    }

    fun onRegistrationCompleted() {
        UserPreferences.instance.clearPreferences(this)
        AppPreferences.instance.clearPreferences(this)
        startActivity(Intent(this, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        finish()
    }

    private fun setSelectedTab(fragment: Fragment?) {
        if (fragment == null) {
            icon1.setImageResource(R.drawable.ic_card_details_selected)
            icon2.setImageResource(R.drawable.ic_mobile_grey)
            icon3.setImageResource(R.drawable.ic_create_login_details)
            icon4.setImageResource(R.drawable.ic_verify_otp)
            return
        }

        when (fragment) {
            is ScanEmiratesFragment, is EmiratesIdDetailsFragment, is TermsConditionsFragment,
            is RegisterCardFragment, is RegisterCardDetailsFragment, is CardPinFragment -> {
                icon1.setImageResource(R.drawable.ic_card_details_selected)
                icon2.setImageResource(R.drawable.ic_mobile_grey)
                icon3.setImageResource(R.drawable.ic_create_login_details)
                icon4.setImageResource(R.drawable.ic_verify_otp)
            }
            is MobileNumberFragment, is OTPFragment -> {
                icon1.setImageResource(R.drawable.ic_card_details)
                icon2.setImageResource(R.drawable.ic_mobile_blue)
                icon3.setImageResource(R.drawable.ic_create_login_details)
                icon4.setImageResource(R.drawable.ic_verify_otp)
            }
            is RegisterLoginDetailsFragment -> {
                icon1.setImageResource(R.drawable.ic_card_details)
                icon2.setImageResource(R.drawable.ic_mobile_grey)
                icon3.setImageResource(R.drawable.ic_create_login_details_selected)
                icon4.setImageResource(R.drawable.ic_verify_otp)
            }
            is FingerPrintFragment -> {
                icon1.setImageResource(R.drawable.ic_card_details)
                icon2.setImageResource(R.drawable.ic_mobile_grey)
                icon3.setImageResource(R.drawable.ic_create_login_details)
                icon4.setImageResource(R.drawable.ic_verify_otp_selected)
            }
            else -> {
                icon1.setImageResource(R.drawable.ic_card_details)
                icon2.setImageResource(R.drawable.ic_mobile_grey)
                icon3.setImageResource(R.drawable.ic_create_login_details)
                icon4.setImageResource(R.drawable.ic_verify_otp)
            }

        }
    }

    private fun attachObservers() {
        viewModel.verifyOtpState.observe(this, Observer { event ->
            if (event == null) return@Observer
            val state = event.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading<*>) {
                return@Observer showProgress(getString(R.string.processing))

            }

            hideProgress()

            when (state) {
                is NetworkState2.Success -> navigateUp()
                is NetworkState2.Error -> onError(state.message)
                is NetworkState2.Failure -> onFailure(findViewById(R.id.root_view),
                        state.throwable)
                else -> onFailure(findViewById(R.id.root_view), Throwable(CONNECTION_ERROR))
            }
        })

        viewModel.dialogState.observe(this, Observer {
            it ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading<*>) {
                return@Observer showProgress(getString(R.string.processing))
            }

            hideProgress()

            when (state) {
                is NetworkState2.Success<*> -> {
                    val message = (state as NetworkState2.Success<String>).data
                    if (message != null) showMessage(message)
                }
                is NetworkState2.Error<*> -> onError(state.message)
                is NetworkState2.Failure<*> -> onFailure(findViewById(R.id.root_view),
                        state.throwable)
                else -> onFailure(findViewById(R.id.root_view), Throwable(CONNECTION_ERROR))
            }
        })

        viewModel.otpGenerationState.observe(this, Observer {
            it ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                return@Observer showProgress(getString(R.string.processing))
            }

            hideProgress()

            when (state) {
                is NetworkState2.Success<*> -> navigateUp()
                is NetworkState2.Error -> handleErrorResponse(state.message, state.errorCode)
                is NetworkState2.Failure -> onFailure(findViewById(R.id.root_view),
                        state.throwable)
                else -> onFailure(findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    fun handleEmiratesResponse(customerInfo: CardLoanResult?) {
        when (customerInfo?.customerType ?: RegistrationViewModel.CARD_CUSTOMER) {
            RegistrationViewModel.LOAN_CUSTOMER -> {
                val loans = customerInfo?.loans ?: return
                val mobileNo = loans[0].mobileNumber ?: return
                viewModel.inputWrapper.mobileNo = mobileNo
                if (!loans.isNullOrEmpty()) onLoanAvailable(mobileNo)
            }
            else -> onCardAvailable(customerInfo?.cards)
        }
    }

    fun handleErrorResponse(message: String, code: String) {
        val dismissListener = AlertDialogFragment.OnDismissListener {
            it.dismiss()
            startLoginActivity()
        }
        val cancelListener = AlertDialogFragment.OnCancelListener {
            it.dismiss()
            startLoginActivity()
        }
        val confirmListener = AlertDialogFragment.OnConfirmListener {
            it.dismiss()
            startLoginActivity()
        }

        when (code) {
            "309" -> onError(message, false, errorIcon, tintColorError, dismissListener,
                    cancelListener, confirmListener, R.string.login)
            "392" -> onError(message, false, errorIcon, tintColorError, dismissListener,
                    cancelListener, confirmListener, R.string.login)
            "765" -> onError(message, false, errorIcon, tintColorError, alertDismissListener,
                alertCancelListener, confirmListener, R.string.login)
            else -> onError(message)
        }
    }

    override fun startLoginActivity() {
        val intent = Intent(Intent(this, LoginActivity::class.java)).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
        finish()
    }

    companion object {
        const val REQUEST_SCAN_PAYDAY_CARD = 1
        const val REQUEST_CAPTURE_EMIRATES_FRONT = 2
        const val REQUEST_CAPTURE_EMIRATES_BACK = 3
        const val REQUEST_GALLERY_EMIRATES_FRONT = 4
        const val REQUEST_GALLERY_EMIRATES_BACK = 5
    }
}