package com.fh.payday.views2.payments.ibp

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.View
import android.view.WindowManager
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.models.payments.*
import com.fh.payday.datasource.models.payments.ServiceCategory.Companion.ELECTRICITY
import com.fh.payday.datasource.models.payments.ServiceCategory.Companion.INSURANCE
import com.fh.payday.datasource.models.payments.ServiceCategory.Companion.LANDLINE
import com.fh.payday.datasource.models.payments.ServiceCategory.Companion.POSTPAID
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.GlideApp
import com.fh.payday.utilities.OnOTPConfirmListener
import com.fh.payday.viewmodels.payments.ibp.IndianBillPaymentViewModel
import com.fh.payday.views.fragments.PaymentSuccessfulDialog
import kotlinx.android.synthetic.main.activity_indian_bill_payment_activity.*
import kotlinx.android.synthetic.main.bottombar_dashboard.*
import kotlinx.android.synthetic.main.toolbar.*


class IndianBillPaymentActivity : BaseActivity() {

    val viewModel: IndianBillPaymentViewModel by lazy {
        ViewModelProviders.of(this).get(IndianBillPaymentViewModel::class.java)
    }

    private val otpConfirmListener = object : OnOTPConfirmListener {
        override fun onOtpConfirm(otp: String) {
            if (otp.length < 6) return

            val user = viewModel.user ?: return

            val accessKey = viewModel.selectedOperator?.accessKey ?: return
            val typeKey =
                    if (viewModel.planType == PlanType.FLEXI) viewModel.operatorDetailsFlexi?.typeKey?.toInt()
                            ?: return
                    else viewModel.selectedPlan?.typeKey ?: return
            val flexiKey =
                    if (viewModel.planType == PlanType.FLEXI) viewModel.operatorDetailsFlexi?.flexiKey
                    else null //viewModel.selectedPlan?.planId
            val planId = viewModel.selectedPlan?.planId
            val transId = viewModel.generateTransId()
            val account = viewModel.data.value?.get("account_no") ?: return
            val amount = viewModel.data.value?.get("amount") ?: return
            val optional1 = getOptional1()
            val optional4 = getOptional4()


            val paymentReq = PaymentRequest(accessKey, typeKey, flexiKey, planId, transId, account,
                    amount, otp, optional1, optional4 = optional4)
            viewModel.makePayment(user.token, user.sessionId, user.refreshToken, user.customerId.toLong(), paymentReq)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        toolbar_back.setOnClickListener(this)
        btm_home.setOnClickListener(this)
        btm_menu_branch.setOnClickListener(this)
        btm_menu_support.setOnClickListener(this)
        btm_menu_loan_calc.setOnClickListener(this)

        toolbar_help.setOnClickListener {
            startHelpActivity("paymentScreenHelp")
        }

        attachObservers()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

    }

    override fun getLayout() = R.layout.activity_indian_bill_payment_activity

    override fun init() {
        toolbar_back.setOnClickListener { onBackPressed() }

        val operator = intent.getParcelableExtra<Operator>("selected_operator") ?: return finish()
        val typeId = intent.getIntExtra("type_id", TypeId.TOP_UP)
        viewModel.selectedOperator = operator
        viewModel.typeId = typeId
        viewModel.user = UserPreferences.instance.getUser(this)

        setToolbarTitle(operator.serviceCategory)

        operator.serviceImage?.apply {
            GlideApp.with(operator_logo)
                    .load(this)
                    .placeholder(R.drawable.ic_operator)
                    .error(R.drawable.ic_operator)
                    .into(operator_logo)
        }
        operator_name.text = operator.serviceProvider

        view_pager.adapter = when (operator.serviceCategory) {
            LANDLINE -> {
                if (operator.accessKey in arrayOf(BSNL_LANDLINE_ACCESS_KEY, MTNL_LANDLINE_ACCESS_KEY)) {
                    SwipeAdapter(supportFragmentManager, getLandlineItemsWithAccountNo(
                            getString(R.string.enter_otp), getString(R.string.submit), otpConfirmListener,getString(R.string.otp_sent_to_registered_number) ))
                } else {
                    SwipeAdapter(supportFragmentManager, getLandlineItems(
                            getString(R.string.enter_otp), getString(R.string.submit), otpConfirmListener, getString(R.string.otp_sent_to_registered_number)))
                }
            }
            INSURANCE -> SwipeAdapter(supportFragmentManager, getInsuranceItems(
                    getString(R.string.enter_otp), getString(R.string.submit), otpConfirmListener, getString(R.string.otp_sent_to_registered_number)))
            else -> if (operator.isFixedAvailable == 1 && ServiceCategory.PREPAID.equals(operator.serviceCategory, true) ){
                SwipeAdapter(supportFragmentManager, getFixedItems(getString(R.string.enter_otp),
                        getString(R.string.submit), otpConfirmListener, getString(R.string.otp_sent_to_registered_number)))
            }else {
                SwipeAdapter(supportFragmentManager, getItems(getString(R.string.enter_otp),
                        getString(R.string.submit), otpConfirmListener, getString(R.string.otp_sent_to_registered_number)))
            }
        }

        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {}

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}

            override fun onPageSelected(position: Int) {

                when {
                    operator.serviceCategory == LANDLINE -> clearLandLineData(position)
                    operator.serviceCategory == INSURANCE -> clearInsuranceData(position)
                    else -> clearData(position)
                }
                when (operator.accessKey) {
                    BSNL_LANDLINE_ACCESS_KEY, AIRTEL_LANDLINE_ACCESS_KEY, RELIANCE_LANDLINE_ACCESS_KEY, MTNL_LANDLINE_ACCESS_KEY, DOCOMO_LANDLINE_ACCESS_KEY -> {
                        if (position == 0) {
                            resetViewPager()
                        }
                    }
                }

                val count = view_pager?.adapter?.count ?: 4
                val otpPosition = count - 1

                when (position) {
                    otpPosition -> setPageOTP()
                    else -> setPageDefault(operator)
                }
            }
        })
    }

    private fun clearData(position: Int) {
        viewModel.dataClear = position == 0
    }

    private fun clearInsuranceData(position: Int) {
        if (position == 0)
            viewModel.dataClear = true
        else
            viewModel.dataClear = position == 1 && viewModel.dataClear
    }

    private fun clearLandLineData(position: Int) {
        if (position == 0)
            viewModel.dataClear = true
        else
            viewModel.dataClear = position == 1 && viewModel.dataClear

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

        viewModel.blbState.observe(this, Observer {
            if (it == true) {
                (view_pager.adapter as SwipeAdapter?)?.apply {
                    deletePage(1)
                    deletePage(1)

                    val accountNo = viewModel.data.value?.get("account_no") ?: return@Observer
                    val (_, accessKey, _, isFlexAvailable) = viewModel.selectedOperator
                            ?: return@Observer
                    val user = viewModel.user ?: return@Observer
                    val planType = if (isFlexAvailable == 1) PlanType.FLEXI else PlanType.FIXED

                    viewModel.operatorDetails(user.token, user.sessionId, user.refreshToken, user.customerId.toLong(),
                            accountNo, accessKey, viewModel.typeId ?: return@Observer,
                            planType, CountryCode.INDIA)
                }
            }
        })

        viewModel.landLineState.observe(this, Observer {
            if (it == true) {
                (view_pager.adapter as SwipeAdapter?)?.apply {
                    deletePage(1)

                    val accountNo = viewModel.data.value?.get("account_no") ?: return@Observer
                    val (_, accessKey, _, isFlexAvailable) = viewModel.selectedOperator
                            ?: return@Observer
                    val user = viewModel.user ?: return@Observer
                    val planType = if (isFlexAvailable == 1) PlanType.FLEXI else PlanType.FIXED

                    viewModel.operatorDetails(user.token, user.sessionId, user.refreshToken, user.customerId.toLong(),
                            accountNo, accessKey, viewModel.typeId ?: return@Observer,
                            planType, CountryCode.INDIA)
                }
            }
        })
    }

    fun resetViewPager() {
        (view_pager.adapter as SwipeAdapter?)?.reset()
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

    private fun getOptional1(): String? {
        return when (viewModel.selectedOperator?.serviceCategory ?: return null) {
            LANDLINE -> viewModel.data.value?.get("bsnl_account")
            INSURANCE -> viewModel.data.value?.get("dob")
            else -> null
        }
    }

    private fun getOptional4(): String {
        return when (viewModel.selectedOperator?.serviceCategory ?: return "") {
            LANDLINE, POSTPAID, ELECTRICITY -> viewModel.data.value?.get("reference_id") ?: ""
            else -> ""
        }
    }
}
