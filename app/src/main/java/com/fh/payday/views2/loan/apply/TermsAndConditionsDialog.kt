package com.fh.payday.views2.loan.apply

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
import android.widget.CheckBox
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.utilities.setTextWithUnderLine

class TermsAndConditionsDialog : DialogFragment() {

    private lateinit var btnAccept: Button
    private lateinit var btnCancel: Button
    private lateinit var checkbox: CheckBox
    private lateinit var termsLink: TextView
    private lateinit var tvError: TextView
    private var linkBlock: () -> Unit = {}
    private var negativeBlock: () -> Unit = {}
    private var positiveBlock: () -> Unit = {}

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = activity?.layoutInflater
        @SuppressLint("InflateParams")
        val v = inflater?.inflate(R.layout.dialog_loan_terms_and_conditions, null)
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

        val message = arguments?.getString(KEY_MESSAGE) ?: getString(R.string.loan_terms_conditions)
        val gravity = arguments?.getInt(KEY_MESSAGE_GRAVITY, Gravity.START) ?: Gravity.START
        val positiveText = arguments?.getString(KEY_POSITIVE_TEXT) ?: getString(R.string.apply_pop_up)
        val negativeText = arguments?.getString(KEY_NEGATIVE_TEXT) ?: getString(R.string.cancel)
        val mTermConditionsLink = arguments?.getBoolean(KEY_TERMS_CONDITIONS_FLAG) ?: false

        view.findViewById<TextView>(R.id.tv_terms_conditions).apply {
            text = message
            this.gravity = gravity
        }

        checkbox = view.findViewById(R.id.cb_terms_conditions)
        termsLink = view.findViewById(R.id.tv_terms_condition_link)
        tvError = view.findViewById(R.id.tvError)

        termsLink.setTextWithUnderLine(termsLink.text.toString())


        checkbox.setOnCheckedChangeListener { _, isChecked ->
            tvError.visibility = View.GONE
        }

        termsLink.setOnClickListener {
            linkBlock()
        }

        if (mTermConditionsLink) {
            checkbox.visibility = View.VISIBLE
            termsLink.visibility = View.VISIBLE
        }

        btnAccept = view.findViewById<Button>(R.id.btn_accept).apply {
            text = positiveText
            setOnClickListener {
                if (mTermConditionsLink) {
                    if (!checkbox.isChecked) {
                        tvError.visibility = View.VISIBLE
                        return@setOnClickListener
                    }
                }
                dismiss(); positiveBlock()
            }
        }

        btnCancel = view.findViewById<Button>(R.id.btn_cancel).apply {
            text = negativeText
            setOnClickListener { dismiss(); negativeBlock() }
        }
    }

    class Builder {
        private var message: String? = null
        private var messageGravity = Gravity.START
        private var positiveText: String? = null
        private var negativeText: String? = null
        private var termsAndConditionsFlag: Boolean = false
        private var negativeBlock: () -> Unit = {}
        private var positiveBlock: () -> Unit = {}
        private var linkBlock: () -> Unit = {}

        fun setMessage(text: String): Builder {
            message = text
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

        fun setTermsConditionsLink(isTermsConditions: Boolean): Builder {
            termsAndConditionsFlag = isTermsConditions
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

        fun attachLinkListener(block: () -> Unit): Builder {
            linkBlock = block
            return this
        }

        fun build(): TermsAndConditionsDialog {
            val fragment = TermsAndConditionsDialog().apply {
                val bundle = Bundle()
                bundle.putInt(KEY_MESSAGE_GRAVITY, messageGravity)
                message?.let { bundle.putString(KEY_MESSAGE, it) }
                positiveText?.let { bundle.putString(KEY_POSITIVE_TEXT, positiveText) }
                negativeText?.let { bundle.putString(KEY_NEGATIVE_TEXT, negativeText) }
                bundle.putBoolean(KEY_TERMS_CONDITIONS_FLAG, termsAndConditionsFlag)
                arguments = bundle
            }
            fragment.negativeBlock = negativeBlock
            fragment.positiveBlock = positiveBlock
            fragment.linkBlock = linkBlock

            return fragment
        }
    }

    companion object {
        private const val KEY_MESSAGE = "message"
        private const val KEY_MESSAGE_GRAVITY = "message_gravity"
        private const val KEY_POSITIVE_TEXT = "positive_text"
        private const val KEY_NEGATIVE_TEXT = "negative_text"
        private const val KEY_TERMS_CONDITIONS_FLAG = "terms_conditions"
    }
}