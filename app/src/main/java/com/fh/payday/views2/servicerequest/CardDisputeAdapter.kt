package com.fh.payday.views2.servicerequest

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.datasource.models.CardTransactionDispute
import com.fh.payday.utilities.DateTime
import com.fh.payday.utilities.getDecimalValue

class CardDisputeAdapter @JvmOverloads constructor(
        private val items: List<CardTransactionDispute>,
        private val action: (CardTransactionDispute) -> Unit = {}
) : RecyclerView.Adapter<CardDisputeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.transaction_history_detail_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bindTo(item, View.OnClickListener { action(item) })
    }

    override fun getItemCount() = items.size

    class ViewHolder(private val itemsView: View) : RecyclerView.ViewHolder(itemsView) {
        private val rootView: View = itemView.findViewById(R.id.root_view)
        private var tvType: TextView = itemsView.findViewById(R.id.tv_transaction_type)
        private var tvDate: TextView = itemsView.findViewById(R.id.tv_transaction_date)
        private var tvRefNumber: TextView = itemsView.findViewById(R.id.tv_ref_number)
        var tvAmount: TextView = itemsView.findViewById(R.id.tv_transaction_amount)
        private var tvDrCr: TextView = itemsView.findViewById(R.id.iv_arrow)

        fun bindTo(item: CardTransactionDispute, listener: View.OnClickListener) {
            tvType.text = item.TransactionDescription
            tvDate.text = DateTime.parse(item.TransactionDateTime, "yyyy-MM-dd'T'HH:mm:ss'Z'", "dd/MM/yyyy hh:mm a")
            tvAmount.text = String.format(itemsView.context.getString(R.string.amount_in_aed), item.TransactionAmount.getDecimalValue())

            tvRefNumber.text = "Ref#${item.TransactionReferenceNumber}"

            if (item.TransactionType == "D") {
                tvDrCr.text = "-"
                tvDrCr.setTextColor(ContextCompat.getColor(itemsView.context, R.color.debit_color))
                tvAmount.setTextColor(ContextCompat.getColor(itemsView.context, R.color.debit_color))
            } else if (item.TransactionType == "C") {
                tvDrCr.text = "+"
                tvDrCr.setTextColor(ContextCompat.getColor(itemsView.context, R.color.credit_color))
                tvAmount.setTextColor(ContextCompat.getColor(itemsView.context, R.color.credit_color))
            }

            rootView.setOnClickListener(listener)
        }
    }

}