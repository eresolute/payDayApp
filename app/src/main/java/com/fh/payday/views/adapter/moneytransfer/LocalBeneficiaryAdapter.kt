package com.fh.payday.views.adapter.moneytransfer

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.datasource.models.moneytransfer.LocalBeneficiary
import com.fh.payday.utilities.OnLocalBeneficiaryClickListener
import com.fh.payday.views2.moneytransfer.beneificaries.EditBeneficiaryActivity

class LocalBeneficiaryAdapter(
        private val localBeneficiaryList: MutableList<LocalBeneficiary>,
        private val localBeneficiaryClickListener: OnLocalBeneficiaryClickListener,
        private val listener: EditBeneficiaryActivity.OnBeneficiaryOptionClick<LocalBeneficiary>
): RecyclerView.Adapter<LocalBeneficiaryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_local_beneficiary, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = localBeneficiaryList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(localBeneficiaryList[position])
        holder.imgOption.setOnClickListener { listener.onOptionClick(localBeneficiaryList[position], it)  }
        holder.layout.setOnClickListener { localBeneficiaryClickListener.onBeneficiaryClick(localBeneficiaryList[position]) }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvName = itemView.findViewById<TextView>(R.id.tv_beneficiary_name)
        val tvBank = itemView.findViewById<TextView>(R.id.tv_bank_name)
        val tvAccount = itemView.findViewById<TextView>(R.id.account_no)
        val tvIban = itemView.findViewById<TextView>(R.id.iban_no)
        val imgOption = itemView.findViewById<ImageView>(R.id.imgOption)
        val layout = itemView.findViewById<ConstraintLayout>(R.id.root_view)

        fun bindTo(beneficiary: LocalBeneficiary) {
            tvName.text = beneficiary.beneficiaryName
            tvBank.text = beneficiary.bank
            tvAccount.text = beneficiary.accountNo
            tvIban.text = beneficiary.IBAN
        }
    }
}