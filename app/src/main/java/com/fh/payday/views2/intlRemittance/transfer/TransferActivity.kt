package com.fh.payday.views2.intlRemittance.transfer

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.BalanceEventBus
import com.fh.payday.datasource.models.Card
import com.fh.payday.datasource.models.intlRemittance.IntlBeneficiary
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.OnOTPConfirmListener
import com.fh.payday.viewmodels.intlRemittance.TransferViewmodel
import com.fh.payday.views.fragments.OTPFragment
import com.fh.payday.views.fragments.PaymentSuccessfulDialog
import com.fh.payday.views2.intlRemittance.DeliveryModes
import com.fh.payday.views2.intlRemittance.ExchangeAccessKey
import com.fh.payday.views2.intlRemittance.ExchangeContainer
import com.fh.payday.views2.intlRemittance.InternationalRemittanceActivity
import com.fh.payday.views2.intlRemittance.mybeneficiaries.addbeneficiary.CustomerActivationOTPActivity
import com.fh.payday.views2.intlRemittance.mybeneficiaries.addbeneficiary.CustomerActivationOTPActivity.Companion.KEY_ACTIVATION_STATUS
import com.fh.payday.views2.intlRemittance.transfer.fragments.BeneficiaryFragment
import com.fh.payday.views2.intlRemittance.transfer.fragments.IMTAmountFragment
import com.fh.payday.views2.intlRemittance.transfer.fragments.IMTSummaryFragment
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_transfer.*
import kotlinx.android.synthetic.main.layout_stepper_intl_transfer.*
import kotlinx.android.synthetic.main.toolbar.*

class TransferActivity : BaseActivity(), OnOTPConfirmListener {

    lateinit var viewmodel: TransferViewmodel
    override fun getLayout(): Int = R.layout.activity_transfer

    val disposable = CompositeDisposable()

