package com.fh.payday.views2.intlRemittance.cashpayout

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.BaseFragment
import com.fh.payday.R
import com.fh.payday.datasource.models.IsoAlpha3
import com.fh.payday.datasource.models.intlRemittance.PayoutAgent
import com.fh.payday.datasource.models.intlRemittance.PayoutAgentBranches
import com.fh.payday.datasource.models.intlRemittance.PayoutCountries
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.*
import com.fh.payday.viewmodels.intlRemittance.AddBeneficiaryViewModel
import com.fh.payday.views2.intlRemittance.DeliveryModes
import com.fh.payday.views2.intlRemittance.ExchangeContainer
import com.fh.payday.views2.intlRemittance.mybeneficiaries.addbeneficiary.showPayoutCountries
import com.fh.payday.views2.shared.custom.showCountries
import kotlinx.android.synthetic.main.fragment_bank_details.*
import kotlinx.android.synthetic.main.fragment_search_agent.*
import kotlinx.android.synthetic.main.fragment_search_agent.et_bank_name
import kotlinx.android.synthetic.main.fragment_search_agent.et_state
import kotlinx.android.synthetic.main.fragment_search_agent.textInputLayout_state


class SearchAgentFragment : BaseFragment() {

    private var cashPayoutActivity: CashPayoutActivity? = null
    private var type: Int? = null
    private var stateData: List<PayoutAgentBranches>? = null
    private var flag = false

    private var pickUpBankName = ArrayList<PickUpBankName>()
    private var pickUpLocation = ArrayList<PickUpLocation>()

