package com.fh.payday.views2.intlRemittance.transactionhistory

import android.os.Build
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.datasource.models.intlRemittance.IntlTransaction
import com.fh.payday.utilities.DateTime
import com.fh.payday.utilities.getDecimalValue


class TransactionHistoryAdapter(
        private val items: List<IntlTransaction> = ArrayList(),
        private val listener: OnTransactionItemClickListener
) : RecyclerView.Adapter<TransactionHistoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.transaction_history_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], View.OnClickListener { listener.onTransactionClick(items[position]) })
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvBeneficiaryName = view.findViewById<TextView>(R.id.tv_beneficiary_name)
        private val tvDate = view.findViewById<TextView>(R.id.tv_date)
        private val tvINRAmount = view.findViewById<TextView>(R.id.tv_inr_amt)
        private val tvAddress = view.findViewById<TextView>(R.id.tv_address)
        private val tvAEDAmount = view.findViewById<TextView>(R.id.tv_aed_amt)
        private val ivOption = view.findViewById<ImageView>(R.id.iv_option)
        val layout: ConstraintLayout = view.findViewById(R.id.root_view)

        fun bind(item: IntlTransaction, listener: View.OnClickListener) {
            
            if (1 == item.favourite?.toInt()) {
                tvBeneficiaryName.text = item.beneficiaryName
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    tvBeneficiaryName.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_heart, 0)
                } else {
                    tvBeneficiaryName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_heart, 0)
                }
            }else{
                tvBeneficiaryName.text = item.beneficiaryName
                tvBeneficiaryName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            }

            tvDate.text = DateTime.parse(item.tranDate, "yyyy-MM-dd'T'HH:mm:ss'Z'", "dd/MM/yyyy hh:mm a")
            tvINRAmount.apply {
                text = String.format(context.getString(R.string.payout_amt), item.payOutCurrency,
                        item.payOutAmount.getDecimalValue())
            }

            tvAEDAmount.apply {
                text = String.format(context.getString(R.string.amount_in_aed),
                        item.amountInAed.getDecimalValue())
            }
            tvAddress.text = item.dealerTxnId
            ivOption.setOnClickListener(listener)
        }
    }
}