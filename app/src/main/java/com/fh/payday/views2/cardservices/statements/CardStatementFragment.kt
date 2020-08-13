package com.fh.payday.views2.cardservices.statements


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.fh.payday.R
import com.fh.payday.datasource.models.CardStatement
import com.fh.payday.utilities.OnItemClickListener
import com.fh.payday.views.adapter.CardStatementAdapter

class CardStatementFragment : Fragment(), OnItemClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_card_statement, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.adapter = CardStatementAdapter(getCardStatements(), this)
        return view
    }

    override fun onItemClick(index: Int) {
        //(activity as CardStatementTypeActivity).onTransactionDetails()
    }

    private fun getCardStatements() = arrayListOf(
            CardStatement("October", "2018"),
            CardStatement("September", "2018"),
            CardStatement("August", "2018"),
            CardStatement("July", "2018"),
            CardStatement("June", "2018"),
            CardStatement("May", "2018"),
            CardStatement("April", "2018"),
            CardStatement("March", "2018"),
            CardStatement("February", "2018"),
            CardStatement("January", "2018")
    )
}
