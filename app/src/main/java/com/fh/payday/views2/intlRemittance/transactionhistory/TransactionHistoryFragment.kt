package com.fh.payday.views2.intlRemittance.transactionhistory

import android.Manifest
import android.app.DownloadManager
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.datasource.DataGenerator
import com.fh.payday.datasource.models.Card
import com.fh.payday.datasource.models.intlRemittance.IntlBeneficiary
import com.fh.payday.datasource.models.intlRemittance.IntlTransaction
import com.fh.payday.datasource.models.intlRemittance.TransactionDetail
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.*
import com.fh.payday.views2.intlRemittance.tracktransaction.TransactionDetailsActivity
import com.fh.payday.views2.intlRemittance.transfer.TransferActivity
import com.fh.payday.views2.shared.custom.AlertDialogFragment
import com.fh.payday.views2.shared.custom.PermissionsDialog
import java.io.File
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class TransactionHistoryFragment : Fragment(), OnTransactionItemClickListener {

    private var activity: IntlTransactionHistoryActivity? = null
    private lateinit var textView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    /*private var receiver: BroadcastReceiver? = null*/

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context as IntlTransactionHistoryActivity?
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_track_transaction, container, false)
        initView(view)
        addObserver()
        return view
    }

    /*override fun onDestroy() {
        super.onDestroy()
        val activity = activity ?: return
        receiver?.let { LocalBroadcastManager.getInstance(activity).unregisterReceiver(it) }
    }*/

    private fun setRecyclerView(list: List<IntlTransaction>?) {
        if (list.isNullOrEmpty()) {
            recyclerView.visibility = View.GONE
            progressBar.visibility = View.GONE
            textView.visibility = View.VISIBLE
            return
        }
        val index = if (arguments != null) arguments!!.getInt("index") else 0
        val items = mutableListOf<IntlTransaction>()
        items.clear()
        when (index) {
            0 -> {
                list.filter {
                    it.favourite?.toInt() == 1
                }.forEach { items.add(it) }
            }
            1 -> {
                items.addAll(list)
            }
        }

        if (items.isNullOrEmpty()) {
            recyclerView.visibility = View.GONE
            progressBar.visibility = View.GONE
            textView.visibility = View.VISIBLE
            return
        }

        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        textView.visibility = View.GONE
        recyclerView.adapter = TransactionHistoryAdapter(items, this)
    }

    private fun initView(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view)
        textView = view.findViewById(R.id.text_view)
        progressBar = view.findViewById(R.id.progress_bar)
    }

    private fun showProgress() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        textView.visibility = View.GONE
    }

    private fun hideProgress() {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.GONE
        textView.visibility = View.VISIBLE
    }


    override fun onTransactionClick(transaction: IntlTransaction) {
        showBottomSheet(transaction)
    }

    private fun showBottomSheet(transaction: IntlTransaction) {

        val transactionOptions = if (transaction.favourite?.toInt() == 1) {
            DataGenerator.getIntlTransactionOptions2(activity)
        } else {
            DataGenerator.getIntlTransactionOptions(activity)
        }

        val bottomSheet = TransactionOptionBottomSheet.newInstance(transactionOptions) { it ->
            when (it) {
                0 -> startActivity(Intent(activity, TransactionDetailsActivity::class.java).also {
                    it.putExtra("transaction", transaction)
                    val arguments = arguments ?: return@newInstance
                    it.putExtra("card", arguments.getParcelable<Card>("card"))
                })
                1 -> {
                    //Add To Favourites
                    if (transaction.favourite?.toInt() == 1) {
                        addFavouriteObserver()
                        val activity = activity ?: return@newInstance
                        val user = UserPreferences.instance.getUser(activity) ?: return@newInstance
                        activity.viewmodel.addFavourite(user.token, user.sessionId, user.refreshToken,
                                user.customerId.toString(), transaction.dealerTxnId, 0)
                    } else {
                        addFavouriteObserver()
                        val activity = activity ?: return@newInstance
                        val user = UserPreferences.instance.getUser(activity) ?: return@newInstance
                        activity.viewmodel.addFavourite(user.token, user.sessionId, user.refreshToken,
                                user.customerId.toString(), transaction.dealerTxnId, 1)
                    }
                }
                2 -> {
                    //Repeat Transaction
                    startActivity(Intent(activity, TransferActivity::class.java).apply {
                        activity?.intent?.extras?.let { putExtras(it) }
                        putExtra("action", "repeat_transaction")

                        transaction.receiverRefNumber ?: return@newInstance
                        transaction.beneficiaryBankName ?: return@newInstance
                        transaction.receiverCountryCode ?: return@newInstance
                        val beneficiary = IntlBeneficiary(transaction.receiverRefNumber,
                                transaction.beneficiaryName,
                                "",
                                "",
                                transaction.beneficiaryAccountNumber,
                                transaction.receiverCountryCode,
                                "",
                                "",
                                transaction.beneficiaryName,
                                "",
                                transaction.beneficiaryBankName,
                                transaction.payOutCurrency,
                                transaction.accessKey,
                                transaction.favourite)

                        putExtra("beneficiary", beneficiary)
                        putExtra("transactions_id", transaction.dealerTxnId)
                        putExtra("partnerTxnRefNo", transaction.partnerTxnRefNo)
                        putExtra("payinAmount", transaction.baseAmount)
                        putExtra("accessKey", transaction.accessKey)
                        putExtra("paymentMode", transaction.paymentMode)


                    })
                }
                3 -> {
                    //Resend Transaction
                    startActivity(Intent(activity, TransferActivity::class.java).apply {
                        activity?.intent?.extras?.let { putExtras(it) }
                        putExtra("action", "MyBeneficiariesActivity")

                        transaction.receiverRefNumber ?: return@newInstance
                        transaction.beneficiaryBankName ?: return@newInstance
                        transaction.receiverCountryCode ?: return@newInstance
                        val beneficiary = IntlBeneficiary(transaction.receiverRefNumber,
                                transaction.beneficiaryName,
                                "",
                                "",
                                transaction.beneficiaryAccountNumber,
                                transaction.receiverCountryCode,
                                "",
                                "",
                                transaction.beneficiaryName,
                                transaction.receiverFlag ?: "",
                                transaction.beneficiaryBankName,
                                transaction.payOutCurrency,
                                transaction.accessKey,
                                transaction.favourite)

                        putExtra("beneficiary", beneficiary)
                        putExtra("paymentMode", transaction.paymentMode)

                    })
                }

                4 -> {
                    addTransDetailsPDFObserver()
                    val activity = activity ?: return@newInstance
                    val user = UserPreferences.instance.getUser(activity) ?: return@newInstance
                    activity.viewmodel.getTransactionDetailsPDF(user.token, user.sessionId, user.refreshToken,
                            user.customerId.toString(), transaction.dealerTxnId)
                }
                5 -> {
                    //Share via SMS
                    val activity = activity ?: return@newInstance
                    val user = UserPreferences.instance.getUser(activity) ?: return@newInstance
                    addDetailObserver()
                    activity.viewmodel.getTransactionDetail(user.token, user.sessionId,
                            user.refreshToken, user.customerId.toString(), transaction.accessKey,transaction.dealerTxnId, transaction.partnerTxnRefNo)
                }
            }
        }

        bottomSheet.show(activity?.supportFragmentManager, bottomSheet.tag)
    }

    private fun addDetailObserver() {
        val activity = activity ?: return
        activity.viewmodel.transactionDetail.observe(this, Observer {
            it ?: return@Observer

            val transactionResponse = it.getContentIfNotHandled() ?: return@Observer

            if (transactionResponse is NetworkState2.Loading) {
                activity.showProgress(activity.getString(R.string.fetching_transaction_details))
                return@Observer
            }

            activity.hideProgress()

            when (transactionResponse) {
                is NetworkState2.Success -> {
                    val transaction = transactionResponse.data ?: return@Observer
                    val transactionText = getFormattedMessage(transaction)

                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, transactionText)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    startActivity(shareIntent)

                    val user = UserPreferences.instance.getUser(activity) ?: return@Observer
                    activity.viewmodel.trackSharedTransaction(user.token, user.sessionId, user.refreshToken,
                            user.customerId.toString(), transaction.txnRefNo)
                }

                is NetworkState2.Error -> {
                    if (transactionResponse.isSessionExpired) {
                        activity.onSessionExpired(transactionResponse.message)
                        return@Observer
                    }

                    val (message) = transactionResponse
                    activity.handleErrorCode(transactionResponse.errorCode.toInt(), message)
                }
                is NetworkState2.Failure -> {
                    activity.onFailure(activity.findViewById(R.id.root_view), transactionResponse.throwable)
                }
            }
        })
    }

    private fun getFormattedMessage(transaction: TransactionDetail): String {
        val card = arguments?.getParcelable<Card>("card") ?: return ""

        val formatter = DecimalFormat("#,###.####", DecimalFormatSymbols.getInstance(Locale("en")))

        return """
            |Date: ${DateTime.parse(transaction.transactionGMTDateTime, "yyyy-MM-dd'T'HH:mm:ss'Z'", "dd/MM/yyyy hh:mm a")}
            |Status: ${transaction.transactionStatusDesc} 
            |Exchange House Reference No: ${transaction.txnRefNo}
            |FH Reference No: ${transaction.partnerTxnRefNo}
            |From : ${transaction.senderFirstName} ${transaction.senderMiddleName} ${transaction.senderLastName}, ${maskCardNumber(card.cardNumber, "xxxx xxxx xxxx ####")}, ${transaction.emailId}
            |To: ${transaction.receiverFirstName} ${transaction.receiverLastName}, ${transaction.receiverBankName}, ${transaction.receiverCountryCode}, ${transaction.receiverBankAccountNo}
            |You sent: ${transaction.payinAmount.getDecimalValue()} ${transaction.payinCcyCode}
            |They received: ${transaction.payoutAmount.getDecimalValue()} ${transaction.payoutCcyCode}
            |Purpose: ${transaction.purposeOfTxn}
            |Promo Code: ${transaction.promoCode}
            |Commission: ${transaction.commission.getDecimalValue()} AED
            |VAT: ${transaction.tax} AED
            |Total Amount Sent: ${transaction.totalPayInAmount.getDecimalValue()} ${transaction.payinCcyCode}
            |Total Amount Received: ${transaction.payoutAmount.getDecimalValue()} ${transaction.payoutCcyCode}
            |Exchange Rate: 1 ${transaction.payoutCcyCode} = ${transaction.xchgRatePayin2Payout} ${transaction.payinCcyCode}
        """.trimMargin()
    }

    private fun addObserver() {
        val activity = activity ?: return
        activity.viewmodel.transactionResponse.observe(this, Observer {
            it ?: return@Observer

            val transactionResponse = it.peekContent()

            if (transactionResponse is NetworkState2.Loading) {
                showProgress()
                return@Observer
            }

            hideProgress()

            when (transactionResponse) {
                is NetworkState2.Success -> {
                    val list = transactionResponse.data
                    setRecyclerView(list)
                }

                is NetworkState2.Error -> {

                    if (transactionResponse.isSessionExpired) {
                        activity.onSessionExpired(transactionResponse.message)
                        return@Observer
                    }
                    activity.handleErrorCode(transactionResponse.errorCode.toInt(), transactionResponse.message)
                }

                is NetworkState2.Failure -> activity.onFailure(activity.findViewById(R.id.root_view),
                        getString(R.string.request_error))
                else -> activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
            }

        })
    }

    private fun addFavouriteObserver() {
        val activity = activity ?: return
        activity.viewmodel.addFavouriteResponse.observe(this, Observer {
            it ?: return@Observer

            val transactionResponse = it.getContentIfNotHandled() ?: return@Observer

            if (transactionResponse is NetworkState2.Loading) {
                progressBar.visibility = View.VISIBLE
                return@Observer
            }
            progressBar.visibility = View.GONE

            when (transactionResponse) {
                is NetworkState2.Success -> {
                    val message = transactionResponse.data ?: return@Observer
                   val dialog =  AlertDialogFragment.Builder()
                            .setMessage(message)
                            .setIcon(R.drawable.ic_success_checked_blue)
                            .setCancelable(false)
                            .setButtonVisibility(View.GONE)
                            .build()
                    dialog.show(fragmentManager, "")


                    Handler().postDelayed({
                        val transactionDate = activity.viewmodel.transactionDate.value
                            ?: return@postDelayed
                        val user = UserPreferences.instance.getUser(activity)
                            ?: return@postDelayed
                        val fromDate = transactionDate.fromDate
                        val toDate = transactionDate.toDate
                        activity.viewmodel.getTransactions(user.token, user.sessionId, user.refreshToken,
                            user.customerId.toString(), fromDate, toDate)
                        dialog.dismiss()
                    }, 2000)
                }

                is NetworkState2.Error -> {

                    if (transactionResponse.isSessionExpired) {
                        activity.onSessionExpired(transactionResponse.message)
                        return@Observer
                    }
                    activity.handleErrorCode(transactionResponse.errorCode.toInt(), transactionResponse.message)
                }

                is NetworkState2.Failure -> activity.onFailure(activity.findViewById(R.id.root_view),
                        getString(R.string.request_error))
                else -> activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
            }

        })
    }

    private fun addTransDetailsPDFObserver() {
        val activity = activity ?: return
        activity.viewmodel.transDetailsPDF.observe(this, Observer {
            it ?: return@Observer

            when (val transDetailsPDF = it.getContentIfNotHandled() ?: return@Observer) {
                is NetworkState2.Success -> {
                    val pdfFile = transDetailsPDF.data ?: return@Observer
                    downloadFile(pdfFile.filePath)
                }

                is NetworkState2.Error -> {

                    if (transDetailsPDF.isSessionExpired) {
                        activity.onSessionExpired(transDetailsPDF.message)
                        return@Observer
                    }
                    activity.handleErrorCode(transDetailsPDF.errorCode.toInt(), transDetailsPDF.message)
                }

                is NetworkState2.Failure -> activity.onFailure(activity.findViewById(R.id.root_view),
                        getString(R.string.request_error))
                else -> activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
            }

        })
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERM_WRITE_STORAGE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    val activity = activity ?: return
                    val path = activity.viewmodel.filePath ?: return
                    downloadFile(path)
                }
            }
        }
    }

    private fun downloadFile(path: String?) {

        val activity = activity ?: return
        if (path.isNullOrEmpty()) return
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            downloadFileIfPermGranted(path)

        } else {
            activity.viewmodel.filePath = path
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                PermissionsDialog.Builder()
                        .setTitle(getString(R.string.app_permissions))
                        .setDescription(getString(R.string.write_storage_permissions))
                        .setNegativeText(getString(R.string.app_settings))
                        .setPositiveText(getString(R.string.not_now))
                        .build()
                        .show(activity)
            } else {
                ActivityCompat.requestPermissions(activity,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERM_WRITE_STORAGE)
            }
        }
    }

    private fun downloadFileIfPermGranted(path: String) {
        val activity = activity ?: return
        try {
            val url = "$BASE_URL$path"
            val uri = Uri.parse(url)
            val request = DownloadManager.Request(uri).apply {
                val filename = File(url).name
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                setAllowedOverRoaming(false)
                setTitle("Downloaded ")
                setDescription(filename)
                setVisibleInDownloadsUi(true)
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                        "/payday/$filename")
            }
            val manager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
            manager?.enqueue(request)

            activity.showMessage(activity.findViewById(R.id.root_view), getString(R.string.download_started))

            /*receiver?.let { LocalBroadcastManager.getInstance(activity).unregisterReceiver(it) }

            receiver = DownloadBroadcastReceiver(manager ?: return, refId ?: return) {
                activity.showMessage(activity.findViewById(R.id.root_view), getString(R.string.download_complete))
            }

            receiver?.let {
                LocalBroadcastManager.getInstance(activity)
                        .registerReceiver(it, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
            }*/
        } catch (e: Exception) {
            activity.onFailure(activity.findViewById(R.id.root_view), getString(R.string.request_error))
        }
    }

    companion object {
        private const val PERM_WRITE_STORAGE = 7
    }
}