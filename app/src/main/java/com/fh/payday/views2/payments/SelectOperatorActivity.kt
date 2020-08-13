package com.fh.payday.views2.payments

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.models.payments.CountryCode
import com.fh.payday.datasource.models.payments.ServiceCategory
import com.fh.payday.datasource.models.payments.TypeId
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.OnItemClickListener
import com.fh.payday.viewmodels.payments.SelectOperatorViewModel
import com.fh.payday.views2.payments.ibp.IndianBillPaymentActivity
import com.fh.payday.views2.payments.internationaltopup.InternationalTopUpActivity
import com.fh.payday.views2.shared.adapter.SelectOperatorAdapter
import com.fh.payday.views2.shared.custom.ProgressDialogFragment
import kotlinx.android.synthetic.main.activity_select_operator.*
import kotlinx.android.synthetic.main.bottombar_dashboard.*
import kotlinx.android.synthetic.main.toolbar.*

class SelectOperatorActivity : BaseActivity(), OnItemClickListener {

    companion object {
        const val REQUEST_CODE = 12
    }

    private val viewModel: SelectOperatorViewModel by lazy {
        ViewModelProviders.of(this).get(SelectOperatorViewModel::class.java)
    }
    private var activityType: Int = 0
    private var selectedCountryCode: Int = 0

    private lateinit var adapter: SelectOperatorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        toolbar_back.setOnClickListener(this)
        btm_home.setOnClickListener(this)
        btm_menu_branch.setOnClickListener(this)
        btm_menu_support.setOnClickListener(this)
        btm_menu_loan_calc.setOnClickListener(this)

        toolbar_help.setOnClickListener{
            findViewById<View>(R.id.toolbar_help).setOnClickListener {
                 startHelpActivity("paymentScreenHelp")
            }
        }

        attachObserver()
    }

    override fun getLayout() = R.layout.activity_select_operator

    override fun init() {

        activityType = intent.getIntExtra("international_activity", 0)

        if (activityType == 107) {

            toolbar_title.text = getString(R.string.operators)
            selectedCountryCode = intent.getIntExtra("country_code", 0)

            getOperators(selectedCountryCode.toString(), "NA")

        } else {

            val countryCode = intent.getStringExtra("country_code") ?: CountryCode.DEFAULT
            val category = intent.getStringExtra("category") ?: ServiceCategory.PREPAID
            setToolbar(category)

            getOperators(countryCode, category)
        }

        adapter = SelectOperatorAdapter(this)
        recycler_view.adapter = adapter

    }

    private fun getOperators(countryCode: String, category: String) {
        hideNoInternetView()
        if (!isNetworkConnected()) {
            frame_layout.visibility = View.GONE
            return showNoInternetView { getOperators(countryCode, category) }
        }
        frame_layout.visibility = View.VISIBLE

        viewModel.user = UserPreferences.instance.getUser(this)
        val user = viewModel.user ?: return
        val typeId = intent.getIntExtra("service", TypeId.TOP_UP)

        viewModel.getOperators(user.token, user.sessionId, user.refreshToken, user.customerId.toLong(),
                typeId, category, countryCode)
    }

    override fun showProgress(message: String, cancellable: Boolean, dismissListener: ProgressDialogFragment.OnDismissListener) {
        progress_bar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        progress_bar.visibility = View.GONE
    }

    override fun onItemClick(index: Int) {
        if (activityType == 107) {
            viewModel.operators?.let {
                if (it.isEmpty() || it.size <= index) return
                val newIntent = Intent(this, InternationalTopUpActivity::class.java)
                newIntent.putExtra("selected_operator", it[index])
                newIntent.putExtra("selected_country_code",selectedCountryCode)
                newIntent.putExtra("type_id", intent.getIntExtra("service", TypeId.TOP_UP))
                startActivityForResult(newIntent, REQUEST_CODE)
            }
        } else {
            viewModel.operators?.let {
                if (it.isEmpty() || it.size <= index) return
                val newIntent = Intent(this, IndianBillPaymentActivity::class.java)
                newIntent.putExtra("selected_operator", it[index])
                newIntent.putExtra("type_id", intent.getIntExtra("service", TypeId.TOP_UP))
                startActivityForResult(newIntent, REQUEST_CODE)
            }
        }

    }

    private fun attachObserver() {
        viewModel.operatorState.observe(this, Observer {
            it ?: return@Observer

            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                return@Observer showProgress()
            }

            hideProgress()

            when (state) {
                is NetworkState2.Success -> {
                    val (data) = state
                    data?.let { operators ->
                        if (operators.isEmpty()) {
                            tv_no_operators.visibility = View.VISIBLE
                        } else {
                            adapter.addAll(operators)
                        }
                    }
                }
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        return@Observer onSessionExpired(state.message)
                    }
                    onError(state.message)
                }
                is NetworkState2.Failure -> onFailure(findViewById(R.id.root_view), CONNECTION_ERROR)
                else -> onFailure(findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    private fun setToolbar(category: String) {
        when (category) {
            ServiceCategory.PREPAID -> {
                toolbar_title.text = getString(R.string.prepaid)
            }
            ServiceCategory.POSTPAID -> {
                toolbar_title.text = getString(R.string.postpaid)
            }
            ServiceCategory.LANDLINE -> {
                toolbar_title.text = getString(R.string.landline)
            }
            ServiceCategory.DTH -> {
                toolbar_title.text = getString(R.string.dth)
            }
            ServiceCategory.INSURANCE -> {
                toolbar_title.text = getString(R.string.insurance)
            }
            ServiceCategory.GAS -> {
                toolbar_title.text = getString(R.string.gas)
            }
            ServiceCategory.ELECTRICITY -> {
                toolbar_title.text = getString(R.string.electricity)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE -> finish()

            }
        }
    }

}
