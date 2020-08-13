package com.fh.payday.views2.moneytransfer


import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.fh.payday.BaseFragment
import com.fh.payday.R
import com.fh.payday.datasource.models.CreditDebit
import com.fh.payday.datasource.models.TransactionHistory
import com.fh.payday.datasource.models.Transactions
import com.fh.payday.datasource.models.shared.ListModel
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.utilities.DateTime
import com.fh.payday.viewmodels.TransferViewModel
import com.fh.payday.viewmodels.TransferViewModel.Companion.P2CC
import com.fh.payday.viewmodels.TransferViewModel.Companion.P2IBAN
import com.fh.payday.viewmodels.TransferViewModel.Companion.P2P
import com.fh.payday.views2.shared.custom.DialogInfoFragment
import com.fh.payday.views2.transactionhistory.TransactionHistoryDetailFragment
import kotlinx.android.synthetic.main.activity_login.*

class TransferTHFragment : BaseFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var textView: TextView
    private lateinit var progressBar: ProgressBar

    val listener = TransactionHistoryDetailFragment.OnTransactionClickListener {

        val list = java.util.ArrayList<ListModel>()
        list.add(ListModel(getString(R.string.reference_no), it.TransactionReferenceNumber))
        list.add(ListModel(getString(R.string.transaction_amount), String.format(getString(R.string.amount_in_aed), it.TransactionAmount)))
        list.add(ListModel(getString(R.string.merchant_name), it.MerchantName))
        list.add(ListModel(getString(R.string.transaction_date),
                DateTime.parse(it.TransactionDateTime, "yyyy-MM-dd'T'HH:mm:ss'Z'", "dd/MM/yyyy hh:mm a"))
        )
        val dialog = DialogInfoFragment.getInstance(it.TransactionDescription,
                list,
                false,
                getString(R.string.create_dispute),
                getString(R.string.ok)
        )

        dialog.show(fragmentManager!!, "")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_transfer_th, container, false)
        init(view)
        attachObserver()
        return view
    }

    private fun init(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view)
        progressBar = view.findViewById(R.id.progress_bar)
        textView = view.findViewById(R.id.text_view)

        recyclerView.adapter = TransactionHistoryAdapter(ArrayList(), listener)
    }

    private fun getViewModel(): TransferViewModel? {
        return when (val activity = activity ?: return null) {
            is TransactionHistoryActivity -> activity.viewModel
            else -> null
        }
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

    fun attachObserver() {
        getViewModel()?.transactionHistoryState?.observe(activity ?: return, Observer {
            it ?: return@Observer

            when(val state = it.peekContent()) {
                is NetworkState2.Loading -> showProgress()
                is NetworkState2.Success -> {
                    hideProgress()
                    onSuccess(state.data)
                }
                is NetworkState2.Error -> {
                    activity?.handleErrorCode(state.errorCode.toInt(), state.message)
                }
                else -> hideProgress()
            }
        })
    }

    private fun onSuccess(data: TransactionHistory?) {
        val type = arguments?.getString("issue")
        val transactions = when(type) {
            P2CC -> {
                data?.P2CC as List<Transactions>
            }

            P2IBAN -> {
                data?.P2IBAN as List<Transactions>
            }

            P2P -> {
                data?.P2P as List<Transactions>
            }

            else -> ArrayList<Transactions>()
        }

        if (transactions.isNullOrEmpty()) return

        recyclerView.visibility = View.VISIBLE
        textView.visibility = View.GONE

        val activity = activity as TransactionHistoryActivity
        activity.setCreditDebit(data?.CreditDebit ?: CreditDebit(0.0, 0.0))

        val adapter = recyclerView.adapter
        if (adapter is TransactionHistoryAdapter) {
            adapter.add(transactions)
        }
    }

}
