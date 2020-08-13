package com.fh.payday.views2.intlRemittance.cashpayout

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.ListPopupWindow
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
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
import com.fh.payday.views.custombindings.setDrawableLeft
import com.fh.payday.views2.intlRemittance.DeliveryModes
import com.fh.payday.views2.intlRemittance.ExchangeContainer
import com.fh.payday.views2.intlRemittance.mybeneficiaries.addbeneficiary.fragments.ListPopupAdapter
import com.fh.payday.views2.shared.custom.showCountries
import kotlinx.android.synthetic.main.fragment_add_benificiary_cashpayout.*
import kotlinx.android.synthetic.main.fragment_add_benificiary_cashpayout.btn_next
import kotlinx.android.synthetic.main.fragment_add_benificiary_cashpayout.et_firstname
import kotlinx.android.synthetic.main.fragment_add_benificiary_cashpayout.et_lastname
import kotlinx.android.synthetic.main.fragment_add_benificiary_cashpayout.et_mobile_no
import kotlinx.android.synthetic.main.fragment_add_benificiary_cashpayout.et_mobile_no_code
import kotlinx.android.synthetic.main.fragment_add_benificiary_cashpayout.et_relationship
import kotlinx.android.synthetic.main.fragment_add_benificiary_cashpayout.radioButton
import kotlinx.android.synthetic.main.fragment_add_benificiary_cashpayout.rb_mr
import kotlinx.android.synthetic.main.fragment_add_benificiary_cashpayout.rb_mrs
import kotlinx.android.synthetic.main.fragment_add_benificiary_cashpayout.rb_ms
import kotlinx.android.synthetic.main.fragment_add_benificiary_cashpayout.textInputLayout_firstname
import kotlinx.android.synthetic.main.fragment_add_benificiary_cashpayout.textInputLayout_lastname
import kotlinx.android.synthetic.main.fragment_add_benificiary_cashpayout.textInputLayout_mobile_no
import kotlinx.android.synthetic.main.fragment_add_benificiary_cashpayout.textInputLayout_relationship
import kotlinx.android.synthetic.main.fragment_add_benificiary_cashpayout.tv_country
import kotlinx.android.synthetic.main.fragment_add_benificiary_cashpayout.tv_currency
import kotlinx.android.synthetic.main.fragment_add_benificiary_cashpayout.tv_error_rg
import kotlinx.android.synthetic.main.fragment_add_benificiary_cashpayout.tv_nationality
import kotlinx.android.synthetic.main.fragment_beneficiary_details.*
import kotlinx.android.synthetic.main.nationality_layout.*
import kotlinx.android.synthetic.main.nationality_layout.editText_addressLine1
import kotlinx.android.synthetic.main.nationality_layout.editText_addressLine2
import kotlinx.android.synthetic.main.nationality_layout.textInputLayout_addressLine1
import kotlinx.android.synthetic.main.nationality_layout.textInputLayout_addressLine2
import kotlinx.android.synthetic.main.pickup_layout.*
import com.fh.payday.datasource.models.intlRemittance.DeliveryModes as DeliveryMode

class AddBenificiaryCashPayoutFragment : BaseFragment() {
    private lateinit var deliveryModeList: List<DeliveryMode>
    private var selectedTv: String? = null
    private var mobileCodeFlag = false

    enum class Favourite { DEFAULT, FAVOURITE }

    private val viewModel by lazy {
        activity?.let { ViewModelProviders.of(it).get(AddBeneficiaryViewModel::class.java) }
    }
    private var cashPayoutActivity: CashPayoutActivity? = null

