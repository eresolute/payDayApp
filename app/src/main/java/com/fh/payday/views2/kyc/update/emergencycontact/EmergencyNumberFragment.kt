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
import com.fh.payday.utilities.setErrorMessage

class EmergencyNumberFragment : Fragment() {

    private lateinit var activity: EditEmergencyActivity
    private lateinit var etNumber: TextInputEditText
    private lateinit var tilNumber: TextInputLayout

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context as EditEmergencyActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_emergency_number, container, false)
        init(view)
        etNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                tilNumber.clearErrorMessage()
            }
        })
        return view
    }

    private fun init(view: View) {

        etNumber = view.findViewById(R.id.et_number)
        tilNumber = view.findViewById(R.id.textInputLayout)

        view.findViewById<MaterialButton>(R.id.btn_confirm).setOnClickListener {
            if (validateEditText(etNumber.text.toString().toString())) {
                activity.viewModel.emergencyMobile = etNumber.text.toString()
                activity.onNumberSubmit()
            }
        }
    }

    private fun validateEditText(editText: String): Boolean {
        if (TextUtils.isEmpty(editText)) {
            etNumber.requestFocus()
            tilNumber.setErrorMessage(getString(R.string.empty_mobile_no))
            return false
        } else if (editText.length < 4) {
            etNumber.requestFocus()
            tilNumber.setErrorMessage(getString(R.string.invalid_mobile_no))
            return false
        } else {
            tilNumber.clearErrorMessage()
            return true
        }

    }
}