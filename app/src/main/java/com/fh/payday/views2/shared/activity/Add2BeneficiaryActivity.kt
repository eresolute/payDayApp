package com.fh.payday.views2.shared.activity

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.constraint.ConstraintLayout
import android.support.design.button.MaterialButton
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.models.login.User
import com.fh.payday.datasource.models.payments.*
import com.fh.payday.datasource.models.payments.utilities.*
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.*
import com.fh.payday.viewmodels.Add2BeneficiaryViewModel
import com.fh.payday.views2.shared.custom.AlertDialogFragment
import com.mukesh.OtpView
import kotlinx.android.synthetic.main.activity_add_to_beneficiary.*
import kotlinx.android.synthetic.main.bottombar_dashboard.*
import kotlinx.android.synthetic.main.toolbar.*


const val REQUEST_CODE_ADD_BENEFICIARY = 176

class Add2BeneficiaryActivity : BaseActivity() {
    private lateinit var tvPrefix: TextView
    private lateinit var etMobileNo: TextInputEditText
    private lateinit var tilShortName: TextInputLayout
    private lateinit var etShortName: TextInputEditText
    private lateinit var ivOperator: ImageView
    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var tvError: TextView
    private lateinit var mobileNo: String
    private lateinit var user: User
    private lateinit var viewModel: Add2BeneficiaryViewModel
    private lateinit var btnSubmit: MaterialButton
    private lateinit var pinView: OtpView
    private lateinit var tvPinLabel: TextView

    override fun getLayout(): Int = R.layout.activity_add_to_beneficiary

    override fun init() {

        tvPrefix = findViewById(R.id.tv_prefix)
        etMobileNo = findViewById(R.id.et_mobile_number)
        constraintLayout = findViewById(R.id.cl_custom_layout)
        tvError = findViewById(R.id.tv_error_message)
        tilShortName = findViewById(R.id.til_short_name)
        etShortName = findViewById(R.id.et_short_name)
        btnSubmit = findViewById(R.id.btn_add_beneficiary)
        ivOperator = findViewById(R.id.iv_operator)
        pinView = findViewById(R.id.pin_view)
        tvPinLabel = findViewById(R.id.tv_pin_label)

        toolbar_back.setOnClickListener(this)
        toolbar_help.setOnClickListener(this)
        btm_home.setOnClickListener(this)
        btm_menu_branch.setOnClickListener(this)
        btm_menu_support.setOnClickListener(this)
        btm_menu_loan_calc.setOnClickListener(this)

        toolbar_back.setOnClickListener { onBackPressed() }
        toolbar_title.text = getString(R.string.add_to_beneficiary)

        addTextWatcher(etMobileNo, tilShortName)
        addTextWatcher(etShortName, tilShortName)

        pinView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tv_pin_error.visibility = View.GONE
            }

        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = UserPreferences.instance.getUser(this) ?: return
        viewModel = ViewModelProviders.of(this).get(Add2BeneficiaryViewModel::class.java)
        addObserver()
        val selectedMobileNo = intent.extras?.getString("mobileNo")
        val accessKey = intent.extras?.getString("access_key")
        val prefIx = intent.extras?.getString("pre_fix") ?: ""
        val len = intent.extras?.getInt("number_len") ?: 12
        val category = intent.extras?.getString("category") ?: getString(R.string.account_no_label)
        val type = intent.getStringExtra("issue") ?: null
        val action = intent.getStringExtra("action") ?: null

        when (type) {

            UAEServiceType.BROADBAND, UAEServiceType.ELIFE, UAEServiceType.EVISION ->{
                tv_mobile_no.text = getString(R.string.enter_account_number)
            }
            else -> {
                tv_mobile_no.text = getString(R.string.enter_mobile_number_payments)
            }
        }
        handleView(category)
        tvPrefix.text = prefIx
        etMobileNo.setMaxLength(len)
        etMobileNo.setText(selectedMobileNo)
        etMobileNo.isEnabled = false

        if (!action.isNullOrEmpty()){
            toolbar_title.text = getString(R.string.update_beneficiary)
            val beneficiaryDetails = intent.getParcelableExtra<Beneficiaries>("beneficiary_data")
            handleView(category)
            etMobileNo.isEnabled = true
            etMobileNo.setMaxLength(beneficiaryDetails.mobileNumber.length)
            etShortName.setText(beneficiaryDetails.shortName)
            etMobileNo.setText(beneficiaryDetails.mobileNumber)

            if (category == "salik") {
                beneficiaryDetails.optional1 ?: return
                pinView.setText(beneficiaryDetails.optional1)
            }

            btnSubmit.text = getString(R.string.update)
        }

