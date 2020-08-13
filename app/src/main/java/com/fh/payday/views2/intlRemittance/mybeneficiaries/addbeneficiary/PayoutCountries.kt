package com.fh.payday.views2.intlRemittance.mybeneficiaries.addbeneficiary

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
import com.fh.payday.datasource.models.intlRemittance.PayOutCurrencies
import com.fh.payday.datasource.models.intlRemittance.PayoutCountries
import com.fh.payday.utilities.OnPayoutCountrySelectListener
import com.fh.payday.utilities.OnPayoutCurrencySelectListener

fun showPayoutCountries(
        activity: BaseActivity,
        items: List<PayoutCountries>,
        onCountrySelectListener: OnPayoutCountrySelectListener
) {
    @SuppressLint("InflateParams")
    val view = LayoutInflater.from(activity).inflate(R.layout.iso_alpha3_layout, null, false)

    val dialog = Dialog(activity)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(view)
    dialog.setCanceledOnTouchOutside(false)

    val tvTitle = dialog.findViewById<View>(R.id.tv_title)
    val etSearch = dialog.findViewById<AppCompatEditText>(R.id.et_search)
    val tvMessage = dialog.findViewById<TextView>(R.id.tv_message)

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


    val adapter = ItemAdapter(items, onCountrySelectListener, dialog, activity)
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

private class ItemAdapter(
        private val list: List<PayoutCountries>,
        private val countrySelectListener: OnPayoutCountrySelectListener,
        private val dialog: Dialog,
        private val activity: BaseActivity
//    private val block: (Int) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    private var items = ArrayList<PayoutCountries>()

    init {
        list.forEach { items.add(it) }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.item_iso_alpha3, p0, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(items[position])
        holder.rootView.setOnClickListener {
            countrySelectListener.onPayoutCountrySelect(items[position])
            activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            dialog.dismiss()
        }
    }

    fun filter(query: String): List<PayoutCountries> {
        items.clear()

        list.filter {
            it.countryCode.startsWith(query, true) || it.countryName.startsWith(query, true)
        }.forEach { items.add(it) }

        notifyDataSetChanged()
        return items
    }
}

private class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val rootView: View = view.findViewById<View>(R.id.root_view)
    private val tvCountry = view.findViewById<TextView>(R.id.tv_country_name)
    private val tvAlpha3 = view.findViewById<TextView>(R.id.tv_alpha3)

    fun bindTo(payoutCountry: PayoutCountries) {
        tvCountry.text = payoutCountry.countryName
        val alpha3 = "(${payoutCountry.countryCode})"
        tvAlpha3.text = alpha3
    }

}

fun showPayoutCurrency(
        activity: BaseActivity,
        items: List<PayOutCurrencies>,
        onPayoutCurrencySelectListener: OnPayoutCurrencySelectListener
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

    tvMessage.text = activity.getString(R.string.no_currency_found)
    tvTitle.text = activity.getString(R.string.select_currency)

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

    val adapter = ItemAdapter2(items, onPayoutCurrencySelectListener, dialog, activity)

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

private class ItemAdapter2(
        private val list: List<PayOutCurrencies>,
        private val currencySelectListener: OnPayoutCurrencySelectListener,
        private val dialog: Dialog,
        private val activity: BaseActivity
) : RecyclerView.Adapter<ViewHolder2>() {

    private var items = ArrayList<PayOutCurrencies>()

    init {
        list.forEach { items.add(it) }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder2 {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.item_iso_alpha3, p0, false)
        return ViewHolder2(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder2, position: Int) {
        holder.bindTo(items[position])
        holder.rootView.setOnClickListener {
            currencySelectListener.onPayoutCurrencySelect(items[position])
            activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            dialog.dismiss()
        }
    }

    fun filter(query: String): List<PayOutCurrencies> {
        items.clear()

        list.filter {
            it.code.startsWith(query, true) || it.name.startsWith(query, true)
        }.forEach { items.add(it) }

        notifyDataSetChanged()
        return items
    }
}

private class ViewHolder2(view: View) : RecyclerView.ViewHolder(view) {

    val rootView: View = view.findViewById<View>(R.id.root_view)
    private val tvCountry = view.findViewById<TextView>(R.id.tv_country_name)
    private val tvAlpha3 = view.findViewById<TextView>(R.id.tv_alpha3)

    fun bindTo(payOutCurrency: PayOutCurrencies) {
        tvCountry.text = payOutCurrency.name
        val alpha3 = "(${payOutCurrency.code})"
        tvAlpha3.text = alpha3
    }
}