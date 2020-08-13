package com.fh.payday.views2.shared.custom

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.fh.payday.BuildConfig
import com.fh.payday.R

class EligibilityDialogFragment : DialogFragment() {
    private var tvTitle: TextView? = null
    private var btn1: Button? = null
    private var btn2: Button? = null
    private var dismissListener: OnDismissListener? = null
    private var confirmListener: OnConfirmListener? = null
    private var cancelListener: OnCancelListener? = null
    private var cancellable = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        if (activity == null) {
            throw IllegalStateException("Invalid State; getActivity() is null")
        }

        val inflater = activity!!.layoutInflater
        @SuppressLint("InflateParams") val view = inflater.inflate(R.layout.dialog_eligibility, null)
        init(view)
        val builder = AlertDialog.Builder(activity!!)
        builder.setView(view).setCancelable(cancellable)
        val dialog = builder.create()
        if (dialog.window != null) {
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        val args = arguments ?: throw IllegalArgumentException("required arguments are null")

        val title = args.getString("title")
        if (TextUtils.isEmpty(title)) {
            tvTitle!!.visibility = View.GONE
        } else {
            tvTitle!!.text = title
        }
        btn1?.text = args.getString("btn1Text")
        btn2?.text = args.getString("btn2Text")
        dialog.setOnDismissListener { dialogInterface -> dismissListener!!.onDismiss(dialogInterface) }
        btn1?.setOnClickListener {
            confirmListener?.onConfirm(dialog)
        }
        btn2?.setOnClickListener {
            cancelListener?.onCancel(dialog)
        }

        if (!BuildConfig.DEBUG)
            dialog.window?.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)


        return dialog
    }

    private fun init(view: View) {
        tvTitle = view.findViewById(R.id.tv_eligible_title)
        btn1 = view.findViewById(R.id.btn_defer)
        btn2 = view.findViewById(R.id.btn_cancel)
    }

    private fun setDismissListener(listener: OnDismissListener) {
        dismissListener = listener
    }

    private fun setConfirmListener(listener: OnConfirmListener) {
        confirmListener = listener
    }

    private fun setCancelListener(listener: OnCancelListener) {
        cancelListener = listener
    }

    private fun isCancelable(cancelable: Boolean) {
        this.cancellable = cancelable
    }

    class Builder(private val mDismissListener: OnDismissListener) {
        private var mTitle: String? = null
        private var mBtnText1: String? = null
        private var mBtnText2: String? = null
        private var mIsCancelable = false
        private lateinit var mConfirmListener: OnConfirmListener
        private lateinit var mCancelListener: OnCancelListener

        fun setTitle(title: String): Builder {
            mTitle = title
            return this
        }

        fun setBtn1Text(text: String): Builder {
            mBtnText1 = text
            return this
        }

        fun setBtn2Text(text: String): Builder {
            mBtnText2 = text
            return this
        }

        fun setCancelable(cancelable: Boolean): Builder {
            mIsCancelable = cancelable
            return this
        }

        fun setConfirmListener(listener: OnConfirmListener): Builder {
            mConfirmListener = listener
            return this
        }

        fun setCancelListener(listener: OnCancelListener): Builder {
            mCancelListener = listener
            return this
        }

        fun build(): EligibilityDialogFragment {
            val fragment = EligibilityDialogFragment()
            val bundle = Bundle()
            bundle.apply {
                putString("title", if (mTitle != null) mTitle else "")
                putString("btn1Text", if (mBtnText1 != null) mBtnText1 else "")
                putString("btn2Text", if (mBtnText2 != null) mBtnText2 else "")
            }
            fragment.arguments = bundle
            fragment.isCancelable(mIsCancelable)
            fragment.isCancelable = mIsCancelable
            fragment.setDismissListener(mDismissListener)
            fragment.setConfirmListener(mConfirmListener)
            fragment.setCancelListener(mCancelListener)
            return fragment
        }
    }

    interface OnDismissListener {
        fun onDismiss(dialog: DialogInterface)
    }

    interface OnConfirmListener {
        fun onConfirm(dialog: DialogInterface)
    }

    interface OnCancelListener {
        fun onCancel(dialog: DialogInterface)
    }
}