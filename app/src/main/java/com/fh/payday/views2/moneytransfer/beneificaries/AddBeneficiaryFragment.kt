package com.fh.payday.views2.moneytransfer.beneificaries

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fh.payday.R
import com.fh.payday.datasource.DataGenerator
import com.fh.payday.utilities.OnItemClickListener
import com.fh.payday.views.shared.IconListAdapter
import com.fh.payday.views2.moneytransfer.beneificaries.local.LocalBeneficiaryEditFragment
import com.fh.payday.views2.moneytransfer.beneificaries.payday.toPayday.PaydayToPaydayBeneficiaryFragment

class AddBeneficiaryFragment : Fragment(), OnItemClickListener {

    lateinit var rvAddBeneficiary: RecyclerView
    lateinit var activity: EditBeneficiaryActivity

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context as EditBeneficiaryActivity
    }

    override fun onResume() {
        super.onResume()
        activity.setToolbarTitle(getString(R.string.add_beneficiary))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_add_beneficiary, container, false)
        init(view)

        val optionList = DataGenerator.getBeneficiaryOptions(activity)
//        optionList.removeAt(2)
        rvAddBeneficiary.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        rvAddBeneficiary.adapter = IconListAdapter(optionList, this )

        return view
    }

    fun init(view: View){
        rvAddBeneficiary = view.findViewById(R.id.rv_add_beneficiary)
    }

    override fun onItemClick(index: Int) {
        when(index) {
            0 -> activity.replaceFragment(PaydayToPaydayBeneficiaryFragment.newInstance())
//            1 -> activity.replaceFragment(PaydayToCreditCardBeneficiaryFragment.newInstance())
            2 -> activity.replaceFragment(LocalBeneficiaryEditFragment.newInstance())
            else -> return
        }
    }
}