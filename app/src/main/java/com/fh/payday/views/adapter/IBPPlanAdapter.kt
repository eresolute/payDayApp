package com.fh.payday.views.adapter

import android.annotation.SuppressLint
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView


import com.fh.payday.R
import com.fh.payday.databinding.IbpPlanItemBinding
import com.fh.payday.datasource.models.Plan
import com.fh.payday.utilities.OnItemClickListener

class IBPPlanAdapter(
        private val listener: OnItemClickListener,
        private val plans: List<Plan>,
        private var selectedCard: Int
) : RecyclerView.Adapter<IBPPlanAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, i: Int): ViewHolder {

        val binding = DataBindingUtil.inflate<IbpPlanItemBinding>(
                LayoutInflater.from(parent.context),
                R.layout.ibp_plan_item,
                parent,
                false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.bindTo(plans[position])
        holder.rootView.setOnClickListener {
            listener.onItemClick(position)
            selectedCard = position
            notifyDataSetChanged()
        }
        if (selectedCard == position) {
            holder.rootView.background = ContextCompat.getDrawable(holder.rootView.context,
                    R.drawable.bg_blue_gradient)
            holder.tvCurrency.setTextColor(Color.WHITE)
            holder.tvAmount.setTextColor(Color.WHITE)
            holder.tvPlanDescription.setTextColor(Color.WHITE)
        } else {
            holder.rootView.background = ContextCompat.getDrawable(holder.rootView.context,
                    R.drawable.bg_blue_grey)
            holder.tvCurrency.setTextColor(Color.GRAY)
            holder.tvPlanDescription.setTextColor(Color.GRAY)
            holder.tvAmount.setTextColor(Color.BLACK)
        }
    }

    override fun getItemCount(): Int {
        return plans.size
    }

    class ViewHolder (
            private val binding: IbpPlanItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        val rootView: LinearLayout = binding.root.findViewById(R.id.linear_layout)
        val tvCurrency: TextView = binding.root.findViewById(R.id.tv_currency)
        val tvAmount: TextView = binding.root.findViewById(R.id.tv_amount)
        val tvPlanDescription: TextView = binding.root.findViewById(R.id.tv_plan_description)

        fun bindTo(plan: Plan) {
            binding.plan = plan
        }
    }
}
