package com.fh.payday.views2.payments.countries

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.fh.payday.R
import com.fh.payday.datasource.models.Country2
import com.fh.payday.utilities.GlideApp
import com.fh.payday.utilities.OnItemClickListener
import de.hdodenhof.circleimageview.CircleImageView

class CountryAdapter(
        private val listener: OnItemClickListener,
        private val countries: ArrayList<Country2> = ArrayList()
) : RecyclerView.Adapter<CountryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val view = LayoutInflater.from(
                parent.context).inflate(R.layout.country_item,
                parent,
                false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return countries.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(countries[position], View.OnClickListener { listener.onItemClick(position) })
    }

    fun addAll(list: List<Country2>) {
        countries.addAll(list)
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val linearLayout = view.findViewById<View>(R.id.linear_layout)
        private val tvCountryName = view.findViewById<TextView>(R.id.tv_country_name)
        private val imageView = view.findViewById<CircleImageView>(R.id.iv_circle)

        fun bind(country: Country2, listener: View.OnClickListener) {
            tvCountryName.text = country.countryName
            val flagUrl = country.countryFlag
            GlideApp.with(imageView)
                    .load(flagUrl)
                    .error(R.drawable.ic_flag_placeholder)
                    .placeholder(R.drawable.ic_flag_placeholder)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE))
                    .centerCrop()
                    .into(imageView)

            linearLayout.setOnClickListener(listener)
        }
    }
}