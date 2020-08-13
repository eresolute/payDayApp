package com.fh.payday.views2.intlRemittance.cashpayout

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import com.fh.payday.R
import kotlinx.android.synthetic.main.fragment_search_agent.*

class PickUpBankAdapter(
    private val list: List<PickUpBankName>,
    private val listener: OnPickBankNameListener,
    private var selectedItem: Int = -1
) : RecyclerView.Adapter<PickUpBankAdapter.ViewHolder>() {
    private var bankNameList = ArrayList<PickUpBankName>()

    init {
        list.forEach { bankNameList.add(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.agent_location_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = bankNameList[position]

        holder.rootView.setOnClickListener {
            selectedItem = position
            listener.onBankSelect(bankNameList[position])
            notifyDataSetChanged()
        }
        holder.radioBtn.setOnClickListener {
            selectedItem = position
            listener.onBankSelect(bankNameList[position])
            notifyDataSetChanged()
        }

        holder.radioBtn.isChecked = selectedItem == position

        holder.bindTo(item)
    }

    fun filter(query: String): List<PickUpBankName> {
        bankNameList.clear()
        bankNameList = ArrayList()
        list.filter {
            it.bankName.startsWith(query, true) || it.bankDetails?.startsWith(query, true) ?: false
        }.forEach { bankNameList.add(it) }
        selectedItem = if (bankNameList.size == 1) 0 else -1
        if (query.isEmpty()) selectedItem = -1
        if (bankNameList.none { name ->
                query.equals(name.bankName, true)
            }) selectedItem = -1
        notifyDataSetChanged()
        return bankNameList
    }

    override fun getItemCount() = bankNameList.size

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val bankName = view.findViewById<TextView>(R.id.location_name)
        private val bankDetails = view.findViewById<TextView>(R.id.location_details)
        val rootView = view.findViewById<ConstraintLayout>(R.id.root_view)

        val radioBtn: RadioButton = view.findViewById(R.id.radiobtn_location)

        fun bindTo(item: PickUpBankName) {
            bankName.text = item.bankName
            bankDetails.text = item.bankDetails
        }

    }

}