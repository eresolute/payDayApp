package com.fh.payday.views2.message


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.widget.ExpandableListView
import android.widget.ImageView
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.models.login.User
import com.fh.payday.datasource.models.message.MessageOption
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.viewmodels.MessageViewModel
import com.fh.payday.views2.dashboard.MainActivity
import com.fh.payday.views2.help.HelpActivity
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity
import com.fh.payday.views2.locator.LocatorActivity
import com.fh.payday.views2.message.adapter.MessageAdapter
import kotlinx.android.synthetic.main.toolbar.*

class MessagesActivity : BaseActivity() {


    private var expandableListView: ExpandableListView? = null
    private var messageAdapter: MessageAdapter? = null
    private var viewModel: MessageViewModel? = null
    private lateinit var messageOptions: ArrayList<MessageOption>
    private var user: User? = null
    private var type = "Inbox"

    override fun onCreate(savedInstanceState: Bundle?) {
        user = UserPreferences.instance.getUser(this) ?: return
        viewModel = ViewModelProviders.of(this).get(MessageViewModel::class.java)

        super.onCreate(savedInstanceState)
        handleBottomBar()

//        expandableListView!!.setOnGroupExpandListener { Toast.makeText(this, "Group Expanded", Toast.LENGTH_SHORT).show() }
//        expandableListView!!.setOnGroupCollapseListener { Toast.makeText(this, "Group Collopsed", Toast.LENGTH_SHORT).show() }
//        expandableListView!!.setOnChildClickListener { _, _, _, id, _ ->
//            Toast.makeText(applicationContext, "Clicked on Child: ", Toast.LENGTH_SHORT).show()
//            false
//        }

    }

    override fun getLayout(): Int {
        return R.layout.activity_messages
    }

    override fun init() {
        toolbar_title.setText(R.string.message)
        toolbar_back.setOnClickListener { onBackPressed() }
        expandableListView = findViewById(R.id.expandable_list_view)

        val ivSendMsg = findViewById<ImageView>(R.id.iv_send_msg)
        ivSendMsg.setOnClickListener {
            val intent = Intent(this, SendMessageActivity::class.java)
            startActivity(intent)
        }

        messageOptions = ArrayList()


    }

    override fun onResume() {
        super.onResume()
        type = "Inbox"
        user?.apply {
            messageOptions = ArrayList()
            //    viewModel?.getMessages(token, sessionId, refreshToken, customerId.toLong(), issue)
            //  addObserver()
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

        findViewById<TextView>(R.id.btm_menu_support).setOnClickListener { v ->
            return@setOnClickListener
        }

        findViewById<TextView>(R.id.btm_menu_loan_calc).setOnClickListener { v ->
            val i = Intent(v.context, LoanCalculatorActivity::class.java)
            startActivity(i)
        }

        findViewById<TextView>(R.id.toolbar_help).setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        }
    }


    private fun addObserver() {
        viewModel?.getMessageState?.observe(this, Observer {
            if (it == null) return@Observer

            val messageResponse = it.getContentIfNotHandled() ?: return@Observer

            if (messageResponse is NetworkState2.Loading) {
                if (type == "Inbox") {
                    showProgress(getString(R.string.processing))
                }
                return@Observer
            }


            when (messageResponse) {
                is NetworkState2.Success -> {
                    if (type == "Inbox") {
                        type = "sent"
                        messageOptions.add(MessageOption(R.drawable.ic_icon_inbox, getString(R.string.inbox), messageResponse.data!!))
                        user?.apply {
                            viewModel?.getMessages(token, sessionId, refreshToken, customerId.toLong())
                        }
                    } else {
                        hideProgress()
                        messageOptions.add(MessageOption(R.drawable.ic_outbox, getString(R.string.sent_box), messageResponse.data!!))
                        //  messageAdapter = MessageAdapter(messageOptions)
                        //expandableListView!!.setAdapter(messageAdapter)
                    }

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
                    onFailure(findViewById(R.id.root_view), CONNECTION_ERROR)

                }
                else -> {
                    hideProgress()
                    onFailure(findViewById(R.id.root_view), CONNECTION_ERROR)
                }
            }
        })
    }

}
