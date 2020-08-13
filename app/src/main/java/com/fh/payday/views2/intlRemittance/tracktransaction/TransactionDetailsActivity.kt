package com.fh.payday.views2.intlRemittance.tracktransaction

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.fh.payday.BaseActivity
import com.fh.payday.R
import com.fh.payday.datasource.models.Card
import com.fh.payday.datasource.models.intlRemittance.IntlTransaction
import com.fh.payday.datasource.models.intlRemittance.TransactionDetail
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.BASE_URL
import com.fh.payday.utilities.DateTime
import com.fh.payday.utilities.getDecimalValue
import com.fh.payday.utilities.maskCardNumber
import com.fh.payday.viewmodels.intlRemittance.TransactionDetailViewModel
import com.fh.payday.views2.dashboard.MainActivity
import com.fh.payday.views2.help.HelpActivity
import com.fh.payday.views2.intlRemittance.ExchangeAccessKey
import com.fh.payday.views2.intlRemittance.ExchangeContainer
import com.fh.payday.views2.intlRemittance.getTransPurpose
import com.fh.payday.views2.intlRemittance.transfer.adapter.SummaryAdapter
import com.fh.payday.views2.intlRemittance.transfer.adapter.SummaryUI
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity
import com.fh.payday.views2.locator.LocatorActivity
import com.fh.payday.views2.message.ContactUsActivity
import kotlinx.android.synthetic.main.activity_transaction_details.*
import kotlinx.android.synthetic.main.rate_calculator_layout.*
import kotlinx.android.synthetic.main.toolbar.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class TransactionDetailsActivity : BaseActivity() {

    lateinit var viewmodel: TransactionDetailViewModel
    override fun getLayout(): Int = R.layout.activity_transaction_details

    override fun init() {
        setUpToolbar()
        handleBottomBar()
        viewmodel = ViewModelProviders.of(this).get(TransactionDetailViewModel::class.java)

        et_sender_amount.isEnabled = false
        et_sender_amount.isFocusable = false
        et_receiver_amount.isEnabled = false
        et_receiver_amount.isFocusable = false
        val transaction = intent.getParcelableExtra<IntlTransaction>("transaction") ?: return
        val user = UserPreferences.instance.getUser(this) ?: return
        addObserver()
        viewmodel.getTransactionDetail(user.token, user.sessionId, user.refreshToken, user.customerId.toString(),
                transaction.accessKey,transaction.dealerTxnId, transaction.partnerTxnRefNo)
    }

    private fun setUpToolbar() {
        toolbar_title.setText(R.string.transaction_details)
        toolbar_back.setOnClickListener(this)
    }

    private fun addObserver() {
        viewmodel.transactionDetailResponse.observe(this, Observer {
            it ?: return@Observer

            val transactionResponse = it.getContentIfNotHandled() ?: return@Observer

            if (transactionResponse is NetworkState2.Loading) {
                scroll_view.visibility = View.GONE
                progress_bar_layout.visibility = View.VISIBLE
                return@Observer
            }

            progress_bar_layout.visibility = View.GONE

            when (transactionResponse) {
                is NetworkState2.Success -> {
                    scroll_view.visibility = View.VISIBLE
                    val data = transactionResponse.data ?: return@Observer
                    setDataViews(data)
                    return@Observer
                }

                is NetworkState2.Error -> {
                    if (transactionResponse.isSessionExpired) {
                        onSessionExpired(transactionResponse.message)
                        return@Observer
                    }

                    val (message) = transactionResponse
                    handleErrorCode(transactionResponse.errorCode.toInt(), message)
                }
                is NetworkState2.Failure -> {
                    onFailure(findViewById(R.id.layout), transactionResponse.throwable)
                }
            }
        })
    }

    private fun setDataViews(data: TransactionDetail) {
        if (data.transactionStatusDesc.equals("Failure", ignoreCase = true)) {
            setFailureView(data)
            return
        }
        et_sender_amount.setText(data.payinAmount.getDecimalValue())
        et_receiver_amount.setText(data.payoutAmount.getDecimalValue())
        tv_sender_currency.text = data.payinCcyCode
        tv_title.text = data.paymentMode
        tv_receiver_currency.text = data.payoutCcyCode

        Glide.with(this)
            .load(Uri.parse("$BASE_URL/${data.receiverFlag}"))
            .into(iv_receiver_flag)
        Glide.with(this)
            .load(R.drawable.ic_united_arab_emirates_round)
            .into(iv_sender_flag)

        recycler_view.adapter = SummaryAdapter {
        }

        val formatter = DecimalFormat("#,###.####", DecimalFormatSymbols.getInstance(Locale("en")))
        try {
            tv_exchange_rate.text = String.format(getString(R.string.exchange_rate_label), "1", data.payinCcyCode,
                formatter.format(1 / data.xchgRatePayin2Payout.toDouble()), data.payoutCcyCode)
        } catch (e: NumberFormatException) {
            tv_exchange_rate.text = ""
        }

        val card = intent?.getParcelableExtra<Card>("card") ?: return

        val fromSummary = SummaryUI(getString(R.string.from),
            maskCardNumber(card.cardNumber),
            getString(R.string.available_balance_in_aed).format(card.availableBalance.getDecimalValue()))

        val toSummary = SummaryUI(getString(R.string.to),
            "${data.receiverFirstName} ${data.receiverLastName}",
            data.receiverBankName,
            data.receiverBankAccountNo)

        (recycler_view.adapter as? SummaryAdapter?)?.apply {
            add(0, toSummary)
            add(0, fromSummary)
        }

        val status = SummaryUI(getString(R.string.status),
            "", "", "", false, data.transactionStatusDesc)
        val transaction = intent.getParcelableExtra<IntlTransaction>("transaction") ?: return
        val exchangeType = if (transaction.accessKey.equals(ExchangeAccessKey.FARD, true)) "Al Fardan" else "UAE Exchange"
        val exchangeLogo = if (transaction.accessKey.equals(ExchangeAccessKey.FARD, true)) R.mipmap.af_logo else R.mipmap.uae_x
        val exchange = SummaryUI(getString(R.string.through),
            "", "", "", false, "", exchangeType, exchangeLogo)

        val summaryList = arrayListOf<SummaryUI>()

        if (data.promoCode.isNotEmpty())
            summaryList.add(SummaryUI(getString(R.string.promo_code), data.promoCode))
        summaryList.add(SummaryUI(getString(R.string.reference_hash), data.partnerTxnRefNo))

        summaryList.add(SummaryUI(getString(R.string.date), DateTime.parse(data.transactionGMTDateTime,
            "yyyy-MM-dd'T'HH:mm:ss'Z'", "dd/MM/yyyy hh:mm a")))
        summaryList.add(exchange)
        summaryList.add(SummaryUI(getString(R.string.purpose), getTransPurpose(this, data.purposeOfTxn)))
        summaryList.add(status)
        summaryList.add(SummaryUI(getString(R.string.commission), getString(R.string.amount_in_aed).format(data.commission.getDecimalValue())))
        summaryList.add(SummaryUI(getString(R.string.vat), getString(R.string.amount_in_aed).format(data.tax.getDecimalValue())))
        val totalPayableAmount = getString(R.string.amount_in_aed)
            .format(data.totalPayInAmount.getDecimalValue().getDecimalValue())
        summaryList.add(SummaryUI(getString(R.string.total), totalPayableAmount))
        (recycler_view.adapter as? SummaryAdapter?)?.addAll(summaryList)

//        val cardNumber = intent?.getParcelableExtra<Card>("card")?.let {
//            maskCardNumber(it.cardNumber, "XXXX XXXX XXXX ####")x
//        } ?: ""
//        val receiverName = "${data.receiverFirstName} ${data.receiverLastName}"
//
//        tv_status.text = data.transactionStatusDesc
//        tv_exchange_house_ref_no.text = data.txnRefNo
//        tv_fh_ref_no.text = data.partnerTxnRefNo
//        tv_from_card_no.text = cardNumber
//        tv_to_name.text = receiverName
//        tv_from.text = data.senderName
//        tv_bank_name.text = "${data.receiverBankName}, ${data.receiverCountryCode}"
//        tv_to_card_no.text = data.receiverBankAccountNo
//        tv_recurring.text = getString(R.string.amount)
//        tv_from_email.text = data.emailId
//
//        tv_aed.text = data.payinCcyCode
//        tv_aed2.text = data.payoutCcyCode
//        tv_purpose.text = data.purposeOfTxn
//        tv_mRecurring_period.setText(R.string.date)
//        tv_recurring_period.text = DateTime.parse(data.transactionGMTDateTime, "yyyy-MM-dd'T'HH:mm:ss'Z'", "dd/MM/yyyy hh:mm a")
//        tv_mRecurring_frequency.visibility = View.GONE
//        tv_recurring_frequency.visibility = View.GONE
//        tv_mRecurrHoliday.visibility = View.GONE
//        tv_recurrHoliday.visibility = View.GONE
//        view8.visibility = View.VISIBLE
//        view9.visibility = View.GONE
//        view10.visibility = View.GONE
//
//        tv_promo_code.text = data.promoCode
////        tv_beneficiaryState.text = data.receiverState
////        tv_country_name.text = data.receiverBankCountryCode
//
//        tv_commission_amt.text = String.format(getString(R.string.amount_in_aed), data.commission.getDecimalValue())
//        tv_vat_amt.text = String.format(getString(R.string.amount_in_aed), data.tax.getDecimalValue())
//
//        tv_amount.text = data.payinAmount.getDecimalValue()
//        tv_amount2.text = data.payoutAmount.getDecimalValue()
//
//        tv_amount1.text = data.totalPayInAmount.getDecimalValue()
//        tv_amount3.text = data.payoutAmount.getDecimalValue()
//        tv_aed1.text = data.payinCcyCode
//        tv_aed3.text = data.payoutCcyCode
//
//        val formatter = DecimalFormat("#,###.####")
//
//        try {
//            tv_exchange_rate_label.text = String.format(getString(R.string.exchange_rate_label),
//                    "1", data.payoutCcyCode,formatter.format(data.xchgRatePayin2Payout.toDouble()), data.payinCcyCode)
//
//            tv_exchange_rate_label2.text = String.format(getString(R.string.exchange_rate_label), "1", data.payoutCcyCode,
//                     formatter.format(data.xchgRatePayin2Payout.toDouble()), data.payinCcyCode)
//        } catch (exception: NumberFormatException) {
//            tv_exchange_rate_label2.text = "N/A"
//            tv_exchange_rate_label.text = "N/A"
//        }


    }

    private fun setFailureView(data: TransactionDetail) {
        et_sender_amount.setText(data.payinAmount.getDecimalValue())
        et_receiver_amount.setText(getString(R.string.amount_zero))
        tv_sender_currency.text = data.payinCcyCode
        tv_receiver_currency.text = data.payoutCcyCode

        recycler_view.adapter = SummaryAdapter {
        }

        val card = intent?.getParcelableExtra<Card>("card") ?: return

        val fromSummary = SummaryUI(getString(R.string.from),
            maskCardNumber(card.cardNumber),
            getString(R.string.available_balance_in_aed).format(card.availableBalance.getDecimalValue()))

        val toSummary = SummaryUI(getString(R.string.to),
            "${data.receiverFirstName} ${data.receiverLastName}",
            data.receiverBankName,
            data.receiverBankAccountNo)

        (recycler_view.adapter as? SummaryAdapter?)?.apply {
            add(0, toSummary)
            add(0, fromSummary)
        }

        Glide.with(this)
            .load(Uri.parse("$BASE_URL/${data.receiverFlag}"))
            .into(iv_receiver_flag)
        Glide.with(this)
            .load(R.drawable.ic_united_arab_emirates_round)
            .into(iv_sender_flag)

        val status = SummaryUI(getString(R.string.status),
            "", "", "", false, data.transactionStatusDesc)

        val summaryList = arrayListOf<SummaryUI>()
        if (data.promoCode.isNotEmpty())
            summaryList.add(SummaryUI(getString(R.string.promo_code), data.promoCode))
        summaryList.add(SummaryUI(getString(R.string.fh_reference_no), data.partnerTxnRefNo))

        summaryList.add(SummaryUI(getString(R.string.date), DateTime.parse(data.transactionGMTDateTime,
            "yyyy-MM-dd'T'HH:mm:ss'Z'", "dd/MM/yyyy hh:mm a")))
        summaryList.add(SummaryUI(getString(R.string.purpose), getTransPurpose(this, data.purposeOfTxn)))
        summaryList.add(status)
        summaryList.add(SummaryUI(getString(R.string.commission), getString(R.string.amount_in_aed).format("0.00")))
        summaryList.add(SummaryUI(getString(R.string.vat), getString(R.string.amount_in_aed).format("0.00")))
        val totalPayableAmount = "0.00"
        summaryList.add(SummaryUI(getString(R.string.total), getString(R.string.amount_in_aed).format(totalPayableAmount)))
        (recycler_view.adapter as? SummaryAdapter?)?.addAll(summaryList)
    }

    private fun handleBottomBar() {

        findViewById<TextView>(R.id.btm_home).setOnClickListener { view ->
            val i = Intent(view.context, MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(i)
        }

        findViewById<TextView>(R.id.btm_menu_branch).setOnClickListener { view ->
            val i = Intent(view.context, LocatorActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(i)
        }

        findViewById<TextView>(R.id.btm_menu_support).setOnClickListener { v ->
            val i = Intent(v.context, ContactUsActivity::class.java)
            startActivity(i)
        }

        findViewById<TextView>(R.id.btm_menu_loan_calc).setOnClickListener { v ->
            val i = Intent(v.context, LoanCalculatorActivity::class.java)
            startActivity(i)
        }

        findViewById<TextView>(R.id.toolbar_help).setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java)
                .putExtra("anchor", "transactionHistoryHelp"))
        }
    }

}