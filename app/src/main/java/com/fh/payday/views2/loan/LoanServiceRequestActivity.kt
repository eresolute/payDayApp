package com.fh.payday.views2.loan

import android.Manifest
import android.app.DownloadManager
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.constraint.ConstraintLayout
import android.support.design.widget.TextInputLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ExpandableListView
import android.widget.ProgressBar
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.*
import com.fh.payday.utilities.AccessMatrix.Companion.APPLY_LOAN
import com.fh.payday.viewmodels.LoanViewModel
import com.fh.payday.views2.loan.apply.ApplyLoanActivity
import com.fh.payday.views2.loan.apply.TermsAndConditionsDialog
import com.fh.payday.views2.shared.custom.EligibilityDialogFragment
import com.fh.payday.views2.shared.custom.PermissionsDialog
import com.fh.payday.views2.shared.fragments.TermsConditionsDialogFragment
import kotlinx.android.synthetic.main.bottombar_dashboard.*
import kotlinx.android.synthetic.main.toolbar.*
import java.io.File

class LoanServiceRequestActivity : BaseActivity() {

    enum class TYPE { CLEARANCE, RESCHEDULE, LIABILITY, E_SETTLEMENT, P_SETTLEMENT }

    private lateinit var expandableList: ExpandableListView
    private lateinit var progressBar: ProgressBar
    private lateinit var viewModel: LoanViewModel
    private lateinit var tvRequest: TextView
    private lateinit var textView: TextView
    private lateinit var etLoanNumber: EditText
    private lateinit var textInput: TextInputLayout
    private lateinit var adapter: LoanRequestAdapter
    private var card: String? = null

    /*private var receiver: BroadcastReceiver? = null*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        btm_home.setOnClickListener(this)
        btm_menu_support.setOnClickListener(this)
        btm_menu_loan_calc.setOnClickListener(this)
        btm_menu_branch.setOnClickListener(this)
        toolbar_back.setOnClickListener(this)
        val dismissListener = object : EligibilityDialogFragment.OnDismissListener {
            override fun onDismiss(dialog: DialogInterface) {
                dialog.dismiss()
            }
        }
        tvRequest.setOnClickListener {
            val label = intent.getSerializableExtra("label")

            if (TextUtils.isEmpty(etLoanNumber.text.toString()) || etLoanNumber.text.toString().length <= 4) {
                textInput.error = getString(R.string.invalid_loan_number)
                return@setOnClickListener
            }
            EligibilityDialogFragment.Builder(dismissListener)
                    .setTitle(getString(R.string.confirm_request))
                    .setBtn1Text(getString(R.string.confirm))
                    .setBtn2Text(getString(R.string.cancel))
                    .setConfirmListener(object : EligibilityDialogFragment.OnConfirmListener {
                        override fun onConfirm(dialog: DialogInterface) {
                            dialog.dismiss()
                            if (!isNetworkConnected()) {
                                onFailure(findViewById(R.id.layout), getString(R.string.no_internet_connectivity))
                                return
                            }
                            val user = UserPreferences.instance.getUser(this@LoanServiceRequestActivity)
                                    ?: return
                            getViewModel().requestRescheduleLoan(user.token, user.sessionId, user.refreshToken,
                                    user.customerId.toLong(), etLoanNumber.text.toString(), label)
                        }
                    })
                    .setCancelListener(object : EligibilityDialogFragment.OnCancelListener {
                        override fun onCancel(dialog: DialogInterface) {
                            dialog.dismiss()
                        }
                    })
                    .setCancelable(false)
                    .build()
                    .show(supportFragmentManager, "")
        }

        etLoanNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                textInput.clearErrorMessage()
            }

        })

        toolbar_help.setOnClickListener {
            startHelpActivity("loanServicesHelp")
        }

    }

    /*override fun onDestroy() {
        receiver?.let { LocalBroadcastManager.getInstance(this).unregisterReceiver(it) }
        super.onDestroy()
    }*/

    private fun getViewModel(): LoanViewModel {
        return viewModel
    }

    override fun getLayout(): Int = R.layout.activity_loan_service_request

    override fun init() {

        expandableList = findViewById(R.id.el_requests)
        progressBar = findViewById(R.id.progress_bar)
        tvRequest = findViewById(R.id.tv_request)
        etLoanNumber = findViewById(R.id.et_loan_number)
        textInput = findViewById(R.id.textInput_loan_number)
        textView = findViewById(R.id.text_view)
        val rootLayout = findViewById<ConstraintLayout>(R.id.root_layout)

        val label = intent.getSerializableExtra("label")
        card = intent.getStringExtra("card")

        val user = UserPreferences.instance.getUser(this)
        when (label) {
            TYPE.RESCHEDULE -> {
                setToolbarTitle(getString(R.string.reschedule_loan))
            }
            TYPE.E_SETTLEMENT -> {
                setToolbarTitle(getString(R.string.early_settlement))
            }
            TYPE.LIABILITY -> {
                setToolbarTitle(getString(R.string.liability))
            }
            TYPE.CLEARANCE -> {
                setToolbarTitle(getString(R.string.clearence_letter))
            }
            TYPE.P_SETTLEMENT -> {
                setToolbarTitle(getString(R.string.partial_settlement))
            }

        }

        val loanNumber = intent?.getStringExtra("loan_number")

        if (loanNumber.isNullOrEmpty()) {
            rootLayout.visibility = View.GONE
            handleNoLoanAvailable()
            return
        }
        etLoanNumber.setText(loanNumber)
        etLoanNumber.setSelection(etLoanNumber.text.length)

        viewModel = ViewModelProviders.of(this).get(LoanViewModel::class.java)

        addRescheduleObserver()
        addRequestRescheduleObserver()




        if (user != null)
            getViewModel().rescheduleLoan(user.token, user.sessionId, user.refreshToken, user.customerId.toLong(), label)
    }

