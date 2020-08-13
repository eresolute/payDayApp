package com.fh.payday.views.adapter.moneytransfer

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.datasource.models.moneytransfer.LocalBeneficiary
import com.fh.payday.datasource.models.moneytransfer.P2CBeneficiary
import com.fh.payday.utilities.OnLocalBeneficiaryClickListener
import com.fh.payday.utilities.OnP2CBeneficiaryClickListener
import com.fh.payday.utilities.maskCardNumber
import com.fh.payday.views2.moneytransfer.beneificaries.EditBeneficiaryActivity

class P2CBeneficiaryAdapter(
        private val p2cBeneficiaryList: MutableList<P2CBeneficiary>,
        private val listener: EditBeneficiaryActivity.OnBeneficiaryOptionClick<P2CBeneficiary>,
        private val beneficiaryListener: OnP2CBeneficiaryClickListener
) : RecyclerView.Adapter<P2CBeneficiaryAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.registered_beneficiaries_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = p2cBeneficiaryList.size

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.bindTo(p2cBeneficiaryList[p1])
        p0.imgOption.setOnClickListener { listener.onOptionClick(p2cBeneficiaryList[p1], it) }
        p0.layout.setOnClickListener { beneficiaryListener.onP2CBendficiaryClick(p2cBeneficiaryList[p1]) }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvName: TextView = itemView.findViewById(R.id.tv_user_name)
        private val tvCard: TextView = itemView.findViewById(R.id.account_no)
        private val tvBankName: TextView = itemView.findViewById(R.id.tv_bank_name)

        val imgOption: ImageView = itemView.findViewById(R.id.img_option)
        val layout: ConstraintLayout = itemView.findViewById(R.id.root_view)

        fun bindTo(beneficiary: P2CBeneficiary) {
            tvBankName.visibility = View.VISIBLE
            tvName.text = beneficiary.shortName
            tvCard.text = maskCardNumber(beneficiary.creditCardNo , "XXXX XXXX XXXX ####")
            tvBankName.text = beneficiary.bankName
        }
    }


}