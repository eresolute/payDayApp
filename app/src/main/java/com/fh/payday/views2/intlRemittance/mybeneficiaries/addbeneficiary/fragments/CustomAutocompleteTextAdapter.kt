package com.fh.payday.views2.intlRemittance.mybeneficiaries.addbeneficiary.fragments

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.datasource.models.intlRemittance.PayoutAgent


//
// Created by Admin on 6/9/2020.
// Copyright (c) {2020} EMatrix All rights reserved.
//

class CustomAutocompleteTextAdapter<T>(var mContext: Context, var layout: Int, val list: List<T>)
    : ArrayAdapter<T>(mContext, layout, list) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (convertView == null) {
            val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(layout, parent, false)
        }
   /*     var data = list[position]
        var title = view!!.findViewById<TextView>(R.id.text1)
        title.text = data.toString()*/
        return view!!
    }

    override fun getItem(position: Int): T? {
        return super.getItem(position)
    }

    override fun getCount(): Int {
        return super.getCount()
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }
}