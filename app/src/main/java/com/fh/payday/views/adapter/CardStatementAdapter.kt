package com.fh.payday.views.adapter

import android.databinding.DataBindingUtil
import android.support.annotation.NonNull
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fh.payday.R
import com.fh.payday.databinding.CardStatementItemBinding
import com.fh.payday.datasource.models.CardStatement
import com.fh.payday.utilities.OnItemClickListener

/**
 * PayDayFH
 * Created by EResolute on 10/26/2018.
 */
class CardStatementAdapter(@NonNull val cardStatements: List<CardStatement>, val listener: OnItemClickListener) : RecyclerView.Adapter<CardStatementAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, item: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_statement_item, parent, false)

        val binding = DataBindingUtil.inflate<CardStatementItemBinding>(
                LayoutInflater.from(parent.context),
                R.layout.card_statement_item,
                parent,
                false
        );
        return ViewHolder(binding)
    }

    override fun getItemCount() = cardStatements.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(View.OnClickListener { listener.onItemClick(position) },cardStatements.get(position))
    }

    class ViewHolder(private val binding: CardStatementItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindTo(listener: View.OnClickListener, cardStatement: CardStatement) {
            binding.clickListener = listener
            binding.cardStatement = cardStatement
        }
    }
}