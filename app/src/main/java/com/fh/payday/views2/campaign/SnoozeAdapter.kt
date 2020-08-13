package com.fh.payday.views2.campaign;

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.fh.payday.R

class SnoozeAdapter(
        private val items: ArrayList<String>,
        private val block: (String) -> Unit) : RecyclerView.Adapter<SnoozeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_snooze, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bindTo(item, View.OnClickListener { block(item) })
    }

    override fun getItemCount() = items.size

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private var tvDay = view.findViewById<TextView>(R.id.tv_day)
        fun bindTo(item: String, listener: View.OnClickListener) {
            tvDay.text = item
            tvDay.setOnClickListener(listener)
        }
    }

}