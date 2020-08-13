package com.fh.payday.views2.kyc.update.emergencycontact

import android.content.Context
import android.os.Bundle
import android.support.design.button.MaterialButton
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fh.payday.R
import com.fh.payday.utilities.clearErrorMessage
import com.fh.payday.utilities.onTextChanged
import com.fh.payday.utilities.setErrorMessage

class EmergencyNameFragment : Fragment() {

    private lateinit var activity: EditEmergencyActivity
    private lateinit var etName: TextInputEditText
    private lateinit var etNumber: TextInputEditText
    private lateinit var etRelation: TextInputEditText
    private lateinit var tilName: TextInputLayout
    private lateinit var tilNumber: TextInputLayout
    private lateinit var tilRelation: TextInputLayout

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context as EditEmergencyActivity
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_emergency_name, container, false)
        init(view)

        etName.onTextChanged { _, _, _, _ -> tilName.clearErrorMessage() }
        etNumber.onTextChanged { _, _, _, _ -> tilNumber.clearErrorMessage() }
        etRelation.onTextChanged { _, _, _, _ -> tilRelation.clearErrorMessage() }


        return view
    }

    fun init(view: View) {
        etName = view.findViewById(R.id.et_name)
        etNumber = view.findViewById(R.id.et_number)
        etRelation = view.findViewById(R.id.et_relation)
        tilName = view.findViewById(R.id.textInputLayout)
        tilNumber = view.findViewById(R.id.textInputLayout_number)
        tilRelation = view.findViewById(R.id.textInputLayout_relation)
        view.findViewById<MaterialButton>(R.id.btn_confirm).setOnClickListener {
            if (validateEditText()) {
                activity.viewModel.emergencyName = etName.text.toString()
                activity.viewModel.emergencyMobile = etNumber.text.toString()
                activity.viewModel.emergencyRelation = etRelation.text.toString()
                activity.onRelationSubmit()
            }

        }
    }

    private fun validateEditText(): Boolean {
        when {
            TextUtils.isEmpty(etName.text.toString().trim()) -> {
                etName.requestFocus()
                tilName.setErrorMessage(getString(R.string.empty_name))
                return false
            }
            TextUtils.isEmpty(etNumber.text.toString().trim()) -> {
                etNumber.requestFocus()
                tilNumber.setErrorMessage(getString(R.string.empty_mobile_no))
                return false
            }
            etNumber.text.toString().trim().length < 4 -> {
                etNumber.requestFocus()
                tilNumber.setErrorMessage(getString(R.string.invalid_mobile_no))
                return false
            }
            TextUtils.isEmpty(etRelation.text.toString().trim()) -> {
                etRelation.requestFocus()
                tilRelation.setErrorMessage(getString(R.string.empty_value))
                return false
            }
            else -> return true
        }

    }
}