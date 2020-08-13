package com.fh.payday.views2.intlRemittance.mybeneficiaries

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.view.Gravity
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.models.intlRemittance.IntlBeneficiary
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.viewmodels.intlRemittance.TransferViewmodel
import com.fh.payday.views2.intlRemittance.*
import com.fh.payday.views2.intlRemittance.cashpayout.CashPayoutActivity
import com.fh.payday.views2.intlRemittance.mybeneficiaries.addbeneficiary.AddBeneficiaryActivity
import com.fh.payday.views2.intlRemittance.mybeneficiaries.addbeneficiary.CustomerActivationOTPActivity
import com.fh.payday.views2.intlRemittance.mybeneficiaries.addbeneficiary.CustomerActivationOTPActivity.Companion.KEY_ACTIVATION_STATUS
import com.fh.payday.views2.intlRemittance.transfer.TransferActivity
import com.fh.payday.views2.loan.apply.TermsAndConditionsDialog
import kotlinx.android.synthetic.main.activity_my_beneficiaries.*
import kotlinx.android.synthetic.main.layout_payment_option.*
import kotlinx.android.synthetic.main.toolbar.*

class MyBeneficiariesActivity : BaseActivity() {
    private lateinit var viewModel: TransferViewmodel
    private var popUpAction: (IntlBeneficiary, View) -> Unit = { _, _ -> Unit }
    override fun getLayout(): Int = R.layout.activity_my_beneficiaries

    override fun onResume() {
        super.onResume()
        var flag = false
        if (ExchangeContainer.exchanges().isEmpty()) return
        ExchangeContainer.exchanges().map {
            if (it.accessKey.equals(ExchangeAccessKey.FARD, true) && it.isActive == "0") {
                flag = true
            }
        }
        if (!flag) getBeneficiaryList()
    }

    override fun init() {
        setUpToolbar()
        handleBottomBar()

        fab_btn.setOnClickListener {
            clickListener()
        }

        fab_btn_noBeneficiary.setOnClickListener{
            clickListener()
        }

        viewModel = ViewModelProviders.of(this).get(TransferViewmodel::class.java)

        addObserver()
        var flag = false
        if (ExchangeContainer.exchanges().isEmpty()) return
        ExchangeContainer.exchanges().map {
            if (it.accessKey.equals(ExchangeAccessKey.FARD, true) && it.isActive == "0") {
                flag = true
            }
        }
        if (flag) {
            val intent = Intent(this, CustomerActivationOTPActivity::class.java)
            startActivityForResult(intent, CUSTOMER_ACTIVATION)
        }

        tv_paymentOptiion.text = getString(R.string.choose_payment_option)
        tv_bank_transfer.setOnClickListener {
            recycler_view.visibility = View.GONE
            fab_btn.visibility = View.GONE
            constraint_layout.visibility = View.GONE
            getBankTransferList()

        }
        tv_cash_payout.setOnClickListener {
            recycler_view.visibility = View.GONE
            constraint_layout.visibility = View.GONE
            fab_btn.visibility = View.GONE
            getCashPayoutList()
        }
    }

    private fun getBankTransferList() {
        viewModel.selectedDeliveryMode = DeliveryModes.BT
        setBackGround(tv_cash_payout, tv_bank_transfer, this)
        setTextColor(tv_cash_payout, tv_bank_transfer, this)
        setDrawableBT(tv_cash_payout, tv_bank_transfer)
        getBeneficiaryList()

    }

    private fun getCashPayoutList() {
        viewModel.selectedDeliveryMode = DeliveryModes.CP
        setBackGround(tv_bank_transfer, tv_cash_payout, this)
        setTextColor(tv_bank_transfer, tv_cash_payout, this)
        setDrawableCP(tv_bank_transfer, tv_cash_payout)
        getBeneficiaryList()

    }

    private fun clickListener(){
        if (viewModel.selectedDeliveryMode == DeliveryModes.BT) {
            val intent = Intent(this, AddBeneficiaryActivity::class.java)
            intent.putExtra("deliveryMode", viewModel.selectedDeliveryMode)
            startActivity(intent)

        } else {
            val intent = Intent(this, CashPayoutActivity::class.java)
            intent.putExtra("deliveryMode", viewModel.selectedDeliveryMode)
            startActivity(intent)
        }

    }

    private fun setUpToolbar() {
        toolbar_back.setOnClickListener(this)
        toolbar_title.text = getString(R.string.select_beneficiary)
    }

    private fun handleBottomBar() {
        findViewById<TextView>(R.id.btm_home).setOnClickListener(this)
        findViewById<TextView>(R.id.btm_menu_branch).setOnClickListener(this)
        findViewById<TextView>(R.id.btm_menu_support).setOnClickListener(this)
        findViewById<TextView>(R.id.btm_menu_loan_calc).setOnClickListener(this)
        findViewById<TextView>(R.id.toolbar_help).setOnClickListener(this)
    }

    private fun getBeneficiaryList() {
        val user = UserPreferences.instance.getUser(this) ?: return
        if (ExchangeContainer.exchanges().isEmpty()) return
        viewModel.getBeneficiariesRx(
            user.token,
            user.sessionId,
            user.refreshToken,
            user.customerId.toString(),
            viewModel.selectedDeliveryMode
        )
    }


