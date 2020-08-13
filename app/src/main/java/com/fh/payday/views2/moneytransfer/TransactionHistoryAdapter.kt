package com.fh.payday.views2.moneytransfer

import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.datasource.models.Transactions
import com.fh.payday.utilities.DateTime
import com.fh.payday.utilities.getDecimalValue
import com.fh.payday.views2.transactionhistory.TransactionHistoryDetailFragment

class TransactionHistoryAdapter(
        private val items: ArrayList<Transactions> = ArrayList(),
        private val listener: TransactionHistoryDetailFragment.OnTransactionClickListener = TransactionHistoryDetailFragment.OnTransactionClickListener { }
) : RecyclerView.Adapter<TransactionHistoryAdapter.ViewHolder>() {
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.transfer_transaction_history_item, p0, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
        if (items[position].TransactionType == "D") {
            holder.ivCreDeb.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.debit_color))
            holder.ivCreDeb.text = "-"
        } else if (items[position].TransactionType == "C") {
            holder.ivCreDeb.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.credit_color))
            holder.ivCreDeb.text = "+"
        }


        holder.layout.setOnClickListener { listener.onTransactionClick(items[position]) }
    }

    fun add(items: List<Transactions>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val ivCreDeb: TextView = view.findViewById(R.id.iv_credit_debit)
        private val tvBeneficiaryName = view.findViewById<TextView>(R.id.tv_beneficiary_name)
        private val tvDate = view.findViewById<TextView>(R.id.tv_date)
        private val tvAmount = view.findViewById<TextView>(R.id.tv_trans_amount)
        private val tvReference = view.findViewById<TextView>(R.id.textView29)
        val layout: ConstraintLayout = view.findViewById(R.id.root_view)

        fun bind(item: Transactions) {
            tvBeneficiaryName.text = item.TransactionDescription
            tvDate.text = DateTime.parse(item.TransactionDateTime, "yyyy-MM-dd'T'HH:mm:ss'Z'", "dd/MM/yyyy hh:mm a")
            tvAmount.apply {
                text = String.format(context.getString(R.string.amount_in_aed),
                        item.TransactionAmount.getDecimalValue())
                setTextColor(ContextCompat.getColor(context, R.color.debit_color))
            }
            if (item.TransactionType == "D") {
                ivCreDeb.text = "-"
                ivCreDeb.setTextColor(ContextCompat.getColor(itemView.context, R.color.debit_color))
                tvAmount.setTextColor(ContextCompat.getColor(itemView.context, R.color.debit_color))

            } else if (item.TransactionType == "C") {
                ivCreDeb.text = "+"
                ivCreDeb.setTextColor(ContextCompat.getColor(itemView.context, R.color.credit_color))
                tvAmount.setTextColor(ContextCompat.getColor(itemView.context, R.color.credit_color))
            }
            tvReference.text = "Ref#${item.TransactionReferenceNumber}"

        }

    }
}