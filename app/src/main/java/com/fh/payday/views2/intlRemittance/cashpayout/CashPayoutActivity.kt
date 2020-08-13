package com.fh.payday.views2.intlRemittance.cashpayout

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.models.login.User
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.OnOTPConfirmListener
import com.fh.payday.viewmodels.intlRemittance.AddBeneficiaryViewModel
import com.fh.payday.views.fragments.OTPFragment
import com.fh.payday.views2.intlRemittance.ExchangeContainer
import com.fh.payday.views2.intlRemittance.cashpayout.AddBenificiaryCashPayoutFragment.Companion.TYPE_DEFAULT
import com.fh.payday.views2.intlRemittance.cashpayout.AddBenificiaryCashPayoutFragment.Companion.TYPE_SUMMARY
import com.fh.payday.views2.intlRemittance.cashpayout.SearchAgentFragment.Companion.TYPE_BANK
import com.fh.payday.views2.intlRemittance.cashpayout.SearchAgentFragment.Companion.TYPE_LOCATION
import com.fh.payday.views2.intlRemittance.mybeneficiaries.addbeneficiary.SwipeAdapter
import com.fh.payday.views2.shared.custom.AlertDialogFragment
import kotlinx.android.synthetic.main.activity_cash_payout.*
import kotlinx.android.synthetic.main.toolbar.*

class CashPayoutActivity : BaseActivity(), OnOTPConfirmListener {

    val viewModel by lazy { ViewModelProviders.of(this).get(AddBeneficiaryViewModel::class.java) }
    lateinit var user: User



    override fun getLayout() = R.layout.activity_cash_payout

    override fun init() {
        user = UserPreferences.instance.getUser(this) ?: return
        intent?.apply {
            viewModel.selectedDeliveryMode = this.getStringExtra("deliveryMode")
        }
        attachObserver()
        attachFavObserver()
        setUpToolbar()
        handleBottomBar()
        view_pager.adapter = SwipeAdapter(supportFragmentManager, fragmentList)
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {}
            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}
            override fun onPageSelected(position: Int) {
                setToolBarTitle(position)
                if (position == 1) {
                    viewModel.clearData = false
                    viewModel.clearBankData = false
                }
                if (position == 0) {
                    viewModel.clearBankDetails = false
                }
            }
        })

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)


    }

    private fun attachObserver() {
        viewModel.createBeneficiaries.observe(this, Observer {
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
                    if (viewModel.flag == 1) {
                        callFavouriteApi(data.newBeneficiaryNo)
                        return@Observer
                    }

                    AlertDialogFragment.Builder()
                            .setMessage(resources.getString(R.string.add_beneficiary_success))
                            .setIcon(R.drawable.ic_success_checked_blue)
                            .setTintColor(R.color.blue)
                            .setCancelable(false)
                            .setButtonVisibility(View.GONE)
                            .build()
                            .show(supportFragmentManager, "")

                    Handler().postDelayed({
                        val returnIntent = Intent()
                        returnIntent.apply {
                            setResult(Activity.RESULT_OK, returnIntent)
                        }
                        finish()
                    }, 2000)
                }
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        return@Observer onSessionExpired(state.message)
                    }
                    handleErrorCode(state.errorCode.toInt(), state.message)
                }
                is NetworkState2.Failure -> onFailure(findViewById(R.id.root_view),
                        getString(R.string.request_error))
                else -> onFailure(findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    private fun attachFavObserver() {
        viewModel.addToFavBeneficiary.observe(this, Observer {
            it ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {

                return@Observer
            }
            hideProgress()

            when (state) {
                is NetworkState2.Success -> {
                    AlertDialogFragment.Builder()
                            .setMessage(resources.getString(R.string.add_beneficiary_success))
                            .setIcon(R.drawable.ic_success_checked_blue)
                            .setTintColor(R.color.blue)
                            .setCancelable(false)
                            .setButtonVisibility(View.GONE)
                            .build()
                            .show(supportFragmentManager, "")

                    Handler().postDelayed({
                        val returnIntent = Intent()
                        returnIntent.apply {
                            setResult(Activity.RESULT_OK, returnIntent)
                        }
                        finish()
                    }, 2000)
                }
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        return@Observer onSessionExpired(state.message)
                    }
                    handleErrorCode(state.errorCode.toInt(), state.message)
                }
                is NetworkState2.Failure -> {
                    onFailure(findViewById(R.id.root_view),
                            getString(R.string.request_error))
                }
                else -> {
                    onFailure(findViewById(R.id.root_view), CONNECTION_ERROR)
                }
            }
        })

    }


    private fun setUpToolbar() {
        toolbar_back.setOnClickListener(this)
        toolbar_title.text = getString(R.string.add_beneficiary)
    }

    private fun setToolBarTitle(position: Int) {
        when (position) {
            0 -> {
                toolbar_title.text = getString(R.string.add_beneficiary)
            }
            1 -> {
                toolbar_title.text = getString(R.string.find_bank)
            }
            2 -> {
                toolbar_title.text = getString(R.string.find_agent)
            }
            3 -> {
                toolbar_title.text = getString(R.string.beneficiary_summary)
            }
            4 -> {
                toolbar_title.text = getString(R.string.verify_otp)
            }
        }
    }


    fun navigateUp() {
        val nextPage = view_pager.currentItem + 1
        if (nextPage < view_pager.adapter?.count ?: 0) {
            view_pager.currentItem = nextPage
        }

    }

    var firstPage=false
    override fun onBackPressed() {
        when (view_pager.currentItem) {
            0 -> {super.onBackPressed()
                firstPage = true}
            /*(view_pager.adapter?.count ?: -1) - 2 -> {
                view_pager.currentItem = view_pager.currentItem - 2
            }*/
            else -> view_pager.currentItem = view_pager.currentItem - 1
        }
    }


    private fun handleBottomBar() {
        findViewById<TextView>(R.id.btm_home).setOnClickListener(this)
        findViewById<TextView>(R.id.btm_menu_branch).setOnClickListener(this)
        findViewById<TextView>(R.id.btm_menu_support).setOnClickListener(this)
        findViewById<TextView>(R.id.btm_menu_loan_calc).setOnClickListener(this)
        findViewById<TextView>(R.id.toolbar_help).setOnClickListener(this)
    }

    private val fragmentList by lazy {
        arrayListOf<Fragment>(
                AddBenificiaryCashPayoutFragment.newInstance(TYPE_DEFAULT),
                SearchAgentFragment.newInstance(TYPE_BANK),
                SearchAgentFragment.newInstance(TYPE_LOCATION),
                AddBenificiaryCashPayoutFragment.newInstance(TYPE_SUMMARY),
                OTPFragment.Builder(this)
                        .setPinLength(6)
                        .setTitle(getString(R.string.enter_otp))
                        .setInstructions(null)
                        .setButtonTitle(getString(R.string.submit))
                        .setHasCardView(false)
                        .build()


        )
    }

    override fun onOtpConfirm(otp: String) {
       viewModel.createBeneficiaries(
            user.token,
            user.sessionId,
            user.refreshToken,
            user.customerId.toLong(),
            ExchangeContainer.accessKey,
            viewModel.inputWrapper,
        viewModel.payoutAgentId ?: return,
        viewModel.payoutBranchId ?: return,
            otp)

    }

    private fun callFavouriteApi(receiverRefNum: String) {

        //todo remove hard coded value for access key
        viewModel.addToFavBeneficiary(
                user.token,
                user.sessionId,
                user.refreshToken,
                user.customerId.toLong(),
                ExchangeContainer.accessKey,
                receiverRefNum,
                viewModel.flag)
    }

}


