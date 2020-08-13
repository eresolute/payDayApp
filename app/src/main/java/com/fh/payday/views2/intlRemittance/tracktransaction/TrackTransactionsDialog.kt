package com.fh.payday.views2.intlRemittance.tracktransaction

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.fh.payday.R

class TrackTransactionsDialog : DialogFragment() {

    private lateinit var btnViewDetails: Button
    private lateinit var btnOkay: Button
    private var negativeBlock: () -> Unit = {}
    private var positiveBlock: () -> Unit = {}

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = activity?.layoutInflater
        @SuppressLint("InflateParams")
        val v = inflater?.inflate(R.layout.dialog_track_transaction, null)

        val builder = AlertDialog.Builder(activity ?: return dialog)
        builder.setView(v).setCancelable(true)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setGravity(Gravity.CENTER)
        initView(v)
        return dialog
    }

    private fun initView(view: View?) {
        view ?: return
        val message = arguments?.getString(KEY_MESSAGE)
                ?: getString(R.string.track_request_initiated_successfully)
        val resIcon = arguments?.getInt(KEY_ICON) ?: R.drawable.ic_success_checked
        val gravity = arguments?.getInt(KEY_MESSAGE_GRAVITY, Gravity.START) ?: Gravity.CENTER
        val positiveText = arguments?.getString(KEY_POSITIVE_TEXT)
                ?: getString(R.string.view_details)
        val negativeText = arguments?.getString(KEY_NEGATIVE_TEXT) ?: getString(R.string.ok)

        view.findViewById<TextView>(R.id.tv_title).apply {
            text = message
            this.gravity = gravity
        }

        view.findViewById<ImageView>(R.id.img_alert).apply {
            setImageResource(resIcon)
        }
        btnViewDetails = view.findViewById<Button>(R.id.btn_viewDetails).apply {
            text = positiveText
            setOnClickListener { dismiss(); positiveBlock() }
        }
        btnOkay = view.findViewById<Button>(R.id.btn_okay).apply {
            text = negativeText
            setOnClickListener { dismiss(); negativeBlock() }
        }
    }

    class Builder {
        private var message: String? = null
        private var resIcon: Int? = null
        private var messageGravity = Gravity.CENTER
        private var positiveText: String? = null
        private var negativeText: String? = null
        private var negativeBlock: () -> Unit = {}
        private var positiveBlock: () -> Unit = {}

        fun setMessage(text: String): Builder {
            message = text
            return this
        }

        fun setResIcon(icon: Int): Builder {
            resIcon = icon
            return this
        }

        fun setMessageGravity(gravity: Int): Builder {
            messageGravity = gravity
            return this
        }

        fun setPositiveText(text: String): Builder {
            positiveText = text
            return this
        }

        fun setNegativeText(text: String): Builder {
            negativeText = text
            return this
        }

        fun attachNegativeListener(block: () -> Unit): Builder {
            negativeBlock = block
            return this
        }

        fun attachPositiveListener(block: () -> Unit): Builder {
            positiveBlock = block
            return this
        }

        fun build(): TrackTransactionsDialog {
            val fragment = TrackTransactionsDialog().apply {
                val bundle = Bundle()
                bundle.putInt(KEY_MESSAGE_GRAVITY, messageGravity)
                message?.let { bundle.putString(KEY_MESSAGE, it) }
                resIcon?.let { bundle.putInt(KEY_ICON, it) }
                positiveText?.let { bundle.putString(KEY_POSITIVE_TEXT, positiveText) }
                negativeText?.let { bundle.putString(KEY_NEGATIVE_TEXT, negativeText) }
                arguments = bundle
            }
            fragment.negativeBlock = negativeBlock
            fragment.positiveBlock = positiveBlock

            return fragment
        }
    }

    companion object {
        private const val KEY_MESSAGE = "message"
        private const val KEY_ICON = "icon"
        private const val KEY_MESSAGE_GRAVITY = "message_gravity"
        private const val KEY_POSITIVE_TEXT = "positive_text"
        private const val KEY_NEGATIVE_TEXT = "negative_text"
    }
}