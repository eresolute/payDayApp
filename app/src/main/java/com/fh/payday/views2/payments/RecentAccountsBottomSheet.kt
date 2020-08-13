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
import com.fh.payday.datasource.models.payments.RecentAccount
import com.fh.payday.utilities.DateTime
import com.fh.payday.utilities.GlideApp
import com.fh.payday.views2.shared.custom.RoundedBottomSheetDialogFragment
import kotlinx.android.synthetic.main.layout_recent_accounts.*


class RecentAccountsBottomSheet : RoundedBottomSheetDialogFragment() {

    private var block: (String) -> Unit = {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_recent_accounts, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recentAccounts = arguments?.getParcelableArrayList<RecentAccount>("recent_accounts")
                ?: return
        val resId = arguments?.getInt("res_id", 0)

        if (resId != 0 && resId != null) {
            recycler_view.adapter = RecentAccountAdapter(recentAccounts, resId) {
                dismiss()
                block(it)
            }
        } else {
            recycler_view.adapter = RecentAccountAdapter(recentAccounts) {
                dismiss()
                block(it)
            }
        }
    }


    companion object {
        @JvmOverloads
        fun newInstance(
                recentAccounts: ArrayList<RecentAccount>,
                @DrawableRes resId: Int? = null,
                block: (String) -> Unit
        ): RecentAccountsBottomSheet {
            val bottomSheet = RecentAccountsBottomSheet()
            val bundle = Bundle()
            bundle.putParcelableArrayList("recent_accounts", recentAccounts)
            resId?.let { bundle.putInt("res_id", resId) }
            bottomSheet.arguments = bundle
            bottomSheet.block = block

            return bottomSheet
        }
    }
}

private class RecentAccountAdapter(
        private val recentList: List<RecentAccount>,
        @DrawableRes private val resId: Int = R.drawable.ic_operator,
        private val block: (String) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, item: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.recent_account_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = recentList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(recentList[position], resId, View.OnClickListener {
            block(recentList[position].accountNo)
        })
    }
}

private class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val imageView = view.findViewById<ImageView>(R.id.image_view)
    private val tvAccountNo = view.findViewById<TextView>(R.id.tv_account_no)
    private val tvDate = view.findViewById<TextView>(R.id.tv_date)
    private val parent = view.findViewById<View>(R.id.parent)

    fun bind(recent: RecentAccount, @DrawableRes resId: Int, listener: View.OnClickListener) {
        recent.logo?.let {
            GlideApp.with(imageView)
                    .load(it)
                    .placeholder(resId)
                    .error(resId)
                    .into(imageView)
        } ?: imageView.setImageResource(resId)

        tvAccountNo.text = recent.accountNo
        val paidOn = DateTime.parse(recent.tranDate, "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                "dd MMMM yyyy")
        tvDate.text = String.format(tvDate.context.getString(R.string.paid_on), paidOn)
        parent.setOnClickListener(listener)
    }
}