        btnSubmit.setOnClickListener {

            if (action.equals("edit", true)){
                val beneficiaryData = intent.getParcelableExtra<Beneficiaries>("beneficiary_data")
                val accountNoLen = beneficiaryData.mobileNumber.length
                if (validateEdiText(accountNoLen, category)) {
                    viewModel.editBeneficiary(
                            user.token, user.sessionId, user.refreshToken, user.customerId.toLong(),
                            beneficiaryData.beneficiaryId.toLong(), etMobileNo.text.toString().trim(),etShortName.text.toString().trim(),
                            beneficiaryData.accessKey
                    )
                }
            } else {
                val prefixCode = tvPrefix.removeHypen(tvPrefix.text.toString())

                mobileNo = prefixCode.plus(etMobileNo.text.toString())

                if (validateEdiText(len, category)) {
                    var stdCode = intent.getStringExtra("std_code") ?: null
                    val bsnlAccount = intent.getStringExtra("bsnl_account") ?: null

                    if (category == "salik") {
                        stdCode = pinView.text.toString()
                    }

                    if (!isNetworkConnected()) {
                        onFailure(findViewById(R.id.root_view), getString(R.string.no_internet_connectivity))
                        return@setOnClickListener
                    }
                    viewModel.add2Beneficiary(user.token, user.sessionId, user.refreshToken,
                            user.customerId.toLong(), mobileNo, etShortName.text.toString().trim(), accessKey
                            ?: return@setOnClickListener, stdCode, bsnlAccount, type)
                }
            }
        }
        etShortName.filterEditText(20)
        tvPrefix.isEnabled = false
    }

    private fun handleView(category: String?) {
        when (category) {
            ServiceCategory.PREPAID, ServiceCategory.POSTPAID -> {
                tv_mobile_no.text = getString(R.string.enter_mobile_number_payments)
                etMobileNo.setMaxLength(10)
            }
            ServiceCategory.LANDLINE -> {
                tv_mobile_no.text = getString(R.string.enter_landline_number)
                etMobileNo.setMaxLength(7)
            }
            ServiceCategory.DTH -> {
                tv_mobile_no.text = getString(R.string.enter_customer_id)
                etMobileNo.setMaxLength(16)
            }
            ServiceCategory.GAS -> {
                tv_mobile_no.text = getString(R.string.enter_account_number)
                etMobileNo.setMaxLength(16)
            }
            ServiceCategory.ELECTRICITY -> {
                tv_mobile_no.text = getString(R.string.enter_consumer_number)
                etMobileNo.setMaxLength(16)
            }
            ServiceCategory.INSURANCE -> {
                tv_mobile_no.text = getString(R.string.enter_policy_number)
                etMobileNo.setMaxLength(16)
            }
            UtilityServiceType.FEWA -> {
                tv_mobile_no.text = getString(R.string.enter_account_number)
            }

            "salik" -> {
                tv_mobile_no.text = getString(R.string.enter_account_number)
                tvPinLabel.visibility = View.VISIBLE
                tvPinLabel.text = getString(R.string.enter_atm_pin_salik)
                pinView.visibility = View.VISIBLE
            }
            "pvt" -> {
                tv_mobile_no.text = getString(R.string.enter_pvt_number)
            }
            INTERNATIONAL_TOPUP -> {
                tv_mobile_no.text = getString(R.string.enter_mobile_number_payments)
            }
            ACCOUNTID -> {
                tv_mobile_no.text = getString(R.string.enter_account_number)
            }
            EMIRATESID -> {
                tv_mobile_no.text = getString(R.string.enter_emirates_id_payments)
                etMobileNo.setMaxLength(15)
            }
            MOBILENO -> {
                tv_mobile_no.text = getString(R.string.enter_mobile_number_payments)
                etMobileNo.setMaxLength(9)
            }
            PERSONID -> {
                tv_mobile_no.text = getString(R.string.enter_person_id)
            }
            MAWAQIF_TOPUP -> {
                tv_mobile_no.text = getString(R.string.enter_mobile_number_payments)
                etMobileNo.setMaxLength(9)
            }
        }
    }

    private fun addTextWatcher(editText: TextInputEditText, textInputLayout: TextInputLayout) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                tvError.removeErrorMessage()
                constraintLayout.clearErrorMsg()
                textInputLayout.clearErrorMessage()
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun addObserver() {
        viewModel.add2BeneficiaryState.observe(this, Observer {
            if (it == null) return@Observer

            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                showProgress(getString(R.string.processing))
                return@Observer
            }

            hideProgress()

            when (state) {
                is NetworkState2.Success -> {
                    val (message) = state
//                    showMessage(message.toString(),
//                            R.drawable.ic_success_checked_blue, R.color.blue,
//                            com.fh.payday.views2.shared.custom.AlertDialogFragment.OnConfirmListener {
////                                val returnIntent = Intent()
////                                returnIntent.apply { setResult(Activity.RESULT_OK, returnIntent) }
////                                finish()
//                            })

                    AlertDialogFragment.Builder()
                            .setMessage(message.toString())
                            .setIcon(R.drawable.ic_success_checked_blue)
                            .setTintColor(R.color.blue)
                            .setCancelable(false)
                            .setButtonVisibility(View.GONE)
                            .build()
                            .show(supportFragmentManager, "")

                    Handler().postDelayed({
                        val returnIntent = Intent()
                        returnIntent.apply { setResult(Activity.RESULT_OK, returnIntent) }
                        finish()
                    }, 2000)
                }
                is NetworkState2.Error -> {
                    hideProgress()
                    val (message) = state
                    if (state.isSessionExpired) {
                        return@Observer onSessionExpired(message)
                    }
                    handleErrorCode(state.errorCode.toInt(), state.message)
                }
                is NetworkState2.Failure -> {
                    hideProgress()
                    val (throwable) = state
                    onFailure(findViewById(R.id.root_view), throwable)
                }
                else -> {
                    hideProgress()
                    onFailure(findViewById(R.id.root_view), CONNECTION_ERROR)
                }
            }
        })
    }

    private fun validateEdiText(numberLen: Int, category: String): Boolean {
        val type = getTypeString(category)

        if (etMobileNo.text.isNullOrEmpty()) {
            tvError.addErrorMessage(String.format(getString(R.string.enter_amount_format), type))
            constraintLayout.onErrorMsg()
            return false
        } else if (tvPrefix.text.isNullOrEmpty() && etMobileNo.text.toString().length < numberLen) {
            tvError.addErrorMessage(String.format(getString(R.string.invalid_type_format), type))
            constraintLayout.onErrorMsg()
            return false
        } else if (tvPrefix.text.toString().length > 1 && etMobileNo.text.toString().length < numberLen) {
            tvError.addErrorMessage(String.format(getString(R.string.invalid_type_format), type))
            constraintLayout.onErrorMsg()
            return false
        } else if (etShortName.text.toString().trim().isEmpty()) {
            tilShortName.setErrorMessage(getString(R.string.empty_name))
            etShortName.requestFocus()
            return false
        } else if (etShortName.text.toString().trim().length < 3) {
            tilShortName.setErrorMessage(getString(R.string.name_should_not_less_than_3))
            etShortName.requestFocus()
            return false
        } else if (category == "salik" && (pinView.text.toString().isEmpty() || pinView.text.toString().length < 4 )) {
            tv_pin_error.visibility = View.VISIBLE
            tv_pin_error.requestFocus()
            return false
        } else {
            tilShortName.clearErrorMessage()
            tvError.removeErrorMessage()
            constraintLayout.clearErrorMsg()
            tv_pin_error.visibility = View.GONE
            return true
        }
    }

    private fun getTypeString(category: String): String {
        return when (category) {
            ServiceCategory.PREPAID, ServiceCategory.POSTPAID, MOBILENO -> {
                getString(R.string.mobile_number)
            }
            ServiceCategory.LANDLINE -> {
                getString(R.string.landline)
            }
            ServiceCategory.DTH -> {
                getString(R.string.customer_id)
            }
            ServiceCategory.GAS -> {
                getString(R.string.account_no_label)
            }
            ServiceCategory.ELECTRICITY -> {
                getString(R.string.consumer_number)
            }
            ServiceCategory.INSURANCE -> {
                getString(R.string.policy_number)
            }
            UtilityServiceType.FEWA, "salik", INTERNATIONAL_TOPUP, ACCOUNTID -> {
                getString(R.string.account_no_label)
            }
            "pvt" -> {
                getString(R.string.pvt_number)
            }
            EMIRATESID -> {
                getString(R.string.emirates_id)
            }
            PERSONID -> {
                getString(R.string.person_id)
            }
            else -> {
                getString(R.string.account_no_label)
            }
        }
    }
}

