package com.fh.payday.views2.loan.apply

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.datasource.models.shared.ListModel
import com.fh.payday.utilities.setTextWithUnderLine
import com.fh.payday.views.shared.ListAdapter
import kotlinx.android.synthetic.main.dialog_loan_eligibility.view.*

class LoanEligibilityDialogFragment : DialogFragment() {

    private lateinit var btnInterested: Button
    private lateinit var btnNotInterested: Button
    private lateinit var checkbox: CheckBox
    private lateinit var termsLink: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvError: TextView
    private var negativeBlock: () -> Unit = {}
    private var positiveBlock: () -> Unit = {}
    private var linkBlock: () -> Unit = {}

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = activity?.layoutInflater
        @SuppressLint("InflateParams")
        val v = inflater?.inflate(R.layout.dialog_loan_eligibility, null)

        val builder = AlertDialog.Builder(activity ?: return dialog)
        builder.setView(v).setCancelable(true)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setGravity(Gravity.CENTER)

        initView(v)

        val items = arguments?.getParcelableArrayList<ListModel>("items") ?: return dialog
        recyclerView.adapter = ListAdapter(items, this.context, "")

        return dialog
    }

    private fun initView(view: View?) {
        view ?: return
        recyclerView = view.findViewById(R.id.recycler_view)
        btnInterested = view.findViewById(R.id.btn_interested)
        btnNotInterested = view.findViewById(R.id.btn_not_interested)
        checkbox = view.findViewById(R.id.cb_terms_conditions)
        termsLink = view.findViewById(R.id.tv_terms_condition_link)
        tvError = view.findViewById(R.id.tvError)

        view.tv_loan_top_note.visibility = arguments?.getInt("note_visibility", View.GONE) ?: View.GONE

        termsLink.setTextWithUnderLine(termsLink.text.toString())

//        btnInterested.isEnabled = false

        checkbox.visibility = View.VISIBLE
        termsLink.visibility = View.VISIBLE

        checkbox.setOnCheckedChangeListener { _, isChecked ->
            tvError.visibility = View.GONE
//            btnInterested.isEnabled = isChecked
        }

        termsLink.setOnClickListener {
            linkBlock()
        }

        btnInterested.setOnClickListener {
            if (!checkbox.isChecked) {
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            dismiss()
            positiveBlock()
        }
        btnNotInterested.setOnClickListener {
            dismiss()
            negativeBlock()
        }
    }

    class Builder {
        private val items = ArrayList<ListModel>()
        private var negativeBlock: () -> Unit = {}
        private var positiveBlock: () -> Unit = {}
        private var linkBlock: () -> Unit = {}
        private var noteVisibility: Int = View.GONE

        fun add(key: String, value: String): Builder {
            items.add(ListModel(key, value))
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

        private fun setNoteVisibility(visibility: Int): Builder {
            noteVisibility = visibility
            return this
        }

        fun showNote(visible: Boolean): Builder {
            return when (visible) {
                true -> setNoteVisibility(View.VISIBLE)
                else -> setNoteVisibility(View.GONE)
            }
        }

        fun build(): LoanEligibilityDialogFragment {
            require(items.isNotEmpty()) {
                "Items cannot be empty. You need to add items"
            }

            val fragment = LoanEligibilityDialogFragment()
            val bundle = Bundle().apply {
                putInt("note_visibility", noteVisibility)
            }
            bundle.putParcelableArrayList("items", items)
            fragment.arguments = bundle
            fragment.negativeBlock = negativeBlock
            fragment.positiveBlock = positiveBlock
            fragment.linkBlock = linkBlock

            return fragment
        }
    }

}