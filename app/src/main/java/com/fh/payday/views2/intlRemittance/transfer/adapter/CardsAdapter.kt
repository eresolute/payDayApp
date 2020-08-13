package com.fh.payday.views2.intlRemittance.transfer.adapter

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.datasource.models.Card
import com.fh.payday.datasource.models.intlRemittance.CardInfo
import com.fh.payday.utilities.getDecimalValue
import com.fh.payday.utilities.maskCardNumber
import com.fh.payday.views2.intlRemittance.transfer.TransferActivity

class CardsAdapter(
    private val cards: ArrayList<CardInfo>,
    private val listener: TransferActivity.OnCardClickListener
) : RecyclerView.Adapter<CardsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.item_cards, p0, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = cards.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(cards[position].card, cards[position].totalPayableAmount,
            cards[position].balAfterTransfer)
        holder.layout.setOnClickListener { listener.onCardClick(position, cards[position].card) }
        holder.imgToggle.setOnClickListener {
//            if (holder.tvTransferLabel.visibility == View.VISIBLE) {
//                holder.imgToggle.setImageResource(R.drawable.ic_arrow_down_grey)
//                toggleVisibility(View.GONE, holder)
//            } else {
//                holder.imgToggle.setImageResource(R.drawable.ic_arrow_right_grey)
//                toggleVisibility(View.VISIBLE, holder)
//            }
            listener.onCardClick(position, cards[position].card)
        }
    }

    private fun toggleVisibility(visible: Int, p0: ViewHolder) {
        /*p0.tvCom.visibility = visible
        p0.tvVat.visibility = visible
        p0.tvVatLabel.visibility = visible
        p0.tvComLabel.visibility = visible*/
        p0.tvTransferLabel.visibility = visible
        p0.tvBalanceLabel.visibility = visible
        p0.tvTransferAmount.visibility = visible
        p0.tvBalance.visibility = visible
    }

    fun add(list: List<CardInfo>) {
        cards.clear()
        cards.addAll(list)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layout: ConstraintLayout = itemView.findViewById(R.id.root_view)
        val imgToggle: ImageView = itemView.findViewById(R.id.img_toggle)
        /*val tvCom = itemView.findViewById<TextView>(R.id.tv_commission)
        val tvVat = itemView.findViewById<TextView>(R.id.tv_vat)
        val tvVatLabel = itemView.findViewById<TextView>(R.id.tv_vat_label)
        val tvComLabel = itemView.findViewById<TextView>(R.id.tv_comission_label)*/
        val tvTransferLabel: TextView = itemView.findViewById(R.id.textView49)
        val tvBalanceLabel: TextView = itemView.findViewById(R.id.textView59)
        val tvTransferAmount: TextView = itemView.findViewById(R.id.tv_transfer_amount)
        val tvBalance: TextView = itemView.findViewById(R.id.tv_balance_amount)

        fun bindTo(card: Card, totalPayableAmount: String, balAfterTransfer: String) {
            itemView.findViewById<TextView>(R.id.tv_card_name).text = card.cardName
            itemView.findViewById<TextView>(R.id.tv_card_number).text = maskCardNumber(card.cardNumber, "xxxx xxxx xxxx ####")
            itemView.findViewById<TextView>(R.id.tv_card_balance).text = String.format(itemView.context.getString(R.string.amount_in_aed,
                    card.availableBalance.getDecimalValue()))
            itemView.findViewById<TextView>(R.id.tv_transfer_amount).text = String.format(itemView.context.getString(R.string.amount_in_aed,
                    totalPayableAmount.getDecimalValue()))
            itemView.findViewById<TextView>(R.id.tv_balance_amount).text = String.format(itemView.context.getString(R.string.amount_in_aed,
                    balAfterTransfer.getDecimalValue()))

            /*itemView.findViewById<TextView>(R.id.tv_commission).text = String.format(itemView.context.getString(R.string.amount_in_aed,
                    comission.getDecimalValue()))
            itemView.findViewById<TextView>(R.id.tv_vat).text = String.format(itemView.context.getString(R.string.amount_in_aed,
                    vat.getDecimalValue()))*/

        }
    }
}