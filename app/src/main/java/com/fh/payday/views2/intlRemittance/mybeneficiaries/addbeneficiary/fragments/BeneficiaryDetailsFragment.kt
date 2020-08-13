package com.fh.payday.views2.intlRemittance.mybeneficiaries.addbeneficiary.fragments

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v7.widget.ListPopupWindow
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.TextView
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.fh.payday.BaseActivity
import com.fh.payday.BaseFragment
import com.fh.payday.R
import com.fh.payday.datasource.models.IsoAlpha3
import com.fh.payday.datasource.models.intlRemittance.PayOutCurrencies
import com.fh.payday.datasource.models.intlRemittance.PayoutCountries
import com.fh.payday.datasource.models.intlRemittance.RelationsItem
import com.fh.payday.datasource.models.login.User
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.*
import com.fh.payday.viewmodels.intlRemittance.AddBeneficiaryViewModel
import com.fh.payday.views2.intlRemittance.DeliveryModes
import com.fh.payday.views2.intlRemittance.ExchangeContainer
import com.fh.payday.views2.intlRemittance.cashpayout.CashPayoutActivity
import com.fh.payday.views2.intlRemittance.mybeneficiaries.addbeneficiary.AddBeneficiaryActivity
import com.fh.payday.views2.shared.custom.showCountries
import kotlinx.android.synthetic.main.fragment_beneficiary_details.*
import kotlinx.android.synthetic.main.layout_payment_option.*

class BeneficiaryDetailsFragment : BaseFragment() {
    private var beneficiaryActivity: AddBeneficiaryActivity? = null
    private var selectedTv: String? = null
    private var mobileCodeFlag = false

    private val viewModel by lazy {
        activity?.let { ViewModelProviders.of(it).get(AddBeneficiaryViewModel::class.java) }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        beneficiaryActivity = context as AddBeneficiaryActivity
    }

    override fun onDetach() {
        super.onDetach()
        activity = null
        beneficiaryActivity = null
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_beneficiary_details, container, false)
    }

    var user: User? = null
    var countries: List<PayoutCountries>? = null
    var nationalities = ArrayList<IsoAlpha3>()
    var relations: List<RelationsItem>? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addTextWatcherListener(et_firstname, textInputLayout_firstname)
        addTextWatcherListener(editText_egyptian_id, textInputLayout_egyptian_id)
        addTextWatcherListener(et_lastname, textInputLayout_lastname)
