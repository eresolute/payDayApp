package com.fh.payday.views2.intlRemittance.mybeneficiaries.addbeneficiary.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fh.payday.BaseActivity
import com.fh.payday.BaseFragment
import com.fh.payday.R
import com.fh.payday.datasource.models.shared.ListModel
import com.fh.payday.views.shared.ListAdapter
import com.fh.payday.views2.intlRemittance.mybeneficiaries.addbeneficiary.AddBeneficiaryActivity
import kotlinx.android.synthetic.main.fragment_beneficiary_summary.*

class BeneficiarySummaryFragment : BaseFragment() {

    override fun onResume() {
        super.onResume()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {

        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_beneficiary_summary, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler_view.adapter = ListAdapter(arrayListOf(), activity
                ?: return, getString(R.string.payment_details), true)
        handleRecyclerView()

        btn_confirm.setOnClickListener {
            val activity = getActivity() as AddBeneficiaryActivity
           /* activity.showMessage(getString(R.string.add_beneficiary_success),
                    R.drawable.ic_success_checked_blue, R.color.blue,
                    com.fh.payday.views2.shared.custom.AlertDialogFragment.OnConfirmListener {
                        activity.finish()
                    })*/
            activity.navigateUp()
        }
    }

    private fun handleRecyclerView() {
        val adapter = recycler_view.adapter


        //To do remove this, get from api
        if (adapter is ListAdapter) {
            val list = arrayListOf(
                    ListModel("Benficiary Name", "Zahir Hussain"),
                    ListModel("Nick Name", "Zahir"),
                    ListModel("Relationship", "Cousin"),
                    ListModel("Mobile Number", String.format(getString(R.string.plus_971_sign), "564747877")),
                    ListModel("IBAN", "XXXXXXXXXXXXX"),
                    ListModel("Account Number", "XXXXXXXXXX"),
                    ListModel("Bank Name", "Jammu and Kashmir Bank"),
                    ListModel("Branch Name", "Islamabad"),
                    ListModel("Branch Address", "Islamabad"),
                    ListModel("Country", "India"),
                    ListModel("State", "J&K"),
                    ListModel("City", "Islamabad")
            )
            adapter.addAll(list)
        }

    }
}
