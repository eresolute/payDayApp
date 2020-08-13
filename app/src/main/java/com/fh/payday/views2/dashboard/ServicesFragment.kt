package com.fh.payday.views2.dashboard

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fh.payday.R
import com.fh.payday.datasource.models.Item
import com.fh.payday.utilities.OnItemClickListener
import com.fh.payday.viewmodels.MainViewModel
import com.fh.payday.views2.payments.callingcards.CallingCardActivity
import com.fh.payday.views2.moneytransfer.InternationalMoneyTransfer
import com.fh.payday.views2.cardservices.withdraw.CardlessWithdrawalActivity
import com.fh.payday.views2.cardservices.earlysalary.EarlySalaryActivity
import com.fh.payday.views2.payments.recharge.MobileRechargeActivity
import com.fh.payday.views.adapter.dashboard.ServiceAdapter
import java.util.*

class ServicesFragment : Fragment() {

    lateinit var viewModel: MainViewModel
    private val serviceListener = object : OnItemClickListener {
        override fun onItemClick(index: Int) {
            when (index) {
                0 -> {
                    val intent = Intent(activity, MobileRechargeActivity::class.java)
                    intent.putExtra("operator", "du")
                    startActivity(intent)
                }
                1 -> {
                    val intent = Intent(activity, MobileRechargeActivity::class.java)
                    intent.putExtra("operator", "etisalat")
                    startActivity(intent)
                }
                2 ->{
                    val intent = Intent(activity, CallingCardActivity::class.java)
                    intent.putExtra("operator", "hello_card")
                    startActivity(intent)
                }
                3 ->{
                    val intent = Intent(activity, InternationalMoneyTransfer::class.java)
                    startActivity(intent)
                }
                4 -> {
                    val intent = Intent(activity, CardlessWithdrawalActivity::class.java)
                    intent.putExtra("operator", "cardless")
                    startActivity(intent)
                }
                5 -> {
                    val intent = Intent(activity, EarlySalaryActivity::class.java)
                    intent.putExtra("operator", "early_salary")
                    startActivity(intent)
                }
                6 -> {
                }
                else -> {

                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_services, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)

        val viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        viewModel.services = getServices()

        recyclerView.adapter = ServiceAdapter(serviceListener, viewModel.services ?: ArrayList<Item>())

        return view
    }

    private fun getServices(): List<Item> {
        val services = ArrayList<Item>()
        val res = resources
        val arrays = res.getStringArray(R.array.services)

        val icons = res.obtainTypedArray(R.array.services_icons)

        for (i in arrays.indices) {
            services.add(Item(arrays[i], icons.getResourceId(i, 0)))
        }

        icons.recycle()
        return services
    }

}