//        addTextWatcherListener(et_relationship, textInputLayout_relationship)
        // addTextWatcherListener(et_mobile_no_code, til_mobile_no_code)
        addTextWatcherListener(et_mobile_no, textInputLayout_mobile_no)
        addTextWatcherListener(editText_addressLine1, textInputLayout_addressLine1)
        addTextWatcherListener(editText_addressLine2, textInputLayout_addressLine2)
        handleHint()
        /*  et_mobile_no.setOnEditorActionListener(object : TextView.OnEditorActionListener {
              override fun onEditorAction(p0: TextView?, actionId: Int, p2: KeyEvent?): Boolean {
                  if (actionId == EditorInfo.IME_ACTION_NEXT) {
                      tv_nationality.performClick()
                      return true
                  }
                  return false
              }

          })*/
        val activity = getActivity() as BaseActivity
        user = UserPreferences.instance.getUser(activity) ?: return
        payoutCountriesObserver()
        attachObserver()
        tv_select_beneficiary.visibility = View.GONE
        btn_next.setOnClickListener {
            if (isValid()) {
                saveDetails()
                onSuccess(activity)
            }
        }

        radioButton.setOnCheckedChangeListener { _, _ ->
            rb_mr.isChecked
            tv_error_rg.visibility = View.GONE
        }

        tv_cash_payout.setOnClickListener {
            val intent = Intent(activity, CashPayoutActivity::class.java)
            startActivityForResult(intent, CASH_PAYOUT)
        }


        viewModel?.getPayoutCountries(
                user!!.token,
                user!!.sessionId,
                user!!.refreshToken,
                user!!.customerId.toLong(),
                ExchangeContainer.accessKey,
                viewModel?.selectedDeliveryMode ?: return
        )

        tv_country.setOnItemClickListener { parent, _, position, _ ->
            val payoutCountries = parent.adapter.getItem(position)
                    as? PayoutCountries? ?: return@setOnItemClickListener
            setCountry(payoutCountries)
        }
        tv_country.setOnClickListener {
            tv_country.showDropDown()
        }

        tv_country.setOnFocusChangeListener { view, b ->
            if (!b) {
                if (countries!!.isNotEmpty()) {
                    var country = countries!!.filter { country -> country.countryName.equals(tv_country.text.toString(), true) }.distinct()
                    if (country.isNotEmpty())
                        setCountry(country[0])
                    else {
                        if (beneficiaryActivity == null) return@setOnFocusChangeListener
                        if (!beneficiaryActivity!!.firstPage)
                            if (tv_country.text.isNotEmpty())
                                beneficiaryActivity?.onError(getString(R.string.choose_country_error))
                        //setTextViewError(getString(R.string.choose_country_error), tv_country)
                        tv_nationality.setText("")
                        tv_nationality.background = ContextCompat.getDrawable(
                                context ?: return@setOnFocusChangeListener,
                                R.drawable.bg_grey_dark_grey_border
                        )
                        et_mobile_no_code.setText(resources.getString(R.string._91))
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            et_mobile_no_code.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                    R.drawable.ic_flag_placeholder,
                                    0,
                                    0,
                                    0
                            )
                        } else
                            et_mobile_no_code.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_flag_placeholder,
                                    0, 0, 0
                            )
                        et_mobile_no_code.setTextColor(
                                ContextCompat.getColor(
                                        context
                                                ?: return@setOnFocusChangeListener, R.color.grey_500
                                )
                        )
                        tv_currency.setText(resources.getString(R.string.cur))
                        tv_currency.background = ContextCompat.getDrawable(
                                context ?: return@setOnFocusChangeListener,
                                R.drawable.bg_grey_dark_grey_border
                        )
                    }
                }
            } else
                tv_country.showDropDown()
        }

        tv_country.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(p0: TextView?, actionId: Int, p2: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    var country = countries!!.filter { country -> country.countryName.equals(tv_country.text.toString(), true) }.distinct()
                    if (country.isNotEmpty())
                        setCountry(country[0])
                    else {
                        activity.onError(getString(R.string.choose_country_error))
                        setTextViewError(getString(R.string.choose_country_error), tv_country)
                    }
                    return false
                }
                return false
            }
        })
        tv_currency.setOnItemClickListener { parent, _, position, _ ->
            val payoutCurrency = parent.adapter.getItem(position)
                    as? PayOutCurrencies? ?: return@setOnItemClickListener
            viewModel?.inputWrapper?.currency = payoutCurrency.code
            tv_currency.setText(payoutCurrency.code)
            tv_currency.background = ContextCompat.getDrawable(
                    activity,
                    R.drawable.bg_grey_blue_border
            )
        }
        tv_currency.setOnClickListener {
            if (viewModel?.payOutCurrencies.isNullOrEmpty()) return@setOnClickListener
            if (viewModel?.payOutCurrencies?.size == 1) {
                tv_currency.setText(viewModel?.payOutCurrencies?.get(0)?.code)
                viewModel?.inputWrapper?.currency = viewModel?.payOutCurrencies?.get(0)?.code
                tv_currency.background = ContextCompat.getDrawable(
                        activity,
                        R.drawable.bg_grey_blue_border
                )
                return@setOnClickListener
            }
            tv_currency.isFocusable = true
            if (viewModel?.payOutCurrencies.isNullOrEmpty()) return@setOnClickListener
            val payOutCurrencies = viewModel?.payOutCurrencies ?: return@setOnClickListener
            var currencyAdapter =
                    ArrayAdapter(activity, R.layout.layout_autcompletetext, payOutCurrencies)
            tv_currency.threshold = 1
            tv_currency.setAdapter(currencyAdapter)
            tv_currency.showDropDown()
            /*   showPayoutCurrency(activity, payOutCurrencies, object : OnPayoutCurrencySelectListener {
                   override fun onPayoutCurrencySelect(payoutCurrency: PayOutCurrencies) {
                       viewModel?.inputWrapper?.currency = payoutCurrency.code
                       tv_currency.setText(payoutCurrency.code)
                       tv_currency.background = ContextCompat.getDrawable(
                               activity,
                               R.drawable.bg_grey_blue_border
                       )
                   }
               })*/
        }

        et_mobile_no_code.setOnClickListener {
            selectedTv = "mobileCode"
            viewModel?.getCountries(
                    user!!.token,
                    user!!.sessionId,
                    user!!.refreshToken,
                    user!!.customerId.toString()
            )
        }


        // get nationality from other API
        tv_nationality.setOnItemClickListener { parent, _, position, _ ->
            val isoAlpha3 = parent.adapter.getItem(position)
                    as? IsoAlpha3? ?: return@setOnItemClickListener
            setNationality(isoAlpha3)
        }
        tv_nationality.setOnFocusChangeListener { view, b ->
            if (b)
                if (tv_nationality.text.toString().isNullOrEmpty())
                    showNationalityDropdown()
        }

        tv_nationality.setOnClickListener { showNationalityDropdown() }

        /*et_relationship.setOnItemClickListener { parent, _, position, _ ->
            val relations = parent.adapter.getItem(position)
                    as? RelationsItem? ?: return@setOnItemClickListener
            et_relationship.setText(relations.name)
        }*/
        /*et_relationship.setOnFocusChangeListener { view, b ->
            if (b)
                if (et_relationship.text.toString().isNullOrEmpty())
                    et_relationship.showDropDown()
        }*/

        et_relationship.setOnClickListener {
            val listPopupWindow = ListPopupWindow(activity)
            listPopupWindow.width = 700
            listPopupWindow.anchorView = et_relationship
            val layoutInflater = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            if (!relations.isNullOrEmpty()) {
                var adapter = ListPopupAdapter(relations!!, layoutInflater) {
                    et_relationship.setText(it)
                    listPopupWindow.dismiss()
                }
                listPopupWindow.setAdapter(adapter)
                listPopupWindow.show()
            }           /*   if (!TextUtils.isEmpty(et_relationship.text.toString()))
                et_relationship.setText("")
            et_relationship.showDropDown()*/
        }

        /*  et_relationship.setOnEditorActionListener { _, _, _ ->
              activity.hideKeyboard()
              true
          }*/
    }

    private fun showNationalityDropdown() {
        tv_nationality.showDropDown()
        selectedTv = "nationality"
        if (viewModel?.countries.isNullOrEmpty()) {
            viewModel?.getCountries(
                    user!!.token,
                    user!!.sessionId,
                    user!!.refreshToken,
                    user!!.customerId.toString()
            )
            return
        }
        val countries = viewModel?.countries
        val isoAlpha = ArrayList<IsoAlpha3>()
        countries?.filter { country ->
            isoAlpha.add(
                    IsoAlpha3(
                            country.country, country.countryCode, country.countryCode,
                            country.currency, country.dialCode, country.imagePath
                    )
            )
        }
        loadNationalityFlag(isoAlpha)
    }

    private fun setNationality(countryName: IsoAlpha3) {
//                    et_mobile_no_code.setText(countryName.dialCode)
        // editText_addressLine1.requestFocus()
        val inputeManager =
                activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputeManager.showSoftInput(editText_addressLine1, InputMethodManager.SHOW_IMPLICIT)
        tv_nationality.setText(countryName.country)
        //                   loadImage(countryName.imagePath)
        tv_nationality.background = ContextCompat.getDrawable(
                context ?: return,
                R.drawable.bg_grey_blue_border
        )
        et_mobile_no_code.background = resources.getDrawable(R.drawable.bg_grey_blue_border)
        viewModel?.inputWrapper?.nationality = countryName.countryCode

    }

    private fun handleHint() {
        textInputLayout_addressLine1.hint = null
        editText_addressLine1.hint = resources.getString(R.string.address_line1)
        textInputLayout_addressLine2.hint = null
        editText_addressLine2.hint = resources.getString(R.string.address_line2)
        textInputLayout_firstname.hint = null
        et_firstname.hint = resources.getString(R.string.first_name_as_per_bank)
        textInputLayout_lastname.hint = null
        et_lastname.hint = resources.getString(R.string.last_name_as_per_bank)
        textInputLayout_mobile_no.hint = null
        et_mobile_no.hint = resources.getString(R.string.mobile_num)
        textInputLayout_relationship.hint = null
        et_relationship.hint = resources.getString(R.string.relationship)
        textInputLayout_egyptian_id.hint = null
        editText_egyptian_id.hint = resources.getString(R.string.egyptian_id)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        if (requestCode == CASH_PAYOUT) {
            beneficiaryActivity?.finish()
        }
    }

    private fun loadNationalityFlag(isoAlpha: ArrayList<IsoAlpha3>) {
        //tv_nationality.showDropDown()
        val activity = getActivity() as BaseActivity
        nationalities = isoAlpha

        var nationalityAdapter =
                ArrayAdapter(activity, R.layout.layout_autcompletetext, nationalities!!)
        tv_nationality.threshold = 1
        tv_nationality.setAdapter(nationalityAdapter)
        /*  val adapter = tv_nationality.adapter as ArrayAdapter<IsoAlpha3>
        adapter.apply {
            clear()
            addAll(isoAlpha)
            notifyDataSetChanged()
        }*/
        /*    showCountries(activity, isoAlpha, getString(R.string.select_country), object : OnCountrySelectListener {
                override fun onCountrySelect(countryName: IsoAlpha3) {
                    if (!isoAlpha.isNullOrEmpty()) {
    //                    et_mobile_no_code.setText(countryName.dialCode)
                        editText_addressLine1.requestFocus()
                        val inputeManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        inputeManager.showSoftInput(editText_addressLine1, InputMethodManager.SHOW_IMPLICIT)
                        tv_nationality.setText(countryName.country)
                        //                   loadImage(countryName.imagePath)
                        tv_nationality.background = ContextCompat.getDrawable(
                                context ?: return,
                                R.drawable.bg_grey_blue_border
                        )
                        et_mobile_no_code.background = resources.getDrawable(R.drawable.bg_grey_blue_border)
                        viewModel?.inputWrapper?.nationality = countryName.countryCode

                    }
                }
            })*/
    }

    private fun loadMobileCode(isoAlpha: ArrayList<IsoAlpha3>) {
        val activity = getActivity() as BaseActivity
        showCountries(
                activity,
                isoAlpha,
                getString(R.string.select_country),
                object : OnCountrySelectListener {
                    override fun onCountrySelect(countryName: IsoAlpha3) {
                        if (!isoAlpha.isNullOrEmpty()) {
                            et_mobile_no_code.setText(countryName.dialCode)
                            et_mobile_no_code.background =
                                    resources.getDrawable(R.drawable.bg_grey_blue_border)
                            mobileCodeFlag = true
//                    tv_nationality.text = countryName.country
                            loadImage(countryName.imagePath)
                            viewModel?.inputWrapper?.flagPath = countryName.imagePath
                            et_mobile_no_code.setTextColor(
                                    ContextCompat.getColor(
                                            context
                                                    ?: return, R.color.textColor
                                    )
                            )
                            //viewModel?.inputWrapper?.nationality = countryName.countryCode
                            et_mobile_no_code.setTextColor(
                                    ContextCompat.getColor(
                                            context
                                                    ?: return, R.color.textColor
                                    )
                            )

                            viewModel?.inputWrapper?.nationality = countryName.countryCode
                        }
                    }
                })
    }

    private fun onSuccess(activity: FragmentActivity) {
        when (activity) {
            is AddBeneficiaryActivity -> activity.navigateUp()
                    .also { viewModel?.submitBeneficiary() }
        }
    }

    private fun saveDetails() {
        viewModel?.inputWrapper?.firstName = et_firstname.text.toString()
        viewModel?.inputWrapper?.lastName = et_lastname.text.toString()
        viewModel?.inputWrapper?.contactNo = et_mobile_no.text.toString()
        viewModel?.inputWrapper?.addressLine =
                editText_addressLine1.text.toString() + ", " + editText_addressLine2.text.toString()
        viewModel?.inputWrapper?.relationShip = et_relationship.text.toString()

    }

    private fun addTextWatcherListener(
            editText: TextInputEditText,
            textInputLayout: TextInputLayout
    ) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                textInputLayout.clearErrorMessage()
                if (editText == et_mobile_no) {
                    //til_mobile_no_code.clearErrorMessage()
                }
            }

        })
    }

    private fun payoutCountriesObserver() {

        viewModel?.payoutCountriesState?.observe(this, Observer {
            it ?: return@Observer
            val payoutCountriesState = it.getContentIfNotHandled() ?: return@Observer

            val activity = getActivity() as BaseActivity
            when (payoutCountriesState) {
                is NetworkState2.Success -> {
                    val data = payoutCountriesState.data ?: return@Observer
                    countries = if (!data.isNullOrEmpty()) data else return@Observer
                    var countryAdapter =
                            ArrayAdapter(activity, R.layout.layout_autcompletetext, countries!!)
                    tv_country.setAdapter(countryAdapter)
                    viewModel?.getCountries(
                            user!!.token,
                            user!!.sessionId,
                            user!!.refreshToken,
                            user!!.customerId.toString()
                    )
                    /* showPayoutCountries(
                            activity,
                            payoutCountries,
                            object : OnPayoutCountrySelectListener {
                                override fun onPayoutCountrySelect(payoutCountry: PayoutCountries) {
                                    if (!payoutCountry.deliveryModes.isNullOrEmpty()) {
                                        viewModel?.inputWrapper?.countryName = payoutCountry.countryName
                                        viewModel?.inputWrapper?.countryCode = payoutCountry.countryCode
                                        tv_nationality.text = payoutCountry.nationality
                                        tv_nationality.background = ContextCompat.getDrawable(
                                            context ?: return,
                                            R.drawable.bg_grey_blue_border
                                        )
                                        et_mobile_no_code.setText(payoutCountry.dialCode)
                                        loadImage(payoutCountry.flag)
                                        mobileCodeFlag = true
                                        viewModel?.inputWrapper?.flagPath = payoutCountry.flag
                                        et_mobile_no_code.setTextColor(ContextCompat.getColor(context
                                                ?: return, R.color.textColor))
                                        et_mobile_no_code.background = ContextCompat.getDrawable(
                                                context ?: return,
                                                R.drawable.bg_grey_blue_border
                                        )
                                        viewModel?.inputWrapper?.nationality = payoutCountry.countryCode
                                        viewModel?.payOutCurrencies = payoutCountry.payOutCurrencies
                                        val deliveryModes =
                                                payoutCountry.deliveryModes.filter { deliveryMode ->
                                                    deliveryMode.code == DeliveryModes.BT
                                                }
                                        viewModel?.inputWrapper?.deliveryMode = deliveryModes[0].code
                                        setCountryView(payoutCountry.countryName)
                                        if (payoutCountry.payOutCurrencies.size == 1) {
                                            viewModel?.inputWrapper?.currency = payoutCountry.payOutCurrencies[0].code
                                            tv_currency.text = payoutCountry.payOutCurrencies[0].code
                                            tv_currency.background = ContextCompat.getDrawable(
                                                context ?: return,
                                                R.drawable.bg_grey_blue_border
                                            )
                                        }

                                        viewModel?.clearBankDetails = true
                                    }
                                }
                            })*/
                }

                is NetworkState2.Error -> {
                    if (payoutCountriesState.isSessionExpired) {
                        activity.onSessionExpired(payoutCountriesState.message)
                        return@Observer
                    }
                    val (message) = payoutCountriesState
                    activity.handleErrorCode(payoutCountriesState.errorCode.toInt(), message)
                }

                is NetworkState2.Failure -> {
                    activity.onFailure(
                            activity.findViewById(R.id.root_view),
                            payoutCountriesState.throwable
                    )
                }
            }

        })
    }

    private fun setCountry(payoutCountry: PayoutCountries) {
        if (!payoutCountry.deliveryModes.isNullOrEmpty()) {
            viewModel?.inputWrapper?.countryName = payoutCountry.countryName
            viewModel?.inputWrapper?.countryCode = payoutCountry.countryCode
            tv_nationality.setText(payoutCountry.nationality)
            tv_nationality.background = ContextCompat.getDrawable(
                    context ?: return,
                    R.drawable.bg_grey_blue_border
            )

            nationalities!!.add(IsoAlpha3(payoutCountry.nationality, ""))
            et_mobile_no_code.setText(payoutCountry.dialCode)
            loadImage(payoutCountry.flag)
            mobileCodeFlag = true
            viewModel?.inputWrapper?.flagPath = payoutCountry.flag
            et_mobile_no_code.setTextColor(
                    ContextCompat.getColor(
                            context
                                    ?: return, R.color.textColor
                    )
            )
            et_mobile_no_code.background = ContextCompat.getDrawable(
                    context ?: return,
                    R.drawable.bg_grey_blue_border
            )
            viewModel?.inputWrapper?.nationality = payoutCountry.countryCode
            viewModel?.payOutCurrencies = payoutCountry.payOutCurrencies
            val deliveryModes =
                    payoutCountry.deliveryModes.filter { deliveryMode ->
                        deliveryMode.code == DeliveryModes.BT
                    }
            viewModel?.inputWrapper?.deliveryMode = deliveryModes[0].code
            setCountryView(payoutCountry.countryName)
            if (payoutCountry.payOutCurrencies.size == 1) {
                viewModel?.inputWrapper?.currency = payoutCountry.payOutCurrencies[0].code
                tv_currency.setText(payoutCountry.payOutCurrencies[0].code)
                tv_currency.background = ContextCompat.getDrawable(
                        context ?: return,
                        R.drawable.bg_grey_blue_border
                )
            }

            viewModel?.clearBankDetails = true
        }
    }

    private fun attachObserver() {

        viewModel?.countryResponse?.observe(this, Observer {
            it ?: return@Observer

            val countryState = it.getContentIfNotHandled() ?: return@Observer

            when (countryState) {
                is NetworkState2.Success -> {
                    val countries = countryState.data
                    val isoAlpha = ArrayList<IsoAlpha3>()
                    countries?.filter { country ->
                        isoAlpha.add(
                                IsoAlpha3(
                                        country.country, country.dialCode, country.countryCode,
                                        country.currency, country.dialCode, country.imagePath
                                )
                        )
                    }
                    viewModel?.getRelationShips(
                            user!!.token,
                            user!!.sessionId,
                            user!!.refreshToken,
                            user!!.customerId.toLong(),
                            "6",
                        DeliveryModes.BT,
                            "Relation"
                    )
                    if (selectedTv == "mobileCode")
                        loadMobileCode(isoAlpha)
                    else {
                        loadNationalityFlag(isoAlpha)
                        /*val adapter = tv_nationality.adapter as ArrayAdapter<IsoAlpha3>
                        adapter.apply {
                            clear()
                            addAll(isoAlpha)
                            notifyDataSetChanged()
                        }*/
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

        viewModel?.payoutRelationsState?.observe(this, Observer {
            it ?: return@Observer

            val countryState = it.getContentIfNotHandled() ?: return@Observer

            when (countryState) {
                is NetworkState2.Success -> {
                    relations = countryState.data!!
                    var relationAdapter =
                            ArrayAdapter(activity, R.layout.layout_autcompletetext, relations)
                    et_relationship.threshold = 1
                    et_relationship.setAdapter(relationAdapter)
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

    private fun setCountryView(countryName: String) {
        tv_country.setText(countryName)
        tv_country.background = ContextCompat.getDrawable(
                context ?: return,
                R.drawable.bg_grey_blue_border
        )
    }

    private fun loadImage(imagePath: String?) {
        GlideApp.with(et_mobile_no_code)
                .load(Uri.parse("$BASE_URL/$imagePath"))
                .placeholder(R.drawable.ic_flag_placeholder)
                .error(R.drawable.ic_flag_placeholder)
                .into(object : CustomTarget<Drawable>(50, 50) {
                    override fun onLoadCleared(placeholder: Drawable?) {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                        et_mobile_no_code.setCompoundDrawablesRelativeWithIntrinsicBounds(placeholder,
//                            null, null, null)
//                    }else
//                        et_mobile_no_code.setCompoundDrawablesWithIntrinsicBounds(placeholder, null, null, null)
                    }

                    override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable>?
                    ) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            et_mobile_no_code.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                    resource,
                                    null,
                                    null,
                                    null
                            )
                        } else
                            et_mobile_no_code.setCompoundDrawablesWithIntrinsicBounds(
                                    resource,
                                    null, null, null
                            )
                    }
                })
    }

    private fun isValid(): Boolean {
        return when {

            tv_country.text.isNullOrEmpty() -> {
                setTextViewError(getString(R.string.choose_country), tv_country)
                false
            }

            countries!!.isNotEmpty() && countries!!.none { country ->
                tv_country.text.toString().equals(country.countryName, true)
            } -> {
                setTextViewError(getString(R.string.choose_country), tv_country)
                false
            }
            tv_currency.text.isNullOrEmpty() -> {
                setTextViewError(getString(R.string.choose_currency), tv_currency)
                false
            }
            (!rb_mr.isChecked && !rb_ms.isChecked && !rb_mrs.isChecked) -> {
                tv_error_rg.visibility = View.VISIBLE
                false
            }
            et_firstname.text.isNullOrEmpty() -> {
                textInputLayout_firstname.setErrorMessage(getString(R.string.enter_first_name))
                et_firstname.requestFocus()
                false
            }
            et_firstname.text.toString().length < 3 -> {
                textInputLayout_firstname.setErrorMessage(getString(R.string.first_name_error))
                et_firstname.requestFocus()
                false
            }
            et_lastname.text.isNullOrEmpty() -> {
                textInputLayout_lastname.setErrorMessage(getString(R.string.enter_last_name))
                et_lastname.requestFocus()
                false
            }
            et_lastname.text.toString().length < 3 -> {
                textInputLayout_lastname.setErrorMessage(getString(R.string.invalid_last_name))
                et_lastname.requestFocus()
                false
            }
            !mobileCodeFlag -> {
                setMobileCodeError(getString(R.string.select_country_code))
                false
            }
            et_mobile_no.text.isNullOrEmpty() -> {
                textInputLayout_mobile_no.setErrorMessage(getString(R.string.enter_mobile))
                // til_mobile_no_code.isErrorEnabled = true
                et_mobile_no.requestFocus()
                false
            }
            et_mobile_no.text.toString().length < 10 -> {
                textInputLayout_mobile_no.setErrorMessage(getString(R.string.error_mobile))
                //til_mobile_no_code.isErrorEnabled = true
                et_mobile_no.requestFocus()
                false
            }
            tv_nationality.text.isNullOrEmpty() -> {
                setTextViewError(getString(R.string.select_nationality), tv_nationality)
                false
            }
            tv_nationality.text.toString()
                    .equals(resources.getString(R.string.nationality), true) -> {
                setTextViewError(getString(R.string.select_nationality), tv_nationality)
                false
            }
            nationalities != null && nationalities!!.none { nationality ->
                tv_nationality.text.toString().equals(nationality.country, true)
            } -> {
                setTextViewError(getString(R.string.select_nationality), tv_nationality)
                false
            }
            (editText_addressLine1.text?.toString() != null && !AddressValidator.validate(
                    editText_addressLine1.text.toString()
            )) -> {
                textInputLayout_addressLine1.setErrorMessage(getString(R.string.address_error))
                editText_addressLine1.requestFocus()
                false
            }
            editText_addressLine1.text.isNullOrEmpty() -> {
                textInputLayout_addressLine1.setErrorMessage(getString(R.string.enter_address))
                editText_addressLine1.requestFocus()
                false
            }
            editText_addressLine1.text.toString().length < 5 -> {
                textInputLayout_addressLine1.setErrorMessage(getString(R.string.error_address))
                editText_addressLine1.requestFocus()
                false
            }
            (editText_addressLine2.text?.toString() != null && !AddressValidator.validate(
                    editText_addressLine2.text.toString()
            )) -> {
                textInputLayout_addressLine2.setErrorMessage(getString(R.string.address_error))
                editText_addressLine2.requestFocus()
                false
            }
            (editText_addressLine2.text.isNullOrEmpty()) -> {
                textInputLayout_addressLine2.setErrorMessage(getString(R.string.enter_address))
                editText_addressLine2.requestFocus()
                false
            }
            (editText_addressLine2.text.toString().length < 5) -> {
                textInputLayout_addressLine2.setErrorMessage(getString(R.string.error_address))
                editText_addressLine2.requestFocus()
                false
            }
            et_relationship.text.isNullOrEmpty() -> {
                textInputLayout_relationship.setErrorMessage(getString(R.string.enter_relationship))
                et_relationship.requestFocus()
                false
            }
            et_relationship.text.toString().length < 3 -> {
                textInputLayout_relationship.setErrorMessage(getString(R.string.relationship_error))
                et_relationship.requestFocus()
                false
            }
            relations != null && relations!!.none { relation ->
                et_relationship.text.toString().equals(relation.name, true)
            } -> {
                setTextViewError(getString(R.string.relationship_error), et_relationship)
                false
            }
            else -> {
                textInputLayout_firstname.clearErrorMessage()
                textInputLayout_lastname.clearErrorMessage()
                textInputLayout_relationship.clearErrorMessage()
                textInputLayout_mobile_no.clearErrorMessage()
                //   til_mobile_no_code.clearErrorMessage()
                textInputLayout_addressLine1.clearErrorMessage()
                textInputLayout_egyptian_id.clearErrorMessage()
                textInputLayout_addressLine2.clearErrorMessage()
                tv_error_rg.visibility = View.GONE
                true
            }
        }
    }

    private fun setTextViewError(message: String, textView: TextView) {
        textView.background = ContextCompat.getDrawable(
                this.context ?: return,
                R.drawable.bg_grey_red_border
        )
        activity?.onFailure(activity?.findViewById(R.id.root_view) ?: return, message)
    }

    private fun setMobileCodeError(message: String) {
        //til_mobile_no_code.boxStrokeColor = ContextCompat.getColor(til_mobile_no_code.context, R.color.colorError)
        activity?.onFailure(activity?.findViewById(R.id.root_view) ?: return, message)
    }

    companion object {
        private const val CASH_PAYOUT = 3
    }
}
