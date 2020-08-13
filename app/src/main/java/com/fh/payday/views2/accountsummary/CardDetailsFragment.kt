package com.fh.payday.views2.accountsummary

import android.arch.lifecycle.Observer
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.fh.payday.BaseFragment
import com.fh.payday.R
import com.fh.payday.datasource.models.Card
import com.fh.payday.datasource.models.shared.ListModel
import com.fh.payday.utilities.DateTime
import com.fh.payday.utilities.getDecimalValue
import com.fh.payday.utilities.maskCardNumber
import com.fh.payday.views.shared.ListAdapter
import com.fh.payday.views2.transactionhistory.paydaycard.PaydayCardActivity

class CardDetailsFragment : BaseFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewOverDraft: RecyclerView
    private lateinit var tvOverdraft: TextView
    private lateinit var tvCustomerName:TextView
    private lateinit var tvCardNumber:TextView
    private lateinit var tvAvailableBalance:TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_card_details, container, false)
        init(view)
        getViewModel()?.summary?.observe(this, Observer {
            if (it?.cards?.isEmpty() ?: return@Observer) return@Observer

            val card = it.cards[0]
            val items = getCardInfo(card)
            val overdraftItems = getOverdraftItems(card)

            tvCardNumber.text = maskCardNumber(card.cardNumber, "XXXX XXXX XXXX ####")
            tvCustomerName.text = card.cardName
            tvAvailableBalance.text = String.format(getString(R.string.amount_in_aed), card.availableBalance.getDecimalValue())

            recyclerView.adapter = ListAdapter(items, context, getString(R.string.card_details))
            recyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

            recyclerViewOverDraft.adapter = ListAdapter(overdraftItems, context, getString(R.string.overdraft))
            recyclerViewOverDraft.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            tvOverdraft.setOnClickListener { setOverdraftRecyclerView() }
        })

        return view
    }

    private fun getOverdraftItems(card: Card): List<ListModel> {

        setOverdraftRecyclerView()
        val items = ArrayList<ListModel>()

        items.add(ListModel("Last Statement Outstanding Balance", "-"))
        items.add(ListModel("Last Statement date", "-"))
        items.add(ListModel("Minimum Payment Due amount", "-"))
        items.add(ListModel("Payment Due on date", "-"))

        return items
    }

    private fun getViewModel() = when (activity) {
        is AccountSummaryActivity -> { (activity as AccountSummaryActivity).viewModel }
        is PaydayCardActivity -> { (activity as PaydayCardActivity).viewModel }
        else -> {
            null
        }
    }

    private fun init(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerViewOverDraft = view.findViewById(R.id.recycler_view_overdraft)
        tvOverdraft = view.findViewById(R.id.tv_overdraft)
        tvCustomerName = view.findViewById(R.id.tv_customer_name)
        tvCardNumber = view.findViewById(R.id.tv_card_no)
        tvAvailableBalance = view.findViewById(R.id.tv_available_balance)
    }

    private fun getCardInfo(card: Card): List<ListModel> {
        val keys = resources.getStringArray(R.array.payday_card_key)

        val list = ArrayList<ListModel>()
//        list.add(ListModel(keys[0], card.cardName))
//        list.add(ListModel(keys[1], maskCardNumber(card.cardNumber, "XXXX XXXX XXXX ####")))
        list.add(ListModel(keys[2], card.cardType))
        list.add(ListModel(keys[3], card.cardStatus ?: ""))
//        list.add(ListModel(keys[4], String.format(getString(R.string.amount_in_aed), card.availableBalance)))
//        list.add(ListModel(keys[5], (if (card.totalOverdraftLimit != null) card.totalOverdraftLimit else "-")!!))
//        list.add(ListModel(keys[6], (if (card.overdraftLimit != null) card.overdraftLimit else "-")!!))
//        list.add(ListModel(keys[7], (if (card.amountOnHold != null) card.amountOnHold else "-")!!))
        list.add(ListModel(keys[8], String.format(getString(R.string.amount_in_aed), card.lastSalaryCredit?.getDecimalValue() ?: "0.00")))
        list.add(ListModel(keys[9], if (card.lastSalaryCreditDate != null) DateTime.parse(card.lastSalaryCreditDate) else "-"))
        return list
    }

    private fun setOverdraftRecyclerView() {
        if (recyclerViewOverDraft.visibility == View.GONE) {
           // recyclerViewOverDraft.visibility = View.VISIBLE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                tvOverdraft.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_down_arrow, 0)
            } else {
                tvOverdraft.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_down_arrow, 0)
            }
        } else {
            recyclerViewOverDraft.visibility = View.GONE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                tvOverdraft.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_right_fill, 0)
            } else {
                tvOverdraft.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_right_fill, 0)

            }
        }
    }
}




