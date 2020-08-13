package com.fh.payday.views2.intlRemittance.tracktransaction

import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.datasource.models.intlRemittance.IntlTransaction
import com.fh.payday.utilities.DateTime
import com.fh.payday.utilities.getDecimalValue

class TrackTransactionAdapter(
        private val items: List<IntlTransaction> = ArrayList(),
        private val listener: OnTransactionItemClickListener,
        private val mListener: TrackTransactionFragment.OnBankTransferClickListener
) : RecyclerView.Adapter<TrackTransactionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.track_transaction_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], View.OnClickListener { listener.onTransactionClick(items[position]) },
                View.OnClickListener { mListener.onBankTransferClick(items[position]) })
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvBeneficiaryName = view.findViewById<TextView>(R.id.tv_beneficiary_name)
        private val tvDate = view.findViewById<TextView>(R.id.tv_date)
        private val tvAmount = view.findViewById<TextView>(R.id.tv_trans_amount)
        private val tvAddress = view.findViewById<TextView>(R.id.tv_address)
        private val tvStatus = view.findViewById<TextView>(R.id.tv_status)
        private val ivView = view.findViewById<ImageView>(R.id.iv_view)
        val layout: ConstraintLayout = view.findViewById(R.id.root_view)

        fun bind(item: IntlTransaction, listener: View.OnClickListener, onClickListener: View.OnClickListener) {
            tvBeneficiaryName.text = item.beneficiaryName
            tvDate.text = DateTime.parse(item.tranDate, "yyyy-MM-dd'T'HH:mm:ss'Z'", "dd/MM/yyyy hh:mm a")
            tvAmount.apply {
                text = String.format(context.getString(R.string.amount_in_aed),
                        item.amountInAed.getDecimalValue())
            }
            tvAddress.text = item.dealerTxnId

            //tvStatus.text = item.status
            if (item.status.equals("SUCCESS", ignoreCase = true)) {
                tvStatus.apply {
                    text = getColoredSpan(context.getString(R.string.status_colon),
                                     item.status, ContextCompat.getColor(context, R.color.credit_color))
                }
            } else {
                tvStatus.apply {
                    text = getColoredSpan(context.getString(R.string.status_colon),
                                    item.status,
                                    ContextCompat.getColor(context, R.color.debit_color))
                }
            }
            layout.setOnClickListener(onClickListener)
            ivView.setOnClickListener(listener)
        }

        private fun getColoredSpan(title: String, status: String, color: Int): SpannableString {
            val finalText = "$title $status"
            val coloredSpan = SpannableString(finalText)
            coloredSpan.setSpan(ForegroundColorSpan(color), title.length, finalText.length,
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            return coloredSpan
        }
    }
}