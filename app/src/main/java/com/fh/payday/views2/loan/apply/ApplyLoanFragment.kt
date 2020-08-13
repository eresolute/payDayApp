package com.fh.payday.views2.loan.apply

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.datasource.models.loan.LoanOffer
import com.fh.payday.datasource.models.login.User
import com.fh.payday.datasource.remote.NetworkState2
import com.fh.payday.preferences.LocalePreferences
import com.fh.payday.preferences.UserPreferences
import com.fh.payday.utilities.CONNECTION_ERROR
import com.fh.payday.utilities.LOAN_TC_URL
import com.fh.payday.utilities.getDecimalValue
import com.fh.payday.viewmodels.LoanViewModel
import com.fh.payday.viewmodels.MIN_LOAN_AMOUNT
import com.fh.payday.views2.shared.custom.AlertDialogFragment
import com.fh.payday.views2.shared.fragments.TermsConditionsDialogFragment
import kotlinx.android.synthetic.main.fragment_apply_loan.*


class ApplyLoanFragment : Fragment(), SeekBar.OnSeekBarChangeListener {

    private lateinit var rootGroup: View
    private lateinit var emiTenureGroup: View
    private lateinit var seekBarLoanAmount: SeekBar
    private lateinit var tvProgressLoanAmount: TextView
    private lateinit var tvMaxLoanAmount: TextView
    private lateinit var tvMinLoanAmount: TextView
    private lateinit var tvEMIAmount: TextView
    private lateinit var tvTenure: TextView
    private lateinit var btnApply: Button

