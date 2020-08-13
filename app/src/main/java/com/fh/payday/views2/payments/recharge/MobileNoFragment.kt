package com.fh.payday.views2.payments.recharge

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.constraint.ConstraintLayout
import android.support.design.widget.TextInputEditText
import android.support.v4.app.Fragment
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
import java.util.*

class MobileNoFragment : Fragment() {
    private lateinit var btnNext: Button
    private var activity: MobileRechargeActivity? = null
    private var operator: String? = null
    private lateinit var accessKey: String
    private var serviceType: String? = null
    private var serviceType1: String? = null
    private var typeId: Int = 0
    private lateinit var constraintLayout: ConstraintLayout
    private var etMobileNumber: TextInputEditText? = null
    private lateinit var tvPrefix: TextView
    private lateinit var tvError: TextView
    private lateinit var tvLabel: TextView
    private lateinit var tvInput: TextView
    private var formatValidation = true
    private var tvRecentAccounts: TextView? = null
    private var tvAdd2Beneficiary: TextView? = null
    private lateinit var mobileNumber: String

    companion object {
        const val DU_PREPAID = "DUPREPAID"
        const val DU_POSTPAID = "DUPOSTPAID"
    }

    private val beneficiaryViewModel by lazy {
        ViewModelProviders.of(this).get(Add2BeneficiaryViewModel::class.java)
    }
    private var beneficiariesList: ArrayList<Beneficiaries> = ArrayList()

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context as MobileRechargeActivity
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (!isVisibleToUser && etMobileNumber != null) {
//            etMobileNumber?.text = null
            tvError.removeErrorMessage()
//            activity.clearRadioButtons()
        }

        if (isVisibleToUser && activity != null) {
            activity?.viewModel?.AMOUNT?.value = null
        }

        val activity = activity ?: return
        activity.viewModel ?: return

