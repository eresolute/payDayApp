package com.fh.payday.views2.intlRemittance.transfer.adapter

import android.annotation.SuppressLint
import android.net.Uri
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.fh.payday.R
import com.fh.payday.datasource.models.intlRemittance.IntlBeneficiary
import com.fh.payday.utilities.BASE_URL
import com.fh.payday.utilities.isEllipsized
import com.fh.payday.views.custombindings.setMaxLinesToggleListener
import com.fh.payday.views2.intlRemittance.ExchangeAccessKey
import com.fh.payday.views2.intlRemittance.transfer.TransferActivity

class BeneficiaryAdapter(val list: List<IntlBeneficiary>, val listener: TransferActivity.OnIntlBeneficiaryClickListener)
    : RecyclerView.Adapter<BeneficiaryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.item_intl_beneficiary, p0, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.layout.isEnabled = true
        val clickListener = View.OnClickListener {
            p0.layout.isEnabled = false
            listener.onIntlBeneficiaryClick(p1, list[p1])
        }
        p0.bindTo(list[p1], clickListener)
        p0.layout.setOnClickListener(clickListener)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val layout: ConstraintLayout = itemView.findViewById(R.id.root_view)

        @SuppressLint("SetTextI18n")
        fun bindTo(beneficiary: IntlBeneficiary, clickListener: View.OnClickListener) {

            itemView.findViewById<TextView>(R.id.tv_beneficiary_name).apply {
                text = "${beneficiary.receiverFirstName} ${beneficiary.receiverLastName}"
                setMaxLinesToggleListener(this, maxLines, context.resources.getInteger(R.integer.imt_beneficiary_collapsed_lines))
                viewTreeObserver.addOnGlobalLayoutListener {
                    if (!isEllipsized(layout)) {
                        setOnClickListener(clickListener)
                    }
                }
            }
            itemView.findViewById<TextView>(R.id.account_no).text = beneficiary.receiverBankAccountNo
            checkAccessKey(beneficiary.accessKey)
            itemView.findViewById<TextView>(R.id.tv_bank_name).apply {
                text = beneficiary.receiverBankName
                setMaxLinesToggleListener(this, maxLines, context.resources.getInteger(R.integer.imt_beneficiary_collapsed_lines))
                viewTreeObserver.addOnGlobalLayoutListener {
                    if (!isEllipsized(layout)) {
                        setOnClickListener(clickListener)
                    }
                }
            }
            checkFavourite(beneficiary.favourite)
            itemView.findViewById<TextView>(R.id.tv_currency).text = " - ${beneficiary.payoutCcyCode}"
            val image = itemView.findViewById<ImageView>(R.id.iv_user)
            Glide.with(itemView.context)
                .load(Uri.parse("$BASE_URL/${beneficiary.flagPath}"))
                .into(image)
        }

        private fun checkAccessKey(accessKey: String) {
            if (accessKey.equals(ExchangeAccessKey.FARD, true)) {
                itemView.findViewById<ImageView>(R.id.img_exchange_logo).setImageResource(R.mipmap.af_logo)
            }
            else {
                itemView.findViewById<ImageView>(R.id.img_exchange_logo).setImageResource(R.mipmap.uae_x)
            }
        }

        private fun checkFavourite(favourite: String) {

            if(favourite == "1") {
                itemView.findViewById<ImageView>(R.id.iv_favourites).visibility = View.VISIBLE
            }
            else{
                itemView.findViewById<ImageView>(R.id.iv_favourites).visibility = View.INVISIBLE
            }
        }
    }
}