    private fun addObserver() {
        viewModel.intlBeneficiary.observe(this, Observer {
            it ?: return@Observer

            val beneficiaryState = it.getContentIfNotHandled() ?: return@Observer

            if (beneficiaryState is NetworkState2.Loading<List<IntlBeneficiary>>) {
                constraint_layout.visibility = View.GONE
                progress_bar.visibility = View.VISIBLE
                return@Observer
            }

            progress_bar.visibility = View.GONE

            when (beneficiaryState) {
                is NetworkState2.Success -> {
                    val data = beneficiaryState.data ?: return@Observer
                    setupRecyclerView(data)
                    return@Observer
                }

                is NetworkState2.Error -> {
                    if (beneficiaryState.isSessionExpired) {
                        onSessionExpired(beneficiaryState.message)
                        return@Observer
                    }
                    val (message) = beneficiaryState
                    handleErrorCode(beneficiaryState.errorCode.toInt(), message)
                }

                is NetworkState2.Failure -> {
                    onFailure(findViewById(R.id.root_view), beneficiaryState.throwable)
                }
            }
        })

        viewModel.deleteBeneficiaryState.observe(this, Observer {
            it ?: return@Observer

            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                showProgress(getString(R.string.processing))
                return@Observer
            }
            hideProgress()
            when (state) {
                is NetworkState2.Success -> {
                    getBeneficiaryList()
                    showMessage(state.message ?: return@Observer)
                    return@Observer
                }

                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        onSessionExpired(state.message)
                        return@Observer
                    }
                    val (message) = state
                    handleErrorCode(state.errorCode.toInt(), message)
                }

                is NetworkState2.Failure -> {
                    onFailure(findViewById(R.id.root_view), state.throwable)
                }
            }
        })
    }

    private fun showNoBeneficiaryViews() {
        progress_bar.visibility = View.GONE
        recycler_view.visibility = View.GONE
        constraint_layout.visibility = View.VISIBLE
        fab_btn.visibility= View.GONE
    }


    private fun setupRecyclerView(list: List<IntlBeneficiary>) {
        recycler_view.visibility = View.VISIBLE
        fab_btn.visibility = View.VISIBLE
        if (list.isEmpty()) {
            showNoBeneficiaryViews()
            return
        }
        attachPopUpListener { beneficiary, view ->
            val menu = showPopUpMenu(view)
            menu.show()
            menu.setOnMenuItemClickListener {
                when (it.itemId) {

                    R.id.delete -> {
                        showDeleteConfirmation(beneficiary)
                        true
                    }
                    else -> false
                }
            }
        }

        recycler_view.adapter = MyBeneficiariesAdapter(list, listener, popUpAction)
    }

    private fun showDeleteConfirmation(beneficiary: IntlBeneficiary) {
        val termsAndCondDialog = TermsAndConditionsDialog.Builder()
            .setMessage(
                String.format(
                    getString(R.string.delele_beneficiary_confirmation),
                    beneficiary.receiverFirstName
                )
            )
            .setMessageGravity(Gravity.CENTER)
            .setPositiveText(getString(R.string.delete))
            .setNegativeText(getString(R.string.cancel))
            .attachPositiveListener {
                val user = UserPreferences.instance.getUser(this)
                    ?: return@attachPositiveListener
                viewModel.deleteBeneficiary(
                    user.token, user.sessionId, user.refreshToken, user.customerId.toLong(),
                    beneficiary.accessKey, beneficiary.recieverRefNumber
                )
            }
            .build()

        val fm = supportFragmentManager ?: return

        termsAndCondDialog.apply {
            isCancelable = false
            show(fm, termsAndCondDialog.tag)
        }
    }

    val listener = object : TransferActivity.OnIntlBeneficiaryClickListener {
        override fun onIntlBeneficiaryClick(position: Int, beneficiary: IntlBeneficiary) {
            startActivity(Intent(this@MyBeneficiariesActivity, TransferActivity::class.java).apply {
                intent.extras?.let { putExtras(it) }
                putExtra("action", "MyBeneficiariesActivity")
                putExtra("beneficiary", beneficiary)
                putExtra("paymentMode", "default")
                putExtra("deliveryMode", viewModel.selectedDeliveryMode)
            })
        }
    }

    private fun showPopUpMenu(view: View): PopupMenu {
        val menu = PopupMenu(this, view)
        menu.inflate(R.menu.menu_beneficiary)
        menu.menu.getItem(0).isVisible = false
        menu.menu.getItem(2).isVisible = false
        return menu
    }

    private fun attachPopUpListener(action: (IntlBeneficiary, View) -> Unit) {
        popUpAction = action
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return

        val code = data?.getIntExtra(KEY_ACTIVATION_STATUS, -1) ?: -1
        if (requestCode == CUSTOMER_ACTIVATION && code == -1) {
            finish()
        } else getBeneficiaryList()
    }

    companion object {
        private const val CUSTOMER_ACTIVATION = 3
    }

}