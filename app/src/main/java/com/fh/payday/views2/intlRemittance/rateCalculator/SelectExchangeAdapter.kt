package com.fh.payday.views2.intlRemittance.rateCalculator

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.Glide
import com.fh.payday.R
import com.fh.payday.datasource.models.Item
import com.fh.payday.utilities.OnItemClickListener

class SelectExchangeAdapter(val item: List<Item>, val listener: OnItemClickListener)
    : RecyclerView.Adapter<SelectExchangeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.layout_exchange, p0,
                false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = item.size

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.bindTo(item[p1])
        p0.layout.setOnClickListener { listener.onItemClick(p1) }
    }

    class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val layout: ConstraintLayout = view.findViewById(R.id.layout)
        fun bindTo(item: Item) {
            view.findViewById<TextView>(R.id.textView50).text = item.name
            //view.findViewById<ImageView>(R.id.imageView11).setImageURI(item.res)
            Glide.with(view.context)
                    .load(item.res)
                    .into(view.findViewById(R.id.imageView11))
        }
    }
}