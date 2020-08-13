package com.fh.payday.views2.intlRemittance

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.view.View
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.models.Card
import com.fh.payday.datasource.models.Item
import com.fh.payday.datasource.models.intlRemittance.CustomerDetail
import com.fh.payday.datasource.models.intlRemittance.Exchange
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.ItemOffsetDecoration
import com.fh.payday.utilities.OnItemClickListener
import com.fh.payday.viewmodels.intlRemittance.IntlRemmittanceViewModel
import com.fh.payday.views.adapter.OperatorAdapter
import com.fh.payday.views2.dashboard.MainActivity
import com.fh.payday.views2.intlRemittance.mybeneficiaries.MyBeneficiariesActivity
import com.fh.payday.views2.intlRemittance.rateCalculator.RateCalculatorActivity
import com.fh.payday.views2.intlRemittance.tracktransaction.TrackTransactionActivity
import com.fh.payday.views2.intlRemittance.transactionhistory.IntlTransactionHistoryActivity
import com.fh.payday.views2.intlRemittance.transfer.TransferActivity
import com.fh.payday.views2.kyc.update.KycOptionActivity
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity
import com.fh.payday.views2.locator.BranchLocationActivity
import com.fh.payday.views2.locator.LocatorActivity
import com.fh.payday.views2.message.ContactUsActivity
import com.fh.payday.views2.shared.custom.AlertDialogFragment
import com.fh.payday.views2.shared.custom.EligibilityDialogFragment
import com.fh.payday.views2.shared.custom.InvalidCustomerDialogFragment
import com.fh.payday.views2.shared.custom.ProgressDialogFragment
import kotlinx.android.synthetic.main.activity_international_remittance.*
import kotlinx.android.synthetic.main.toolbar.*


class InternationalRemittanceActivity : BaseActivity(), OnItemClickListener {
    private var flag = true

    private val viewModel: IntlRemmittanceViewModel by lazy {
        ViewModelProviders.of(this).get(IntlRemmittanceViewModel::class.java)
    }

    override fun getLayout(): Int = R.layout.activity_international_remittance

    override fun init() {
        setUpToolbar()
        handleBottomBar()
        addObserver()

        rv_remitt_options.layoutManager = GridLayoutManager(this, 2)
        rv_remitt_options.addItemDecoration(ItemOffsetDecoration(3))
    }

    override fun onResume() {
        super.onResume()
        val user = UserPreferences.instance.getUser(this) ?: return
        viewModel.getCustomerState(user.token, user.sessionId, user.refreshToken, user.customerId.toString())
        flag = true
    }

    private fun setUpToolbar() {
        toolbar_title.text = getString(R.string.international_remittance)
        toolbar_back.setOnClickListener(this)
    }

    private fun setUpOptionMenu(isAllowed: Boolean) {
        if (isAllowed)
            rv_remitt_options.adapter = OperatorAdapter(isAllowedListener, getItems(), 3)
        else
            rv_remitt_options.adapter = OperatorAdapter(notAllowedListener, getItems(), 3)
    }

    fun sendEmiratesId(emiratesCard: String) {
        val user = UserPreferences.instance.getUser(this) ?: return
        addFindCustomerObserver()
        viewModel.findCustomer(user.token, user.sessionId, user.refreshToken, user.customerId.toString(), emiratesCard)
    }

    override fun showProgress(message: String, cancellable: Boolean, dismissListener: ProgressDialogFragment.OnDismissListener) {
        layout.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
        progress_bar.visibility = View.VISIBLE
        rv_remitt_options.visibility = View.GONE
    }

    override fun hideProgress() {
        layout.setBackgroundColor(ContextCompat.getColor(this, R.color.grey_300))
        rv_remitt_options.visibility = View.VISIBLE
        progress_bar.visibility = View.GONE
    }

