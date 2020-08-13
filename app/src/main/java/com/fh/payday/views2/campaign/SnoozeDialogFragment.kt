package com.fh.payday.views2.campaign

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import com.fh.payday.R
import java.util.*

class SnoozeDialogFragment : DialogFragment() {

    private var clickListenerBlock: (String) -> Unit = {}

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (activity == null) {
            throw IllegalStateException("Invalid State; getActivity() is null")
        }
        val inflater = activity!!.layoutInflater
        @SuppressLint("InflateParams") val view = inflater.inflate(R.layout.fragment_dialog_snooze, null)

        val builder = AlertDialog.Builder(activity ?: return dialog)
        builder.setView(view).setCancelable(true)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setGravity(Gravity.CENTER)

        val days = arguments?.getStringArrayList("daysList") ?: return dialog

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView?.adapter = SnoozeAdapter(days) {
            dialog.dismiss()
            clickListenerBlock(it)
        }

        return dialog
    }

    class Builder {
        private var days = ArrayList<String>()
        private var clickListenerBlock: (String) -> Unit = {}

        fun days(daysList: ArrayList<String>): Builder {
            days = daysList
            return this
        }

        fun attachClickListener(block: (String) -> Unit): Builder {
            clickListenerBlock = block
            return this
        }

        fun build(): SnoozeDialogFragment {

            require(days.isNotEmpty()) {
                "Items cannot be empty. You need to add items"
            }

            val fragment = SnoozeDialogFragment()
            val bundle = Bundle()
            bundle.putStringArrayList("daysList", days)
            fragment.arguments = bundle
            fragment.clickListenerBlock = clickListenerBlock
            return fragment

        }

    }
}