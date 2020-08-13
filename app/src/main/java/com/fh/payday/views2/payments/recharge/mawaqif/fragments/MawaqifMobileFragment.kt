package com.fh.payday.views2.payments.recharge.mawaqif.fragments

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
import com.fh.payday.datasource.models.payments.utilities.MAWAQIF_TOPUP
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.*
import com.fh.payday.viewmodels.Add2BeneficiaryViewModel
import com.fh.payday.views2.loan.apply.TermsAndConditionsDialog
import com.fh.payday.views2.payments.BeneficiaryBottomSheet
import com.fh.payday.views2.payments.recharge.mawaqif.MawaqifTopUpActivity
import com.fh.payday.views2.shared.activity.Add2BeneficiaryActivity
import com.fh.payday.views2.shared.activity.REQUEST_CODE_ADD_BENEFICIARY
import kotlinx.android.synthetic.main.fragment_mawaqif_mobile.*


class MawaqifMobileFragment : BaseFragment() {

    private var beneficiariesList = ArrayList<Beneficiaries>()
    private fun getViewModel() = when (activity) {
        is MawaqifTopUpActivity -> (activity as MawaqifTopUpActivity).viewModel
        else -> null
    }

    private val beneficiaryViewModel by lazy {
        ViewModelProviders.of(this).get(Add2BeneficiaryViewModel::class.java)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if (isVisibleToUser) {
            getViewModel()?.enteredAmount = null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
            : View? = inflater.inflate(R.layout.fragment_mawaqif_mobile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        attachListener()
        attachObserver()
        getMawaqifTopUpDetails()
        fetchBeneficiaries().also { addBeneficiariesObserver() }
        et_mobile_no.mask = "+971-#########"

        tv_add_to_beneficiary.setTextWithUnderLine(tv_add_to_beneficiary.text.toString())
        tv_beneficiary.setTextWithUnderLine(tv_beneficiary.text.toString())
        val activity = activity as MawaqifTopUpActivity
        btn_next.setOnClickListener {

            val mobileNo = et_mobile_no.text?.toString()?.replace("+971-", "0") ?: ""

            if (isValid(mobileNo)) {
                getViewModel()?.setMobileNo(mobileNo)
                val operator = getViewModel()?.selectedOperator ?: return@setOnClickListener
                val user = UserPreferences.instance.getUser(activity) ?: return@setOnClickListener
                val (_, accessKey, _, isFlexAvailable) = operator

                val planType = if (isFlexAvailable == 1) PlanType.FLEXI else PlanType.FIXED
                getViewModel()?.planType = planType
                getViewModel()?.accessKey = accessKey
                val typeId = getViewModel()?.typeId ?: return@setOnClickListener
                getViewModel()?.getOperatorDetails(user.token, user.sessionId, user.refreshToken,
                        user.customerId.toLong(), accessKey, typeId, planType, CountryCode.DEFAULT)

            }
        }
    }

    private fun attachListener() {

        et_mobile_no.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                til_account_no.clearErrorMessage()
                handleBeneficiariesView(s?.toString())

            }
        })

        tv_add_to_beneficiary.setOnClickListener {
            val account = et_mobile_no.text?.toString()?.replace("+971-", "0") ?: ""
            if (isValid(account)) {
                val operator = getViewModel()?.selectedOperator ?: return@setOnClickListener
                val mIntent = Intent(activity, Add2BeneficiaryActivity::class.java).apply {
                    putExtra("number_len", account.length)
                    putExtra("mobileNo", account)
                    putExtra("access_key", operator.accessKey)
                    putExtra("res_id", 1)
                    putExtra("category", MAWAQIF_TOPUP)
                    putExtra("issue", operator.serviceCategory)

                }
                startActivityForResult(mIntent, REQUEST_CODE_ADD_BENEFICIARY)
            }
        }

