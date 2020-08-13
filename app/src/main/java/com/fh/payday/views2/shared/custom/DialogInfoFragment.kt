package com.fh.payday.views2.shared.custom

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.fh.payday.BuildConfig
import com.fh.payday.R
import com.fh.payday.datasource.models.shared.ListModel
import com.fh.payday.views.shared.ListAdapter

class DialogInfoFragment : DialogFragment() {

    private var cancelAction: () -> Unit = {}
    private var confirmAction: () -> Unit = {}

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = activity?.layoutInflater
        @SuppressLint("InflateParams")
        val view = inflater?.inflate(R.layout.fragment_dialog_itemed_info, null)

        val builder = AlertDialog.Builder(activity ?: return dialog)
        builder.setView(view).setCancelable(true)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setGravity(Gravity.CENTER)

        init(view)

        if (!BuildConfig.DEBUG)
            dialog.window?.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)


        return dialog
    }

    fun setConfirmAction(block: () -> Unit): DialogInfoFragment {
        confirmAction = block
        return this
    }

    private fun init(view: View?) {
        view ?: return
        view.findViewById<Button>(R.id.btn_confirm).apply {
            setOnClickListener { dismiss(); confirmAction() }
            text = arguments?.getString("confirm_text") ?: text
        }
        view.findViewById<Button>(R.id.btn_cancel).apply {
            setOnClickListener { dismiss(); cancelAction() }
            text = arguments?.getString("cancel_text") ?: text
        }

        view.findViewById<Button>(R.id.btn_confirm).apply {
            visibility = if (arguments?.getBoolean("isVisible") != false) View.VISIBLE else View.GONE
        }
        view.findViewById<TextView>(R.id.tv_title).apply {
            text = arguments?.getString("title") ?: getString(R.string.confirmation)
        }
        view.findViewById<RecyclerView>(R.id.recycler_view).apply {
            val items = arguments?.getParcelableArrayList<ListModel>("items") ?: return
            adapter = ListAdapter(items, this.context, "")
        }
    }

    companion object {

        fun getInstance(
            title: String,
            items: ArrayList<ListModel>,
            isVisible: Boolean = true,
            confirmText: String? = null,
            cancelText: String? = null
        ): DialogInfoFragment {
            val bundle = Bundle().apply {
                putString("title", title)
                putParcelableArrayList("items", items)
                putBoolean("isVisible", isVisible)
                confirmText?.let { putString("confirm_text", it) }
                cancelText?.let { putString("cancel_text", it) }
            }

            return DialogInfoFragment().apply { arguments = bundle }
        }
    }

}