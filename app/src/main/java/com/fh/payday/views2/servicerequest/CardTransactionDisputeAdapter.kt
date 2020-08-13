package com.fh.payday.views2.servicerequest;

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.datasource.models.transactionhistory.Transactions
import com.fh.payday.utilities.DateTime
import com.fh.payday.utilities.getDecimalValue

class CardTransactionDisputeAdapter(private val items: List<Transactions>) : RecyclerView.Adapter<CardTransactionDisputeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card_transaction_dispute, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bindTo(item)
    }

    override fun getItemCount() = items.size

    class ViewHolder(private val itemsView: View) : RecyclerView.ViewHolder(itemsView) {
        private var tvType: TextView = itemsView.findViewById(R.id.tv_transaction_type)
        private var tvDate: TextView = itemsView.findViewById(R.id.tv_transaction_date)
        var tvAmount: TextView = itemsView.findViewById(R.id.tv_transaction_amount)

        fun bindTo(item: Transactions) {
            tvType.text = item.TransactionDescription
            tvDate.text = DateTime.parse(item.TransactionDateTime)
            tvAmount.text = String.format(itemsView.context.getString(R.string.amount_in_aed), item.TransactionAmount.getDecimalValue())
        }
    }

}