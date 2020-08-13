package com.fh.payday.views2.campaign

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.content.LocalBroadcastManager
import android.view.View
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.models.CustomerSummary
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.Action
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.LOAN_TC_URL
import com.fh.payday.viewmodels.CampaignViewModel
import com.fh.payday.viewmodels.LoanViewModel
import com.fh.payday.views2.auth.LoginActivity
import com.fh.payday.views2.loan.apply.ApplyLoanActivity
import com.fh.payday.views2.loan.apply.TermsAndConditionsDialog
import com.fh.payday.views2.shared.fragments.TermsConditionsDialogFragment
import kotlinx.android.synthetic.main.activity_campaign.*
import kotlinx.android.synthetic.main.bottombar_dashboard.*
import kotlinx.android.synthetic.main.toolbar.*

class CampaignActivity : BaseActivity() {

    private val viewModel: CampaignViewModel by lazy {
        ViewModelProviders.of(this).get(CampaignViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.user = UserPreferences.instance.getUser(this) ?: return startLoginActivity()

        btm_home.setOnClickListener(this)
        btm_menu_support.setOnClickListener(this)
        btm_menu_loan_calc.setOnClickListener(this)
        btm_menu_branch.setOnClickListener(this)
        toolbar_back.setOnClickListener(this)
        toolbar_help.setOnClickListener(this)

        UserPreferences.instance.getUser(this)?.let {
            viewModel.fetchCustomerSummary(
                    it.token, it.sessionId, it.refreshToken, it.customerId.toLong()
            )
        }.also { addCustomerStateObserver() }

        addSnoozeObserver()
    }

    override fun getLayout() = R.layout.activity_campaign

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, i: Intent) {
            if (intent.extras != null) {
                clearNotification()
                setLoanOffer(i)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter(Action.ACTION_OFFER))
        clearNotification()
    }

    override fun onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        super.onStop()
    }

    override fun init() {
        toolbar_title.text = getString(R.string.campaign_management)
        setLoanOffer(intent)

        btn_snooze.setOnClickListener {
            SnoozeDialogFragment.Builder()
                    .days(resources.getStringArray(R.array.snooze).toCollection(ArrayList()))
                    .attachClickListener {
                        val days = it.replace("\\D".toRegex(), "").toInt()
                        val user = viewModel.user ?: return@attachClickListener
                        viewModel.updateSnoozeDays(user.token, user.sessionId, user.refreshToken,
                                user.customerId.toLong(), days)
                    }
                    .build()
                    .show(supportFragmentManager, "dialog")
        }

    }

    private fun setLoanOffer(intent: Intent) {
        if (!viewModel.isValidOffer(intent.extras)) {
            return finish()
        }

        tv_title.text = intent.getStringExtra("title")
        tv_description.text = intent.getStringExtra("body")
    }

    override fun startLoginActivity() {
        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("body")
        val expiryDays = intent.getStringExtra("expiryDays")
        val notificationId = intent.getStringExtra("notificationId")
        val productType = intent.getStringExtra("productType")

        val intent = Intent(Intent(this, LoginActivity::class.java)).apply {
            putExtra("title", title)
            putExtra("body", description)
            putExtra("expiryDays", expiryDays)
            putExtra("notificationId", notificationId)
            putExtra("productType", productType)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(intent)
        intent.removeExtra("title")
        finish()
    }

    private fun addSnoozeObserver() {
        viewModel.snoozeState.observe(this, Observer {
            it ?: return@Observer

            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                return@Observer showProgress(getString(R.string.processing))
            }

            hideProgress()

            when (state) {
                is NetworkState2.Success -> {
                    showMessage(state.data ?: return@Observer,
                            R.drawable.ic_success_checked_blue,
                            R.color.blue,
                            com.fh.payday.views2.shared.custom.AlertDialogFragment.OnConfirmListener { dialog ->
                                dialog.dismiss()
                                onBackPressed()
                            })

                }
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        return@Observer onSessionExpired(state.message)
                    }
                    onError(state.message)
                }
                is NetworkState2.Failure -> onFailure(findViewById(R.id.root_view), getString(R.string.request_error))
                else -> onFailure(findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    private fun addCustomerStateObserver() {

        fun showProgress() {
            tv_title.visibility = View.GONE
            tv_description.visibility = View.GONE
            btn_snooze.visibility = View.GONE
            btn_apply.visibility = View.GONE
            progress_bar.visibility = View.VISIBLE
        }

        fun hideProgress() {
            progress_bar.visibility = View.GONE
            tv_title.visibility = View.VISIBLE
            tv_description.visibility = View.VISIBLE
            btn_snooze.visibility = View.VISIBLE
            btn_apply.visibility = View.VISIBLE
        }

        viewModel.customerSummaryState.observe(this, Observer {
            it ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                return@Observer showProgress()
            }

            hideProgress()

            when (state) {
                is NetworkState2.Success -> onSuccess(state.data)
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        return@Observer onSessionExpired(state.message)
                    }
                    onError(state.message)
                }
                is NetworkState2.Failure -> onFailure(findViewById(R.id.root_view), getString(R.string.request_error))
                else -> onFailure(findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })

    }

    private fun onSuccess(data: CustomerSummary?) {
        data ?: return

        btn_apply.setOnClickListener {
            val termsAndCondDialog = TermsAndConditionsDialog.Builder()
                    .setTermsConditionsLink(true)
                    .attachPositiveListener {
                        finish()
                        startActivity(Intent(this, ApplyLoanActivity::class.java).apply {
                            val productType = intent?.getStringExtra("productType")
                                    ?: LoanViewModel.LOAN
                            putExtra("product_type", productType)
                            putExtra("from_push_notification", true)

                            if (data.loans.isNotEmpty()) {
                                putExtra("loan_number", data.loans[0].loanNumber)
                            }

                        })
                    }
                    .attachLinkListener {
                        val dialogFragment: DialogFragment = TermsConditionsDialogFragment
                                .newInstance(LOAN_TC_URL, getString(R.string.close))
                        dialogFragment.show(supportFragmentManager, "dialog")
                    }
                    .build()

            termsAndCondDialog.show(supportFragmentManager, termsAndCondDialog.tag)
        }
    }
}
