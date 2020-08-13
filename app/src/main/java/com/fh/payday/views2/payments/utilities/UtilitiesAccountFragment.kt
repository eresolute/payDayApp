package com.fh.payday.views2.payments.utilities

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.design.widget.TextInputLayout
import android.support.v4.app.FragmentActivity
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.BaseFragment
import com.fh.payday.R
import com.fh.payday.datasource.models.login.User
import com.fh.payday.datasource.models.payments.Beneficiaries
import com.fh.payday.datasource.models.payments.CountryCode
import com.fh.payday.datasource.models.payments.UtilityServiceType
import com.fh.payday.datasource.models.payments.utilities.*
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.*
import com.fh.payday.utilities.maskededittext.MaskedEditText
import com.fh.payday.viewmodels.Add2BeneficiaryViewModel
import com.fh.payday.views2.loan.apply.TermsAndConditionsDialog
import com.fh.payday.views2.payments.BeneficiaryBottomSheet
import com.fh.payday.views2.shared.activity.Add2BeneficiaryActivity
import com.fh.payday.views2.shared.activity.REQUEST_CODE_ADD_BENEFICIARY
import com.fh.payday.views2.shared.custom.AlertDialogFragment
import java.util.*

class UtilitiesAccountFragment : BaseFragment() {
    private var tvNoteMobile: TextView? = null
    private lateinit var bttnNext: Button
    private lateinit var textInputLayout: TextInputLayout
    private var etAccountNo: MaskedEditText? = null
    private lateinit var tvTitle: TextView

