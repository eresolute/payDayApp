package com.fh.payday.views2.moneytransfer.beneificaries.creditCard

import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.design.button.MaterialButton
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.datasource.models.moneytransfer.P2CBeneficiary
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.*
import com.fh.payday.views2.moneytransfer.beneificaries.shared.SelectBankFragment

class CreditCardDetailFragment : Fragment() {

    private lateinit var activity: CreditCardBeneficiaryActivity
    private lateinit var tvBankName: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var etName: TextInputEditText
    private lateinit var etCard: TextInputEditText
    private lateinit var tilCard: TextInputLayout
    private lateinit var tilName: TextInputLayout
    private lateinit var map: LinkedHashMap<String, String>

    var listener = SelectBankFragment.OnBankSelectedListener {
        activity.getViewModel().selectedBank = it
        tvBankName.text = it
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context as CreditCardBeneficiaryActivity
    }

    override fun onResume() {
        super.onResume()
        clearError()
        activity.hideKeyboard()
        if (isEmptyList(activity.getViewModel().bankList))
            fetchBanks()
    }

    private fun fetchBanks() {
        if (!activity.isNetworkConnected()) {
            return activity.showNoInternetView { fetchBanks() }
        }

        activity.hideNoInternetView()

        val user = UserPreferences.instance.getUser(activity)
        if (user != null && isEmptyList(activity.getViewModel().bankList))
            activity.getViewModel().getBankList(user.token, user.sessionId,
                user.refreshToken, user.customerId.toLong())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_credit_card_detail, container, false)
        init(view)
        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        if (activity.getViewModel().selectedBeneficiary != null) {
            val beneficiary = activity.getViewModel().selectedBeneficiary
            setDetails(beneficiary!!)
        }
        addObserver()
        return view
    }

    fun init(view: View) {
        etName = view.findViewById(R.id.et_name)
        etCard = view.findViewById(R.id.et_card_number)
        tilCard = view.findViewById(R.id.textInputLayoutCard)
        tilName = view.findViewById(R.id.textInputLayoutName)
        tvBankName = view.findViewById(R.id.tvBankName)
        progressBar = view.findViewById(R.id.progressBar)
        map = activity.getViewModel().ccBeneficiaryMap

        tvBankName.text = activity.getViewModel().selectedBank ?: getString(R.string.select_bank)

        view.findViewById<MaterialButton>(R.id.btn_submit).setOnClickListener {
            if(validateInput(it)) {
                setDetailMap()
                activity.navigateUp()
            }
        }

        tvBankName.setOnClickListener {
            val bankList = activity.getViewModel().bankList
            if (isEmptyList(bankList)) return@setOnClickListener
            showBanks(activity, bankList, listener)
        }

        etName.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tilName.clearErrorMessage()
            }
        })

        etCard.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tilCard.clearErrorMessage()
            }
        })
    }

    private fun setDetails(beneficiary: P2CBeneficiary) {
        tvBankName.text = beneficiary.bankName
        etName.setText(beneficiary.shortName)
        etCard.setText(beneficiary.creditCardNo)
    }

    private fun setDetailMap() {
        activity.getViewModel().name = etName.text.toString().trimEnd().trimStart()
        activity.getViewModel().cardNumber = etCard.text.toString().trimEnd().trimStart()
        activity.getViewModel().selectedBank = tvBankName.text.toString().trimEnd().trimStart()
        map.clear()
        map[getString(R.string.bank)] = tvBankName.text.toString().trimEnd().trimStart()
        map[getString(R.string.beneficiary_name)] = etName.text.toString().trimEnd().trimStart()
        map[getString(R.string.card_number)] = etCard.text.toString().trimEnd().trimStart()
    }

    private fun validateInput(view: View): Boolean {
        when {
            (tvBankName.text.toString() == getString(R.string.select_bank)) -> {
                activity.onError(getString(R.string.please_select_bank))
                return false
            }

            TextUtils.isEmpty(etName.text.toString()) -> {
                tilName.setErrorMessage(getString(R.string.invalid_name))
                return false
            }
            TextUtils.isEmpty(etCard.text.toString()) -> {
                tilCard.setErrorMessage(getString(R.string.invalid_card_no))
                return false
            }
            etCard.text.toString().length != 16 -> {
                tilCard.setErrorMessage(getString(R.string.invalid_account_length, "16"))
                return false
            }
            else -> {
                tilName.clearErrorMessage()
                tilCard.clearErrorMessage()
            }
        }
        return true
    }

    private fun clearError() {
        if (tilName == null || tilCard == null) return
        tilCard.clearErrorMessage()
        tilName.clearErrorMessage()
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

                    activity.handleErrorCode(Integer.parseInt(response.errorCode), message)

//                    activity.onError(message)
                }

                is NetworkState2.Failure -> {
                    activity.onFailure(activity.findViewById(R.id.root_view), response.throwable)
                }

                else -> {
                    activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
                }
            }
        })
    }
}