    var countries: List<PayoutCountries>? = null
    var nationalities = ArrayList<IsoAlpha3>()
    var relations: List<RelationsItem>? = null
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_benificiary_cashpayout, container, false)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        cashPayoutActivity = context as CashPayoutActivity
    }

    override fun onDetach() {
        super.onDetach()
        activity = null
        cashPayoutActivity = null
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if (isVisible) {
            when (arguments?.getInt(KEY_TYPE, TYPE_DEFAULT) ?: TYPE_DEFAULT) {
                TYPE_SUMMARY -> {
                    initSummary()
                    showDetails()

                }
                TYPE_DEFAULT -> {
                    et_mobile_no_code.text = viewModel?.inputWrapper?.dialCode
                    tv_nationality.setText(viewModel?.nationality)
                    tv_currency.setText(viewModel?.inputWrapper?.currency)
                    loadImage(viewModel?.inputWrapper?.flagPath)
                    initDefault()
                }
            }
        }
    }

    var user: User? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = getActivity() as BaseActivity
        deliveryModeList = ArrayList()
        user = UserPreferences.instance.getUser(activity) ?: return
        handleHint()
        attachObserver()
        attachOtpObserver()
        radioButton.setOnCheckedChangeListener { _, _ ->
            rb_mr.isChecked
            tv_error_rg.visibility = View.GONE
        }

        viewModel?.getPayoutCountries(
                user!!.token,
                user!!.sessionId,
                user!!.refreshToken,
                user!!.customerId.toLong(),
                ExchangeContainer.accessKey,
                viewModel?.selectedDeliveryMode ?: return
        )
        val countryAdapter = ArrayAdapter(
                activity,
                R.layout.layout_autcompletetext,
                mutableListOf<PayoutCountries>()
        )
        tv_country.setAdapter(countryAdapter)
        tv_country.setOnItemClickListener { parent, _, position, _ ->
            val payoutCountries = parent.adapter.getItem(position)
                    as? PayoutCountries? ?: return@setOnItemClickListener
            setCountry(payoutCountries)
            viewModel?.clearBankDetails = true
        }
        tv_country.setOnClickListener {
            tv_currency.text = null
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
        tv_country.setOnFocusChangeListener { view, b ->
            if (!b) {
                if (countries!!.isNotEmpty()) {
                    var country = countries!!.filter { country -> country.countryName.equals(tv_country.text.toString(), true) }.distinct()
                    if (country.isNotEmpty())
                        setCountry(country[0])
                    else {
                        if (cashPayoutActivity == null) return@setOnFocusChangeListener
                        if (!cashPayoutActivity!!.firstPage)
                            if (tv_country.text.isNotEmpty())
                                cashPayoutActivity?.onError(getString(R.string.choose_country_error))
                        //setTextViewError(getString(R.string.choose_country_error), tv_country)
                        tv_nationality.setText("")
                        viewModel?.nationality = ""
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
            } else {
                when (arguments?.getInt(KEY_TYPE, TYPE_DEFAULT) ?: TYPE_DEFAULT) {
                    TYPE_DEFAULT -> {
                        tv_currency.text = null
                        tv_country.showDropDown()
                    }
                }


            }
        }

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
            /*showPayoutCurrency(activity, payOutCurrencies, object : OnPayoutCurrencySelectListener {
                override fun onPayoutCurrencySelect(payoutCurrency: PayOutCurrencies) {
                    viewModel?.inputWrapper?.currency = payoutCurrency.code
                    tv_currency.text = payoutCurrency.code
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

        tv_nationality.setOnClickListener {
            showNationalityDropdown()
        }

        /*    et_relationship.setOnItemClickListener { parent, _, position, _ ->
                val relations = parent.adapter.getItem(position)
                        as? RelationsItem? ?: return@setOnItemClickListener
                et_relationship.setText(relations.name)
            }*/
        /*  et_relationship.setOnFocusChangeListener { view, b ->
              if (b)
                  if (et_relationship.text.toString().isNullOrEmpty())
                      et_relationship.showDropDown()
          }
  */
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
            }
        }


        addTextWatcherListener(et_firstname, textInputLayout_firstname)
        addTextWatcherListener(et_lastname, textInputLayout_lastname)
        addTextWatcherListener(et_mobile_no, textInputLayout_mobile_no)
        addTextWatcherListener(editText_egyptianId, textInputLayout_egyptianId)
        // addTextWatcherListener(et_relationship, textInputLayout_relationship)
        addTextWatcherListener(editText_addressLine1, textInputLayout_addressLine1)
        addTextWatcherListener(editText_addressLine2, textInputLayout_addressLine2)
        init(view)

        val type = arguments?.getInt(KEY_TYPE, TYPE_DEFAULT) ?: TYPE_DEFAULT

        btn_next.setOnClickListener {
            activity.let {
                if (isValid() && type == TYPE_DEFAULT) {
                    /* deliveryModeList = deliveryModeList.filter { deliveryMode ->
                         deliveryMode.code == DeliveryModes.CP
                     }
                     if (deliveryModeList.isEmpty()) {
                         activity.onError(getString(R.string.no_delivery_modes_for_selected_country))
                         return@setOnClickListener
                     }*/
                    saveDetails()
                    viewModel?.selectedRadioType = when {
                        rb_mr.isChecked -> RADIO_MR
                        rb_mrs.isChecked -> RADIO_MRS
                        rb_ms.isChecked -> RADIO_MS
                        else -> null
                    }

                    onSuccess(it)
                } else if (type == TYPE_SUMMARY) {
                    viewModel?.generateOtp(
                            user!!.token,
                            user!!.sessionId,
                            user!!.refreshToken,
                            user!!.customerId.toLong()
                    )
                }
            }

        }
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
        textInputLayout_egyptianId.hint = null
        editText_egyptianId.hint = resources.getString(R.string.egyptian_id)
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
            }

        })
    }

    private fun init(view: View) {
        // initialize common views
    }

    private fun initDefault() {
        lay_nationality.visibility = View.VISIBLE
        pickup_layout.visibility = View.GONE
    }

    private fun initSummary() {
        val user = UserPreferences.instance.getUser(requireContext()) ?: return

        lay_nationality.visibility = View.GONE
        pickup_layout.visibility = View.VISIBLE
        tv_select_remittance.visibility = View.GONE
        btn_next.text = getString(R.string.submit)
        if (viewModel?.flag == 1) {
            tv_favourite_list_added.visibility = View.VISIBLE
        } else {
            tv_favourite_list_added.visibility = View.GONE
        }


        btn_next.setOnClickListener {

            viewModel?.generateOtp(
                    user.token,
                    user.sessionId,
                    user.refreshToken,
                    user.customerId.toLong()
            )
        }

    }

    private fun attachOtpObserver() {
        viewModel?.generateOtpState?.observe(this, Observer {
            it ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                cashPayoutActivity?.showProgress(getString(R.string.loading))
                return@Observer
            }
            cashPayoutActivity?.hideProgress()

            when (state) {
                is NetworkState2.Success -> {
                    cashPayoutActivity?.navigateUp()
                }
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        cashPayoutActivity?.onSessionExpired(state.message)
                        return@Observer
                    }
                    cashPayoutActivity?.handleErrorCode(state.errorCode.toInt(), state.message)
                }
                is NetworkState2.Failure -> cashPayoutActivity?.onFailure(
                        root_view,
                        getString(R.string.request_error)
                )
                else -> cashPayoutActivity?.onFailure(
                        root_view,
                        getString(R.string.request_error)
                )
            }

        })
    }

    private fun onSuccess(activity: BaseActivity) {
        when (activity) {
            is CashPayoutActivity -> activity.navigateUp()
        }
    }

    companion object {
        const val KEY_TYPE = "type"
        const val TYPE_DEFAULT = 6
        const val TYPE_SUMMARY = 9
        const val RADIO_MR = "mr"
        const val RADIO_MS = "ms"
        const val RADIO_MRS = "mrs"

        fun newInstance(type: Int): AddBenificiaryCashPayoutFragment {
            return AddBenificiaryCashPayoutFragment().apply {
                val bundle = Bundle()
                bundle.putInt(KEY_TYPE, type)
                arguments = bundle
            }
        }


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
            /*  !mobileCodeFlag -> {
                  setMobileCodeError(getString(R.string.select_country_code))
                  false
              }*/
            et_mobile_no.text.isNullOrEmpty() -> {
                textInputLayout_mobile_no.setErrorMessage(getString(R.string.enter_mobile))
                et_mobile_no.requestFocus()
                false
            }
            et_mobile_no.text.toString().length < 10 -> {
                textInputLayout_mobile_no.setErrorMessage(getString(R.string.error_mobile))
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
            (tv_nationality.text.toString().equals("Egypt", true) && editText_egyptianId.text.isNullOrEmpty()) -> {
                textInputLayout_egyptianId.setErrorMessage(getString(R.string.enter_egyptian_id))
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

            (!tv_nationality.text.toString().equals("Egypt", true) && editText_addressLine2.text?.toString() != null && !AddressValidator.validate(
                    editText_addressLine2.text.toString()
            )) -> {
                textInputLayout_addressLine2.setErrorMessage(getString(R.string.address_error))
                editText_addressLine2.requestFocus()
                false
            }
            (!tv_nationality.text.toString().equals("Egypt", true) && editText_addressLine2.text.isNullOrEmpty()) -> {
                textInputLayout_addressLine2.setErrorMessage(getString(R.string.enter_address))
                editText_addressLine2.requestFocus()
                false
            }
            (!tv_nationality.text.toString().equals("Egypt", true) && editText_addressLine2.text.toString().length < 5) -> {
                textInputLayout_addressLine2.setErrorMessage(getString(R.string.error_address))
                editText_addressLine2.requestFocus()
                false
            }
            else -> {
                textInputLayout_firstname.clearErrorMessage()
                textInputLayout_lastname.clearErrorMessage()
                textInputLayout_relationship.clearErrorMessage()
                textInputLayout_mobile_no.clearErrorMessage()
                textInputLayout_addressLine1.clearErrorMessage()
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
        // til_mobile_no_code.background = ContextCompat.getDrawable(this.context ?: return, R.drawable.bg_grey_red_border)
        activity?.onFailure(activity?.findViewById(R.id.root_view) ?: return, message)
    }


    private fun setCountryView(countryName: String) {
        tv_country.setText(countryName)
        tv_country.background = ContextCompat.getDrawable(
                context ?: return,
                R.drawable.bg_grey_blue_border
        )
    }

    private fun setNationality(countryName: IsoAlpha3) {
//                    et_mobile_no_code.setText(countryName.dialCode)
        editText_addressLine1.requestFocus()
        tv_nationality.setText(countryName.country)
        viewModel?.nationality = countryName.country
        viewModel?.inputWrapper?.nationality = countryName.countryCode
        tv_nationality.background = ContextCompat.getDrawable(
                context ?: return,
                R.drawable.bg_grey_blue_border
        )
        if (countryName.country.equals("Egypt", true)) {
            textInputLayout_egyptianId.visibility = View.VISIBLE
            textInputLayout_addressLine2.visibility = View.GONE
            editText_addressLine1.imeOptions = EditorInfo.IME_ACTION_DONE
        }else{
            textInputLayout_egyptianId.visibility = View.GONE
            textInputLayout_addressLine2.visibility = View.VISIBLE
        }

    }

    private fun loadNationalityFlag(isoAlpha: ArrayList<IsoAlpha3>) {

        val activity = getActivity() as BaseActivity
        nationalities = isoAlpha
        var nationalityAdapter =
                ArrayAdapter(activity, R.layout.layout_autcompletetext, nationalities!!)
        tv_nationality.setAdapter(nationalityAdapter)
        /* showCountries(activity, isoAlpha, "Select Country", object : OnCountrySelectListener {
             override fun onCountrySelect(countryName: IsoAlpha3) {
                 if (!isoAlpha.isNullOrEmpty()) {
                     tv_nationality.setText(countryName.country)
                     viewModel?.inputWrapper?.nationality = countryName.countryCode
                     tv_nationality.background = ContextCompat.getDrawable(
                             context ?: return,
                             R.drawable.bg_grey_blue_border
                     )

                     *//*til_mobile_no_code.background = ContextCompat.getDrawable(activity,
                        R.drawable.bg_grey_blue_border)*//*
                }
            }
        })*/
    }

    private fun loadMobileCode(isoAlpha: ArrayList<IsoAlpha3>) {
        val activity = getActivity() as BaseActivity
        showCountries(activity, isoAlpha, "Select Country", object : OnCountrySelectListener {
            override fun onCountrySelect(countryName: IsoAlpha3) {
                if (!isoAlpha.isNullOrEmpty()) {
                    mobileCodeFlag = true
                    et_mobile_no_code.setText(countryName.dialCode)
                    et_mobile_no_code.background =
                            resources.getDrawable(R.drawable.bg_grey_blue_border)
                    loadImage(countryName.imagePath)
                    viewModel?.inputWrapper?.flagPath = countryName.imagePath
                    viewModel?.inputWrapper?.dialCode = countryName.dialCode
                    et_mobile_no_code.setTextColor(
                            ContextCompat.getColor(
                                    context
                                            ?: return, R.color.textColor
                            )
                    )
                }
            }
        })
    }

    private fun loadImage(imagePath: String?) {
        GlideApp.with(et_mobile_no_code)
                .load(Uri.parse("$BASE_URL/$imagePath"))
                .placeholder(R.drawable.ic_flag_placeholder)
                .error(R.drawable.ic_flag_placeholder)
                .into(object : CustomTarget<Drawable>(50, 50) {
                    override fun onLoadCleared(placeholder: Drawable?) {
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                            et_mobile_no_code.setCompoundDrawablesRelativeWithIntrinsicBounds(placeholder, null, null, null)
//                        } else
//                            et_mobile_no_code.setCompoundDrawablesWithIntrinsicBounds(placeholder, null, null, null)
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

    private fun saveDetails() {
        viewModel?.inputWrapper?.countryName = tv_country.text.toString()
        viewModel?.inputWrapper?.firstName = et_firstname.text.toString()
        viewModel?.inputWrapper?.lastName = et_lastname.text.toString()
        viewModel?.inputWrapper?.contactNo = et_mobile_no.text.toString()
        viewModel?.inputWrapper?.relationShip = et_relationship.text.toString()
        viewModel?.inputWrapper?.nationality = nationalities.filter { nationality -> nationality.country.equals(tv_nationality.text.toString(), true) }.distinct()[0].countryCode
        if (viewModel?.inputWrapper?.nationality.equals("EG", true))
            viewModel?.inputWrapper?.IdNo = editText_egyptianId.text.toString()
        viewModel?.inputWrapper?.addressLine =
                editText_addressLine1.text.toString() + ", " + editText_addressLine2.text.toString()
    }

    private fun showDetails() {
        et_firstname.setText(viewModel?.inputWrapper?.firstName)
        et_lastname.setText(viewModel?.inputWrapper?.lastName)
        et_mobile_no.setText(viewModel?.inputWrapper?.contactNo)
        val address = viewModel?.inputWrapper?.addressLine?.split(", ")
        editText_addressLine1.setText(address!![0])
        editText_addressLine2.setText(address!![1])
        et_relationship.setText(viewModel?.inputWrapper?.relationShip)
        et_mobile_no_code.setText(viewModel?.inputWrapper?.dialCode)
        et_mobile_no_code.setTextColor(ContextCompat.getColor(requireContext(), R.color.textColor))
        tv_country.setText(viewModel?.inputWrapper?.countryName)
        tv_country.setTextColor(ContextCompat.getColor(requireContext(), R.color.textColor))
        nationalities!!.add(IsoAlpha3(viewModel?.inputWrapper?.nationality!!, ""))
        tv_nationality.setText(viewModel?.nationality)
        //tv_nationality.setText(viewModel?.inputWrapper?.nationality)
        tv_nationality.setTextColor(ContextCompat.getColor(requireContext(), R.color.textColor))
        tv_currency.setText(viewModel?.inputWrapper?.currency)
        tv_currency.setTextColor(ContextCompat.getColor(requireContext(), R.color.textColor))
        pickup_address.text = viewModel?.inputWrapper?.branchName
        pickup_address_details.text = viewModel?.inputWrapper?.branchAddress
        loadImage(viewModel?.inputWrapper?.flagPath)
        et_firstname.isEnabled = false
        et_lastname.isEnabled = false
        et_mobile_no.isEnabled = false
        editText_addressLine1.isEnabled = false
        editText_addressLine2.isEnabled = false
        et_relationship.isEnabled = false
        et_mobile_no_code.isEnabled = false
        tv_country.isEnabled = false
        tv_currency.isEnabled = false
        tv_nationality.isEnabled = false
        radioButton.isEnabled = false
        rb_mr.isEnabled = false
        rb_ms.isEnabled = false
        rb_mrs.isEnabled = false

        when (viewModel?.selectedRadioType) {
            RADIO_MR -> rb_mr.isChecked = true
            RADIO_MS -> rb_ms.isChecked = true
            RADIO_MRS -> rb_mrs.isChecked = true
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
                            DeliveryModes.CP,
                            "Relation"
                    )
                    if (selectedTv == "mobileCode")
                        loadMobileCode(isoAlpha)
                    else
                        loadNationalityFlag(isoAlpha)
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
                    activity?.onFailure(root_view, countryState.throwable)
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
                    if (viewModel?.inputWrapper?.nationality == null)
                        viewModel?.getCountries(
                                user!!.token,
                                user!!.sessionId,
                                user!!.refreshToken,
                                user!!.customerId.toString()
                        )
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

        viewModel?.submitBeneficiaryDetails?.observe(
                viewLifecycleOwner, Observer {
            pickup_address.text = viewModel?.inputWrapper?.branchName
            pickup_address_details.text = viewModel?.inputWrapper?.branchAddress
        })
    }

    private fun setCountry(payoutCountry: PayoutCountries) {
        if (!payoutCountry.deliveryModes.isNullOrEmpty()) {
            viewModel?.inputWrapper?.countryName = payoutCountry.countryName
            viewModel?.inputWrapper?.countryCode = payoutCountry.countryCode
            viewModel?.payOutCurrencies = payoutCountry.payOutCurrencies
            if (viewModel?.payOutCurrencies?.size == 1) {
                tv_currency.setText(viewModel?.payOutCurrencies?.get(0)?.code)
                viewModel?.inputWrapper?.currency = viewModel?.payOutCurrencies?.get(0)?.code
                tv_currency.background = ContextCompat.getDrawable(
                        activity!!,
                        R.drawable.bg_grey_blue_border
                )
            }
            if (payoutCountry.nationality.equals("Egypt", true)) {
                textInputLayout_egyptianId.visibility = View.VISIBLE
                textInputLayout_addressLine2.visibility = View.GONE
                editText_addressLine1.imeOptions = EditorInfo.IME_ACTION_DONE
            } else{
                textInputLayout_egyptianId.visibility = View.GONE
                textInputLayout_addressLine2.visibility = View.VISIBLE
            }
            tv_nationality.setText(payoutCountry.nationality)
            viewModel?.nationality = payoutCountry.nationality
            viewModel?.inputWrapper?.nationality = payoutCountry.countryCode
            tv_nationality.background = ContextCompat.getDrawable(
                    context ?: return,
                    R.drawable.bg_grey_blue_border
            )
            mobileCodeFlag = true
            et_mobile_no_code.setText(payoutCountry.dialCode)
            et_mobile_no_code.background = resources.getDrawable(R.drawable.bg_grey_blue_border)
            loadImage(payoutCountry.flag)
            viewModel?.inputWrapper?.flagPath = payoutCountry.flag
            viewModel?.inputWrapper?.dialCode = payoutCountry.dialCode
            et_mobile_no_code.setTextColor(ContextCompat.getColor(context
                    ?: return, R.color.textColor))
            et_mobile_no_code.setTextColor(
                    ContextCompat.getColor(
                            context ?: return,
                            R.color.textColor
                    )
            )
            deliveryModeList = payoutCountry.deliveryModes
            val deliveryModes = payoutCountry.deliveryModes.filter { deliveryMode ->
                deliveryMode.code == DeliveryModes.CP
            }
            // TODO handle on next button , display choose another country
            if (!deliveryModes.isNullOrEmpty())
                viewModel?.inputWrapper?.deliveryMode = deliveryModes[0].code

            setCountryView(payoutCountry.countryName)
            nationalities!!.add(IsoAlpha3(payoutCountry.nationality, payoutCountry.countryCode))
        }
    }
}