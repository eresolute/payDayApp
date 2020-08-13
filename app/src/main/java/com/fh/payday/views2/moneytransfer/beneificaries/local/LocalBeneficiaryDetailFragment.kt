package com.fh.payday.views2.moneytransfer.beneificaries.local

import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.button.MaterialButton
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextUtils.isEmpty
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.*
import com.fh.payday.views2.moneytransfer.beneificaries.shared.SelectBankFragment
import kotlinx.android.synthetic.main.fragment_local_beneficiary_detail.*

class LocalBeneficiaryDetailFragment : Fragment(), TextWatcher {

    lateinit var activity: LocalBeneficiaryActivity
    lateinit var tvBankName: TextView
    lateinit var etName: TextInputEditText
    lateinit var etAccount: TextInputEditText
    lateinit var etMobile: TextInputEditText
    lateinit var tilName: TextInputLayout
    lateinit var tilAccount: TextInputLayout

    private lateinit var tvPrefix: TextView
    private lateinit var tvError: TextView
    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var btnSubmit: MaterialButton
    lateinit var map: LinkedHashMap<String, String>
    lateinit var mobileNumber: String

    var listener = SelectBankFragment.OnBankSelectedListener {
        activity.getViewModel().selectedBank = it
        tvBankName.text = it
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context as LocalBeneficiaryActivity
    }

    override fun onResume() {
        super.onResume()
        clearError()
        activity.hideKeyboard()
        fetchBanks()
    }