        if (isVisibleToUser && activity.viewModel.dataClear) {
            etMobileNumber?.text = null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = activity ?: return
        activity.viewModel.TYPE_ID.observe(this, Observer {
            this.typeId = it ?: return@Observer
            tvLabel.text = getString(R.string.enter_mobile_number_payments)

            if (operator.equals("du", ignoreCase = true) && typeId == 1) {
                etMobileNumber?.setMaxLength(9)
                fetchBeneficiaries().also { addBeneficiariesObserver() }
            }

            if (operator.equals("du", ignoreCase = true) && typeId == 2) {
                tvRecentAccounts?.visibility = View.GONE
            }

            if (operator.equals("etisalat", ignoreCase = true) && typeId == 2) {
                tvRecentAccounts?.visibility = View.GONE
            }

            if (operator.equals("du", ignoreCase = true) && (typeId == 1 || typeId == 2) ||
                    typeId == 2 && operator.equals("etisalat", ignoreCase = true)) {
                tvInput.visibility = View.VISIBLE
                tvPrefix.text = getString(R.string.du_prefix_no)
                // fetchBeneficiaries().also { addBeneficiariesObserver() }

            } else {
                tvInput.visibility = View.GONE
                tvPrefix.text = null
            }

            if (operator != null && typeId > 0 && !activity.viewModel.isLoading) {
            }
        })

        activity.viewModel.SERVICE.observe(this, Observer { s ->
            serviceType1 = when (s?.toUpperCase()) {
                UAEServiceType.TIME, UAEServiceType.CREDIT,
                UAEServiceType.INTERNATIONAL, UAEServiceType.DATA -> {
                    DU_PREPAID
                }
                else -> {
                    s
                }
            }
            serviceType = s
            handleEditTextLen(s)
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_mobile_number, container, false)
        init(view)

        operator = arguments?.getString("operator") ?: return null

        addObserver()
        addDetailObserver()
        addBillStateEtisalatObserver()
        addRechargeStateObserver()
        addBillStateDuObserver()

        addTextWatcher()

        tvRecentAccounts?.text = getString(R.string.beneficiary)
        tvRecentAccounts?.setTextWithUnderLine(tvRecentAccounts?.text.toString())
        tvAdd2Beneficiary?.setTextWithUnderLine(tvAdd2Beneficiary?.text.toString())

        val activity = activity ?: return view

        showLabel(typeId, activity.viewModel.SERVICE.value)


        if (activity.viewModel.mobileNumber != null) {
            etMobileNumber?.setText(activity.viewModel.mobileNumber)
        }

        btnNext.setOnClickListener {
            val mobileNoPrefix = tvPrefix.text.toString().replace("+971-", "0")
            val mobileNo = etMobileNumber?.text.toString().trim()
            mobileNumber = mobileNoPrefix + mobileNo

            if (validate(mobileNumber, typeId, activity.viewModel?.SERVICE?.value)) {

                activity.viewModel?.MOBILE?.value = mobileNumber

                if (operator.equals("du", ignoreCase = true) && typeId == 1) {
                    if (!activity.isNetworkConnected()) {
                        activity.onFailure(activity.findViewById(R.id.card_view), getString(R.string.no_internet_connectivity))
                        return@setOnClickListener
                    }
                    startService()
                } else if (operator.equals("du", ignoreCase = true) && typeId == 2 && serviceType != null) {
                    if (!activity.isNetworkConnected()) {
                        activity.onFailure(activity.findViewById(R.id.card_view), getString(R.string.no_internet_connectivity))
                        return@setOnClickListener
                    }
                    startService()
                } else if (operator.equals("etisalat", ignoreCase = true) && serviceType != null && typeId != 0) {
                    if (!activity.isNetworkConnected()) {
                        activity.onFailure(activity.findViewById(R.id.card_view), getString(R.string.no_internet_connectivity))
                        return@setOnClickListener
                    }
                    startService()
                } else {
                    if (typeId == 0)
                        activity.onFailure(activity.findViewById(R.id.card_view)
                                ?: return@setOnClickListener, getString(R.string.please_select_service))
                    else
                        activity.onFailure(activity.findViewById(R.id.card_view)
                                ?: return@setOnClickListener, getString(R.string.please_select_plan))
                }
            }
        }
        activity.viewModel.SERVICE.observe(this, Observer { service ->
            if (service == null) return@Observer
            if (service.equals("BROADBAND", ignoreCase = true) ||
                    service.equals("ELIFE", ignoreCase = true) ||
                    service.equals("EVISION", ignoreCase = true)) {
                formatValidation = false
                tvLabel.setText(R.string.enter_account_number)
                tvPrefix.text = null
                tvInput.visibility = View.GONE
            } else {
                formatValidation = true
                tvLabel.setText(R.string.enter_mobile_number_payments)
                tvInput.visibility = View.VISIBLE
                tvPrefix.text = getString(R.string.du_prefix_no)
            }
        })

        return view
    }

    private fun getEditTextLen(serviceType: String?): Int {
        if (serviceType != null) {
            when (serviceType.toUpperCase()) {
                UAEServiceType.TIME, UAEServiceType.CREDIT,
                UAEServiceType.INTERNATIONAL, UAEServiceType.DATA -> {
                    return 9
                }
                UAEServiceType.WASEL -> {
                    return 8
                }
                UAEServiceType.ELIFE -> {
                    return 9
                }
                UAEServiceType.EVISION -> {
                    return 12
                }
                UAEServiceType.BROADBAND -> {
                    return 9
                }
                UAEServiceType.GSM -> {
                    return 9
                }
                else -> return 9
            }
        } else {
            return 9
        }
    }

    private fun handleEditTextLen(serviceType: String?) {
        tvError.removeErrorMessage()
        constraintLayout.clearErrorMsg()
        etMobileNumber?.text = null
        if (serviceType != null) {
            when (serviceType.toUpperCase()) {
                UAEServiceType.TIME, UAEServiceType.CREDIT,
                UAEServiceType.INTERNATIONAL, UAEServiceType.DATA -> {
                    fetchBeneficiaries().also { addBeneficiariesObserver() }
                    etMobileNumber?.setMaxLength(9)
                }
                UAEServiceType.WASEL -> {
                    fetchBeneficiaries().also { addBeneficiariesObserver() }
                    etMobileNumber?.setMaxLength(9)
                }
                UAEServiceType.ELIFE -> {
                    fetchBeneficiaries().also { addBeneficiariesObserver() }
                    etMobileNumber?.setMaxLength(9)
                }
                UAEServiceType.EVISION -> {
                    fetchBeneficiaries().also { addBeneficiariesObserver() }
                    etMobileNumber?.setMaxLength(12)
                }
                UAEServiceType.BROADBAND -> {
                    fetchBeneficiaries().also { addBeneficiariesObserver() }
                    etMobileNumber?.setMaxLength(9)
                }
                UAEServiceType.GSM -> {
                    fetchBeneficiaries().also { addBeneficiariesObserver() }
                    etMobileNumber?.setMaxLength(9)
                }
            }
        }
    }

    private fun addTextWatcher() {
        val activity = activity ?: return
        etMobileNumber?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                activity.viewModel?.mobileNumber = s.toString()
                tvError.removeErrorMessage()
                constraintLayout.clearErrorMsg()
            }

            override fun afterTextChanged(s: Editable?) {
                filterAccountNo(s.toString())
                if (activity.viewModel?.selectedAccessKey.isNullOrEmpty())
                    tvAdd2Beneficiary?.visibility = View.GONE
            }
        })

        tvAdd2Beneficiary?.setOnClickListener {
            val mobileNoPrefix = tvPrefix.text.toString().replace("+971-", "0")
            val mobileNo = etMobileNumber?.text.toString().trim()
            mobileNumber = mobileNoPrefix + mobileNo
            if (validate(mobileNumber, typeId, activity.viewModel?.SERVICE?.value)) {
                val mIntent = Intent(activity, Add2BeneficiaryActivity::class.java)
                val accessKey = activity.viewModel?.selectedAccessKey ?: return@setOnClickListener
                val prefix = tvPrefix.text.toString().replace("+971-", "0")
                val len = mobileNumber.length
                mIntent.putExtra("mobileNo", mobileNumber)
                mIntent.putExtra("access_key", accessKey)
                //mIntent.putExtra("pre_fix", prefix)
                mIntent.putExtra("number_len", len)
                if (operator.equals("du", ignoreCase = true) && typeId == 1)
                    mIntent.putExtra("issue", DU_POSTPAID)
                else mIntent.putExtra("issue", serviceType1?.toUpperCase())

                startActivityForResult(mIntent, REQUEST_CODE_ADD_BENEFICIARY)
            }
        }

        tvRecentAccounts?.setOnClickListener { showBeneficiaries() }
    }

    private fun filterAccountNo(text: String?) {
        val no: String = if (operator.equals("du", ignoreCase = true) && (typeId == 1 || typeId == 2)) {
            "0".plus(text)
        } else if (operator.equals("etisalat", ignoreCase = true) && typeId == 2 ||
                (operator.equals("etisalat", ignoreCase = true) && (typeId == 1 && "GSM" == serviceType))) {
            "0".plus(text)
        } else {
            text ?: return
        }
        when {
            beneficiariesList.any {

                it.mobileNumber == no.trim()
            } -> {
                tvAdd2Beneficiary?.visibility = View.GONE
                tvRecentAccounts?.visibility = View.VISIBLE
            }
            no.trim().length >= getEditTextLen(serviceType) -> {
                tvAdd2Beneficiary?.visibility = View.VISIBLE
                tvRecentAccounts?.visibility = View.GONE
            }
            beneficiariesList.isNotEmpty() -> {
                tvAdd2Beneficiary?.visibility = View.GONE
                tvRecentAccounts?.visibility = View.VISIBLE
            }
            else -> {
                tvAdd2Beneficiary?.visibility = View.GONE
                tvRecentAccounts?.visibility = View.GONE
            }
        }
    }

    private fun startService() {
        val activity = activity ?: return
        val user = activity.viewModel.user
        if (user != null) {
            activity.disableButtons()
            activity.viewModel.operatorRequest(user.token, user.sessionId,
                    user.refreshToken, user.customerId.toLong(), "operators", typeId, "971")
        }
    }

    private fun init(view: View) {
        btnNext = view.findViewById(R.id.btn_next)
        constraintLayout = view.findViewById(R.id.cl_custom_layout)
        etMobileNumber = view.findViewById(R.id.et_mobile_number)
        tvPrefix = view.findViewById(R.id.tv_prefix)
        tvError = view.findViewById(R.id.tv_error_message)

        tvLabel = view.findViewById(R.id.tv_mobile_number)
        tvRecentAccounts = view.findViewById(R.id.tv_recent_accounts)
        tvAdd2Beneficiary = view.findViewById(R.id.tv_add_to_beneficiary)
        tvInput = view.findViewById(R.id.tv_input)

        val activity = activity ?: return

        activity.setOnFocusListener(etMobileNumber ?: return, constraintLayout)


        //        activity.getViewModel().getSERVICE().observe(this, service -> {
        //            if (service == null) return;
        //            if (service.equalsIgnoreCase("BROADBAND") || service.equalsIgnoreCase("ELIFE") || service.equalsIgnoreCase("EVISION")) {
        //                formatValidation = false;
        //                tvLabel.setText(R.string.enter_account_number);
        //            } else {
        //                formatValidation = true;
        //                tvLabel.setText(R.string.enter_mobile_number);
        //            }
        //        });
    }

    private fun validateMobileNo(serviceType: ServiceType, mobileNo: String): Boolean {

        when (serviceType) {
            ServiceType.ELIFE -> if (TextUtils.isEmpty(mobileNo)) {
                tvError.addErrorMessage(getString(R.string.empty_account_no))
                constraintLayout.onErrorMsg()
                return false
            } else if (mobileNo.length != 9) {
                tvError.addErrorMessage(getString(R.string.account_no_nine))
                constraintLayout.onErrorMsg()
                return false
            } else if (!MobileNoValidator.validateElifeNo(mobileNo)) {
                tvError.addErrorMessage(getString(R.string.invalid_account_no))
                constraintLayout.onErrorMsg()
                return false
            }
            ServiceType.BROADBAND -> if (TextUtils.isEmpty(mobileNo)) {
                tvError.addErrorMessage(getString(R.string.empty_account_no))
                constraintLayout.onErrorMsg()
                return false
            } else if (mobileNo.length != 9) {
                tvError.addErrorMessage(getString(R.string.account_no_nine))
                constraintLayout.onErrorMsg()
                return false
            } else if (!MobileNoValidator.validateBroadbandNo(mobileNo)) {
                tvError.addErrorMessage(getString(R.string.invalid_account_no))
                constraintLayout.onErrorMsg()
                return false
            }
            ServiceType.EVISION -> if (TextUtils.isEmpty(mobileNo)) {
                tvError.addErrorMessage(getString(R.string.empty_account_no))
                constraintLayout.onErrorMsg()
                return false
            } else if (mobileNo.length != 12) {
                tvError.addErrorMessage(getString(R.string.long_account_no))
                constraintLayout.onErrorMsg()
                return false
            } else if (!mobileNo.matches("^[0-9]{12}$".toRegex())) {
                tvError.addErrorMessage(getString(R.string.invalid_account_no))
                return false
            }
        }
        return true
    }

    private fun validate(mobileNo: String, typeId: Int, serviceType: String?): Boolean {
        val activity = activity ?: return false
        when (typeId) {
            1 -> if (serviceType != null) {
                when (serviceType.toUpperCase()) {
                    UAEServiceType.ELIFE -> {
                        tvLabel.setText(R.string.enter_account_number)
                        return validateMobileNo(ServiceType.ELIFE, mobileNo)
                    }
                    UAEServiceType.BROADBAND -> {
                        tvLabel.setText(R.string.enter_account_number)
                        return validateMobileNo(ServiceType.BROADBAND, mobileNo)
                    }
                    UAEServiceType.EVISION -> {
                        tvLabel.setText(R.string.enter_account_number)
                        return validateMobileNo(ServiceType.EVISION, mobileNo)
                    }
                    else -> {
                        tvLabel.setText(R.string.enter_mobile_number_payments)
                        if (TextUtils.isEmpty(mobileNo) || mobileNo.length <= 1) {
                            tvError.addErrorMessage(getString(R.string.empty_mobile_no_payments))
                            constraintLayout.onErrorMsg()
                            return false
                        } else if (mobileNo.length != 10) {
                            tvError.addErrorMessage(getString(R.string.uae_mobile_length))
                            constraintLayout.onErrorMsg()
                            return false
                        } else if (!formatValidation) {
                            return true
                        } else if (!MobileNoValidator.validate(mobileNo)) {
                            tvError.addErrorMessage(getString(R.string.invalid_mobile_no))
                            constraintLayout.onErrorMsg()
                            //addErrorMessage(tvError, getString(R.string.number_format_error));
                            return false
                        }
                    }
                }
            } else {
                tvLabel.setText(R.string.enter_mobile_number_payments)
                if (operator.equals("etisalat", ignoreCase = true)) {
                    activity.onFailure(constraintLayout, getString(R.string.please_select_plan))
                    return false
                } else if (TextUtils.isEmpty(mobileNo) || mobileNo.length <= 1) {
                    tvError.addErrorMessage(getString(R.string.empty_mobile_no_payments))
                    constraintLayout.onErrorMsg()
                    return false
                } else if (mobileNo.length != 10) {
                    tvError.addErrorMessage(getString(R.string.uae_mobile_length))
                    constraintLayout.onErrorMsg()
                    return false
                } else if (!formatValidation) {
                    return true
                } else if (!MobileNoValidator.validate(mobileNo)) {
                    tvError.addErrorMessage(getString(R.string.invalid_mobile_no))
                    constraintLayout.onErrorMsg()
                    return false
                }
            }
            2 -> if (serviceType != null) {
                if (TextUtils.isEmpty(mobileNo) || mobileNo.length <= 1) {
                    tvError.addErrorMessage(getString(R.string.empty_mobile_no_payments))
                    constraintLayout.onErrorMsg()
                    return false
                } else if (mobileNo.length != 10) {
                    tvError.addErrorMessage(getString(R.string.uae_mobile_length))
                    constraintLayout.onErrorMsg()
                    return false
                } else if (!formatValidation) {
                    return true
                } else if (!MobileNoValidator.validate(mobileNo)) {
                    tvError.addErrorMessage(getString(R.string.invalid_mobile_no))
                    constraintLayout.onErrorMsg()
                    return false
                }
            } else {
                activity.onFailure(constraintLayout, getString(R.string.please_select_plan))
                return false
            }
        }
        return true
    }

    private fun showLabel(typeId: Int, serviceType: String?) {
        when (typeId) {
            1 -> if (serviceType != null) {
                when (serviceType.toUpperCase()) {
                    UAEServiceType.ELIFE -> {
                        tvInput.visibility = View.GONE
                        tvPrefix.text = null
                    }
                    UAEServiceType.BROADBAND -> {
                        tvInput.visibility = View.GONE
                        tvPrefix.text = null
                    }
                    UAEServiceType.EVISION -> {
                        tvInput.visibility = View.GONE
                        tvPrefix.text = null
                    }
                    else -> {
                        tvInput.visibility = View.VISIBLE
                        tvPrefix.text = getString(R.string.du_prefix_no)
                    }
                }
            } else {
                tvPrefix.text = getString(R.string.du_prefix_no)
                tvInput.visibility = View.VISIBLE

            }
            2 -> {
                tvPrefix.text = getString(R.string.du_prefix_no)
                tvInput.visibility = View.VISIBLE
            }
        }
    }

    private fun addObserver() {

        val activity = activity ?: return
        activity.viewModel.operaterState.observe(this, Observer { event ->
            if (event == null) return@Observer

            val operatorState = event.getContentIfNotHandled() ?: return@Observer

            if (operatorState is NetworkState2.Loading<*>) {

                activity.showProgress(getString(R.string.processing))
                btnNext.isEnabled = false
                return@Observer
            }

            btnNext.isEnabled = true

            if (operatorState is NetworkState2.Success<*>) {

                val data = (operatorState as NetworkState2.Success<List<Operator>>).data

                val user = activity.viewModel.user
                if (user != null) {

                    for (i in data?.indices ?: return@Observer) {
                        if (data[i].serviceProvider.equals(operator, ignoreCase = true)) {
                            activity.viewModel.operator = operator
                            accessKey = data[i].accessKey
                            activity.viewModel.operatorDetails(user.token, user.sessionId, user.refreshToken, user.customerId.toLong(),
                                    data[i].accessKey, typeId, "flexi", "971")
                            break
                        }
                    }
                }
            } else if (operatorState is NetworkState2.Error<*>) {
                activity.hideProgress()
                activity.enableButtons()
                val (message, _, isSessionExpired) = operatorState
                if (isSessionExpired) {
                    activity.onSessionExpired(message)
                    return@Observer
                }
//                activity.onError(message)
                activity.handleErrorCode(Integer.parseInt(operatorState.errorCode), operatorState.message)

            } else if (operatorState is NetworkState2.Failure<*>) {

                activity.hideProgress()
                activity.enableButtons()
                val (throwable) = operatorState
                activity.onFailure(activity.findViewById<View>(R.id.card_view), throwable)

            } else {
                activity.hideProgress()
                activity.enableButtons()
                activity.onFailure(activity.findViewById<View>(R.id.card_view), CONNECTION_ERROR)
            }
        })
    }

    private fun addDetailObserver() {
        val activity = activity ?: return

        activity.viewModel.operatorDetailState.observe(this, Observer { event ->
            if (event == null) return@Observer

            val operatorDetailState = event.getContentIfNotHandled() ?: return@Observer

            if (operatorDetailState is NetworkState2.Loading<*>) {
                btnNext.isEnabled = false
                return@Observer
            }

            btnNext.isEnabled = true

            val user = activity.viewModel.user ?: return@Observer

            if (operatorDetailState is NetworkState2.Success<*>) {

                val data = (operatorDetailState as NetworkState2.Success<OperatorDetail>).data

                if (activity.viewModel == null) return@Observer

                if (operator!!.equals("etisalat", ignoreCase = true) && activity.viewModel.TYPE_ID.value == 2) {

                    activity.viewModel.rechargeDetailEtisalat(user.token, user.sessionId, user.refreshToken, user.customerId.toLong(),
                            accessKey, "getBalance", typeId, activity.viewModel?.MOBILE?.value
                            ?: return@Observer, data?.flexiKey
                            ?: return@Observer, Integer.parseInt(data.typeKey), activity.viewModel.SERVICE.value
                            ?: return@Observer)
                } else if (operator!!.equals("etisalat", ignoreCase = true) && activity.viewModel.TYPE_ID.value == 1) {

                    activity.viewModel.billDetailEtisalat(user.token, user.sessionId, user.refreshToken, user.customerId.toLong(),
                            accessKey, "getBalance", typeId,
                            activity.viewModel.MOBILE.value ?: return@Observer,
                            data?.flexiKey ?: return@Observer, Integer.parseInt(data.typeKey),
                            activity.viewModel?.SERVICE?.value ?: return@Observer)
                } else if (operator!!.equals("du", ignoreCase = true) && activity.viewModel.TYPE_ID.value == 1) {

                    activity.viewModel.billBalanceDu(user.token, user.sessionId, user.refreshToken, user.customerId.toLong(),
                            accessKey, "getBalance", typeId, activity.viewModel.MOBILE.value!!,
                            data!!.flexiKey, Integer.parseInt(data.typeKey))
                } else {
                    activity.hideProgress()
                    (getActivity() as MobileRechargeActivity).onMobileNoSelected()
                }

            } else if (operatorDetailState is NetworkState2.Error<*>) {
                activity.hideProgress()
                activity.enableButtons()
                val (message, _, isSessionExpired) = operatorDetailState
                if (isSessionExpired) {
                    activity.onSessionExpired(message)
                    return@Observer
                }
//                activity.onError(message)
                activity.handleErrorCode(Integer.parseInt(operatorDetailState.errorCode), operatorDetailState.message)

            } else if (operatorDetailState is NetworkState2.Failure<*>) {
                activity.hideProgress()
                activity.enableButtons()
                val (throwable) = operatorDetailState
                activity.onFailure(activity.findViewById<View>(R.id.card_view), throwable)

            } else {
                activity.hideProgress()
                activity.enableButtons()
                activity.onFailure(activity.findViewById<View>(R.id.card_view), CONNECTION_ERROR)
            }

        })
    }

    private fun addRechargeStateObserver() {

        val activity = activity ?: return

        activity.viewModel.rechargeState.observe(this, Observer { event ->
            if (event == null) {
                return@Observer
            }

            val billState = event.getContentIfNotHandled() ?: return@Observer

            if (billState is NetworkState2.Loading<*>) {
                btnNext.isEnabled = false
                return@Observer
            }

            activity.hideProgress()
            btnNext.isEnabled = true

            when (billState) {
                is NetworkState2.Success<*> -> //val data = (billState as NetworkState2.Success<RechargeDetail>).data
                    (getActivity() as MobileRechargeActivity).onMobileNoSelected()
                is NetworkState2.Error<*> -> {

                    activity.enableButtons()
                    val (message, _, isSessionExpired) = billState
                    if (isSessionExpired) {
                        activity.onSessionExpired(message)
                        return@Observer
                    }
//                    activity.onError(message)
                    activity.handleErrorCode(Integer.parseInt(billState.errorCode), billState.message)

                }
                is NetworkState2.Failure<*> -> {

                    activity.enableButtons()
                    val (throwable) = billState
                    activity.onFailure(activity.findViewById<View>(R.id.card_view), throwable)

                }
                else -> {

                    activity.enableButtons()
                    activity.onFailure(activity.findViewById<View>(R.id.card_view), CONNECTION_ERROR)
                }
            }
        })
    }


    private fun addBillStateEtisalatObserver() {
        val activity = activity ?: return

        activity.viewModel.billStateEtisalat.observe(this, Observer { event ->
            if (event == null) {
                return@Observer
            }

            val billState = event.getContentIfNotHandled() ?: return@Observer

            if (billState is NetworkState2.Loading<*>) {
                btnNext.isEnabled = false
                return@Observer
            }

            activity.hideProgress()
            btnNext.isEnabled = true

            when (billState) {
                is NetworkState2.Success<*> ->
                    (getActivity() as MobileRechargeActivity).onMobileNoSelected()
                is NetworkState2.Error<*> -> {

                    activity.enableButtons()
                    val (message, _, isSessionExpired) = billState
                    if (isSessionExpired) {
                        activity.onSessionExpired(message)
                        return@Observer
                    }
//                    activity.onError(message)
                    activity.handleErrorCode(Integer.parseInt(billState.errorCode), billState.message)
                }
                is NetworkState2.Failure<*> -> {

                    activity.enableButtons()
                    activity.onFailure(activity.findViewById<View>(R.id.card_view), billState.throwable)

                }
                else -> {

                    activity.enableButtons()
                    activity.onFailure(activity.findViewById<View>(R.id.card_view), CONNECTION_ERROR)
                }
            }
        })
    }

    private fun addBillStateDuObserver() {
        val activity = activity ?: return

        activity.viewModel.billStateDu.observe(this, Observer { event ->
            if (event == null) return@Observer

            val billState = event.getContentIfNotHandled() ?: return@Observer

            if (billState is NetworkState2.Loading<*>) {
                btnNext.isEnabled = false
                return@Observer
            }

            activity.hideProgress()
            btnNext.isEnabled = true

            when (billState) {
                is NetworkState2.Success<*> -> {
                    val data = (billState as NetworkState2.Success<BillDetailDU>).data
                    if (data != null)
                        if (data.Balance != null)
                            (getActivity() as MobileRechargeActivity).onMobileNoSelected()
                        else {
                            activity.onError(getString(R.string.generic_payment_error))
                            activity.enableButtons()
                        }
                }
                is NetworkState2.Error<*> -> {

                    activity.enableButtons()
                    val (message, _, isSessionExpired) = billState
                    if (isSessionExpired) {
                        activity.onSessionExpired(message)
                        return@Observer
                    }
//                    activity.onError(message)
                    activity.handleErrorCode(Integer.parseInt(billState.errorCode), billState.message)
                }
                is NetworkState2.Failure<*> -> {
                    activity.enableButtons()
                    activity.onFailure(activity.findViewById<View>(R.id.card_view), billState.throwable)
                }
                else -> {
                    activity.enableButtons()
                    activity.onFailure(activity.findViewById<View>(R.id.card_view), CONNECTION_ERROR)
                }
            }
        })
    }

    private fun addBeneficiariesObserver() {
        val activity = this.activity ?: return

        activity.viewModel.beneficiariesState.observe(this, Observer { event ->
            if (event == null) return@Observer

            val state = event.peekContent()

            tvRecentAccounts?.visibility = View.GONE
            tvAdd2Beneficiary?.visibility = View.GONE

            when (state) {
                is NetworkState2.Success<*> -> {
                    val data = (state as NetworkState2.Success<ArrayList<Beneficiaries>>).data

                    val filteredData = state.data?.filter { beneficiary ->

                        if (operator.equals("du", ignoreCase = true) && (typeId == 2)) {
                            beneficiary.type == DU_PREPAID
                        } else if (operator.equals("du", ignoreCase = true) && (typeId == 1)) {
                            beneficiary.type == DU_POSTPAID

                        } else {
                            when (activity.viewModel.SERVICE.value.toString().toUpperCase()) {

                                UAEServiceType.TIME, UAEServiceType.CREDIT,
                                UAEServiceType.INTERNATIONAL, UAEServiceType.DATA -> {
                                    beneficiary.type == DU_PREPAID
                                }
                                UAEServiceType.WASEL -> {
                                    beneficiary.type == UAEServiceType.WASEL
                                }
                                UAEServiceType.ELIFE -> {
                                    beneficiary.type == UAEServiceType.ELIFE
                                }
                                UAEServiceType.EVISION -> {
                                    beneficiary.type == UAEServiceType.EVISION
                                }
                                UAEServiceType.BROADBAND -> {
                                    beneficiary.type == UAEServiceType.BROADBAND
                                }
                                UAEServiceType.GSM -> {
                                    beneficiary.type == UAEServiceType.GSM
                                }
                                else -> {
                                    beneficiary.type == DU_POSTPAID
                                }
                            }
                        }
                    }
                    if (filteredData.isNullOrEmpty()) {
                        tvRecentAccounts?.visibility = View.GONE
                        beneficiariesList.clear()
                        return@Observer
                    }
                    tvRecentAccounts?.visibility = View.VISIBLE
                    beneficiariesList.apply { clear(); addAll(filteredData) }
                }
                is NetworkState2.Error<*> -> {
                    val (message, _, isSessionExpired) = state

                    if (isSessionExpired) {
                        activity.onSessionExpired(message)
                        return@Observer
                    }
                    activity.handleErrorCode(Integer.parseInt(state.errorCode), state.message)
                }
            }
        })

        beneficiaryViewModel.deleteBeneficiaryState.observe(this, Observer {
            it ?: return@Observer
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

    @DrawableRes
    private fun getLogo(operator: String?): Int {
        if (operator == null) return R.drawable.ic_operator

        return when (operator.toLowerCase()) {
            "du" -> {
                R.mipmap.ic_du_xhdpi
            }
            "etisalat" -> {
                R.mipmap.ic_etisalat_xhdpi
            }
            else -> {
                R.drawable.ic_operator
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            tvAdd2Beneficiary?.visibility = View.GONE
            fetchBeneficiaries()
        }
    }

    private fun fetchBeneficiaries() {
        val activity = activity ?: return

        val user = activity.viewModel.user
        if (user != null) {
            activity.viewModel.getBeneficiaryAccounts(user.token, user.sessionId, user.refreshToken,
                    user.customerId.toLong(),
                    typeId, operator ?: return)
        }
    }

    private fun showBeneficiaries() {

        if (beneficiariesList.isNullOrEmpty()) return
        val activity = activity ?: return

        val bottomSheet = BeneficiaryBottomSheet
                .newInstance(beneficiariesList, getLogo(operator)) { mobileNo1, _, _ ->
                    val accessKey = activity.viewModel.selectedAccessKey
                    val service = activity.viewModel.SERVICE.value

                    if ("etisalat_bill".equals(accessKey, ignoreCase = true) &&
                            !"GSM".equals(service, ignoreCase = true)) {
                        etMobileNumber?.setText(mobileNo1)
                    } else {
                        etMobileNumber?.setText(PhoneUtils.extractMobileNo(
                                mobileNo1, "0"))
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
                            putExtra("number_len", beneficiary.mobileNumber.length)
                            if (operator.equals("du", ignoreCase = true) && typeId == 1)
                                putExtra("issue", DU_POSTPAID)
                            else putExtra("issue", serviceType1?.toUpperCase())
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

    enum class ServiceType {
        ELIFE, EVISION, BROADBAND
    }
}
