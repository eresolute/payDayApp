package com.fh.payday.views2.servicerequest.paymentholiday

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.views2.servicerequest.ServiceRequestOptionActivity

class PaymentHolidayInstallmentFragment : Fragment(), SeekBar.OnSeekBarChangeListener {

    private lateinit var seekBar: SeekBar
    private lateinit var tvProgressValue: TextView
    private lateinit var btnConfirm: Button
    private var activity: ServiceRequestOptionActivity? = null


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context as ServiceRequestOptionActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.payment_holiday_installement_fragment, container, false)
        init(view)
        tvProgressValue.visibility = View.GONE
        seekBar.setOnSeekBarChangeListener(this)
        btnConfirm.setOnClickListener {
            if(seekBar.progress > 0) {
                activity?.viewModel?.installment?.value = tvProgressValue.text.toString()
                activity?.replaceFragment(PaymentHolidayConditionFragment())
            } else {
                activity?.onError(getString(R.string.invalid_installment_value))
            }
        }
        return view
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, b: Boolean) {
        tvProgressValue.visibility = View.VISIBLE
        tvProgressValue.text = progress.toString()
        setProgressText(seekBar, tvProgressValue)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {

    }


    private fun init(view: View) {
        seekBar = view.findViewById(R.id.seekBar)
        tvProgressValue = view.findViewById(R.id.tv_progress_value)
        btnConfirm = view.findViewById(R.id.btn_confirm)
    }

    private fun setProgressText(seekBar: SeekBar, textView: TextView) {
        val progress = seekBar.progress
        val max = seekBar.max
        val offset = seekBar.thumbOffset
        val percent = progress.toFloat() / max.toFloat()
        val width = seekBar.width - 2 * offset
        val answer = (width * percent + offset.toFloat() + offset.toFloat()).toInt()
        textView.x = answer.toFloat()
    }
}