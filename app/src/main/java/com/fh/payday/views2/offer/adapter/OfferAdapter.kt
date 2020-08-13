package com.fh.payday.views2.offer.adapter;

import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.fh.payday.R
import com.fh.payday.datasource.models.OfferResponse
import com.fh.payday.utilities.BASE_URL
import com.fh.payday.utilities.GlideApp
import com.fh.payday.utilities.OnItemClickListener
import com.fh.payday.views2.offer.OfferActivity

//class OfferAdapter(private val offerList: List<OfferResponse>, private val onOfferListener: OfferActivity.OnOfferClickListener) : RecyclerView.Adapter<OfferAdapter.ViewHolder>() {
//
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_offer, parent, false)
//        return ViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val item = offerList[position]
//        holder.bindTo(item, onOfferListener)
//    }
//
//    override fun getItemCount() = offerList.size
//
//    open inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//
//        private val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
//        private val ivOffer: ImageView = itemView.findViewById(R.id.iv_offer)
//        private val progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar)
//        fun bindTo(item: OfferResponse, onOfferListener: OfferActivity.OnOfferClickListener) {
//
//            progressBar.visibility = View.VISIBLE
//            tvTitle.text = item.bannerTitle
//            GlideApp.with(itemView)
//                    .load(Uri.parse(BASE_URL+"/" + item.fileUrl))
//                    .listener(object: RequestListener<Drawable> {
//                        override fun onLoadFailed(e: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, isFirstResource: Boolean): Boolean {
//                           progressBar.visibility = View.GONE
//                            return false
//                        }
//
//                        override fun onResourceReady(resource: Drawable?, model: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, dataSource: com.bumptech.glide.load.DataSource?, isFirstResource: Boolean): Boolean {
//                            progressBar.visibility = View.GONE
//                            return false
//                        }
//
//
//                    })
//                    .into(ivOffer)
//
//            ivOffer.setOnClickListener { onOfferListener.onOfferClick(item.fileUrl) }
//        }
//    }
//
//}