package com.fh.payday.views2.loan.apply

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.models.loan.LoanAcceptance
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.OnOTPConfirmListener
import com.fh.payday.viewmodels.LoanViewModel
import com.fh.payday.views.fragments.OTPFragment
import com.fh.payday.views2.shared.custom.AlertDialogFragment
import kotlinx.android.synthetic.main.activity_apply_loan.*
import kotlinx.android.synthetic.main.bottombar_dashboard.*
import kotlinx.android.synthetic.main.toolbar.*

class ApplyLoanActivity : BaseActivity() {

    val viewModel: LoanViewModel by lazy {
        ViewModelProviders.of(this).get(LoanViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.user = UserPreferences.instance.getUser(this)
        viewModel.fromPN = intent?.getBooleanExtra("from_push_notification", false) ?: false

        toolbar_back.setOnClickListener(this)
        toolbar_help.setOnClickListener(this)
        btm_home.setOnClickListener(this)
        btm_menu_branch.setOnClickListener(this)
        btm_menu_support.setOnClickListener(this)
        btm_menu_loan_calc.setOnClickListener(this)

        attachListeners()
        attachObservers()
    }

    override fun onBackPressed() {
        if (view_pager.currentItem == 0) {
            super.onBackPressed()
        } else {
            view_pager.currentItem = view_pager.currentItem - 1
        }
    }

    override fun getLayout() = R.layout.activity_apply_loan

    override fun init() {
        val productType = intent?.getStringExtra("product_type")
        val loanNumber = intent?.getStringExtra("loan_number")
        if (!productType.isNullOrEmpty()) {
            viewModel.productType = productType
            viewModel.loanNumber = loanNumber
        }

        when (viewModel.productType) {
            LoanViewModel.LOAN -> setToolbarTitle()
            LoanViewModel.TOPUP_LOAN -> setToolbarTitle(R.string.apply_loan_top_up_title)
            else -> setToolbarTitle()
        }

        view_pager.adapter = SwipeAdapter(supportFragmentManager, getItems())
    }

    fun navigateUp() {
        val nextPage = view_pager.currentItem + 1
        if (nextPage < view_pager.adapter?.count ?: 0) {
            view_pager.currentItem = nextPage
        }
    }

    private fun setToolbarTitle(@StringRes res: Int = R.string.apply_loan_title) = toolbar_title.setText(res)

    private fun attachListeners() {
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {}

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}

            override fun onPageSelected(position: Int) {
                when (position) {
                    1 -> setToolbarTitle(R.string.verify_otp)
                    else -> {
                        if (viewModel.productType == LoanViewModel.TOPUP_LOAN) {
                            setToolbarTitle(R.string.apply_loan_top_up_title)
                        } else {
                            setToolbarTitle()
                        }
                    }
                }
            }
        })
    }

    private fun attachObservers() {
        viewModel.acceptLoanOfferState.observe(this, Observer {
            it ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                return@Observer showProgress(getString(R.string.processing))
            }

            hideProgress()

            when (state) {
                is NetworkState2.Success -> onSuccess(state.data)
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        return@Observer onSessionExpired(state.message)
                    }
                    handleErrorCode(state.errorCode.toInt(), state.message)
                }
                is NetworkState2.Failure -> onFailure(findViewById(R.id.root_view), getString(R.string.request_error))
                else -> onFailure(findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })

        viewModel.generateOtpState.observe(this, Observer {
            it ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                return@Observer showProgress(getString(R.string.processing))
            }

            hideProgress()

            when (state) {
                is NetworkState2.Success -> navigateUp()
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        return@Observer onSessionExpired(state.message)
                    }
                    onError(state.message)
                }
                is NetworkState2.Failure -> onFailure(findViewById(R.id.root_view),
                    getString(R.string.request_error))
                else -> onFailure(findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    private fun onSuccess(loan: LoanAcceptance?) {
        loan ?: return

        AlertDialogFragment.Builder()
                .setCancelable(false)
                .setMessage(getString(R.string.loan_disbursal))
                .setIcon(R.drawable.ic_success_checked_blue)
                .setDismissListener { dialogInterface ->
                    dialogInterface.dismiss()
                    finish()
                }
                .setCancelListener { dialogInterface ->
                    dialogInterface.dismiss()
                    finish()
                }
                .setConfirmListener { dialog ->
                    dialog.dismiss()
                    finish()
                }
                .build()
                .show(supportFragmentManager, "loan-accepted")
    }

    private fun getItems(): ArrayList<Fragment> {
        val otpConfirmListener = object : OnOTPConfirmListener {
            override fun onOtpConfirm(otp: String) {
                val loanOffer = viewModel.loanOffer ?: return

                val token = viewModel.user?.token ?: return
                val sessionId = viewModel.user?.sessionId ?: return
                val refreshToken = viewModel.user?.refreshToken ?: return
                val customerId = viewModel.user?.customerId ?: return

                viewModel.acceptLoanOffer(token, sessionId, refreshToken, customerId.toLong(), otp,
                        viewModel.selectedLoanAmount.toString(), loanOffer.tenure,
                        loanOffer.interestRate, loanOffer.applicationId, loanOffer.lastSalaryCredit)
            }
        }

        return arrayListOf(
                ApplyLoanFragment(),
                OTPFragment.Builder(otpConfirmListener)
                        .setTitle(getString(R.string.enter_otp))
                        .setButtonTitle(getString(R.string.submit))
                        .setPinLength(6)
                        .setHasCardView(false)
                        .setInstructions(getString(R.string.apply_loan_otp_instructions))
                        .build()
        )
    }

    private class SwipeAdapter(
            fm: FragmentManager,
            private val items: List<Fragment>
    ) : FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int) = items[position]

        override fun getCount() = items.size
    }

}
