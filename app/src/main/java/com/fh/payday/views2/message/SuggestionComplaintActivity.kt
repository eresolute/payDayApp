package com.fh.payday.views2.message

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.models.login.User
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.isEmptyList
import com.fh.payday.viewmodels.MessageViewModel
import com.fh.payday.views2.dashboard.MainActivity
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity
import com.fh.payday.views2.locator.LocatorActivity
import com.fh.payday.views2.message.adapter.SuggestionCompAdapter
import kotlinx.android.synthetic.main.activity_suggestion_complaint.*
import kotlinx.android.synthetic.main.toolbar.*

class SuggestionComplaintActivity : BaseActivity() {

    private var viewModel: MessageViewModel? = null
    private lateinit var recyclerView: RecyclerView

    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        user = UserPreferences.instance.getUser(this) ?: return
        viewModel = ViewModelProviders.of(this).get(MessageViewModel::class.java)

        super.onCreate(savedInstanceState)
        handleBottomBar()
    }

    override fun getLayout(): Int {
        return R.layout.activity_suggestion_complaint
    }

    override fun init() {
        recyclerView = findViewById(R.id.recycler_view)
        toolbar_title.text = getString(R.string.suggestions_complaints)
        toolbar_help.text = ""
        ignored_view.text = ""

        toolbar_help.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                R.drawable.ic_send_msg_gradient, 0)
        ignored_view.setCompoundDrawablesWithIntrinsicBounds(0, 0,
            R.drawable.ic_send_msg_gradient, 0)

        toolbar_back.setOnClickListener { onBackPressed() }
    }

    override fun onResume() {
        super.onResume()
        getComplaints()
    }

    private fun getComplaints() {
        hideNoInternetView()
        if (!isNetworkConnected()) {
            constraint_layout.visibility = View.GONE
            return showNoInternetView { getComplaints() }
        }
        constraint_layout.visibility = View.VISIBLE
        user?.apply {
            viewModel?.getMessages(token, sessionId, refreshToken, customerId.toLong())
            addObserver()
        }
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

        findViewById<TextView>(R.id.btm_menu_support).setOnClickListener {
            return@setOnClickListener
        }

        findViewById<TextView>(R.id.btm_menu_loan_calc).setOnClickListener { v ->
            val i = Intent(v.context, LoanCalculatorActivity::class.java)
            startActivity(i)
        }

        findViewById<TextView>(R.id.toolbar_help).setOnClickListener {
            startActivity(Intent(it.context, SendMessageActivity::class.java))
        }
    }

    private fun addObserver() {
        viewModel?.getMessageState?.observe(this, Observer {
            if (it == null) return@Observer

            val messageResponse = it.getContentIfNotHandled() ?: return@Observer

            if (messageResponse is NetworkState2.Loading) {
                showProgress(getString(R.string.processing))
                return@Observer
            }

            hideProgress()
            when (messageResponse) {
                is NetworkState2.Success -> {
                    val messageData = messageResponse.data ?: return@Observer

                    if (isEmptyList(messageData)) {
                        findViewById<TextView>(R.id.tv_no_messages).visibility = View.VISIBLE
                    }else{
                        findViewById<TextView>(R.id.tv_no_messages).visibility = View.GONE
                    }

                    recyclerView.setHasFixedSize(true)
                    val offerAdapter = SuggestionCompAdapter(messageData)
                    recyclerView.adapter = offerAdapter
                }
                is NetworkState2.Error -> {
                    hideProgress()
                    val (message) = messageResponse
                    if (messageResponse.isSessionExpired) {
                        return@Observer onSessionExpired(message)
                    }
                    onError(message)
                }
                is NetworkState2.Failure -> {
                    hideProgress()
                    val (throwable) = messageResponse
                    onFailure(findViewById(R.id.root_view), throwable)

                }
                else -> {
                    hideProgress()
                    onFailure(findViewById(R.id.root_view), CONNECTION_ERROR)
                }
            }
        })
    }
}