    private fun fetchBanks() {
        if (!activity.isNetworkConnected()) {
            return activity.showNoInternetView { fetchBanks() }
        }

        activity.hideNoInternetView()

        val user = UserPreferences.instance.getUser(activity)
        if (user != null && isEmptyList(activity.getViewModel().bankList))
            activity.getViewModel().getBankList(user.token, user.sessionId, user.refreshToken, user.customerId.toLong())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_local_beneficiary_detail, container, false)
        init(view)
        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        addObserver()
        return view
    }

    private fun addObserver() {
        activity.getViewModel().bankListState.observe(this, Observer {
            it ?: return@Observer

            val response = it.getContentIfNotHandled() ?: return@Observer

            if (response is NetworkState2.Loading) {
                progressBar.visibility = View.VISIBLE
                return@Observer
            }

            progressBar.visibility = View.GONE

            when (response) {
                is NetworkState2.Success -> {

                }

                is NetworkState2.Error -> {
                    if(response.isSessionExpired)
                        return@Observer activity.onSessionExpired(response.message)

                    val (message) = response
                    activity.onError(message)
                }

                is NetworkState2.Failure -> {
                    activity.onFailure(activity.findViewById<View>(R.id.root_view), getString(R.string.request_error))
                }

                else -> {
                    activity.onFailure(activity.findViewById<View>(R.id.root_view), CONNECTION_ERROR)
                }
            }

        })

        activity.getViewModel().ibanState.observe(this, Observer {
            it ?: return@Observer

            val response = it.getContentIfNotHandled() ?: return@Observer

            if (response is NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing))
                return@Observer
            }

            activity.hideProgress()
            progressBar.visibility = View.GONE

            when (response) {
                is NetworkState2.Success -> {
                    map[getString(R.string.iban_number)] = response.data as String
                    map[getString(R.string.mobile_number)] = mobileNumber.trimEnd().trimStart()
                    activity.navigateUp()
                }

                is NetworkState2.Error -> {
                    if(response.isSessionExpired)
                        return@Observer activity.onSessionExpired(response.message)

                    val (message) = response

                    activity.handleErrorCode(Integer.parseInt(response.errorCode), message)

//                    activity.onError(message)
                }

                is NetworkState2.Failure -> {
                    activity.onFailure(activity.findViewById<View>(R.id.root_view), getString(R.string.request_error))
                }
                else -> {
                    activity.onFailure(activity.findViewById<View>(R.id.root_view), CONNECTION_ERROR)

                }
            }
        })
    }

    fun init(view: View) {
        btnSubmit = view.findViewById(R.id.btn_submit)
        tvBankName = view.findViewById(R.id.tvBankName)
        etName = view.findViewById(R.id.et_name)
        etAccount = view.findViewById(R.id.et_account)
        etMobile = view.findViewById(R.id.et_mobile_number)
        tilName = view.findViewById(R.id.textInputLayoutName)
        tilAccount = view.findViewById(R.id.textInputLayoutAccount)
        constraintLayout = view.findViewById(R.id.cl_custom_layout)
        tvError = view.findViewById(R.id.tv_error_message)
        tvPrefix = view.findViewById(R.id.tv_prefix)


        tvBankName.text = activity.getViewModel().selectedBank ?: getString(R.string.select_bank)

        etMobile.setMaxLength(9)
        tvPrefix.text = getString(R.string.du_prefix_no)
        activity.setOnFocusListener(etMobile, constraintLayout)

        if (activity.getViewModel().selectedBeneficiary != null && activity.getViewModel().localBeneficiaryMap != null) {
            val beneficiary = activity.getViewModel().selectedBeneficiary!!
            tvBankName.text = beneficiary.bank
            etName.setText(beneficiary.beneficiaryName)
            etMobile.setText(PhoneUtils.extractMobileNo(
                    beneficiary.mobileNumber, "0"))
            etAccount.setText(beneficiary.accountNo)
        }

        etMobile.addTextChangedListener(this)
        etAccount.addTextChangedListener(this)
        etName.addTextChangedListener(this)

        map = activity.getViewModel().localBeneficiaryMap
        btnSubmit.setOnClickListener {
            val prefix = tvPrefix.text.trim().toString().replace("+971-","0")
            mobileNumber = prefix.plus(etMobile.text.toString().trim())
            if (validateInput(view)) {
                setDetailMap()
                val user = UserPreferences.instance.getUser(activity) ?: return@setOnClickListener
                val id = user.customerId
                activity.getViewModel().getIban(user.token, user.sessionId, user.refreshToken, id.toLong(), tvBankName.text.toString(), etAccount.text.toString())
            }
        }

        tvBankName.setOnClickListener {
            val bankList = activity.getViewModel().bankList
            if (isEmptyList(bankList)) return@setOnClickListener
            showBanks(activity, bankList, listener)
        }
    }
    private fun setDetailMap() {
        map.clear()
        map[getString(R.string.bank)] = tvBankName.text.toString().trimEnd().trimStart()
        map[getString(R.string.beneficiary_name)] = etName.text.toString().trimEnd().trimStart()
        map[getString(R.string.account_number)] = etAccount.text.toString().trimEnd().trimStart()
    }

    private fun validateInput(view: View): Boolean {
        if (tvBankName.text.toString() == getString(R.string.select_bank)) {
            activity.onError(getString(R.string.please_select_bank))
            return false
        } else if (isEmpty(etName.text.toString())) {
            tilName.setErrorMessage(getString(R.string.invalid_name))
            return false
        } else if (isEmpty(etAccount.text.toString())) {
            tilAccount.setErrorMessage(getString(R.string.invalid_account_no))
            return false
        } else if (etAccount.length() > 16) {
            tilAccount.setErrorMessage(getString(R.string.invalid_account_no))
            return false
        } else if (isEmpty(mobileNumber)) {
            tvError.addErrorMessage(getString(R.string.empty_mobile_no_payments))
            constraintLayout.onErrorMsg()
            return false
        } else if (mobileNumber.length != 10) {
            tvError.addErrorMessage(getString(R.string.uae_mobile_length))
            constraintLayout.onErrorMsg()
            return false
        } else if (!MobileNoValidator.validate(mobileNumber)) {
            tvError.addErrorMessage(getString(R.string.uae_mobile_format))
            constraintLayout.onErrorMsg()
            return false
        } else {
            tvError.removeErrorMessage()
            constraintLayout.clearErrorMsg()
            tilAccount.clearErrorMessage()
            tilName.clearErrorMessage()
        }
        return true
    }

    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        tvError.removeErrorMessage()
        constraintLayout.clearErrorMsg()
        tilAccount.clearErrorMessage()
        tilName.clearErrorMessage()
    }

    private fun clearError() {
        if (tilName == null || tilAccount == null || constraintLayout == null) return
        tvError.removeErrorMessage()
        constraintLayout.clearErrorMsg()
        tilAccount.clearErrorMessage()
        tilName.clearErrorMessage()
    }
}