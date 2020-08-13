package com.fh.payday.utilities

import android.app.Dialog
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.AppCompatEditText
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.views2.moneytransfer.beneificaries.shared.SelectBankFragment

fun showBanks(activity: BaseActivity, banks: List<String>, listener: SelectBankFragment.OnBankSelectedListener) {
    val view = LayoutInflater.from(activity).inflate(R.layout.layout_bank_dialog, null, false)

    val dialog = Dialog(activity)
    dialog.setTitle(activity.getString(R.string.select_bank))
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(view)
    dialog.setCanceledOnTouchOutside(false)

    val recyclerView = dialog.findViewById<RecyclerView>(R.id.rvBankList)
    val imgSearch = dialog.findViewById<ImageView>(R.id.iv_search)
    val etSearch = dialog.findViewById<AppCompatEditText>(R.id.et_search)
    val tvTitle = dialog.findViewById<TextView>(R.id.tv_title)
    val tvMessage = dialog.findViewById<TextView>(R.id.tv_message)

    val adapter = BankListAdapter(banks, listener, dialog)
    recyclerView.adapter = adapter
    recyclerView.layoutManager = LinearLayoutManager(activity)
    adapter.notifyDataSetChanged()

    dialog.setOnCancelListener {
        dialog.dismiss()
    }

    imgSearch.setOnClickListener {
        if (etSearch.visibility == View.GONE) {
            (it as ImageView).setImageResource(R.drawable.ic_error_small)
            etSearch.visibility = View.VISIBLE
            tvTitle.visibility = View.GONE
            etSearch.requestFocus()
            activity.showKeyboard(etSearch)
        } else {
            activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            etSearch.setText("")
            (it as ImageView).setImageResource(R.drawable.ic_search)
            etSearch.visibility = View.GONE
            tvTitle.visibility = View.VISIBLE
        }
    }

    val lp = WindowManager.LayoutParams()
    val window = dialog.window
    if (window != null) {
        lp.copyFrom(window.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        window.attributes = lp
    }

    etSearch.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
//            if (window != null) {
//                lp.gravity = Gravity.CENTER
//                window.attributes = lp
//            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            if (window != null) {
                lp.gravity = Gravity.TOP
                window.attributes = lp
            }

            s?.let {
                val itemCount = adapter.filter(it.toString()).count()
                when {
                    itemCount < 1 -> tvMessage.visibility = View.VISIBLE
                    itemCount in 1..2 -> {
                        if (window != null) {
                            lp.gravity = Gravity.TOP
                            window.attributes = lp
                        }
                    }
                    else -> {
                        if (window != null) {
                            lp.gravity = Gravity.CENTER
                            window.attributes = lp
                        }
                        tvMessage.visibility = View.GONE
                    }
                }
            }
        }
    })




    dialog.show()
}

class BankListAdapter(
        private val banks: List<String>,
        private val listener: SelectBankFragment.OnBankSelectedListener,
        private val dialog: Dialog
) : RecyclerView.Adapter<BankListAdapter.ViewHolder>() {

    private var items = ArrayList<String>()

    init {
        banks.forEach { items.add(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bank, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    fun filter(query: String): List<String> {
        items.clear()

        banks.filter {
            it.startsWith(query, true)
        }.forEach { items.add(it) }

        notifyDataSetChanged()

        return items
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(items[position])
        holder.rootLayout.setOnClickListener {
            listener.onBankSelect(items[position])
            dialog.dismiss()
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName = view.findViewById<TextView>(R.id.tv_name)
        val rootLayout = view.findViewById<ConstraintLayout>(R.id.layout_bank)
        fun bindTo(bank: String) {
            tvName.text = bank
        }
    }
}