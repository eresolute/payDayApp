package com.fh.payday.views2.shared.custom

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.design.button.MaterialButton
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.WindowManager
import com.fh.payday.BuildConfig
import com.fh.payday.R
import com.fh.payday.views2.auth.LoginActivity
import com.fh.payday.views2.kyc.KycActivity

class EmiratesExpiredFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        if (activity == null) {
            throw IllegalStateException("Invalid State; getActivity() is null")
        }

        val inflater = activity!!.layoutInflater
        val view = inflater.inflate(R.layout.dialog_emirates_expire, null)
        val builder = AlertDialog.Builder(activity!!)
        builder.setView(view).setCancelable(false)

        val dialog = builder.create()

        if (dialog.window != null) {
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        view.findViewById<MaterialButton>(R.id.btn_update).setOnClickListener {
            val intent = Intent(activity, KycActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra("index", 3)
            intent.putExtra("expired", true)
            startActivity(intent)
            activity!!.finish()
        }

        view.findViewById<MaterialButton>(R.id.btn_cancel).setOnClickListener {
            val intent = Intent(activity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            activity!!.finish()
        }

        if (!BuildConfig.DEBUG)
            dialog.window?.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)


        return dialog
    }
}