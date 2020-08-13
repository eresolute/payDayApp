package com.fh.payday.views2.intlRemittance.mybeneficiaries.addbeneficiary.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import com.fh.payday.BaseFragment
import com.fh.payday.R
import com.fh.payday.datasource.models.IsoAlpha3
import com.fh.payday.datasource.models.intlRemittance.AlFardanIntlAddBeneficiary
import com.fh.payday.datasource.models.intlRemittance.PayoutAgent
import com.fh.payday.datasource.models.intlRemittance.PayoutAgentBranches
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.OnCountrySelectListener
import com.fh.payday.utilities.clearErrorMessage
import com.fh.payday.utilities.onTextChanged
import com.fh.payday.viewmodels.intlRemittance.AddBeneficiaryViewModel
import com.fh.payday.views.custombindings.setDrawableLeft
import com.fh.payday.views2.intlRemittance.DeliveryModes
import com.fh.payday.views2.intlRemittance.ExchangeContainer
import com.fh.payday.views2.intlRemittance.mybeneficiaries.addbeneficiary.AddBeneficiaryActivity
import com.fh.payday.views2.shared.custom.showCountries
import kotlinx.android.synthetic.main.fragment_bank_details.*
import kotlinx.android.synthetic.main.fragment_beneficiary_details.*

class BeneficiaryBankDetailsFragment : BaseFragment() {

    enum class Favourite { DEFAULT, FAVOURITE }

    private val viewModel by lazy {
        activity?.let { ViewModelProviders.of(it).get(AddBeneficiaryViewModel::class.java) }
    }

    private val user by lazy { activity?.let { UserPreferences.instance.getUser(it) } }

