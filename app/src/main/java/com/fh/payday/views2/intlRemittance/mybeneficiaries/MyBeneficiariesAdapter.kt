package com.fh.payday.views2.intlRemittance.mybeneficiaries

import android.annotation.SuppressLint
import android.net.Uri
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
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

class MyBeneficiariesAdapter(
        val list: List<IntlBeneficiary>, val listener: TransferActivity.OnIntlBeneficiaryClickListener,
       private val popUpAction: (IntlBeneficiary, View) -> Unit
) : RecyclerView.Adapter<MyBeneficiariesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mybeneficiaries_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(list[position], View.OnClickListener { listener.onIntlBeneficiaryClick(position, list[position]) }, popUpAction)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val layout: ConstraintLayout = itemView.findViewById(R.id.root_view)
        private val ivOption: ImageView = itemView.findViewById(R.id.iv_option)

        @SuppressLint("SetTextI18n")
        fun bindTo(beneficiary: IntlBeneficiary, clickListener: View.OnClickListener, popUpAction: (IntlBeneficiary, View) -> Unit) {
            layout.setOnClickListener(clickListener)
            ivOption.setOnClickListener { popUpAction(beneficiary, it) }

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
            itemView.findViewById<TextView>(R.id.tv_currency).text = " - ${beneficiary.payoutCcyCode}"
            itemView.findViewById<TextView>(R.id.tv_bank_name).apply {
                text = beneficiary.receiverBankName
                setMaxLinesToggleListener(this, maxLines, context.resources.getInteger(R.integer.imt_beneficiary_collapsed_lines))
                viewTreeObserver.addOnGlobalLayoutListener {
                    if (!isEllipsized(layout)) {
                        setOnClickListener(clickListener)
                    }
                }
                checkFavourite(beneficiary.favourite)
                checkAccessKey(beneficiary.accessKey)
                Glide.with(itemView.context)
                        .load(Uri.parse("$BASE_URL/${beneficiary.flagPath}"))
                        .into(itemView.findViewById(R.id.iv_user))
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

        private fun checkAccessKey(accessKey :String){
            if(accessKey.equals(ExchangeAccessKey.FARD, true)){
                itemView.findViewById<ImageView>(R.id.iv_transfer).setImageResource(R.mipmap.af_logo)
            }else{
                itemView.findViewById<ImageView>(R.id.iv_option).visibility = View.INVISIBLE
                itemView.findViewById<ImageView>(R.id.iv_transfer).setImageResource(R.mipmap.uae_x)
            }
        }
    }
}