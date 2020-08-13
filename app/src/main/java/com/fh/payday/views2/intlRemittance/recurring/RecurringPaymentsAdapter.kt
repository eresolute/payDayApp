package com.fh.payday.views2.intlRemittance.recurring

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.datasource.models.intlRemittance.RecurringPayment
import com.fh.payday.utilities.OnItemClickListener

class RecurringPaymentsAdapter(
        private val paymentList: List<RecurringPayment>,
        private val paymentHistoryList: List<RecurringPayment>,
        private val listener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val VIEW_TYPE_PAYMENT = 1
    private val VIEW_TYPE_HEADER = 2
    private val VIEW_TYPE_HISTORY = 3
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutItem = LayoutInflater.from(parent.context).inflate(R.layout.registered_beneficiaries_item, parent, false)
        val layoutHeader = LayoutInflater.from(parent.context).inflate(R.layout.item_header, parent, false)

        return when (viewType) {
            VIEW_TYPE_PAYMENT -> ViewHolderPayments(layoutItem)
            VIEW_TYPE_HEADER -> ViewHolderHeader(layoutHeader)
            VIEW_TYPE_HISTORY -> ViewHolderHistory(layoutItem)
            else -> ViewHolderHistory(layoutItem)
        }
    }

    override fun getItemCount(): Int = paymentHistoryList.size + paymentList.size + 1

    override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
        when (p0) {
            is ViewHolderHeader -> p0.bindHeader("Transaction History")
            is ViewHolderPayments -> {
                p0.bindPayment(paymentList[p1], View.OnClickListener { listener.onItemClick(p1) })
            }
            is ViewHolderHistory -> p0.bindHistory(paymentHistoryList[p1 - paymentList.size - 1], View.OnClickListener { listener.onItemClick(p1) })
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position < paymentList.size) return VIEW_TYPE_PAYMENT
        if (position == paymentList.size) return VIEW_TYPE_HEADER
        if (position - paymentList.size - 1 < paymentHistoryList.size) return VIEW_TYPE_HISTORY

        return -1
    }

    class ViewHolderHistory(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.tv_user_name)
        val accoutNo = itemView.findViewById<TextView>(R.id.account_no)
        val options = itemView.findViewById<ImageView>(R.id.img_option)
        fun bindHistory(item: RecurringPayment, listener: View.OnClickListener) {
            name.text = item.name
            accoutNo.text = item.bankName
            options.setOnClickListener(listener)
        }
    }

    class ViewHolderPayments(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.tv_user_name)
        val accoutNo = itemView.findViewById<TextView>(R.id.account_no)
        val options = itemView.findViewById<ImageView>(R.id.img_option)
        fun bindPayment(item: RecurringPayment, listener: View.OnClickListener) {
            name.text = item.name
            accoutNo.text = item.bankName
            options.setOnClickListener(listener)
        }
    }

    class ViewHolderHeader(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val header = itemView.findViewById<TextView>(R.id.tvHeader)
        fun bindHeader(string: String) {
            header.text = string
        }
    }
}