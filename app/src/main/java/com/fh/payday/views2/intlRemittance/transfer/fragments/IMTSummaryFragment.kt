package com.fh.payday.views2.intlRemittance.transfer.fragments

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fh.payday.BaseFragment
import com.fh.payday.R
import com.fh.payday.datasource.models.Card
import com.fh.payday.datasource.models.intlRemittance.InitializeTransaction
import com.fh.payday.datasource.models.intlRemittance.IntlBeneficiary
import com.fh.payday.datasource.models.intlRemittance.RateConversion
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.*
import com.fh.payday.viewmodels.intlRemittance.TransferViewmodel
import com.fh.payday.views2.intlRemittance.DeliveryModes
import com.fh.payday.views2.intlRemittance.getTransPurpose
import com.fh.payday.views2.intlRemittance.termsandconditions.TermsAndConditionsActivity
import com.fh.payday.views2.intlRemittance.transfer.TransferActivity
import com.fh.payday.views2.intlRemittance.transfer.adapter.SummaryAdapter
import com.fh.payday.views2.intlRemittance.transfer.adapter.SummaryUI
import kotlinx.android.synthetic.main.fragment_imt_summary.*
import kotlinx.android.synthetic.main.rate_calculator_layout.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class IMTSummaryFragment : BaseFragment() {

    private val viewModel: TransferViewmodel? by lazy {
        when (val activity = activity) {
            is TransferActivity -> activity.viewmodel
            else -> null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_imt_summary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler_view.adapter = SummaryAdapter {
            viewModel?.promoCode = it
        }

        attachOtpObserver()
        attachListeners()
        when (arguments?.getString("type")) {
            TYPE_REPEAT_TRANS -> {
                if (viewModel?.selectedDeliveryMode == DeliveryModes.BT ||
                    viewModel?.selectedDeliveryMode == DeliveryModes.BTALTERNATE)
                    tv_title.text = getString(R.string.bank_transfer)
                else tv_title.text = getString(R.string.cash_payout)
                val user = UserPreferences.instance.getUser(requireContext()) ?: return
                viewModel?.fetchCustomerSummary(
                    user.token,
                    user.sessionId,
                    user.refreshToken,
                    user.customerId.toLong()
                )
                attachCustomerObserver()
                attachTransDetailsObserver()
            }
            else -> attachValidationObserver()

        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        activity ?: return

        if (isVisibleToUser) {
            (recycler_view?.adapter as? SummaryAdapter)?.clear()
            onViewVisible()
            val mViewModel = viewModel ?: return
            if (!mViewModel.isChecked) {
                cb_terms_condition.isChecked = false
            }
        }

    }

    private fun onViewVisible() {
        when (arguments?.getString("type")) {
            TYPE_REPEAT_TRANS -> {
                val user = UserPreferences.instance.getUser(requireContext()) ?: return
                viewModel?.fetchCustomerSummary(
                    user.token,
                    user.sessionId,
                    user.refreshToken,
                    user.customerId.toLong()
                )
            }
            else -> {
                when (val activity = activity) {
                    is TransferActivity -> {
                        validateTrans(activity)
                        val card = activity.viewmodel.selectedCard ?: return
                        val beneficiary = activity.viewmodel.selectedBeneficiary ?: return
                        setupDataViews(card, beneficiary)
                    }
                }
            }
        }
    }

    private fun showProgress() {
        group.visibility = View.GONE
        progress_bar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        group.visibility = View.VISIBLE
        progress_bar.visibility = View.GONE
    }

    private fun setupDataViews(card: Card, beneficiary: IntlBeneficiary) {
        val fromSummary = SummaryUI(
            getString(R.string.from),
            maskCardNumber(card.cardNumber),
            getString(R.string.available_balance_in_aed).format(card.availableBalance.getDecimalValue())
        )

        val toSummary = SummaryUI(
            getString(R.string.to),
            "${beneficiary.receiverFirstName} ${beneficiary.receiverLastName}",
            beneficiary.receiverBankName,
            beneficiary.receiverBankAccountNo
        )

        (recycler_view.adapter as? SummaryAdapter?)?.apply {
            add(0, toSummary)
            add(0, fromSummary)
        }
    }

    private fun validateTrans(activity: TransferActivity) {
        val user = UserPreferences.instance.getUser(activity) ?: return
        val beneficiary = activity.viewmodel.selectedBeneficiary ?: return
        val temp = activity.viewmodel.payInAmount ?: return
        val payInAmount = temp.getDecimalValue().replace(",", "")

        activity.viewmodel.validateTransaction2(
            user.token,
            user.sessionId,
            user.refreshToken,
            user.customerId.toString(),
            beneficiary.recieverRefNumber,
            payInAmount,
            activity.viewmodel.payoutCurrency ?: return,
            activity.viewmodel.payoutCountryCode ?: return,
            activity.viewmodel.payInCurrency ?: return,
            activity.viewmodel.selectedAccessKey ?: return,
            activity.viewmodel.initializeTransaction?.trackingid,
            activity.viewmodel.selectedPurposePaymentCode ?: return,
            if (viewModel?.selectedDeliveryMode == DeliveryModes.BT ) DeliveryModes.BTALTERNATE else DeliveryModes.CPALTERNATE
        )
    }

    private fun attachListeners() {
        val user = UserPreferences.instance.getUser(requireContext()) ?: return
        btn_confirm.setOnClickListener {
            if (validate()) {
                viewModel?.isChecked = true
                viewModel?.generateOtp(
                    user.token,
                    user.sessionId,
                    user.refreshToken,
                    user.customerId.toLong()
                )
            }
        }

        tv_terms_condition_link.apply {
            setTextWithUnderLine(tv_terms_condition_link.text.toString())
            setOnClickListener {
                activity?.startActivity(
                    Intent(
                        activity,
                        TermsAndConditionsActivity::class.java
                    ).apply {
                        putExtra("link", UAEX_TERMS_CONDITIONS)
                    })
            }
        }
    }

    private fun attachOtpObserver() {

        viewModel?.generateOtpState?.observe(this, Observer {
            val activity = activity ?: return@Observer
            it ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing))
                return@Observer
            }
            activity.hideProgress()

            when (state) {
                is NetworkState2.Success -> when (activity) {
                    is TransferActivity -> activity.navigateUp()
                }
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        return@Observer activity.onSessionExpired(state.message)
                    }
                    activity.handleErrorCode(state.errorCode.toInt(), state.message)
                }
                is NetworkState2.Failure -> activity.onFailure(
                    activity.findViewById(R.id.root_view),
                    getString(R.string.request_error)
                )
                else -> activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    private fun attachValidationObserver() {

        viewModel?.validateTransaction2?.observe(this, Observer {
            val activity = activity ?: return@Observer
            it ?: return@Observer

            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                showProgress()
                return@Observer
            }

            hideProgress()

            when (state) {
                is NetworkState2.Success -> when (activity) {
                    is TransferActivity -> {
                        val data = state.data ?: return@Observer
                        activity.viewmodel.payInAmount = data.payInAmount
                        activity.viewmodel.payOutAmount = data.payOutAmount
                        activity.viewmodel.totalAmount = data.totalPayableAmount
                        activity.viewmodel.fxQuoteNo = data.fx_QuoteNo
                        setPayments(activity)
                        setFlags(data)
                        tv_title.visibility = View.VISIBLE
                        if (viewModel?.selectedDeliveryMode == DeliveryModes.BT || viewModel?.selectedDeliveryMode == DeliveryModes.BTALTERNATE) tv_title.text =
                            getString(R.string.bank_transfer)
                        else tv_title.text = getString(R.string.cash_payout)
                    }
                }
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        activity.onSessionExpired(state.message)
                        return@Observer
                    }
                    val (message) = state
                    activity.handleErrorCode(state.errorCode.toInt(), message)
                }
                is NetworkState2.Failure -> activity.onFailure(
                    activity.findViewById(R.id.layout),
                    state.throwable
                )
            }
        })
    }

    private fun attachCustomerObserver() {
        viewModel?.customerSummaryState?.observe(viewLifecycleOwner, Observer {
            val activity = activity ?: return@Observer
            it ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                showProgress()
                return@Observer
            }

            hideProgress()

            when (state) {
                is NetworkState2.Success -> {
                    if (!state.data?.cards.isNullOrEmpty()) {
                        val card = state.data?.cards?.get(0) ?: return@Observer
                        val beneficiary = viewModel?.selectedBeneficiary ?: return@Observer
                        setupDataViews(card, beneficiary)
                        fetchPurposeOfPayment()
                    }
                }
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        activity.onSessionExpired(state.message)
                        return@Observer
                    }
                    val (message) = state
                    activity.handleErrorCode(state.errorCode.toInt(), message)
                }
                is NetworkState2.Failure -> activity.onFailure(
                    activity.findViewById(R.id.layout),
                    getString(R.string.request_error)
                )
                else -> activity.onFailure(
                    activity.findViewById(R.id.layout),
                    getString(R.string.request_error)
                )
            }
        })
    }

    private fun fetchTransDetails(initializeTransaction: InitializeTransaction) {
        val user = UserPreferences.instance.getUser(requireContext()) ?: return
        val transactionId = arguments?.getString("transactions_id") ?: return
        val partnerTxnRefNo = arguments?.getString("partnerTxnRefNo") ?: return
        val beneficiary = viewModel?.selectedBeneficiary ?: return

        val payInAmount = viewModel?.payInAmount?.getDecimalValue() ?: return
        val payOutCurr = viewModel?.payoutCurrency ?: return
        val payOutCountryCode = viewModel?.payoutCountryCode ?: return
        val payInCurr = viewModel?.payInCurrency ?: return
        viewModel?.selectedAccessKey = arguments?.getString("accessKey") ?: return
        viewModel?.selectedDeliveryMode = arguments?.getString("paymentMode") ?: return

        viewModel?.getTransDetailsAndRateCon(
            user.token,
            user.sessionId,
            user.refreshToken,
            user.customerId.toString(),
            transactionId,
            partnerTxnRefNo,
            beneficiary.recieverRefNumber,
            payInAmount,
            payOutCurr,
            payOutCountryCode,
            payInCurr,
            arguments?.getString("accessKey") ?: return,
            initializeTransaction.trackingid,
            if (initializeTransaction.transactionPurposes.isEmpty()) return else initializeTransaction.transactionPurposes[0].code,
            arguments?.getString("paymentMode") ?: return
        )
    }

    private fun fetchPurposeOfPayment() {
        val user = UserPreferences.instance.getUser(requireContext()) ?: return
        val beneficiary = viewModel?.selectedBeneficiary ?: return
        viewModel?.purposeOfPayment(
            user.token,
            user.sessionId,
            user.refreshToken,
            user.customerId.toLong(),
            arguments?.getString("accessKey") ?: return,
            beneficiary.recieverRefNumber
        )
    }

    private fun attachTransDetailsObserver() {
        viewModel?.transDetailsState?.observe(viewLifecycleOwner, Observer {
            val activity = activity ?: return@Observer
            it ?: return@Observer

            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                showProgress()
                return@Observer
            }

            hideProgress()

            when (state) {
                is NetworkState2.Success -> {
                    tv_title.visibility = View.VISIBLE
                    val data = state.data ?: return@Observer
                    viewModel?.payInAmount = data.rateConResponse.data?.payInAmount
                    viewModel?.payOutAmount = data.rateConResponse.data?.payOutAmount
                    viewModel?.totalAmount = data.rateConResponse.data?.totalPayableAmount
                    viewModel?.fxQuoteNo = data.rateConResponse.data?.fx_QuoteNo
                    viewModel?.transferPurpose = when (data.transDetailsResponse.data) {
                        null -> ""
                        else -> getTransPurpose(
                            requireContext(),
                            data.transDetailsResponse.data.purposeOfTxn
                        )
                    }

                    when (activity) {
                        is TransferActivity -> {
                            setPayments(activity)
                            data.rateConResponse.data?.let { rate -> setFlags(rate) }
                        }
                    }
                }
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        activity.onSessionExpired(state.message)
                        return@Observer
                    }
                    val (message) = state
                    activity.handleErrorCode(state.errorCode.toInt(), message)
                }
                is NetworkState2.Failure -> activity.onFailure(
                    activity.findViewById(R.id.layout),
                    getString(R.string.request_error)
                )
            }
        })

        viewModel?.purposeOfPaymentState?.observe(viewLifecycleOwner, Observer {
            val activity = activity ?: return@Observer
            it ?: return@Observer

            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                showProgress()
                return@Observer
            }
            hideProgress()
            when (state) {
                is NetworkState2.Success -> {
                    state.data ?: return@Observer
                    fetchTransDetails(state.data)

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

    private fun setPayments(activity: TransferActivity) {
        val payInAmount = activity.viewmodel.payInAmount ?: return
        val payOutAmount = activity.viewmodel.payOutAmount ?: return

        et_sender_amount.setText(payInAmount.getDecimalValue())
        et_receiver_amount.setText(payOutAmount.getDecimalValue())
        tv_sender_currency.text = activity.viewmodel.payInCurrency
        tv_receiver_currency.text = activity.viewmodel.payoutCurrency

        val rates = activity.viewmodel.rateResponse ?: return

        val formatter = DecimalFormat("#,###.####", DecimalFormatSymbols.getInstance(Locale("en")))
        tv_exchange_rate.text = String.format(
            getString(R.string.exchange_rate_label), "1", activity.viewmodel.payInCurrency,
            formatter.format(1 / rates.exchangeRate.toDouble()), activity.viewmodel.payoutCurrency
        )


        val summaryList = arrayListOf<SummaryUI>()

        val purpose = activity.viewmodel.transferPurpose
        if (!purpose.isNullOrEmpty()) {
            summaryList.add(SummaryUI(getString(R.string.purpose), purpose))
        }


        when (arguments?.getString("type")) {
            TYPE_REPEAT_TRANS -> summaryList.add(
                SummaryUI(
                    getString(R.string.promo_code),
                    "",
                    isEditable = true
                )
            )
            else -> {
                val promoCode = activity.viewmodel.promoCode
                if (!promoCode.isNullOrEmpty()) {
                    summaryList.add(SummaryUI(getString(R.string.promo_code), promoCode))
                }
            }
        }
        summaryList.add(
            SummaryUI(
                getString(R.string.commission),
                getString(R.string.amount_in_aed).format(rates.commission)
            )
        )
        summaryList.add(
            SummaryUI(
                getString(R.string.vat),
                getString(R.string.amount_in_aed).format(rates.vat)
            )
        )

        val totalPayableAmount = getString(R.string.amount_in_aed)
            .format(activity.viewmodel.totalAmount.toString().getDecimalValue())
        summaryList.add(SummaryUI(getString(R.string.total), totalPayableAmount))

        (recycler_view.adapter as? SummaryAdapter?)?.addAll(summaryList)
    }

    private fun setFlags(rate: RateConversion) {
        rate.payInFlag?.let { path ->
            if (path.isNotEmpty())
                GlideApp.with(iv_sender_flag)
                    .load(BASE_URL + path)
                    .placeholder(R.drawable.ic_flag_placeholder)
                    .error(R.drawable.ic_flag_placeholder)
                    .into(iv_sender_flag)
        }

        rate.payOutFlag?.let { path ->
            if (path.isNotEmpty())
                GlideApp.with(iv_receiver_flag)
                    .load(BASE_URL + path)
                    .placeholder(R.drawable.ic_flag_placeholder)
                    .error(R.drawable.ic_flag_placeholder)
                    .into(iv_receiver_flag)
        }
    }

    private fun validate(): Boolean {
        val activity = activity ?: return false
        return when {
            !cb_terms_condition.isChecked -> {
                activity.onFailure(
                    activity.findViewById(R.id.root_view),
                    getString(R.string.terms_conditions_error)
                )
                false
            }
            arguments?.getString("type") == TYPE_REPEAT_TRANS -> {
                val status =
                    isValidPromoCode(viewModel?.promoCode) || viewModel?.promoCode.isNullOrEmpty()
                if (!status) {
                    activity.onFailure(
                        activity.findViewById(R.id.root_view),
                        getString(R.string.invalid_promo_code)
                    )
                }
                status
            }
            else -> true
        }
    }

    companion object {
        const val TYPE_REPEAT_TRANS = "repeat_transaction"

        fun build(
            type: String? = null,
            transId: String,
            partnerTxnRefNo: String,
            accessKey: String,
            paymentMode: String

        ): IMTSummaryFragment {
            return IMTSummaryFragment().apply {
                if (!type.isNullOrEmpty()) {
                    val bundle = Bundle()
                    bundle.putString("type", type)
                    bundle.putString("transactions_id", transId)
                    bundle.putString("partnerTxnRefNo", partnerTxnRefNo)
                    bundle.putString("accessKey", accessKey)
                    bundle.putString("paymentMode", paymentMode)
                    arguments = bundle
                }
            }
        }
    }
}