    private var deliveryMode: String? = null
    private var beneficiaryActivity: AddBeneficiaryActivity? = null
    private var stateData: List<PayoutAgentBranches>? = null
    private var payoutAgentBranches: List<PayoutAgentBranches>? = null
    private var stateSizeOne: Boolean = false
    private var bankNames: List<PayoutAgent>? = null
    private var states = ArrayList<IsoAlpha3>()
    private var cityList = ArrayList<IsoAlpha3>()
    private var branchName = ArrayList<IsoAlpha3>()
    private var branchAddress = ArrayList<IsoAlpha3>()
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        beneficiaryActivity = context as AddBeneficiaryActivity
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if (isVisibleToUser && viewModel?.clearBankDetails ?: return) {
            clearDetails()
            et_bank_name.text?.clear()
            et_account_number.text?.clear()
            handleVisibility(View.VISIBLE)
        }
    }

    override fun onDetach() {
        super.onDetach()
        beneficiaryActivity = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bank_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = UserPreferences.instance.getUser(requireContext()) ?: return


        textInputLayout_bank_name.hint = null
        et_bank_name.hint = resources.getString(R.string.bank_name)
        textInputLayout_state.hint = null
        et_state.hint = resources.getString(R.string.state)
        textInputLayout_city.hint = null
        et_city.hint = resources.getString(R.string.city)
        textInputLayout_branch_name.hint = null
        et_branch_name.hint = resources.getString(R.string.branch_name)
        textInputLayout_branch_address.hint = null
        et_branch_address.hint = resources.getString(R.string.branch_address)
        textInputLayout_account_number.hint = null
        et_account_number.hint = resources.getString(R.string.account_no_label)
        textInputLayout_ifsc_code.hint = null
        et_ifsc_code.hint = resources.getString(R.string.ifsc_code)
        textInputLayout_swift_code.hint = null
        et_swift_code.hint = resources.getString(R.string.swift_code)
        textInputLayout_iban.hint = null
        et_iban.hint = resources.getString(R.string.name_of_bank)

        btn_submit.setOnClickListener {
            if (validate()) {
                saveDetails()
                viewModel?.generateOtp(
                    user.token,
                    user.sessionId,
                    user.refreshToken,
                    user.customerId.toLong()
                )
            }

        }

        //todo remove hard coded value for access key

        tv_favourite_list.setOnClickListener {
            when (viewModel?.flag) {
                Favourite.DEFAULT.ordinal -> {
                    viewModel?.flag = 1
                    tv_favourite_list.text = getString(R.string.beneficiary_added_to_fav)
                    setDrawableLeft(this.tv_favourite_list, R.drawable.ic_fav_icon2x)
                }
                Favourite.FAVOURITE.ordinal -> {
                    viewModel?.flag = 0
                    tv_favourite_list.text =
                        getString(R.string.add_beneficiary_to_my_favourite_list)
                    setDrawableLeft(this.tv_favourite_list, R.drawable.ic_add_fav)
                }
            }
        }

        et_bank_name.setOnItemClickListener { parent, _, position, _ ->
            val payoutAgent = parent.adapter.getItem(position)
                    as? PayoutAgent? ?: return@setOnItemClickListener
            val country = payoutAgent.payOutAgentName
            clearDetails()
            handleVisibility(View.VISIBLE)
            et_bank_name.setText(country)
            stateSizeOne = false
            callStateApi(payoutAgent)
        }

        et_state.setOnItemClickListener { _, _, _, _ ->
            et_branch_name.text?.clear()
            et_branch_address.text?.clear()
            et_ifsc_code.text?.clear()
            et_iban.text?.clear()
            et_city.text?.clear()
            et_account_number.text?.clear()
            et_swift_code.text?.clear()
            showCities()
        }

        et_city.setOnItemClickListener { parent, _, position, _ ->
            val isoAlpha3 = parent.adapter.getItem(position)
                    as? IsoAlpha3? ?: return@setOnItemClickListener
            val city = isoAlpha3.country
            et_city.setText(city)
            et_branch_name.text?.clear()
            et_branch_address.text?.clear()
            et_ifsc_code.text?.clear()
            et_iban.text?.clear()
            et_account_number.text?.clear()
            et_swift_code.text?.clear()
            showBranchName()
        }

        et_branch_name.setOnItemClickListener { parent, _, position, _ ->
            val isoAlpha3 = parent.adapter.getItem(position)
                    as? IsoAlpha3? ?: return@setOnItemClickListener
            val branchName = isoAlpha3.country
            et_branch_name.setText(branchName)
            payoutAgentBranches?.forEach {
                if (it.payOutBranchName == isoAlpha3.country) {
                    et_branch_address.setText(it.payOutBranchAddress)
                    et_branch_address.isEnabled = false
                    et_account_number.text?.clear()
                    if (viewModel?.inputWrapper?.countryName.equals("India", true)) {
                        when {
                            !it.ifscCode.isNullOrEmpty() -> et_ifsc_code.isEnabled = false
                            else -> {
                                et_ifsc_code.isEnabled = true
                            }
                        }
                    } else {
                        when {
                            !it.swiftCode.isNullOrEmpty() -> et_swift_code.isEnabled = false
                            else -> {
                                et_swift_code.isEnabled = true
                            }
                        }
                        when {
                            !it.ibanNo.isNullOrEmpty() -> et_iban.isEnabled = false
                            else -> {
                                et_iban.isEnabled = true
                            }
                        }
                    }
                    viewModel?.payoutBranchId = it.payOutBranchId
                }
            }
        }

        et_branch_address.setOnItemClickListener { parent, _, position, _ ->
            val isoAlpha3 = parent.adapter.getItem(position)
                    as? IsoAlpha3? ?: return@setOnItemClickListener
            val branchAddress = isoAlpha3.country
            et_branch_address.setText(branchAddress)
            payoutAgentBranches?.forEach {
                if (it.payOutBranchAddress == isoAlpha3.country) {
                    et_swift_code.setText(it.swiftCode)
                    et_iban.setText(it.ibanNo)
                    et_ifsc_code.setText(it.ifscCode)
                    viewModel?.payoutBranchId = it.payOutBranchId
                }
            }
        }
        addTextWatcherListener(et_bank_name, textInputLayout_bank_name)
        addTextWatcherListener(et_city, textInputLayout_city)
        addTextWatcherListener(et_state, textInputLayout_state)
        addTextWatcherListener(et_branch_name, textInputLayout_branch_name)
        addTextWatcherListener(et_branch_address, textInputLayout_branch_address)
        addTextWatcherListener(et_account_number, textInputLayout_account_number)
        addTextWatcherListener(et_ifsc_code, textInputLayout_ifsc_code)
        addTextWatcherListener(et_swift_code, textInputLayout_swift_code)
        addTextWatcherListener(et_iban, textInputLayout_iban)

        /*     et_city.setOnFocusChangeListener { view, b ->
                 if (b)
                     et_city.showDropDown()
             }
             et_state.setOnFocusChangeListener { view, b ->
                 if (b)
                     et_state.showDropDown()
             }
             et_bank_name.setOnFocusChangeListener { view, b ->
                 if (b)
                     et_bank_name.showDropDown()
             }

             et_branch_name.setOnFocusChangeListener { view, b ->
                 if(b)
                 et_branch_name.showDropDown()
             }

             et_branch_address.setOnFocusChangeListener { view, b ->
                 if(b)
                 et_branch_address.showDropDown()
             }*/

        et_city.setOnClickListener {
            et_city.showDropDown()
        }
        et_state.setOnClickListener {
            et_state.showDropDown()
        }
        et_bank_name.setOnClickListener {
            et_bank_name.showDropDown()
        }

        et_branch_name.setOnClickListener {
            et_branch_name.showDropDown()
        }

        et_branch_address.setOnClickListener {
            et_branch_address.showDropDown()
        }
        addObserver()
    }

    private fun callStateApi(payoutAgent: PayoutAgent) {
        /*      val payoutAgents = viewModel?.payoutAgents
                  ?: return
              val payOutAgentFilter =
                  payoutAgents.filter { bankName.equals(it.payOutAgentName, true) }
              val payOutAgentId = payOutAgentFilter.map { it.payOutAgentId }.distinct().single()*/
        viewModel?.payoutAgentId = payoutAgent.payOutAgentId.toLong()
        viewModel?.getPayoutAgentBranches(
            user?.token ?: return, user?.sessionId ?: return,
            user?.refreshToken ?: return, user?.customerId?.toLong() ?: return,
            ExchangeContainer.accessKey, payoutAgent.payOutAgentId.toLong(),
            deliveryMode ?: return
        )
        if (!viewModel?.inputWrapper?.countryName.equals("india", true))
        viewModel?.getAccountDetails(
            user?.token ?: return,
            user?.sessionId ?: return,
            user?.refreshToken ?: return,
            user?.customerId?.toLong() ?: return,
            payoutAgent.payOutAgentId.toString(),
            deliveryMode ?: return,
            "Account Details"
        )
    }

    private fun showCities() {
        cityList = ArrayList()
        val payoutAgentBranches = viewModel?.payoutAgentBranches
            ?: return
        if (payoutAgentBranches.isNullOrEmpty()) {
            cityList.add(IsoAlpha3("N/A", ""))
            et_city.setText("N/A")
            showBranchName()
            handleVisibility(View.GONE)
            return
        }
        val selectedState =
            if (et_state.text.toString().isEmpty()) return else et_state.text.toString().trim()
        val filteredCities =
            payoutAgentBranches.filter {
                if (it.payOutBranchState.isNullOrEmpty()) {
                    cityList.add(IsoAlpha3("N/A", ""))
                    et_city.setText("N/A")
                    handleVisibility(View.GONE)
                    showBranchName()
                    return
                }
                selectedState.equals(it.payOutBranchState, true)
            }


        if (!filteredCities.isNullOrEmpty()) {
            val cities = ArrayList<IsoAlpha3>()
            filteredCities.map { item ->
                if (item.payOutBranchCity.isNullOrEmpty()) {
                    et_city.setText("N/A")
                    handleVisibility(View.GONE)
                    showBranchName()
                    return
                }
                cities.add(IsoAlpha3(item.payOutBranchCity, ""))
            }.distinct()


            cities.distinct().filter { country ->
                cityList.add(
                    IsoAlpha3(
                        country.country, ""
                    )
                )
            }
            if (cityList.size == 1) {
                et_city.setText(cityList[0].country)
                showBranchName()
                et_city.isEnabled = false
                return
            }
            et_city.isEnabled = true
            if (cities.size != 1) {
                val cityAdapter =
                    ArrayAdapter(requireContext(), R.layout.layout_autcompletetext, cityList)
                et_city.setAdapter(cityAdapter)
            }
        }
    }

    private fun showBranchName() {
        payoutAgentBranches = viewModel?.payoutAgentBranches
        if (payoutAgentBranches.isNullOrEmpty()) {
            handleVisibility(View.GONE)
            et_branch_name.setText("N/A")
            et_branch_address.setText("N/A")
            et_branch_name.isEnabled = false
            et_branch_address.isEnabled = false
            return
        }
        val selectedState =
            if (et_state.text.toString().isEmpty()) return else et_state.text.toString().trim()
        val selectedCity =
            if (et_city.text.toString().isEmpty()) return else et_city.text.toString().trim()
        var filteredBranchName = payoutAgentBranches?.filter {
            selectedState.equals(
                it.payOutBranchState,
                true
            ) && selectedCity.equals(it.payOutBranchCity, true)
        }



        if (filteredBranchName.isNullOrEmpty()) {
            branchName = ArrayList()
            branchAddress = ArrayList()
            if (payoutAgentBranches?.size == 1) {
                et_branch_name.isEnabled = false
                et_branch_address.isEnabled = false
                viewModel?.payoutBranchId = payoutAgentBranches?.get(0)?.payOutBranchId
                if (payoutAgentBranches?.get(0)?.payOutBranchName.isNullOrEmpty()) {
                    branchName.add(
                        IsoAlpha3(
                            "N/A",
                            ""
                        )
                    )
                    et_branch_name.setText("N/A")
                } else {
                    val mBankName = if (payoutAgentBranches?.get(0)?.payOutBranchName.equals(
                            "Anywhere Payout",
                            true
                        ) || (payoutAgentBranches?.get(0)?.payOutBranchName.equals(
                            "Payout Anywhere",
                            true
                        )) || (payoutAgentBranches?.get(0)?.payOutBranchName.equals(
                            "Based on Swift Code",
                            true
                        ))
                    ) "All Branches" else payoutAgentBranches?.get(0)?.payOutBranchName
                        ?: "All Branches"
                    branchName.add(
                        IsoAlpha3(
                            mBankName,
                            ""
                        )
                    )
                    et_branch_name.setText(mBankName)
                    branchName.add(IsoAlpha3(mBankName, ""))
                }
                if (payoutAgentBranches?.get(0)?.payOutBranchAddress.isNullOrEmpty()) {
                    handleVisibility(View.GONE)
                    branchAddress.add(
                        IsoAlpha3(
                            "N/A", ""
                        )
                    )
                    et_branch_address.setText("N/A")
                } else {
                    branchAddress.add(
                        IsoAlpha3(
                            payoutAgentBranches?.get(0)?.payOutBranchAddress ?: "N/A", ""
                        )
                    )
                    et_branch_address.setText(payoutAgentBranches?.get(0)?.payOutBranchAddress)
                }
            } else {
                et_branch_name.isEnabled = true
                et_branch_address.isEnabled = true

                payoutAgentBranches?.map { item ->
                    branchName.add(IsoAlpha3(item.payOutBranchName, ""))
                    if (item.payOutBranchAddress != null)
                        branchAddress.add(IsoAlpha3(item.payOutBranchAddress, ""))
                }?.distinct()

                val branchAdapter =
                    ArrayAdapter(requireContext(), R.layout.layout_autcompletetext, branchName)
                et_branch_name.setAdapter(branchAdapter)

                /*val branchAddressAdapter =
                    ArrayAdapter(requireContext(), R.layout.layout_autcompletetext, branchAddress)
                et_branch_address.setAdapter(branchAddressAdapter)*/
            }
            return
        }
        filteredBranchName = filteredBranchName.distinct()

        if (filteredBranchName.size == 1) {
            et_branch_name.setText(filteredBranchName[0].payOutBranchName)
            et_branch_address.setText(filteredBranchName[0].payOutBranchAddress)
            viewModel?.payoutBranchId = filteredBranchName[0].payOutBranchId
            branchName.add(IsoAlpha3(filteredBranchName[0].payOutBranchName, ""))
            et_branch_name.isEnabled = false
            et_branch_address.isEnabled = false
            return
        }

        if (!filteredBranchName.isNullOrEmpty()) {
            et_branch_name.isEnabled = true
            et_branch_address.isEnabled = true
            branchName = ArrayList()
            branchAddress = ArrayList()
            filteredBranchName.map { item ->
                branchName.add(IsoAlpha3(item.payOutBranchName, ""))
                if (item.payOutBranchAddress != null)
                    branchAddress.add(IsoAlpha3(item.payOutBranchAddress, ""))
            }.distinct()

            val branchAdapter =
                ArrayAdapter(requireContext(), R.layout.layout_autcompletetext, branchName)
            et_branch_name.setAdapter(branchAdapter)

            val branchAddressAdapter =
                ArrayAdapter(requireContext(), R.layout.layout_autcompletetext, branchAddress)
            et_branch_address.setAdapter(branchAddressAdapter)
        }
    }

    private fun showBranchAddress() {
        val payoutAgentBranches = beneficiaryActivity?.viewModel?.payoutAgentBranches
        if (payoutAgentBranches.isNullOrEmpty()) {
            et_branch_address.setText("N/A")
            return
        }
        val selectedState =
            if (et_state.text.toString().isEmpty()) return else et_state.text.toString().trim()
        val selectedCity =
            if (et_city.text.toString().isEmpty()) return else et_city.text.toString().trim()
        val filteredBranchName = payoutAgentBranches.filter {
            if (it.payOutBranchState.isNullOrEmpty()) {
                viewModel?.payoutBranchId = payoutAgentBranches[0].payOutBranchId
                et_branch_address.setText("N/A")
                return
            }
            selectedState.equals(
                it.payOutBranchState,
                true
            ) && selectedCity.equals(it.payOutBranchCity, true)
        }

        if (!filteredBranchName.isNullOrEmpty()) {
            val cities = ArrayList<IsoAlpha3>()
            filteredBranchName.map { item ->
                if (item.payOutBranchName.equals(et_branch_name.text.toString(), true)) {
                    if (item.payOutBranchAddress != null)
                        cities.add(IsoAlpha3(item.payOutBranchAddress, ""))
                }
            }.distinct()

            showCountries(
                beneficiaryActivity
                    ?: return,
                cities,
                getString(R.string.select_branch_address),
                object : OnCountrySelectListener {
                    override fun onCountrySelect(countryName: IsoAlpha3) {
                        et_branch_address.setText(countryName.country)
                        // et_branch_name.setText(countryName.code)
                        payoutAgentBranches.forEach {
                            if (it.payOutBranchAddress == countryName.country) {
                                et_swift_code.setText(it.swiftCode)
                                et_iban.setText(it.ibanNo)
                                et_ifsc_code.setText(it.ifscCode)
                                viewModel?.payoutBranchId = it.payOutBranchId
                            }
                        }
                    }
                },
                getString(R.string.no_branch_address_found)
            )
        }

    }

    private fun saveDetails() {
        viewModel?.inputWrapper?.bankName = et_bank_name.text.toString()
        viewModel?.inputWrapper?.city = et_city.text.toString()
        viewModel?.inputWrapper?.state = et_state.text.toString()
        viewModel?.inputWrapper?.branchName = et_branch_name.text.toString()
        viewModel?.inputWrapper?.branchAddress = et_branch_address.text.toString()
        viewModel?.inputWrapper?.accountNo = et_account_number.text.toString()
        viewModel?.inputWrapper?.ifscCode = et_ifsc_code.text.toString()
        viewModel?.inputWrapper?.swiftCode = et_swift_code.text.toString()
        viewModel?.inputWrapper?.iBan = et_iban.text.toString()
    }

    private fun clearDetails() {
        et_city.text?.clear()
        et_state.text?.clear()
        et_branch_name.text?.clear()
        et_branch_address.text?.clear()
        et_ifsc_code.text?.clear()
        et_iban.text?.clear()
        et_swift_code.text?.clear()
        et_account_number.text?.clear()
    }

    private fun validate(): Boolean {
        return when {
            et_bank_name.text.toString().isEmpty() -> {
                textInputLayout_bank_name.error = getString(R.string.select_bank_name)
                et_bank_name.requestFocus()
                false
            }

            bankNames!!.isNotEmpty() && bankNames!!.none { name ->
                et_bank_name.text.toString().equals(name.payOutAgentName, true)
            } -> {
                textInputLayout_bank_name.error = getString(R.string.select_bank_name)
                return false
            }

            et_state.text.toString().isEmpty() -> {
                textInputLayout_state.error = getString(R.string.select_state)
                et_state.requestFocus()
                false
            }
            states.none { name -> et_state.text.toString().equals(name.country, true) } -> {
                textInputLayout_state.error = getString(R.string.select_state)
                return false
            }
            et_city.text.toString().isEmpty() -> {
                textInputLayout_city.error = getString(R.string.select_city)
                et_city.requestFocus()
                false
            }
            cityList.isNotEmpty() && cityList.none { name ->
                et_city.text.toString().equals(name.country, true)
            } -> {
                textInputLayout_city.error = getString(R.string.select_city)
                return false
            }

            branchName.isNotEmpty() && branchName.none { name ->
                et_branch_name.text.toString().equals(name.country, true)
            } -> {
                textInputLayout_branch_name.error = getString(R.string.branch_name_error)
                et_branch_name.requestFocus()
                false
            }

            /*  textInputLayout_branch_address.visibility == View.VISIBLE && branchAddress.isNotEmpty() && branchAddress.none { name ->
                  et_branch_address.text.toString().equals(name.country, true)
              } -> {
                  textInputLayout_branch_address.error = getString(R.string.branch_address_error)
                  et_branch_address.requestFocus()
                  false
              }*/

            textInputLayout_branch_address.visibility == View.VISIBLE && et_branch_address.text.toString()
                .isEmpty() -> {
                textInputLayout_branch_address.error = getString(R.string.branch_address_error)
                et_branch_address.requestFocus()
                false
            }
            et_account_number.text.toString().isEmpty() -> {
                textInputLayout_account_number.error = getString(R.string.empty_account_no)
                et_account_number.requestFocus()
                false
            }
            viewModel!!.inputWrapper.countryName.equals("India", true) -> {
                if (TextUtils.isEmpty(et_ifsc_code.text.toString())) {
                    textInputLayout_ifsc_code.error = getString(R.string.enter_ifsc_code)
                    false
                } else true
            }
            textInputLayout_swift_code.visibility == View.VISIBLE &&  et_swift_code.text.toString().isEmpty() -> {
                textInputLayout_swift_code.error = getString(R.string.empty_swift_code)
                et_swift_code.requestFocus()
                false
            }
            !viewModel!!.inputWrapper.countryName.equals("India") -> {
                if (textInputLayout_iban.visibility == View.VISIBLE && TextUtils.isEmpty(et_iban.text.toString())) {
                    textInputLayout_iban.error = getString(R.string.enter_iban)
                    false
                } else true
            }
            /* et_account_number.text.toString().length < 16 -> {
                 textInputLayout_account_number.error = "Invalid Account Number"
                 et_account_number.requestFocus()
                 false
             }*/
            /*      et_ifsc_code.text.toString().isEmpty() -> {
                      textInputLayout_ifsc_code.error = getString(R.string.empty_ifsc_code)
                      et_ifsc_code.requestFocus()
                      false
                  }
                  et_swift_code.text.toString().isEmpty() -> {
                      textInputLayout_swift_code.error = getString(R.string.empty_swift_code)
                      et_swift_code.requestFocus()
                      false
                  }
                  et_iban.text.toString().isEmpty() -> {
                      textInputLayout_iban.error = getString(R.string.empty_iban)
                      et_iban.requestFocus()
                      false
                  }*/

            else -> true
        }
    }

    private fun addTextWatcherListener(editText: EditText, textInputLayout: TextInputLayout) {
        editText.onTextChanged { _, _, _, _ -> textInputLayout.clearErrorMessage() }
    }

    private fun handleVisibility(visibility: Int) {
        textInputLayout_city.visibility = visibility
        textInputLayout_state.visibility = visibility
        textInputLayout_branch_address.visibility = visibility
    }

    private fun addObserver() {

        viewModel?.generateOtpState?.observe(this, Observer {
            it ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                beneficiaryActivity?.showProgress(getString(R.string.loading))
                return@Observer

            }
            beneficiaryActivity?.hideProgress()

            when (state) {
                is NetworkState2.Success -> {
                    viewModel?.clearBankDetails = false
                    beneficiaryActivity?.navigateUp()
                }

                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        beneficiaryActivity?.onSessionExpired(state.message)
                        return@Observer
                    }
                    beneficiaryActivity?.handleErrorCode(state.errorCode.toInt(), state.message)
                }
                is NetworkState2.Failure -> beneficiaryActivity?.onFailure(
                    root_layout,
                    getString(R.string.request_error)
                )
                else -> beneficiaryActivity?.onFailure(
                    root_layout,
                    getString(R.string.request_error)
                )
            }
        })

        viewModel?.payoutAgentState?.observe(this, Observer {
            it ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                return@Observer

            }
            beneficiaryActivity?.hideProgress()

            when (state) {
                is NetworkState2.Success -> state.data?.let { data -> onSuccess(data) }
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        beneficiaryActivity?.onSessionExpired(state.message)
                        return@Observer
                    }
                    beneficiaryActivity?.handleErrorCode(state.errorCode.toInt(), state.message)
                }
                is NetworkState2.Failure -> beneficiaryActivity?.onFailure(
                    root_layout,
                    getString(R.string.request_error)
                )
                else -> beneficiaryActivity?.onFailure(
                    root_layout,
                    getString(R.string.request_error)
                )
            }
        })

        viewModel?.payoutAgentBranchesState?.observe(this, Observer {
            it ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                progress_bar_state.visibility = View.VISIBLE
                et_state.isEnabled = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    et_state.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0,
                        0,
                        0,
                        0
                    )
                } else
                    et_state.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        0,
                        0
                    )
                return@Observer
            }
            progress_bar_state.visibility = View.GONE
            et_state.isEnabled = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                et_state.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_arrow_down_grey,
                    0
                )
            } else
                et_state.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_arrow_down_grey,
                    0
                )

            when (state) {
                is NetworkState2.Success -> {
                    state.data ?: return@Observer
                    if (state.data.isEmpty()) return@Observer
                    stateData = state.data.distinct()
                    stateSizeOne = false
                    showStateDialog(state.data)
                }
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        beneficiaryActivity?.onSessionExpired(state.message)
                        return@Observer
                    }
                    beneficiaryActivity?.handleErrorCode(state.errorCode.toInt(), state.message)
                }
                is NetworkState2.Failure -> beneficiaryActivity?.onFailure(
                    root_layout,
                    getString(R.string.request_error)
                )
                else -> beneficiaryActivity?.onFailure(
                    root_layout,
                    getString(R.string.request_error)
                )
            }
        })

        viewModel?.submitBeneficiaryDetails?.observe(
            viewLifecycleOwner,
            Observer { onSubmitBeneficiaryDetails(it) })

        viewModel?.payoutAccountDetailsState?.observe(this, Observer {
            it ?: return@Observer

            val countryState = it.getContentIfNotHandled() ?: return@Observer
            if (countryState is NetworkState2.Loading) {
                progress_bar_account.visibility = View.VISIBLE
                return@Observer
            }
            progress_bar_account.visibility = View.GONE
            when (countryState) {
                is NetworkState2.Success -> {
                    val list = countryState.data ?: return@Observer
                    list.forEach {item->
                        if (item.code.equals("Account No", true))
                            textInputLayout_account_number.visibility = View.VISIBLE

                        if (item.code.equals("SWIFT Code", true))
                            textInputLayout_swift_code.visibility = View.VISIBLE

                        if (item.code.equals("Bank Name", true))
                            textInputLayout_iban.visibility = View.VISIBLE
                    }

                }

                is NetworkState2.Error -> {
                    if (countryState.isSessionExpired) {
                        activity?.onSessionExpired(countryState.message)
                        return@Observer
                    }
                    val (message) = countryState
                    activity?.handleErrorCode(countryState.errorCode.toInt(), message)
                }

                is NetworkState2.Failure -> {
                    activity?.onFailure(parent_view, countryState.throwable)
                }
            }
        })
    }

    private fun onSubmitBeneficiaryDetails(wrapper: AlFardanIntlAddBeneficiary?) {
        et_ifsc_code.isEnabled = wrapper?.countryName.equals("india", true)
        if (wrapper?.countryName.equals("india", true)) {
            textInputLayout_swift_code.visibility = View.GONE
            textInputLayout_iban.visibility = View.GONE
            textInputLayout_ifsc_code.visibility = View.VISIBLE
        } else {
            textInputLayout_ifsc_code.visibility = View.GONE
        }
        progress_bar1.visibility = View.VISIBLE
        val bankNameAdapter = ArrayAdapter(
            requireContext(),
            R.layout.layout_autcompletetext,
            ArrayList<PayoutAgent>()
        )
        et_bank_name.setAdapter(bankNameAdapter)
        //todo remove hard-coded value for access key
        val user = user ?: return
        deliveryMode = wrapper?.deliveryMode
        viewModel?.getPayoutAgents(
            user.token, user.sessionId, user.refreshToken,
            user.customerId.toLong(), ExchangeContainer.accessKey,
            wrapper?.countryCode ?: return,
            wrapper.deliveryMode ?: return
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun onSuccess(list: List<PayoutAgent>?) {
        if (list.isNullOrEmpty()) return
        bankNames = list.filter { it.payOutCurrency.equals(viewModel?.inputWrapper?.currency, true) }
        progress_bar1.visibility = View.GONE
        if (bankNames?.size == 1) {
            val country = bankNames?.get(0)?.payOutAgentName ?: return
            clearDetails()
            handleVisibility(View.VISIBLE)
            et_bank_name.setText(country)
            et_bank_name.isEnabled = false
            stateSizeOne = false
            callStateApi(bankNames!![0])
            return
        }
        et_bank_name.isEnabled = true
        val bankNameAdapter =
            ArrayAdapter(requireContext(), R.layout.layout_autcompletetext, bankNames!!)
        et_bank_name.setAdapter(bankNameAdapter)
    }

    private fun showStateDialog(list: List<PayoutAgentBranches>?) {
        if (list.isNullOrEmpty()) {
            states.add(IsoAlpha3("N/A", ""))
            et_state.setText("N/A")
            handleVisibility(View.GONE)
            showCities()
            return
        }
        val statesName = ArrayList<IsoAlpha3>()
        list.filter { item ->
            if (item.payOutBranchState.isNullOrEmpty()) {
                states.add(IsoAlpha3("N/A", ""))
                et_state.setText("N/A")
                handleVisibility(View.GONE)
                showCities()
                return
            }
            statesName.add(IsoAlpha3(item.payOutBranchState, ""))
        }
        val filteredState = statesName.distinct()

        if (filteredState.size == 1) {
            et_state.setText(filteredState[0].country)
            states.add(IsoAlpha3(filteredState[0].country, ""))
            et_state.isEnabled = false
            stateSizeOne = true
            showCities()
            return
        }
        et_state.isEnabled = true
        if (filteredState.size != 1) {
            states = filteredState.distinct() as ArrayList<IsoAlpha3>
            val stateAdapter =
                ArrayAdapter(requireContext(), R.layout.layout_autcompletetext, states)
            et_state.setAdapter(stateAdapter)
        }

    }
}
