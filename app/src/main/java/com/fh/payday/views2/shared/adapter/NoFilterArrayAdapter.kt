package com.fh.payday.views2.shared.adapter

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter

class NoFilterArrayAdapter<T>(
    context: Context, layout: Int, var values: MutableList<T>
) : ArrayAdapter<T>(context, layout, values) {

    constructor(context: Context, layout: Int, values: Array<T>) : this(
        context,
        layout,
        values.toMutableList()
    )

    private val filterNothing = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()
            results.values = values
            results.count = values.size
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            notifyDataSetChanged()
        }
    }

    override fun getFilter(): Filter {
        return filterNothing
    }
}