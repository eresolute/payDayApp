package com.fh.payday.views2.message

import android.app.Dialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.models.Item
import com.fh.payday.datasource.models.login.User
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.*
import com.fh.payday.viewmodels.MessageViewModel
import com.fh.payday.views2.dashboard.MainActivity
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity
import com.fh.payday.views2.locator.LocatorActivity
import com.fh.payday.views2.support.adapter.SupportIssueAdapter
import kotlinx.android.synthetic.main.activity_send_message.*
import kotlinx.android.synthetic.main.toolbar.*

class SendMessageActivity : BaseActivity() {

    private lateinit var etMessage: TextInputEditText
    private lateinit var btnSubmit: Button
    private lateinit var tvArea: TextView
    private lateinit var tvIssue: TextView
    private lateinit var dialog: Dialog
    private lateinit var tvError: TextView
    private lateinit var tvIssueError: TextView
    private lateinit var tvCharCounter: TextView
    private lateinit var areaProgressBar: ProgressBar
    private lateinit var issueProgressBar: ProgressBar

    private lateinit var viewModel: MessageViewModel

    private var areaOfIssue: MutableList<Item>? = null
    private var issues: MutableList<Item>? = null
    private var user: User? = null

    private val areaIssueListener = object : OnItemClickListener {
        override fun onItemClick(index: Int) {
            tvArea.text = areaOfIssue!![index].name
            dialog.dismiss()
            issues = ArrayList()
            tvIssue.text = getString(R.string.select_issue)
            issueProgressBar.visibility = View.VISIBLE
            setDrawableRight(R.drawable.ic_right_arrow, 0)
            getIssues()
        }
    }

