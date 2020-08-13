package com.fh.payday.views2.notification

import android.support.design.button.MaterialButton
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.datasource.models.Notification
import com.fh.payday.utilities.DateTime

class NotificationAdapter(private val notifications: List<Notification>,
                          private val listener: (Int) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val category = "CAMPAIGN NOTIFICATION"

    enum class TYPE { LOAN, OTHERS }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        return if (viewType == TYPE.LOAN.ordinal) {
            view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification_loan, parent, false)
            LoanViewHolder(view)
        } else {
            view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
            OtherViewHolder(view)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE.LOAN.ordinal) {
            (holder as LoanViewHolder).bindTo(notifications[position], listener, position)
        } else (holder as OtherViewHolder).bindTo(notifications[position])

    }

    override fun getItemCount() = notifications.size

    override fun getItemViewType(position: Int): Int {
        return if (notifications[position].category == category)
            TYPE.LOAN.ordinal
        else
            TYPE.OTHERS.ordinal
    }

    class LoanViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        private val tvDescription = view.findViewById<TextView>(R.id.tv_description)
        private val tvTime = view.findViewById<TextView>(R.id.tv_time)
        private val btnView = view.findViewById<MaterialButton>(R.id.btn_view)
        fun bindTo(item: Notification, listener: (Int) -> Unit, position: Int) {
            tvTitle.text = item.category
            tvDescription.text = item.notificationBody
            tvTime.text = DateTime.parse(item.notificationDate, "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "dd MMM yyyy hh:mm a")
            btnView.setOnClickListener {
                listener(position)
            }
        }
    }

    class OtherViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val tvTitle = view.findViewById<TextView>(R.id.tv_title)
        private val tvDescription = view.findViewById<TextView>(R.id.tv_description)
        private val tvTime = view.findViewById<TextView>(R.id.tv_time)
        fun bindTo(item: Notification) {
            tvTitle.text = item.category
            tvDescription.text = item.notificationBody
            tvTime.text = DateTime.parse(item.notificationDate, "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "dd MMM yyyy hh:mm a")
        }
    }


}