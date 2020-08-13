package com.fh.payday.views2.locator;

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.datasource.models.SMSBanking

class SMSBankingAdapter(private val items: List<SMSBanking>) : RecyclerView.Adapter<SMSBankingAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sms_banking, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bindTo(item)
    }

    override fun getItemCount() = items.size

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val tvShortCode = view.findViewById<TextView>(R.id.tv_short_code)
        private val tvDescription = view.findViewById<TextView>(R.id.tv_description)

        fun bindTo(item: SMSBanking) {
            tvShortCode.text = item.code
            tvDescription.text = item.description
        }
    }

}