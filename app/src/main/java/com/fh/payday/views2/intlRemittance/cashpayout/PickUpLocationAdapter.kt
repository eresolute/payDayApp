package com.fh.payday.views2.intlRemittance.cashpayout;

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import com.fh.payday.R

class PickUpLocationAdapter(
        private val items: List<PickUpLocation>,
       private val pickUpLocationListener: OnPickUpLocationListener,
        private var selectedItem: Int = -1

) : RecyclerView.Adapter<PickUpLocationAdapter.ViewHolder>() {

    private val locationList = ArrayList<PickUpLocation>()

    init {
        items.forEach {
            locationList.add(it)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.agent_location_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = locationList[position]
        holder.bindTo(item)
        holder.rootView.setOnClickListener {
            selectedItem = position
            pickUpLocationListener.onLocationSelect(locationList[position])
            notifyDataSetChanged()
        }
        holder.radioBtn.setOnClickListener {
            selectedItem = position
            pickUpLocationListener.onLocationSelect(locationList[position])
            notifyDataSetChanged()
        }
        holder.radioBtn.isChecked = selectedItem == position
    }

    fun filter(query: String): List<PickUpLocation> {
        locationList.clear()

        items.filter {
            it.locationName.startsWith(query, true) || it.locationDetails?.startsWith(query, true) ?: false
        }.forEach { locationList.add(it) }
        selectedItem = if (locationList.size == 1) 0 else -1
        if (query.isEmpty()) selectedItem = -1
        if (locationList.none { name ->
                query.equals(name.locationName, true)
            }) selectedItem = -1
        notifyDataSetChanged()
        return locationList
    }

    override fun getItemCount() = locationList.size

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val locationName = view.findViewById<TextView>(R.id.location_name)
        private val locationDetails = view.findViewById<TextView>(R.id.location_details)
        val rootView: ConstraintLayout = view.findViewById<ConstraintLayout>(R.id.root_view)
        val radioBtn: RadioButton = view.findViewById(R.id.radiobtn_location)

        fun bindTo(item: PickUpLocation) {
            locationName.text =item.locationName
            locationDetails.text=item.locationDetails
        }
    }

}