package com.fh.payday.views2.notification

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.models.Notification
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.Action
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.isEmptyList
import com.fh.payday.viewmodels.NotificationViewModel
import com.fh.payday.views2.campaign.CampaignActivity
import kotlinx.android.synthetic.main.activity_notification.*
import kotlinx.android.synthetic.main.bottombar_dashboard.*
import kotlinx.android.synthetic.main.toolbar.*

class NotificationActivity : BaseActivity() {
    private lateinit var viewModel: NotificationViewModel
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var readNotificationList: HashSet<Long>
    private lateinit var notificationsList: List<Notification>

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.extras != null) {
                clearNotification()
                notificationsList = ArrayList()
                recycler_view.adapter = NotificationAdapter(notificationsList)
                val user = UserPreferences.instance.getUser(context)
                user?.apply {
                    viewModel.notifications(token, sessionId, refreshToken, customerId.toLong())
                }
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
        readNotificationList = HashSet()
        notificationsList = ArrayList()
        viewModel = ViewModelProviders.of(this).get(NotificationViewModel::class.java)
        addObservers()
        layoutManager = LinearLayoutManager(this)
        recycler_view.layoutManager = layoutManager
        toolbar_title.text = getString(R.string.notifications)
        toolbar_back.setOnClickListener(this)
        toolbar_help.visibility = View.INVISIBLE
        btm_home.setOnClickListener(this)
        btm_menu_branch.setOnClickListener(this)
        btm_menu_support.setOnClickListener(this)
        btm_menu_loan_calc.setOnClickListener(this)
        recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val position = layoutManager.findLastCompletelyVisibleItemPosition()
                for (i in 0 until position + 1) {
                    readNotificationList.add(notificationsList[i].Id)
                }
            }
        })

    }

    override fun onResume() {
        super.onResume()
        notificationsList = ArrayList()
        recycler_view.adapter = NotificationAdapter(notificationsList)
        val user = UserPreferences.instance.getUser(this)
        user?.apply {
            viewModel.notifications(token, sessionId, refreshToken, customerId.toLong())
        }
    }

    private fun addObservers() {
        viewModel.notificationState.observe(this, Observer {
            it ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                progress_bar.visibility = View.VISIBLE
                return@Observer
            }
            progress_bar.visibility = View.GONE
            when (state) {
                is NetworkState2.Success -> {
                    state.data ?: return@Observer
                    if (isEmptyList(state.data.filter { notification ->
                            notification.flag == 0
                        })) {
                        tv_no_notifications.visibility = View.VISIBLE
                        return@Observer
                    }
                    tv_no_notifications.visibility = View.GONE
                    notificationsList = state.data.filter { notification ->
                        notification.flag == 0
                    }
                     recycler_view.adapter = NotificationAdapter(notificationsList) { index ->
                         val title = state.data[index].offerTitle
                         val intent = Intent(this, CampaignActivity::class.java).apply {
                             putExtra("title", title ?: " ")
                             putExtra("body", state.data[index].notificationBody)
                             putExtra("productType", state.data[index].type)
                             putExtra("notificationId", state.data[index].Id)
                         }
                         startActivity(intent)
                     }


                }
                is NetworkState2.Error -> {
                    val (message) = state
                    if (state.isSessionExpired) {
                        return@Observer onSessionExpired(message)
                    }
                    handleErrorCode(state.errorCode.toInt(), state.message)
                }
                is NetworkState2.Failure -> {
                    onFailure(root_view, getString(R.string.request_error))
                }
                else -> {
                    onFailure(root_view, CONNECTION_ERROR)
                }
            }
        })
    }

    override fun getLayout() = R.layout.activity_notification

    override fun onBackPressed() {
        sendBroadCast()
        finish()
    }

    override fun onClick(v: View?) {
        sendBroadCast()
        super.onClick(v)
    }

    private fun sendBroadCast() {
        val returnIntent = Intent(Action.ACTION_NOTIFICATION_READ_LIST)
        val position = layoutManager.findLastCompletelyVisibleItemPosition()
        for (i in 0 until position + 1) {
            readNotificationList.add(notificationsList[i].Id)

        }
        returnIntent.apply {
            putExtra("data", readNotificationList)
            setResult(Activity.RESULT_OK, returnIntent)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(returnIntent)
    }
}
