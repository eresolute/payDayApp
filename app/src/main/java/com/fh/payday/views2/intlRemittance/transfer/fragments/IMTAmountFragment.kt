package com.fh.payday.views2.intlRemittance.transfer.fragments


import android.arch.lifecycle.Observer
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import com.fh.payday.BaseFragment
import com.fh.payday.R
import com.fh.payday.datasource.models.intlRemittance.RateConversion
import com.fh.payday.datasource.models.login.User
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.*
import com.fh.payday.utilities.Validator.isValidAmount
import com.fh.payday.viewmodels.intlRemittance.TransferViewmodel
import com.fh.payday.views.adapter.SelectLanguageAdapter
import com.fh.payday.views2.intlRemittance.transfer.TransferActivity
import com.fh.payday.views2.intlRemittance.transfer.adapter.CardsAdapter2
import com.jakewharton.rxbinding2.widget.textChanges
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_imt_amount.*
import kotlinx.android.synthetic.main.rate_calculator_layout.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class IMTAmountFragment : BaseFragment() {

    private val viewModel: TransferViewmodel? by lazy {
        when (val activity = activity) {
            is TransferActivity -> activity.viewmodel
            else -> null
        }
    }


    private val cardSheetItemClickListener: OnItemClickListener = object : OnItemClickListener {
        override fun onItemClick(index: Int) {
            setCardDetails(index)
            bottomSheet?.dismiss()
        }
    }

    private fun setCardDetails(index: Int) {
        val viewModel = viewModel ?: return
        val card = viewModel.cards ?: return
        viewModel.selectedCard = card[index]
        tv_card_status.text = maskCardNumber(card[index].cardNumber)
        card_balance.text = String.format(getString(R.string.amount_in_aed, card[index].availableBalance.getDecimalValue()))
    }

    private var isViewShown: Boolean = false
    private var user: User? = null
    var bottomSheet: BottomSheetDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = UserPreferences.instance.getUser(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_imt_amount, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!isViewShown) {
            initViews()
            addObserver()
            attachInputTextChangeListeners()
            attachConversionObserver()
            attachTextChangeListener(et_sender_amount)
            attachTextChangeListener(et_receiver_amount)
            attachSummaryObserver()
        }

        et_sender_amount.nextFocusForwardId = et_promo_code.id
        et_receiver_amount.imeOptions = EditorInfo.IME_ACTION_NEXT
        et_receiver_amount.nextFocusForwardId = et_promo_code.id

        et_sender_amount.isEnabled = true
        et_receiver_amount.isEnabled = true

        tv_purpose.setOnClickListener { showBottomSheet() }
        btn_next.setOnClickListener { onSubmit() }
        card_layout.setOnClickListener { showCardBottomSheet() }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if (isVisibleToUser) {
            if (view != null) {
                if (viewModel?.toClear == true) {
                    clearFields()
                }

                btn_next.isEnabled = true
                isViewShown = true
                initViews()
                addObserver()
                attachInputTextChangeListeners()
                attachConversionObserver()
                attachTextChangeListener(et_sender_amount)
                attachTextChangeListener(et_receiver_amount)
                attachSummaryObserver()
            } else {
                isViewShown = false
            }
        }
    }

    private fun initViews() {
        val user = user ?: return
        viewModel?.purposeOfPayment(user.token, user.sessionId, user.refreshToken, user.customerId.toLong(), viewModel?.selectedAccessKey
                ?: return,
                viewModel?.selectedBeneficiary?.recieverRefNumber ?: return)

        tv_sender_currency.text = viewModel?.payInCurrency ?: ""
        tv_receiver_currency.text = viewModel?.payoutCurrency ?: ""

        if (viewModel?.amountFrom != null) {
            et_sender_amount.setText(viewModel?.amountFrom ?: "")
        }
        et_sender_amount.requestFocus()

        et_sender_amount.filters = arrayOf(DecimalDigitsInputFilter2(5, 2))
        et_receiver_amount.filters = arrayOf(DecimalDigitsInputFilter2(5, 2))

        fetchRates(payInAmount = "0.0")
        fetchCustomer()
        tv_exchange_rate.text = getString(R.string.exchange_rate_label)
                .format("0.00", viewModel?.payInCurrency, "0.00", viewModel?.payoutCurrency)

        btn_next.isEnabled = true
        //setFlags()
    }

    private fun setFlags(rateConversion: RateConversion) {
        GlideApp.with(iv_sender_flag)
                .load(R.drawable.ic_united_arab_emirates_round)
                .placeholder(R.drawable.ic_flag_placeholder)
                .error(R.drawable.ic_flag_placeholder)
                .into(iv_sender_flag)

        val receiverFlagPath = rateConversion.payOutFlag
        if (!receiverFlagPath.isNullOrEmpty()) {
            GlideApp.with(iv_receiver_flag)
                    .load(BASE_URL + receiverFlagPath)
                    .placeholder(R.drawable.ic_flag_placeholder)
                    .error(R.drawable.ic_flag_placeholder)
                    .into(iv_receiver_flag)
        }
    }

    private fun onSubmit() {
        if (validate()) {
            viewModel?.payInCurrency = "AED"
            val beneficiary = viewModel?.selectedBeneficiary ?: return
            val rateConversion = viewModel?.rateResponse ?: return
            viewModel?.payoutCountryCode = beneficiary.receiverCountryCode
            viewModel?.payoutCurrency = beneficiary.payoutCcyCode

            viewModel?.payInAmount = et_sender_amount.text.toString()
            viewModel?.payOutAmount = et_receiver_amount.text.toString()
            viewModel?.totalAmount = rateConversion.totalPayableAmount
            viewModel?.transferPurpose = tv_purpose.text.toString()
            viewModel?.promoCode = et_promo_code.text?.toString() ?: ""

            when (val activity = activity) {
                is TransferActivity? -> {
                    viewModel?.isChecked = false
                    btn_next.isEnabled = false
                    activity?.navigateUp()
                }
            }
        }
    }

    private fun attachInputTextChangeListeners() {
        et_sender_amount.onTextChanged { s, _, _, count ->

            tv_sender_amount_error.visibility = View.GONE

            if (et_sender_amount.isFocused && count < 1) {
                return@onTextChanged clearReceiverInput()
            }

            if (s.toString() == ".") {
                et_sender_amount.setText("0.")
                et_sender_amount.setSelection(count)
            } else if (s.toString() == "00") {
                et_sender_amount.setText("0")
                et_sender_amount.setSelection(count)
            }
        }

        et_receiver_amount.onTextChanged { s, _, _, count ->

            tv_receiver_amount_error.visibility = View.GONE

            if (et_receiver_amount.isFocused && count < 1) {
                return@onTextChanged clearSenderInput()
            }

            if (s.toString() == ".") {
                et_receiver_amount.setText("0.")
            } else if (s.toString() == "00") {
                et_receiver_amount.setText("0")
            }
        }

        et_promo_code.onTextChanged { _, _, _, count ->
            til_promo_code.clearErrorMessage()

            when {
                count < 1 -> setDrawableEnd(et_promo_code, 0)
                isValidPromoCode(et_promo_code.text.toString()) -> {
                    setDrawableEnd(et_promo_code, R.drawable.ic_success_tick)
                }
                else -> setDrawableEnd(et_promo_code, R.drawable.ic_error_cross)
            }
        }
    }

    private fun showBottomSheet() {
        val activity = activity ?: return
        val view = LayoutInflater.from(activity).inflate(R.layout.layout_select_exchange,
                activity.findViewById(android.R.id.content), false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)

        val purposes = viewModel?.initializeTransaction?.transactionPurposes ?: return
        val purposesArray = purposes.map { it.name }.toTypedArray()

        recyclerView.adapter = SelectLanguageAdapter(purposesArray, object : OnItemClickListener {
            override fun onItemClick(index: Int) {
                tv_purpose.text = purposes[index].name
                tv_purpose.background =  ContextCompat.getDrawable(context ?: return,
                        R.drawable.bg_grey_blue_border)
                viewModel?.selectedPurposePaymentCode = purposes[index].code
                bottomSheet?.dismiss()
            }
        })
        recyclerView.layoutManager = LinearLayoutManager(activity)

        bottomSheet = BottomSheetDialog(activity)
        bottomSheet?.setContentView(view)
        (view.parent as View).setBackgroundColor(ContextCompat.getColor(activity, R.color.transparent))
        bottomSheet?.show()

        bottomSheet?.setOnDismissListener {
            bottomSheet = BottomSheetDialog(activity)
            bottomSheet?.dismiss()
        }

        bottomSheet?.setOnCancelListener {
            bottomSheet = BottomSheetDialog(activity)
            bottomSheet?.dismiss()
        }
    }

    private fun showCardBottomSheet() {
        val activity = activity ?: return
        val array = viewModel?.cards ?: return
        val view = LayoutInflater.from(activity).inflate(R.layout.layout_select_exchange,
                activity.findViewById(android.R.id.content), false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)

        recyclerView.adapter = CardsAdapter2(cardSheetItemClickListener).apply {
            addAll(array)
        }
        recyclerView.layoutManager = LinearLayoutManager(activity)

        bottomSheet = BottomSheetDialog(activity)
        bottomSheet?.setContentView(view)
        (view.parent as View).setBackgroundColor(ContextCompat.getColor(activity, R.color.transparent))
        bottomSheet?.show()

        bottomSheet?.setOnDismissListener {
            bottomSheet = BottomSheetDialog(activity)
            bottomSheet?.dismiss()
        }

        bottomSheet?.setOnCancelListener {
            bottomSheet = BottomSheetDialog(activity)
            bottomSheet?.dismiss()
        }
    }

