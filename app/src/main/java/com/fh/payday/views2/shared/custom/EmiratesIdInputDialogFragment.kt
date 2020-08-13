package com.fh.payday.views2.shared.custom

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.design.button.MaterialButton
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import com.fh.payday.R
import com.fh.payday.utilities.clearErrorMessage
import com.fh.payday.views2.intlRemittance.InternationalRemittanceActivity

class EmiratesIdInputDialogFragment : DialogFragment() {
    lateinit var etEmiratesId: TextInputEditText
    lateinit var tilEmiratesId: TextInputLayout
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        activity ?: throw IllegalStateException("Invalid State; getActivity() is null")
        val activity = activity as InternationalRemittanceActivity

        val inflater = activity.layoutInflater
        val view = inflater.inflate(R.layout.dialog_input_emirates_id, null)
//        val title = view.findViewById<TextView>(R.id.tv_eligible_title)
        etEmiratesId = view.findViewById(R.id.et_emirates_id)
        tilEmiratesId = view.findViewById(R.id.til_emirates_id)
        val builder = AlertDialog.Builder(activity!!)
        builder.setView(view).setCancelable(false)

        val dialog = builder.create()

        if (dialog.window != null) {
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        view.findViewById<MaterialButton>(R.id.btn_proceed).setOnClickListener {
            if (validate(etEmiratesId.text.toString())) {
                activity.sendEmiratesId(etEmiratesId.text.toString())
                dialog.dismiss()
            }
        }

        view.findViewById<MaterialButton>(R.id.btn_dismiss).setOnClickListener {
            dialog.dismiss()
            activity.finish()
        }

        etEmiratesId.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tilEmiratesId.clearErrorMessage()
            }

        })

        return dialog
    }

    private fun validate(emiratesId: String?): Boolean {
        if (emiratesId.isNullOrEmpty()) {
            tilEmiratesId.error = getString(R.string.empty_emirate_card_number)
            return false
        } else if (emiratesId.length != 9) {
            tilEmiratesId.error = getString(R.string.invalid_emirate_card_number)
            return false
        }
        return true
    }
}