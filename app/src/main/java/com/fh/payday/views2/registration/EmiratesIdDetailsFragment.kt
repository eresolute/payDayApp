package com.fh.payday.views2.registration

import android.app.DatePickerDialog
import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.models.CardLoanResult
import com.fh.payday.datasource.models.IsoAlpha3
import com.fh.payday.datasource.models.customer.EmiratesID
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.*
import com.fh.payday.utilities.maskededittext.MaskedEditText
import com.fh.payday.views.fragments.OTPFragment
import com.fh.payday.views2.kyc.update.KycOptionActivity
import com.fh.payday.views2.shared.custom.AlertDialogFragment
import com.fh.payday.views2.shared.custom.showCountries
import com.fh.payday.views2.shared.fragments.DatePickerFragment
import com.fh.payday.views2.shared.fragments.ExpiryDatePickerFragment
import com.fh.payday.views2.shared.fragments.TermsConditionsDialogFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.nio.charset.Charset
import java.util.*

class EmiratesIdDetailsFragment : Fragment(), OnItemClickListener {

    private var activity: BaseActivity? = null
    private lateinit var etEmiratesId: MaskedEditText
    private lateinit var llNationality: View
    private lateinit var tvNationality: TextView
    private lateinit var tvExpiry: TextView
    private lateinit var tvDob: TextView
    private lateinit var tvNationalityLabel: TextView
    private lateinit var tilEmiratesId: TextInputLayout
    private lateinit var checkBox: CheckBox
    private lateinit var btnSubmit: Button
    private var emiratesId: EmiratesID? = null
    private var counter: Int = 0
    private var emirates = ""
    private var expiry = ""
    private var dob = ""
    private var nationality = ""

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context as BaseActivity?
    }

    override fun onDetach() {
        super.onDetach()
        activity = null
        counter = 0
    }

    override fun onResume() {
        super.onResume()
        when (val activity = activity ?: return) {
            is KycOptionActivity -> activity.setToolbarText(getString(R.string.update_emirates))
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        counter = 0
        if (isVisibleToUser) {

            when (val activity = activity ?: return) {
                is RegisterActivity -> {
                    activity.viewModel.clearWrapper()
                    activity.resetViewPager()

                    emiratesId = activity.viewModel.inputWrapper.emiratesId

                    // val id = emiratesId?.id ?: ""

//                    val emiratesID = if (emiratesId?.id != null && !TextUtils.isEmpty(id))
//                        maskCardNumber(id, "###-####-#######-#")
//                    else ""

                    etEmiratesId.setText(emiratesId?.id ?: "")

                    val country = getCountry(emiratesId?.country)
                    if (country.isEmpty()) {
                        clearNationalityView()
                    } else {
                        setNationalityView(country)
                    }

                    val expiry = emiratesId?.expiry ?: ""
                    val dob = emiratesId?.dob ?: ""

                    if (expiry.isEmpty()) unsetDateView(tvExpiry)
                    else setDateView(tvExpiry, expiry)

                    if (dob.isEmpty()) unsetDateView(tvDob)
                    else setDateView(tvDob, dob)

                    if (activity.viewModel.isEmiratesDetailSet) {
                        activity.viewModel.isEmiratesDetailSet = false
                        clearFields()
                    }
                }
            }
        }
    }

    private fun clearFields() {
        etEmiratesId.text = null
        tvNationality.text = null
        tvExpiry.text = null
        tvDob.text = null
        checkBox.isChecked = false

    }

    private fun setDetails() {

        val activity = activity as KycOptionActivity
        //activity.getViewModel().getCountries()

        llNationality.isClickable = false
        llNationality.isEnabled = false
        llNationality.visibility = View.GONE
        tvNationalityLabel.visibility = View.GONE

        val emiratesId = activity.getViewModel().inputWrapper.emiratesId

        //  val emiratesID = if (emiratesId?.id != null && !TextUtils.isEmpty(emiratesId.id) && !(emiratesId.id).isEmpty())
        //    maskCardNumber((emiratesId.id), "###-####-#######-#")
        //else ""

        etEmiratesId.setText(emiratesId?.id ?: "")

        val country = getCountry(emiratesId?.country)
        if (country.isEmpty()) {
            clearNationalityView()
        } else {
            setNationalityView(country)
        }

        val expiry = emiratesId?.expiry ?: ""
        val dob = emiratesId?.dob ?: ""

        if (expiry.isEmpty()) unsetDateView(tvExpiry)
        else setDateView(tvExpiry, expiry)

        if (dob.isEmpty()) unsetDateView(tvDob)
        else setDateView(tvDob, dob)

        activity.getViewModel().isEmirateDetailSet
        activity.getViewModel().isEmirateDetailSet

        if (activity.getViewModel().isEmirateDetailSet) {
            activity.getViewModel().isEmirateDetailSet = false
            clearFields()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_emirates_details, container, false)

        etEmiratesId = view.findViewById(R.id.et_emirates_id)
        tvExpiry = view.findViewById(R.id.tv_expiry)
        llNationality = view.findViewById(R.id.ll_nationality)
        tvNationalityLabel = view.findViewById(R.id.tv_nationality_label)
        tvNationality = view.findViewById(R.id.tv_nationality)
        tvDob = view.findViewById(R.id.tv_dob)
        tilEmiratesId = view.findViewById(R.id.textInputLayout_emirates_id)
        checkBox = view.findViewById(R.id.cb_terms_conditions)
        btnSubmit = view.findViewById(R.id.btn_submit)

        etEmiratesId.setOnEditorActionListener { _, _, _ -> false }
        etEmiratesId.hint = getString(R.string.placeholder)
        if (activity is KycOptionActivity) {
            setDetails()
        }
        view.findViewById<TextView>(R.id.tv_terms_condition_link).apply {
            setTextWithUnderLine(text.toString())
            setOnClickListener {

                val dialogFragment: DialogFragment = TermsConditionsDialogFragment
                    .newInstance(DIGITAL_BANKING_TC_URL, getString(R.string.close))
                if (activity == null) return@setOnClickListener
                dialogFragment.show(activity?.supportFragmentManager, "dialog")
            }
        }

        tvDob.setOnClickListener {
            setEmiratesIdEnabled()
            showDobDatePicker()
        }
        tvExpiry.setOnClickListener {
            setEmiratesIdEnabled()
            showExpiryPicker()
        }
        val clickListener = View.OnClickListener {
            setEmiratesIdEnabled()
            val countries = getCountries()
            if (!countries.isNullOrEmpty()) {
                showCountries(activity
                    ?: return@OnClickListener, countries, getString(R.string.select_country), object : OnCountrySelectListener {
                    override fun onCountrySelect(countryName: IsoAlpha3) {
                        val countryList = getCountries() ?: return
                        if (!countryList.isNullOrEmpty()) {
                            setNationalityView(countryName.country)
                        }
                    }

                })
            }
        }
        llNationality.setOnClickListener(clickListener)
        view.findViewById<View>(R.id.iv_nationality).setOnClickListener(clickListener)

        addTextWatcherListener(etEmiratesId, tilEmiratesId)

        btnSubmit.setOnClickListener {
            if (validateEditText()) {

                setEmiratesIdEnabled()

                val emiratesId = etEmiratesId.text?.toString()?.trim()?.replace("-", "") ?: ""
                emirates = emiratesId
                expiry = tvExpiry.text.toString()
                dob = tvDob.text.toString()
                nationality = getCountryCode(tvNationality.text.toString())

                btnSubmit.isEnabled = false
                submitEmiratesId(emiratesId, expiry, dob, nationality)
            }
        }

        addObserver()

        return view
    }

    private fun setEmiratesIdEnabled() {
        when (val activity = activity ?: return) {
            is RegisterActivity -> activity.viewModel.isEmiratesDetailSet = true
            is KycOptionActivity -> activity.getViewModel().isEmirateDetailSet = true
        }
    }

    override fun onItemClick(index: Int) {
        val countries = getCountries() ?: return
        if (!countries.isNullOrEmpty() && countries.size > index) {
            val (country) = countries[index]
            setNationalityView(country)
        }
    }

    private fun submitEmiratesId(
        emiratesId: String,
        expiry: String,
        dob: String,
        nationality: String
    ) {

        if (activity?.isNetworkConnected() == false) {
            btnSubmit.isEnabled = true
            return activity?.onFailure(activity?.findViewById(R.id.root_view) ?: return,
                    getString(R.string.no_internet_connectivity)) ?: Unit
        }

        when (val activity = activity ?: return) {
            is RegisterActivity -> {
                val inputWrapper = activity.viewModel.inputWrapper
                val emiratesID = inputWrapper.emiratesId ?: EmiratesID().apply {
                    inputWrapper.emiratesId = this
                }

                emiratesID.id = emiratesId
                emiratesID.expiry = expiry
                emiratesID.dob = dob
                emiratesID.country = nationality

                val front = inputWrapper.capturedEmiratesFront ?: return
                val back = inputWrapper.capturedEmiratesBack ?: return

                activity.viewModel.submitEmiratesId(front, back, emiratesId, expiry, dob,
                    nationality, emiratesID.gender)
            }
            is KycOptionActivity -> {
                val user = UserPreferences.instance.getUser(activity) ?: return
                activity.getViewModel().generateOtp(user.token, user.sessionId, user.refreshToken, user.customerId.toLong())

            }
        }
    }

    private fun showExpiryPicker() {
        val fragmentManager = activity?.supportFragmentManager ?: return

        val maxDate = Calendar.getInstance().apply {
            set(get(Calendar.YEAR) + 3, get(Calendar.MONTH), get(Calendar.DAY_OF_MONTH))
        }.time
        val minDate = Calendar.getInstance().apply {
            set(get(Calendar.YEAR), get(Calendar.MONTH), get(Calendar.DAY_OF_MONTH))
            add(Calendar.DAY_OF_MONTH, 1)
        }.time

        val day: Int
        val month: Int
        val year: Int
        val calendar = Calendar.getInstance()
        when (activity) {
            is KycOptionActivity -> {
                day = if ((activity as KycOptionActivity).getViewModel().expiryDay < 0) calendar.get(Calendar.DAY_OF_MONTH)
                else (activity as KycOptionActivity).getViewModel().expiryDay

                month = if ((activity as KycOptionActivity).getViewModel().expiryMonth < 0) calendar.get(Calendar.MONTH)
                else (activity as KycOptionActivity).getViewModel().expiryMonth

                year = if ((activity as KycOptionActivity).getViewModel().expiryYear < 0) calendar.get(Calendar.YEAR)
                else (activity as KycOptionActivity).getViewModel().expiryYear
            }
            is RegisterActivity -> {
                day = if ((activity as RegisterActivity).viewModel.expiryDay < 0) calendar.get(Calendar.DAY_OF_MONTH)
                else (activity as RegisterActivity).viewModel.expiryDay

                month = if ((activity as RegisterActivity).viewModel.expiryMonth < 0) calendar.get(Calendar.MONTH)
                else (activity as RegisterActivity).viewModel.expiryMonth

                year = if ((activity as RegisterActivity).viewModel.expiryYear < 0) calendar.get(Calendar.YEAR)
                else (activity as RegisterActivity).viewModel.expiryYear

            }
            else -> {
                day = calendar.get(Calendar.DAY_OF_MONTH)
                month = calendar.get(Calendar.MONTH)
                year = calendar.get(Calendar.YEAR)
            }
        }

        ExpiryDatePickerFragment.Builder()
            .attachDateSetListener(DatePickerDialog.OnDateSetListener { _, y, m, dayOfMonth ->
                val d = String.format(getString(R.string.date_format),
                    String.format("%02d", dayOfMonth), String.format("%02d", (m + 1)), y.toString())
                setDateView(tvExpiry, d)
                saveEmiratesDate(dayOfMonth, m, y)
            })
            .setDay(day)
            .setMonth(month)
            .setYear(year)
            .setMaxDate(maxDate)
            .setMinDate(minDate)
            .build()
            .show(fragmentManager, "datePicker")
    }

    private fun showDobDatePicker() {
        val fragmentManager = activity?.supportFragmentManager ?: return
        val maxDate = Calendar.getInstance().apply {
            set(get(Calendar.YEAR) - 20, get(Calendar.MONTH), get(Calendar.DAY_OF_MONTH))
        }
        val minDate = Calendar.getInstance().apply {
            set(get(Calendar.YEAR) - 70, get(Calendar.MONTH), get(Calendar.DAY_OF_MONTH))
        }

        val day: Int
        val month: Int
        val year: Int

        when (activity) {
            is KycOptionActivity -> {
                day = if ((activity as KycOptionActivity).getViewModel().day < 0) maxDate.get(Calendar.DAY_OF_MONTH)
                else (activity as KycOptionActivity).getViewModel().day

                month = if ((activity as KycOptionActivity).getViewModel().month < 0) maxDate.get(Calendar.MONTH)
                else (activity as KycOptionActivity).getViewModel().month

                year = if ((activity as KycOptionActivity).getViewModel().year < 0) maxDate.get(Calendar.YEAR)
                else (activity as KycOptionActivity).getViewModel().year
            }
            is RegisterActivity -> {
                day = if ((activity as RegisterActivity).viewModel.day < 0) maxDate.get(Calendar.DAY_OF_MONTH)
                else (activity as RegisterActivity).viewModel.day

                month = if ((activity as RegisterActivity).viewModel.month < 0) maxDate.get(Calendar.MONTH)
                else (activity as RegisterActivity).viewModel.month

                year = if ((activity as RegisterActivity).viewModel.year < 0) maxDate.get(Calendar.YEAR)
                else (activity as RegisterActivity).viewModel.year

            }
            else -> {
                day = maxDate.get(Calendar.DAY_OF_MONTH)
                month = maxDate.get(Calendar.MONTH)
                year = maxDate.get(Calendar.YEAR)
            }
        }

        DatePickerFragment.Builder()
            .setDay(day)
            .setMonth(month)
            .setYear(year)
            .setMinDate(minDate.time)
            .setMaxDate(Calendar.getInstance().apply {
                set(get(Calendar.YEAR) - 20, get(Calendar.MONTH), get(Calendar.DAY_OF_MONTH))
                add(Calendar.DAY_OF_MONTH, -1)
            }.time)
            .attachDateSetListener(DatePickerDialog.OnDateSetListener { _, y, m, dayOfMonth ->
                val d = String.format(getString(R.string.date_format),
                    String.format("%02d", dayOfMonth), String.format("%02d", (m + 1)), y.toString())
                setDateView(tvDob, d)
                saveDOBDate(dayOfMonth, m, y)
            })
            .build()
            .show(fragmentManager, "datePicker")
    }

    private fun saveDOBDate(day: Int, month: Int, year: Int) {
        when (activity) {
            is KycOptionActivity -> {
                (activity as KycOptionActivity).getViewModel().day = day
                (activity as KycOptionActivity).getViewModel().month = month
                (activity as KycOptionActivity).getViewModel().year = year
            }
            is RegisterActivity -> {
                (activity as RegisterActivity).viewModel.day = day
                (activity as RegisterActivity).viewModel.month = month
                (activity as RegisterActivity).viewModel.year = year
            }
            else -> throw IllegalStateException()
        }
    }

    private fun saveEmiratesDate(day: Int, month: Int, year: Int) {
        when (activity) {
            is KycOptionActivity -> {
                (activity as KycOptionActivity).getViewModel().expiryDay = day
                (activity as KycOptionActivity).getViewModel().expiryMonth = month
                (activity as KycOptionActivity).getViewModel().expiryYear = year
            }
            is RegisterActivity -> {
                (activity as RegisterActivity).viewModel.expiryDay = day
                (activity as RegisterActivity).viewModel.expiryMonth = month
                (activity as RegisterActivity).viewModel.expiryYear = year
            }
            else -> throw IllegalStateException()
        }
    }

    private fun addTextWatcherListener(editText: MaskedEditText, textInputLayout: TextInputLayout?) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
//                setEmiratesIdEnabled()
                textInputLayout?.clearErrorMessage()
            }

            override fun afterTextChanged(editable: Editable) {
                //formatEmiratesId(editText, editable)
                val activity = activity ?: return

                val emiratesId = emiratesId ?: return

                if (emiratesId.id.isEmpty() && emiratesId.dob.isEmpty() && emiratesId.expiry.isEmpty() && emiratesId.gender.isEmpty()) {
                    when (activity) {
                        is RegisterActivity -> {
                            activity.viewModel.isEmiratesDetailSet = true
                        }
                        is KycOptionActivity -> activity.getViewModel().isEmirateDetailSet = true
                    }
                }
            }
        })
    }

    private fun setDateView(view: TextView, date: String) {
        view.text = date
        view.background = ContextCompat.getDrawable(this.context
            ?: return, R.drawable.bg_grey_blue_border)
    }

    private fun unsetDateView(view: TextView, hasError: Boolean = false, message: String = "Required") {
        if (hasError) {
            view.background = ContextCompat.getDrawable(this.context
                ?: return, R.drawable.bg_grey_red_border)
            activity?.onFailure(activity?.findViewById(R.id.root_view) ?: return, message)
        } else {
            view.text = ""
            view.background = ContextCompat.getDrawable(this.context
                ?: return, R.drawable.bg_grey_dark_grey_border)
        }
    }

    private fun setNationalityView(text: String) {
        llNationality.background = ContextCompat.getDrawable(this.context ?: return,
            R.drawable.bg_grey_blue_border)
        tvNationality.text = text
    }

    private fun setNationalityError(message: String) {
        llNationality.background = ContextCompat.getDrawable(this.context ?: return,
            R.drawable.bg_grey_red_border)
        activity?.onFailure(activity?.findViewById(R.id.root_view) ?: return, message)
    }

    private fun clearNationalityView() {
        llNationality.background = ContextCompat.getDrawable(this.context ?: return,
            R.drawable.bg_grey_dark_grey_border)
        tvNationality.text = ""
    }

    private fun getCountries(): List<IsoAlpha3>? {
        //val activity = activity
        val country = loadJSONFromAsset() ?: return null
        return Gson().fromJson<List<IsoAlpha3>>(country, object : TypeToken<List<IsoAlpha3>>() {}.type)
        //return Gson().fromJson<List<IsoAlpha3>>(country, IsoAlpha3::class.java)
        /*   when (activity) {
               is RegisterActivity -> {
                   val countries = activity.viewModel.countries
                   if (!countries.isNullOrEmpty()) return countries

                   return null
               }
               is KycOptionActivity -> {
                   val countries = activity.getViewModel().countries
                   if (!countries.isNullOrEmpty()) return countries

                   return null
               }
               else -> return null
           }*/
    }

    private fun getCountryCode(country: String): String {
        var countryCode = ""
        getCountries()?.forEach {
            if (it.country == country) {
                countryCode = it.code
            }
        }

        return countryCode
    }

    private fun getCountry(code: String?): String {
        if (code.isNullOrEmpty()) return ""

        var country = ""
        getCountries()?.forEach {
            if (it.code == code) {
                country = it.country
            }
        }

        return country
    }

    private fun validateEditText(): Boolean {

        val emiratesID = etEmiratesId.text.toString().trim().replace("-", "")

        if (TextUtils.isEmpty(emiratesID)) {
            tilEmiratesId.setErrorMessage(getString(R.string.empty_emirate_id))
            etEmiratesId.requestFocus()
            return false
        } else if (emiratesID.length < 15) {
            tilEmiratesId.error = getString(R.string.invalid_emirate_id)
            etEmiratesId.requestFocus()
            return false
        } else if (!Validator.isValidEmiratesId(emiratesID)) {
            tilEmiratesId.error = getString(R.string.invalid_emirate_id)
            etEmiratesId.requestFocus()
            return false
        } else if (tvExpiry.text.isNullOrEmpty()) {
            unsetDateView(tvExpiry, true, getString(R.string.empty_expiry_date))
            return false
        } else if (!DateTime.isValid(tvExpiry.text?.toString() ?: "", "dd/MM/yyyy")
            || !DateTime.isAfter(tvExpiry.text?.toString() ?: "")) {
            unsetDateView(tvExpiry, true, getString(R.string.invalid_expiry_date))
            return false
        } else if (tvNationality.text.isNullOrEmpty() && activity is RegisterActivity) {
            setNationalityError(getString(R.string.empty_nationality))
            return false
        } else if (getCountryCode(tvNationality.text.toString()).trim().isEmpty() && activity is RegisterActivity) {
            setNationalityError(getString(R.string.invalid_nationality))
            return false
        } else if (tvDob.text.isNullOrEmpty()) {
            unsetDateView(tvDob, true, getString(R.string.empty_dob))
            return false
        } else if (!DateTime.isValid(tvDob.text?.toString() ?: "", "dd/MM/yyyy")
            || !DateTime.isBefore(tvDob.text?.toString() ?: "")) {
            unsetDateView(tvDob, true, getString(R.string.invalid_dob))
            return false
        } else if (!Validator.isValidDob(emiratesID, tvDob.text.toString().trim())) {
            unsetDateView(tvDob, true, getString(R.string.dob_mismatch))
            return false
        } else if (!checkBox.isChecked) {
            val message = getString(R.string.terms_conditions_error)
            activity?.onFailure(activity?.findViewById(R.id.root_view) ?: return false, message)
            return false
        }

        tilEmiratesId.clearErrorMessage()
        return true
    }

    private fun loadJSONFromAsset(): String? {
        var json: String? = null
        try {
            val inputStream = getActivity()!!.assets.open("countries.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer, Charset.forName("UTF-8"))
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

        return json
    }

    private fun addObserver() {
        when (val activity = activity ?: return) {
            is RegisterActivity -> regEmiratesIdObserver(activity)
            is KycOptionActivity -> kycEmiratesIdObserver(activity)
        }
    }

    private fun regEmiratesIdObserver(a: RegisterActivity) {
        a.viewModel.emiratesIdState.observe(this, Observer {
            val activity = (getActivity() as? RegisterActivity?) ?: return@Observer
            it ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                btnSubmit.isEnabled = false
                return@Observer activity.showProgress(getString(R.string.processing))
            }

            btnSubmit.isEnabled = true
            activity.hideProgress()

            when (state) {
                is NetworkState2.Success<*> -> activity.handleEmiratesResponse(state.data as CardLoanResult?)
                is NetworkState2.Error -> activity.handleErrorResponse(state.message, state.errorCode)
                is NetworkState2.Failure -> activity.onFailure(activity.findViewById(R.id.root_view)
                    ?: return@Observer, state.throwable)
                else -> activity.onFailure(activity.findViewById(R.id.root_view)
                    ?: return@Observer, CONNECTION_ERROR)
            }
        })
    }


    private fun kycEmiratesIdObserver(a: KycOptionActivity) {
        a.getViewModel().emiratesDetail.observe(a, Observer {
            val activity = (getActivity() as? KycOptionActivity?) ?: return@Observer
            it ?: return@Observer

            val emiratesState = it.getContentIfNotHandled() ?: return@Observer

            if (emiratesState is NetworkState2.Loading) {
                return@Observer activity.showProgress(getString(R.string.processing))
            }

            activity.hideProgress()

            when (emiratesState) {
                is NetworkState2.Success -> {
                    AlertDialogFragment.Builder()
                        .setMessage(getString(R.string.emirates_update_succesful))
                        .setIcon(R.drawable.ic_success_checked_blue)
                        .setCancelable(false)
                        .setConfirmListener { dialog ->
                            dialog.dismiss()
                            activity.finish()
                        }
                        .build()
                        .show(activity.supportFragmentManager, "")
                }
                is NetworkState2.Error -> {

                    if (emiratesState.isSessionExpired) {
                        activity.onSessionExpired(emiratesState.message)
                        return@Observer
                    }

                    if (emiratesState.errorCode.toInt() == STATUS_EMIRATES_QUEUE) {
                        activity.showMessage(emiratesState.message,
                            R.drawable.ic_error,
                            R.color.colorError,
                            AlertDialogFragment.OnConfirmListener {
                                activity.finish()
                            })
                        return@Observer
                    }

//                    activity.onError(emiratesState.message)
                    activity.handleErrorCode(emiratesState.errorCode.toInt(), emiratesState.message)

                }
                is NetworkState2.Failure -> activity.onFailure(activity.findViewById(R.id.root_view),
                        emiratesState.throwable)
                else -> activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })


        a.getViewModel().otpRequest.observe(this, Observer {
            val activity = (getActivity() as? KycOptionActivity?) ?: return@Observer
            it ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                btnSubmit.isEnabled = false
                return@Observer activity.showProgress(getString(R.string.processing))
            }
            btnSubmit.isEnabled = true
            activity.hideProgress()
            when (state) {
                is NetworkState2.Success -> {
                    val fragment = OTPFragment.Builder(object : OnOTPConfirmListener {
                        override fun onOtpConfirm(otp: String) {
                            val user = UserPreferences.instance.getUser(activity) ?: return
                            val inputWrapper = activity.getViewModel().inputWrapper
                            val emiratesID = inputWrapper.emiratesId ?: EmiratesID().apply {
                                inputWrapper.emiratesId = this
                            }

                            emiratesID.id = emirates
                            emiratesID.expiry = expiry
                            emiratesID.dob = dob
                            emiratesID.country = nationality

                            val front = inputWrapper.capturedEmiratesFront ?: return
                            val back = inputWrapper.capturedEmiratesBack ?: return

                            activity.getViewModel().updateEmirates(user.token, user.sessionId, user.refreshToken,
                                user.customerId.toLong(), front, back, emirates,
                                expiry, nationality, emiratesID.gender, dob, otp)
                        }

                    }).setPinLength(6)
                        .setTitle(getString(R.string.enter_otp))
                        .setOnResumeListener {
                            activity.setToolbarText(getString(R.string.verify_otp))
                        }
                        .setButtonTitle(getString(R.string.submit))
                        .setHasCardView(true).build()
                    activity.replaceFragment(fragment)
                }
                is NetworkState2.Error -> {
                    if (state.isSessionExpired)
                        return@Observer activity.onSessionExpired(state.message)
//                    activity.onError(state.message)
                    activity.handleErrorCode(state.errorCode.toInt(), state.message)
                }
                is NetworkState2.Failure -> activity.onFailure(activity.findViewById(R.id.root_view), state.throwable)
                else -> activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }


}