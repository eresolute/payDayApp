package com.fh.payday.views2.payments

import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.datasource.models.payments.Beneficiaries
import com.fh.payday.utilities.GlideApp
import com.fh.payday.views2.shared.custom.RoundedBottomSheetDialogFragment
import kotlinx.android.synthetic.main.layout_recent_accounts.*


class BeneficiaryBottomSheet : RoundedBottomSheetDialogFragment() {

    private var block: ((String, String?, String?) -> Unit) = { _, _, _ -> Unit }
    private var editAction: (Beneficiaries) -> Unit = {}
    private var deleteAction: (Beneficiaries) -> Unit = {}
    private var popUpAction: (Beneficiaries, View) -> Unit = { _, _ -> Unit }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_recent_accounts, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val beneficiaries = arguments?.getParcelableArrayList<Beneficiaries>("beneficiaries_number")
                ?: return
        val resId = arguments?.getInt("res_id", 0)

        if (resId != 0 && resId != null) {
            recycler_view.adapter = BeneficiaryAdapter(
                    beneficiaries,
                    resId,
                    popUpAction,
                    editAction,
                    deleteAction
            ) { s: String, s1: String, s2: String ->
                dismiss()
                block(s, s1, s2)
            }
        } else {
            recycler_view.adapter = BeneficiaryAdapter(
                    beneficiaries,
                    popUpAction = popUpAction,
                    editAction = editAction,
                    deleteAction = deleteAction
            ) { s: String, s1: String, s2: String ->
                dismiss()
                block(s, s1, s2)
            }
        }
    }

    fun attachEditListener(action: (Beneficiaries) -> Unit) {
        editAction = action
    }

    fun attachDeleteListener(action: (Beneficiaries) -> Unit) {
        deleteAction = action
    }

    fun attachPopUpListener(action: (Beneficiaries, View) -> Unit) {
        popUpAction = action
    }

    companion object {
        @JvmOverloads
        fun newInstance(
                beneficiaries: ArrayList<Beneficiaries>,
                @DrawableRes resId: Int? = null,
                block: (String, String?, String?) -> Unit
        ): BeneficiaryBottomSheet {
            val bottomSheet = BeneficiaryBottomSheet()
            val bundle = Bundle()
            bundle.putParcelableArrayList("beneficiaries_number", beneficiaries)
            resId?.let { bundle.putInt("res_id", resId) }
            bottomSheet.arguments = bundle
            bottomSheet.block = block
            return bottomSheet
        }
    }
}

class BeneficiaryAdapter(
        private val beneficiariesList: ArrayList<Beneficiaries>,
        private val resId: Int = R.drawable.ic_operator,
        private val popUpAction: (Beneficiaries, View) -> Unit,
        private val editAction: (Beneficiaries) -> Unit,
        private val deleteAction: (Beneficiaries) -> Unit,
        private val block: (String, String, String) -> Unit
) : RecyclerView.Adapter<BeneficiaryAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, item: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.recent_account_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = beneficiariesList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(beneficiariesList[position], resId, View.OnClickListener {
            block(
                    beneficiariesList[position].mobileNumber,
                    beneficiariesList[position].optional1 ?: "",
                    beneficiariesList[position].optional2 ?: ""
            )
        }, popUpAction, editAction, deleteAction)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageView = view.findViewById<ImageView>(R.id.image_view)
        private val tvAccountNo = view.findViewById<TextView>(R.id.tv_account_no)
        private val tvName = view.findViewById<TextView>(R.id.tv_date)
        private val parent = view.findViewById<View>(R.id.parent)
        private val ivEdit = view.findViewById<View>(R.id.iv_edit)
        private val ivDelete = view.findViewById<View>(R.id.iv_delete)
        private val ivOption = view.findViewById<View>(R.id.iv_option)

        fun bind(
                beneficiary: Beneficiaries,
                @DrawableRes resId: Int,
                listener: View.OnClickListener,
                popUpAction: (Beneficiaries, View) -> Unit,
                editAction: (Beneficiaries) -> Unit,
                deleteAction: (Beneficiaries) -> Unit
        ) {
            beneficiary.logo?.let {
                GlideApp.with(imageView)
                        .load(it)
                        .placeholder(resId)
                        .error(resId)
                        .into(imageView)
            } ?: imageView.setImageResource(resId)

            if (!beneficiary.enabled) {
                tvName.text = beneficiary.shortName
                tvAccountNo.text = beneficiary.mobileNumber
                tvAccountNo.alpha = 0.3F
                tvName.alpha = 0.3F
                imageView.alpha = 0.3F
                ivOption.setOnClickListener { popUpAction(beneficiary, it) }
                ivEdit.setOnClickListener { editAction(beneficiary) }
                ivDelete.setOnClickListener { deleteAction(beneficiary) }
            } else {
                tvName.text = beneficiary.shortName
                tvAccountNo.text = beneficiary.mobileNumber
                parent.setOnClickListener(listener)
                ivOption.setOnClickListener { popUpAction(beneficiary, it) }
                ivEdit.setOnClickListener { editAction(beneficiary) }
                ivDelete.setOnClickListener { deleteAction(beneficiary) }
            }

        }
    }
}

