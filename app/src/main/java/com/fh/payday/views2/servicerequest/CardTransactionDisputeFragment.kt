package com.fh.payday.views2.servicerequest

import android.app.DatePickerDialog
import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.datasource.models.CardTransactionDispute
import com.fh.payday.datasource.models.shared.ListModel
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.DateTime
import com.fh.payday.views2.shared.custom.AlertDialogFragment
import com.fh.payday.views2.shared.custom.DialogInfoFragment
import com.fh.payday.views2.shared.fragments.DatePickerFragment
import kotlinx.android.synthetic.main.fragment_card_transaction_dispute.*
import java.util.*

class CardTransactionDisputeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private var activity: ServiceRequestOptionActivity? = null
    private lateinit var progressBar: ProgressBar
    private lateinit var textView: TextView
    private lateinit var tvToCalender: TextView
    private lateinit var tvFromCalender: TextView
    private lateinit var constraintLayout: ConstraintLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity?.viewModel?.apply {
            if (user == null) {
                user = UserPreferences.instance.getUser(activity ?: return@apply)
            }
        }

        return inflater.inflate(R.layout.fragment_card_transaction_dispute, container, false).apply {
            init(this)
            attachObservers()
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context as ServiceRequestOptionActivity?
    }

    override fun onDetach() {
        activity = null
        super.onDetach()
    }

    private fun init(view: View) {
        recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view).apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }
        progressBar = view.findViewById(R.id.progress_bar)
        textView = view.findViewById(R.id.text_view)
        tvFromCalender = view.findViewById(R.id.tv_calender_from)
        tvToCalender = view.findViewById(R.id.tv_calender_to)
        constraintLayout = view.findViewById(R.id.root_view)

        tvFromCalender.setOnClickListener { showDatePicker(it) }
        tvToCalender.setOnClickListener { showDatePicker(it) }

    }

    private fun showDatePicker(view: View) {
        val viewModel = activity?.viewModel ?: return
        val fragmentManager = fragmentManager ?: return
        val maxDate = Calendar.getInstance()
        val minDate = Calendar.getInstance().apply { add(Calendar.YEAR, -1) }

        val dateTime = when (view.id) {
            tvToCalender.id -> DateTime.parseDate(viewModel.transactionDate.value?.toDate)
            else -> DateTime.parseDate(viewModel.transactionDate.value?.fromDate)
        }

        val day = dateTime.dayOfMonth
        val month = dateTime.monthOfYear - 1
        val year = dateTime.year

        DatePickerFragment.Builder()
                .setDay(day)
                .setMonth(month)
                .setYear(year)
                .setMinDate(minDate.time)
                .setMaxDate(maxDate.time)
                .attachDateSetListener(DatePickerDialog.OnDateSetListener { _, y, m, dayOfMonth ->
                    val d = String.format(
                            getString(R.string.date_format_hypen),
                            y.toString(),
                            String.format("%02d", (m + 1)),
                            String.format("%02d", dayOfMonth)
                    ).replace("\\s+".toRegex(), "")

                    /* when (view.id) {
                         tvFromCalender.id -> viewModel.setDate(d, tvToCalender.text.toString())
                         tvToCalender.id -> viewModel.setDate(tvFromCalender.text.toString(), d)
                     }*/
                    val (fromDate, toDate) = viewModel.transactionDate.value
                            ?: return@OnDateSetListener

                    when (view.id) {
                        tvFromCalender.id -> {
                            if (DateTime.isValidDateRange(d, toDate))
                                viewModel.setDate(d, toDate)
                            else
                                activity?.onFailure(root_view, getString(R.string.invalid_date))
                        }
                        tvToCalender.id -> {
                            if (DateTime.isValidDateRange(fromDate, d))
                                viewModel.setDate(fromDate, d)
                            else
                                activity?.onFailure(root_view, getString(R.string.invalid_date))
                        }
                    }
                })
                .build()
                .show(fragmentManager, "datePicker")
    }

    private fun fetchCardTransactions() {
        val viewModel = activity?.viewModel ?: return
        val (fromDate, toDate) = viewModel.transactionDate.value ?: return
        val (_, _, token1, refreshToken, customerId, _, sessionId) = viewModel.user ?: return
        activity?.hideNoInternetView()
        if (activity?.isNetworkConnected() == false) {
            constraintLayout.visibility = View.GONE
            return activity?.showNoInternetView { fetchCardTransactions() } ?: Unit
        }

        constraintLayout.visibility = View.VISIBLE

        viewModel.getCardTransactions(
                token1, sessionId, customerId.toLong(),
                refreshToken, fromDate, toDate
        )
    }

    private fun attachObservers() {
        activity?.viewModel?.transactionDate?.observe(this, Observer { transDate ->
            transDate ?: return@Observer
            tvFromCalender.text = DateTime.parse(transDate.fromDate, outputFormat = "dd MMM yyyy")
            tvToCalender.text = DateTime.parse(transDate.toDate, outputFormat = "dd MMM yyyy")
            fetchCardTransactions()
        })

        activity?.viewModel?.cardTransactions?.observe(this, Observer {
            val activity = activity ?: return@Observer
            it ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                textView.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
                return@Observer
            }

            progressBar.visibility = View.GONE

            when (state) {
                is NetworkState2.Success -> onSuccess(
                        state.data ?: return@Observer
                )
                is NetworkState2.Error -> {
                    val (message, _, isSessionExpired) = state
                    if (isSessionExpired) {
                        activity.onSessionExpired(message)
                        return@Observer
                    }

                    activity.handleErrorCode(state.errorCode.toInt(), message)

//                    activity.onError(message)
                }
                is NetworkState2.Failure -> activity.onFailure(activity.findViewById(R.id.root_view),
                        state.throwable)
                else -> activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })

        activity?.viewModel?.createDisputeState?.observe(this, Observer {
            val activity = activity ?: return@Observer
            it ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing))
                return@Observer
            }

            activity.hideProgress()

            when (state) {
                is NetworkState2.Success -> {
                    val message = state.data ?: return@Observer
                    activity.showMessage(message, R.drawable.ic_success_checked, R.color.colorAccent, AlertDialogFragment.OnConfirmListener { it ->
                        it.dismiss()
                        fetchCardTransactions()
                    })
                }
                is NetworkState2.Error -> {
                    val (message, _, isSessionExpired) = state
                    if (isSessionExpired) {
                        activity.onSessionExpired(message)
                        return@Observer
                    }

                    activity.handleErrorCode(state.errorCode.toInt(), message)
                }
                is NetworkState2.Failure -> activity.onFailure(activity.findViewById(R.id.root_view),
                        state.throwable)
                else -> activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    private fun onSuccess(transactions: List<CardTransactionDispute>) {
        if (transactions.isNullOrEmpty()) {
            textView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            return
        }

        textView.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        recyclerView.adapter = CardDisputeAdapter(transactions) {
            it.TransactionDescription ?: return@CardDisputeAdapter
            val items = arrayListOf(
                    ListModel(getString(R.string.reference_no), it.TransactionReferenceNumber),
                    ListModel(getString(R.string.transaction_amount), String.format(getString(R.string.amount_in_aed), it.TransactionAmount)),
                    ListModel(getString(R.string.merchant_name), it.MerchantName ?: ""),
                    ListModel(getString(R.string.transaction_date),
                            DateTime.parse(it.TransactionDateTime, "yyyy-MM-dd'T'HH:mm:ss'Z'", "dd/MM/yyyy hh:mm a"))
            )

            var dialog = DialogInfoFragment.getInstance(it.TransactionDescription, items, false, getString(R.string.create_dispute))
            if (!it.status.isNullOrEmpty() && !it.remarks.isNullOrEmpty()) {
                items.add(ListModel("Remarks", it.remarks))
                items.add(ListModel("Status", it.status.toUpperCase()))
            }
            if (it.status.isNullOrEmpty()) {
                dialog = DialogInfoFragment.getInstance(it.TransactionDescription, items, true, getString(R.string.create_dispute))

                dialog.setConfirmAction {
                    if (activity?.isNetworkConnected() == false) {
                        activity?.onFailure(root_view, getString(R.string.no_internet_connectivity))
                        return@setConfirmAction
                    }
                    createDispute(it)
                }
            }
            dialog.show(fragmentManager, "")
        }
    }

    private fun createDispute(tran: CardTransactionDispute) {
        val (_, _, token, refreshToken, customerId,
                _, sessionId) = activity?.viewModel?.user ?: return
        activity?.viewModel?.createDispute(token, sessionId, refreshToken, customerId.toLong(),
                tran.TransactionAmount, tran.TransactionDateTime,
                tran.MerchantName ?: "", tran.TransactionReferenceNumber)
    }
}
