package com.fh.payday.views2.shared.custom

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.fh.payday.BuildConfig
import com.fh.payday.R

class ConfirmDialogFragment : DialogFragment() {

    private lateinit var tvConfirmText: TextView
    private lateinit var tvConfirm: TextView
    private lateinit var ivClose: ImageView
    private var dismissListener: OnDismissListener? = null
    private var confirmListener: OnConfirmListener? = null
    private var cancellable = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (activity == null) {
            throw IllegalStateException("Invalid State; getActivity() is null")
        }
        val inflater = activity!!.layoutInflater
        @SuppressLint("InflateParams") val view = inflater.inflate(R.layout.dialog_confirm, null)
        val builder = AlertDialog.Builder(activity!!)
        builder.setView(view).setCancelable(cancellable)
        init(view)

        val args = arguments ?: throw IllegalArgumentException("required arguments are null")


        val dialog = builder.create()
        if (dialog.window != null) {
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        val title = args.getString("title")
        if (TextUtils.isEmpty(title)) {
            tvConfirmText.visibility = View.GONE
        } else {
            tvConfirmText.text = title
        }

        tvConfirm.text = args.getString("confirmText")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            tvConfirmText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, args.getInt("icon"), 0, 0)
        } else {
            tvConfirmText.setCompoundDrawablesWithIntrinsicBounds(0, args.getInt("icon"), 0, 0)
        }

        tvConfirm.setOnClickListener {
            confirmListener?.onConfirm(dialog)
        }

        ivClose.setOnClickListener {
            dismissListener?.onDismiss(dialog)
            dialog.dismiss()
        }

        if (!BuildConfig.DEBUG)
            dialog.window?.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)


        return dialog
    }

    private fun setDismissListener(listener: OnDismissListener) {
        dismissListener = listener
    }

    private fun setConfirmListener(listener: OnConfirmListener) {
        confirmListener = listener
    }


    private fun isCancelable(cancelable: Boolean) {
        this.cancellable = cancelable
    }


    class Builder {
        private var mTitle: String? = null
        private var confirmText: String? = null
        private var mIcon: Int = R.drawable.ic_call_center_number_large
        private var mIsCancelable = false
        private var mDismissListener: OnDismissListener = object : OnDismissListener {
            override fun onDismiss(dialog: DialogInterface) {
                dialog.dismiss()
            }
        }
        private lateinit var mConfirmListener: OnConfirmListener

        fun setTitle(title: String): Builder {
            mTitle = title
            return this
        }

        fun setConfirmText(text: String): Builder {
            confirmText = text
            return this
        }

        fun setIcon(icon: Int): Builder {
            mIcon = icon
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

        fun setDismissListener(listener: OnDismissListener): Builder {
            mDismissListener = listener
            return this
        }

        fun build(): ConfirmDialogFragment {
            val fragment = ConfirmDialogFragment()
            val bundle = Bundle()
            bundle.apply {
                putString("title", if (mTitle != null) mTitle else "")
                putString("confirmText", if (confirmText != null) confirmText else "")
                putInt("icon", mIcon)
            }
            fragment.arguments = bundle
            fragment.isCancelable(mIsCancelable)
            fragment.isCancelable = mIsCancelable
            fragment.setDismissListener(mDismissListener)
            fragment.setConfirmListener(mConfirmListener)
            return fragment
        }
    }

    private fun init(view: View) {
        tvConfirmText = view.findViewById(R.id.tv_confirm_text)
        tvConfirm = view.findViewById(R.id.tv_confirm)
        ivClose = view.findViewById(R.id.image_view)
    }


    interface OnDismissListener {
        fun onDismiss(dialog: DialogInterface)
    }

    interface OnConfirmListener {
        fun onConfirm(dialog: DialogInterface)
    }
}