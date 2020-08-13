package com.fh.payday.views2.payments.ibp


import android.app.DatePickerDialog
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.fh.payday.BaseFragment

import com.fh.payday.R
import com.fh.payday.utilities.DateTime
import com.fh.payday.views2.shared.fragments.DatePickerFragment
import kotlinx.android.synthetic.main.fragment_select_dob.*
import java.util.*

class SelectDobFragment : BaseFragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_select_dob, container, false)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        activity ?: return
        val activity = activity as IndianBillPaymentActivity

        if (isVisibleToUser && activity.viewModel.dataClear) {
            tv_dob.text = null
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_dob.setOnClickListener { showDobDatePicker() }
        btn_next.setOnClickListener {
            val dob = tv_dob.text?.toString() ?: ""
            if (isValid(dob)) {
                onSuccess(dob)
            }
        }

        handleView()
    }

    private fun handleView() {
        val activity = activity ?: return

        when (activity) {
            is IndianBillPaymentActivity -> {
                val dob = activity.viewModel.data.value?.get("dob") ?: ""
                tv_dob.text = dob
            }
        }
    }

    private fun onSuccess(dob: String) {
        val activity = activity ?: return
        when (activity) {
            is IndianBillPaymentActivity -> {
                activity.viewModel.setDob(dob)
                activity.navigateUp()
            }
        }
    }

    private fun showDobDatePicker() {
        val fragmentManager = activity?.supportFragmentManager ?: return

        val maxDate = Calendar.getInstance().apply {
            set(get(Calendar.YEAR) - 20, get(Calendar.MONTH), get(Calendar.DAY_OF_MONTH))
        }
        val minDate = Calendar.getInstance().apply {
            set(get(Calendar.YEAR) - 70, get(Calendar.MONTH), get(Calendar.DAY_OF_MONTH))
        }

        DatePickerFragment.Builder()
                .setDay(maxDate.get(Calendar.DAY_OF_MONTH))
                .setMonth(maxDate.get(Calendar.MONTH))
                .setYear(maxDate.get(Calendar.YEAR))
                .setMinDate(minDate.time)
                .setMaxDate(maxDate.time)
                .attachDateSetListener(DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    val d = String.format(getString(R.string.date_format),
                            String.format("%02d", dayOfMonth), String.format("%02d", (month + 1)), year.toString())
                    setDateView(tv_dob, d)
                })
                .build()
                .show(fragmentManager, "datePicker")
    }

    private fun setDateView(view: TextView, date: String) = with(view) {
        val activity = activity ?: return@with
        text = date
        background = ContextCompat.getDrawable(activity, R.drawable.bg_grey_blue_border)
    }

    private fun unsetDateView(view: TextView) = with(view) {
        val activity = activity ?: return@with
        text = ""
        background = ContextCompat.getDrawable(activity, R.drawable.bg_grey_dark_grey_border)
    }

    private fun setError(view: TextView, message: String) = with(view) {
        val activity = activity ?: return@with
        background = ContextCompat.getDrawable(activity, R.drawable.bg_grey_red_border)
        activity.onFailure(activity.findViewById(R.id.root_view), message)
    }

    private fun isValid(dob: String) = when {
        dob.isEmpty() -> {
            setError(tv_dob, getString(R.string.empty_dob))
            false
        }
        !DateTime.isValid(dob, "dd/MM/yyyy") || !DateTime.isBefore(dob) -> {
            setError(tv_dob, getString(R.string.invalid_dob))
            false
        }
        else -> true
    }
}
