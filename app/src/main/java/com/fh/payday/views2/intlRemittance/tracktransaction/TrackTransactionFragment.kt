package com.fh.payday.views2.intlRemittance.tracktransaction

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.datasource.models.Card
import com.fh.payday.datasource.models.intlRemittance.IntlTransaction
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.views2.intlRemittance.DeliveryModes

class TrackTransactionFragment : Fragment(), OnTransactionItemClickListener {

    private var activity: TrackTransactionActivity? = null
    private lateinit var textView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context as TrackTransactionActivity?
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments != null) {
            val index = arguments!!.getInt("index")
            showPages(index)
            setTextTitle(index)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_track_transaction, container, false)

        initView(view)
        return view
    }

    private fun initView(view: View) {

        recyclerView = view.findViewById(R.id.recycler_view)
        textView = view.findViewById(R.id.text_view)
        progressBar = view.findViewById(R.id.progress_bar)
    }

    private fun setTextTitle(index: Int) {
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

    private fun showPages(index: Int) {
        val activity = activity ?: return
        val user = UserPreferences.instance.getUser(activity) ?: return
        when (index) {
            0 -> {
                addObserver()
                activity.getViewModel().transactionDate.observe(this, Observer {
                    it ?: return@Observer
                    activity.getViewModel().getTransactions(user.token, user.sessionId, user.refreshToken, user.customerId.toString(), it.fromDate, it.toDate)
                })
            }
            1 -> {
                setRecyclerView(emptyList())
            }
        }
    }

    private fun addObserver() {
        val activity = activity ?: return
        activity.getViewModel().transactionResponse.observe(this, Observer {
            it ?: return@Observer

            val transactionResponse = it.getContentIfNotHandled() ?: return@Observer

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

    private fun setRecyclerView( list: List<IntlTransaction>?) {
        recyclerView.adapter = TrackTransactionAdapter(ArrayList(), this, listener)
        if (list.isNullOrEmpty()) {
            recyclerView.visibility = View.GONE
            progressBar.visibility = View.GONE
            textView.visibility = View.VISIBLE
            return
        }
        val filteredList = list.filter {
            it.paymentMode.equals(activity?.getViewModel()?.deliveryMode, true)
        }
        if (filteredList.isNullOrEmpty()) {
            recyclerView.visibility = View.GONE
            progressBar.visibility = View.GONE
            textView.visibility = View.VISIBLE
            return
        }
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        textView.visibility = View.GONE
        recyclerView.adapter = TrackTransactionAdapter(filteredList, this, listener)
    }

    override fun onTransactionClick(transaction: IntlTransaction) {
        val activity = activity ?: return
        val arguments = arguments ?: return
        startActivity(Intent(activity, TransactionDetailsActivity::class.java).also {
            it.putExtra("transaction", transaction)
            it.putExtra("card", arguments.getParcelable<Card>("card"))
            activity.intent?.extras?.let { bundle -> it.putExtras(bundle) }
        })
    }

    interface OnBankTransferClickListener {
        fun onBankTransferClick(transaction: IntlTransaction)
    }

    private val listener = object : OnBankTransferClickListener {
        override fun onBankTransferClick(transaction: IntlTransaction) {
//            val trackTransactionDialog = TrackTransactionsDialog.Builder()
//                    .setMessage(getString(R.string.track_request_initiated_successfully))
//                    .setResIcon(R.drawable.ic_success_checked)
//                    .setMessageGravity(Gravity.CENTER)
//                    .setPositiveText(getString(R.string.view_details))
//                    .setNegativeText(getString(R.string.okay))
//                    .attachPositiveListener {
//                        startActivity(Intent(activity, TransactionDetailsActivity::class.java).also {
//                            it.putExtra("transaction", transaction)
//                            activity?.intent?.extras?.let { bundle -> it.putExtras(bundle) }
//                        })
//                    }
//                    .build()
//            val fm = fragmentManager ?: return
//
//            trackTransactionDialog.apply {
//                isCancelable = false
//                show(fm, trackTransactionDialog.tag)
//            }
        }
    }
}