package com.fh.payday.views2.transactionhistory.billpayment

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.datasource.models.transactionhistory.BillPaymentTransaction
import com.fh.payday.utilities.DateTime
import com.fh.payday.utilities.getDecimalValue

class BillTransactionAdapter @JvmOverloads constructor (
    private val list: List<BillPaymentTransaction>,
    private val showAccountNo: Boolean = false,
    private val isMoneyTransfer: Boolean = false
) : RecyclerView.Adapter<BillTransactionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.transaction_history_detail_item, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        if (list[i].amountInAed != "")
        holder.tvAmount.text = String.format(holder.context.getString(R.string.amount_in_aed), list[i].amountInAed.getDecimalValue())

        if (isMoneyTransfer) {
            holder.tvDate.text = DateTime.parse(list[i].tranDate, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "dd/MM/yyyy hh:mm a")
        } else {
            holder.tvDate.text = DateTime.parse(list[i].tranDate, "yyyy-MM-dd'T'HH:mm:ss'Z'", "dd/MM/yyyy hh:mm a")
        }
        holder.tvType.text = list[i].serviceProvider
        if (showAccountNo) {
            holder.tvAccountNo.text = list[i].accountNo
            holder.tvAccountNo.visibility = View.VISIBLE
        }
        holder.tvAmount.setTextColor(ContextCompat.getColor(holder.context, R.color.debit_color))

    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val context: Context = itemView.context
        val tvType: TextView = itemView.findViewById(R.id.tv_transaction_type)
        val tvDate: TextView = itemView.findViewById(R.id.tv_transaction_date)
        val tvAmount: TextView = itemView.findViewById(R.id.tv_transaction_amount)
        val tvAccountNo: TextView = itemView.findViewById(R.id.tv_account_no)
    }
}