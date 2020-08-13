package com.fh.payday.views.fragments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.views2.transactionhistoryv2.TransactionHistoryV2Activity
import org.joda.time.DateTime
import java.util.*
import com.fh.payday.utilities.DateTime as Date

@Deprecated("Use google calendar")
class MonthPickerFragment : DialogFragment() {

    private lateinit var dayPicker: NumberPicker
    private lateinit var monthPicker: NumberPicker
    private lateinit var yearPicker: NumberPicker
    private var selectedDay = 0
    private var toDate: DateTime = DateTime.now()
    private var fromDate: DateTime = DateTime.now()
    private val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec")
    private val monthString = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

    lateinit var activity: TransactionHistoryV2Activity
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context as TransactionHistoryV2Activity
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.layout_month_picker, null)

            dayPicker = view.findViewById(R.id.picker_day)
            monthPicker = view.findViewById(R.id.picker_month)
            yearPicker = view.findViewById(R.id.picker_year)
            setMonthPicker(monthPicker)
            setYearPicker(yearPicker)
            setDayPicker()

            toDate = Date.parseDate(activity.viewModel.transactionDate.value?.toDate)
            fromDate = Date.parseDate(activity.viewModel.transactionDate.value?.fromDate)

            val option = arguments?.getString("option")
            if (option != null) {
                if (option == "from") {
                    setValues(view, fromDate.dayOfMonth, fromDate.monthOfYear, fromDate.year)
                } else {
                    setValues(view, toDate.dayOfMonth, toDate.monthOfYear, toDate.year)
                }
            } else
                setValues(view)


            monthPicker.setOnValueChangedListener { picker, oldVal, newVal ->
                view.findViewById<TextView>(R.id.tv_month).text = monthString[newVal - 1]
                setDayPicker()
            }

            yearPicker.setOnValueChangedListener { picker, oldVal, newVal ->
                view.findViewById<TextView>(R.id.tv_year).text = newVal.toString()
                setDayPicker()
                setMonthPicker(monthPicker)
            }

            dayPicker.setOnValueChangedListener { picker, oldVal, newVal ->
                view.findViewById<TextView>(R.id.tv_day).text = newVal.toString()
            }


            builder.setView(view)
                    // Add action buttons
                    .setPositiveButton(R.string.ok
                    ) { dialog, id ->
                        val month = monthPicker.value
                        val year = yearPicker.value
                        arguments ?: return@setPositiveButton
                        val option = arguments?.getString("option")
                        if (option != null) {
                            //activity.setDateTime(month, dayPicker.value, monthString[month - 1], year, option)
                        }
                    }
                    .setNegativeButton(R.string.cancel
                    ) { dialog, id ->
                        getDialog().cancel()
                    }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun adjustMaxDay() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, yearPicker.value)
        cal.set(Calendar.MONTH, monthPicker.value - 1)
        val max = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        selectedDay = max
    }


    private fun setDayPicker() {
        dayPicker.minValue = 1;
        adjustMaxDay()
        if (yearPicker.value == DateTime.now().year && monthPicker.value == DateTime.now().monthOfYear().get())
            dayPicker.maxValue = DateTime.now().dayOfMonth
        else
            dayPicker.maxValue = selectedDay

        dayPicker.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
    }

    private fun setYearPicker(yearPicker: NumberPicker) {
        yearPicker.minValue = DateTime.now().year - 1
        yearPicker.maxValue = DateTime.now().year


        yearPicker.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS

    }

    private fun setMonthPicker(monthPicker: NumberPicker) {
        monthPicker.minValue = 1
        if (yearPicker.value == DateTime.now().year)
            monthPicker.maxValue = DateTime.now().monthOfYear
        else
            monthPicker.maxValue = 12
        monthPicker.displayedValues = months


        monthPicker.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS

    }

    private fun setValues(view: View, day: Int = DateTime.now().dayOfMonth, month: Int = DateTime.now().monthOfYear, year: Int = DateTime.now().year) {
        yearPicker.value = year
        monthPicker.value = month
        dayPicker.value = day

        setMonthPicker(monthPicker)
        setYearPicker(yearPicker)
        setDayPicker()

        view.findViewById<TextView>(R.id.tv_day).text = day.toString()
        view.findViewById<TextView>(R.id.tv_month).text = months[month - 1]
        view.findViewById<TextView>(R.id.tv_year).text = year.toString()


    }
}