    private var operator: String? = null
    private lateinit var accessKey: String
    private var aadcServiceType: String? = null
    private var typeId: Int = 0
    private var user: User? = null
    private var tvBeneficiary: TextView? = null
    private var tvAdd2Beneficiary: TextView? = null
    private var beneficiariesList: ArrayList<Beneficiaries> = ArrayList()
    private lateinit var beneficiaryViewModel: Add2BeneficiaryViewModel

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context as UtilitiesActivity
        user = UserPreferences.instance.getUser(context)
        beneficiaryViewModel =  ViewModelProviders.of(this).get(Add2BeneficiaryViewModel::class.java)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            handleView()
            addBeneficiariesObserver()
            fetchBeneficiaries()
        }

        activity ?: return
        val activity = activity as UtilitiesActivity
        activity.viewModel ?: return

        if (isVisibleToUser && activity.viewModel.dataClear) {
            etAccountNo?.text = null
        }

    }

    private fun handleView() {
        if (operator != null && operator.equals(UtilityServiceType.AADC, ignoreCase = true)) {
            aadcServiceType = getViewModel()?.aadcSelectedService.toString()
            tvNoteMobile?.visibility = View.GONE

            when (aadcServiceType) {
                ACCOUNTID -> {
                    tvTitle.text = getString(R.string.enter_account_number)
                    etAccountNo?.setMaxLength(12)
                    etAccountNo?.mask = "############"
                }
                EMIRATESID -> {
                    tvTitle.text = getString(R.string.enter_emirates_id_payments)
                    etAccountNo?.setMaxLength(18)
                    etAccountNo?.mask = "###-####-#######-#"
                    //etAccountNo?.isKeepHint = true
                    //etAccountNo?.hint = "xxxxxxxxxxxxxxx"
                }
                PERSONID -> {
                    tvTitle.text = getString(R.string.enter_person_id)
                    etAccountNo?.setMaxLength(10)
                    etAccountNo?.mask = "##########"
                }
                MOBILENO -> {
                    tvTitle.text = getString(R.string.enter_mobile_number_payments)
                    etAccountNo?.setMaxLength(14)
                    etAccountNo?.mask = "+971-#########"
                    tvNoteMobile?.visibility = View.VISIBLE
                }
                else -> {
                    tvTitle.text = getString(R.string.enter_account_number)
                    etAccountNo?.setMaxLength(12)
                    etAccountNo?.mask = "############"
                }
            }

            val activity = activity as UtilitiesActivity? ?: return
            etAccountNo?.setText(activity.viewModel.accountNumber ?: "")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_utilities_account, container, false)
        init(view)

        if (arguments != null) {
            operator = arguments?.getString("operator") ?: "fewa"
            typeId = 1
        }
        addObserver()
        addDetailObserver()
        addBillStateObserver()
        addAadcBillStateObserver()
        addBeneficiariesObserver()
        fetchBeneficiaries()
        addTextWatcher()

        etAccountNo?.setOnEditorActionListener { _, _, _ -> false }
        tvBeneficiary?.setTextWithUnderLine(tvBeneficiary?.text.toString())
        tvAdd2Beneficiary?.setTextWithUnderLine(tvAdd2Beneficiary?.text.toString())

        bttnNext.setOnClickListener {
            val aadcServiceType = getViewModel()?.aadcSelectedService
            getViewModel()?.amount?.value = null
            val accountNo = etAccountNo?.text.toString().trim().replace("-", "").replace("+971", "0")
            if (validateEditText(accountNo, aadcServiceType)) {
                val user = user ?: return@setOnClickListener

                val activity = getActivity() as BaseActivity? ?: return@setOnClickListener
                if (!activity.isNetworkConnected()) {
                    activity.onFailure(activity.findViewById(R.id.card_view), getString(R.string.no_internet_connectivity))
                    return@setOnClickListener
                }
                getViewModel()?.operatorRequest(user.token, user.sessionId, user.refreshToken,
                        user.customerId.toLong(), typeId, CountryCode.DEFAULT)
            }
        }

        return view
    }


    private fun getViewModel() = when (activity) {
        is UtilitiesActivity -> (activity as UtilitiesActivity?)?.viewModel
        else -> null
    }

    private fun addObserver() {

        getViewModel()?.operatorState?.observe(this, Observer { event ->
            if (event == null) return@Observer

            val activity = getActivity() as BaseActivity? ?: return@Observer
            val operatorState = event.getContentIfNotHandled() ?: return@Observer

            if (operatorState is NetworkState2.Loading<*>) {
                activity.showProgress(getString(R.string.processing))
                return@Observer
            }
            bttnNext.isEnabled = false
            //activity.hideProgress();
            if (operatorState is NetworkState2.Success<*>) {

                val data = (operatorState as NetworkState2.Success<List<Operator>>).data
                for (i in data?.indices ?: return@Observer) {
                    if (data[i].serviceProvider.equals(operator, ignoreCase = true)) {
                        getViewModel()?.operator = operator
                        accessKey = data[i].accessKey
                        getViewModel()?.operatorDetails(user?.token
                                ?: return@Observer, user?.sessionId ?: return@Observer,
                                user?.refreshToken ?: return@Observer, user?.customerId?.toLong()
                                ?: return@Observer,
                                data[i].accessKey, typeId, "flexi", CountryCode.DEFAULT)
                        break
                    }
                }
            } else if (operatorState is NetworkState2.Error<*>) {
                activity.hideProgress()
                bttnNext.isEnabled = true
                val (message, _, isSessionExpired) = operatorState
                if (isSessionExpired) {
                    activity.onSessionExpired(message)
                    return@Observer
                }
//                activity.onError(message)
                activity.handleErrorCode(operatorState.errorCode.toInt(), operatorState.message)
            } else if (operatorState is NetworkState2.Failure<*>) {

                activity.hideProgress()
                bttnNext.isEnabled = true
                activity.onFailure(activity.findViewById<View>(R.id.card_view), operatorState.throwable)

            } else {
                activity.hideProgress()
                bttnNext.isEnabled = true
                activity.onFailure(activity.findViewById<View>(R.id.card_view), CONNECTION_ERROR)
            }
        })
    }

    private fun addUpdateObserver() {
        beneficiaryViewModel.deleteBeneficiaryState.observe(this, Observer {
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

    private fun addDetailObserver() {
        getViewModel()?.operatorDetailState?.observe(this, Observer { event ->
            if (event == null) return@Observer

            val activity = getActivity() as BaseActivity? ?: return@Observer
            val operatorDetailState = event.getContentIfNotHandled() ?: return@Observer

            if (operatorDetailState is NetworkState2.Loading<*>) {
                //activity.showProgress("Processing");
                return@Observer
            }

            when (operatorDetailState) {
                is NetworkState2.Success<*> -> {
                    if (user == null) return@Observer

                    val user = user ?: return@Observer
                    val data = (operatorDetailState as NetworkState2.Success<OperatorDetails>).data
                    val accountNo = etAccountNo?.text.toString().trim()
                            .replace("-", "")
                            .replace("+971", "0")

                    when {
                        "fewa".equals(operator, ignoreCase = true) -> {
                            getViewModel()?.billBalanceFewa(user.token, user.sessionId, user.refreshToken,
                                    user.customerId.toLong(), accessKey, "getBalance", typeId, accountNo,
                                    data?.flexiKey
                                            ?: return@Observer, Integer.parseInt(data.typeKey))
                        }
                        "aadc".equals(operator, ignoreCase = true) -> {

                            val optional1 = getViewModel()?.aadcSelectedService
                                    ?: return@Observer
                            val optional2 = "ENG"

                            getViewModel()?.billAadc(user.token, user.sessionId, user.refreshToken,
                                    user.customerId.toLong(), accessKey, "getBalance", typeId, accountNo,
                                    data?.flexiKey
                                            ?: return@Observer, Integer.parseInt(data.typeKey), optional1, optional2)
                        }
                        else -> {
                            activity.hideProgress()
                            bttnNext.isEnabled = true
                        }
                    }
                }
                is NetworkState2.Error<*> -> {
                    activity.hideProgress()
                    bttnNext.isEnabled = true
                    val (message, _, isSessionExpired) = operatorDetailState
                    if (isSessionExpired) {
                        activity.onSessionExpired(message)
                        return@Observer
                    }
//                    activity.onError(message)
                    activity.handleErrorCode(operatorDetailState.errorCode.toInt(), operatorDetailState.message)
                }
                is NetworkState2.Failure<*> -> {
                    activity.hideProgress()
                    bttnNext.isEnabled = true
                    activity.onFailure(activity.findViewById(R.id.card_view), operatorDetailState.throwable)
                }
                else -> {
                    activity.hideProgress()
                    bttnNext.isEnabled = true
                    activity.onFailure(activity.findViewById<View>(R.id.card_view), CONNECTION_ERROR)
                }
            }

        })
    }

    private fun addBillStateObserver() {

        getViewModel()?.fewaBillState?.observe(this, Observer { event ->
            if (event == null) {
                return@Observer
            }
            val activity = getActivity() as BaseActivity? ?: return@Observer
            val billState = event.getContentIfNotHandled() ?: return@Observer

            if (billState is NetworkState2.Loading<*>) {
                //                activity.showProgress("Processing");
                bttnNext.isEnabled = false
                return@Observer
            }

            activity.hideProgress()
            bttnNext.isEnabled = true

            if (billState is NetworkState2.Success<*>) {

                val data = (billState as NetworkState2.Success<BillDetails>).data
                if (data != null) {

                    if (data.Balance != null) {
                        getViewModel()?.accountNumber = etAccountNo?.text.toString().trim()
                                .replace("-", "")
                                .replace("+971", "0")


                        onSuccess(activity)
                    } else {
                        activity.onError(getString(R.string.generic_payment_error))
                    }
                }
            } else if (billState is NetworkState2.Error<*>) {
                val (message, _, isSessionExpired) = billState
                if (isSessionExpired) {
                    activity.onSessionExpired(message)
                    return@Observer
                }
//                activity.onError(message)
                activity.handleErrorCode(billState.errorCode.toInt(), billState.message)

            } else if (billState is NetworkState2.Failure<*>) {
                activity.onFailure(activity.findViewById<View>(R.id.card_view), billState.throwable)

            } else {
                activity.onFailure(activity.findViewById<View>(R.id.card_view), CONNECTION_ERROR)
            }
        })
    }

    private fun addAadcBillStateObserver() {

        getViewModel()?.aadcBillState?.observe(this, Observer { event ->
            if (event == null) {
                return@Observer
            }
            val activity = getActivity() as BaseActivity? ?: return@Observer
            val billState = event.getContentIfNotHandled() ?: return@Observer

            if (billState is NetworkState2.Loading<*>) {
                //                activity.showProgress("Processing");
                bttnNext.isEnabled = false
                return@Observer
            }

            activity.hideProgress()
            bttnNext.isEnabled = true

            if (billState is NetworkState2.Success<*>) {

                val data = (billState as NetworkState2.Success<AadcBillResponse>).data
                if (data != null) {
                    if ("000" == data.ResponseCode) {
                        getViewModel()?.accountNumber = etAccountNo?.text.toString().trim()
                                .replace("-", "")
                                .replace("+971", "0")
                        onSuccess(activity)
                    } else {
                        activity.onError(getString(R.string.generic_payment_error))
                    }
                }
            } else if (billState is NetworkState2.Error<*>) {
                val (message, _, isSessionExpired) = billState
                if (isSessionExpired) {
                    activity.onSessionExpired(message)
                    return@Observer
                }
//                activity.onError(message)
                activity.handleErrorCode(billState.errorCode.toInt(), billState.message)

            } else if (billState is NetworkState2.Failure<*>) {
                activity.onFailure(activity.findViewById<View>(R.id.card_view), billState.throwable)

            } else {
                activity.onFailure(activity.findViewById<View>(R.id.card_view), CONNECTION_ERROR)
            }
        })
    }

    private fun addTextWatcher() {
        etAccountNo?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                textInputLayout.clearErrorMessage()
                handleBeneficiariesView(s.toString())
                if (getViewModel()?.selectedAccessKey.isNullOrEmpty()) tvAdd2Beneficiary?.visibility = View.GONE
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        tvAdd2Beneficiary?.setOnClickListener {
            val accountNo = etAccountNo?.text.toString().trim()
                    .replace("-", "")
                    .replace("+971", "0")
            if (validateEditText(accountNo, aadcServiceType)) {
                val mIntent = Intent(activity, Add2BeneficiaryActivity::class.java)
                val len = accountNo.length
                val accessKey = getViewModel()?.selectedAccessKey
                mIntent.putExtra("mobileNo", accountNo)
                mIntent.putExtra("access_key", accessKey)
                mIntent.putExtra("number_len", len)
                if (operator.equals(UtilityServiceType.AADC, ignoreCase = true))
                    mIntent.putExtra("category", aadcServiceType)
                else
                    mIntent.putExtra("category", UtilityServiceType.FEWA)

                mIntent.putExtra("issue", aadcServiceType)
                startActivityForResult(mIntent, REQUEST_CODE_ADD_BENEFICIARY)
            }
        }
        tvBeneficiary?.setOnClickListener { showBeneficiaries() }
    }

    fun handleBeneficiariesView(s: String?) {
        val accountNo = s?.trim()?.replace("-", "")?.replace("+971", "0")
        when {
            beneficiariesList.any { it.mobileNumber == accountNo } -> {
                tvAdd2Beneficiary?.visibility = View.GONE
                tvBeneficiary?.visibility = View.VISIBLE
            }
            accountNo != null && accountNo.length >= getEditMaxLength() -> {
                tvAdd2Beneficiary?.visibility = View.VISIBLE
                tvBeneficiary?.visibility = View.GONE
            }
            beneficiariesList.isNotEmpty() -> {
                tvAdd2Beneficiary?.visibility = View.GONE
                tvBeneficiary?.visibility = View.VISIBLE
            }
            else -> {
                tvAdd2Beneficiary?.visibility = View.GONE
                tvBeneficiary?.visibility = View.GONE
            }
        }
    }

    private fun validateEditText(accountNo: String, aadcServiceType: String?): Boolean {

        if (operator.equals(UtilityServiceType.AADC, ignoreCase = true)) {
            when (aadcServiceType) {
                ACCOUNTID -> when {
                    TextUtils.isEmpty(accountNo) -> {
                        textInputLayout.setErrorMessage(getString(R.string.empty_account_no))
                        etAccountNo?.requestFocus()
                        return false
                    }
                    accountNo.length < 8 -> {
                        textInputLayout.setErrorMessage(getString(R.string.invalid_account_no))
                        etAccountNo?.requestFocus()
                        return false
                    }
                    else -> {
                        textInputLayout.clearErrorMessage()
                        return true
                    }
                }
                EMIRATESID -> when {
                    TextUtils.isEmpty(accountNo) -> {
                        textInputLayout.setErrorMessage(getString(R.string.empty_emirate_id))
                        etAccountNo?.requestFocus()
                        return false
                    }
                    accountNo.length < 15 -> {
                        textInputLayout.setErrorMessage(getString(R.string.invalid_emirate_id))
                        etAccountNo?.requestFocus()
                        return false
                    }
                    !Validator.isValidEmiratesId(accountNo) -> {
                        textInputLayout.setErrorMessage(getString(R.string.invalid_emirate_id))
                        etAccountNo?.requestFocus()
                        return false
                    }
                    else -> {
                        textInputLayout.clearErrorMessage()
                        return true
                    }
                }
                PERSONID -> return when {
                    TextUtils.isEmpty(accountNo) -> {
                        textInputLayout.setErrorMessage(getString(R.string.empty_person_id))
                        etAccountNo?.requestFocus()
                        false
                    }
                    accountNo.length < 10 -> {
                        textInputLayout.setErrorMessage(getString(R.string.inValid_person_id))
                        etAccountNo?.requestFocus()
                        false
                    }
                    else -> {
                        textInputLayout.clearErrorMessage()
                        true
                    }
                }
                MOBILENO -> when {
                    TextUtils.isEmpty(accountNo) -> {
                        textInputLayout.setErrorMessage(getString(R.string.empty_mobile_no_payments))
                        etAccountNo?.requestFocus()
                        return false
                    }
                    accountNo.length < 10 -> {
                        textInputLayout.setErrorMessage(getString(R.string.invalid_mobile_no))
                        etAccountNo?.requestFocus()
                        return false
                    }
                    else -> {
                        textInputLayout.clearErrorMessage()
                        return true
                    }
                }
            }
        } else {

            when {
                TextUtils.isEmpty(accountNo) -> {
                    textInputLayout.setErrorMessage(getString(R.string.empty_account_no))
                    etAccountNo?.requestFocus()
                    return false
                }
                accountNo.length < 8 -> {
                    textInputLayout.setErrorMessage(getString(R.string.invalid_account_no))
                    etAccountNo?.requestFocus()
                    return false
                }
                else -> {
                    textInputLayout.clearErrorMessage()
                    return true
                }
            }
        }
        return false
    }

    private fun init(view: View) {
        bttnNext = view.findViewById(R.id.btn_next)
        textInputLayout = view.findViewById(R.id.til_account_no)
        etAccountNo = view.findViewById(R.id.et_account_number)
        tvBeneficiary = view.findViewById(R.id.tv_recent_accounts)
        tvAdd2Beneficiary = view.findViewById(R.id.tv_add_to_beneficiary)
        tvTitle = view.findViewById(R.id.tvTitle)
        tvNoteMobile = view.findViewById(R.id.tv_note_mobile)
    }

    private fun addBeneficiariesObserver() {

        getViewModel()?.beneficiariesState?.observe(this, Observer { event ->
            if (event == null) return@Observer
            val state = event.peekContent()

            tvBeneficiary?.visibility = View.GONE
            tvAdd2Beneficiary?.visibility = View.GONE

            if (state is NetworkState2.Success) {
                val filteredData = state.data?.filter { beneficiary ->
                    when (aadcServiceType) {
                        ACCOUNTID, PERSONID, MOBILENO, EMIRATESID -> {
                            beneficiary.type == aadcServiceType
                        }
                        else -> {
                            beneficiary.type.isNullOrEmpty()
                        }
                    }
                }
                if (filteredData.isNullOrEmpty()) {
                    beneficiariesList.clear()
                    return@Observer
                }
                tvBeneficiary?.visibility = View.VISIBLE
                beneficiariesList.apply { clear(); addAll(filteredData) }
            } else if (state is NetworkState2.Error) {
                if (state.isSessionExpired) {
                    activity?.onSessionExpired(state.message)
                    activity?.showMessage(state.message, R.drawable.ic_warning, R.color.colorAccent,
                            AlertDialogFragment.OnConfirmListener {
                        activity?.startLoginActivity()
                                activity?.finish()
                                return@OnConfirmListener
                    })
                    return@Observer
                }
                activity?.handleErrorCode(state.errorCode.toInt(), state.message)
            }
        })
    }

    private fun showBeneficiaries() {

        if (beneficiariesList.isNullOrEmpty()) return

        val bottomSheet = BeneficiaryBottomSheet
                .newInstance(beneficiariesList, getLogo(operator)) { mobileNo1, _, _ ->
                    if (aadcServiceType.equals(MOBILENO, true))
                        etAccountNo?.setText(PhoneUtils.extractMobileNo(mobileNo1, "0"))
                    else
                        etAccountNo?.setText(mobileNo1)
                }


        bottomSheet.attachPopUpListener { beneficiary, view ->
            val menu = showPopUpMenu(beneficiary, view)
            menu.show()

            menu.setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.edit ->  {
                        bottomSheet.dismiss()
                        val intent = Intent(activity, Add2BeneficiaryActivity::class.java)
                        intent.putExtra("beneficiary_data", beneficiary)
                        intent.putExtra("action", "edit")
                        intent.putExtra("number_len", beneficiary.mobileNumber.length)
                        if (operator.equals(UtilityServiceType.AADC, ignoreCase = true))
                            intent.putExtra("category", aadcServiceType)
                        else
                            intent.putExtra("category", UtilityServiceType.FEWA)
                        intent.putExtra("issue", aadcServiceType)
                        startActivityForResult(intent, REQUEST_CODE_ADD_BENEFICIARY)
                        true
                    }
                    R.id.delete -> {
                        bottomSheet.dismiss()
                        addUpdateObserver()
                        showDeleteConfirmation(beneficiary)
                        true
                    }
                    R.id.enable -> {
                        bottomSheet.dismiss()
                        addUpdateObserver()
                        showEnableConfirmation(beneficiary)
                        true
                    }
                    else -> false
                }
            }
        }

        bottomSheet.show(activity?.supportFragmentManager, bottomSheet.tag)
    }

    private fun showDeleteConfirmation(beneficiary: Beneficiaries) {
        val termsAndCondDialog = TermsAndConditionsDialog.Builder()
                .setMessage(String.format(getString(R.string.delele_beneficiary_confirmation), beneficiary.shortName))
                .setMessageGravity(Gravity.CENTER)
                .setPositiveText(getString(R.string.delete))
                .setNegativeText(getString(R.string.cancel))
                .attachPositiveListener {
                    val user = UserPreferences.instance.getUser(requireContext()) ?: return@attachPositiveListener
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

    @DrawableRes
    private fun getLogo(operator: String?): Int {
        if (operator == null) return R.drawable.ic_operator

        return when (operator.toLowerCase()) {
            UtilityServiceType.DEWA -> {
                R.drawable.ic_dewa
            }
            UtilityServiceType.FEWA -> {
                R.drawable.ic_fewa_
            }
            UtilityServiceType.AADC -> {
                R.drawable.ic_addc
            }
            else -> R.drawable.ic_operator
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            beneficiariesList.clear()
            tvAdd2Beneficiary?.visibility = View.GONE
            tvBeneficiary?.visibility = View.GONE
            addBeneficiariesObserver()
            fetchBeneficiaries()
        }
    }

    private fun fetchBeneficiaries() {
        val user = user ?: return
        getViewModel()?.getBeneficiaryAccounts(user.token, user.sessionId, user.refreshToken,
                user.customerId.toLong(), typeId, operator ?: return)
    }

    private fun onSuccess(activity: FragmentActivity) {
        if (activity is UtilitiesActivity) {
            if (getActivity() != null)
                (getActivity() as UtilitiesActivity).navigateUp()
        }
    }

    private fun getEditMaxLength(): Int {

        when (aadcServiceType) {

            ACCOUNTID -> {
                return 8
            }
            EMIRATESID -> {
                return 15
            }
            PERSONID -> {
                return 10
            }
            MOBILENO -> {
                return 10
            }
            else -> {
                return 8
            }
        }
    }
}