    private var user: User? = null
    private var activity: ApplyLoanActivity? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context as ApplyLoanActivity?
    }

    override fun onDetach() {
        super.onDetach()
        activity = null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_apply_loan, container, false)
        init(view)

        user = UserPreferences.instance.getUser(activity ?: return view) ?: return view

        seekBarLoanAmount.setOnSeekBarChangeListener(this)
        hideLoanInfoViews()

        createLoanRequest()
        attachListener()
        attachLoanOfferObservers()
        attachCustomizeLoanObserver()

        return view
    }

    override fun onProgressChanged(seekBar: SeekBar, p: Int, b: Boolean) {
        var progress = p
        val activity = activity ?: return
        val minLoanAmount = activity.viewModel.minLoanAmount ?: return
        when (seekBar.id) {
            R.id.seekBar_loan_amount -> {
                if (progress < minLoanAmount.toInt()) {
                    progress = minLoanAmount.toInt()
                } else {
                    progress /= 100
                    progress *= 100
                }

                seekBar.progress = progress

                tvProgressLoanAmount.text = progress.toString().getDecimalValue()
                setProgressText(seekBarLoanAmount, tvProgressLoanAmount)
                setMonthlyEmi()
                tvProgressLoanAmount.visibility = View.VISIBLE
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}

    override fun onStopTrackingTouch(seekBar: SeekBar) {}

    private fun createLoanRequest() {
        activity?.hideNoInternetView()
        if (activity?.isNetworkConnected() == false) {
            return activity?.showNoInternetView { createLoanRequest() } ?: Unit
        }

        val activity = activity ?: return
        val user = user ?: return
        activity.viewModel.loanRequest(user.token, user.sessionId, user.refreshToken, user.customerId.toLong())
    }

    private fun attachListener() {
        btnApply.setOnClickListener {
            val viewModel = activity?.viewModel ?: return@setOnClickListener
            val loanOffer = viewModel.loanOffer ?: return@setOnClickListener

            //if (isLoanOfferNotCustomized(loanOffer)) {

            if (!viewModel.isValidLoanOffer(loanOffer)) return@setOnClickListener

            /*val loanAmount = String.format(getString(R.string.amount_in_aed), loanOffer.approvedAmount.getDecimalValue())*/
            val selectedLoanAmount = tvProgressLoanAmount.text.toString().replace(",", "").toDouble()
            if (selectedLoanAmount == 0.0) return@setOnClickListener

            val loanAmount = String.format(getString(R.string.amount_in_aed), selectedLoanAmount.getDecimalValue())
            val emi = viewModel.calcLoanEmi(selectedLoanAmount,
                loanOffer.interestRate.toDouble(), loanOffer.tenure.toInt()).toString().getDecimalValue()
            val calculatedEmi = String.format(getString(R.string.amount_in_aed), emi)
            val tenure = String.format(getString(R.string.months_valued), loanOffer.tenure)

            val amountLabel = when(viewModel.productType) {
                LoanViewModel.TOPUP_LOAN -> getString(R.string.loan_topup_amount)
                else -> getString(R.string.loan_amount_title)
            }
            val emiLabel = when(viewModel.productType) {
                LoanViewModel.TOPUP_LOAN -> getString(R.string.loan_topup_emi)
                else -> getString(R.string.emi)
            }
            val tenureLabel = when(viewModel.productType) {
                LoanViewModel.TOPUP_LOAN -> getString(R.string.new_tenure)
                else -> getString(R.string.tenure)
            }

            val dialog = LoanEligibilityDialogFragment.Builder()
                .add(amountLabel, loanAmount)
                .add(emiLabel, calculatedEmi)
                .add(tenureLabel, tenure)
                .showNote(viewModel.productType == LoanViewModel.TOPUP_LOAN)
                .attachPositiveListener {
                    viewModel.user?.let {
                        if (activity?.isNetworkConnected() == false)
                            return@attachPositiveListener activity?.let { a ->
                                a.onFailure(a.findViewById(R.id.root_view), getString(R.string.no_internet_connectivity))
                            } ?: Unit

                        viewModel.generateOtp(it.token, it.sessionId, it.refreshToken, it.customerId.toLong())
                        viewModel.setSelectedLoanAmount(tvProgressLoanAmount.text.toString().replace(",", "").toDouble())
                    }
                }
                .attachLinkListener {
                    val dialogFragment: DialogFragment = TermsConditionsDialogFragment
                        .newInstance(LOAN_TC_URL, getString(R.string.close))
                    if (activity == null) return@attachLinkListener
                    dialogFragment.show(activity?.supportFragmentManager, "dialog")
                }
                .build()

            dialog.show(activity?.supportFragmentManager, "dialog")
            return@setOnClickListener
            //}

            /*val token = user?.token ?: return@setOnClickListener
            val sessionId = user?.sessionId ?: return@setOnClickListener
            val customerId = user?.customerId ?: return@setOnClickListener
            val refreshToken = user?.refreshToken ?: return@setOnClickListener
            val loanAmount = seekBarLoanAmount.progress.getDecimalValue()
            val loanTenure = loanOffer?.tenure ?: return@setOnClickListener

            viewModel.customizeLoan(token, sessionId, refreshToken, customerId.toLong(), loanAmount, loanTenure)*/
        }
    }

    /*private fun isLoanOfferNotCustomized(loanOffer: LoanOffer?): Boolean {
        loanOffer ?: return false

        return try {
            loanOffer.approvedAmount.replace(",", "").toInt() == seekBarLoanAmount.progress
        } catch (e: NumberFormatException) {
            false
        }
    }*/

    private fun init(view: View) {
        rootGroup = view.findViewById(R.id.root_group)
        emiTenureGroup = view.findViewById(R.id.group_loan_emi_tenure)
        seekBarLoanAmount = view.findViewById(R.id.seekBar_loan_amount)
        tvProgressLoanAmount = view.findViewById(R.id.tv_progress_value)
        tvMaxLoanAmount = view.findViewById(R.id.tv_max_loan_amount)
        tvMinLoanAmount = view.findViewById(R.id.tv_min_loan_amount)
        tvEMIAmount = view.findViewById(R.id.tv_emi_amount)
        tvTenure = view.findViewById(R.id.tv_tenure)
        btnApply = view.findViewById(R.id.btn_apply)

        btnApply.visibility = View.GONE
    }

    private fun setProgressText(seekbar: SeekBar, textView: TextView) {
        val lang = LocalePreferences.instance.getLocale(context!!)
        val progress = seekbar.progress
        val max = seekbar.max
        val offset = seekbar.thumbOffset
        val percent = progress.toFloat() / max.toFloat()
        val width = seekbar.width - 4 * offset
        val answer = (width * percent + offset.toFloat() + offset.toFloat()).toInt()
        //textView.x = answer.toFloat()
        val offsetMax = seekbar.thumbOffset
        val percentMax = max.toFloat() / max.toFloat()
        val widthMax = seekbar.width - 1 * offsetMax
        val answerMax = (widthMax * percentMax + offsetMax.toFloat() + offsetMax.toFloat()).toInt()
        if (lang == "ar" || lang == "ur")
            textView.x =  answerMax.toFloat() - answer.toFloat()
        else
            textView.x = answer.toFloat()

        val loanAmountStr = activity?.viewModel?.loanOffer?.approvedAmount ?: return
        try {
            val loanAmount = loanAmountStr.toDouble().toInt()

            if (progress == loanAmount) {
                textView.text = loanAmountStr.getDecimalValue()
            }

        } catch (e: NumberFormatException) { e.printStackTrace() }
    }

    private fun attachLoanOfferObservers() {
        activity?.viewModel?.loanRequestState?.observe(this, Observer {
            it ?: return@Observer
            val activity = this.activity ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                btnApply.visibility = View.GONE
                progress_bar.visibility = View.VISIBLE
                return@Observer
            }

            progress_bar.visibility = View.GONE
            activity.hideProgress()

            when (state) {
                is NetworkState2.Success -> {
                    setLoanOfferView(state.data, state.message)
                }
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        return@Observer activity.onSessionExpired(state.message)
                    }

                    if (state.errorCode == "349" || state.errorCode == "399") {
                        activity.showWarning(
                            state.message,
                            R.drawable.ic_warning, R.color.blue,
                            AlertDialogFragment.OnConfirmListener { dialog ->
                                dialog.dismiss()
                                activity.finish()
                            }
                        )
                        return@Observer
                    }

                    val dismissListener = AlertDialogFragment.OnDismissListener { dialog ->
                        dialog.dismiss()
                        activity.finish()
                    }
                    val cancelListener = AlertDialogFragment.OnCancelListener { dialog ->
                        dialog.dismiss()
                        activity.finish()
                    }
                    val confirmListener = AlertDialogFragment.OnConfirmListener { dialog ->
                        dialog.dismiss()
                        activity.finish()
                    }

                    activity.onError(state.message, false, activity.errorIcon,
                        activity.tintColorError, dismissListener, cancelListener, confirmListener)
                }
                is NetworkState2.Failure -> activity.onFailure(activity.findViewById(R.id.root_view),
                    state.throwable)
                else -> activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    private fun attachCustomizeLoanObserver() {
        activity?.viewModel?.customizeLoanState?.observe(this, Observer {
            it ?: return@Observer
            val activity = this.activity ?: return@Observer
            val state = it.getContentIfNotHandled() ?: return@Observer

            if (state is NetworkState2.Loading) {
                btnApply.visibility = View.INVISIBLE
                progress_bar_2.visibility = View.VISIBLE
                return@Observer
            }

            progress_bar_2.visibility = View.GONE
            btnApply.visibility = View.VISIBLE

            when (state) {
                is NetworkState2.Success -> onCustomizeSuccess(state.data)
                is NetworkState2.Error -> {
                    if (state.isSessionExpired) {
                        return@Observer activity.onSessionExpired(state.message)
                    }
                    activity.onError(state.message)
                }
                is NetworkState2.Failure -> activity.onFailure(activity.findViewById(R.id.root_view),
                    state.throwable)
                else -> activity.onFailure(activity.findViewById(R.id.root_view), CONNECTION_ERROR)
            }
        })
    }

    private fun onCustomizeSuccess(loanOffer: LoanOffer?) {
        loanOffer ?: return

        val loanAmount = String.format(getString(R.string.amount_in_aed), loanOffer.approvedAmount.getDecimalValue())
        val emi = String.format(getString(R.string.amount_in_aed), loanOffer.emi.getDecimalValue())
        val tenure = String.format(getString(R.string.months_valued), loanOffer.tenure)

        LoanEligibilityDialogFragment.Builder()
            .add(getString(R.string.loan_amount_title), loanAmount)
            .add(getString(R.string.emi), emi)
            .add(getString(R.string.tenure), tenure)
            .attachPositiveListener {
                activity?.navigateUp()
            }
            .attachLinkListener {
                val dialogFragment: DialogFragment = TermsConditionsDialogFragment
                    .newInstance(LOAN_TC_URL, getString(R.string.close))
                if (activity == null) return@attachLinkListener
                dialogFragment.show(activity?.supportFragmentManager, "dialog")
            }
            .build()
            .show(activity?.supportFragmentManager, "dialog")
    }

    private fun setLoanOfferView(loanOffer: LoanOffer?, message: String?, minLoanAmount: Int = MIN_LOAN_AMOUNT) {

        if (!message.isNullOrEmpty() && message != "null") {
            activity?.showWarning(message,
                R.drawable.ic_warning, R.color.blue,
                AlertDialogFragment.OnConfirmListener {
                    it.dismiss()
                    activity?.finish()
                })
            return
        }

        loanOffer ?: return
        try {
            rootGroup.visibility = View.VISIBLE
            emiTenureGroup.visibility = View.VISIBLE

            val aedAmount = getString(R.string.amount_in_aed)
//            tvMinLoanAmount.text = String.format(aedAmount, loanOffer.minAmount.getDecimalValue())
            tvMaxLoanAmount.text = String.format(aedAmount, loanOffer.approvedAmount.getDecimalValue())
            tvEMIAmount.text = String.format(aedAmount, loanOffer.emi.getDecimalValue())
            tvTenure.text = String.format(getString(R.string.months_value), loanOffer.tenure)

            val loanAmount = loanOffer.approvedAmount.getDecimalValue().replace(",", "").toDouble()
            val tenure = loanOffer.tenure.toInt()
            seekBarLoanAmount.max = loanAmount.toInt()
            seekBarLoanAmount.progress = loanAmount.toInt()
            setMonthlyEmi(loanAmount, loanOffer.interestRate.toDouble(), tenure)

            tvProgressLoanAmount.visibility = View.GONE
            btnApply.visibility = View.VISIBLE
        } catch (e: NumberFormatException) {
            activity?.onFailure(activity?.findViewById(R.id.root_view)
                ?: return, getString(R.string.request_error))
        }
    }

    private fun setMonthlyEmi(amount: Double, interest: Double, tenure: Int) {
        val viewModel = activity?.viewModel ?: return
        val emi = viewModel.calcLoanEmi(amount, interest, tenure)
        tvEMIAmount.text = String.format(getString(R.string.amount_in_aed), emi.toString().getDecimalValue())
    }

    private fun setMonthlyEmi() {
        try {
            val viewModel = activity?.viewModel ?: return
            val loanOffer = viewModel.loanOffer ?: return
            val emi = viewModel.calcLoanEmi(seekBarLoanAmount.progress.toDouble(),
                loanOffer.interestRate.toDouble(), loanOffer.tenure.toInt())
            tvEMIAmount.text = String.format(getString(R.string.amount_in_aed), emi.toString().getDecimalValue())
        } catch (e: NumberFormatException) {
            activity?.onFailure(activity?.findViewById(R.id.root_view)
                ?: return, getString(R.string.request_error))
        }
    }

    private fun hideLoanInfoViews() {
        rootGroup.visibility = View.INVISIBLE
        emiTenureGroup.visibility = View.INVISIBLE
        tvEMIAmount.text = ""
        tvMaxLoanAmount.text = ""
        tvEMIAmount.text = ""
        tvTenure.text = ""
    }
}
