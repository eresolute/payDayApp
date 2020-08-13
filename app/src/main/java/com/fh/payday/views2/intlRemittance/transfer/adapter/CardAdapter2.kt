package com.fh.payday.views2.intlRemittance.transfer.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.fh.payday.R
import com.fh.payday.datasource.models.Card
import com.fh.payday.utilities.OnItemClickListener
import com.fh.payday.utilities.getDecimalValue
import com.fh.payday.utilities.maskCardNumber
import kotlinx.android.synthetic.main.item_cards.view.*

class CardsAdapter2(val listener: OnItemClickListener) : RecyclerView.Adapter<CardsAdapter2.ViewHolder>() {

    private val cards: ArrayList<Card> = arrayListOf()
    private val selectedCardPosition = 0

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.imt_card_item, p0, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = cards.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(cards[position])
        holder.layout.setOnClickListener { listener.onItemClick(position) }
        /*holder.setSelectedCard(selectedCardPosition == position)*/
    }

    fun addAll(list: ArrayList<Card>) {
        cards.clear()
        cards.addAll(list)
        notifyDataSetChanged()
    }

    fun getSelectedCard(): Card = cards[selectedCardPosition]

    class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val layout: LinearLayout = view.findViewById(R.id.card_layout)
        fun bindTo(card: Card) {
            view.tv_card_name.text = card.cardName
            view.tv_card_number.text = maskCardNumber(card.cardNumber)
            view.tv_card_balance.text = String.format(view.context.getString(R.string.amount_in_aed,
                card.availableBalance.getDecimalValue()))
        }

        /*fun setSelectedCard(isSelected: Boolean) {
            if (isSelected) {
                view.root_view.setBackgroundResource(R.drawable.bg_blue_border_light)
            } else {
                view.root_view.setBackgroundResource(android.R.color.transparent)
            }
        }*/
    }
}