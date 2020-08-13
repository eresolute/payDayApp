package com.fh.payday.views2.registration

import android.content.Context
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.utilities.*
import com.fh.payday.views2.shared.calendar.MonthPickerDialog
import java.util.*
import org.joda.time.DateTime as JodaDateTime

class RegisterCardDetailsFragment : Fragment() {

    private lateinit var textInputLayoutCardNo: TextInputLayout
    private lateinit var textInputLayoutCardName: TextInputLayout
    private lateinit var etCardNumber: TextInputEditText
    private lateinit var etCardName: TextInputEditText
    private lateinit var tvExpiry: TextView

    private var activity: RegisterActivity? = null

    private val isValidCardNo: Boolean
        get() {
            val cardNumber = if (etCardNumber.text != null) etCardNumber.text.toString() else ""
            return cardNumber.replace("\\s+", "").length >= 16
        }

    private val isValidName: Boolean
        get() {
            val cardName = if (etCardName.text != null) etCardName.text.toString() else ""
            return cardName.length >= 2
        }

    private val isValidExpiry: Boolean
        get() {
            val expiry = if (tvExpiry.text != null) tvExpiry.text.toString() else ""
            return (expiry.trim().matches(DateTime.REGEX_MM_YY.toRegex())
                && DateTime.isValid(expiry.trim(), "MM/YY")
                && DateTime.isAfter(expiry, "MM/YY"))
        }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context as RegisterActivity?
    }

    override fun onDetach() {
        super.onDetach()
        activity = null
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        val activity = activity ?: return

        if (isVisibleToUser) {
            val wrapper = activity.viewModel.wrapper
            val card = wrapper.cardDetails
            if (card != null) {
                val cardNo = if (card.cardNumber != null && !TextUtils.isEmpty(card.cardNumber))
                    maskCardNumber(card.cardNumber, "XXXX XXXX XXXX ####")
                else ""
                val cardName = card.customerName ?: ""
                val expiry = if (card.cardExpiryDate != null)
                    DateTime.parse(card.cardExpiryDate, "yyyy-MM-dd", "MM/yy")
                else ""

                etCardNumber.setText(cardNo)
                etCardName.setText(cardName)
                tvExpiry.text = expiry

                setBehaviour(etCardNumber, isValidCardNo)
                setBehaviour(etCardName, isValidName)
                setBehaviour(tvExpiry, isValidExpiry)

                if(isValidExpiry) {
                    this.context?.apply {
                        tvExpiry.setTextColor(ContextCompat.getColor(this, R.color.textDisabled))
                    }
                }
            } else {
                val inputWrapper = activity.viewModel.inputWrapper
                etCardNumber.setText(if (inputWrapper.cardNumber != null) inputWrapper.cardNumber else "")
                etCardName.setText(if (inputWrapper.cardName != null) {
                    inputWrapper.cardName
                } else "")
                tvExpiry.text = if (inputWrapper.cardExpiry != null) inputWrapper.cardExpiry else ""
            }

            if(activity.viewModel.isPaydayDetailSet && card == null) clearData(activity)
        }
    }

    private fun clearData(activity: RegisterActivity) {
        activity.viewModel.isPaydayDetailSet = false
        etCardName.text = null
        etCardNumber.text = null
        tvExpiry.text = null

        etCardName.isEnabled = true
        etCardNumber.isEnabled = true
        tvExpiry.isEnabled = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_register_card_details, container, false)

        val btnConfirm = view.findViewById<Button>(R.id.btn_confirm)
        etCardNumber = view.findViewById(R.id.et_card_number)
        etCardName = view.findViewById(R.id.et_card_name)
        tvExpiry = view.findViewById(R.id.tv_expiry)

        etCardNumber.addTextChangedListener(CardFormatWatcher())

        textInputLayoutCardNo = view.findViewById(R.id.textInputLayout_card_no)
        textInputLayoutCardName = view.findViewById(R.id.textInputLayout_card_name)

        addTextWatcherListener(etCardNumber, textInputLayoutCardNo)
        addTextWatcherListener(etCardName, textInputLayoutCardName)
        tvExpiry.setOnClickListener { showMonthYearPicker() }

        if (activity != null) {
            val inputWrapper = activity?.viewModel?.inputWrapper
            btnConfirm.setOnClickListener {
                if (validateEditText()) {
                    val cardNo = if (etCardNumber.text != null) etCardNumber.text.toString() else ""
                    val cardName = if (etCardName.text != null) etCardName.text.toString() else ""
                    val expiryDate = if (tvExpiry.text != null) tvExpiry.text.toString() else ""

                    inputWrapper?.cardNumber = cardNo
                    inputWrapper?.cardName = cardName
                    inputWrapper?.cardExpiry = expiryDate

                    activity?.navigateUp()
                }
            }
        }

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        return view
    }

    private fun addTextWatcherListener(editText: TextInputEditText, textInputLayout: TextInputLayout) {
        editText.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (editText.text == null) return

                val text = editText.text.toString()
                if (text.startsWith(" ")) {
                    editText.setText(text.trim())
                }
                textInputLayout.clearErrorMessage()
            }

            override fun afterTextChanged(editable: Editable) {

            }
        })
    }

    private fun setBehaviour(view: View, isValid: Boolean) {
        view.isEnabled = !isValid
    }

    private fun setDateView(view: TextView, date: String) {
        val activity = activity ?: return
        activity.viewModel.isPaydayDetailSet = true
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

    private fun showMonthYearPicker() {
        val context = this.context ?: return

        val listener = MonthPickerDialog.OnDateSetListener { selectedMonth, selectedYear ->
            val selectedDate = DateTime.date(1, selectedMonth + 1, selectedYear, "MM/yy")
            setDateView(tvExpiry, selectedDate)
        }

        val c = Calendar.getInstance()
        val currentYear = c.get(Calendar.YEAR)
        val currentMonth = c.get(Calendar.MONTH)

        MonthPickerDialog.Builder(context, listener, currentYear, currentMonth)
            .setTitle(getString(R.string.select_card_expiry))
            .setActivatedMonth(currentMonth)
            .setActivatedYear(currentYear)
            .setYearRange(currentYear, currentYear + 25)
            .build()
            .show()
    }

    private fun validateEditText(): Boolean {
        if (etCardNumber.text == null || TextUtils.isEmpty(etCardNumber.text.toString().trim())) {
            textInputLayoutCardNo.setErrorMessage(getString(R.string.empty_card_no))
            etCardNumber.requestFocus()
            return false
        } else if (etCardNumber.text.toString().trim().replace(" ", "").length < 16 || etCardNumber.text.toString().trim().replace(" ", "").length > 16) {
            textInputLayoutCardNo.setErrorMessage(getString(R.string.invalid_card_no))
            etCardNumber.requestFocus()
            return false
        } else if (etCardName.text == null || TextUtils.isEmpty(etCardName.text.toString().trim())) {
            textInputLayoutCardName.setErrorMessage(getString(R.string.empty_card_name))
            etCardName.requestFocus()
            return false
        } else if (etCardName.text.toString().length < 2) {
            textInputLayoutCardName.setErrorMessage(getString(R.string.card_username_length))
            etCardName.requestFocus()
            return false
        } else if (tvExpiry.text == null || TextUtils.isEmpty(tvExpiry.text.toString().trim())) {
            unsetDateView(tvExpiry, true, getString(R.string.empty_expiry_date))
            return false
        } else if (tvExpiry.text == null
            || !tvExpiry.text.toString().trim().matches(DateTime.REGEX_MM_YY.toRegex())
            || !DateTime.isValid(tvExpiry.text.toString().trim(), "MM/yy")
            || !DateTime.isAfter(tvExpiry.text.toString(), "MM/yy")) {
            unsetDateView(tvExpiry, true, getString(R.string.invalid_expiry_date))
            return false
        } else {
            textInputLayoutCardNo.clearErrorMessage()
            textInputLayoutCardNo.clearErrorMessage()
            return true
        }
    }
}
