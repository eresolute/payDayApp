package com.fh.payday.views2.intlRemittance.mybeneficiaries.addbeneficiary.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.datasource.models.intlRemittance.RelationsItem


//
// Created by Admin on 7/2/2020.
// Copyright (c) {2020} EMatrix All rights reserved.
//

class ListPopupAdapter(val list: List<RelationsItem>, val layoutInflater: LayoutInflater, val listner: (String) -> Unit) : BaseAdapter() {

    class ViewHolder {
        var text: TextView? = null;
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        var viewHolder: ViewHolder? = null
        if (view == null) {
            viewHolder = ViewHolder()
            view = layoutInflater.inflate(R.layout.layout_autcompletetext_1, null)
            viewHolder.text = view.findViewById(R.id.relation_text) as TextView
            view.tag = viewHolder
        } else {
            viewHolder = view.tag as ViewHolder
        }
        viewHolder.text?.text = list[position].name
        viewHolder.text?.setOnClickListener{listner(list[position].name)}
        return view!!
    }

    override fun getItem(p0: Int): String {
        return list[p0].name
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return list.size
    }
}