    private fun setToolbarTitle(title: String) {
        findViewById<TextView>(R.id.toolbar_title).text = title
    }

    private fun addRescheduleObserver() {
        getViewModel().rescheduleLoanState.observe(this, Observer {
            it ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                progressBar.visibility = View.VISIBLE
                return@Observer
            }

            progressBar.visibility = View.GONE

            when (state) {
                is NetworkState2.Success -> {
                    val label = (intent.getSerializableExtra("label") as TYPE?) ?: TYPE.RESCHEDULE
                    if (isEmptyList(state.data)) {
                        textView.visibility = View.VISIBLE
                        //return@Observer
                    }
                    adapter = LoanRequestAdapter(this, state.data, label)
                    expandableList.setAdapter(adapter)
                }
                is NetworkState2.Error -> {
                    textView.visibility = View.VISIBLE
                    if (state.isSessionExpired) {
                        return@Observer onSessionExpired(state.message)
                    }
                    onError(state.message)
                }
                is NetworkState2.Failure -> {
                    textView.visibility = View.VISIBLE
                    onFailure(findViewById(R.id.layout), state.throwable)
                }
                else -> {
                    textView.visibility = View.VISIBLE
                    onFailure(findViewById(R.id.layout)!!, CONNECTION_ERROR)
                }
            }
        })
    }

    private fun addRequestRescheduleObserver() {
        getViewModel().requestRescheduleLoanState.observe(this, Observer {
            it ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                showProgress(getString(R.string.processing))
                return@Observer
            }

            hideProgress()

            when (state) {
                is NetworkState2.Success -> {
                    if (state.message != null)
                        showMessage(state.message)
                    textView.visibility = View.GONE
                    adapter.addItem(state.data)
                }
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        return@Observer onSessionExpired(state.message)
                    }
                    onError(state.message)
                }
                is NetworkState2.Failure -> {
                    onFailure(findViewById(R.id.layout), state.throwable)
                }
                else -> {
                    onFailure(findViewById(R.id.layout)!!, CONNECTION_ERROR)
                }
            }
        })
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        when (requestCode) {
            PERM_WRITE_STORAGE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    val path = viewModel.filePath ?: return
                    downloadFile(path)
                }
            }
        }
    }

    fun downloadFile(path: String?) {
        if (path.isNullOrEmpty()) return

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            downloadFileIfPermGranted(path)
        } else {
            viewModel.filePath = path

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                PermissionsDialog.Builder()
                        .setTitle(getString(R.string.app_permissions))
                        .setDescription(getString(R.string.write_storage_permissions))
                        .setNegativeText(getString(R.string.app_settings))
                        .setPositiveText(getString(R.string.not_now))
                        .build()
                        .show(this)
            } else {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERM_WRITE_STORAGE)
            }
        }
    }

    private fun downloadFileIfPermGranted(path: String) {
        try {
            val url = BASE_URL + path

            val uri = Uri.parse(url)
            val request = DownloadManager.Request(uri).apply {
                val filename = File(url).name

                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                setAllowedOverRoaming(false)
                setTitle("Downloading")
                setDescription(filename)
                setVisibleInDownloadsUi(true)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                        "/payday/$filename")
            }
            val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
            manager?.enqueue(request)

            showMessage(findViewById(R.id.layout), getString(R.string.download_started))

            /*if (receiver != null) {
                unregisterReceiver(receiver)
            }

            receiver = DownloadBroadcastReceiver(manager ?: return, refId ?: return) {
                showMessage(findViewById(R.id.layout), getString(R.string.download_complete))
            }

            receiver?.let {
                LocalBroadcastManager.getInstance(this)
                        .registerReceiver(it, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
            }*/
        } catch (e: Exception) {
            onFailure(findViewById(R.id.layout), getString(R.string.request_error))
        }
    }

    companion object {
        private const val PERM_WRITE_STORAGE = 7
    }

    private fun handleNoLoanAvailable() {
        findViewById<View>(R.id.cl_no_loan_available).visibility = View.VISIBLE
        findViewById<View>(R.id.btn_apply_loan).setOnClickListener {
            card ?: return@setOnClickListener
            if (hasAccess(card, APPLY_LOAN)) {
                val termsAndCondDialog = TermsAndConditionsDialog.Builder()
                        .attachPositiveListener {
                            finish()
                            startActivity(Intent(this, ApplyLoanActivity::class.java))
                        }
                        .attachLinkListener {
                            val dialogFragment: DialogFragment = TermsConditionsDialogFragment
                                    .newInstance(LOAN_TC_URL, getString(R.string.close))
                            dialogFragment.show(supportFragmentManager, "dialog")
                        }
                        .setTermsConditionsLink(true)
                        .build()

                termsAndCondDialog.apply {
                    isCancelable = false
                    show(supportFragmentManager, termsAndCondDialog.tag)
                }
            } else {
                showCardStatusError(card)
            }

        }
    }

}
