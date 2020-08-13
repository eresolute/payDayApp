package com.fh.payday.views2.payments.recharge.mawaqif

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.WindowManager
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.models.payments.PaymentRequest
import com.fh.payday.datasource.models.payments.PaymentResult
import com.fh.payday.datasource.models.payments.PlanType
import com.fh.payday.datasource.models.payments.TypeId
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.OnOTPConfirmListener
import com.fh.payday.viewmodels.payments.billpayment.MawaqifViewModel
import com.fh.payday.views.fragments.PaymentSuccessfulDialog
import kotlinx.android.synthetic.main.activity_mawaqif_top_up.*
import kotlinx.android.synthetic.main.bottombar_dashboard.*
import kotlinx.android.synthetic.main.toolbar.*

class MawaqifTopUpActivity : BaseActivity() {

    val viewModel: MawaqifViewModel by lazy {
        ViewModelProviders.of(this).get(MawaqifViewModel::class.java)
    }

    override fun getLayout(): Int = R.layout.activity_mawaqif_top_up

    private val otpConfirmListener = object : OnOTPConfirmListener {
        override fun onOtpConfirm(otp: String) {
            if (otp.length < 6) return
            val user = viewModel.user ?: return

            val accessKey = viewModel.selectedOperator?.accessKey ?: return
            val typeKey = if (viewModel.planType == PlanType.FLEXI) viewModel.operatorDetailsFlexi?.typeKey?.toInt()
                    ?: return else return

            val flexiKey = viewModel.operatorDetailsFlexi?.flexiKey ?: return

            val transId = viewModel.data.value?.get("transaction_id") ?: return
            val account = viewModel.data.value?.get("mobile_no") ?: return
            val amount = viewModel.data.value?.get("payable_amount") ?: return

            val paymentReq = PaymentRequest(accessKey, typeKey, flexiKey, "", transId, account,
                    amount, otp)
            viewModel.makePayment(user.token, user.sessionId, user.refreshToken, user.customerId.toLong(), paymentReq)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        attachObservers()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

    }

    override fun init() {
        setUpToolbar(intent.getStringExtra("operator") ?: return)
        setUpBottomBar()
        setUpViewPager()
        viewModel.user = UserPreferences.instance.getUser(this)
        viewModel.typeId = TypeId.TOP_UP
    }

    private fun setUpViewPager() {
        view_pager.adapter = SwipeAdapter(supportFragmentManager, getItems(
                getString(R.string.enter_otp), getString(R.string.submit), otpConfirmListener, getString(R.string.otp_sent_to_registered_number)))

        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {}
            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}
            override fun onPageSelected(position: Int) {
                //viewModel.dataClear = position == 0
                if (position == 3) setPageOTP()
                else setUpToolbar("mawaqif")
            }
        })
    }

    private fun setUpBottomBar() {
        btm_home.setOnClickListener(this)
        btm_menu_branch.setOnClickListener(this)
        btm_menu_loan_calc.setOnClickListener(this)
        btm_menu_support.setOnClickListener(this)
    }

    private fun setUpToolbar(operatorType: String) {
        when (operatorType.toLowerCase()) {
            "mawaqif" -> {
                toolbar_title.text = getString(R.string.mawaqif_topup)
                iv_operator.setImageResource(R.mipmap.ic_mawaqif_img)
            }
        }
        toolbar_back.setOnClickListener { onBackPressed() }
        toolbar_help.setOnClickListener { startHelpActivity("mobileRechargeHelp") }
    }

    override fun onBackPressed() {
        if (view_pager.currentItem == 0)
            super.onBackPressed()
        else
            view_pager.currentItem = view_pager.currentItem - 1
    }

    fun navigateUp() {
        val nextPage = view_pager.currentItem + 1
        if (nextPage < view_pager.adapter?.count ?: 0) {
            view_pager.currentItem = nextPage
        }
    }

    private fun setPageOTP() {
        toolbar_title.text = getString(R.string.verify_otp)
    }

    private fun attachObservers() {
        viewModel.paymentState.observe(this, Observer {
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
                else -> onFailure(findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    private fun onSuccess(paymentResult: PaymentResult?) {
        paymentResult?.let {
            val dialogFragment = PaymentSuccessfulDialog.newInstance(getString(R.string.successfully_paid),
                    String.format(getString(R.string.amount_in_aed), it.availableBalance),
                    R.drawable.ic_success_checked,
                    R.color.blue
            ) { dialog ->
                dialog.dismiss()
                Intent().apply {
                    setResult(Activity.RESULT_OK, this)
                    finish()
                }
            }
            dialogFragment.isCancelable = false
            dialogFragment.show(supportFragmentManager, null)
        }
    }
}