        tv_beneficiary.setOnClickListener { showBeneficiaries() }
    }

    private fun attachObserver() {

        getViewModel()?.operatorRequest?.observe(this, Observer {
            it ?: return@Observer
            val activity = activity as BaseActivity
            val state = it.getContentIfNotHandled() ?: return@Observer
            if (state is NetworkState2.Loading) {
                btn_next.visibility = View.GONE
                progress_bar.visibility = View.VISIBLE
                return@Observer
            }

            btn_next.visibility = View.VISIBLE
            progress_bar.visibility = View.GONE
            when (state) {
                is NetworkState2.Success -> {
                    fetchBeneficiaries().also { addBeneficiariesObserver() }
                }
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        return@Observer activity.onSessionExpired(state.message)
                    }
                    activity.handleErrorCode(state.errorCode.toInt(), state.message)
                }
                is NetworkState2.Failure -> activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
                else -> activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })

        getViewModel()?.operatorDetail?.observe(this, Observer {
            it ?: return@Observer
            val activity = activity as BaseActivity
            val state = it.getContentIfNotHandled() ?: return@Observer
            if (state is NetworkState2.Loading) {
                btn_next.visibility = View.GONE
                progress_bar.visibility = View.VISIBLE
                return@Observer
            }

            btn_next.visibility = View.VISIBLE
            progress_bar.visibility = View.GONE

            when (state) {
                is NetworkState2.Success -> onSuccess()
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        return@Observer activity.onSessionExpired(state.message)
                    }
                    activity.handleErrorCode(state.errorCode.toInt(), state.message)
                }
                is NetworkState2.Failure -> activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
                else -> activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })

        getViewModel()?.balanceState?.observe(this, Observer {
            it ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer
            val activity = activity ?: return@Observer
            if (state is NetworkState2.Loading) {
                btn_next.visibility = View.GONE
                progress_bar.visibility = View.VISIBLE
                return@Observer
            }

            btn_next.visibility = View.VISIBLE
            progress_bar.visibility = View.GONE

            when (state) {
                is NetworkState2.Success -> onSuccess(getActivity() ?: return@Observer)
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        activity.onSessionExpired(state.message)
                        return@Observer
                    }
                    activity.handleErrorCode(state.errorCode.toInt(), state.message)
                }
                is NetworkState2.Failure -> activity.onFailure(activity.findViewById(R.id.root_view)
                        ?: return@Observer, state.throwable)
                else -> activity.onFailure(activity.findViewById(R.id.root_view)
                        ?: return@Observer, CONNECTION_ERROR)
            }
        })
    }

    private fun onSuccess() {

        val (_, _, token, refreshToken, customerId, _, sessionId) = getViewModel()?.user ?: return
        val (_, _, _, _, flexiKey, typeKey) = getViewModel()?.operatorDetailsFlexi ?: return
        val typeId = getViewModel()?.typeId ?: return
        val accountNo = getViewModel()?.data?.value?.get("mobile_no") ?: return
        val accessKey = getViewModel()?.accessKey ?: return

        getViewModel()?.getBalance(token, sessionId, refreshToken, customerId.toLong(),
                accessKey, typeId, accountNo, flexiKey, typeKey.toInt())

    }

    private fun onSuccess(activity: FragmentActivity) {
        when (activity) {
            is MawaqifTopUpActivity ->
                activity.navigateUp()
        }
    }

    private fun getMawaqifTopUpDetails() {

        val activity = activity as BaseActivity
        val user = UserPreferences.instance.getUser(activity) ?: return
        getViewModel()?.getOperatorRequest(user.token, user.sessionId, user.refreshToken,
                user.customerId.toLong(), "operators", TypeId.TOP_UP, CountryCode.DEFAULT)
    }

    private fun fetchBeneficiaries() {
        val viewModel = getViewModel() ?: return
        val user = viewModel.user ?: return
        viewModel.getBeneficiaries(user.token, user.sessionId,
                user.refreshToken, user.customerId.toLong(), "mawaqif")
    }

    fun handleBeneficiariesView(s: String?) {

        val mobileNo = s?.replace("+971-", "0")?.trim()
        when {
            beneficiariesList.any { it.mobileNumber == mobileNo } -> {
                tv_add_to_beneficiary.visibility = View.GONE
                tv_beneficiary.visibility = View.VISIBLE
            }
            mobileNo != null && mobileNo.trim().length >= 8 -> {
                tv_add_to_beneficiary.visibility = View.VISIBLE
                tv_beneficiary.visibility = View.GONE
            }
            beneficiariesList.isNotEmpty() -> {
                tv_add_to_beneficiary.visibility = View.GONE
                tv_beneficiary.visibility = View.VISIBLE
            }
            else -> {
                tv_add_to_beneficiary.visibility = View.GONE
                tv_beneficiary.visibility = View.GONE
            }
        }
    }

    private fun addBeneficiariesObserver() {
        getViewModel()?.beneficiariesState?.observe(this, Observer {
            it ?: return@Observer
            val state = it.peekContent()

            tv_add_to_beneficiary.visibility = View.GONE
            tv_beneficiary.visibility = View.GONE

            if (state is NetworkState2.Success) {
                val filteredData = state.data?.filter { beneficiaries ->
                    ServiceCategory.TOP_UP.equals(beneficiaries.type, true)
                }

                if (filteredData.isNullOrEmpty()) return@Observer
                tv_beneficiary.visibility = View.VISIBLE
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
    }

    private fun showBeneficiaries() {

        if (beneficiariesList.isNullOrEmpty()) return

        val activity = activity ?: return
        val logo = getViewModel()?.selectedOperator?.serviceImage
        val (_, _, _, _) = getViewModel()?.selectedOperator ?: return

        val recentAccounts = beneficiariesList.map { it.logo = logo; it } as ArrayList

        val bottomSheet = BeneficiaryBottomSheet.newInstance(recentAccounts) { accountNo: String, _: String?, _: String? ->
            et_mobile_no?.setText(PhoneUtils.extractMobileNo(
                    accountNo, "0"))
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
                            putExtra("category", MAWAQIF_TOPUP)

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
                    val user = UserPreferences.instance.getUser(requireContext())
                            ?: return@attachPositiveListener
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
            tv_add_to_beneficiary.visibility = View.GONE
            when (requestCode) {
                REQUEST_CODE_ADD_BENEFICIARY -> fetchBeneficiaries()
            }
        }
    }

    private fun isValid(mobileNo: String) =
            when {
                mobileNo.trim().isEmpty() -> {
                    til_account_no.setErrorMessage(String.format(getString(R.string.empty_mobile_no)))
                    false
                }
                mobileNo.length < 10 -> {
                    til_account_no.setErrorMessage(String.format(getString(R.string.uae_mobile_length)))
                    false
                }
                !MobileNoValidator.validate(mobileNo) -> {
                    til_account_no.setErrorMessage(String.format(getString(R.string.invalid_mobile_no)))
                    false
                }
                else -> {
                    til_account_no.clearErrorMessage()
                    true
                }
            }
}
