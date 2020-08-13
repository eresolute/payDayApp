package com.fh.payday.views2.intlRemittance.rateCalculator

import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.fh.payday.R
import com.fh.payday.databinding.SelectCurrItemBinding
import com.fh.payday.datasource.models.intlRemittance.CountryCurrency
import com.fh.payday.utilities.BASE_URL
import com.fh.payday.utilities.OnItemClickListener
import java.util.ArrayList

class SelectCurrencyAdapter2(val list: ArrayList<CountryCurrency>, val listener: OnItemClickListener)
    : RecyclerView.Adapter<SelectCurrencyAdapter2.ViewHolder>() {
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.layout_country_currency, p0,
                false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size
    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.bindTo(list[p1])
        p0.layout.setOnClickListener { listener.onItemClick(p1) }
    }

    class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val layout = view.findViewById<LinearLayout>(R.id.layout_country)
        fun bindTo(country: CountryCurrency) {
            view.findViewById<TextView>(R.id.tv_country).text = country.country
            Glide.with(view.context)
                    .load(Uri.parse("$BASE_URL/${country.imagePath}"))
                    .into(view.findViewById(R.id.flag))
        }
    }
}