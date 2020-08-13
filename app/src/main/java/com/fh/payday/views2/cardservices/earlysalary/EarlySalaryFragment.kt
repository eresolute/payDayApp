package com.fh.payday.views2.cardservices.earlysalary

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
import com.fh.payday.utilities.OnOTPConfirmListener
import com.fh.payday.views.fragments.OTPFragment


class EarlySalaryFragment : Fragment(), SeekBar.OnSeekBarChangeListener {

    private lateinit var seekBar: SeekBar
    private lateinit var tvProgressValue: TextView
    private lateinit var btnSubmit: Button

    private var activity: EarlySalaryActivity? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context as EarlySalaryActivity
    }

    override fun onDetach() {
        super.onDetach()
        activity = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_early_salary, container, false)
        init(view)
        tvProgressValue.visibility = View.GONE
        seekBar.setOnSeekBarChangeListener(this)
        btnSubmit.setOnClickListener{
            activity?.replaceFragment(  OTPFragment.Builder(object : OnOTPConfirmListener{
                override fun onOtpConfirm(otp: String) {
                    /*AlertDialogFragment.Builder()
                            .setMessage(getString(R.string.limit_setup_successful))
                            .setIcon(R.drawable.ic_success_checked_blue)
                            .setCancelable(false)
                            .setConfirmListener { dialog ->
                                dialog.dismiss()
                                activity?.finish()
                            }
                            .build()
                            .show(fragmentManager, "")*/
                }
            })
                    .setPinLength(6)
                    .setTitle(getString(R.string.enter_otp))
                    .setInstructions(null)
                    .setButtonTitle(getString(R.string.submit))
                    .setHasCardView(false)
                    .build())
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
        btnSubmit = view.findViewById(R.id.btn_submit)
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
