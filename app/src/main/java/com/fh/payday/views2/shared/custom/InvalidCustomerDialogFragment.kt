package com.fh.payday.views2.shared.custom

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.design.button.MaterialButton
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.views2.intlRemittance.InternationalRemittanceActivity
import com.fh.payday.views2.intlRemittance.rateCalculator.RateCalculatorActivity
import com.fh.payday.views2.locator.BranchLocationActivity

class InvalidCustomerDialogFragment : DialogFragment() {
    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        activity ?: throw IllegalStateException("Invalid State; getActivity() is null")
        val activity = when (activity) {
            is InternationalRemittanceActivity -> activity as InternationalRemittanceActivity
            is RateCalculatorActivity -> activity as RateCalculatorActivity
            else -> null
        }

        val inflater = activity!!.layoutInflater
        val view = inflater.inflate(R.layout.dialog_invalid_customer, null)
        val title = view.findViewById<TextView>(R.id.tv_eligible_title)
        val builder = AlertDialog.Builder(activity)
        builder.setView(view).setCancelable(false)

        title.text = arguments?.getString("message")
        val dialog = builder.create()

        if (dialog.window != null) {
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        view.findViewById<MaterialButton>(R.id.btn_locate_exchange).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(activity, BranchLocationActivity::class.java)
            intent.putExtra("issue", "uae_exchange")
                .putExtra("allowed", false)
            startActivity(intent)
        }

        view.findViewById<MaterialButton>(R.id.btn_dismiss).setOnClickListener {
            dialog.dismiss()
        }

        return dialog
    }
}