package com.fh.payday.views2.payments.ibp

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import com.fh.payday.BaseActivity
import com.fh.payday.BaseFragment
import com.fh.payday.R
import com.fh.payday.datasource.models.payments.*
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.*
import com.fh.payday.viewmodels.Add2BeneficiaryViewModel
import com.fh.payday.views2.loan.apply.TermsAndConditionsDialog
import com.fh.payday.views2.payments.BeneficiaryBottomSheet
import com.fh.payday.views2.shared.activity.Add2BeneficiaryActivity
import com.fh.payday.views2.shared.activity.REQUEST_CODE_ADD_BENEFICIARY
import kotlinx.android.synthetic.main.fragment_account_no.*

class AccountNoFragment : BaseFragment() {

    companion object {
        const val STD_CODE = 56
        const val BSNL_ACCOUNT = 61
        const val ACCOUNT = 88
        const val TYPE_BSNL_ACCOUNT = "bsnl_account"
    }

    private var beneficiariesList = ArrayList<Beneficiaries>()
    private var accountType: Int? = null
    private lateinit var category: String
    private val beneficiaryViewModel by lazy {
        ViewModelProviders.of(this).get(Add2BeneficiaryViewModel::class.java)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
//        if (!isVisibleToUser && et_account_no != null) {
////            et_account_no.text = null
//        }
        val viewModel = getViewModel() ?: return
        if (isVisibleToUser && viewModel.dataClear) {
            et_account_no.text = null
        }

        if (isVisibleToUser) {
            viewModel.enteredAmount = null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_account_no, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_add_to_beneficiary.setTextWithUnderLine(tv_add_to_beneficiary.text.toString())
        tv_beneficiary.setTextWithUnderLine(tv_beneficiary.text.toString())

        val viewModel = getViewModel() ?: return
        val user = viewModel.user ?: return
        val (_, accessKey, _, isFlexAvailable) = viewModel.selectedOperator ?: return
        val planType = if (isFlexAvailable == 1) PlanType.FLEXI else PlanType.FIXED
        category = viewModel.selectedOperator?.serviceCategory ?: ServiceCategory.PREPAID
        accountType = arguments?.getInt("account_type") ?: ACCOUNT

        if (category == ServiceCategory.POSTPAID || category == ServiceCategory.PREPAID) {
            et_account_no.mask = "+91-##########"
        }
        et_account_no.setOnEditorActionListener { _, _, _ -> false }

        when (accountType) {
            STD_CODE -> {
                tv_account_no.text = getString(R.string.std_code)
                et_account_no.setMaxLength(maxLenLandlineStdCode)
                fetchBeneficiaries().also { addBeneficiariesObserver(); attachObserver() }
            }
            BSNL_ACCOUNT -> {
                tv_account_no.text = getString(R.string.enter_account_number)
                et_account_no.setMaxLength(maxLenBSNLAccount)
            }
            else -> {
                handleView(category)
                fetchBeneficiaries().also { addBeneficiariesObserver() }
                if (planType != PlanType.FIXED) attachObserver()
            }
        }

        btn_next.setOnClickListener {
            val operator = viewModel.selectedOperator ?: return@setOnClickListener
            if (accountType == STD_CODE) {
                val stdCode = et_account_no.text?.toString() ?: ""
                if (isValid(stdCode, minLenLandlineStdCode, maxLenLandlineStdCode)) {
                    viewModel.setSTDCode(stdCode)
                    onSuccess(getActivity() ?: return@setOnClickListener)
                }
            } else if (accountType == BSNL_ACCOUNT) {
                val accountNo = et_account_no.text?.toString() ?: ""
                if (isValid(accountNo, minLenBSNLAccount, maxLenBSNLAccount)) {
                    viewModel.setBSNLAccount(accountNo)
                    onSuccess(getActivity() ?: return@setOnClickListener)
                }
            } else if (planType == PlanType.FIXED){
                val account = et_account_no.text?.toString()?.replace("+91-", "") ?: ""
                if (isValid(account, operator.getAccountMinLen(accountType), operator.getAccountMaxLen(accountType))) {
                    viewModel.setAccountNumber(account)
                    onSuccess(getActivity() ?: return@setOnClickListener)
                }
            }
            else {
                val account = et_account_no.text?.toString()?.replace("+91-", "") ?: ""
                if (isValid(account, operator.getAccountMinLen(accountType), operator.getAccountMaxLen(accountType))) {

                    if (activity?.isNetworkConnected() == false) {
                        activity?.onFailure(activity?.findViewById(R.id.card_view)!!, getString(R.string.no_internet_connectivity))
                        return@setOnClickListener
                    }
                    viewModel.operatorDetails(user.token, user.sessionId, user.refreshToken, user.customerId.toLong(),
                            account, accessKey, viewModel.typeId ?: return@setOnClickListener,
                            planType, CountryCode.INDIA)
                }
            }
        }
        attachListener()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_ADD_BENEFICIARY -> fetchBeneficiaries()
            }
        }
    }

    private fun handleView(category: String?) {
        when (category) {
            ServiceCategory.PREPAID, ServiceCategory.POSTPAID -> tv_account_no.text = getString(R.string.enter_mobile_number_payments)
            ServiceCategory.LANDLINE -> tv_account_no.text = getString(R.string.enter_landline_number)
            ServiceCategory.DTH -> tv_account_no.text = getString(R.string.enter_customer_id)
            ServiceCategory.GAS -> tv_account_no.text = getString(R.string.enter_account_number)
            ServiceCategory.ELECTRICITY -> tv_account_no.text = getString(R.string.enter_consumer_number)
            ServiceCategory.INSURANCE -> tv_account_no.text = getString(R.string.enter_policy_number)
        }

        getViewModel()?.selectedOperator?.let { et_account_no.setMaxLength(it.maxLenAccountNo) }
        if (category == ServiceCategory.POSTPAID || category == ServiceCategory.PREPAID) {
            et_account_no.setMaxLength(14)
        }
    }

    private fun getViewModel() = when (activity) {
        is IndianBillPaymentActivity -> (activity as IndianBillPaymentActivity?)?.viewModel
        else -> null
    }

    private fun attachListener() {

        if (category == ServiceCategory.POSTPAID || category == ServiceCategory.PREPAID) {
            et_account_no.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    til_account_no.clearErrorMessage()
                    handleBeneficiariesView(s?.toString())
                }
            })
        } else {
            et_account_no.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    til_account_no.clearErrorMessage()
                    handleBeneficiariesView(s?.toString())
                }
            })
        }

        val operator = getViewModel()?.selectedOperator ?: return
        tv_add_to_beneficiary.setOnClickListener {
            val account = et_account_no.text?.toString()?.replace("+91-", "") ?: ""
            if (isValid(account, operator.getAccountMinLen(accountType), operator.getAccountMaxLen(accountType))) {
                val category = operator.serviceCategory
                val mIntent = Intent(activity, Add2BeneficiaryActivity::class.java).apply {
                    putExtra("number_len", account.length)
                    putExtra("mobileNo", account)
                    putExtra("access_key", operator.accessKey)
                    putExtra("res_id", operator.serviceImage)
                    putExtra("category", category)

                    when {
                        BSNL_LANDLINE_ACCESS_KEY.equals(operator.accessKey, ignoreCase = true) -> {
                            val stdCode = getViewModel()?.data?.value?.get("std_code")
                                    ?: return@setOnClickListener
                            val bsnlAccount = getViewModel()?.data?.value?.get("bsnl_account")
                                    ?: return@setOnClickListener
                            putExtra("std_code", stdCode)
                            putExtra("bsnl_account", bsnlAccount)
                        }
                        MTNL_LANDLINE_ACCESS_KEY.equals(operator.accessKey, ignoreCase = true) -> {
                            val stdCode = getViewModel()?.data?.value?.get("std_code")
                                ?: return@setOnClickListener
                            val mtnlAccount = getViewModel()?.data?.value?.get("bsnl_account")
                                ?: return@setOnClickListener
                            putExtra("std_code", stdCode)
                            putExtra("bsnl_account", mtnlAccount)
                        }
                        AIRTEL_LANDLINE_ACCESS_KEY.equals(operator.accessKey, ignoreCase = true) -> {
                            val stdCode = getViewModel()?.data?.value?.get("std_code")
                                    ?: return@setOnClickListener
                            putExtra("std_code", stdCode)
                        }
                        RELIANCE_LANDLINE_ACCESS_KEY.equals(operator.accessKey, ignoreCase = true) -> {
                            val stdCode = getViewModel()?.data?.value?.get("std_code")
                                    ?: return@setOnClickListener
                            putExtra("std_code", stdCode)
                        }
                        MTNL_LANDLINE_ACCESS_KEY.equals(operator.accessKey, ignoreCase = true) -> {
                            val stdCode = getViewModel()?.data?.value?.get("std_code")
                                    ?: return@setOnClickListener
                            putExtra("std_code", stdCode)
                        }
                        DOCOMO_LANDLINE_ACCESS_KEY.equals(operator.accessKey, ignoreCase = true) -> {
                            val stdCode = getViewModel()?.data?.value?.get("std_code")
                                    ?: return@setOnClickListener
                            putExtra("std_code", stdCode)
                        }
                    }

                }
                startActivityForResult(mIntent, REQUEST_CODE_ADD_BENEFICIARY)
            }
        }

        tv_beneficiary.setOnClickListener { showBeneficiaries() }
    }

    fun handleBeneficiariesView(s: String?) {

        val accountNumber = s?.replace("+91-", "")?.trim()
        val viewModel = getViewModel() ?: return
        val operator = viewModel.selectedOperator ?: return

        if (accountType == BSNL_ACCOUNT) {
            tv_add_to_beneficiary.visibility = View.GONE
            tv_beneficiary.visibility = View.GONE
            return
        }
        when {
            beneficiariesList.any { it.mobileNumber == accountNumber } -> {
                tv_add_to_beneficiary.visibility = View.GONE

                if (accountType != BSNL_ACCOUNT &&
                    (BSNL_LANDLINE_ACCESS_KEY.equals(operator.accessKey, ignoreCase = true)
                        || MTNL_LANDLINE_ACCESS_KEY.equals(operator.accessKey, ignoreCase = true))) {
                    tv_beneficiary.visibility = View.GONE
                    return
                }

                when(operator.accessKey){

                         AIRTEL_LANDLINE_ACCESS_KEY, RELIANCE_LANDLINE_ACCESS_KEY,
                         MTNL_LANDLINE_ACCESS_KEY, DOCOMO_LANDLINE_ACCESS_KEY ->{
                             tv_beneficiary.visibility = View.GONE
                             return
                         }
                    }

                tv_beneficiary.visibility = View.VISIBLE
            }
            accountNumber != null && accountNumber.length >= operator.getAccountMinLen(accountType)
                    && accountNumber.length <= operator.getAccountMaxLen(accountType) -> {

                if (accountType == STD_CODE) {
                    tv_add_to_beneficiary.visibility = View.GONE
                    return
                }
                tv_add_to_beneficiary.visibility = View.VISIBLE
                tv_beneficiary.visibility = View.GONE
            }
            beneficiariesList.isNotEmpty() -> {

                if (accountType == STD_CODE) {
                    when(operator.accessKey){
                        AIRTEL_LANDLINE_ACCESS_KEY, RELIANCE_LANDLINE_ACCESS_KEY, MTNL_LANDLINE_ACCESS_KEY, BSNL_LANDLINE_ACCESS_KEY, DOCOMO_LANDLINE_ACCESS_KEY ->{
                            tv_add_to_beneficiary.visibility = View.GONE
                            return
                        }
                    }
                }

                if (accountType != BSNL_ACCOUNT &&
                    (BSNL_LANDLINE_ACCESS_KEY.equals(operator.accessKey, ignoreCase = true)
                        || MTNL_LANDLINE_ACCESS_KEY.equals(operator.accessKey, ignoreCase = true))) {
                    tv_beneficiary.visibility = View.GONE
                    tv_add_to_beneficiary.visibility = View.GONE
                    return
                }
                when(operator.accessKey){
                    AIRTEL_LANDLINE_ACCESS_KEY, RELIANCE_LANDLINE_ACCESS_KEY,
                    MTNL_LANDLINE_ACCESS_KEY, DOCOMO_LANDLINE_ACCESS_KEY ->{
                        tv_beneficiary.visibility = View.GONE
                        tv_add_to_beneficiary.visibility = View.GONE
                        return
                    }
                }

                tv_add_to_beneficiary.visibility = View.GONE
                tv_beneficiary.visibility = View.VISIBLE
            }
            else -> {
                tv_add_to_beneficiary.visibility = View.GONE
                tv_beneficiary.visibility = View.GONE
            }
        }
    }

    private fun attachObserver() {
        getViewModel()?.operatorDetailFlexiState?.observe(this, Observer {
            it ?: return@Observer
            val activity = getActivity() as BaseActivity? ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer
            if (state is NetworkState2.Loading) {
                btn_next.visibility = View.GONE
                progress_bar.visibility = View.VISIBLE
                return@Observer
            }

            btn_next.visibility = View.VISIBLE
            progress_bar.visibility = View.GONE

            when (state) {
                is NetworkState2.Success -> onSuccess(activity)
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        return@Observer activity.onSessionExpired(state.message)
                    }
//                    activity.onError(state.message)
                    activity.handleErrorCode(state.errorCode.toInt(), state.message)
                }
                is NetworkState2.Failure -> activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
                else -> activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })

        getViewModel()?.operatorDetailFixedState?.observe(this, Observer {
            it ?: return@Observer
            val activity = getActivity() as BaseActivity? ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                btn_next.visibility = View.GONE
                progress_bar.visibility = View.VISIBLE
                return@Observer
            }

            btn_next.visibility = View.VISIBLE
            progress_bar.visibility = View.GONE

            when (state) {
                is NetworkState2.Success -> onSuccess(activity)
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        return@Observer activity.onSessionExpired(state.message)
                    }
//                    activity.onError(state.message)
                    activity.handleErrorCode(state.errorCode.toInt(), state.message)
                }
                is NetworkState2.Failure -> activity.onFailure(activity.findViewById(R.id.root_view), state.throwable)
                else -> activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    private fun isValid(account: String, minLength: Int = 10, maxLength: Int) =
            when {
                account.trim().isEmpty() -> {
                    setError(minLength, maxLength); false
                }
                account.length < minLength -> {
                    setError(minLength, maxLength); false
                }
                else -> true
            }

    private fun setError(minLength: Int, maxLength: Int) {
        if (minLength >= maxLength) {
            return til_account_no.setErrorMessage(
                    String.format(getString(R.string.invalid_account_length), minLength)
            )
        }
        til_account_no.setErrorMessage(String.format(getString(R.string.invalid_account_length_ranged),
                minLength, maxLength))
    }

    private fun onSuccess(activity: FragmentActivity) {
        when (activity) {
            is IndianBillPaymentActivity -> activity.navigateUp()
        }
    }

    private fun fetchBeneficiaries() {
        val viewModel = getViewModel() ?: return
        val user = viewModel.user ?: return
        val operator = viewModel.selectedOperator ?: return
        viewModel.getBeneficiaries(user.token, user.sessionId,
                user.refreshToken, user.customerId.toLong(), operator.accessKey)
    }

    private fun addBeneficiariesObserver() {
        getViewModel()?.beneficiariesState?.observe(this, Observer {
            it ?: return@Observer
            val state = it.peekContent()
            tv_add_to_beneficiary.visibility = View.GONE
            tv_beneficiary.visibility = View.GONE

            if (state is NetworkState2.Success) {
                val filteredData = state.data?.filter { beneficiary ->
                    if (accountType == BSNL_ACCOUNT) beneficiary.type == TYPE_BSNL_ACCOUNT
                    else beneficiary.type.isNullOrEmpty()
                }
                if (filteredData.isNullOrEmpty()) return@Observer
                if (accountType != STD_CODE && accountType != BSNL_ACCOUNT &&
                    (BSNL_LANDLINE_ACCESS_KEY.equals(getViewModel()?.selectedOperator?.accessKey, ignoreCase = true)
                        || MTNL_LANDLINE_ACCESS_KEY.equals(getViewModel()?.selectedOperator?.accessKey, ignoreCase = true))) {
                    tv_beneficiary.visibility = View.GONE
                    beneficiariesList.apply { clear(); addAll(filteredData) }
                    return@Observer
                }

                if (accountType != STD_CODE) {
                    when(getViewModel()?.selectedOperator?.accessKey){

                        AIRTEL_LANDLINE_ACCESS_KEY, RELIANCE_LANDLINE_ACCESS_KEY, MTNL_LANDLINE_ACCESS_KEY, DOCOMO_LANDLINE_ACCESS_KEY ->{
                            tv_beneficiary.visibility = View.GONE
                            beneficiariesList.apply { clear(); addAll(filteredData) }
                            return@Observer
                        }
                    }
                }

                tv_beneficiary.visibility = View.VISIBLE
                beneficiariesList.apply { clear(); addAll(filteredData) }
            } else if (state is NetworkState2.Error) {
                if (state.isSessionExpired) {
                    activity?.onSessionExpired(state.message)
                }
            }
        })

        beneficiaryViewModel.deleteBeneficiaryState.observe(this, Observer {
            it ?: return@Observer
            val activity = activity ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing))
                return@Observer
            }

            activity.hideProgress()

            when (state) {
                is NetworkState2.Success -> {
                    fetchBeneficiaries()
                    activity.showMessage(state.data ?: return@Observer)
                }
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        return@Observer activity.onSessionExpired(state.message)
                    }
                    activity.handleErrorCode(state.errorCode.toInt(), state.message)
                }
                is NetworkState2.Failure -> activity.onFailure(activity.findViewById(R.id.root_view), state.throwable)
                else -> activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })

        beneficiaryViewModel.enableBeneficiaryState.observe(this, Observer {
            it ?: return@Observer
            val activity = activity ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing))
                return@Observer
            }
            activity.hideProgress()

            when(state) {
                is NetworkState2.Success -> {
                    fetchBeneficiaries()
                    activity.showMessage(state.data ?: return@Observer)
                }
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        return@Observer activity.onSessionExpired(state.message)
                    }
                    activity.handleErrorCode(state.errorCode.toInt(), state.message)
                }
                is NetworkState2.Failure -> activity.onFailure(activity.findViewById(R.id.root_view), state.throwable)
                else -> activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    private fun showBeneficiaries() {

        if (beneficiariesList.isNullOrEmpty()) return

        val activity = activity ?: return
        val logo = getViewModel()?.selectedOperator?.serviceImage
        val (_, accessKey, _, _) = getViewModel()?.selectedOperator ?: return

        val recentAccounts = beneficiariesList.map { it.logo = logo; it } as ArrayList

        val bottomSheet = BeneficiaryBottomSheet.newInstance(recentAccounts) { accountNo: String, optional1: String?, optional2: String? ->

            if (accountType == STD_CODE && (BSNL_LANDLINE_ACCESS_KEY.equals(accessKey, ignoreCase = true)
                    || MTNL_LANDLINE_ACCESS_KEY.equals(accessKey, ignoreCase = true))) {
                et_account_no.setText(optional1)
                getViewModel()?.apply {
                    setSTDCode(optional1 ?: return@newInstance)
                    setBSNLAccount(optional2 ?: return@newInstance)
                    setAccountNo(accountNo)
                    setBLBSelected()
                }
            }else if (accountType == STD_CODE) {

                when(accessKey){
                    AIRTEL_LANDLINE_ACCESS_KEY, RELIANCE_LANDLINE_ACCESS_KEY, MTNL_LANDLINE_ACCESS_KEY, DOCOMO_LANDLINE_ACCESS_KEY ->{
                        et_account_no.setText(optional1)
                        getViewModel()?.apply {
                            setSTDCode(optional1 ?: return@newInstance)
                            setAccountNo(accountNo)
                            setLandLineSelected()
                        }
                    }
                }
            }
            else {
                et_account_no.setText(accountNo)
            }
        }

        bottomSheet.attachPopUpListener { beneficiary, view ->
            val menu = showPopUpMenu(beneficiary, view)
            menu.show()

            menu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.edit -> {
                        bottomSheet.dismiss()
                        val intent = Intent(activity, Add2BeneficiaryActivity::class.java).apply {
                            putExtra("beneficiary_data", beneficiary)
                            putExtra("action", "edit")
                            putExtra("category", category)
                            putExtra("number_len", beneficiary.mobileNumber.length)
                        }
                        startActivityForResult(intent, REQUEST_CODE_ADD_BENEFICIARY)
                        true
                    }
                    R.id.delete -> {
                        bottomSheet.dismiss()
                        showDeleteConfirmation(beneficiary)
                        true
                    }
                    R.id.enable -> {
                        bottomSheet.dismiss()
                        showEnableConfirmation(beneficiary)
                        true
                    }
                    else -> false
                }
            }
        }
        bottomSheet.show(activity.supportFragmentManager, bottomSheet.tag)
    }
    private fun showPopUpMenu(beneficiary: Beneficiaries, view: View): PopupMenu {
        val menu = PopupMenu(activity, view)
        menu.inflate(R.menu.menu_beneficiary)

        if (beneficiary.enabled) {
            menu.menu.getItem(2).setTitle(R.string.disable)
        } else {
            menu.menu.getItem(2).setTitle(R.string.enable)
        }
        return menu
    }

    private fun showEnableConfirmation(beneficiary: Beneficiaries) {

        val message = if (beneficiary.enabled) {
            String.format(getString(R.string.enable_beneficiary_confirmation), getString(R.string.disable), beneficiary.shortName)
        } else {
            String.format(getString(R.string.enable_beneficiary_confirmation), getString(R.string.enable), beneficiary.shortName)
        }

        val positiveLabel = if (beneficiary.enabled) getString(R.string.disable) else getString(R.string.enable)
        val termsAndCondDialog = TermsAndConditionsDialog.Builder()
                .setMessage(message)
                .setMessageGravity(Gravity.CENTER)
                .setPositiveText(positiveLabel)
                .setNegativeText(getString(R.string.cancel))
                .attachPositiveListener {
                    val user = UserPreferences.instance.getUser(requireContext()) ?: return@attachPositiveListener
                    beneficiaryViewModel.enableBeneficiary(
                            user.token, user.sessionId, user.refreshToken, user.customerId.toLong(),
                            beneficiary.beneficiaryId.toLong(), !beneficiary.enabled)
                }
                .build()

        val fm = fragmentManager ?: return

        termsAndCondDialog.apply {
            isCancelable = false
            show(fm, termsAndCondDialog.tag)
        }
    }

    private fun showDeleteConfirmation(beneficiary: Beneficiaries) {
        val termsAndCondDialog = TermsAndConditionsDialog.Builder()
                .setMessage(String.format(getString(R.string.delele_beneficiary_confirmation), beneficiary.shortName))
                .setMessageGravity(Gravity.CENTER)
                .setPositiveText(getString(R.string.delete))
                .setNegativeText(getString(R.string.cancel))
                .attachPositiveListener {
                    val user = UserPreferences.instance.getUser(requireContext())
                            ?: return@attachPositiveListener
                    beneficiaryViewModel.deleteBeneficiary(
                            user.token, user.sessionId, user.refreshToken, user.customerId.toLong(),
                            beneficiary.beneficiaryId.toLong()
                    )
                }
                .build()

        val fm = fragmentManager ?: return

        termsAndCondDialog.apply {
            isCancelable = false
            show(fm, termsAndCondDialog.tag)
        }
    }

    private fun Operator.getAccountMinLen(accountType: Int?) = when (accountType) {
        BSNL_ACCOUNT -> minLenBSNLAccount
        else -> minLenAccountNo
    }

    private fun Operator.getAccountMaxLen(accountType: Int?) = when (accountType) {
        BSNL_ACCOUNT -> maxLenBSNLAccount
        else -> maxLenAccountNo
    }
}
