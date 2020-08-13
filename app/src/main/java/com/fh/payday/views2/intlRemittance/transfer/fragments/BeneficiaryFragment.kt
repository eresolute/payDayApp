package com.fh.payday.views2.intlRemittance.transfer.fragments

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.fh.payday.BaseFragment
import com.fh.payday.R
import com.fh.payday.datasource.models.intlRemittance.IntlBeneficiary
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.ItemOffsetDecoration
import com.fh.payday.views2.intlRemittance.*
import com.fh.payday.views2.intlRemittance.cashpayout.CashPayoutActivity
import com.fh.payday.views2.intlRemittance.mybeneficiaries.addbeneficiary.AddBeneficiaryActivity
import com.fh.payday.views2.intlRemittance.rateCalculator.RateCalculatorActivity
import com.fh.payday.views2.intlRemittance.transfer.TransferActivity
import com.fh.payday.views2.intlRemittance.transfer.adapter.BeneficiaryAdapter
import com.fh.payday.views2.locator.LocatorActivity
import kotlinx.android.synthetic.main.fragment_beneficiary.*
import kotlinx.android.synthetic.main.layout_payment_option.*

class BeneficiaryFragment : BaseFragment(), TransferActivity.OnIntlBeneficiaryClickListener {

    lateinit var progressBar: ProgressBar
    var type: Int = 0
    var action: String = ""
    var count = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_beneficiary, container, false)
        progressBar = view.findViewById(R.id.progress_bar)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = activity as TransferActivity
        super.onViewCreated(view, savedInstanceState)
        tv_add_beneficiary.setOnClickListener {
            clickListener()
        }
        fab_btn_noBeneficiary.setOnClickListener {
            clickListener()
        }

        tv_paymentOptiion.text = getString(R.string.choose_payment_option)
        tv_bank_transfer.setOnClickListener {
            rv_intl_beneficiaries.visibility = View.GONE
            tv_add_beneficiary.visibility = View.GONE
            constraint_layout.visibility = View.GONE
            getBankTransferList(activity)

        }
        tv_cash_payout.setOnClickListener {
            rv_intl_beneficiaries.visibility = View.GONE
            tv_add_beneficiary.visibility = View.GONE
            constraint_layout.visibility = View.GONE
            getCashPayoutList(activity)
        }
    }

    private fun getBankTransferList(activity: TransferActivity) {
        type = 0
        activity.viewmodel.selectedDeliveryMode = DeliveryModes.BT
        setBackGround(tv_cash_payout, tv_bank_transfer, context ?: return)
        setTextColor(tv_cash_payout, tv_bank_transfer, context ?: return)
        setDrawableBT(tv_cash_payout, tv_bank_transfer)
        getBeneficiaryList(type)
    }

    private fun getCashPayoutList(activity: TransferActivity) {
        type = 0
        activity.viewmodel.selectedDeliveryMode = DeliveryModes.CP
        getBeneficiaryList(type)
        setBackGround(tv_bank_transfer, tv_cash_payout, context ?: return)
        setDrawableCP(tv_bank_transfer, tv_cash_payout)
        setTextColor(tv_bank_transfer, tv_cash_payout, context ?: return)
    }

    private fun clickListener() {
        val activity = activity as TransferActivity
        if (activity.viewmodel.selectedDeliveryMode == DeliveryModes.BT) {
            val intent = Intent(activity, AddBeneficiaryActivity::class.java)
            intent.putExtra("deliveryMode", activity.viewmodel.selectedDeliveryMode)
            startActivity(intent)

        } else {
            val intent = Intent(activity, CashPayoutActivity::class.java)
            intent.putExtra("deliveryMode", activity.viewmodel.selectedDeliveryMode)
            startActivity(intent)
        }
    }


    private fun getCpBeneficiaryList() {
        constraint_layout.visibility = View.VISIBLE
        tv_add_beneficiary.visibility = View.GONE
        rv_intl_beneficiaries.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        action = arguments?.getString("action") ?: return
        getBeneficiaryList(type)
        addObserver()
    }


    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (activity != null && isVisibleToUser) {
            rv_intl_beneficiaries ?: return
            val adapter = rv_intl_beneficiaries.adapter ?: return
            adapter.notifyDataSetChanged()
        }
    }

    private fun setupRecyclerView(list: List<IntlBeneficiary>) {
        rv_intl_beneficiaries.visibility = View.VISIBLE
        tv_add_beneficiary.visibility = View.VISIBLE
        val activity = activity as TransferActivity
        rv_intl_beneficiaries.adapter = BeneficiaryAdapter(emptyList(), this)
        if (list.isEmpty()) {
            when {
                activity.viewmodel.filterCcyCode == null -> showNoBeneficiaryViews()
                type == 0 -> showLoadMoreView()
                else -> showNoBeneficiaryViews()
            }
            return
        }

        constraint_layout.visibility = View.GONE
        tvNoBeneficiaryInstruction.visibility = View.GONE
        btn_locate_exchange.visibility = View.GONE
        val adapter = rv_intl_beneficiaries.adapter
        if (list.isNotEmpty() && type == 0 && adapter != null && adapter.itemCount != list.size
            && action == RateCalculatorActivity::class.java.name
        ) {
            showMore()
        }

        rv_intl_beneficiaries.layoutManager = LinearLayoutManager(activity)
        rv_intl_beneficiaries.addItemDecoration(ItemOffsetDecoration(1))
        rv_intl_beneficiaries.adapter = BeneficiaryAdapter(list, this)

    }

    private fun showNoBeneficiaryViews() {
        constraint_layout.visibility = View.VISIBLE
        rv_intl_beneficiaries.visibility = View.GONE
        tv_add_beneficiary.visibility = View.GONE
        tvNoBeneficiaryInstruction.visibility = View.GONE
        btn_locate_exchange.visibility = View.GONE

        btn_locate_exchange.setOnClickListener {
            activity?.startActivity(Intent(activity, LocatorActivity::class.java).also {
                it.putExtra("action", "exchange")
            })
        }
    }

    private fun showMore() {
        val activity = activity as TransferActivity

        constraint_layout.visibility = View.GONE
        tv_add_beneficiary.visibility = View.GONE
        tvNoBeneficiaryInstruction.visibility = View.GONE
        if (count > 0)
            btn_locate_exchange.visibility = View.GONE
        else
            btn_locate_exchange.visibility = View.VISIBLE
        btn_locate_exchange.text = activity.getString(R.string.load_more)

        btn_locate_exchange.setOnClickListener {
            setupRecyclerView(activity.viewmodel.beneficiaryList ?: emptyList())
            constraint_layout.visibility = View.GONE
            tv_add_beneficiary.visibility = View.GONE
            tvNoBeneficiaryInstruction.visibility = View.GONE
            btn_locate_exchange.visibility = View.GONE
        }
    }

    private fun showLoadMoreView() {
        val activity = activity as TransferActivity

        constraint_layout.visibility = View.VISIBLE
        tv_add_beneficiary.visibility = View.GONE
        fab_btn_noBeneficiary.visibility = View.GONE
        tvNoBeneficiaryInstruction.visibility = View.GONE
        btn_locate_exchange.visibility = View.VISIBLE
        btn_locate_exchange.text = activity.getString(R.string.load_more)

        btn_locate_exchange.setOnClickListener {
            count = 1
            setupRecyclerView(activity.viewmodel.beneficiaryList ?: emptyList())
        }
    }

    private fun hideViews() {
        constraint_layout.visibility = View.GONE
        tvNoBeneficiaryInstruction.visibility = View.GONE
        btn_locate_exchange.visibility = View.GONE
    }


    private fun getBeneficiaryList(type: Int) {
        activity ?: return
        val activity = activity as TransferActivity
        val user = UserPreferences.instance.getUser(activity) ?: return
        when (type) {
            0 ->
                if (action == RateCalculatorActivity::class.java.name)
                    activity.viewmodel.getBeneficiaries(
                        user.token,
                        user.sessionId,
                        user.refreshToken,
                        user.customerId.toString(),
                        activity.viewmodel.selectedAccessKey ?: return,
                        activity.viewmodel.selectedDeliveryMode
                    )
                else {
                    if (ExchangeContainer.exchanges().isEmpty()) return
                    activity.viewmodel.getBeneficiariesRx(
                        user.token,
                        user.sessionId,
                        user.refreshToken,
                        user.customerId.toString(),
                        activity.viewmodel.selectedDeliveryMode
                    )
                }

            1 -> setupRecyclerView(emptyList())
        }
    }

    private fun addObserver() {
        activity ?: return
        val activity = activity as TransferActivity
        activity.viewmodel.intlBeneficiary.observe(this, Observer {
            it ?: return@Observer

            val beneficiaryState = it.getContentIfNotHandled() ?: return@Observer

            if (beneficiaryState is NetworkState2.Loading<List<IntlBeneficiary>>) {
                progressBar.visibility = View.VISIBLE
                hideViews()
                return@Observer
            }

            progressBar.visibility = View.GONE

            when (beneficiaryState) {
                is NetworkState2.Success -> {
                    val data: List<IntlBeneficiary> = beneficiaryState.data ?: return@Observer

                    if (activity.viewmodel.filterCcyCode != null) {
                        setupRecyclerView(
                            filterBeneficiary(
                                activity.viewmodel.filterCcyCode!!,
                                data
                            )
                        )
                    } else {
                        setupRecyclerView(data)
                    }
                    return@Observer
                }

                is NetworkState2.Error -> {
                    if (beneficiaryState.isSessionExpired) {
                        activity.onSessionExpired(beneficiaryState.message)
                        return@Observer
                    }
                    val (message) = beneficiaryState
                    activity.handleErrorCode(beneficiaryState.errorCode.toInt(), message)
                }

                is NetworkState2.Failure -> {
                    activity.onFailure(
                        activity.findViewById(R.id.layout),
                        beneficiaryState.throwable
                    )
                }
            }
        })
    }

    private fun filterBeneficiary(
        filter: String,
        list: List<IntlBeneficiary>
    ): List<IntlBeneficiary> {
        val activity = activity as TransferActivity
        val tempList = mutableListOf<IntlBeneficiary>()

        if (filter.isEmpty()) return list

        list.filter {
            it.payoutCcyCode.toLowerCase() == activity.viewmodel.filterCcyCode?.toLowerCase()
        }.forEach {
            tempList.add(it)
        }

        return tempList
    }

    override fun onIntlBeneficiaryClick(position: Int, beneficiary: IntlBeneficiary) {
        val activity = activity as TransferActivity
        activity.viewmodel.toClear = true
        activity.viewmodel.selectedBeneficiary = beneficiary
        activity.viewmodel.payInCurrency = "AED"
        activity.viewmodel.payoutCurrency = beneficiary.payoutCcyCode
        activity.viewmodel.payoutCountryCode = beneficiary.receiverCountryCode
        activity.viewmodel.selectedAccessKey = beneficiary.accessKey
        activity.navigateUp()
    }
}

