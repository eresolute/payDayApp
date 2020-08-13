package com.fh.payday.views2.message.adapter

import android.annotation.SuppressLint
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.fh.payday.R
import com.fh.payday.datasource.models.message.MessageBody
import com.fh.payday.utilities.DateTime

class SuggestionCompAdapter(
        private val messageList: List<MessageBody>
) : RecyclerView.Adapter<SuggestionCompAdapter.ViewHolder>() {
    private var selectedPostion = -1
    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.suggestion_complaint_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.bindTo(messageList[position])

        holder.itemView.setOnClickListener {
            selectedPostion = position
            if (holder.constraintLayoutReply.visibility == View.VISIBLE) {
                holder.ivArrow.setImageResource(R.drawable.ic_arrow_right_grey)
                holder.constraintLayoutReply.visibility = View.GONE

            } else {
                holder.ivArrow.setImageResource(R.drawable.ic_arrow_down_blue)
                holder.constraintLayoutReply.visibility = View.VISIBLE
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private var tvDate: TextView = view.findViewById(R.id.tv_date)
        private var tvId: TextView = view.findViewById(R.id.tv_id)
        private var tvBody: TextView = view.findViewById(R.id.tv_msg_body)
        private var tvReplyDate: TextView = view.findViewById(R.id.tv_reply_date)
        private var tvStatus: TextView = view.findViewById(R.id.tv_reply_status)
        private var tvReply: TextView = view.findViewById(R.id.tv_reply)
        private var tvIssueSubject: TextView = view.findViewById(R.id.tv_issue_subject)
        private var tvIssueArea: TextView = view.findViewById(R.id.tv_issue_area)
        val constraintLayoutReply: ConstraintLayout = view.findViewById(R.id.reply_view)
        val ivArrow: ImageView = view.findViewById(R.id.iv_arrow)

        fun bindTo(item: MessageBody) {
            tvId.text = item.id.toString()
            tvDate.text = DateTime.parse(item.createdAt, "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "dd-MM-yyyy hh:mm a")
            tvBody.text = item.body
            tvReply.text = item.reply
            tvIssueSubject.text = item.subject
            tvIssueArea.text = item.issue
            if (item.reply.isNullOrEmpty() || item.reply == "null") {
                tvReplyDate.text = ""
                tvStatus.text = item.status
            } else {
                tvStatus.text = item.status
                tvReplyDate.text = DateTime.parse(item.updatedAt, "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "dd-MM-yyyy hh:mm a")
            }
        }
    }
}