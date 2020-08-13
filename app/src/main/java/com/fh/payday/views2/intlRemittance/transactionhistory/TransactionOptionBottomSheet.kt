package com.fh.payday.views2.intlRemittance.transactionhistory

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.datasource.models.Item
import com.fh.payday.views2.shared.custom.RoundedBottomSheetDialogFragment
import kotlinx.android.synthetic.main.layout_transaction_option.*

class TransactionOptionBottomSheet : RoundedBottomSheetDialogFragment() {
    private var clickListener: (Int) -> Unit = {}
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_transaction_option, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val options = arguments?.getParcelableArrayList<Item>("transaction_option")
                ?: return
        recycler_view.adapter = TransactionOptionAdapter(options) {
            dismiss()
            clickListener(it)
        }
    }

    companion object {
        fun newInstance(
                items: ArrayList<Item>,
                listener: (Int) -> Unit
        ): TransactionOptionBottomSheet {
            val bottomSheet = TransactionOptionBottomSheet().apply {
                clickListener = listener
            }
            val bundle = Bundle()
            bundle.putParcelableArrayList("transaction_option", items)
            bottomSheet.arguments = bundle
            return bottomSheet
        }
    }
}

class TransactionOptionAdapter(
        private val items: ArrayList<Item>,
        private val listener: (Int) -> Unit

) : RecyclerView.Adapter<TransactionOptionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder =
            ViewHolder(LayoutInflater.from(
                    parent.context)
                    .inflate(R.layout.transaction_option_item, parent, false)
            )

    override fun getItemCount(): Int = items.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(items[position], View.OnClickListener { listener(position) })
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvOption = view.findViewById<TextView>(R.id.tv_transaction_option)
        private val imageView = view.findViewById<ImageView>(R.id.image_view)
        private val constraintLayout = view.findViewById<ConstraintLayout>(R.id.parent)
        fun bindTo(item: Item, listener: View.OnClickListener) {
            tvOption.text = item.name
            imageView.setImageResource(item.res)
            constraintLayout.setOnClickListener(listener)
        }
    }
}