    private val issueListener = object : OnItemClickListener {
        override fun onItemClick(index: Int) {
            tvIssue.text = issues!![index].name
            dialog.dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        areaOfIssue = ArrayList()

        etMessage.imeOptions = EditorInfo.IME_ACTION_DONE
        etMessage.setRawInputType(InputType.TYPE_CLASS_TEXT)

        user = UserPreferences.instance.getUser(this) ?: return

        tvArea.setOnClickListener {

            if (isEmptyList(areaOfIssue)) return@setOnClickListener

            tvError.visibility = View.GONE
            areaOfIssue?.let { showIssueDialog(it, areaIssueListener) }
        }

        tvIssue.setOnClickListener {

            if (isEmptyList(issues)) {
                tvIssue.isEnabled = false
                return@setOnClickListener
            }

            tvIssueError.visibility = View.GONE
            issues?.let { showIssueDialog(it, issueListener) }
        }

        btnSubmit.setOnClickListener {
            if (validate()) {
                if (!isNetworkConnected()) {
                    onFailure(root_view, getString(R.string.no_internet_connectivity))
                    return@setOnClickListener
                }
                val user = user ?: return@setOnClickListener
                viewModel.sendMessage(user.token, user.sessionId, user.refreshToken, user.customerId.toLong(),
                        tvArea.text.toString(), tvIssue.text.toString(), etMessage.text.toString())
            }
        }

        et_message.hint = getString(R.string.enter_message_hint)

        etMessage.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                textInputLayout.clearErrorMessage()
                tv_message_error.visibility = View.GONE

            }

            override fun afterTextChanged(editable: Editable) {

                val len = 0 + editable.length
                val totalLen = "/255"

                val counter = "$len$totalLen"
                tvCharCounter.text = counter
            }
        })

        getAreaOfIssues()
    }

    override fun onResume() {
        super.onResume()
        clearError()
    }

    private fun clearError() {
        tvIssueError.visibility = View.GONE
        tvError.visibility = View.GONE
        tv_message_error.visibility = View.GONE
        textInputLayout.clearErrorMessage()
    }

    override fun init() {

        toolbar_title.setText(R.string.contact_us)
        toolbar_back.setOnClickListener { onBackPressed() }

        tvArea = findViewById(R.id.tv_issue_area)
        tvIssue = findViewById(R.id.tv_issue)
        btnSubmit = findViewById(R.id.btn_submit)
        etMessage = findViewById(R.id.et_message)
        tvError = findViewById(R.id.tv_error)
        tvCharCounter = findViewById(R.id.tv_counter)
        tvIssueError = findViewById(R.id.tv_issue_error)
        areaProgressBar = findViewById(R.id.pb_area)
        issueProgressBar = findViewById(R.id.pb_issue)

        tvIssue.isEnabled = false
        areaProgressBar.visibility = View.VISIBLE
        setDrawableRight(0, R.drawable.ic_right_arrow)


        viewModel = ViewModelProviders.of(this).get(MessageViewModel::class.java)

        handleBottomBar()
        addAreaIssueObserver()
        addIssueObserver()
        addObserver()
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
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(i)
        }

        findViewById<TextView>(R.id.btm_menu_loan_calc).setOnClickListener {
            val i = Intent(it.context, LoanCalculatorActivity::class.java)
            startActivity(i)
        }

        findViewById<TextView>(R.id.toolbar_help).visibility = View.INVISIBLE
    }

    override fun showNoInternetView(message: String, retryAction: () -> Unit) {
        container.visibility = View.GONE
        super.showNoInternetView(message, retryAction)
    }

    override fun hideNoInternetView() {
        super.hideNoInternetView()
        container.visibility = View.VISIBLE
    }

    private fun getAreaOfIssues() {
        if (!isNetworkConnected()) {
            return showNoInternetView { getAreaOfIssues() }
        }

        hideNoInternetView()

        val user = user ?: return
        viewModel.getIssueAreas(user.token, user.sessionId, user.refreshToken, user.customerId.toLong())
    }

    private fun getIssues() {
        if (!isNetworkConnected()) {
            return showNoInternetView { getIssues() }
        }

        hideNoInternetView()

        val user = user ?: return
        viewModel.getIssues(user.token, user.sessionId, user.refreshToken, user.customerId.toLong(), tvArea.text.toString())
    }

    private fun addObserver() {
        viewModel.messageState.observe(this, Observer {
            if (it == null) return@Observer

            val messageResponse = it.getContentIfNotHandled() ?: return@Observer

            if (messageResponse is NetworkState2.Loading) {
                showProgress(getString(R.string.processing))
                btnSubmit.isEnabled = false
                return@Observer
            }

            hideProgress()
            btnSubmit.isEnabled = true

            when (messageResponse) {
                is NetworkState2.Success -> showMessage(String.format(getString(R.string.message_request), messageResponse.data?.requestNumber!!),
                        R.drawable.ic_success_checked_blue, R.color.blue,
                        com.fh.payday.views2.shared.custom.AlertDialogFragment.OnConfirmListener { finish() })
                is NetworkState2.Error -> {
                    val (message) = messageResponse
                    if (messageResponse.isSessionExpired) {
                        return@Observer onSessionExpired(message)
                    }
                    handleErrorCode(messageResponse.errorCode.toInt(), messageResponse.message)
                }
                is NetworkState2.Failure -> {
                    val (throwable) = messageResponse
                    onFailure(findViewById(R.id.root_view), throwable)
                }
                else -> onFailure(findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    private fun addAreaIssueObserver() {

        viewModel.issueState.observe(this, Observer {
            if (it == null) return@Observer

            val messageResponse = it.getContentIfNotHandled() ?: return@Observer

            if (messageResponse is NetworkState2.Loading) {
                tvArea.isEnabled = false
                return@Observer
            }

            areaProgressBar.visibility = View.GONE
            setDrawableRight(R.drawable.ic_right_arrow, R.drawable.ic_right_arrow)

            when (messageResponse) {
                is NetworkState2.Success -> {
                    tvArea.isEnabled = true
                    for (item in messageResponse.data!!) {
                        areaOfIssue?.add(Item(item, 0))
                    }
                }
                is NetworkState2.Error -> {
                    val (message) = messageResponse
                    if (messageResponse.isSessionExpired) {
                        return@Observer onSessionExpired(message)
                    }
                    onError(message)
                }
                is NetworkState2.Failure -> {
                    val (throwable) = messageResponse
                    onFailure(findViewById(R.id.root_view), throwable)
                }
                else -> onFailure(findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }


    private fun addIssueObserver() {

        viewModel.issueSelectedState.observe(this, Observer {
            if (it == null) return@Observer

            val messageResponse = it.getContentIfNotHandled() ?: return@Observer

            if (messageResponse is NetworkState2.Loading) {
                tvArea.isEnabled = false
                tvIssue.isEnabled = false
                return@Observer
            }

            tvArea.isEnabled = true
            issueProgressBar.visibility = View.GONE
            setDrawableRight(R.drawable.ic_right_arrow, R.drawable.ic_right_arrow)

            when (messageResponse) {
                is NetworkState2.Success -> {
                    if (isEmptyList(messageResponse.data)) return@Observer
                    tvIssue.isEnabled = true
                    for (item in messageResponse.data!!) {
                        issues?.add(Item(item, 0))
                    }
                }
                is NetworkState2.Error -> {
                    val (message) = messageResponse
                    if (messageResponse.isSessionExpired) {
                        return@Observer onSessionExpired(message)
                    }
                    onError(message)
                }
                is NetworkState2.Failure -> {
                    val (throwable) = messageResponse
                    onFailure(findViewById(R.id.root_view), throwable)
                }
                else -> onFailure(findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    override fun getLayout(): Int {
        return R.layout.activity_send_message
    }

    private fun showIssueDialog(listOfIssues: MutableList<Item>, listener: OnItemClickListener) {

        val view = LayoutInflater.from(this).inflate(R.layout.layout_issue, findViewById(android.R.id.content), false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)


        val supportIssueAdapter = SupportIssueAdapter(listOfIssues, listener)
        recyclerView.adapter = supportIssueAdapter

        dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(view)
        dialog.setOnCancelListener { it.dismiss() }
        dialog.show()
    }

    fun validate(): Boolean {
        return when {
            tvArea.text.toString() == getString(R.string.area_of_issue) -> {
                tvError.visibility = View.VISIBLE
                false
            }
            tvIssue.text.toString() == getString(R.string.select_issue) -> {
                tvIssueError.visibility = View.VISIBLE
                false
            }
            TextUtils.isEmpty(etMessage.text.toString()) -> {
                tv_message_error.visibility = View.VISIBLE
                textInputLayout.setErrorMessage(getString(R.string.valid_message_error))
                false
            }
            else -> true
        }

    }

    private fun setDrawableRight(areaIssue: Int, issue: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            tvArea.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, areaIssue, 0)
            tvIssue.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, issue, 0)
        } else {
            tvArea.setCompoundDrawablesWithIntrinsicBounds(0, 0, areaIssue, 0)
            tvIssue.setCompoundDrawablesWithIntrinsicBounds(0, 0, issue, 0)
        }
    }
}