    private fun addObserver() {
        viewModel.customerState.observe(this, Observer {
            it ?: return@Observer

            val customerState = it.getContentIfNotHandled() ?: return@Observer

            if (customerState is NetworkState2.Loading<List<Exchange>>) {
                showProgress(getString(R.string.fetching_account_details))
                return@Observer
            }

            hideProgress()

            when (customerState) {
                is NetworkState2.Success -> {
                    val exchanges = customerState.data ?: return@Observer
                    exchanges.forEach { exchange ->
                        if (exchange.emailId.isNullOrEmpty()) {
                            showEmailDialog()
                            return@Observer
                        }
                    }

//                    addGraphTransactions()
//                    getGraphTransactions()
                    setUpOptionMenu(flag)
                    return@Observer
                }

                is NetworkState2.Error -> {
                    if (customerState.isSessionExpired) {
                        onSessionExpired(customerState.message)
                        return@Observer
                    }

                    val (message) = customerState

//                    if (customerState.errorCode.toInt() == 301) {
//                        val fragment = EmiratesIdInputDialogFragment()
//                        fragment.isCancelable = false
//                        fragment.show(supportFragmentManager, "EmiratesInput")
//                    } else {
//                        handleErrorCode(customerState.errorCode.toInt(), message)
//                    }
                    if (customerState.errorCode.toInt() == 301 ) {

                        // comment out for Alfardan, this needs to be handled in case of uae exchange only (error code 301)
                      /*  val fragment = InvalidCustomerDialogFragment()
                        fragment.isCancelable = false
                        fragment.arguments = Bundle().also { b ->
                            b.putString("message", message)
                        }
                        fragment.show(supportFragmentManager, "EmiratesExpire")*/

                        showMessage(message,
                            R.drawable.ic_error,
                            R.color.colorError,
                            AlertDialogFragment.OnConfirmListener { dialog->
                                dialog.dismiss()
                            }
                        )
                    } else if (customerState.errorCode.toInt() == 304 ){
                        showMessage(message)
                    }else  if (customerState.errorCode.toInt() == 303) {
                        if (flag)
                        showRemitDialog(customerState.message)
                    } else {
                        handleErrorCode(customerState.errorCode.toInt(), message)
                    }

                    setUpOptionMenu(false)
                }

                is NetworkState2.Failure -> {
                    onFailure(findViewById(R.id.layout), customerState.throwable)
                    setUpOptionMenu(false)
                }
            }
        })
    }

    private fun showEmailDialog() {
        EligibilityDialogFragment.Builder(object : EligibilityDialogFragment.OnDismissListener {
            override fun onDismiss(dialog: DialogInterface) {

            }

        }).setBtn1Text(getString(R.string.ok))
            .setBtn2Text(getString(R.string.cancel))
            .setTitle(getString(R.string.update_exchange_email))
            .setConfirmListener(object : EligibilityDialogFragment.OnConfirmListener {
                override fun onConfirm(dialog: DialogInterface) {
                    startActivity(Intent(this@InternationalRemittanceActivity,
                        KycOptionActivity::class.java).apply {
                        putExtra("index", 0)
                    })
                    dialog.dismiss()
                }

            }).setCancelListener(object : EligibilityDialogFragment.OnCancelListener {
                override fun onCancel(dialog: DialogInterface) {
                    finish()
                }
            })
            .build()
            .show(supportFragmentManager, "EmailUpdate")
    }

    private fun showRemitDialog(message: String) {
        EligibilityDialogFragment.Builder(object : EligibilityDialogFragment.OnDismissListener {
            override fun onDismiss(dialog: DialogInterface) {

            }

        }).setBtn1Text(getString(R.string.confirm))
            .setBtn2Text(getString(R.string.cancel))
            .setTitle(message)
            .setConfirmListener(object : EligibilityDialogFragment.OnConfirmListener {
                override fun onConfirm(dialog: DialogInterface) {
                    dialog.dismiss()
                    val user = UserPreferences.instance.getUser(this@InternationalRemittanceActivity) ?: return
                    viewModel.getCustomerState(user.token, user.sessionId, user.refreshToken, user.customerId.toString(), "1")
                }

            }).setCancelListener(object : EligibilityDialogFragment.OnCancelListener {
                override fun onCancel(dialog: DialogInterface) {
                    dialog.dismiss()
                    flag = false
                    val user = UserPreferences.instance.getUser(this@InternationalRemittanceActivity) ?: return
                    viewModel.getCustomerState(user.token, user.sessionId, user.refreshToken, user.customerId.toString(), "0")
                }
            })
            .build()
            .show(supportFragmentManager, "EmailUpdate")
    }

