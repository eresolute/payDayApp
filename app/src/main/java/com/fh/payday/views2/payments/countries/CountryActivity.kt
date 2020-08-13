package com.fh.payday.views2.payments.countries

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.models.payments.TypeId
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.OnItemClickListener
import com.fh.payday.viewmodels.CountryViewModel
import com.fh.payday.views2.payments.SelectOperatorActivity
import kotlinx.android.synthetic.main.bottombar_dashboard.*
import kotlinx.android.synthetic.main.toolbar.*

class CountryActivity : BaseActivity(), OnItemClickListener {

    companion object {
        const val InternationalTopUpActivity = 107
    }

    private val viewModel: CountryViewModel by lazy {
        ViewModelProviders.of(this).get(CountryViewModel::class.java)
    }

    private lateinit var adapter: CountryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        toolbar_back.setOnClickListener(this)
        toolbar_help.setOnClickListener(this)
        btm_home.setOnClickListener(this)
        btm_menu_branch.setOnClickListener(this)
        btm_menu_support.setOnClickListener(this)
        btm_menu_loan_calc.setOnClickListener(this)

        attachObserver()

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        adapter = CountryAdapter(this)
        recyclerView.adapter = adapter
    }

    override fun getLayout(): Int {
        return R.layout.activity_country
    }

    override fun init() {
        toolbar_back.setOnClickListener { onBackPressed() }
        toolbar_title.text = getString(R.string.country)
        adapter = CountryAdapter(this)

        getCountries()

    }

    private fun getCountries(){
        hideNoInternetView()
        if (!isNetworkConnected()) {
            return showNoInternetView { getCountries() }
        }

        val user = UserPreferences.instance.getUser(this) ?: return

        viewModel.getCountries(user.token, user.sessionId, user.refreshToken)
    }


    override fun onItemClick(index: Int) {
        viewModel.countries?.let {
            if (it.isEmpty() || it.size <= index) return
            val mIntent = Intent(this, SelectOperatorActivity::class.java)
            mIntent.putExtra("country_code", it[index].countryCode)
            mIntent.putExtra("service", TypeId.TOP_UP)
            mIntent.putExtra("international_activity", InternationalTopUpActivity)
            startActivity(mIntent)
        }
    }

    private fun attachObserver() {

        viewModel.countryState.observe(this, Observer {
            it ?: return@Observer

            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                return@Observer showProgress()
            }

            hideProgress()

            when (state) {
                is NetworkState2.Success -> {
                    val (data) = state
                    data?.let { countries ->
                        adapter.addAll(countries)
                    }
                }
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        return@Observer onSessionExpired(state.message)
                    }
                    onError(state.message)
                }
                is NetworkState2.Failure -> onFailure(findViewById(R.id.root_view), state.throwable)
                else -> onFailure(findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

}