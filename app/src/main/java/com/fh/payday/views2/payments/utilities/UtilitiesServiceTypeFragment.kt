package com.fh.payday.views2.payments.utilities

import android.content.Context
import android.os.Bundle
import android.support.design.button.MaterialButton
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import com.fh.payday.R


class UtilitiesServiceTypeFragment : Fragment() {
    private lateinit var layoutSpinner: LinearLayout
    private lateinit var spinner: Spinner
    private lateinit var spinnerLang: Spinner
    private lateinit var btnNext: MaterialButton
    private lateinit var activity: UtilitiesActivity
    private lateinit var tvError: TextView
    private lateinit var tvErrorLang: TextView


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context as UtilitiesActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_aadc_service, container, false)
        initView(view)

        val adapter = ArrayAdapter.createFromResource(view.context,
                R.array.aadc_service_type, R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val langAdapter = ArrayAdapter.createFromResource(view.context,
                R.array.aadc_languages, R.layout.simple_spinner_item)
        langAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        spinnerLang.adapter = langAdapter

        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                tvError.visibility = View.GONE
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
            }
        }
        spinnerLang.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                tvErrorLang.visibility = View.GONE
            }

        }

        btnNext.setOnClickListener {

            when {
                spinner.selectedItem == null -> {
                    tvError.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                "" == spinner.selectedItem.toString() -> {
                    tvError.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                else -> {
                    tvError.visibility = View.GONE

                    val mArray = resources.getStringArray(R.array.aadc_service_type)
                    when (spinner.selectedItem.toString()) {
                        mArray[1] -> {
                            activity.viewModel.aadcSelectedService = "AccountID"
                        }
                        mArray[2] -> {
                            activity.viewModel.aadcSelectedService = "EmiratesID"
                        }
                        mArray[3] -> {
                            activity.viewModel.aadcSelectedService = "PersonId"
                        }
                        mArray[4] -> {
                            activity.viewModel.aadcSelectedService = "MobileNo"

                        }
                    }
//                    activity.viewModel.aadcSelectedService = spinner.selectedItem.toString()
                    onSuccess(activity)
                }
            }
        }
        return view
    }

    private fun initView(view: View) {
        layoutSpinner = view.findViewById(R.id.layout_spinner)
        spinner = view.findViewById(R.id.spinner)
        spinnerLang = view.findViewById(R.id.spinner_lang)
        btnNext = view.findViewById(R.id.btn_next)
        tvError = view.findViewById(R.id.tv_error)
        tvErrorLang = view.findViewById(R.id.tv_lang_error)
    }

    private fun onSuccess(activity: FragmentActivity) {
        if (activity is UtilitiesActivity) {
            if (getActivity() != null)
                (getActivity() as UtilitiesActivity).navigateUp()
        }
    }
}