    private val viewModel by lazy {
        cashPayoutActivity?.let {
            ViewModelProviders.of(it).get(AddBeneficiaryViewModel::class.java)
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        cashPayoutActivity = context as CashPayoutActivity
    }

    override fun onDetach() {
        super.onDetach()
        cashPayoutActivity = null
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if (isVisibleToUser) {
            if (viewModel?.clearBankDetails ?: return) {
                /* tv_country.text = null*/
                et_state.text?.clear()
                et_bank_name.text?.clear()
                rv_locations.visibility = View.GONE

            }
            when (arguments?.getInt(KEY_TYPE, TYPE_BANK) ?: TYPE_BANK) {
                TYPE_LOCATION -> {
                    initLocation()
                    et_state.setText(viewModel?.inputWrapper?.state.toString())
                }
                TYPE_BANK -> initBank()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search_agent, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = getActivity() as BaseActivity
        stateData = ArrayList()

        et_state.isCursorVisible = false
        et_state.isFocusable = false
        et_state.isEnabled = false

        tv_country.isCursorVisible = false
        tv_country.isFocusable = false

        textInputLayout_state.hint = null
        et_state.hint = resources.getString(R.string.state)
        type = arguments?.getInt(KEY_TYPE, TYPE_BANK) ?: TYPE_BANK
        rv_locations.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        init()
        addTextWatcherListener(et_state, textInputLayout_state)
        addTextWatcherListener(et_bank_name, textInputLayout_pickup_location)
        tv_country.setTextColor(resources.getColor(R.color.textColor))
     /*   when (type) {
            TYPE_LOCATION -> {
                initLocation()
                et_state.setText(viewModel?.inputWrapper?.state.toString())
            }
            TYPE_BANK -> initBank()
        }*/
    }


    private fun init() {
    }

    private fun initBank() {
        view ?: return
        tv_pickup_location.text = getString(R.string.search_for_bank)
        et_bank_name.hint = getString(R.string.pickup_banks)
        tv_not_available.text = getString(R.string.no_bank_found)
        tv_location_title.visibility = View.VISIBLE
        rv_locations.visibility = View.VISIBLE
        tv_not_available.visibility = View.GONE
        tv_country.isEnabled = true
        et_state.isEnabled = true
        tv_country.text = viewModel?.inputWrapper?.countryName
        tv_country.isFocusable = false
        val user = UserPreferences.instance.getUser(context ?: return) ?: return
        val wrapper = viewModel?.inputWrapper ?: return
       // btn_next.visibility = View.GONE
        btn_next.backgroundTintList = resources.getColorStateList(R.color.grey_600)
        btn_next.setOnClickListener {
            if(TextUtils.isEmpty(et_bank_name.text.toString())){
                setError(resources.getString(R.string.error_bank_name1),et_bank_name)
            }
        }
        viewModel?.getPayoutAgents(
            user.token, user.sessionId, user.refreshToken, user.customerId.toLong(),
            ExchangeContainer.accessKey, wrapper.countryCode
                ?: return, wrapper.deliveryMode
                ?: return
        ).also { payoutAgentsObserver() }
        /*     tv_country.setOnClickListener {
                 loadPayoutAgents()
                 tv_country.background = ContextCompat.getDrawable(
                     context ?: return@setOnClickListener,
                     R.drawable.bg_grey_blue_border
                 )
                 viewModel?.clearData = true
                 et_state.text?.clear()
             }*/


        et_state.setOnClickListener {
            flag = false
            showStateDialog(stateData ?: return@setOnClickListener)
            /*viewModel?.getPayoutAgentBranches(user.token, user.sessionId, user.refreshToken, user.customerId.toLong(),
                ExchangeContainer.accessKey, viewModel?.payoutAgentId
                    ?: return@setOnClickListener, wrapper.deliveryMode ?: return@setOnClickListener).also { payoutAgentStateObserver() }*/

        }



        if (viewModel?.clearBankData ?: return) {
            /*tv_country.text = null*/
            et_state.text?.clear()
            et_bank_name.text?.clear()
            rv_locations.visibility = View.GONE
        }

    }

    private fun initLocation() {
        view ?: return
        tv_pickup_location.text = getString(R.string.search_for_agent)
        et_bank_name.hint = getString(R.string.pickup_locations)
        tv_not_available.text = getString(R.string.no_location)
        rv_locations.visibility = View.VISIBLE
        tv_not_available.visibility = View.GONE
        tv_location_title.visibility = View.VISIBLE
        tv_country.isEnabled = false
        et_state.isEnabled = false
        tv_country.text = viewModel?.inputWrapper?.countryName.toString()

        viewModel?.submitBeneficiaryDetails?.observe(
            viewLifecycleOwner, Observer {
                et_state.setText(it?.state.toString())
            }
        )


        if (viewModel?.clearData ?: return) {
            tv_country.text = null
            et_state.text?.clear()
        }

        tv_country.text = viewModel?.inputWrapper?.countryName.toString()

        val mActivity = getActivity() as BaseActivity
        val user = UserPreferences.instance.getUser(mActivity) ?: return
        val wrapper = viewModel?.inputWrapper ?: return
        viewModel?.getPayoutAgentBranchLocations(
            user.token, user.sessionId, user.refreshToken, user.customerId.toLong(),
            ExchangeContainer.accessKey, viewModel?.payoutAgentId
                ?: return, wrapper.deliveryMode ?: return,
            if (viewModel?.inputWrapper?.state == DEFAULT_STATE) "" else viewModel?.inputWrapper?.state?.toUpperCase() ?: ""
        ).also { payoutAgentBranchesObserver() }

        btn_next.setOnClickListener {
            val activity = activity ?: return@setOnClickListener
            if (isValid()) {
                //if (viewModel?.inputWrapper?.state == DEFAULT_STATE) viewModel?.inputWrapper?.state = null
                onSuccess(activity)
            }
        }
    }


    private fun onSuccess(activity: BaseActivity) {
        when (activity) {
            is CashPayoutActivity -> activity.navigateUp().also {
                viewModel?.submitBeneficiary()
            }
        }
    }

    companion object {
        const val KEY_TYPE = "type"
        const val TYPE_BANK = 7
        const val TYPE_LOCATION = 8
        const val DEFAULT_STATE = "All States"

        fun newInstance(type: Int): SearchAgentFragment {
            return SearchAgentFragment().apply {
                val bundle = Bundle()
                bundle.putInt(KEY_TYPE, type)
                arguments = bundle
            }
        }
    }

    private fun loadPayoutAgents() {

        val payoutCountries = viewModel?.payOutCountries ?: return

        val result =
            payoutCountries.filterNot { m -> m.deliveryModes.isEmpty() || m.deliveryModes.all { it.code == DeliveryModes.BT } }

        showPayoutCountries(cashPayoutActivity
            ?: return, result, object : OnPayoutCountrySelectListener {
            override fun onPayoutCountrySelect(payoutCountry: PayoutCountries) {

                if (!payoutCountry.deliveryModes.isNullOrEmpty()) {
                    viewModel?.inputWrapper?.countryName = payoutCountry.countryName
                    viewModel?.inputWrapper?.countryCode = payoutCountry.countryCode
                    viewModel?.payOutCurrencies = payoutCountry.payOutCurrencies
                    val deliveryModes = payoutCountry.deliveryModes.filter { deliveryMode ->
                        deliveryMode.code == DeliveryModes.CP
                    }
                    // TODO handle on next button , show dialog this choose another country
                    if (!deliveryModes.isNullOrEmpty())
                        viewModel?.inputWrapper?.deliveryMode = deliveryModes[0].code

                    tv_country.text = payoutCountry.countryName

                    val mActivity = getActivity() as BaseActivity
                    val user = UserPreferences.instance.getUser(mActivity) ?: return
                    val wrapper = viewModel?.inputWrapper ?: return
                    viewModel?.getPayoutAgents(
                        user.token, user.sessionId, user.refreshToken, user.customerId.toLong(),
                        ExchangeContainer.accessKey, wrapper.countryCode
                            ?: return, wrapper.deliveryMode
                            ?: return
                    ).also { payoutAgentsObserver() }
                }
            }
        })
    }

    private fun payoutAgentsObserver() {
        viewModel?.payoutAgentState?.observe(viewLifecycleOwner, Observer {
            it ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            val activity = getActivity() as BaseActivity
            if (state is NetworkState2.Loading) {
                progress_bar.visibility = View.VISIBLE
                return@Observer
            }

            progress_bar.visibility = View.GONE
            when (state) {
                is NetworkState2.Success -> {
                    state.data?.let { data -> loadBankName(data) }
                }
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        activity.onSessionExpired(state.message)
                        return@Observer
                    }
                    activity.handleErrorCode(state.errorCode.toInt(), state.message)
                }
                is NetworkState2.Failure -> activity.onFailure(
                    activity.findViewById(R.id.root_view), getString(R.string.request_error)
                )
                else -> activity.onFailure(
                    activity.findViewById(R.id.root_view), getString(R.string.request_error)
                )
            }
        })
    }

    private fun payoutAgentBranchesObserver() {
        viewModel?.payoutAgentBranchesLocation?.observe(viewLifecycleOwner, Observer {
            it ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            val activity = getActivity() as BaseActivity
            if (state is NetworkState2.Loading) {
                progress_bar.visibility = View.VISIBLE
                return@Observer
            }
            progress_bar.visibility = View.GONE
            when (state) {
                is NetworkState2.Success -> {
                    state.data?.let { data -> pickUpBranchLocation(data) }
                }
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        activity.onSessionExpired(state.message)
                        return@Observer
                    }
                    activity.handleErrorCode(state.errorCode.toInt(), state.message)
                }
                is NetworkState2.Failure -> activity.onFailure(
                    activity.findViewById(R.id.root_view),
                    getString(R.string.request_error)
                )
                else -> activity.onFailure(
                    activity.findViewById(R.id.root_view),
                    getString(R.string.request_error)
                )
            }
        })
    }

    private fun payoutAgentStateObserver() {
        viewModel?.payoutAgentBranchesState?.observe(viewLifecycleOwner, Observer {
            it ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            val activity = getActivity() as BaseActivity
            if (state is NetworkState2.Loading) {
                return@Observer
            }
            when (state) {
                is NetworkState2.Success -> {
                    state.data?.let { data ->
                        stateData = data
                        showStateDialog(data)
                    }
                }
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        activity.onSessionExpired(state.message)
                        return@Observer
                    }
                    activity.handleErrorCode(state.errorCode.toInt(), state.message)
                }
                is NetworkState2.Failure -> activity.onFailure(
                    activity.findViewById(R.id.root_view),
                    getString(R.string.request_error)
                )
                else -> activity.onFailure(
                    activity.findViewById(R.id.root_view),
                    getString(R.string.request_error)
                )
            }
        })

    }


    private fun loadBankName(list: List<PayoutAgent>) {
        rv_locations.visibility = View.VISIBLE
        val list2 = list.map {
            PickUpBankName(it.payOutAgentName, it.payOutCurrency, it.payOutAgentId)
        }
        pickUpBankName.addAll(list2)
        val adapter = PickUpBankAdapter(
                list2.filter { it.bankDetails.equals(viewModel?.inputWrapper?.currency, true) }, pickUpBankNameListener,
            if (list2.size == 1) 0 else -1
        )
        rv_locations.adapter = adapter
        btn_next.visibility = View.VISIBLE
        btn_next.backgroundTintList = resources.getColorStateList(R.color.btn_primary_color)
        btn_next.setOnClickListener {
            val activity = activity ?: return@setOnClickListener
            if (isValid() && type == TYPE_BANK) {
                viewModel?.inputWrapper?.state = et_state.text.toString()
                onSuccess(activity)
            } else if (type == TYPE_LOCATION) {
                onSuccess(activity)

            }
        }


        img_search.setOnClickListener {
            val itemCount = adapter.filter(et_bank_name.text.toString().trim()).count()
            when {
                itemCount < 1 -> {
                    rv_locations.visibility = View.GONE
                    tv_not_available.visibility = View.VISIBLE
                }
                else -> {
                    rv_locations.visibility = View.VISIBLE
                    tv_not_available.visibility = View.GONE
                }
            }

        }

        et_bank_name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                s?.let {
                    val itemCount = adapter.filter(it.toString()).count()
                    when {
                        itemCount < 1 -> {
                            rv_locations.visibility = View.GONE
                            tv_not_available.visibility = View.VISIBLE
                        }
                        else -> {
                            rv_locations.visibility = View.VISIBLE
                            tv_not_available.visibility = View.GONE
                        }
                    }
                }
            }
        })

        if (list2.size == 1) {
            val selected = list2[0]
            pickUpBankNameListener.onBankSelect(selected)
        }
    }

    private val pickUpBankNameListener = object : OnPickBankNameListener {
        override fun onBankSelect(pickUpBankName: PickUpBankName) {
            viewModel?.clearBankData = true
            viewModel?.inputWrapper?.bankName = pickUpBankName.bankName
            viewModel?.payoutAgentId = pickUpBankName.payOutAgentId.toLong()
            et_bank_name.setText(pickUpBankName.bankName)
            val user = UserPreferences.instance.getUser(context ?: return) ?: return
            val wrapper = viewModel?.inputWrapper ?: return
            flag = true
            viewModel?.getPayoutAgentBranches(
                user.token, user.sessionId, user.refreshToken, user.customerId.toLong(),
                ExchangeContainer.accessKey, viewModel?.payoutAgentId
                    ?: return, wrapper.deliveryMode ?: return
            ).also { payoutAgentStateObserver() }

        }
    }

    private fun isValid(): Boolean {
        when (type) {
            TYPE_BANK -> {
                if (tv_country.text.isNullOrEmpty()) {
                    setError(getString(R.string.choose_country), tv_country)
                    return false
                }
                if(TextUtils.isEmpty(et_bank_name.text.toString()) || pickUpBankName.none { name ->
                        et_bank_name.text.toString().equals(name.bankName, true)
                    }){
                    setError(resources.getString(R.string.error_bank_name1),et_bank_name)
                    return false
                }

                if (viewModel?.payoutAgentId == null) {
                    textInputLayout_pickup_location.setErrorMessage(getString(R.string.select_bank))
                    return false
                }

                if (et_state.text.isNullOrEmpty()) {
                    textInputLayout_state.setErrorMessage(getString(R.string.please_select_state))
                    et_state.requestFocus()
                    return false
                }

            }
            TYPE_LOCATION -> {
                if (et_state.text.isNullOrEmpty()) {
                    textInputLayout_state.setErrorMessage(getString(R.string.please_select_state))
                    et_state.requestFocus()
                    return false
                }
                if(TextUtils.isEmpty(et_bank_name.text.toString())|| pickUpLocation.none { name ->
                        et_bank_name.text.toString().equals(name.locationName, true)
                    }){
                    setError(resources.getString(R.string.select_location),et_bank_name)
                    return false
                }
                if (viewModel?.payoutBranchId == null) {
                    textInputLayout_pickup_location.setErrorMessage(getString(R.string.select_location))
                    return false
                }
            }
            else -> {
                textInputLayout_state.clearErrorMessage()
                textInputLayout_pickup_location.clearErrorMessage()
                return true
            }
        }
        return true
    }

    private fun addTextWatcherListener(editText: EditText, textInputLayout: TextInputLayout) {
        editText.onTextChanged { _, _, _, _ -> textInputLayout.clearErrorMessage() }

    }

    private fun pickUpBranchLocation(branches: List<PayoutAgentBranches>) {
        rv_locations.visibility = View.VISIBLE
        val list = branches.map {
            PickUpLocation(it.payOutBranchName, it.payOutBranchAddress, it.payOutBranchId)
        }
        pickUpLocation.addAll(list)
        val adapter = PickUpLocationAdapter(
            list, pickUpLocationListener,
            if (list.size == 1) 0 else -1
        )
        rv_locations.adapter = adapter



        img_search.setOnClickListener {
            val itemCount = adapter.filter(et_bank_name.text.toString().trim()).count()
            when {
                itemCount < 1 -> {
                    rv_locations.visibility = View.GONE
                    tv_not_available.visibility = View.VISIBLE
                }
                else -> {
                    rv_locations.visibility = View.VISIBLE
                    tv_not_available.visibility = View.GONE
                }
            }
        }

        et_bank_name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                s?.let {
                    val itemCount = adapter.filter(it.toString()).count()
                    when {
                        itemCount < 1 -> {
                            rv_locations.visibility = View.GONE
                            tv_not_available.visibility = View.VISIBLE
                        }
                        else -> {
                            rv_locations.visibility = View.VISIBLE
                            tv_not_available.visibility = View.GONE
                        }
                    }
                }
            }

        })

        if (list.size == 1) {
            val selected = list[0]
            pickUpLocationListener.onLocationSelect(selected)
        }
    }


    private val pickUpLocationListener = object : OnPickUpLocationListener {
        override fun onLocationSelect(pickUpLocation: PickUpLocation) {
            viewModel?.clearLocationData = true
            viewModel?.inputWrapper?.branchName = pickUpLocation.locationName
            viewModel?.inputWrapper?.branchAddress = pickUpLocation.locationDetails
            viewModel?.payoutBranchId = pickUpLocation.payOutBranchId
            et_bank_name.setText(pickUpLocation.locationName)
        }
    }


    private fun setError(message: String, textView: TextView) {
        textView.background = ContextCompat.getDrawable(
            this.context ?: return,
            R.drawable.bg_grey_red_border
        )
        activity?.onFailure(activity?.findViewById(R.id.root_view) ?: return, message)
    }

    private fun showStateDialog(list: List<PayoutAgentBranches>) {
        if (list.isEmpty()) return
        val statesName = ArrayList<IsoAlpha3>()
        list.filter { item ->

            if (item.payOutBranchState.isNullOrEmpty()){
                et_state.setText(DEFAULT_STATE)
                return
            }
            statesName.add(IsoAlpha3(item.payOutBranchState, ""))
        }
        val filteredState = statesName.distinct()
        if (filteredState.size == 1) {
            flag = true
            et_state.setText(filteredState[0].country)
            return
        }
        if (flag) return
        showCountries(cashPayoutActivity
            ?: return,
            filteredState,
            getString(R.string.select_state),
            object : OnCountrySelectListener {
                override fun onCountrySelect(countryName: IsoAlpha3) {
                    et_state.setText(countryName.country)
                }

            },
            getString(R.string.no_state_found)
        )

    }

}
