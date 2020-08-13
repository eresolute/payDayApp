package com.fh.payday.views2.offer

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.webkit.URLUtil
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.remote.isSessionExpired
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.viewmodels.OfferViewModel
import com.fh.payday.views2.dashboard.MainActivity
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity
import com.fh.payday.views2.locator.LocatorActivity
import com.fh.payday.views2.message.ContactUsActivity
import com.fh.payday.views2.offer.adapter.OfferPageAdapter
import kotlinx.android.synthetic.main.activity_offer.*
import kotlinx.android.synthetic.main.toolbar.*


class OfferActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvNoOffers: TextView

    private val offerListener = object : OnOfferClickListener {
        override fun onOfferClick(url: String) {

            if (URLUtil.isHttpUrl(url) || URLUtil.isHttpsUrl(url)) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            } else {
                return
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent != null && intent.extras != null) {
            finish()
            startActivity(Intent(this, OfferActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        getOffers()
        handleBottomBar()
    }

    private fun getOffers() {

        if (!isNetworkConnected()) {
            return showNoInternetView { getOffers() }
        }

        hideNoInternetView()

        val user = UserPreferences.instance.getUser(this) ?: return
        val viewModel = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return OfferViewModel(user.token, user.sessionId, user.refreshToken, user.customerId.toLong()) as T
            }
        }).get(OfferViewModel::class.java)

        val pageAdapter = OfferPageAdapter(this, offerListener)

        viewModel.itemPagedList.observe(this, Observer { items ->
            if (items == null) return@Observer
            pageAdapter.submitList(items)
        })
        recyclerView.adapter = pageAdapter

        viewModel.onError.observe(this, Observer {
            it ?: return@Observer

            val (code, message) = it
            if (isSessionExpired(code)) {
                onSessionExpired(message)
            }
        })

        viewModel.offerState.observe(this, Observer {
            it ?: return@Observer
            when {
                it.status == OfferState.Status.LOADING -> {
                    recyclerView.visibility = View.GONE
                    tvNoOffers.visibility = View.GONE
                    progress_bar.visibility = View.VISIBLE
                }
                it.status == OfferState.Status.EMPTY -> {
                    recyclerView.visibility = View.GONE
                    progress_bar.visibility = View.GONE
                    tvNoOffers.visibility = View.VISIBLE
                }
                it.status == OfferState.Status.LOADED -> {
                    tvNoOffers.visibility = View.GONE
                    progress_bar.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
                it.status == OfferState.Status.FAILED -> {
                    recyclerView.visibility = View.GONE
                    progress_bar.visibility = View.GONE
                    tvNoOffers.visibility = View.VISIBLE
                }
            }
        })
    }

    override fun getLayout() = R.layout.activity_offer

    override fun showNoInternetView(message: String, retryAction: () -> Unit) {
        recyclerView.visibility = View.GONE
        super.showNoInternetView(message, retryAction)
    }

    override fun hideNoInternetView() {
        super.hideNoInternetView()
        recyclerView.visibility = View.VISIBLE
    }

    private fun handleBottomBar() {

        findViewById<TextView>(R.id.btm_home).setOnClickListener { view ->
            val i = Intent(view.context, MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(i)
        }

        findViewById<TextView>(R.id.btm_menu_branch).setOnClickListener { v ->
            val i = Intent(v.context, LocatorActivity::class.java)
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

        findViewById<TextView>(R.id.toolbar_help).setOnClickListener {
            startHelpActivity("offersHelp")
        }
    }

    override fun init() {
        toolbar_title.setText(R.string.Offers)
        toolbar_back.setOnClickListener { onBackPressed() }
        recyclerView = findViewById(R.id.recyclerView)
        tvNoOffers = findViewById(R.id.tv_no_offers)
    }

    interface OnOfferClickListener {
        fun onOfferClick(url: String)
    }

    companion object {
        const val BANNER = "banners"
    }

//    private fun addObservers() {
//        viewModel.offerState.observe(this, Observer {
//            it ?: return@Observer
//
//            val offerState = it.getContentIfNotHandled() ?: return@Observer
//            if (offerState is NetworkState2.Loading) {
//                showProgress(getString(R.string.loading))
//                return@Observer
//            }
//
//            hideProgress()
//
//            when (offerState) {
//                is NetworkState2.Success -> {
//                    val offerList = offerState.data
//                    if (isEmptyList(offerList)) {
//                        recyclerView.visibility = View.GONE
//                        text_view.visibility = View.VISIBLE
//                        ivBanner.visibility = View.GONE
//                    } else {
//                        recyclerView.setHasFixedSize(true)
//                        val offerAdapter = OfferAdapter(offerList!!, offerListener)
//                        recyclerView.adapter = offerAdapter
//                        ivBanner.visibility = View.VISIBLE
//                        recyclerView.visibility = View.VISIBLE
//                        text_view.visibility = View.GONE
//                    }
//
//                }
//                is NetworkState2.Error -> {
//                    val (message) = offerState
//                    onError(message)
//                }
//                is NetworkState2.Failure -> {
//                    val (throwable) = offerState
//                    onFailure(findViewById(R.id.root_view), throwable)
//                }
//                else -> onFailure(findViewById(R.id.root_view), CONNECTION_ERROR)
//            }
//        })
//    }

}