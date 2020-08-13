package com.fh.payday.views2.payments.internationaltopup

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.View
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.models.payments.*
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.GlideApp
import com.fh.payday.utilities.OnOTPConfirmListener
import com.fh.payday.viewmodels.payments.internationaltopup.InternationalTopupViewModel
import com.fh.payday.views.fragments.PaymentSuccessfulDialog
import kotlinx.android.synthetic.main.activity_international_top_up.*
import kotlinx.android.synthetic.main.bottombar_dashboard.*
import kotlinx.android.synthetic.main.toolbar.*
import java.math.BigInteger
import java.util.UUID.randomUUID


class InternationalTopUpActivity : BaseActivity() {

    val viewModel: InternationalTopupViewModel by lazy {
        ViewModelProviders.of(this).get(InternationalTopupViewModel::class.java)
    }
    private val otpConfirmListener = object : OnOTPConfirmListener {
        override fun onOtpConfirm(otp: String) {
            if (otp.length < 6) return
            val accessKey = viewModel.selectedOperator?.accessKey ?: return
            val typeKey =
                    if (viewModel.planType == PlanType.FLEXI) viewModel.operatorDetailsFlexi?.typeKey?.toInt()
                            ?: return
                    else viewModel.selectedPlan?.typeKey ?: return
            val flexiKey =
                    if (viewModel.planType == PlanType.FLEXI) viewModel.operatorDetailsFlexi?.flexiKey
                    else null

            val planId = viewModel.selectedPlan?.planId
            val transId = String.format("%040d", BigInteger(randomUUID().toString()
                    .replace("-", ""), 16)).substring(0, 15)
            val account = viewModel.data.value?.get("account_no")?.replace("[+-]".toRegex(), "")
                    ?: return
            val amount = viewModel.data.value?.get("amount") ?: return
            val stdCode = viewModel.data.value?.get("std_code")
            val user = viewModel.user ?: return

            val paymentReq = PaymentRequest(accessKey, typeKey, flexiKey, planId, transId, account, amount, otp, stdCode)

            viewModel.makePayment(user.token, user.sessionId, user.refreshToken, user.customerId.toLong(), paymentReq)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBottomBar()

        attachObservers()
    }

    override fun getLayout(): Int {
        return R.layout.activity_international_top_up
    }

    override fun init() {
        toolbar_back.setOnClickListener { onBackPressed() }
        toolbar_title.text = getString(R.string.international_topup)

        val operator = intent.getParcelableExtra<Operator>("selected_operator") ?: return finish()
        val typeId = intent.getIntExtra("type_id", TypeId.TOP_UP)
        val countryCode = intent.getIntExtra("selected_country_code", 0)

        viewModel.user = UserPreferences.instance.getUser(this)
        viewModel.countryCode = countryCode
        viewModel.typeId = typeId
        viewModel.selectedOperator = operator


        setToolbarTitle(operator.serviceCategory)
        operator.serviceImage?.apply {
            GlideApp.with(operator_logo)
                    .load(this)
                    .placeholder(R.drawable.ic_operator)
                    .error(R.drawable.ic_operator)
                    .into(operator_logo)
        }

        operator_name.text = operator.serviceProvider

        val pagerAdapter = SwipeAdapter(supportFragmentManager,
                getString(R.string.enter_otp),
                getString(R.string.submit),
                otpConfirmListener
        )

        view_pager.adapter = pagerAdapter

        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {}

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}

            override fun onPageSelected(position: Int) {

                viewModel.dataClear = position == 0

                if (position == 3) setPageOTP()
                else setPageDefault(operator)
            }
        })
    }

    private fun initBottomBar() {
        toolbar_back.setOnClickListener(this)
        btm_home.setOnClickListener(this)
        btm_menu_branch.setOnClickListener(this)
        btm_menu_support.setOnClickListener(this)
        btm_menu_loan_calc.setOnClickListener(this)

        toolbar_help.setOnClickListener {
            startHelpActivity("paymentScreenHelp")
        }
    }

    private fun setToolbarTitle(text: String) {
        when (text.toUpperCase()) {
            "DTH" -> toolbar_title.text = text.toUpperCase()
            else -> toolbar_title.text = text.toLowerCase().capitalize()
        }
    }

    private fun setPageOTP() {
        toolbar_title.text = getString(R.string.verify_otp)
        operator_logo.visibility = View.GONE
        operator_name.visibility = View.GONE
    }

    private fun setPageDefault(operator: Operator) {
        setToolbarTitle(operator.serviceCategory)
        operator_logo.visibility = View.VISIBLE
        operator_name.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        if (view_pager.currentItem == 0) {
            super.onBackPressed()
        } else {
            view_pager.currentItem = view_pager.currentItem - 1

        }
    }

    fun navigateUp() {
        val nextPage = view_pager.currentItem + 1
        if (nextPage < view_pager.adapter?.count ?: 0) {
            view_pager.currentItem = nextPage
        }
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
//                    onError(state.message)
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