//    private fun getPurposes(): Array<String> = resources.getStringArray(R.array.imt_transfer_purpose)

    private fun showProgress(progressBar: ProgressBar) {
        progressBar.visibility = View.VISIBLE
        val convertToView = when (progressBar.id) {
            progress_bar_sender.id -> et_sender_amount
            else -> et_receiver_amount
        }
        disableViews(btn_next, tv_purpose, convertToView)
    }

    private fun hideProgress(progressBar: ProgressBar) {
        val convertToView = when (progressBar.id) {
            progress_bar_sender.id -> et_sender_amount
            else -> et_receiver_amount
        }
        enableViews(btn_next, tv_purpose, et_promo_code, convertToView)
        progressBar.visibility = View.GONE
    }

    private fun enableViews(vararg views: View) {
        views.forEach { it.isEnabled = true }
    }

    private fun disableViews(vararg views: View) {
        views.forEach { it.isEnabled = false }
    }

    private fun fetchRates(
            payInAmount: String? = null,
            payOutAmount: String? = null
    ) {
        if (payInAmount == null && payOutAmount == null) {
            throw NullPointerException("payInAmount and payOutAmount are null")
        }

        val user = user ?: return
        val payoutCountryCode = viewModel?.payoutCountryCode ?: return
        val payoutCurrency = viewModel?.payoutCurrency ?: return
        val accessKey = viewModel?.selectedAccessKey ?: return

        viewModel?.getCurrencyConversion(user.token, user.sessionId, user.refreshToken, user.customerId.toString(),
                payInAmount ?: "", payOutAmount ?: "", payoutCurrency, payoutCountryCode, accessKey)

        if (et_sender_amount.isFocused) {
            et_sender_amount.filters = arrayOf(DecimalDigitsInputFilter2(5, 2))
            et_receiver_amount.filters = arrayOf(DecimalDigitsInputFilter3(5, 2))
        } else if (et_receiver_amount.isFocused) {
            et_receiver_amount.filters = arrayOf(DecimalDigitsInputFilter2(5, 2))
            et_sender_amount.filters = arrayOf(DecimalDigitsInputFilter3(5, 2))
        }
    }

    private fun fetchCustomer() {
        val user = user ?: return
        viewModel?.fetchCustomerSummary(user.token, user.sessionId, user.refreshToken, user.customerId.toLong())
    }

    private fun attachSummaryObserver() {
        viewModel?.customerSummaryState?.observe(viewLifecycleOwner, Observer {
            val activity = activity ?: return@Observer
            it ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                return@Observer
            }

            when (state) {
                is NetworkState2.Success -> {
                    val data = state.data ?: return@Observer
                    if (viewModel?.cards == null)
                        viewModel?.cards = ArrayList(data.cards)
                    else {
                        viewModel?.cards?.clear()
                        viewModel?.cards = ArrayList(data.cards)
                    }
                    setCardDetails(0)
                    viewModel?.selectedCard = data.cards[0]
//                    (recycler_view.adapter as? CardsAdapter2)?.addAll(data.cards)
                }
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        activity.onSessionExpired(state.message)
                        return@Observer
                    }
                    val (message) = state
                    activity.handleErrorCode(state.errorCode.toInt(), message)
                }
                is NetworkState2.Failure -> activity.onFailure(activity.findViewById(R.id.layout), getString(R.string.request_error))
                else -> activity.onFailure(activity.findViewById(R.id.layout), getString(R.string.request_error))
            }
        })
    }

    private fun attachConversionObserver() {
        viewModel?.senderCurrencyResponse?.observe(viewLifecycleOwner, Observer {
            it ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                showProgress(progress_bar_receiver)
                return@Observer
            }

            hideProgress(progress_bar_receiver)

            handleState(state, et_receiver_amount)
        })

        viewModel?.receiverCurrencyResponse?.observe(viewLifecycleOwner, Observer {
            it ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                showProgress(progress_bar_sender)
                return@Observer
            }

            hideProgress(progress_bar_sender)

            handleState(state, et_sender_amount)
        })
    }

    private fun addObserver() {
        viewModel?.purposeOfPaymentState?.observe(viewLifecycleOwner, Observer {
            val activity = activity ?: return@Observer
            it ?: return@Observer

            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                tv_purpose.isEnabled = false
                return@Observer
            }
            progress_bar.visibility = View.GONE
            when (state) {
                is NetworkState2.Success -> {
                    state.data ?: return@Observer
                    tv_purpose.isEnabled = true
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        tv_purpose.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down_blue, 0)
                    } else
                        tv_purpose.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down_blue, 0)
                }
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        activity.onSessionExpired(state.message)
                        return@Observer
                    }
                    val (message) = state
                    activity.handleErrorCode(state.errorCode.toInt(), message)
                }
                is NetworkState2.Failure -> activity.onFailure(activity.findViewById(R.id.layout), getString(R.string.request_error))
                else -> activity.onFailure(activity.findViewById(R.id.layout), getString(R.string.request_error))
            }
        })
    }

    private fun handleState(state: NetworkState2<RateConversion>, editText: EditText) {
        val activity = activity ?: return
        when (state) {
            is NetworkState2.Success -> onSuccess(state.data, editText)
            is NetworkState2.Error -> {
                if (state.isSessionExpired) {
                    activity.onSessionExpired(state.message)
                    return
                }
                val (message) = state
                activity.handleErrorCode(state.errorCode.toInt(), message)
            }
            is NetworkState2.Failure -> activity.onFailure(activity.findViewById(R.id.layout), getString(R.string.request_error))
            else -> activity.onFailure(activity.findViewById(R.id.layout), getString(R.string.request_error))
        }
    }

    private fun onSuccess(data: RateConversion?, editText: EditText) {
        data ?: return

        val payInAmount = try {
            if (data.payInAmount.toDouble() > 0) data.payInAmount.getDecimalValue() else ""
        } catch (e: NumberFormatException) {
            ""
        }
        val payOutAmount = try {
            if (data.payOutAmount.toDouble() > 0) data.payOutAmount.getDecimalValue() else ""
        } catch (e: NumberFormatException) {
            ""
        }

        when (editText) {
            et_sender_amount -> et_sender_amount.setText(payInAmount)
            et_receiver_amount -> et_receiver_amount.setText(payOutAmount)
        }

        val formatter = DecimalFormat("#,###.####", DecimalFormatSymbols.getInstance(Locale("en")))

        tv_exchange_rate.text = String.format(getString(R.string.exchange_rate_label), "1", data.payInCurrency,
                formatter.format(1 / data.exchangeRate.toDouble()), data.payOutCurrency)

        tv_exchange_rate.visibility = View.VISIBLE
        setFlags(data)
    }

    private fun attachTextChangeListener(e: EditText) {
        val d = e.textChanges()
                .debounce(500, TimeUnit.MILLISECONDS)
                .map { it.replace(",".toRegex(), "") }
                .filter { it.isNotEmpty() && isValidAmount(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    when (e) {
                        et_sender_amount -> {
                            val amount = et_sender_amount.text?.toString()
                            if (et_sender_amount.isFocused && !amount.isNullOrEmpty()) {
                                fetchRates(payInAmount = amount)
                            }
                        }
                        et_receiver_amount -> {
                            val amount = et_receiver_amount.text?.toString()
                            if (et_receiver_amount.isFocused && !amount.isNullOrEmpty()) {
                                fetchRates(payOutAmount = amount)
                            }
                        }
                    }
                }

        when (val activity = activity) {
            is TransferActivity? -> activity?.disposable?.add(d)
        }
    }

    private fun clearSenderInput() {
        et_sender_amount.setText("")
        et_sender_amount.isEnabled = true
        progress_bar_sender.visibility = View.GONE

        viewModel?.cancelRequest()
    }

    private fun clearReceiverInput() {
        et_receiver_amount.setText("")
        et_receiver_amount.isEnabled = true
        progress_bar_receiver.visibility = View.GONE
        viewModel?.cancelRequest()
    }

    private fun clearFields() {
        tv_purpose.text = ""
        et_sender_amount.setText("")
        et_receiver_amount.setText("")
        et_promo_code.setText("")
    }

    private fun validate(): Boolean {

        when {
            et_sender_amount.text.isNullOrEmpty() || et_sender_amount.text?.toString()
                    ?.replace(",", "")?.toDouble() == 0.0 -> {
                tv_sender_amount_error.visibility = View.VISIBLE
                return false
            }
            et_receiver_amount.text.isNullOrEmpty() || et_receiver_amount.text?.toString()
                    ?.replace(",", "")?.toDouble() == 0.0 -> {
                tv_receiver_amount_error.visibility = View.VISIBLE
                return false
            }
            tv_purpose.text.isNullOrEmpty() ->{
                setTextViewError(getString(R.string.select_prupose_of_payment), tv_purpose)
                return false
            }
            et_promo_code.text.isNullOrEmpty() -> {
                return true
            }

            !isValidPromoCode(et_promo_code.text?.toString()) -> {
                til_promo_code.setErrorMessage(getString(R.string.invalid_promo_code))
                return false
            }

        }
        return true
    }

    private fun setTextViewError(message: String, textView: TextView) {
        textView.background = ContextCompat.getDrawable(this.context ?: return,
                R.drawable.bg_grey_red_border)
        activity?.onFailure(activity?.findViewById(R.id.root_view) ?: return, message)
    }
}




//        if (et_sender_amount.text.isNullOrEmpty() || et_sender_amount.text?.toString()
//                        ?.replace(",", "")?.toDouble() == 0.0) {
//            tv_sender_amount_error.visibility = View.VISIBLE
//            return false
//        } else if (et_receiver_amount.text.isNullOrEmpty() || et_receiver_amount.text?.toString()
//                        ?.replace(",", "")?.toDouble() == 0.0) {
//            tv_receiver_amount_error.visibility = View.VISIBLE
//            return false
//        } else if (et_promo_code.text.isNullOrEmpty()) {
//            return true
//        } else if (!isValidPromoCode(et_promo_code.text?.toString())) {
//            til_promo_code.setErrorMessage(getString(R.string.invalid_promo_code))
//            return false
//        }else
//
//        return true

