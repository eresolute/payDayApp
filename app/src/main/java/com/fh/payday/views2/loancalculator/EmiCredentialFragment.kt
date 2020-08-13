package com.fh.payday.views2.loancalculator

import android.content.Context
import android.os.Bundle
import android.support.design.button.MaterialButton
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.preferences.LocalePreferences
import com.fh.payday.utilities.getDecimalValue
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity.Companion.MAX_INTEREST
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity.Companion.MAX_LOAN
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity.Companion.MAX_TENURE
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity.Companion.MIN_INTEREST
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity.Companion.MIN_LOAN
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity.Companion.MIN_TENURE
import java.lang.IllegalArgumentException


class EmiCredentialFragment : Fragment(), SeekBar.OnSeekBarChangeListener {

    private lateinit var seekBarLoanAmount: SeekBar
    private lateinit var seekBarTenure: SeekBar
    private lateinit var seekBarInterest: SeekBar
    private lateinit var tvProgressLoanAmount: TextView
    private lateinit var tvProgressTenure: TextView
    private lateinit var tvProgressInterest: TextView
    private lateinit var tvMaxLoanAmount: TextView
    private lateinit var tvMinLoanAmount: TextView
    private lateinit var tvMaxTenure: TextView
    private lateinit var tvMinTenure: TextView
    private lateinit var tvMaxInterest: TextView
    private lateinit var tvMinInterest: TextView

    lateinit var activity: LoanCalculatorActivity

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = when(context) {
            is LoanCalculatorActivity -> context
            else -> throw IllegalArgumentException("Illegal context")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(container?.context).inflate(R.layout.fragment_emi_credential, container, false)
        init(view)

        tvProgressLoanAmount.visibility = View.INVISIBLE
        tvProgressTenure.visibility = View.INVISIBLE
        tvProgressInterest.visibility = View.INVISIBLE
        seekBarLoanAmount.setOnSeekBarChangeListener(this)
        seekBarTenure.setOnSeekBarChangeListener(this)
        seekBarInterest.setOnSeekBarChangeListener(this)


        view.findViewById<MaterialButton>(R.id.btn_get_emi).setOnClickListener {

            if (seekBarTenure.progress == 0 || seekBarInterest.progress == 0 || seekBarLoanAmount.progress == 0)
                return@setOnClickListener

            activity.viewModel.loanAmount = seekBarLoanAmount.progress
            activity.viewModel.interestRate = seekBarInterest.progress
            activity.viewModel.tenure = seekBarTenure.progress
            activity.onCredentialsAdded()
        }

        return view
    }

    fun init(view: View) {

        seekBarLoanAmount = view.findViewById(R.id.seekBar_loan_amount)
        seekBarTenure = view.findViewById(R.id.seekBar_tenure)
        seekBarInterest = view.findViewById(R.id.seekBar_interest)
        tvProgressLoanAmount = view.findViewById(R.id.tv_progress_loan)
        tvProgressTenure = view.findViewById(R.id.tv_progress_tenure)
        tvProgressInterest = view.findViewById(R.id.tv_progress_interest)

        tvMaxLoanAmount = view.findViewById(R.id.max_loan_amount)
        tvMinLoanAmount = view.findViewById(R.id.min_loan_amount)
        tvMinTenure = view.findViewById(R.id.min_tenure)
        tvMaxTenure = view.findViewById(R.id.max_tenure)
        tvMinInterest = view.findViewById(R.id.min_interest)
        tvMaxInterest = view.findViewById(R.id.max_interest)

        tvMaxLoanAmount.text = String.format(getString(R.string.amount_in_aed), MAX_LOAN.toString().getDecimalValue())
        tvMinLoanAmount.text = String.format(getString(R.string.amount_in_aed), MIN_LOAN.toString())
        tvMinInterest.text = String.format(getString(R.string.percentage), MIN_INTEREST.toString())
        tvMaxInterest.text = String.format(getString(R.string.percentage), MAX_INTEREST.toString())
        tvMinTenure.text = MIN_TENURE.toString()
        tvMaxTenure.text = MAX_TENURE.toString()


    }

    override fun onProgressChanged(seekBar: SeekBar, p: Int, fromUser: Boolean) {
        var progress = p
        when(seekBar.id) {
            R.id.seekBar_loan_amount -> {
                when {
                    progress < MIN_LOAN -> progress = MIN_LOAN
                    progress > MAX_LOAN -> progress = MAX_LOAN
                    else -> {
                        progress /= 500
                        progress *= 500
                    }
                }
                seekBar.progress = progress

                tvProgressLoanAmount.visibility = View.VISIBLE
                tvProgressLoanAmount.text =  String.format(getString(R.string.amount_in_aed, progress.toString().getDecimalValue() ))
                setProgressText(seekBarLoanAmount, tvProgressLoanAmount, 4)
            }

            R.id.seekBar_tenure -> {
                when {
                    progress < MIN_TENURE -> progress = MIN_TENURE
                    progress > MAX_TENURE -> progress = MAX_TENURE
                }
                seekBar.progress = progress

                tvProgressTenure.visibility = View.VISIBLE
                tvProgressTenure.text = progress.toString()
                setProgressText(seekBarTenure, tvProgressTenure, 2)
            }

            R.id.seekBar_interest -> {
                when {
                    progress < MIN_INTEREST -> progress = MIN_INTEREST
                    progress > MAX_INTEREST -> progress = MAX_INTEREST
                }
                seekBar.progress = progress

                tvProgressInterest.visibility = View.VISIBLE
                tvProgressInterest.text = String.format(getString(R.string.percentage), progress)
                setProgressText(seekBarInterest, tvProgressInterest, 2)
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {

    }

    private fun setProgressText(seekBar: SeekBar, textView: TextView, widthOffset: Int) {
        val lang = LocalePreferences.instance.getLocale(context!!)
        val progress = seekBar.progress
        val max = seekBar.max
        val offset = seekBar.thumbOffset
        val percent = progress.toFloat() / max.toFloat()
        val width = seekBar.width - widthOffset * offset
        val answer = (width * percent + offset.toFloat() + offset.toFloat()).toInt()
        //textView.x = answer.toFloat()
        val offsetMax = seekBar.thumbOffset
        val percentMax = max.toFloat() / max.toFloat()
        val widthMax = seekBar.width - 1 * offsetMax
        val answerMax = (widthMax * percentMax + offsetMax.toFloat() + offsetMax.toFloat()).toInt()
        if (lang == "ar" || lang == "ur")
            textView.x =  answerMax.toFloat() - answer.toFloat()
        else
            textView.x = answer.toFloat()
    }
}