package com.fh.payday.views2.shared.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.datasource.models.payments.Operator
import com.fh.payday.utilities.GlideApp
import com.fh.payday.utilities.OnItemClickListener

class SelectOperatorAdapter(
    private val listener: OnItemClickListener,
    private val operators: ArrayList<Operator> = ArrayList()
) : RecyclerView.Adapter<SelectOperatorAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.select_operator_item,
            parent,
            false
        )

        return ViewHolder(view)
    }

    override fun getItemCount() = operators.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(operators[position], View.OnClickListener { listener.onItemClick(position) })
    }

    fun addAll(list: List<Operator>) {
        operators.addAll(list)
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val linearLayout = view.findViewById<View>(R.id.linear_layout)
        private val tvName = view.findViewById<TextView>(R.id.tv_name)
        private val imageView = view.findViewById<ImageView>(R.id.image_view)

        fun bind(operator: Operator, listener: View.OnClickListener) {
            tvName.text = operator.serviceProvider
            operator.serviceImage?.let {
                GlideApp.with(imageView)
                    .load(it)
                    .error(R.drawable.ic_operator)
                    .placeholder(R.drawable.ic_operator)
                    .into(imageView)
            }

            linearLayout.setOnClickListener(listener)
        }
    }

}