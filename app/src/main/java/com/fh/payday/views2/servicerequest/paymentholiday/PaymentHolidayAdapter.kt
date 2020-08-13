package com.fh.payday.views2.servicerequest.paymentholiday;

import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.datasource.models.PaymentHoliday
import com.fh.payday.utilities.OnItemClickListener
import com.fh.payday.utilities.OnLoanClickListener

class PaymentHolidayAdapter(private val items: List<PaymentHoliday>, private val listener : OnLoanClickListener) : RecyclerView.Adapter<PaymentHolidayAdapter.ViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.payment_holiday_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bindTo(item)

        item.loanNumber ?: return
        holder.clItem.setOnClickListener{listener.onLoanClick(item.loanNumber)}
    }

    override fun getItemCount() = items.size

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val tvLoan = view.findViewById<TextView>(R.id.tv_loan_title)
        private val tvLoanEmi = view.findViewById<TextView>(R.id.tv_loan_emi)
        private val tvLoanDue = view.findViewById<TextView>(R.id.tv_loan_due)
        val clItem = view.findViewById<ConstraintLayout>(R.id.cl_item)

        fun bindTo(item: PaymentHoliday) {
            tvLoan.text = item.title
            tvLoanEmi.text= (getColoredSpan(view.context.getString(R.string.emi),view.context.getString(R.string.aed),
                    item.emi, ContextCompat.getColor(view.context, R.color.grey_400)))
            tvLoanDue.text = (getColoredSpan(view.context.getString(R.string.due),view.context.getString(R.string.aed),
                    item.due, ContextCompat.getColor(view.context, R.color.grey_400)))
        }
        private fun getColoredSpan(title: String, aed: String, amount: Float, color: Int): SpannableString {
            val finalText = "$title  $aed  $amount"

            val coloredSpan = SpannableString(finalText)
            coloredSpan.setSpan(ForegroundColorSpan(color), 0, title.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)

            return coloredSpan
        }
    }



}