    override fun init() {
        setUpToolbar()
        handleBottomBar()
        viewmodel = ViewModelProviders.of(this).get(TransferViewmodel::class.java)
        val beneficiaryActivity = intent?.getStringExtra("action")
        viewmodel.from = beneficiaryActivity


        if (beneficiaryActivity != null && beneficiaryActivity == "MyBeneficiariesActivity") {
            view_pager.adapter =
                SliderPagerAdapter(myBeneficiaryfragmentList, supportFragmentManager)
            val beneficiary = intent.getParcelableExtra<IntlBeneficiary>("beneficiary")
            val paymentMode = intent.getStringExtra("paymentMode")
            if (paymentMode != "default") {
                viewmodel.selectedDeliveryMode = if (paymentMode == DeliveryModes.BTALTERNATE)
                    DeliveryModes.BT else DeliveryModes.CP
            } else {
                viewmodel.selectedDeliveryMode = intent.getStringExtra("deliveryMode")
            }
            viewmodel.from = beneficiaryActivity

            viewmodel.selectedBeneficiary = beneficiary
            viewmodel.payInCurrency = "AED"
            viewmodel.payoutCurrency = beneficiary.payoutCcyCode
            viewmodel.payoutCountryCode = beneficiary.receiverCountryCode
            viewmodel.selectedAccessKey = beneficiary.accessKey
            setInitialTab(beneficiaryActivity)

        } else if (beneficiaryActivity != null && beneficiaryActivity == "repeat_transaction") {
            view_pager.adapter =
                SliderPagerAdapter(repeatTransactionfragmentList, supportFragmentManager)
            val beneficiary = intent.getParcelableExtra<IntlBeneficiary>("beneficiary")
            val payInAmount = intent.getStringExtra("payinAmount")
            viewmodel.selectedDeliveryMode =
                if (intent.getStringExtra("paymentMode") == DeliveryModes.BTALTERNATE ||
                    intent.getStringExtra("paymentMode") == DeliveryModes.BT) DeliveryModes.BT
                else DeliveryModes.CP
            viewmodel.from = beneficiaryActivity
            viewmodel.payInAmount = payInAmount
            viewmodel.selectedBeneficiary = beneficiary
            viewmodel.payInCurrency = "AED"
            viewmodel.payoutCurrency = beneficiary.payoutCcyCode
            viewmodel.payoutCountryCode = beneficiary.receiverCountryCode
            setInitialTab(beneficiaryActivity)
        } else {
            if (isNotActive()) {
                val intent = Intent(this, CustomerActivationOTPActivity::class.java)
                startActivityForResult(intent, CUSTOMER_ACTIVATION)
            } else {
                val amountFrom = intent.getStringExtra("amountFrom")
                val amountTo = intent.getStringExtra("amountTo")
                viewmodel.filterCcyCode = intent.getStringExtra("payoutCcy")
                viewmodel.filterExchangeName = intent.getStringExtra("exchange")
                viewmodel.amountFrom = amountFrom
                viewmodel.amountTo = amountTo
                viewmodel.selectedAccessKey = intent.getStringExtra("access_key")
                view_pager.adapter = SliderPagerAdapter(fragmentList, supportFragmentManager)
                setInitialTab(beneficiaryActivity)
            }
        }

        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {}
            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}
            override fun onPageSelected(position: Int) {
                if (position == 1) viewmodel.toClear = false
                setSelectedTab(position, beneficiaryActivity)
            }
        })

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    private fun setInitialTab(beneficiaryActivity: String?) {
        setSelectedTab(0, beneficiaryActivity)
    }

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }

    private fun isNotActive() = ExchangeContainer.exchanges().map {
        ExchangeAccessKey.FARD.equals(it.accessKey, true) && it.isActive == "0"
    }.firstOrNull { it } ?: false

    private fun setSelectedTab(position: Int, action: String?) {
        when (position) {
            0 -> {
                when (action) {
                    "repeat_transaction" -> {
                        icon2.setImageResource(R.drawable.ic_intl_beneficiary_grey)
                        icon3.setImageResource(R.drawable.ic_summary_active)
                        icon4.setImageResource(R.drawable.ic_card_details)
                    }
                    "MyBeneficiariesActivity" -> {
                        icon2.setImageResource(R.drawable.ic_intl_beneficiary)
                        icon3.setImageResource(R.drawable.ic_summary)
                        icon4.setImageResource(R.drawable.ic_card_details)
                    }
                    else -> {
                        icon2.setImageResource(R.drawable.ic_intl_beneficiary)
                        icon3.setImageResource(R.drawable.ic_summary)
                        icon4.setImageResource(R.drawable.ic_card_details)
                    }
                }

            }
            1 -> {
                when (action) {
                    "repeat_transaction" -> {
                        icon2.setImageResource(R.drawable.ic_intl_beneficiary_grey)
                        icon3.setImageResource(R.drawable.ic_summary)
                        icon4.setImageResource(R.drawable.ic_card_details_selected)
                    }
                    "MyBeneficiariesActivity" -> {
                        icon2.setImageResource(R.drawable.ic_intl_beneficiary_grey)
                        icon3.setImageResource(R.drawable.ic_summary_active)
                        icon4.setImageResource(R.drawable.ic_card_details)
                    }
                    else -> {
                        icon2.setImageResource(R.drawable.ic_intl_beneficiary)
                        icon3.setImageResource(R.drawable.ic_summary)
                        icon4.setImageResource(R.drawable.ic_card_details)
                    }
                }

            }
            2 -> {
                when (action) {
                    "repeat_transaction" -> {
                    }
                    "MyBeneficiariesActivity" -> {
                        icon2.setImageResource(R.drawable.ic_intl_beneficiary_grey)
                        icon3.setImageResource(R.drawable.ic_summary)
                        icon4.setImageResource(R.drawable.ic_card_details_selected)
                    }
                    else -> {
                        icon2.setImageResource(R.drawable.ic_intl_beneficiary_grey)
                        icon3.setImageResource(R.drawable.ic_summary_active)
                        icon4.setImageResource(R.drawable.ic_card_details)
                    }
                }

            }

            3 -> {
                icon2.setImageResource(R.drawable.ic_intl_beneficiary_grey)
                icon3.setImageResource(R.drawable.ic_summary)
                icon4.setImageResource(R.drawable.ic_card_details_selected)

            }

            4 -> {
                icon2.setImageResource(R.drawable.ic_intl_beneficiary_grey)
                icon3.setImageResource(R.drawable.ic_summary)
                icon4.setImageResource(R.drawable.ic_card_details_selected)
            }

        }
    }

    private fun setUpToolbar() {
        toolbar_back.setOnClickListener(this)
        toolbar_title.text = getString(R.string.intl_money_transfer)
    }

    private fun handleBottomBar() {
        findViewById<TextView>(R.id.btm_home).setOnClickListener(this)
        findViewById<TextView>(R.id.btm_menu_branch).setOnClickListener(this)
        findViewById<TextView>(R.id.btm_menu_support).setOnClickListener(this)
        findViewById<TextView>(R.id.btm_menu_loan_calc).setOnClickListener(this)
        findViewById<TextView>(R.id.toolbar_help).setOnClickListener(this)
    }

    fun navigateUp() {
        val nextPage = view_pager.currentItem + 1
        if (nextPage < view_pager.adapter?.count ?: 0) {
            view_pager.currentItem = nextPage
        }
    }

    override fun onBackPressed() {
        if (view_pager.currentItem == 0)
            super.onBackPressed()
        else
            view_pager.currentItem = view_pager.currentItem - 1
    }

    override fun onOtpConfirm(otp: String) {
        val user = UserPreferences.instance.getUser(this) ?: return
        val beneficiary = viewmodel.selectedBeneficiary ?: return
        val payoutCurrency = viewmodel.payoutCurrency ?: return
        val totalAmount = viewmodel.totalAmount ?: return
        val baseAmount = viewmodel.payInAmount ?: return
        val promoCode = viewmodel.promoCode ?: ""
        val receiverCountryCode = beneficiary.receiverCountryCode

        addObserver()

        viewmodel.transferFunds(
            user.token,
            user.sessionId,
            user.refreshToken,
            user.customerId.toLong(),
            beneficiary.receiverFirstName,
            beneficiary.receiverLastName,
            beneficiary.recieverRefNumber,
            beneficiary.receiverBankAccountNo ?: "",
            beneficiary.receiverBankName,
            receiverCountryCode,
            baseAmount.replace(",", ""),
            totalAmount.replace(",", ""),
            payoutCurrency,
            promoCode,
            viewmodel.selectedAccessKey ?: return,
            otp,
            viewmodel.fxQuoteNo ?: return,
            if (viewmodel.selectedDeliveryMode == DeliveryModes.BT ||
                viewmodel.selectedDeliveryMode == DeliveryModes.BTALTERNATE
            ) DeliveryModes.BTALTERNATE else DeliveryModes.CPALTERNATE
        )
    }


    class SliderPagerAdapter(
        private val fragmentList: Array<Fragment>,
        val fragmentManager: FragmentManager
    ) : FragmentStatePagerAdapter(fragmentManager) {

        override fun getItem(p0: Int): Fragment = fragmentList[p0]
        override fun getCount(): Int = fragmentList.size
    }


    private val fragmentList by lazy {
        val fragment = BeneficiaryFragment()
        val bundle = Bundle()
        bundle.putString("action", viewmodel.from)
        fragment.arguments = bundle
        arrayOf(
//            IntlBeneficiaryFragment(),
            fragment,
            IMTAmountFragment(),
            IMTSummaryFragment(),
            OTPFragment.Builder(this)
                .setPinLength(6)
                .setTitle(getString(R.string.enter_otp))
                .setInstructions(null)
                .setButtonTitle(getString(R.string.submit))
                .setHasCardView(false)
                .build()
        )
    }


    private val myBeneficiaryfragmentList by lazy {
        arrayOf(
            IMTAmountFragment(),
            IMTSummaryFragment(),
            OTPFragment.Builder(this)
                .setPinLength(6)
                .setTitle(getString(R.string.enter_otp))
                .setInstructions(null)
                .setButtonTitle(getString(R.string.submit))
                .setHasCardView(false)
                .build()
        )
    }

    private val repeatTransactionfragmentList by lazy {
        arrayOf(
            IMTSummaryFragment.build(
                type = IMTSummaryFragment.TYPE_REPEAT_TRANS,
                transId = intent.getStringExtra("transactions_id"),
                partnerTxnRefNo = intent.getStringExtra("partnerTxnRefNo"),
                accessKey = intent.getStringExtra("accessKey"),
                paymentMode = intent.getStringExtra("paymentMode")
            ),
            OTPFragment.Builder(this)
                .setPinLength(6)
                .setTitle(getString(R.string.enter_otp))
                .setInstructions(null)
                .setButtonTitle(getString(R.string.submit))
                .setHasCardView(false)
                .build()
        )
    }

    private fun addObserver() {
        viewmodel.funTransferResponse.observe(this, Observer {
            it ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                showProgress(getString(R.string.processing))
                return@Observer
            }
            hideProgress()

            when (state) {
                is NetworkState2.Success -> {
                    val data = state.data ?: return@Observer

                    val message =
                        getString(R.string.uae_exchange_transfer_success).format(data.fhTransactionRef)

                    val dialog = PaymentSuccessfulDialog.newInstance(
                        message,
                        getString(R.string.amount_in_aed).format(data.availableBalance),
                        R.drawable.ic_success_checked,
                        R.color.blue, Gravity.START
                    ) { d -> d.dismiss(); startIntlRemittanceActivity(data.availableBalance) }
                    dialog.show(supportFragmentManager, "success")
                    dialog.isCancelable = false
                }
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        return@Observer onSessionExpired(state.message)
                    }
                    handleErrorCode(state.errorCode.toInt(), state.message)
                }
                is NetworkState2.Failure -> onFailure(
                    findViewById(R.id.root_view),
                    getString(R.string.request_error)
                )
                else -> onFailure(findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }


    private fun startIntlRemittanceActivity(newBalance: String) {
        try {
            BalanceEventBus.sendEvent(newBalance.toDouble())
        } catch (e: NumberFormatException) {
            onFailure(findViewById(R.id.root_view), getString(R.string.something_went_wrong))
        }

        val i = Intent(this@TransferActivity, InternationalRemittanceActivity::class.java)
        intent?.removeExtra("paymentMode")
        intent?.removeExtra("deliveryMode")
        intent?.removeExtra("action")
        intent?.let { i.putExtras(it) }
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(i)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return

        val code = data?.getIntExtra(KEY_ACTIVATION_STATUS, -1) ?: -1
        if (requestCode == CUSTOMER_ACTIVATION && code == -1) {
            finish()
        } else {
            val amountFrom = intent.getStringExtra("amountFrom")
            val amountTo = intent.getStringExtra("amountTo")
            viewmodel.filterCcyCode = intent.getStringExtra("payoutCcy")
            viewmodel.filterExchangeName = intent.getStringExtra("exchange")
            viewmodel.amountFrom = amountFrom
            viewmodel.amountTo = amountTo
            viewmodel.selectedAccessKey = intent.getStringExtra("access_key")
            view_pager.adapter = SliderPagerAdapter(fragmentList, supportFragmentManager)
            setInitialTab(intent?.getStringExtra("action"))
        }
    }

    interface OnIntlBeneficiaryClickListener {
        fun onIntlBeneficiaryClick(position: Int, beneficiary: IntlBeneficiary)
    }

    interface OnCardClickListener {
        fun onCardClick(position: Int, card: Card)
    }

    companion object {
        private const val CUSTOMER_ACTIVATION = 3
    }
}
