package com.fh.payday.views2.bottombar.location

import android.content.Context
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
import com.fh.payday.preferences.LocalePreferences

class BranchATMAdapter constructor(
        private val branchLocators: List<BranchLocator>
) : BaseExpandableListAdapter() {

    override fun getGroupCount(): Int {
        return branchLocators.size
    }

    override fun getChildrenCount(i: Int): Int {
        return 1
    }

    override fun getGroup(i: Int): Any {
        return branchLocators[i]
    }

    override fun getChild(listPosition: Int, expandedListPosition: Int): Any {
        return branchLocators.size
    }

    override fun getGroupId(i: Int): Long {
        return i.toLong()
    }

    override fun getChildId(listPosition: Int, expandedListPosition: Int): Long {
        return expandedListPosition.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(listPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup): View? {
        var mView = convertView
        if (mView == null) {
            val layoutInflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            mView = layoutInflater.inflate(R.layout.item_branch_location, null)
        }

        val rootLayout = mView?.findViewById<ConstraintLayout>(R.id.root_view)
        val tvAddress = mView?.findViewById<TextView>(R.id.tv_address)
        val tvName = mView?.findViewById<TextView>(R.id.tv_country)
        val tvTiming = mView?.findViewById<TextView>(R.id.tv_timing)
        val ivLocator = mView?.findViewById<ImageView>(R.id.iv_locator)
        val ivPlus = mView?.findViewById<ImageView>(R.id.iv_plus)

        tvAddress?.text = branchLocators[listPosition].branchName
        tvName?.text = String.format("%s - %s", branchLocators[listPosition].city,
                branchLocators[listPosition].country)
        tvTiming?.text = String.format("%s (%s - %s)", branchLocators[listPosition].workingDays, branchLocators[listPosition].fromTime,
                branchLocators[listPosition].toTime)


        if (isExpanded) {
            ivLocator?.setImageResource(R.drawable.ic_bank_white)
            ivPlus?.setImageResource(R.drawable.ic_plus_white)
            rootLayout?.background = ContextCompat.getDrawable(parent.context, R.drawable.bg_blue_gradient)
            tvAddress?.setTextColor(ContextCompat.getColor(parent.context, R.color.white))
            tvName?.setTextColor(ContextCompat.getColor(parent.context, R.color.white))
            tvTiming?.setTextColor(ContextCompat.getColor(parent.context, R.color.white))

        } else {
            rootLayout?.background = ContextCompat.getDrawable(parent.context, R.drawable.bg_grey)
            ivLocator?.setImageResource(R.drawable.ic_bank_small)
            ivPlus?.setImageResource(R.drawable.ic_plus)
            tvAddress?.setTextColor(ContextCompat.getColor(parent.context, R.color.textColor))
            tvName?.setTextColor(ContextCompat.getColor(parent.context, R.color.textColor))
            tvTiming?.setTextColor(ContextCompat.getColor(parent.context, R.color.textColor))
        }
        return mView
    }

    override fun getChildView(listPosition: Int, i1: Int, b: Boolean, convertView: View?, parent: ViewGroup): View? {
        var mView = convertView
        if (mView == null) {
            val layoutInflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            mView = layoutInflater.inflate(R.layout.item_branch_location_detail, null)
        }

        val tvAddress = mView?.findViewById<TextView>(R.id.tv_address_detail)
        val tvTollFree = mView?.findViewById<TextView>(R.id.tv_toll_free)
        val tvTelephone = mView?.findViewById<TextView>(R.id.tv_telephone)
        val tvFax = mView?.findViewById<TextView>(R.id.tv_fax)
        val tvEmail = mView?.findViewById<TextView>(R.id.tv_email)

        tvAddress?.text = branchLocators[listPosition].address

        val locale = mView?.let { LocalePreferences.instance.getLocale(it.context) }
        if (locale == "ar" || locale == "ur") {
            tvTollFree?.text = parent.context.getString(R.string.uae_toll_free)
                .plus(" ")
                .plus(branchLocators[listPosition].tollFree.replace("[\\s+]".toRegex(), ""))
            tvTelephone?.text = parent.context.getString(R.string.tel)
                .plus(" ")
                .plus(branchLocators[listPosition].telephone.replace("[\\s+]".toRegex(), ""))
            tvFax?.text = parent.context.getString(R.string.fax)
                .plus(" ")
                .plus(branchLocators[listPosition].fax.replace("[\\s+]".toRegex(), ""))
        } else {
            tvTollFree?.text = parent.context.getString(R.string.uae_toll_free)
                .plus(" ")
                .plus(branchLocators[listPosition].tollFree)
            tvTelephone?.text = parent.context.getString(R.string.tel)
                .plus(" ")
                .plus(branchLocators[listPosition].telephone)
            tvFax?.text = parent.context.getString(R.string.fax)
                .plus(" ")
                .plus(branchLocators[listPosition].fax)
        }
        tvEmail?.text = parent.context.getString(R.string.email).plus(" ").plus(branchLocators[listPosition].emailID)
        return mView
    }

    override fun isChildSelectable(i: Int, i1: Int): Boolean {
        return true
    }
}