package com.fh.payday.views2.shared.fragments

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.WindowManager
import com.fh.payday.BuildConfig
import com.fh.payday.preferences.LocalePreferences
import java.util.*

class DatePickerFragment : DialogFragment() {
    private var mDateSetListener: DatePickerDialog.OnDateSetListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (!BuildConfig.DEBUG) {
            activity?.window?.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        }

        val locale = Locale("en")
        Locale.setDefault(locale)

        val configuration = context?.resources?.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration?.setLocale(locale)
        }
        val c = Calendar.getInstance()
        val day = arguments?.getInt("day", c.get(Calendar.DAY_OF_WEEK))
            ?: c.get(Calendar.DAY_OF_WEEK)
        val month = arguments?.getInt("month", c.get(Calendar.MONTH)) ?: c.get(Calendar.MONTH)
        val year = arguments?.getInt("year", c.get(Calendar.YEAR)) ?: c.get(Calendar.YEAR)
        val dateSetListener = mDateSetListener
            ?: DatePickerDialog.OnDateSetListener { _, _, _, _ -> }

        val dialog = DatePickerDialog(activity!!, dateSetListener, year, month, day)

        val minDate = arguments?.getSerializable("minDate") as Date?
        val maxDate = arguments?.getSerializable("maxDate") as Date?

        minDate?.let { dialog.datePicker.minDate = it.time }
        maxDate?.let { dialog.datePicker.maxDate = it.time }

        return dialog
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        if (activity == null) return
        val locale = Locale(LocalePreferences.instance.getLocale(requireContext()))
        Locale.setDefault(locale)

        val configuration = context?.resources?.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration?.setLocale(locale)
        }
    }


    class Builder {
        private var mDay: Int? = null
        private var mMonth: Int? = null
        private var mYear: Int? = null
        private var mDateSetListener: DatePickerDialog.OnDateSetListener? = null
        private var mMinDate: Date? = null
        private var mMaxDate: Date? = null

        fun setDay(day: Int): Builder {
            this.mDay = day
            return this
        }

        fun setMonth(month: Int): Builder {
            this.mMonth = month
            return this
        }

        fun setYear(year: Int): Builder {
            this.mYear = year
            return this
        }

        fun attachDateSetListener(dateSetListener: DatePickerDialog.OnDateSetListener): Builder {
            this.mDateSetListener = dateSetListener
            return this
        }

        fun setMaxDate(maxDate: Date): Builder {
            this.mMaxDate = maxDate
            return this
        }

        fun setMinDate(minDate: Date): Builder {
            this.mMinDate = minDate
            return this
        }

        fun build(): DatePickerFragment {
            val fragment = DatePickerFragment()
            val bundle = Bundle()

            mDay?.let { bundle.putInt("day", it) }
            mMonth?.let { bundle.putInt("month", it) }
            mYear?.let { bundle.putInt("year", it) }
            mMinDate?.let { bundle.putSerializable("minDate", it) }
            mMaxDate?.let { bundle.putSerializable("maxDate", it) }
            mDateSetListener?.let { fragment.mDateSetListener = it }

            fragment.arguments = bundle

            return fragment
        }
    }
}