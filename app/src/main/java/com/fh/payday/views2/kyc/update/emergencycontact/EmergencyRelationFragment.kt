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

class EmergencyRelationFragment : Fragment() {

    private lateinit var activity: EditEmergencyActivity
    private lateinit var etRelation: TextInputEditText
    private lateinit var tilRelation: TextInputLayout

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        activity = context as EditEmergencyActivity
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_emergency_relation, container, false)
        init(view)

        etRelation.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { tilRelation.clearErrorMessage() }
        })

        return view
    }

    private fun init(view: View) {
        etRelation = view.findViewById(R.id.et_relation)
        tilRelation = view.findViewById(R.id.textInputLayout)

        view.findViewById<MaterialButton>(R.id.btn_confirm).setOnClickListener {
            if (validateEditText(etRelation.text.toString().trim())) {
                if (!activity.isNetworkConnected()) {
                    activity.onFailure(activity.findViewById(R.id.root_view), getString(R.string.no_internet_connectivity))
                    return@setOnClickListener
                }
                activity.viewModel.emergencyRelation = etRelation.text.toString()
                activity.onRelationSubmit()
            }
        }
    }
    private fun validateEditText(editText: String): Boolean {
        if (TextUtils.isEmpty(editText)) {
            etRelation.requestFocus()
            tilRelation.setErrorMessage(getString(R.string.empty_value))
            return false
        } else {
            tilRelation.clearErrorMessage()
            return true
        }

    }
}