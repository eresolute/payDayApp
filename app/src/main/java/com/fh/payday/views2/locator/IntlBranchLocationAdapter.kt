package com.fh.payday.views2.locator

import android.graphics.PorterDuff
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.datasource.models.BranchLocator
import com.fh.payday.datasource.models.IntlBranchLocator
import com.fh.payday.preferences.LocalePreferences
import kotlinx.android.synthetic.main.item_branch_location_detail.view.*

class IntlBranchLocationAdapter(val branches: MutableList<IntlBranchLocator>) : BaseExpandableListAdapter(){
    override fun getGroup(groupPosition: Int) = branches[groupPosition]
    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true
    override fun hasStableIds(): Boolean = false
    override fun getChildrenCount(groupPosition: Int) = 1
    override fun getChild(groupPosition: Int, childPosition: Int) = branches.size
    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()
    override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()
    override fun getGroupCount(): Int = branches.size

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, v: View?, parent: ViewGroup?): View {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.item_branch_location, parent, false)
        val rootLayout = view.findViewById<ConstraintLayout>(R.id.root_view)
        val tvAddress = view.findViewById<TextView>(R.id.tv_address)
        val tvName = view.findViewById<TextView>(R.id.tv_country)
        val tvTiming = view.findViewById<TextView>(R.id.tv_timing)
        val ivLocator = view.findViewById<ImageView>(R.id.iv_locator)
        val ivPlus = view.findViewById<ImageView>(R.id.iv_plus)

        tvTiming.visibility = View.GONE
        tvAddress.text = branches[groupPosition].branchName
        tvName.text = String.format("%s - %s", branches[groupPosition].city,
                branches[groupPosition].country)
        tvTiming.text = String.format("%s (%s - %s)", branches[groupPosition].workingDays, branches[groupPosition].fromTime,
                branches[groupPosition].toTime)


        if (isExpanded && parent != null) {
            ivLocator.setColorFilter(ContextCompat.getColor(parent.context, R.color.white), PorterDuff.Mode.SRC_IN)
            ivPlus.setImageDrawable(ContextCompat.getDrawable(parent.context, R.drawable.ic_collapse))
            rootLayout.background = ContextCompat.getDrawable(parent.context, R.drawable.bg_blue_gradient)
            tvAddress.setTextColor(ContextCompat.getColor(parent.context, R.color.white))
            tvName.setTextColor(ContextCompat.getColor(parent.context, R.color.white))
            tvTiming.setTextColor(ContextCompat.getColor(parent.context, R.color.white))

        } else {
            if (parent != null) {
                rootLayout.background = ContextCompat.getDrawable(parent.context, R.drawable.bg_grey)
                tvAddress.setTextColor(ContextCompat.getColor(parent.context, R.color.textColor))
                tvName.setTextColor(ContextCompat.getColor(parent.context, R.color.textColor))
                tvTiming.setTextColor(ContextCompat.getColor(parent.context, R.color.textColor))
            }
        }
        //   ivPlus.setOnClickListener(v -> listener.onItemClick(listPosition));

        return view
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {

        val view = LayoutInflater.from(parent?.context).inflate(R.layout.item_branch_location_detail, parent, false)
        val tvAddress = view.findViewById<TextView>(R.id.tv_address_detail)
        val tvTollFree = view.findViewById<TextView>(R.id.tv_toll_free)
        val tvTelephone = view.findViewById<TextView>(R.id.tv_telephone)
        val tvFax = view.findViewById<TextView>(R.id.tv_fax)
        val tvEmail = view.findViewById<TextView>(R.id.tv_email)

        tvFax.visibility = View.GONE
        tvEmail.visibility = View.GONE
        tvTollFree.visibility = View.GONE

        tvAddress.text = branches[groupPosition].address
        val locale = LocalePreferences.instance.getLocale(view.context)
        if (locale == "ar" || locale == "ur") {
            tvTelephone.append(" " + branches[groupPosition].telephone.replace("[\\s+]".toRegex(), ""))
        } else {
            tvTelephone.append(" " + branches[groupPosition].telephone)
        }

        setWeeklyTimings(view, branches[groupPosition])

        //tvEmail.append(branches[groupPosition].emailID)
        return view
    }

    private fun setWeeklyTimings(view: View, branch: IntlBranchLocator) {
        view.group.visibility = View.VISIBLE
        val days = view.context.resources.getStringArray(R.array.weekly_timing)

        view.chip_sun.apply {
            branch.sundayTiming?.let { view.chip_sun.text = days[0].format(it) }
            visibility = if (branch.sundayTiming.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
        view.chip_mon.apply {
            branch.mondayTiming?.let { view.chip_mon.text = days[1].format(it) }
            visibility = if (branch.mondayTiming.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
        view.chip_tue.apply {
            branch.tuesdayTiming?.let { view.chip_tue.text = days[2].format(it) }
            visibility = if (branch.tuesdayTiming.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
        view.chip_wed.apply {
            branch.wednesdayTiming?.let { view.chip_wed.text = days[3].format(it) }
            visibility = if (branch.wednesdayTiming.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
        view.chip_thu.apply {
            branch.thursdayTiming?.let { view.chip_thu.text = days[4].format(it) }
            visibility = if (branch.thursdayTiming.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
        view.chip_fri.apply {
            branch.fridayTiming?.let { view.chip_fri.text = days[5].format(it) }
            visibility = if (branch.fridayTiming.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
        view.chip_sat.apply {
            branch.saturdayTiming?.let { view.chip_sat.text = days[6].format(it) }
            visibility = if (branch.saturdayTiming.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
    }

    fun filter(locators: List<IntlBranchLocator>) {
        this.branches.clear()
        this.branches.addAll(locators)
        notifyDataSetChanged()
    }
}