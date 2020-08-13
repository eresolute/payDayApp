package com.fh.payday.views2.intlRemittance.rateCalculator

import android.app.Dialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.bumptech.glide.Glide
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.models.Item
import com.fh.payday.datasource.models.intlRemittance.CountryCurrency
import com.fh.payday.datasource.models.intlRemittance.RateConversion
import com.fh.payday.datasource.models.login.User
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.*
import com.fh.payday.viewmodels.intlRemittance.RateCalculatorViewModel
import com.fh.payday.views2.intlRemittance.ExchangeAccessKey
import com.fh.payday.views2.intlRemittance.ExchangeContainer
import com.fh.payday.views2.intlRemittance.transfer.TransferActivity
import com.fh.payday.views2.shared.custom.InvalidCustomerDialogFragment
import com.fh.payday.views2.shared.custom.showCurrencies
import com.jakewharton.rxbinding2.widget.textChanges
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_rate_calculator.*
import kotlinx.android.synthetic.main.rate_calculator_layout.*
import kotlinx.android.synthetic.main.toolbar.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class RateCalculatorActivity : BaseActivity() {
    private lateinit var payoutCurrency: String
    private lateinit var payoutCountryCode: String
    private var dialog: Dialog? = null

    var bottomSheet: BottomSheetDialog? = null

    val viewModel by lazy { ViewModelProviders.of(this).get(RateCalculatorViewModel::class.java) }

    private val disposable = CompositeDisposable()
    private lateinit var user: User

    override fun getLayout(): Int = R.layout.activity_rate_calculator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val allowed = intent.getBooleanExtra("allowed", true)
        if (!allowed) {
            viewModel.selectedAccessKey = ExchangeAccessKey.FARD
            btn_confirm.visibility = View.GONE
        }
        getCurrency()
        attachFromTextChangeListener(et_sender_amount)
        attachFromTextChangeListener(et_receiver_amount)
        addObserver()
        et_receiver_amount
        getItems().forEach {
            tv_exchange_name.text = it.name
            Glide.with(this)
                    .load(it.res)
                    .into(findViewById(R.id.iv_logo))
        }
    }

    override fun init() {
        setUpToolbar()
        handleBottomBar()
        user = UserPreferences.instance.getUser(this) ?: return logout()
        setupListeners()

        et_sender_amount.requestFocus()
        tv_sender_currency.text = getString(R.string.aed)
        iv_sender_flag.setImageResource(R.drawable.ic_united_arab_emirates_round)
        iv_drop_down.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }

    private fun setupListeners() {

        val allowed = intent.getBooleanExtra("allowed", true)

        if (allowed) linear_layout.setOnClickListener { showBottomSheet() }

        btn_confirm.setOnClickListener {
            if (!isNetworkConnected()) {
                showNoInternetDialog()
                return@setOnClickListener
            }
            if (allowed) {
                val amountFrom = if (et_sender_amount.text.toString()
                                .isEmpty()
                ) "" else et_sender_amount.text.toString()
                val amountTo = if (et_receiver_amount.text.toString()
                                .isEmpty()
                ) "" else et_receiver_amount.text.toString()
                startActivity(Intent(this, TransferActivity::class.java).apply {
                    intent.extras?.let { putExtras(it) }
                    putExtra("payoutCcy", tv_receiver_currency.text)
                    putExtra("exchange", tv_exchange_name.text)
                    putExtra("amountFrom", amountFrom)
                    putExtra("amountTo", amountTo)
                    putExtra("access_key", viewModel.selectedAccessKey ?: return@setOnClickListener)
                    putExtra("action", this@RateCalculatorActivity::class.java.name)
                })
            } else {
                val fragment = InvalidCustomerDialogFragment()
                fragment.isCancelable = false
                fragment.arguments = Bundle().also {
                    it.putString("message", getString(R.string.not_valid_emirates))
                }
                fragment.show(supportFragmentManager, "EmiratesExpire")
            }
        }
        tv_receiver_currency.setOnClickListener { showCurrencyBottomSheet() }
        iv_receiver_flag.setOnClickListener { showCurrencyBottomSheet() }
        iv_drop_down.setOnClickListener { showCurrencyBottomSheet() }

        et_sender_amount.onTextChanged { s, _, _, count ->
            if (et_sender_amount.isFocused && count < 1) {
                return@onTextChanged clearPayOutInput()
            }
            when {
                s.toString() == "." -> {
                    et_sender_amount.setText("0.")
                    et_sender_amount.setSelection(et_sender_amount.text.toString().length)
                }
                s.toString() == "00" -> {
                    et_sender_amount.setText("0")
                    et_sender_amount.setSelection(et_sender_amount.text.toString().length)
                }
            }
        }

        et_receiver_amount.onTextChanged { s, _, _, count ->
            if (et_receiver_amount.isFocused && count < 1) {
                return@onTextChanged clearPayInInput()
            }

            when {
                s.toString() == "." -> {
                    et_receiver_amount.setText("0.")
                    et_receiver_amount.setSelection(et_receiver_amount.text.toString().length)
                }
                s.toString() == "00" -> {
                    et_receiver_amount.setText("0")
                    et_receiver_amount.setSelection(et_receiver_amount.text.toString().length)
                }
            }
        }
    }

    private fun showNoInternetDialog() {
        onFailure(findViewById(R.id.layout), getString(R.string.no_internet_connectivity))
    }

    private fun getConversionRate(etFrom: EditText, etTo: EditText) {
        payoutCountryCode = viewModel.payoutCountryCode ?: return
        payoutCurrency = viewModel.payoutCurrency ?: return
        viewModel.etTo = etTo.id
        viewModel.etFrom = etFrom.id
        viewModel.payInCurrency = "AED"
        val accessKey = viewModel.selectedAccessKey ?: return

        val payInCurrency = "AED"
        etFrom.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter2(5, 2))
        etTo.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter3(5, 2))

        val amount =
                if (etFrom.text.toString().isEmpty()) "0" else etFrom.text.toString().replace(",", "")
        if (et_receiver_amount.isFocused) {
            return viewModel.getConversionRate(
                    user, "", amount,
                    payoutCurrency, payoutCountryCode, payInCurrency, accessKey
            )
        }

        viewModel.getConversionRate(
                user, amount,
                "", payoutCurrency, payoutCountryCode, payInCurrency, accessKey
        )
    }

    private fun clearPayInInput() {
        et_sender_amount.setText("")
        et_sender_amount.isEnabled = true
        progress_bar_sender.visibility = View.GONE

        viewModel.cancelRequest()
    }

    private fun clearPayOutInput() {
        et_receiver_amount.setText("")
        et_receiver_amount.isEnabled = true
        progress_bar_receiver.visibility = View.GONE

        viewModel.cancelRequest()
    }

    val listener: OnItemClickListener = object : OnItemClickListener {
        override fun onItemClick(index: Int) {
            tv_exchange_name.text = getItems()[index].name
            iv_logo.setImageResource(getItems()[index].res)
            viewModel.selectedAccessKey = ExchangeContainer.exchanges()[index].accessKey
            et_receiver_amount.text?.clear()
            et_sender_amount.text?.clear()
            bottomSheet?.dismiss()
        }
    }

    private val currencyListener: OnItemClickListener = object : OnItemClickListener {
        override fun onItemClick(index: Int) {
            val dialog = dialog ?: return
            dialog.dismiss()
            tv_receiver_currency.text = viewModel.countries[index].currency
            loadImages(viewModel.countries[index].imagePath)
            viewModel.payoutCountryCode = viewModel.countries[index].countryCode
            viewModel.payoutCurrency = viewModel.countries[index].currency

            val etFrom: EditText
            val etTo: EditText
            if (et_sender_amount.isFocused) {
                etFrom = et_sender_amount
                etTo = et_receiver_amount
            } else {
                etFrom = et_receiver_amount
                etTo = et_sender_amount
            }
            getConversionRate(etFrom, etTo)
            bottomSheet?.dismiss()
        }
    }

    private fun loadImages(imagePath: String) {
        GlideApp.with(iv_receiver_flag)
                .load(Uri.parse("$BASE_URL/$imagePath"))
                .placeholder(R.drawable.ic_flag_placeholder)
                .error(R.drawable.ic_flag_placeholder)
                .into(iv_receiver_flag)
    }

    private fun showBottomSheet() {
        val array = getItems()
        val view = LayoutInflater.from(this)
                .inflate(R.layout.layout_select_exchange, findViewById(android.R.id.content), false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)

        recyclerView.adapter = SelectExchangeAdapter(array, listener)
        recyclerView.layoutManager = LinearLayoutManager(this)

        bottomSheet = BottomSheetDialog(this)
        bottomSheet?.setContentView(view)
        (view.parent as View).setBackgroundColor(ContextCompat.getColor(this, R.color.transparent))
        bottomSheet?.show()

        bottomSheet?.setOnDismissListener {
            bottomSheet = BottomSheetDialog(this)
            bottomSheet?.dismiss()
        }

        bottomSheet?.setOnCancelListener {
            bottomSheet = BottomSheetDialog(this)
            bottomSheet?.dismiss()
        }
    }

    private fun showCurrencyBottomSheet() {
        val array = ArrayList(viewModel.countries)
        dialog = showCurrencies(this, array, currencyListener)
    }

    private fun addObserver() {

        viewModel.currencyResponse.observe(this, Observer {
            it ?: return@Observer

            val rateConversionState = it.getContentIfNotHandled() ?: return@Observer

            if (rateConversionState is NetworkState2.Loading<RateConversion>) {
                if (et_receiver_amount.isFocused) {
                    progress_bar_sender.visibility = View.VISIBLE
                    et_sender_amount.text = null
                    et_sender_amount.isEnabled = false
                } else {
                    progress_bar_receiver.visibility = View.VISIBLE
                    et_receiver_amount.text = null
                    et_receiver_amount.isEnabled = false
                }
                return@Observer
            }

            if (et_receiver_amount.isFocused) {
                progress_bar_sender.visibility = View.GONE
                et_sender_amount.isEnabled = true
            } else {
                progress_bar_receiver.visibility = View.GONE
                et_receiver_amount.isEnabled = true
            }

            when (rateConversionState) {
                is NetworkState2.Success -> {
                    val data = rateConversionState.data ?: return@Observer

                    if (et_receiver_amount.isFocused) {
                        val text =
                                if (viewModel.isValidAmount(data.payInAmount)) data.payInAmount.getDecimalValue() else ""
                        findViewById<EditText>(R.id.et_sender_amount).setText(text)
                    } else {
                        val text =
                                if (viewModel.isValidAmount(data.payOutAmount)) data.payOutAmount.getDecimalValue() else ""
                        findViewById<EditText>(R.id.et_receiver_amount).setText(text)
                    }

                    val format =
                            DecimalFormat("#,###.####", DecimalFormatSymbols.getInstance(Locale("en")))

                    tv_exchange_rate.text = String.format(
                            getString(R.string.exchange_rate_label), "1", viewModel.payInCurrency,
                            format.format(1 / data.exchangeRate.toDouble()), payoutCurrency
                    )
                    return@Observer
                }

                is NetworkState2.Error -> {
                    if (rateConversionState.isSessionExpired) {
                        onSessionExpired(rateConversionState.message)
                        return@Observer
                    }
                    val (message) = rateConversionState
                    handleErrorCode(rateConversionState.errorCode.toInt(), message)
                }

                is NetworkState2.Failure -> {
                    onFailure(findViewById(R.id.layout), rateConversionState.throwable)
                }
            }
        })
    }

    private fun getItems(): List<Item> {

        val textItems = resources.getStringArray(R.array.intl_locator_option)
        val icons = resources.obtainTypedArray(R.array.money_exchange_logo)
        val items = ArrayList<Item>()

        val exchanges = ExchangeContainer.exchanges()
        exchanges.forEach {
            if (ExchangeAccessKey.UAEX.equals(it.accessKey, true)) {
                items.add(Item(textItems[0], icons.getResourceId(0, 0)))
                viewModel.selectedAccessKey = it.accessKey
            } else if (ExchangeAccessKey.FARD.equals(it.accessKey, true)) {
                items.add(Item(textItems[1], icons.getResourceId(1, 0)))
                viewModel.selectedAccessKey = it.accessKey
            }
        }

        icons.recycle()
        return items
    }

    private fun getCurrency() {

        val user = UserPreferences.instance.getUser(this) ?: return
        addCountriesObserver()
        enableFields(false)
        viewModel.getCountries(
                user.token,
                user.sessionId,
                user.refreshToken,
                user.customerId.toString()
        )
    }

    private fun addCountriesObserver() {
        viewModel.countryResponse.observe(this, Observer {
            it ?: return@Observer

            val countryState = it.getContentIfNotHandled() ?: return@Observer

            if (countryState is NetworkState2.Loading<List<CountryCurrency>>) {
                //progressCurrency.visibility = View.VISIBLE
                tv_receiver_currency.visibility = View.INVISIBLE
                return@Observer
            }

            //progressCurrency.visibility = View.GONE
            tv_receiver_currency.visibility = View.VISIBLE

            when (countryState) {
                is NetworkState2.Success -> {
                    val data = countryState.data ?: return@Observer
                    enableFields(true)

                    viewModel.payoutCountryCode = data[0].countryCode
                    viewModel.payoutCurrency = data[0].currency
                    loadImages(data[0].imagePath)
                    tv_receiver_currency.text = data[0].currency
                    getConversionRate(et_sender_amount, et_receiver_amount)
                    return@Observer
                }

                is NetworkState2.Error -> {
                    if (countryState.isSessionExpired) {
                        onSessionExpired(countryState.message)
                        return@Observer
                    }
                    val (message) = countryState
                    handleErrorCode(countryState.errorCode.toInt(), message)
                }

                is NetworkState2.Failure -> {
                    onFailure(findViewById(R.id.layout), countryState.throwable)
                }
            }


        })
    }

    private fun enableFields(b: Boolean) {
        tv_receiver_currency.isEnabled = b
        et_sender_amount.isEnabled = b
        et_receiver_amount.isEnabled = b
        btn_confirm.isEnabled = b
    }

    private fun setUpToolbar() {
        toolbar_back.setOnClickListener(this)
        toolbar_title.text = getString(R.string.rate_calculator)
    }

    private fun handleBottomBar() {
        findViewById<TextView>(R.id.btm_home).setOnClickListener(this)
        findViewById<TextView>(R.id.btm_menu_branch).setOnClickListener(this)
        findViewById<TextView>(R.id.btm_menu_support).setOnClickListener(this)
        findViewById<TextView>(R.id.btm_menu_loan_calc).setOnClickListener(this)
        findViewById<TextView>(R.id.toolbar_help).setOnClickListener(this)
    }

    private fun attachFromTextChangeListener(editText: EditText) {
        val d = editText.textChanges()
                .debounce(500, TimeUnit.MILLISECONDS)
                .map { it.replace(",".toRegex(), "") }
                .filter { it.isNotEmpty() && viewModel.isValidAmount(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    when (editText) {
                        et_sender_amount -> {
                            if (et_sender_amount.isFocused) {
                                getConversionRate(et_sender_amount, et_receiver_amount)
                            }
                        }
                        et_receiver_amount -> {
                            if (et_receiver_amount.isFocused) {
                                getConversionRate(et_receiver_amount, et_sender_amount)
                            }
                        }
                    }
                }
        disposable.add(d)
    }
}
