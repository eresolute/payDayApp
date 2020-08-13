package com.fh.payday.views2.shared.custom


import android.annotation.SuppressLint
import android.app.Dialog
import android.support.v7.widget.AppCompatEditText
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.BuildConfig
import com.fh.payday.R
import com.fh.payday.datasource.models.IndianState
import com.fh.payday.utilities.OnItemClickListener
import com.fh.payday.utilities.OnStateSelectListener


fun showIndianStates(
        activity: BaseActivity,
        items: List<IndianState>,
        onStateSelectListener: OnStateSelectListener
) {
    @SuppressLint("InflateParams")
    val view = LayoutInflater.from(activity).inflate(R.layout.iso_alpha3_layout, null, false)

    val dialog = Dialog(activity)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(view)
    dialog.setCanceledOnTouchOutside(false)

    val tvTitle = dialog.findViewById<TextView>(R.id.tv_title)
    val etSearch = dialog.findViewById<AppCompatEditText>(R.id.et_search)
    val tvMessage = dialog.findViewById<TextView>(R.id.tv_message)
    tvTitle.text = view.resources.getString(R.string.select_state)
    tvMessage.text = view.resources.getString(R.string.no_state_found)
    dialog.findViewById<View>(R.id.iv_search).setOnClickListener {
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


    val adapter = ItemAdapters(items, onStateSelectListener, dialog, activity)

    val recyclerView = dialog.findViewById<RecyclerView>(R.id.recycler_view)
    recyclerView.adapter = adapter

    dialog.setOnCancelListener { dialog.dismiss() }

    etSearch.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            s?.let {
                val itemCount = adapter.filter(it.toString()).count()
                when {
                    itemCount < 1 -> tvMessage.visibility = View.VISIBLE
                    else -> tvMessage.visibility = View.GONE
                }
            }
        }
    })

    val lp = WindowManager.LayoutParams()
    val window = dialog.window
    if (window != null) {
        lp.copyFrom(window.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        window.attributes = lp
    }

    if (!BuildConfig.DEBUG) {
        dialog.window?.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
    }

    dialog.show()
}

private class ItemAdapters(
        private val list: List<IndianState>,
        private val stateSelectListener: OnStateSelectListener,
        private val dialog: Dialog,
        private val activity: BaseActivity
) : RecyclerView.Adapter<ItemAdapters.MyViewHolder>() {

    private var items = ArrayList<IndianState>()

    init {
        list.forEach { items.add(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_iso_alpha3, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindTo(items[position])
        holder.rootView.setOnClickListener {
            stateSelectListener.onStateSelect(items[position])
            activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            dialog.dismiss()
        }
    }

    fun filter(query: String): List<IndianState> {
        items.clear()

        list.filter {
            it.stateAlias.startsWith(query, true) || it.stateName.startsWith(query, true)
        }.forEach { items.add(it) }

        notifyDataSetChanged()

        return items
    }


    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val rootView: View = view.findViewById<View>(R.id.root_view)
        private val tvStateName = view.findViewById<TextView>(R.id.tv_country_name)
        private val tvStateAlias = view.findViewById<TextView>(R.id.tv_alpha3)

        fun bindTo(indianState: IndianState) {
            tvStateName.text = indianState.stateName
            val alpha3 = "(${indianState.stateAlias})"
            tvStateAlias.text = alpha3
        }

    }

}