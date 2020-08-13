package com.fh.payday.views2.payments.internationaltopup

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.TextInputEditText
import android.support.v4.app.FragmentActivity
import android.text.Editable
import android.text.InputFilter
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
import com.fh.payday.datasource.models.payments.Beneficiaries
import com.fh.payday.datasource.models.payments.INTERNATIONAL_TOPUP
import com.fh.payday.datasource.models.payments.PlanType
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.*
import com.fh.payday.viewmodels.Add2BeneficiaryViewModel
import com.fh.payday.views2.loan.apply.TermsAndConditionsDialog
import com.fh.payday.views2.payments.BeneficiaryBottomSheet
import com.fh.payday.views2.shared.activity.Add2BeneficiaryActivity
import com.fh.payday.views2.shared.activity.REQUEST_CODE_ADD_BENEFICIARY
import kotlinx.android.synthetic.main.fragment_internl_mobile_no.*
import java.util.regex.Pattern


class MobileNoFragment : BaseFragment() {

    private lateinit var constraintLayout: ConstraintLayout
    private var etAccoutno: TextInputEditText? = null
    private var tvPrefix: TextView? = null
    private var tvError: TextView? = null
    private var bttnNext: Button? = null
    private lateinit var tvAdd2Beneficiary: TextView
    private lateinit var tvBeneficiary: TextView
    private val beneficiaryViewModel by lazy {
        ViewModelProviders.of(this).get(Add2BeneficiaryViewModel::class.java)
    }
    private var beneficiariesList = ArrayList<Beneficiaries>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_internl_mobile_no, container, false)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        activity ?: return
        val activity = activity as InternationalTopUpActivity
        if (isVisibleToUser && activity.viewModel.dataClear) {
            etAccoutno?.text = null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleView(view)
        attachListener()
        attachObserver()

        val viewModel = getViewModel() ?: return

        val (_, _, _, _, isFixedAvailable) = viewModel.selectedOperator ?: return
        val planType = if (isFixedAvailable == 1) PlanType.FIXED else PlanType.FLEXI

        val countryCode = viewModel.countryCode ?: return

        getOperatorDetails()

        tvPrefix?.text = String.format(getString(R.string.plus_sign), countryCode.toString().plus("-"))

        tvAdd2Beneficiary.setTextWithUnderLine(tvAdd2Beneficiary.text.toString())
        tvBeneficiary.setTextWithUnderLine(tvBeneficiary.text.toString())

        onChangeListener(etAccoutno ?: return)
        bttnNext?.setOnClickListener {

            val prefix = tvPrefix?.text.toString()
            val accountNo = prefix.plus(etAccoutno?.text.toString().trim())
            val regex: String? = getViewModel()?.validationRegex

            when (planType) {
                PlanType.FIXED -> {
                    val plans = getViewModel()?.plans
                    if (!plans.isNullOrEmpty()) viewModel.setAccountNo(accountNo, plans[0].currency)
                }
                PlanType.FLEXI -> {
                    val data = getViewModel()?.operatorDetailsFlexi ?: return@setOnClickListener
                    viewModel.setAccountNo(accountNo, data.baseCurrency)
                }
            }
            if (validateEditText(accountNo, prefix, regex ?: return@setOnClickListener)) {
                val activity = getActivity() as BaseActivity? ?: return@setOnClickListener
                onSuccess(activity)
            }
        }
    }

    private fun getOperatorDetails() {
        activity?.hideNoInternetView()
        if (activity?.isNetworkConnected() == false) {
            constraint_layout.visibility = View.GONE
            return activity?.showNoInternetView { getOperatorDetails() } ?: Unit
        }

        constraint_layout.visibility = View.VISIBLE

        val viewModel = getViewModel() ?: return
        val user = viewModel.user ?: return
        val (_, accessKey, _, _, isFixedAvailable) = viewModel.selectedOperator ?: return
        val planType = if (isFixedAvailable == 1) PlanType.FIXED else PlanType.FLEXI

        val countryCode = viewModel.countryCode ?: return

        viewModel.operatorDetails(user.token, user.sessionId, user.refreshToken, user.customerId.toLong(),
                accessKey, viewModel.typeId ?: return, planType, countryCode.toString())

    }

    private fun handleView(view: View) {
        etAccoutno = view.findViewById(R.id.et_mobile_number)
        bttnNext = view.findViewById(R.id.btn_next)
        tvError = view.findViewById(R.id.tv_error_message)
        tvPrefix = view.findViewById(R.id.tv_prefix)
        constraintLayout = view.findViewById(R.id.cl_custom_layout)
        tv_account_no.text = getString(R.string.enter_mobile_number_payments)
        tvBeneficiary = view.findViewById(R.id.tv_beneficiary)
        tvAdd2Beneficiary = view.findViewById(R.id.tv_add_to_beneficiary)
        //et_mobile_number.setMaxLength(14)
    }

    private fun getViewModel() =
            when (activity) {
                is InternationalTopUpActivity -> (activity as InternationalTopUpActivity?)?.viewModel
                else -> null
            }

    private fun onChangeListener(editText: TextInputEditText) {
        editText.filters = arrayOf(InputFilter { source, sourceStart, sourceEnd, destination, destinationStart, destinationEnd ->
            val pattern = Pattern.compile(getViewModel()?.validationRegex.toString())
            val countryCode = getViewModel()?.countryCode.toString()
            val textToCheck = countryCode.plus(destination.subSequence(0, destinationStart).toString() +
                    source.subSequence(sourceStart, sourceEnd) +
                    destination.subSequence(destinationEnd, destination.length).toString())

            val matcher = pattern.matcher(textToCheck)

            if (!matcher.matches()) {
                if (!matcher.hitEnd()) {
                    return@InputFilter ""
                }
            }

            null
        })
    }

    private fun attachListener() {
        etAccoutno?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                val value = getViewModel()?.countryCode.toString().plus(editable.toString())

                if (value.matches(getViewModel()?.validationRegex?.toRegex() ?: return)) {
                    etAccoutno?.setTextColor(Color.BLACK)
                } else {
                    etAccoutno?.setTextColor(Color.RED)
                    return
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tvError?.removeErrorMessage()
                constraintLayout.clearErrorMsg()
                handleBeneficiariesView(s?.toString())
            }
        })

        val operator = getViewModel()?.selectedOperator ?: return

        tvAdd2Beneficiary.setOnClickListener {

            val prefix = tvPrefix?.text.toString().replace("-", "")
            val accountNo = prefix.plus(etAccoutno?.text.toString().trim())

            val regexp = getViewModel()?.validationRegex ?: return@setOnClickListener

            if (validateEditText(accountNo, prefix, regexp)) {

                val mIntent = Intent(activity, Add2BeneficiaryActivity::class.java).apply {
                    putExtra("number_len", etAccoutno?.text.toString().length)
                    putExtra("mobileNo", etAccoutno?.text.toString())
                    putExtra("access_key", operator.accessKey)
                    putExtra("res_id", operator.serviceImage)
                    putExtra("category", INTERNATIONAL_TOPUP)
                }
                startActivityForResult(mIntent, REQUEST_CODE_ADD_BENEFICIARY)
            }
        }

        tvBeneficiary.setOnClickListener { showBeneficiaries() }

        activity?.setOnFocusListener(etAccoutno ?: return, constraintLayout)
    }

    fun handleBeneficiariesView(s: String?) {

        when {
            beneficiariesList.any { it.mobileNumber == s?.trim() } -> {
                tvAdd2Beneficiary.visibility = View.GONE
                tvBeneficiary.visibility = View.VISIBLE
            }
            s != null && s.trim().length >= 9 -> {
                tvAdd2Beneficiary.visibility = View.VISIBLE
                tvBeneficiary.visibility = View.GONE
            }
            beneficiariesList.isNotEmpty() -> {
                tvAdd2Beneficiary.visibility = View.GONE
                tvBeneficiary.visibility = View.VISIBLE
            }
            else -> {
                tvAdd2Beneficiary.visibility = View.GONE
                tvBeneficiary.visibility = View.GONE
            }
        }
    }

    private fun attachObserver() {

        getViewModel()?.operatorDetailFlexiState?.observe(this, Observer {
            it ?: return@Observer
            val activity = getActivity() as BaseActivity? ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                hideItem()
                progress_bar.visibility = View.VISIBLE
                return@Observer
            }

            showItem()
            progress_bar.visibility = View.GONE

            when (state) {
                is NetworkState2.Success -> {
                    fetchBeneficiaries().also { addBeneficiariesObserver() }
                }
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

        getViewModel()?.operatorDetailFixedState?.observe(this, Observer {
            it ?: return@Observer
            val activity = getActivity() as BaseActivity? ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                hideItem()
                progress_bar.visibility = View.VISIBLE
                return@Observer
            }

            showItem()
            progress_bar.visibility = View.GONE

            when (state) {
                is NetworkState2.Success -> {
                    fetchBeneficiaries().also { addBeneficiariesObserver() }
                }
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
    }

    private fun onSuccess(activity: FragmentActivity) {
        when (activity) {
            is InternationalTopUpActivity -> activity.navigateUp()
        }
    }

    private fun fetchBeneficiaries() {

        val viewModel = getViewModel() ?: return
        val user = viewModel.user ?: return
        val operator = viewModel.selectedOperator ?: return
        viewModel.getBeneficiaries(user.token, user.sessionId,
                user.refreshToken, user.customerId.toLong(), operator.accessKey)
    }

    private fun validateEditText(accountNo: String, prefix: String, regex: String): Boolean {

        if (TextUtils.isEmpty(accountNo)) {
            tvError?.addErrorMessage(getString(R.string.empty_mobile_no_payments))
            constraintLayout.onErrorMsg()
            etAccoutno?.requestFocus()
            return false
        } else if (accountNo == prefix) {
            tvError?.addErrorMessage(getString(R.string.empty_mobile_no_payments))
            constraintLayout.onErrorMsg()
            etAccoutno?.requestFocus()
            return false
        } else if (!accountNo.replace("+", "").replace("-", "").matches(regex.toRegex())) {
            tvError?.addErrorMessage(getString(R.string.invalid_mobile_no))
            constraintLayout.onErrorMsg()
            etAccoutno?.requestFocus()
            return false
        } else {
            tvError?.removeErrorMessage()
            constraintLayout.clearErrorMsg()
            return true
        }
    }

    private fun addBeneficiariesObserver() {
        getViewModel()?.beneficiariesState?.observe(this, Observer {
            it ?: return@Observer
            val state = it.peekContent()

            tvAdd2Beneficiary.visibility = View.GONE
            tvBeneficiary.visibility = View.GONE

            if (state is NetworkState2.Success) {
                val filteredData = state.data

                if (filteredData.isNullOrEmpty()) return@Observer

                tvBeneficiary.visibility = View.VISIBLE
                beneficiariesList.apply { clear(); addAll(filteredData) }
            } else if (state is NetworkState2.Error) {
                if (state.isSessionExpired) {
                    activity?.onSessionExpired(state.message)
                    return@Observer
                }

                activity?.handleErrorCode(state.errorCode.toInt(), state.message)
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
        val recentAccounts = beneficiariesList.map { it.logo = logo; it } as ArrayList

        val bottomSheet = BeneficiaryBottomSheet.newInstance(recentAccounts) { mobileNo, _, _ ->
            etAccoutno?.setText(mobileNo)
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
                            putExtra("category", INTERNATIONAL_TOPUP)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_ADD_BENEFICIARY -> fetchBeneficiaries()
            }
        }
    }

    private fun hideItem() {
        btn_next.visibility = View.GONE
        constraintLayout.visibility = View.GONE
        tv_account_no.visibility = View.GONE
    }

    private fun showItem() {
        btn_next.visibility = View.VISIBLE
        constraintLayout.visibility = View.VISIBLE
        tv_account_no.visibility = View.VISIBLE
    }
}