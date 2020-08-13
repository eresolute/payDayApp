package com.fh.payday.views2.intlRemittance.transfer.adapter

import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fh.payday.R
import com.fh.payday.utilities.isValidPromoCode
import com.fh.payday.utilities.onTextChanged
import kotlinx.android.synthetic.main.imt_summary_item.view.*

class SummaryAdapter(
    private val promoCodeChangeListener: (String) -> Unit
) : RecyclerView.Adapter<SummaryAdapter.ViewHolder>() {

    private val items = arrayListOf<SummaryUI>()

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.imt_summary_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (items[position].isEditable) {
            holder.bindEditableView(items[position], promoCodeChangeListener)
        } else {
            holder.bind(items[position])
        }
    }

    fun addAll(list: List<SummaryUI>) {
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun add(index: Int, summary: SummaryUI) {
        items.add(index, summary)
        notifyDataSetChanged()
    }

    fun clear() {
        items.clear()
        notifyDataSetChanged()
    }

    class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(summary: SummaryUI) {
            view.tv_label.text = summary.label

            view.tv_title1.apply {
                text = summary.title
                visibility = View.VISIBLE
            }

            view.tv_heading1.apply {
                text = summary.heading1
                visibility = if (!summary.heading1.isNullOrEmpty()) View.VISIBLE else View.GONE
            }

            view.tv_heading2.apply {
                text = summary.heading2
                visibility = if (!summary.heading2.isNullOrEmpty()) View.VISIBLE else View.GONE
            }

            view.tv_status.apply {
                summary.status ?: return@apply
                text = summary.status
                view.tv_label.text = summary.label
                //setTextColor(ContextCompat.getColor(view.context, R.color.grey_500))
                visibility = View.VISIBLE
                view.edit_text.visibility = View.GONE
                view.tv_title1.visibility = View.GONE
                view.tv_heading1.visibility = View.GONE
                view.tv_heading2.visibility = View.GONE
                view.divider.visibility = View.GONE
            }

            view.tv_exchange.apply {
                summary.exchange ?: return@apply
                text = summary.exchange
                view.tv_label.text = summary.label
                visibility = View.VISIBLE
                view.img_exchange.setImageResource(summary.exchangeLogo)
                view.img_exchange.visibility = View.VISIBLE
                view.edit_text.visibility = View.GONE
                view.tv_title1.visibility = View.GONE
                view.tv_heading1.visibility = View.GONE
                view.tv_heading2.visibility = View.GONE
                view.divider.visibility = View.GONE
            }

            view.divider.visibility = View.GONE
            view.edit_text.visibility = View.GONE
        }

        fun bindEditableView(summary: SummaryUI, listener: (String) -> Unit) {
            view.tv_label.text = summary.label
            view.edit_text.visibility = View.VISIBLE

            view.tv_title1.visibility = View.GONE
            view.tv_heading1.visibility = View.GONE
            view.tv_heading2.visibility = View.GONE
            view.divider.visibility = View.GONE

            view.edit_text.onTextChanged { s, _, _, _ ->
                listener(s.toString())
                if (isValidPromoCode(s.toString())) {
                    view.edit_text.setTextColor(ContextCompat.getColor(view.context, R.color.verified))
                    setDrawable(R.drawable.ic_success_tick)
                } else if (!s.isNullOrEmpty()) {
                    view.edit_text.setTextColor(ContextCompat.getColor(view.context, R.color.colorError))
                    setDrawable(R.drawable.ic_error_cross)
                } else {
                    setDrawable(null)
                }
            }
        }

        private fun setDrawable(resource: Int?) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                view.edit_text.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0,
                    resource ?: 0, 0)
            } else {
                view.edit_text.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                    resource ?: 0, 0)
            }
        }

    }
}