    private fun addFindCustomerObserver() {
        viewModel.searchCustomerState.observe(this, Observer {
            it ?: return@Observer

            val customerState = it.getContentIfNotHandled() ?: return@Observer

            if (customerState is NetworkState2.Loading<CustomerDetail>) {
                showProgress(getString(R.string.fetching_account_details))
                return@Observer
            }

            hideProgress()

            when (customerState) {
                is NetworkState2.Success -> {
                    customerState.data ?: return@Observer
                    setUpOptionMenu(true)
                    return@Observer
                }

                is NetworkState2.Error -> {
                    if (customerState.isSessionExpired) {
                        onSessionExpired(customerState.message)
                        return@Observer
                    }

                    val (message) = customerState

                    if (customerState.errorCode.toInt() == 301) {
                        val fragment = InvalidCustomerDialogFragment()
                        fragment.isCancelable = false
                        fragment.arguments = Bundle().also { b ->
                            b.putString("message", message)
                        }
                        fragment.show(supportFragmentManager, "EmiratesExpire")
                    } else {
                        handleErrorCode(customerState.errorCode.toInt(), message)
                    }

                    setUpOptionMenu(false)
                }

                is NetworkState2.Failure -> {
                    onFailure(findViewById(R.id.layout), customerState.throwable)
                    setUpOptionMenu(false)
                }
            }
        })
    }

    private val isAllowedListener = object : OnItemClickListener {
        override fun onItemClick(index: Int) {
            when (index) {
                0 -> startActivity(Intent(this@InternationalRemittanceActivity, RateCalculatorActivity::class.java).apply {
                    intent.extras?.let { putExtras(it) }
                })
                1 -> startActivity(Intent(this@InternationalRemittanceActivity, TransferActivity::class.java).apply {
                    intent.extras?.let {
                        removeExtra("action")
                        putExtras(it)
                    }
                    putExtra("action", "normal")
                })
                2 -> {
                    startActivity(Intent(this@InternationalRemittanceActivity, MyBeneficiariesActivity::class.java).apply {
                        intent.extras?.let { putExtras(it) }
                    })
                }
                3 -> {
                    val intent = Intent(this@InternationalRemittanceActivity, TrackTransactionActivity::class.java)
                    val card = this@InternationalRemittanceActivity.intent.getParcelableExtra<Card>("card")
                    intent.putExtra("card", card)
                    startActivity(intent)
                }
                4 -> startActivity(Intent(this@InternationalRemittanceActivity, IntlTransactionHistoryActivity::class.java).apply {
                    val card = this@InternationalRemittanceActivity.intent.getParcelableExtra<Card>("card")
                    this.putExtra("card", card)
                })
                5 -> {
                    startActivity(Intent(this@InternationalRemittanceActivity, BranchLocationActivity::class.java).apply {
                        putExtra("issue", "uae_exchange")
                        intent.extras?.let { putExtras(it) }
                    })
                }
            }
        }
    }

    private val notAllowedListener = object : OnItemClickListener {
        override fun onItemClick(index: Int) {
            when (index) {
                0 -> startActivity(Intent(this@InternationalRemittanceActivity, RateCalculatorActivity::class.java).apply {
                    putExtra("allowed", false)
                })
                5 -> {
                    startActivity(Intent(this@InternationalRemittanceActivity, BranchLocationActivity::class.java).apply {
                        putExtra("issue", "uae_exchange")
                        putExtra("allowed", false)
                        intent.extras?.let { putExtras(it) }
                    })
                }
            }
        }
    }

    override fun onItemClick(index: Int) {

    }

    private fun getItems(): List<Item> {
        val textItems = resources.getStringArray(R.array.intl_remittance)
        val icons = resources.obtainTypedArray(R.array.intl_remittance_icons)
        val items = ArrayList<Item>()

        for (i in textItems.indices) {
            items.add(Item(textItems[i], icons.getResourceId(i, 0)))
        }
        icons.recycle()
        return items
    }

    private fun handleBottomBar() {

        findViewById<TextView>(R.id.btm_home).setOnClickListener { view ->
            val i = Intent(view.context, MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(i)
        }

        findViewById<TextView>(R.id.btm_menu_branch).setOnClickListener { view ->
            val i = Intent(view.context, LocatorActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(i)
        }

        findViewById<TextView>(R.id.btm_menu_support).setOnClickListener { v ->
            val i = Intent(v.context, ContactUsActivity::class.java)
            startActivity(i)
        }

        findViewById<TextView>(R.id.btm_menu_loan_calc).setOnClickListener { v ->
            val i = Intent(v.context, LoanCalculatorActivity::class.java)
            startActivity(i)
        }

        findViewById<TextView>(R.id.toolbar_help).setOnClickListener(this)
    }

}